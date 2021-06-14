package algorithm_compare.logic.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import algorithm_compare.logic.algorithms.HighestVertexGapRelabelling3.Vertex;
import mrmcmax.data_structures.graphs.OneEndpointEdge;
import mrmcmax.data_structures.graphs.ResidualGraphList;
import mrmcmax.data_structures.linear.ArrayLimitQueue;
import mrmcmax.data_structures.linear.EasyQueue;
import mrmcmax.data_structures.linear.EraserLinkedList;
import mrmcmax.data_structures.linear.EraserLinkedList.Node;

public class FIFOPR_Gl_Gap3_Init extends FlowAlgorithm {

	public FIFOPR_Gl_Gap3_Init() {
		super("FIFOPR_Gl_Gap3_Init");
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
	//For gap relabelling
	protected EraserLinkedList<Vertex>[] vertexHeights;
	protected int maxHeight;
	//For debug
	protected int iteration = 0;
	
	protected class Vertex {
		protected int v;
		protected int currentEdge;
		protected int height;
		protected int excess;
		protected Node<Vertex> heightPointer;
		
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
		
		public String toString() {
			return "(vertex " + v + ", d(v)=" + height + ")";
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
		int m = g.getNumEdges();
		this.GLOBAL_RELABEL_FREQ = n;
		// Set up data structures. This method can be overriden for 
		// different data structure choices.
		initDataStructures();
		
		// Initialization of the algorithm
		initAlgorithm();
		
		return algorithm();
	}

	@SuppressWarnings("unchecked")
	public void initDataStructures() {
		//Create data structures
		vertices = new ArrayList<Vertex>(n);
		FIFOQueue = new LinkedList<Vertex>();
		vertexHeights = new EraserLinkedList[n];
		visited = new boolean[n];
		q = new ArrayLimitQueue<Vertex>(Vertex.class, n);
		for (int i = 0; i < n; i++) {
			vertexHeights[i] = new EraserLinkedList<Vertex>();
		}
		//Create vertices
		Vertex v;
		EraserLinkedList<Vertex> zeroHeight = vertexHeights[0];
		for (int i = 0; i < n; i++) {
			v = new Vertex(i);
			v.heightPointer = zeroHeight.addAndReturnPointer(v);
			vertices.add(v);
		}
		Vertex source = vertices.get(s);
		source.increaseHeightBy(n);
		zeroHeight.remove(source.heightPointer);
		source.heightPointer = null;
		maxHeight = 0;
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
				Vertex w = vertices.get(e.endVertex);
				w.increaseExcessBy(e.capacity); //Excesses start at 0 in this implementation
				addVertexWithExcess(vertices.get(e.endVertex));
			}
		}
		//No need to change current edge of s because it will return to the start.
		globalRelabel();
	}
	
