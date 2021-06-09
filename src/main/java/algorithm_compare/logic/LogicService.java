package algorithm_compare.logic;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.reflections.Reflections;

import algorithm_compare.logic.algorithms.FlowAlgorithm;
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
		initialise();
	}
	
	public LogicService(IPersistenceService persistenceService) {
		this.persistenceService = persistenceService;
		initialise();
	}
	
	private void initialise() {
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
	
	@Override
	public List<String> getListOfAlgorithms() {
		List<String> unsorted = algorithmMap.keySet().stream().collect(Collectors.toList());
		unsorted.sort((s1, s2) -> s1.compareTo(s2));
		return unsorted;
	}
	
	@Override
	public List<String> getListOfNetworks() throws IOException {
		List<String> unsorted = persistenceService.getAllNetworkNames();
		unsorted.sort((s1, s2) -> s1.compareTo(s2));
		return unsorted;
	}

	@Override
	public long[] computeNetworkWithAlgorithms(String netName, String[] algNames) throws IOException {
		return computeNetworkWithAlgorithms(netName, algNames, 1);
	}
	
	@Override
	public long[] computeNetworkWithAlgorithms(String netName, String[] algNames, int repetitions) throws IOException {
		long[] results = new long[algNames.length];
		long[] times = new long[algNames.length];
		long[] flowTimes = new long[repetitions]; //Will be reused in each run
		ResidualGraphList g = getGraph(netName);
		System.out.println("Graph loaded");
		for (int i = 0; i < algNames.length; i++) {
			if (algorithmMap.containsKey(algNames[i])) {
				FlowAlgorithm alg = algorithmMap.get(algNames[i]);
				//Loop for repetitions
				for (int k = 0; k < repetitions; k++) {
					g.resetFlowsToZero();
					long t1 = System.currentTimeMillis();
					results[i] = alg.maxFlow(g);
					long t2 = System.currentTimeMillis();
					flowTimes[k] = t2 - t1;
				}
				times[i] = (long) Math.ceil(getAndPrintStats(flowTimes));
				System.out.println("TIMES FOR NETWORK " + netName + " ON ALGORITHM " + algNames[i] + ": " + times[i]);
			} else {
				throw new RuntimeException("This algorithm hasn't been implemented yet: " + algNames[i]);
			}
		}
		g.resetFlowsToZero();
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
			ResidualGraphList rg = persistenceService.loadNetwork(networkName);
			loadedGraphs.put(networkName, rg);
			return rg;
		}
	}
	
	/**
	 * DEPRECATED
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
	
	public static double getAndPrintStats(long[] data) {
		LongSummaryStatistics stats = LongStream.of(data).summaryStatistics();
		System.out.println("Max: " + stats.getMax());
		System.out.println("Min: " + stats.getMin());
		double average = stats.getAverage();
		System.out.println("Average: " + average);
		System.out.println("Standard deviation: " + std(data, stats));
		return average;
	}	
	
	public static double std(long[] data, LongSummaryStatistics stats) {
		double sum = 0, diff = 0;
		for (long d : data) {
			diff = d - stats.getAverage();
			sum += diff * diff;
		}
		double factor = 1.0 / stats.getCount();
		return Math.sqrt(factor * sum);
	}
}
