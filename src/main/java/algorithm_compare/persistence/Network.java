package algorithm_compare.persistence;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import algorithm_compare.persistence.GraphData.TwoEndpointEdge;

public class Network implements Serializable {

	private String name;
	private transient String path;
	private Map<String, Long> recordedTimes;

	private static final long serialVersionUID = 1L;

	public Network() {
		
	}

	public Network(String name) {
		this.name = name;
		recordedTimes = new HashMap<String, Long>();
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public GraphData loadNetwork() throws IOException {
		FastReader fr = new FastReader(path);
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
			s = Integer.parseInt(nodeDescriptor1[1]) - 1;	//So that the vertices start at 0
			t = Integer.parseInt(nodeDescriptor2[1]) - 1;
		} else if (nodeDescriptor1[2].equals("t") && nodeDescriptor2[2].equals("s")) {
			t = Integer.parseInt(nodeDescriptor1[1]) - 1;
			s = Integer.parseInt(nodeDescriptor2[1]) - 1;
		} else {
			throw new IOException("Bad DIMACS format: can't read node descriptors");
		}
		// Then we read edges
		TwoEndpointEdge[] edges = new TwoEndpointEdge[m];
		int[] edge;
		for (int i = 0; i < m; i++) {
			edge = dimacsReadEdge(fr);
			edges[i] = new TwoEndpointEdge(edge[0], edge[1], edge[2]);
		}
		fr.close();
		return new GraphData(n, s, t, edges);
	}

	private String[] dimacsNextLine(FastReader br) throws IOException {
		String line = br.readLine();
		while (line.equals("") || line.startsWith("c")) {
			line = br.readLine();
		}
		return line.split("\\s+");
	}

	private int[] dimacsProblemLine(FastReader fr) throws IOException {
		String line = fr.readLine();
		while (line.equals("") || line.startsWith("c")) {
			line = fr.readLine();
		}
		String[] str = line.split("\\s+");
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
			//-1 so that the vertices start at 0
			int[] res = new int[] { fr.nextInt() - 1, fr.nextInt() - 1, fr.nextInt() };
			return res;
		} catch (IOException e) {
			throw new IOException("Bad DIMACS format: can't read edge");
		}
	}
	
	/**
	 * Will replace any previous values. Will store in disk.
	 * @param algorithms
	 * @param times
	 * @throws IOException
	 */
	public void storeTime(String[] algorithms, long[] times) throws IOException {
		for (int i = 0; i < algorithms.length; i++) {
			System.out.println("STORING TIME " + times[i] + " FOR ALGORITHM " + algorithms[i] + " ON NETWORK " + this.name);
			recordedTimes.put(algorithms[i], times[i]);
		}
		storeObject();
	}
	
	/**
	 * Will overwrite the file.
	 * @throws IOException
	 */
	private void storeObject() throws IOException {
		System.out.println("STORING OBJECT: " + this.name);
		String path = PersistenceService.PATH_TO_DATA + name;
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
		oos.writeObject(this);
		oos.flush();
		oos.close();
	}

	public long[] retrieveTimes(String[] algs) {
		long[] res = new long[algs.length];
		for (int i = 0; i < algs.length; i++) {
			System.out.println("RETRIEVING TIME FOR NETWORK " + name + " ON ALGORITHM " + algs[i]);
			res[i] = recordedTimes.getOrDefault(algs[i], (long)-1);
			System.out.println("TIME " + res[i]);
		}
		return res;
	}

	public void deleteTimes(String[] algs) throws IOException {
		for (int i = 0; i < algs.length; i++) {
			Long time = recordedTimes.remove(algs[i]);
			if (time == null) {
				System.err.println("Warning: algorithm " + algs[i] + " had no time for network " + name);
			}
		}
		storeObject();
	}
}
