package algorithm_compare.persistence;

import java.io.IOException;
import java.util.List;

public interface IPersistenceService {

	List<String> getAllNetworkNames() throws IOException;
	
	GraphData loadNetwork(String name) throws IOException;

	void storeTimes(String name, String[] algs, long[] times) throws IOException;
	
	/**
	 * If the time is not found, it stores a -1 in the long value.
	 * @param name
	 * @param algs
	 * @return
	 * @throws IOException
	 */
	long[] retrieveTimes(String name, String[] algs) throws IOException;
	
	void deleteTimes(String name, String[] algs) throws IOException;
}
