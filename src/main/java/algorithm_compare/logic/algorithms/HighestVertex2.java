package algorithm_compare.logic.algorithms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import algorithm_compare.logic.algorithms.HighestVertex2.Vertex;
import mrmcmax.data_structures.graphs.OneEndpointEdge;
import mrmcmax.data_structures.graphs.ResidualGraphList;

public class HighestVertex2 extends FlowAlgorithm {

	public HighestVertex2() {
		super("HighestVertex2");
	}

	protected ResidualGraphList g;
	protected int s;
	protected int t;
	protected int n;
	protected ArrayList<Vertex> vertices;
	protected LinkedList<Vertex>[] activeHeights;
	protected LinkedList<Integer> b;
	//For debugging purposes
	protected int iteration = 0;

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
		for (int i = 0; i < n+1; i++) {
			activeHeights[i] = new LinkedList<Vertex>();
		}
		b = new LinkedList<Integer>();
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
		for (int i = 0; i < adjS.size(); i++) {
			e = adjS.get(i);
			if (e.remainingCapacity() <= 0)
				continue;
			e.augment(e.capacity);
			g.getAdjacencyList(e.endVertex).get(e.reverseEdgeIndex).decrement(e.capacity);
			if (e.endVertex != t) {
				Vertex w = vertices.get(e.endVertex);
				w.increaseExcessBy(e.capacity); // Excesses start at 0 in this implementation
				activeHeights[0].add(w);
				if (b.isEmpty()) {
					b.push(0); //No other height at the start of the algorithm
				}
			}
		}
		// No need to change current edge of s because it will return to the start.
	}

	protected long algorithm() {
		// Go
		long maxFlow = 0;
		while (thereAreVerticesWithExcess()) {
			if (DEBUG) {
				iteration++;
			}
			Vertex vertex = getVertexWithExcess(); // Peeks
			if (DEBUG) {
				int height = 0;
				Vertex maxVertex = null;
				for (int i = 0; i < vertices.size(); i++) {
					if (i == s || i == t) continue;
					if (vertices.get(i).isActive() && vertices.get(i).height > height) {
						height = Math.max(vertices.get(i).height, height);
						maxVertex = vertices.get(i);
					}
				}
				if (vertex.height < height) {
					System.out.println("Iteration " + iteration + ":");
					throw new RuntimeException("Candidate vertex " + vertex.v + " at height " + vertex.height + " was selected,"
							+ "even though the highest height is of the vertex " + maxVertex.v + " at height " + maxVertex.height);
				}
			}
			//End debug
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
							// New vertex with excess. Must add to the data structure.
							activeHeights[w.height].add(w);
						}
						//Add the vertex just behind the stack
						if (b.get(1) < w.height) {
							b.add(1, w.height);
						}
					}
					// Push might clear excess
					vertex.decreaseExcessBy(delta);
					if (vertex.excess == 0) {
						LinkedList<Vertex> vertexHeight = activeHeights[vertex.height];
						vertexHeight.pop();
						if (vertexHeight.isEmpty()) {
							b.pop();
						}
					}
				} else { //Relabel
					activeHeights[vertex.height].pop();
					if (activeHeights[vertex.height].isEmpty()) {
						b.pop();
					}
					relabelByMin(vertex);
					if (vertex.height < n) { //Still active
						activeHeights[vertex.height].add(vertex);
						b.push(vertex.height);
					}
					vertex.setCurrentEdge(0);
					relabel = true;
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
		int h = b.peek();
		return activeHeights[h].peek();
	}

	protected boolean thereAreVerticesWithExcess() {
		return !b.isEmpty();
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
	}
	
	protected void relabelBy1(Vertex vertex) {
		vertex.height+=1;
	}
	
	public static void writeToFile(String filename, String text) {
		try {
			PrintWriter out = new PrintWriter(new File(filename));
			out.print(text);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
