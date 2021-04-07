package algorithm_compare.logic;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import algorithm_compare.persistence.GraphData;
import static algorithm_compare.persistence.GraphData.TwoEndpointEdge;
import algorithm_compare.persistence.IPersistenceService;
import algorithm_compare.persistence.PersistenceService;

import mrmcmax.data_structures.graphs.ResidualGraphList;

public class LogicService implements ILogicService {
	
	private Map<String, Algorithm> algorithmMap;
	private Map<String, ResidualGraphList> loadedGraphs;
	
	private IPersistenceService persistenceService;
	
	
	/**
	 * Creates a logic service with the default persistence service
	 * and the default path to resources.
	 */
	public LogicService() throws IOException {
		this.persistenceService = new PersistenceService();
		loadedGraphs = new HashMap<>();
		algorithmMap = new HashMap<>();
		Algorithm edmondsKarp = new EdmondsKarp();
		algorithmMap.put(edmondsKarp.getName(), edmondsKarp);
	}
	
	public LogicService(IPersistenceService persistenceService) {
		this.persistenceService = persistenceService;
	}
	
	@Override
	public List<String> getListOfAlgorithms() {
		return algorithmMap.keySet().stream().collect(Collectors.toList());
	}
	
	@Override
	public List<String> getListOfNetworks() throws IOException {
		return persistenceService.getAllNetworkNames();
	}

	@Override
	public long[] computeNetworkWithAlgorithms(String netName, String[] algNames) throws IOException {
		long[] results = new long[algNames.length];
		ResidualGraphList g = getGraph(netName);
		for (int i = 0; i < algNames.length; i++) {
			results[i] = algorithmMap.get(algNames[i]).maxFlow(g);
		}
		return results;
	}
	
	/**
	 * Gets the ResidualGraph object if it's loaded, or loads it and returns it.
	 * @param networkName the network's unique name
	 * @return the associated ResidualGraph object
	 */
	protected ResidualGraphList getGraph(String networkName) throws IOException {
		if (loadedGraphs.containsKey(networkName)) {
			return loadedGraphs.get(networkName);
		} else {
			GraphData gd = persistenceService.loadNetwork(networkName);
			ResidualGraphList rg = buildAdjacencyListGraph(gd);
			loadedGraphs.put(networkName, rg);
			return rg;
		}
	}
	
	/**
	 * Builds the ResidualGraph representation of a network given the raw graph data.
	 * @param networkName the network's unique name
	 * @return the associated ResidualGraph object, built by adjacency lists.
	 */
	private ResidualGraphList buildAdjacencyListGraph(GraphData gd) {
		ResidualGraphList rg = new ResidualGraphList(gd.n);
		TwoEndpointEdge[] edges = gd.edges;
		for (int i = 0; i < edges.length; i++) {
			rg.addEdge(edges[i].v_in - 1, edges[i].v_out - 1, edges[i].capacity);
		}
		return rg;
	}
	
}
