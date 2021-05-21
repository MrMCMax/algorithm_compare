package algorithm_compare.logic.algorithms;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mrmcmax.data_structures.graphs.OneEndpointEdge;
import mrmcmax.data_structures.graphs.ResidualGraphList;

public class FIFOPushRelabelVertex extends FlowAlgorithm {

	public FIFOPushRelabelVertex() {
		super("FIFOPushRelabelVertex");
	}

	protected ResidualGraphList g;
	protected int s;
	protected int t;
	protected int n;
	protected ArrayList<Vertex> vertices;
	protected LinkedList<Vertex> FIFOQueue;
	
	protected class Vertex {
		protected int v;
		protected int currentEdge;
		protected int height;
		protected int excess;
		
		public Vertex(int v) {
			this.v = v;
			this.currentEdge = 0;
			this.height = 0;
			this.excess = 0;
		}
		
		public void increaseExcessBy(int ex) {
			this.excess += ex;
		}
		
		public void decreaseExcessBy(int ex) {
			this.excess -= ex;
		}
		
		public void increaseHeightBy(int h) {
			this.height += h;
		}
		
		public void advanceCurrentEdge() {
			this.currentEdge++;
		}
		
		public void setCurrentEdge(int e) {
			this.currentEdge = e;
		}
		
		/**
		 * Relabel operation.
		 * @param vertex vertex to relabel.
		 */
		protected void relabelBy1() {
			this.height += 1;
		}
	}
	/**
	 * Precondition: all flows are zero.
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

	public void initDataStructures() {
		vertices = new ArrayList<Vertex>(n);
		FIFOQueue = new LinkedList<Vertex>();
		//Create vertices
		for (int i = 0; i < n; i++) {
			vertices.add(new Vertex(i));
		}
		vertices.get(s).increaseHeightBy(n);
	}
	
	protected void initAlgorithm() {
		//Send flow to the adjacent to s, saturating them
		List<OneEndpointEdge> adjS = g.getAdjacencyList(s);
		OneEndpointEdge e = null;
		for (int i = 0; i < adjS.size(); i++) {
			e = adjS.get(i);
			if (e.remainingCapacity() <= 0) continue;
			e.augment(e.capacity);
			g.getAdjacencyList(e.endVertex).get(e.reverseEdgeIndex).decrement(e.capacity);
			if (e.endVertex != t) {
				vertices.get(e.endVertex).increaseExcessBy(e.capacity); //Excesses start at 0 in this implementation
				addVertexWithExcess(vertices.get(e.endVertex));
			}
		}
		//No need to change current edge of s because it will return to the start.
	}
	
	protected long algorithm() {
		// Go
		long maxFlow = 0;
		while (!thereAreVerticesWithExcess()) {
			Vertex vertex = getVertexWithExcess(); // Polls
			List<OneEndpointEdge> adj = g.getAdjacencyList(vertex.v);
			int e = vertex.currentEdge;
			int v_h = vertex.height;
			boolean relabel = false;
			//While there's no relabel and there's still excess
			while (!relabel && vertex.excess > 0) {
				//Search for an edge to push
				boolean eligible = false;
				while (e < adj.size() && !eligible) {
					OneEndpointEdge edge = adj.get(e);
					if (vertices.get(edge.endVertex).height >= v_h || edge.remainingCapacity() <= 0) {
						e++;
					} else {
						eligible = true;
					}
				}
				if (eligible) {
					vertex.setCurrentEdge(e);
					int delta = Math.min(vertex.excess, adj.get(e).remainingCapacity());
					push(vertex, e, delta);
				} else {
					relabelByMin(vertex);
					vertex.setCurrentEdge(0);
					relabel = true;
				}
			}
			if (vertex.excess > 0) {
				addVertexWithExcess(vertex);
			}
		}
		// That's it. Calculate the value of the flow.
		return calculateMaxFlow();
	}
	
	/**
	 * Push operation. Also fixes excesses and vertices with excess.
	 * @param vertex the vertex to push from
	 * @param edge the edge in its adjacency list
	 * @param flow the amount of flow to push (has to be calculated before)
	 */
	protected void push(Vertex vertex, int edge, int flow) {
		OneEndpointEdge e = g.getAdjacencyList(vertex.v).get(edge);
		e.augment(flow);
		g.getAdjacencyList(e.endVertex).get(e.reverseEdgeIndex).decrement(flow);
		vertex.decreaseExcessBy(flow);
		if (e.endVertex != t && e.endVertex != s) {
			Vertex w = vertices.get(e.endVertex);
			w.increaseExcessBy(flow);
			addVertexWithExcess(w); //Lemma discovered with Inge: w will always will have excess
		}
	}
	
	/**
	 * Improved relabel operation.
	 */
	protected void relabelByMin(Vertex vertex) {
		List<OneEndpointEdge> adj = g.getAdjacencyList(vertex.v);
		int newHeight = Integer.MAX_VALUE;
		OneEndpointEdge edge = null;
		for (int i = 0; i < adj.size(); i++) {
			edge = adj.get(i);
			if (edge.remainingCapacity() > 0) {
				newHeight = Math.min(newHeight, vertices.get(edge.endVertex).height);
			}
		}
		vertex.height = newHeight + 1;
	}

	protected Vertex getVertexWithExcess() {
		return FIFOQueue.poll();
	}
	
	protected void addVertexWithExcess(Vertex v) {
		if (v.height < n)
			FIFOQueue.add(v);
	}
	
	protected boolean thereAreVerticesWithExcess() {
		return FIFOQueue.isEmpty();
	}
	
	protected long calculateMaxFlow() {
		long maxFlow = 0;
		List<OneEndpointEdge> adjT = g.getAdjacencyList(t);
		for (int i = 0; i < adjT.size(); i++) {
			OneEndpointEdge e = adjT.get(i);
			//Count backward edges from t
			if (e.capacity == 0) {
				maxFlow += e.remainingCapacity();
			}
		}
		return maxFlow;
	}
}
