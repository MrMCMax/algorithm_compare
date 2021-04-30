package algorithm_compare.logic.algorithms;

import mrmcmax.data_structures.graphs.ResidualGraphList;

public class NaivePushRelabel extends FlowAlgorithm {

	public NaivePushRelabel() {
		super("NaivePushRelabel");
	}

	@Override
	public long maxFlow(ResidualGraphList g) {
		return g.PushRelabel1Algorithm(g.getSource(), g.getSink());
	}
	
}
