package algorithm_compare.logic.algorithms;

import java.util.List;

import mrmcmax.data_structures.graphs.OneEndpointEdge;

public class FIFOPushRelabel2 extends FIFOPushRelabel1 {
	public FIFOPushRelabel2() {
		super("FIFOPushRelabel2");
	}
	
	public FIFOPushRelabel2(String string) {
		super(string);
	}

	@Override
	protected void relabel(int vertex) {
		List<OneEndpointEdge> adj = g.getAdjacencyList(vertex);
		int newHeight = Integer.MAX_VALUE;
		for (int i = 0; i < adj.size(); i++) {
			if (adj.get(i).remainingCapacity() > 0) {
				newHeight = Math.min(newHeight, heights.get(adj.get(i).endVertex));
			}
		}
		heights.set(vertex, newHeight + 1);
	}
}
