package algorithm_compare.logic;

import mrmcmax.data_structures.graphs.ResidualGraphList;

public abstract class Algorithm {
	private String name;
	
	public Algorithm(String name) {
		this.name = name;
	}
	
	@Override
	public boolean equals(Object other) {
		return (other instanceof Algorithm) &&
				((Algorithm) other).name.equals(this.name);
	}

	public String getName() {
		return name;
	}
	
	public abstract long maxFlow(ResidualGraphList g);
}
