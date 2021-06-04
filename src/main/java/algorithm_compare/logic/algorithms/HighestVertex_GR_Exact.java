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
import mrmcmax.data_structures.linear.EraserLinkedList;
import mrmcmax.data_structures.linear.EraserLinkedList.Node;

public class HighestVertex_GR_Exact extends FlowAlgorithm {

	public HighestVertex_GR_Exact() {
		super("HighestVertex_GR_Exact");
	}

	protected ResidualGraphList g;
	protected int s;
	protected int t;
	protected int n;
	protected int m;
	protected ArrayList<Vertex> vertices;
	protected LinkedList<Vertex>[] activeHeights;
	protected EraserLinkedList<Vertex>[] nonActiveHeights;
	protected int b;
	protected int highestNonActiveHeight;
	protected int iteration = 0;
	// For global relabel update at the start
	protected EasyQueue<Vertex> q;
	protected boolean visited[];
	//For exact relabelling
	protected LinkedList<Vertex> relabelQueue;

	protected class Vertex {
		protected int v;
		protected int currentEdge;
		protected int height;
		protected int excess;
		protected Node<Vertex> nonActivePointer;

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
		activeHeights = new LinkedList[n];
		nonActiveHeights = new EraserLinkedList[n];
		for (int i = 0; i < n; i++) {
			activeHeights[i] = new LinkedList<Vertex>();
			nonActiveHeights[i] = new EraserLinkedList<Vertex>();
		}
		b = 0;
		q = new ArrayLimitQueue<Vertex>(Vertex.class, n);
		relabelQueue = new LinkedList<>();
		visited = new boolean[n];
		// Create vertices
		Vertex v;
		Node<Vertex> nonActivePtr;
		EraserLinkedList<Vertex> zeroHeight = nonActiveHeights[0];
		for (int i = 0; i < n; i++) {
			v = new Vertex(i);
			nonActivePtr = zeroHeight.addAndReturnPointer(v);
			v.nonActivePointer = nonActivePtr;
			vertices.add(v);
		}
		Vertex source = vertices.get(s);
		nonActiveHeights[0].remove(source.nonActivePointer);
		source.nonActivePointer = null;
		source.increaseHeightBy(n);
		highestNonActiveHeight = 0;
	}

	protected void initAlgorithm() {
		// Send flow to the adjacent to s, saturating them
		List<OneEndpointEdge> adjS = g.getAdjacencyList(s);
		OneEndpointEdge e = null;
		EraserLinkedList<Vertex> zeroHeight = nonActiveHeights[0];
		for (int i = 0; i < adjS.size(); i++) {
			e = adjS.get(i);
			if (e.remainingCapacity() <= 0)
				continue;
			e.augment(e.capacity);
			g.getAdjacencyList(e.endVertex).get(e.reverseEdgeIndex).decrement(e.capacity);
			if (e.endVertex != t) {
				Vertex w = vertices.get(e.endVertex);
				if (!w.isActive()) { //Not needed I think
					zeroHeight.remove(w.nonActivePointer);
					w.nonActivePointer = null;
					activeHeights[0].add(w);
				}
				w.increaseExcessBy(e.capacity);
			}
		}
		// No need to change current edge of s because it will return to the start.
		globalRelabel();
	}

