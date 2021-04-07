package algorithm_compare.persistence;

public class GraphData {
	public int n;
	public int s;
	public int t;
	
	public TwoEndpointEdge[] edges;
	
	public GraphData(int n, int s, int t, TwoEndpointEdge[] edges) {
		this.n = n;
		this.edges = edges;
		this.s = s;
		this.t = t;
	}
	
	public static class TwoEndpointEdge {
		public int v_in;
		public int v_out;
		public int capacity;
		
		public TwoEndpointEdge(int v_in, int v_out, int capacity) {
			this.v_in = v_in;
			this.v_out = v_out;
			this.capacity = capacity;
		}
	}
}
