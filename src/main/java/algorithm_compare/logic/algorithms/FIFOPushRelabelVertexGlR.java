package algorithm_compare.logic.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import algorithm_compare.logic.algorithms.HighestVertexGapRelabelling2.Vertex;
import mrmcmax.data_structures.graphs.OneEndpointEdge;
import mrmcmax.data_structures.graphs.ResidualGraphList;
import mrmcmax.data_structures.linear.ArrayLimitQueue;
import mrmcmax.data_structures.linear.EasyQueue;
import mrmcmax.data_structures.linear.EraserLinkedList;
import mrmcmax.data_structures.linear.EraserLinkedList.Node;

public class FIFOPushRelabelVertexGlR extends FlowAlgorithm {

	public FIFOPushRelabelVertexGlR() {
		super("FIFOPushRelabelVertexGlR");
	}

	protected ResidualGraphList g;
	protected int s;
	protected int t;
	protected int n;
	protected ArrayList<Vertex> vertices;
	protected LinkedList<Vertex> FIFOQueue;
	//For global relabel update
	protected int relabels = 0;
	protected static int GLOBAL_RELABEL_FREQ;
	protected EasyQueue<Vertex> q;
	protected boolean visited[];
	
	protected class Vertex {
		protected int v;
		protected int currentEdge;
		protected int height;
		protected int excess;
		protected Node<Vertex> globalRelabelPointer;
		
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
		
		public boolean isActive() {
			return height < n && excess > 0;
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
		this.GLOBAL_RELABEL_FREQ = n;
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
		visited = new boolean[n];
		q = new ArrayLimitQueue<Vertex>(Vertex.class, n);
		//Create vertices
		for (int i = 0; i < n; i++) {
			vertices.add(new Vertex(i));
		}
		vertices.get(s).increaseHeightBy(n);
		globalRelabel();
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
			Vertex vertex = getVertexWithExcess(); // Peeks
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
			if (relabel) {
				if (relabels % GLOBAL_RELABEL_FREQ == 0) {
					globalRelabel();
				}
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
		relabels++;
	}

	protected Vertex getVertexWithExcess() {
		return FIFOQueue.poll();
	}
	
	protected void addVertexWithExcess(Vertex v) {
			FIFOQueue.add(v);
	}
	
	protected boolean thereAreVerticesWithExcess() {
		return FIFOQueue.isEmpty();
	}
	
	protected void globalRelabel() {
		//Backwards BFS from t
		//It will follow residual edges.
		//The distance is set from the vertex that is popped to the adjacent ones
		//that haven't been visited yet.
		//The heights might change completely. The easiest thing is to clear the
		//height data structures and fill them again.
		//PUERTO RICO ME LO REGALO
		Arrays.fill(visited, false);
		EraserLinkedList<Vertex> allVertices = new EraserLinkedList<>();
		Vertex v;
		FIFOQueue.clear();
		for (int i = 0; i < n; i++) {
			v = vertices.get(i);
			v.globalRelabelPointer = allVertices.addAndReturnPointer(v);
		}
		Vertex source = vertices.get(s);
		allVertices.remove(source.globalRelabelPointer);
		source.globalRelabelPointer = null;
		Vertex sink = vertices.get(t);
		allVertices.remove(sink.globalRelabelPointer);
		sink.globalRelabelPointer = null;
		visited[t] = true;
		visited[s] = true;
		q.reset();
		q.add(vertices.get(t));
		while (!q.isEmpty()) {
			Vertex vertex = q.poll();
			int newHeight = vertex.height + 1;
			List<OneEndpointEdge> adj = g.getAdjacencyList(vertex.v);
			OneEndpointEdge edge, reverseEdge;
			int out_v;
			for (int i = 0; i < adj.size(); i++) {
				edge = adj.get(i);
				out_v = edge.endVertex;
				if (visited[out_v]) continue;
				reverseEdge = g.getAdjacencyList(out_v).get(edge.reverseEdgeIndex);
				if (reverseEdge.remainingCapacity() > 0) { //This applies both to backward and forward edges
					//We got an augmenting path
					visited[out_v] = true;
					Vertex out_vertex = vertices.get(out_v);
					out_vertex.height = newHeight;
					out_vertex.currentEdge = 0;
					if (out_vertex.isActive()) {
						addVertexWithExcess(out_vertex);
					}
					allVertices.remove(out_vertex.globalRelabelPointer);
					out_vertex.globalRelabelPointer = null;
					q.add(out_vertex);
				}
			}
		}
		while (!allVertices.isEmpty()) {
			v = allVertices.poll();
			v.globalRelabelPointer = null;
			v.height = n;
		}
		//End of global relabel
		if (DEBUG) {
			//Check that the conditions hold
			for (int i = 0; i < vertices.size(); i++) {
				if (i == s || i == t) continue;
				List<OneEndpointEdge> adj = g.getAdjacencyList(i);
				Vertex vertex = vertices.get(i);
				int height = vertex.height;
				//BFS to see if the height is correct
				
				//Check if it has too steep edges
				for (int j = 0; j < adj.size(); j++) {
					OneEndpointEdge e = adj.get(j);
					if (e.remainingCapacity() > 0) {
						Vertex outVertex = vertices.get(e.endVertex);
						if (outVertex.height < vertex.height - 1) {
							//System.err.println("Iteration " + iteration);
							throw new RuntimeException("Global relabelling made edge too steep: " + 
									vertex.v + ", " + outVertex.v + " at heights " + vertex.height + ", " + outVertex.height);
						}
					}
				}
			}
		}
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
