package algorithm_compare.logic.algorithms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import mrmcmax.data_structures.graphs.OneEndpointEdge;
import mrmcmax.data_structures.graphs.ResidualGraphList;
import mrmcmax.data_structures.linear.ArrayLimitQueue;
import mrmcmax.data_structures.linear.EasyQueue;

public class HighestVertexGapRelabelling2 extends FlowAlgorithm {

	public HighestVertexGapRelabelling2() {
		super("HighestVertexGapRelabelling2");
	}

	protected ResidualGraphList g;
	protected int s;
	protected int t;
	protected int n;
	protected int m;
	protected ArrayList<Vertex> vertices;
	protected LinkedList<Vertex>[] activeHeights;
	protected int b;
	protected int[] nonActiveHeights;
	protected int iteration = 0;
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

		@Override
		public int hashCode() {
			return v;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof Vertex))
				return false;
			Vertex other = (Vertex) obj;
			if (currentEdge != other.currentEdge)
				return false;
			if (excess != other.excess)
				return false;
			if (height != other.height)
				return false;
			if (v != other.v)
				return false;
			return true;
		}

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

		public String toString() {
			return "(vertex " + v + ")";
		}

		public boolean isActive() {
			return height < n && excess > 0;
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
		this.m = g.getNumEdges();
		int ratio = m/n;
		if (ratio > 6) {
			this.GLOBAL_RELABEL_FREQ = m;
		} else {
			this.GLOBAL_RELABEL_FREQ = n;
		}
		// Set up data structures. This method can be overriden for
		// different data structure choices.
		initDataStructures();

		// Initialization of the algorithm
		initAlgorithm();

		return algorithm();
	}

	@SuppressWarnings("unchecked")
	public void initDataStructures() {
		vertices = new ArrayList<Vertex>(n);
		activeHeights = new LinkedList[n + 1];
		for (int i = 0; i < n+1; i++)
			activeHeights[i] = new LinkedList<Vertex>();
		nonActiveHeights = new int[n];
		b = 0;
		q = new ArrayLimitQueue<Vertex>(Vertex.class, n);
		visited = new boolean[n];
		// Create vertices
		for (int i = 0; i < n; i++) {
			vertices.add(new Vertex(i));
		}
		vertices.get(s).increaseHeightBy(n);
	}

	protected void initAlgorithm() {
		// Send flow to the adjacent to s, saturating them
		List<OneEndpointEdge> adjS = g.getAdjacencyList(s);
		OneEndpointEdge e = null;
		int excessVertices = 0;
		for (int i = 0; i < adjS.size(); i++) {
			e = adjS.get(i);
			if (e.remainingCapacity() <= 0)
				continue;
			e.augment(e.capacity);
			g.getAdjacencyList(e.endVertex).get(e.reverseEdgeIndex).decrement(e.capacity);
			if (e.endVertex != t) {
				excessVertices++;
				Vertex w = vertices.get(e.endVertex);
				w.increaseExcessBy(e.capacity); // Excesses start at 0 in this implementation
				activeHeights[0].add(w);
			}
		}
		nonActiveHeights[0] = n - 1 - excessVertices; // n - source - excess
		// No need to change current edge of s because it will return to the start.
	}

	protected long algorithm() {
		// Go
		while (thereAreVerticesWithExcess()) {
			if (DEBUG) {
				iteration++;
				if (iteration > 225) {
					System.out.println("Hey its gonna break");
				}
			}
			Vertex vertex = getVertexWithExcess(); // Peeks
			List<OneEndpointEdge> adj = g.getAdjacencyList(vertex.v);
			int e = vertex.currentEdge;
			int v_h = vertex.height;
			boolean relabel = false;
			// While there's no relabel and there's still excess
			while (!relabel && vertex.excess > 0) {
				// Search for an edge to push
				boolean eligible = false;
				OneEndpointEdge edge = null;
				while (e < adj.size() && !eligible) {
					edge = adj.get(e);
					if (vertices.get(edge.endVertex).height >= v_h || edge.remainingCapacity() <= 0) {
						if (DEBUG) {
							if (edge.remainingCapacity() > 0 && vertices.get(edge.endVertex).height < v_h - 1) {
								throw new RuntimeException("EDGE TOO STEEP: (" + vertex.v + ", " + edge.endVertex
										+ "), heights: " + vertex.height + ", " + vertices.get(edge.endVertex).height);
							}
						}
						e++;
					} else {
						eligible = true;
					}
				}
				if (eligible) {
					vertex.setCurrentEdge(e);
					int delta = Math.min(vertex.excess, adj.get(e).remainingCapacity());
					push(vertex, e, delta);
					// Push creates excess on the endvertex
					if (edge.endVertex != t && edge.endVertex != s) {
						Vertex w = vertices.get(edge.endVertex);
						int oldExcess = w.excess;
						w.increaseExcessBy(delta);
						if (oldExcess == 0) {
							// New vertex with excess. Must add to the data structure. Must remove from
							// non-active
							nonActiveHeights[w.height]--;
							activeHeights[w.height].add(w);
						}
						if (DEBUG) {
							// Make sure that the vertex that has excess is stored
							List<Vertex> h = activeHeights[w.height];
							Iterator<Vertex> it = h.iterator();
							int i = 0;
							while (it.hasNext()) {
								Vertex inList = it.next();
								if (inList.v != w.v)
									i++;
							}
							boolean found = i < h.size();
							if (!found)
								throw new RuntimeException(
										"Active vertex " + w.v + " not found " + "at height " + w.height);
						}
					}
					// Push might clear excess
					vertex.decreaseExcessBy(delta);
					if (vertex.excess == 0) {
						nonActiveHeights[vertex.height]++;
						LinkedList<Vertex> vertexHeight = activeHeights[vertex.height];
						vertexHeight.pop();
						if (vertexHeight.isEmpty()) {
							b--;
						}
					}
				} else {
					// Relabel sets the new height of the vertex
					int oldHeight = vertex.height;
					relabelByMin(vertex); //Might trigger a global relabel
					int newHeight = vertex.height;
					activeHeights[oldHeight].pop();
					// WE MIGHT HAVE A GAP
					if (activeHeights[oldHeight].isEmpty() && nonActiveHeights[oldHeight] == 0) {
						// The vertex is above a gap
						vertex.height = n;
						// There are no other excess vertices at this height
						b--;
					} else if (newHeight >= n) {
						// Vertex might have risen to n. There's no more augmenting path for him
						if (activeHeights[oldHeight].isEmpty()) {
							b--;
						}
					} else {
						activeHeights[newHeight].add(vertex);
						b = newHeight;
						vertex.setCurrentEdge(0);
					}
					relabel = true;
					if (relabels % GLOBAL_RELABEL_FREQ == 0) {
						globalRelabel();
					}
				}
			}
		}
		// That's it. Calculate the value of the flow.
		return calculateMaxFlow();
	}

	/**
	 * Push operation. Doesn't fix excesses, only the edge.
	 * 
	 * @param vertex the vertex to push from
	 * @param edge   the edge in its adjacency list
	 * @param flow   the amount of flow to push (has to be calculated before)
	 */
	protected void push(Vertex vertex, int edge, int flow) {
		OneEndpointEdge e = g.getAdjacencyList(vertex.v).get(edge);
		e.augment(flow);
		g.getAdjacencyList(e.endVertex).get(e.reverseEdgeIndex).decrement(flow);
	}

	protected Vertex getVertexWithExcess() {
		Vertex ret = activeHeights[b].peek();
		return ret;
	}

	protected boolean thereAreVerticesWithExcess() {
		boolean ret;
		while (b >= 0 && activeHeights[b].isEmpty()) {
			b--;
		}
		ret = b >= 0;
		if (DEBUG) {
			int i = 0;
			while (i < n && (activeHeights[i] == null || activeHeights[i].isEmpty())) {
				i++;
			}
			boolean excess = i < n;
			if (excess && !ret) {
				throw new RuntimeException("There was a vertex with excess at height " + i + " but the pointer was "
						+ "at height " + b + " and it found none");
			}
		}
		return ret;
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
		newHeight++;
		vertex.height = newHeight;
		relabels++;
	}

	protected void relabelBy1(Vertex vertex) {
		vertex.height += 1;
	}

	protected long calculateMaxFlow() {
		long maxFlow = 0;
		List<OneEndpointEdge> adjT = g.getAdjacencyList(t);
		for (int i = 0; i < adjT.size(); i++) {
			OneEndpointEdge e = adjT.get(i);
			// Count backward edges from t
			if (e.capacity == 0) {
				maxFlow += e.remainingCapacity();
			}
		}
		return maxFlow;
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
		Arrays.fill(nonActiveHeights, 0);
		for (int i = 0; i < n+1; i++) 
			activeHeights[i].clear();
		b = 0;
		visited[t] = true;
		visited[s] = true;
		nonActiveHeights[0]++;
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
					if (out_vertex.isActive()) {
						activeHeights[newHeight].add(out_vertex);
						if (newHeight < n)
							b = Math.max(b, newHeight);
					} else {
						nonActiveHeights[newHeight]++;
					}
					q.add(out_vertex);
				}
			}
		}
	}

	/*
	 * public static void writeToFile(String filename, String text) { try {
	 * PrintWriter out = new PrintWriter(new File(filename)); out.print(text);
	 * out.flush(); out.close(); } catch (FileNotFoundException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } }
	 */
}
