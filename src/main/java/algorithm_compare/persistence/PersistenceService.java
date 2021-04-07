package algorithm_compare.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static algorithm_compare.persistence.GraphData.TwoEndpointEdge;

public class PersistenceService implements IPersistenceService {

	public static final String PATH_TO_RESOURCES = "./src/main/resources/persistence/networks/";

	// Where the times are stored
	private static final String PATH_TO_DATA = "./src/main/resources/persistence/networkData/";

	private String rootDirectory;

	private Map<String, Network> networks;

	/**
	 * Creates a persistence service with the default path to resources
	 */
	public PersistenceService() throws IOException {
		this(PATH_TO_RESOURCES);
	}

	public PersistenceService(String rootDirectory) throws IOException {
		this.rootDirectory = rootDirectory;
		networks = new HashMap<String, Network>();
		getAllNetworkNames();
	}

	/**
	 * Finds what networks are available, loads their data and returns their names
	 */
	@Override
	public List<String> getAllNetworkNames() throws IOException {
		List<String> names = new ArrayList<>();
		try {
			Files.walk(Paths.get(rootDirectory)).sequential().filter((path) -> {
				return !(new File(path.toUri()).isDirectory());
			}).forEach((path) -> {
				String name = path.getFileName().toString();
				names.add(name);
				try {
					loadMetadata(name, path.toAbsolutePath().toString());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		} catch (RuntimeException e) {
			throw new IOException(e);
		}
		return names;
	}

	private void loadMetadata(String name, String path) throws IOException {
		if (networks.containsKey(name)) return;
		File directory = new File(PATH_TO_DATA);
		if (directory.exists()) {
			File metadata = new File(PATH_TO_DATA + name);
			if (metadata.exists()) {
				try {
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(metadata));
					Network net = (Network) ois.readObject();
					net.setPath(path);
					networks.put(name, net);
					ois.close();
				} catch (ClassNotFoundException e) {
					System.err.println("Couldn't read metadata: " + e);
				}
			} else {
				Network net = new Network(name);
				net.setPath(path);
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(metadata));
				oos.writeObject(net);
				oos.close();
				networks.put(name, net);
			}
		} else {
			throw new IOException("BAD METADATA DIRECTORY");
		}
	}

	private String getRootDirectorySlash() {
		return (rootDirectory.endsWith("/") ? rootDirectory : rootDirectory + "/");
	}

	@Override
	public GraphData loadNetwork(String name) {
		try {
			FastReader fr = new FastReader(getRootDirectorySlash() + name);
			GraphData gd = readNetwork(fr);
			fr.close();
			return gd;
		} catch (IOException e) {
			System.err.println("Error reading file " + name + ": " + e);
			return null;
		}
	}

	private GraphData readNetwork(FastReader fr) throws IOException {
		// First we read the problem line
		int[] values = dimacsProblemLine(fr);
		int n = values[0];
		int m = values[1];
		// Then we read source and terminus
		String[] nodeDescriptor1 = dimacsNextLine(fr);
		String[] nodeDescriptor2 = dimacsNextLine(fr);
		if (!(nodeDescriptor1.length == 3) || !nodeDescriptor1[0].equals("n") || !(nodeDescriptor2.length == 3)
				|| !nodeDescriptor2[0].equals("n")) {
			throw new IOException("Bad DIMACS format: can't read node descriptors");
		}
		int s = 0, t = 0;
		if (nodeDescriptor1[2].equals("s") && nodeDescriptor2[2].equals("t")) {
			s = Integer.parseInt(nodeDescriptor1[1]);
			t = Integer.parseInt(nodeDescriptor2[1]);
		} else if (nodeDescriptor1[2].equals("t") && nodeDescriptor2[2].equals("s")) {
			t = Integer.parseInt(nodeDescriptor1[1]);
			s = Integer.parseInt(nodeDescriptor2[1]);
		} else {
			throw new IOException("Bad DIMACS format: can't read node descriptors");
		}
		// Then we read edges
		TwoEndpointEdge[] edges = new TwoEndpointEdge[m];
		int[] edge;
		for (int i = 0; i < m; i++) {
			edge = dimacsReadEdge(fr);
			edges[i] = new TwoEndpointEdge(edge[1], edge[2], edge[3]);
		}
		return new GraphData(n, s, t, edges);
	}

	private String[] dimacsNextLine(FastReader br) throws IOException {
		String line = br.readLine();
		while (line.equals("") || line.startsWith("c")) {
			line = br.readLine();
		}
		return line.split(" ");
	}

	private int[] dimacsProblemLine(FastReader fr) throws IOException {
		String line = fr.readLine();
		while (line.equals("") || line.startsWith("c")) {
			line = fr.readLine();
		}
		String[] str = line.split(" ");
		if (!(str.length == 4) || !str[0].equals("p") || !str[1].equals("max")) {
			throw new IOException("Bad DIMACS format: can't read problem line");
		}
		return new int[] { Integer.parseInt(str[2]), Integer.parseInt(str[3]) };
	}

	private int[] dimacsReadEdge(FastReader fr) throws IOException {
		try {
			char next = fr.nextChar();
			while (next != 'a') {
				fr.readLine();
				next = fr.nextChar();
			}
			int[] res = new int[] { fr.nextInt(), fr.nextInt(), fr.nextInt() };
			return res;
		} catch (IOException e) {
			throw new IOException("Bad DIMACS format: can't read edge");
		}
	}
}