	protected long algorithm() {
		System.out.println(DEBUG);
		// Go
		while (thereAreVerticesWithExcess()) {
			iteration++;
			Vertex vertex = getVertexWithExcess(); // Peeks
			if (vertex.height >= n) {
				activeHeights[b].pop(); // It has been changed by a gap labelling
				continue;
			}
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
						if (DEBUG) {
							//testTooSteep(vertex, edge, v_h);
						}
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
							nonActiveHeights[w.height].remove(w.nonActivePointer);
							w.nonActivePointer = null;
							activeHeights[w.height].add(w);
						}
						if (DEBUG) {
							// Make sure that the vertex that has excess is stored
							//testStored(w);
						}
					}
					// Push might clear excess
					vertex.decreaseExcessBy(delta);
					if (vertex.excess == 0) {
						vertex.nonActivePointer = nonActiveHeights[vertex.height].addAndReturnPointer(vertex);
						LinkedList<Vertex> vertexHeight = activeHeights[vertex.height];
						vertexHeight.pop(); // If we are working with vertex, it is at the start of the list
						if (vertexHeight.isEmpty()) {
							b--;
						}
					}
				} else {
					vertex.currentEdge = e; //To know that it has finished
					// Let's start the relabel queue by adding "vertex". Then, the relabel function will add
					// the required vertices to the queue.
					relabelQueue.add(vertex);
					while (!relabelQueue.isEmpty()) {
						//if (DEBUG && iteration % 100 == 0) System.out.println(relabelQueue);
						vertex = relabelQueue.poll();
						//The vertex could find a new current edge - operation 1
						adj = g.getAdjacencyList(vertex.v);
						boolean found = false;
						while (vertex.currentEdge < adj.size() && !found) {
							edge = adj.get(vertex.currentEdge);
							if (edge.remainingCapacity() > 0 &&
									(vertices.get(edge.endVertex).height == vertex.height - 1)) {
								found = true;
							} else {
								vertex.currentEdge += 1;
							}
						}
						if (found) continue;
						// Else, relabel sets the new height of the vertex.
						int oldHeight = vertex.height;
						//exactRelabel(vertex);
						relabelByMin(vertex);
						int newHeight = vertex.height;
						if (vertex.excess > 0) {
							activeHeights[oldHeight].pop();
						} else {
							nonActiveHeights[oldHeight].remove(vertex.nonActivePointer);
							vertex.nonActivePointer = null;
						}
						if (newHeight < n) {
							highestNonActiveHeight = Math.max(newHeight, highestNonActiveHeight);
						}
						// WE MIGHT HAVE A GAP
						//DEBUG: don't do gap relabelling
						if (activeHeights[oldHeight].isEmpty() && nonActiveHeights[oldHeight].isEmpty()) {
							// The vertex is above a gap
							vertex.height = n;
							performGapRelabelling(oldHeight);
							//The other vertices in the queue were at a distance higher than the vertex above the gap.
							//They were cleared by the gap relabelling. They are not needed anymore.
							relabelQueue.clear();
							// There are no other excess vertices at this height
							b--;
						} else if (newHeight >= n) {
							// Vertex might have risen to n. There's no more augmenting path for him.
							// But there are still non-active vertices at height oldHeight, so there's no gap.
							// The vertices in the relabel queue might find another current edge.
							if (activeHeights[oldHeight].isEmpty()) {
								b--;
							}
						} else {
							// Normal relabel situation
							if (vertex.excess > 0) {
								activeHeights[newHeight].add(vertex);
								b = newHeight;
							} else {
								vertex.nonActivePointer = nonActiveHeights[newHeight].addAndReturnPointer(vertex);
								highestNonActiveHeight = Math.max(highestNonActiveHeight, newHeight);
							}
							//b = newHeight;
							vertex.setCurrentEdge(0);
						}
					}
					relabel = true;
				}
			}
		}
		// That's it. Calculate the value of the flow.
		return calculateMaxFlow();
	}
	
	/**
	 * Clears all the vertices that are above oldHeight.
	 * Does not change b or the queue.
	 * @param oldHeight
	 */
	protected void performGapRelabelling(int oldHeight) {
		// RELABEL GLOBAL
		// Set to n the height of all vertices above oldHeight.
		// We only need to check those above oldHeight. They will be either
		// in activeHeights or nonActiveHeights.
		Vertex w;
		LinkedList<Vertex> activeHeight;
		EraserLinkedList<Vertex> nonActiveHeight;
		for (int i = oldHeight + 1; i <= highestNonActiveHeight; i++) {
			activeHeight = activeHeights[i];
			nonActiveHeight = nonActiveHeights[i];
			while (!activeHeight.isEmpty()) {
				w = activeHeight.poll();
				w.height = n;
			}
			while (!nonActiveHeight.isEmpty()) {
				w = nonActiveHeight.poll();
				w.nonActivePointer = null;
				w.height = n;
			}
		}
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
			//testB(ret);
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
	}

	protected void exactRelabel(Vertex vertex) {
		List<OneEndpointEdge> adj = g.getAdjacencyList(vertex.v);
		int newHeight = Integer.MAX_VALUE;
		int oldHeight = vertex.height;
		OneEndpointEdge edge = null;
		for (int i = 0; i < adj.size(); i++) {
			edge = adj.get(i);
			if (edge.remainingCapacity() > 0) {
				// Checking for which height to go
				Vertex outVertex = vertices.get(edge.endVertex);
				newHeight = Math.min(newHeight, outVertex.height);
				if ((edge.endVertex != s || edge.endVertex != t) &&
						(outVertex.height == oldHeight + 1) && 
						(outVertex.currentEdge == edge.reverseEdgeIndex)) {
					//We have to change the current edge of this vertex, and maybe relabel it
					relabelQueue.add(outVertex);
				}
			}
		}
		newHeight++;
		vertex.height = newHeight;
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
		// Backwards BFS from t
		// It will follow residual edges.
		// The distance is set from the vertex that is popped to the adjacent ones
		// that haven't been visited yet.
		// The heights might change completely. The easiest thing is to clear the
		// height data structures and fill them again.
		// PUERTO RICO ME LO REGALO
		Arrays.fill(visited, false);
		EraserLinkedList<Vertex> allVertices = new EraserLinkedList<>();
		Vertex v;
		for (int i = 0; i < n; i++) {
			activeHeights[i].clear();
			nonActiveHeights[i].clear();
			v = vertices.get(i);
			v.nonActivePointer = allVertices.addAndReturnPointer(v);
		}
		Vertex source = vertices.get(s);
		allVertices.remove(source.nonActivePointer);
		source.nonActivePointer = null;
		Vertex sink = vertices.get(t);
		allVertices.remove(sink.nonActivePointer);
		sink.nonActivePointer = nonActiveHeights[0].addAndReturnPointer(sink);
		b = 0;
		visited[t] = true;
		visited[s] = true;
		q.reset();
		q.add(sink);
		while (!q.isEmpty()) {
			Vertex vertex = q.poll();
			vertex.currentEdge = -1;
			int newHeight = vertex.height + 1;
			List<OneEndpointEdge> adj = g.getAdjacencyList(vertex.v);
			OneEndpointEdge edge, reverseEdge;
			int out_v;
			Vertex out_vertex;
			for (int i = 0; i < adj.size(); i++) {
				edge = adj.get(i);
				out_v = edge.endVertex;
				out_vertex = vertices.get(out_v);
				if (visited[out_v]) {
					//It might be a chance to set the current edge
					if (vertex.currentEdge == -1 && 
							out_vertex.height == vertex.height - 1 &&
							edge.remainingCapacity() > 0) {
						vertex.currentEdge = i;
					}
					continue;
				} //Else, it is a vertex at a higher distance. It won't be eligible for current edge
				reverseEdge = g.getAdjacencyList(out_v).get(edge.reverseEdgeIndex);
				if (reverseEdge.remainingCapacity() > 0) { // This applies both to backward and forward edges
					// We got an augmenting path
					visited[out_v] = true;
					out_vertex.height = newHeight;
					allVertices.remove(out_vertex.nonActivePointer);
					if (out_vertex.isActive()) {
						out_vertex.nonActivePointer = null;
						activeHeights[newHeight].add(out_vertex);
						//This height is less than n because it was found with a backwards BFS
						b = Math.max(b, newHeight);
					} else {
						out_vertex.nonActivePointer = nonActiveHeights[newHeight].addAndReturnPointer(out_vertex);
					}
					if (DEBUG) {
						//testAugPathN(newHeight, out_vertex);
					}
					q.add(out_vertex);
				}
			}
		}
		while (!allVertices.isEmpty()) {
			v = allVertices.poll();
			v.nonActivePointer = null;
			v.height = n;
		}
		// End of global relabel
		if (DEBUG) {testGlobalRelabel();System.out.println("Debug");}
	}

	/*
	 * TESTS
	 */

	protected void testTooSteep(Vertex vertex, OneEndpointEdge edge, int v_h) {
		if (edge.remainingCapacity() > 0 && vertices.get(edge.endVertex).height < v_h - 1) {
			System.err.println("Iteration " + iteration);
			throw new RuntimeException("EDGE TOO STEEP: (" + vertex.v + ", " + edge.endVertex + "), heights: "
					+ vertex.height + ", " + vertices.get(edge.endVertex).height);
		}
	}

	protected void testStored(Vertex w) {
		List<Vertex> h = activeHeights[w.height];
		Iterator<Vertex> it = h.iterator();
		int i = 0;
		while (it.hasNext()) {
			Vertex inList = it.next();
			if (inList.v != w.v)
				i++;
		}
		boolean found = i < h.size();
		if (!found) {
			System.err.println("Iteration " + iteration);
			throw new RuntimeException("Active vertex " + w.v + " not found " + "at height " + w.height);
		}
	}

	protected void testValidRelabel(Vertex vertex, List<OneEndpointEdge> adj) {
		// check that it has excess and it cannot push forward
		/*
		if (vertex.isActive()) {
			OneEndpointEdge outEdge;
			for (int i = 0; i < adj.size(); i++) {
				outEdge = adj.get(i);
				if (outEdge.remainingCapacity() > 0 && vertices.get(outEdge.endVertex).height == vertex.height - 1) {
					System.err.println("Iteration " + iteration);
					throw new RuntimeException("Relabelling vertex " + vertex.v + " at height " + vertex.height
							+ " when there's a valid edge: " + outEdge + " leading to " + outEdge.endVertex
							+ " at height " + vertices.get(outEdge.endVertex).height);
				}

			}
		} else {
			System.err.println("Iteration " + iteration);
			throw new RuntimeException(
					"Relabelling a vertex without excess: " + vertex.v + " at height " + vertex.height);
		}
		*/
	}
	
	protected void testB(boolean ret) {
		/*
		int i = 0;
		while (i < n && (activeHeights[i] == null || activeHeights[i].isEmpty())) {
			i++;
		}
		boolean excess = i < n;
		if (excess && !ret) {
			System.err.println("Iteration " + iteration);
			throw new RuntimeException("There was a vertex with excess at height " + i + " but the pointer was "
					+ "at height " + b + " and it found none");
		}
		*/
	}
	
	protected void testAugPathN(int newHeight, Vertex out_vertex) {
		if (newHeight >= n) {
			System.err.println("Iteration " + iteration);
			throw new RuntimeException("Found an augmenting path from " + out_vertex.v
					+ " to t where v has a height " + newHeight);
		}
	}

	protected void testGlobalRelabel() {
		// Check that the conditions hold
		/*
		for (int i = 0; i < vertices.size(); i++) {
			if (i == s || i == t)
				continue;
			List<OneEndpointEdge> adj = g.getAdjacencyList(i);
			Vertex vertex = vertices.get(i);
			int height = vertex.height;
			// BFS to see if the height is correct

			// Check if it has too steep edges
			for (int j = 0; j < adj.size(); j++) {
				OneEndpointEdge e = adj.get(j);
				if (e.remainingCapacity() > 0) {
					Vertex outVertex = vertices.get(e.endVertex);
					if (outVertex.height < vertex.height - 1) {
						System.err.println("Iteration " + iteration);
						throw new RuntimeException("Global relabelling made edge too steep: " + vertex.v + ", "
								+ outVertex.v + " at heights " + vertex.height + ", " + outVertex.height);
					}
				}
			}
		}
		*/
	}

	/*
	 * public static void writeToFile(String filename, String text) { try {
	 * PrintWriter out = new PrintWriter(new File(filename)); out.print(text);
	 * out.flush(); out.close(); } catch (FileNotFoundException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } }
	 */
}
