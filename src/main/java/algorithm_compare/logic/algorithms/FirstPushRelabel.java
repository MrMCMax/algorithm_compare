package algorithm_compare.logic.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;

import mrmcmax.data_structures.graphs.OneEndpointEdge;
import mrmcmax.data_structures.graphs.ResidualGraphList;

public class FirstPushRelabel extends FlowAlgorithm {

	public FirstPushRelabel() {
		super("FirstPushRelabel");
	}

	/*
	 * ADDITIONAL INFO 0, ..., n-1 are the vertices. excesses stores the excesses of
	 * each vertex.
	 * 
	 */
	
	public FirstPushRelabel(String string) {
		super(string);
	}

	protected ResidualGraphList g;
	protected int s;
	protected int t;
	protected List<Integer> heights;
	protected List<Integer> excesses;
	protected List<Integer> currentEdge;
	protected Collection<Integer> verticesWithExcess;
	protected int n;

	/**
	 * Precondition: all flows are zero
	 */
	@Override
	public long maxFlow(ResidualGraphList g) {
		this.g = g;
		this.n = g.getNumVertices();
		this.s = g.getSource();
		this.t = g.getSink();
		// Set up data structures. This method can be overriden for 
		// different data structure choices.
		initDataStructures();
		
		// Initialization of the algorithm
		initAlgorithm();
		
		return algorithm();
	}

	protected void initDataStructures() {
		excesses = new ArrayList<Integer>(n);
		heights = new ArrayList<Integer>(n);
		currentEdge = new ArrayList<Integer>(n);
		verticesWithExcess = new HashSet<Integer>();

		//Set excesses, heights and current edges to default values
		for (int i = 0; i < n; i++) {
			excesses.add(0);
			heights.add(0);
			currentEdge.add(0);
		}
		heights.set(s, n);
	}
	
	protected void initAlgorithm() {
		//Send flow to the adjacent to s, saturating them
		List<OneEndpointEdge> adjS = g.getAdjacencyList(s);
		for (int i = 0; i < adjS.size(); i++) {
			OneEndpointEdge e = adjS.get(i);
			if (e.remainingCapacity() <= 0) continue;
			e.augment(e.capacity);
			g.getAdjacencyList(e.endVertex).get(e.reverseEdgeIndex).decrement(e.capacity);
			if (e.endVertex != t) {
				excesses.set(e.endVertex, e.capacity); //Excesses start at 0 in this implementation
				addVertexWithExcess(e.endVertex);
			}
		}
		//No need to change current edge of s because it will return to the start.
	}
	
	protected long algorithm() {
		//Go
		long maxFlow = 0;
		while (!verticesWithExcess.isEmpty()) {
			int v = getVertexWithExcess();
			List<OneEndpointEdge> adj = g.getAdjacencyList(v);
			int e = currentEdge.get(v);
			int v_h = heights.get(v);
			boolean eligible = false;
			while (e < adj.size() && !eligible) {
				OneEndpointEdge edge = adj.get(e);
				if (heights.get(edge.endVertex) >= v_h ||
						edge.remainingCapacity() <= 0) {
					e++;
				} else {
					eligible = true;
				}
			}
			if (eligible) {
				currentEdge.set(v, e);
				int delta = Math.min(excesses.get(v), adj.get(e).remainingCapacity());
				push(v, e, delta);
			} else {
				relabel(v);
				currentEdge.set(v, 0);
			}
		}
		//That's it. Calculate the value of the flow.
		List<OneEndpointEdge> adjS = g.getAdjacencyList(s);
		for (int i = 0; i < adjS.size(); i++) {
			if (adjS.get(i).capacity > 0)
				maxFlow += adjS.get(i).flow;
		}
		return maxFlow;
	}
	
	/**
	 * Push operation. Also fixes excesses and vertices with excess.
	 * @param vertex the vertex to push from
	 * @param edge the edge in its adjacency list
	 * @param flow the amount of flow to push (has to be calculated before)
	 */
	protected void push(int vertex, int edge, int flow) {
		OneEndpointEdge e = g.getAdjacencyList(vertex).get(edge);
		e.augment(flow);
		g.getAdjacencyList(e.endVertex).get(e.reverseEdgeIndex).decrement(flow);
		int newExcess = excesses.get(vertex) - flow;
		excesses.set(vertex, newExcess);
		if (newExcess == 0) {
			removeVertexWithExcess(vertex);
		}
		if (e.endVertex != t && e.endVertex != s) {
			excesses.set(e.endVertex, excesses.get(e.endVertex) + flow);
			addVertexWithExcess(e.endVertex); //Lemma discovered with Inge: always will have excess
		}
	}
	
	/**
	 * Relabel operation.
	 * @param vertex vertex to relabel.
	 */
	protected void relabel(int vertex) {
		int actualHeight = heights.get(vertex);
		heights.set(vertex, actualHeight + 1);
		if (actualHeight > n - 1) {
		}
	}
	
	protected int getVertexWithExcess() {
		return verticesWithExcess.iterator().next();
	}
	
	protected void addVertexWithExcess(int v) {
		verticesWithExcess.add(v);
	}
	
	protected void removeVertexWithExcess(int v) {
		verticesWithExcess.remove(v);
	}
}
