package algorithm_compare.logic;

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
	
	public abstract long maxFlow(ResidualGraph g);
}
