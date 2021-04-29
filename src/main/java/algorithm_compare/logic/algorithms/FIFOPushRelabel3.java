package algorithm_compare.logic.algorithms;

import java.util.List;

import mrmcmax.data_structures.graphs.OneEndpointEdge;

public class FIFOPushRelabel3 extends FIFOPushRelabel2 {
	
	public FIFOPushRelabel3() {
		super("FIFOPushRelabel3");
	}
	
	@Override
	protected int getVertexWithExcess() {
		return FIFOQueue.poll();
	}
	
	@Override
	protected void addVertexWithExcess(int v) {
		if (heights.get(v) < n) 
			FIFOQueue.add(v);
	}
	
	@Override
	protected boolean thereAreVerticesWithExcess() {
		return FIFOQueue.isEmpty();
	}

	@Override
	protected long calculateMaxFlow() {
		long maxFlow = 0;
		List<OneEndpointEdge> adjT = g.getAdjacencyList(t);
		for (int i = 0; i < adjT.size(); i++) {
			OneEndpointEdge e = adjT.get(i);
			//Count backward edges from t
			if (e.capacity == 0) {
				maxFlow += e.remainingCapacity();
			}
		}
		return maxFlow;
	}
}
