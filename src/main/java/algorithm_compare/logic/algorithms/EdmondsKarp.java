package algorithm_compare.logic.algorithms;

import mrmcmax.data_structures.graphs.OneEndpointEdge;
import mrmcmax.data_structures.graphs.ResidualGraphList;
import mrmcmax.data_structures.graphs.Graph.Backtrack;

public class EdmondsKarp extends FlowAlgorithm {

	public EdmondsKarp() {
		super("EdmondsKarp");
	}
	
	private ResidualGraphList g;
	
	@Override
	public long maxFlow(ResidualGraphList g) {
		return g.EdmondsKarp(g.getSource(), g.getSink());
		/*
		this.g = g;
		return EdmondsKarpDelta(g.getSource(), g.getSink(), 0);
		*/
	}
	/*
	protected int EdmondsKarpDelta(int s, int t, int delta) {
		int maxFlow = 0;

		while (g.hasAugmentingPath(s, t, (edge) -> edge.remainingCapacity() > delta)) {
			int bottleneck = Integer.MAX_VALUE;
			int v_out = t;

			// Find the bottleneck value
			while (v_out != s) {
				Backtrack back = g.bfsBacktrack[v_out];
				OneEndpointEdge e = g.getAdjacencyList(back.v_in).get(back.edgeIndex);
				bottleneck = Math.min(bottleneck, e.remainingCapacity());
				v_out = back.v_in;
			}

			// update the network
			v_out = t;
			while (v_out != s) {
				Backtrack back = g.bfsBacktrack[v_out];
				int v_in = back.v_in;
				OneEndpointEdge forward = g.getAdjacencyList(back.v_in).get(back.edgeIndex);
				forward.augment(bottleneck);
				g.getAdjacencyList(v_out).get(forward.reverseEdgeIndex).decrement(bottleneck);
				v_out = v_in;
			}
			maxFlow += bottleneck;
		}
		return maxFlow;
	}
	*/
}
