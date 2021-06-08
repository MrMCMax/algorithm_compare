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

import mrmcmax.data_structures.graphs.ResidualGraphList;

import static algorithm_compare.persistence.GraphData.TwoEndpointEdge;

public class PersistenceService implements IPersistenceService {

	public static final String PATH_TO_RESOURCES = "./src/main/resources/persistence/networks/";

	// Where the times are stored
	protected static final String PATH_TO_DATA = "./src/main/resources/persistence/networkData/";

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
					throw new IOException(e);
				}
			} else {
				Network net = new Network(name);
				net.setPath(path);
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(metadata));
				oos.writeObject(net);
				oos.flush();
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
	public ResidualGraphList loadNetwork(String name) throws IOException {
		Network net = networks.get(name);
		return net.loadNetwork();
	}

	/*
	 * Methods for times
	 */
	
	@Override
	public void storeTimes(String name, String[] algs, long[] times) throws IOException {
		if (!networks.containsKey(name)) {
			throw new IOException("Network not found");
		}
		Network net = networks.get(name);
		net.storeTime(algs, times);
	}

	@Override
	public long[] retrieveTimes(String name, String[] algs) throws IOException {
		if (!networks.containsKey(name)) {
			throw new IOException("Network not found: \"" + name + "\"");
		}
		Network net = networks.get(name);
		return net.retrieveTimes(algs);
	}

	@Override
	public void deleteTimes(String name, String[] algs) throws IOException {
		if (!networks.containsKey(name)) {
			throw new IOException("Network not found: \"" + name + "\"");
		}
		Network net = networks.get(name);
		net.deleteTimes(algs);
	}
}
