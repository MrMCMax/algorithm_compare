package algorithm_compare.logic;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;

import algorithm_compare.logic.algorithms.DinicsAlgorithm;
import algorithm_compare.logic.algorithms.EdmondsKarp;
import algorithm_compare.logic.algorithms.FIFOPushRelabel1;
import algorithm_compare.logic.algorithms.FIFOPushRelabel2;
import algorithm_compare.logic.algorithms.FIFOPushRelabel3;
import algorithm_compare.logic.algorithms.FIFOPushRelabelVertex;
import algorithm_compare.logic.algorithms.FirstPushRelabel;
import algorithm_compare.logic.algorithms.FlowAlgorithm;
import algorithm_compare.logic.algorithms.HighestVertex1;
import algorithm_compare.logic.algorithms.NaivePushRelabel;
import algorithm_compare.logic.algorithms.ScalingEdmondsKarp;
import algorithm_compare.logic.algorithms.SecondPushRelabel;
import algorithm_compare.persistence.GraphData;
import algorithm_compare.persistence.GraphData.TwoEndpointEdge;
import algorithm_compare.persistence.IPersistenceService;
import algorithm_compare.persistence.PersistenceService;
import mrmcmax.data_structures.graphs.ResidualGraphList;

public class LogicService implements ILogicService {
	
	private Map<String, FlowAlgorithm> algorithmMap;
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
		/*
		FlowAlgorithm edmondsKarp = new EdmondsKarp();
		FlowAlgorithm scalingEdmondsKarp = new ScalingEdmondsKarp();
		FlowAlgorithm dinicsAlgorithm = new DinicsAlgorithm();
		FlowAlgorithm firstPushRelabel = new FirstPushRelabel();
		FlowAlgorithm secondPushRelabel = new SecondPushRelabel();
		FlowAlgorithm fifoPushRelabel1 = new FIFOPushRelabel1();
		FlowAlgorithm fifoPushRelabel2 = new FIFOPushRelabel2();
		FlowAlgorithm highestVertex1 = new HighestVertex1();
		FlowAlgorithm fifoPushRelabel3 = new FIFOPushRelabel3();
		FlowAlgorithm fifoPushRelabelVertex = new FIFOPushRelabelVertex();
		FlowAlgorithm naivePushRelabel = new NaivePushRelabel();
		algorithmMap.put(edmondsKarp.getName(), edmondsKarp);
		algorithmMap.put(scalingEdmondsKarp.getName(), scalingEdmondsKarp);
		algorithmMap.put(dinicsAlgorithm.getName(), dinicsAlgorithm);
		algorithmMap.put(firstPushRelabel.getName(), firstPushRelabel);
		algorithmMap.put(secondPushRelabel.getName(), secondPushRelabel);
		algorithmMap.put(fifoPushRelabel1.getName(), fifoPushRelabel1);
		algorithmMap.put(fifoPushRelabel2.getName(), fifoPushRelabel2);
		algorithmMap.put(highestVertex1.getName(), highestVertex1);
		algorithmMap.put(fifoPushRelabel3.getName(), fifoPushRelabel3);
		algorithmMap.put(fifoPushRelabelVertex.getName(), fifoPushRelabelVertex);
		algorithmMap.put(naivePushRelabel.getName(), naivePushRelabel);
		*/
		Reflections refls = new Reflections("algorithm_compare.logic.algorithms");
		Set<Class<? extends FlowAlgorithm>> subTypes = refls.getSubTypesOf(FlowAlgorithm.class);
		Iterator<Class<? extends FlowAlgorithm>> it = subTypes.iterator();
		while (it.hasNext()) {
			Class<? extends FlowAlgorithm> algClass = it.next();
			try {
				Constructor<? extends FlowAlgorithm> ctor = algClass.getConstructor();
				FlowAlgorithm alg = ctor.newInstance();
				algorithmMap.put(alg.getName(), alg);
			} catch (NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
		long[] times = new long[algNames.length];
		ResidualGraphList g = getGraph(netName);
		g.resetFlowsToZero();
		System.out.println("Graph loaded");
		for (int i = 0; i < algNames.length; i++) {
			if (algorithmMap.containsKey(algNames[i])) {
				FlowAlgorithm alg = algorithmMap.get(algNames[i]);
				long t1 = System.currentTimeMillis();
				results[i] = alg.maxFlow(g);
				long t2 = System.currentTimeMillis();
				times[i] = t2 - t1;
				System.out.println("TIMES FOR NETWORK " + netName + " ON ALGORITHM " + algNames[i] + ": " + times[i]);
				g.resetFlowsToZero();
			} else {
				throw new RuntimeException("This algorithm hasn't been implemented yet: " + algNames[i]);
			}
		}
		storeTimes(netName, algNames, times);
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
		rg.setSource(gd.s);
		rg.setSink(gd.t);
		TwoEndpointEdge[] edges = gd.edges;
		for (int i = 0; i < edges.length; i++) {
			rg.addEdge(edges[i].v_in, edges[i].v_out, edges[i].capacity);
		}
		return rg;
	}

	@Override
	public long[] retrieveTimes(String network, String[] algNames) throws IOException {
		long[] times = persistenceService.retrieveTimes(network, algNames);
		return times;
	}
	
	private void storeTimes(String network, String[] algNames, long[] times) throws IOException {
		persistenceService.storeTimes(network, algNames, times);
	}

	@Override
	public int getNetworkNVertices(String netName) throws IOException {
		ResidualGraphList graph = getGraph(netName);
		return graph.getNumVertices();
	}
	
}
