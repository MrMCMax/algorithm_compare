package algorithm_compare.logic.algorithms;

import mrmcmax.data_structures.graphs.ResidualGraphList;

public abstract class FlowAlgorithm {
	private String name;
	
	public static boolean DEBUG = false;
	
	public FlowAlgorithm(String name) {
		this.name = name;
	}
	
	@Override
	public boolean equals(Object other) {
		return (other instanceof FlowAlgorithm) &&
				((FlowAlgorithm) other).name.equals(this.name);
	}

	public String getName() {
		return name;
	}
	
	public abstract long maxFlow(ResidualGraphList g);
}
