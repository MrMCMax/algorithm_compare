package algorithm_compare.logic.algorithms;

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

public class HV_Gap_Gl_True_Exact extends FlowAlgorithm {

	public HV_Gap_Gl_True_Exact() {
		super("HV_Gap_Gl_True_Exact");
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
	// For initial global relabel update
	protected EasyQueue<Vertex> q;
	protected boolean visited[];
	// For gap relabel:
	protected int gap_count = 0;
	// For exact labelling:
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
		source.increaseHeightBy(n);
		nonActiveHeights[0].remove(source.nonActivePointer);
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
				if (!w.isActive()) {
					zeroHeight.remove(w.nonActivePointer);
					w.nonActivePointer = null;
					activeHeights[0].add(w);
				}
				w.increaseExcessBy(e.capacity);
			}
		}
		if (DEBUG) {
			// System.out.println(iteration);
			System.out.println(DEBUG);
		}
		// No need to change current edge of s because it will return to the start.
		// We disable the initial exact labelling because it hurts the gap heuristic
		// and the overall performance.
		// globalRelabel();
	}

	protected long algorithm() {
		// Go
		while (thereAreVerticesWithExcess()) {
			if (DEBUG) {
				iteration++;
			}
			Vertex vertex = getVertexWithExcess(); // Peeks
			if (vertex.height >= n) {
				System.out.println("Weirdness");
				activeHeights[b].pop(); // It has been changed by a gap labelling
				continue;
			}
			List<OneEndpointEdge> adj = g.getAdjacencyList(vertex.v);
			int e = vertex.currentEdge;
			int v_h = vertex.height;
			boolean relabel = false;
			boolean currentEdgeValid = false;
			// While there's no relabel and there's still excess
			while (!relabel && !currentEdgeValid) {
				// Search for a valid edge
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
				vertex.setCurrentEdge(e);
				if (eligible) {
					//We have found a good edge. If the vertex has excess, we shall push.
					if (vertex.excess > 0) {
						int delta = Math.min(vertex.excess, edge.remainingCapacity());
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
						// The push might clear the excess. In that case, we have to move the vertex.
						vertex.decreaseExcessBy(delta);
						if (vertex.excess == 0) {
							vertex.nonActivePointer = nonActiveHeights[vertex.height].addAndReturnPointer(vertex);
							highestNonActiveHeight = Math.max(highestNonActiveHeight, vertex.height);
							LinkedList<Vertex> vertexHeight = activeHeights[vertex.height];
							vertexHeight.pop(); // If we are working with vertex, it is at the start of the list
							if (vertexHeight.isEmpty()) {
								b--;
							}
							//If the edge is not saturated, then there has been a non-saturating push and the
							//current edge is valid
							if (edge.remainingCapacity() > 0) {
								currentEdgeValid = true;
							}
						}
					} else {
						//This was a good edge
						currentEdgeValid = true;
					}
				} else {
					//We have not found a good edge. We need to relabel. The vertex might not have excess anymore.
					if (DEBUG) {
						//testValidRelabel(vertex, adj);
					}
					relabelQueue.add(vertex);
					while (!relabelQueue.isEmpty()) {
						//The relabel can now happen on an active or non-active vertex.
						vertex = relabelQueue.poll();
						boolean wasActive = vertex.isActive();
						//OPERATION 1: Search for a valid current edge
						e = vertex.currentEdge;
						adj = g.getAdjacencyList(vertex.v);
						boolean valid = false;
						while (e < adj.size() && !valid) {
							edge = adj.get(e);
							if (edge.remainingCapacity() > 0 &&
									vertices.get(edge.endVertex).height == vertex.height - 1) {
								valid = true;
							} else {
								e++;
							}
						}
						vertex.setCurrentEdge(e);
						if (valid) continue; //We have found a current edge for this vertex
						// OPERATION 2: Relabel sets the new height of the vertex
						int oldHeight = vertex.height;
						exactRelabel(vertex);
						int newHeight = vertex.height;
						if (DEBUG) {
							if (newHeight <= oldHeight) {
								System.err.println("Iteration " + iteration);
								throw new RuntimeException("The label didn't increase in vertex" + vertex.v);
							}
						}
						//Remove the vertex from the previous position
						if (wasActive) {
							activeHeights[oldHeight].pop();
						} else {
							nonActiveHeights[oldHeight].remove(vertex.nonActivePointer);
							vertex.nonActivePointer = null;
						}
						// WE MIGHT HAVE A GAP. In that case, we won't add the vertex to the structures again.
						if (activeHeights[oldHeight].isEmpty() && nonActiveHeights[oldHeight].isEmpty()) {
							// if (DEBUG)
							//     testGapRelabel(oldHeight);
							// RELABEL GLOBAL
							// Set to n the height of all vertices above oldHeight.
							// We only need to check those above oldHeight. They will be in non-active heights
							// because the vertex at oldHeight was the highest, one and only, active vertex! 
							// We set it to n too
							if (DEBUG)
								gap_count++;
							vertex.height = n;
							Vertex w;
							EraserLinkedList<Vertex> nonActiveHeight;
							for (int i = oldHeight + 1; i <= highestNonActiveHeight; i++) {
								nonActiveHeight = nonActiveHeights[i];
								while (!nonActiveHeight.isEmpty()) {
									w = nonActiveHeight.poll();
									w.nonActivePointer = null;
									w.height = n;
								}
							}
							// There are no other active vertices above this height and below n.
							// But this gap could have happened above the highest active vertex in an exact relabelling operation.
							// Therefore we can only decrease b if the gap happened with the active vertex.
							if (wasActive && activeHeights[oldHeight].isEmpty())
								b--;
							// We can put a cap on the non-active height.
							highestNonActiveHeight = oldHeight - 1;
							//All vertices to relabel were above the gap
							relabelQueue.clear();
						} else if (newHeight >= n) {
							// Vertex has risen to n. There's no more augmenting path for him.
							// But there are still non-active vertices at height oldHeight, so there's no gap.
							// We cannot conclude anything about the highest non-active height.
							// But we can about the highest active height, because this was the highest active height.
							// EXACT DISTANCE LABELLING: ONLY IF THE VERTEX WAS ACTIVE!
							// In fact, this clause may only happen for the first vertex, since we always relabel vertices that
							// are higher up, and none of them have excess.
							if (wasActive && activeHeights[oldHeight].isEmpty()) {
								b--;
							}
						} else {
							// Normal relabel situation
							if (wasActive) {
								activeHeights[newHeight].add(vertex);
								b = newHeight;
							} else {
								vertex.nonActivePointer = nonActiveHeights[newHeight].addAndReturnPointer(vertex);
								highestNonActiveHeight = Math.max(highestNonActiveHeight, newHeight);
							}
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
			testThoroughB(b);
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

	/**
	 * For maximum relabel
	 * 
	 * @param vertex
	 */
	protected void exactRelabel(Vertex vertex) {
		List<OneEndpointEdge> adj = g.getAdjacencyList(vertex.v);
		int newHeight = Integer.MAX_VALUE;
		OneEndpointEdge edge = null;
		Vertex outVertex = null;
		for (int i = 0; i < adj.size(); i++) {
			edge = adj.get(i);
			if (edge.remainingCapacity() > 0) {
				newHeight = Math.min(newHeight, vertices.get(edge.endVertex).height);
			}
			// Add to relabel queue if the vertex is at the required height and the
			// current edge of it points to this vertex.
			if (edge.endVertex == t || edge.endVertex == s) continue;
			outVertex = vertices.get(edge.endVertex);
			if (outVertex.height == vertex.height + 1 && outVertex.currentEdge == edge.reverseEdgeIndex) {
				relabelQueue.add(outVertex);
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
		System.out.println("Gap count: " + gap_count);
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
		Vertex v;
		// Clear data structures. Create all vertices list.
		for (int i = 0; i < n; i++) {
			v = vertices.get(i);
			v.nonActivePointer = null;
			v.height = n;
		}
		for (int i = 0; i <= b; i++) {
			activeHeights[i].clear();
		}
		for (int i = 0; i <= highestNonActiveHeight; i++) {
			nonActiveHeights[i].clear();
		}
		b = 0;
		highestNonActiveHeight = 0;
		Vertex sink = vertices.get(t);
		sink.height = 0;
		// Start the BFS. Set source and sink as visited, reset queue, enqueue t.
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
				if (visited[out_v])
					continue;
				reverseEdge = g.getAdjacencyList(out_v).get(edge.reverseEdgeIndex);
				if (reverseEdge.remainingCapacity() > 0) { // This applies both to backward and forward edges
					// We got an augmenting path
					visited[out_v] = true;
					Vertex out_vertex = vertices.get(out_v);
					if (DEBUG) {
						testAugPathN(newHeight, out_vertex);
					}
					out_vertex.height = newHeight;
					out_vertex.currentEdge = 0;
					if (out_vertex.isActive()) {
						activeHeights[newHeight].add(out_vertex);
						out_vertex.nonActivePointer = null;
						if (newHeight >= n)
							throw new RuntimeException("Found a vertex at height n with global relabelling");
						b = Math.max(b, newHeight);
					} else {
						out_vertex.nonActivePointer = nonActiveHeights[newHeight].addAndReturnPointer(out_vertex);
						highestNonActiveHeight = Math.max(highestNonActiveHeight, newHeight);
					}
					q.add(out_vertex);
				}
			}
		}
		// End of global relabel
		System.out.println("Global relabel iteration " + iteration);
		if (DEBUG)
			testGlobalRelabel();
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
		//if (vertex.isActive()) {
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
		//} else {
			//System.err.println("Iteration " + iteration);
			//throw new RuntimeException(
		//			"Relabelling a vertex without excess: " + vertex.v + " at height " + vertex.height);
		//}
	}

	protected void testThoroughB(int b) {
		int i = n-1;
		while (i >= 0 && activeHeights[i].isEmpty()) i--;
		if (b < i) {
			System.err.println("Iteration: " + iteration);
			throw new RuntimeException("There was a vertex with excess at height " + i + " but hte pointer was at " 
					 + b);
		}
	}
	
	protected void testB(boolean ret) {
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
	}

	protected void testAugPathN(int newHeight, Vertex out_vertex) {
		if (newHeight >= n) {
			System.err.println("Iteration " + iteration);
			throw new RuntimeException(
					"Found an augmenting path from " + out_vertex.v + " to t where v has a height " + newHeight);
		}
	}

	protected void testGlobalRelabel() {
		// Check that the conditions hold
		for (int i = 0; i < vertices.size(); i++) {
			if (i == s || i == t)
				continue;
			List<OneEndpointEdge> adj = g.getAdjacencyList(i);
			Vertex vertex = vertices.get(i);
			int height = vertex.height;
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
	}
	
	protected void testGapRelabel(int gap) {
		LinkedList<Vertex> activeHeight;
		EraserLinkedList<Vertex> nonActiveHeight;
		for (int i = gap + 1; i <= b; i++) {
			activeHeight = activeHeights[i];
			for (Vertex u : activeHeight) {
				for (OneEndpointEdge edge : g.getAdjacencyList(u.v)) {
					//Not implemented in the end
				}
			}
		}
	}
	
	protected void testBigGapRelabel() {
		//Check that the conditions hold, and that all the vertices with paths are still considered
		int[] realDistances = new int[n];
		Arrays.fill(realDistances, n);
		realDistances[t] = 0;
		EasyQueue<Vertex> queue = new ArrayLimitQueue<Vertex>(Vertex.class, n);
		queue.add(vertices.get(t));
		Vertex vertex;
		List<OneEndpointEdge> adj;
		while (!queue.isEmpty()) {
			vertex = queue.poll();
			adj = g.getAdjacencyList(vertex.v);
			int newRealDistance = realDistances[vertex.v] + 1;
			OneEndpointEdge edge; OneEndpointEdge reverseEdge; Vertex u;
			for (int i = 0; i < adj.size(); i++) {
				edge = adj.get(i);
				u = vertices.get(edge.endVertex);
				if (realDistances[u.v] < n) continue; //visited
				reverseEdge = g.getAdjacencyList(u.v).get(edge.reverseEdgeIndex);
				if (reverseEdge.remainingCapacity() <= 0) continue; //saturated edge
				realDistances[u.v] = newRealDistance;
				if (u.height > newRealDistance) {
					System.err.println("Iteration: " + iteration);
					throw new RuntimeException("Vertex " + u.v + " has a height of " + u.height +
							" even though its real distance is " + newRealDistance);
				}				
				if (vertex.height < u.height - 1 && edge.remainingCapacity() > 0) {
					System.err.println("Iteration: " + iteration);
					throw new RuntimeException("Edge too steep: from " + u.v + " to " + vertex.v + " with heights "+
							u.height + ", " + vertex.height);
				}
				queue.add(u);
			}
		}
	}
}
