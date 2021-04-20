package algorithm_compare.logic.algorithms;

import java.util.ArrayList;
import java.util.List;

import mrmcmax.data_structures.graphs.OneEndpointEdge;
import mrmcmax.data_structures.graphs.ResidualGraphList;

public class FirstPushRelabel extends FlowAlgorithm {
	
	public FirstPushRelabel() {
		super("FirstPushRelabel");
	}

	/**
	 * Precondition: all flows are zero
	 */
	@Override
	public long maxFlow(ResidualGraphList g) {
		//Set up data structures
		int n = g.getNumVertices();
		int maxHeight = 2*n - 1;
		ArrayList<ArrayList<Integer>> heights = new ArrayList<>(maxHeight);
		
		for (int i = 0; i < maxHeight; i++) {
			heights.add(new ArrayList<>());
		}
		//Initialization
		List<OneEndpointEdge> adjS = g.getAdjacencyList(g.getSource());
		
		//for (int i = 0; i < )
		return 0;
	}

}
