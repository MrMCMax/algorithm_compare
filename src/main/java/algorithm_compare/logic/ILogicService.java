package algorithm_compare.logic;

import java.io.IOException;
import java.util.List;

public interface ILogicService {

	/** Retrieves a list of the algorithm names */
	List<String> getListOfAlgorithms();

	/** Retrieves a list of the network names */
	List<String> getListOfNetworks() throws IOException;

	/**Computes the maximum flow on a network using different algorithms.
	 * Stores the result in the default directory.
	 * @return The value of the maximum flow */
	long[] computeNetworkWithAlgorithms(String netName, String[] algNames) throws IOException;
	
	long[] computeNetworkWithAlgorithms(String netName, String[] algNames, int repetitions) throws IOException;

	long[] retrieveTimes(String network, String[] algNames) throws IOException;
	
	int getNetworkNVertices(String netName) throws IOException;
}