	protected long algorithm() {
		// Go
		long maxFlow = 0;
		while (!thereAreVerticesWithExcess()) {
			if (DEBUG)
				iteration++;
			Vertex vertex = getVertexWithExcess(); // Polls
			if (!vertex.isActive()) {
				continue;
			}
			List<OneEndpointEdge> adj = g.getAdjacencyList(vertex.v);
			int e = vertex.currentEdge;
			int v_h = vertex.height;
			boolean relabel = false;
			//While there's no relabel and there's still excess
			while (!relabel && vertex.excess > 0) {
				//Search for an edge to push
				boolean eligible = false;
				OneEndpointEdge edge = null;
				while (e < adj.size() && !eligible) {
					edge = adj.get(e);
					if (vertices.get(edge.endVertex).height >= v_h || edge.remainingCapacity() <= 0) {
						e++;
					} else {
						if (DEBUG) {
							testTooSteep(vertex, edge, v_h);
						}
						eligible = true;
					}
				}
				if (eligible) {
					vertex.setCurrentEdge(e);
					if (DEBUG) {
						if (edge.endVertex == 196188) {
							System.out.println("Break");
						}
					}
					int delta = Math.min(vertex.excess, edge.remainingCapacity());
					push(vertex, e, delta);
					//Push might create excess on w.
					if (edge.endVertex != t && edge.endVertex != s) {
						Vertex w = vertices.get(edge.endVertex);
						int oldExcess = w.excess;
						w.increaseExcessBy(delta);
						if (oldExcess == 0) {
							addVertexWithExcess(w); //Lemma discovered with Inge: w will always will have excess
							//We have to remove it from non-active vertices and add it to active vertices.
						}
					}
					//Push might clear excess from the vertex. If that's the case, we have to take it out from active
					//and add it to non-active. If it still has excess, its position in active is still correct.
					vertex.decreaseExcessBy(delta);
				} else {
					relabelByMin(vertex);
					int newHeight = vertex.height;
					vertex.setCurrentEdge(0);
					//LETS CHECK FOR GAPS!
					if (vertexHeights[v_h].isEmpty()) {
						//We have a gap.
						vertex.height = n;
						vertex.heightPointer = null;
						Vertex w;
						EraserLinkedList<Vertex> nonActiveHeight;
						for (int i = v_h + 1; i <= maxHeight; i++) {
							nonActiveHeight = vertexHeights[i];
							while (!nonActiveHeight.isEmpty()) {
								w = nonActiveHeight.poll();
								w.height = n;
								w.heightPointer = null;
							}
						}
						maxHeight = v_h - 1;
					} else {
						//We have to change the vertex from one active height to another
						vertexHeights[v_h].remove(vertex.heightPointer);
						if (newHeight < n) {
							vertex.heightPointer = vertexHeights[newHeight].addAndReturnPointer(vertex);
							maxHeight = Math.max(maxHeight, newHeight);
						} else {
							vertex.heightPointer = null;
						}
					}
					relabel = true;
				}
			}
			if (vertex.isActive()) {
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
	 * Push operation. Only fixes edge.
	 * @param vertex the vertex to push from
	 * @param edge the edge in its adjacency list
	 * @param flow the amount of flow to push (has to be calculated before)
	 */
	protected void push(Vertex vertex, int edge, int flow) {
		OneEndpointEdge e = g.getAdjacencyList(vertex.v).get(edge);
		e.augment(flow);
		g.getAdjacencyList(e.endVertex).get(e.reverseEdgeIndex).decrement(flow);
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
		Vertex v;
		//FIFOQueue.clear();
		for (int i = 0; i < n; i++) {
			v = vertices.get(i);
			v.heightPointer = null;
			v.height = n;
		}
		for (int i = 0; i <= maxHeight; i++) {
			vertexHeights[i].clear();
		}
		maxHeight = 0;
		Vertex sink = vertices.get(t);
		sink.height = 0;
		vertexHeights[0].addAndReturnPointer(sink);
		visited[t] = true;
		visited[s] = true;
		q.reset();
		q.add(sink);
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
					maxHeight = Math.max(maxHeight, newHeight);
					out_vertex.currentEdge = 0;
					out_vertex.heightPointer = vertexHeights[newHeight].addAndReturnPointer(out_vertex);
					q.add(out_vertex);
				}
			}
		}
		//End of global relabel
		if (DEBUG) {
			System.out.println("Global relabel iteration " + iteration);
			testGlobalRelabel();
		}
	}
	
	protected long calculateMaxFlow() {
		if (DEBUG)
			System.out.println(iteration);
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
	
	/*
	 * TESTS
	 */
	
	
	private void testGlobalRelabel() {
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
	
	protected void testTooSteep(Vertex vertex, OneEndpointEdge edge, int v_h) {
		if (edge.remainingCapacity() > 0 && vertices.get(edge.endVertex).height < v_h - 1) {
			System.err.println("Iteration " + iteration);
			throw new RuntimeException("EDGE TOO STEEP: (" + vertex.v + ", " + edge.endVertex + "), heights: "
					+ vertex.height + ", " + vertices.get(edge.endVertex).height);
		}
	}

}
