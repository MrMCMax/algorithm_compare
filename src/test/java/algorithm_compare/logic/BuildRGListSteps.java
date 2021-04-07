package algorithm_compare.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import mrmcmax.data_structures.graphs.OneEndpointEdge;
import mrmcmax.data_structures.graphs.ResidualGraphList;

public class BuildRGListSteps {

	LogicService logicService;
	ResidualGraphList rg;

	@Given("The GraphData from testgraph.txt")
	public void theGraphDataFromTestgraphTxt() {
		try {
			logicService = new LogicService();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e);
		}
	}

	@When("the graph is built")
	public void theGraphIsBuilt() {
		try {
			rg = logicService.getGraph("testgraph.txt");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e);
		}
	}

	@Then("the graph has {int} vertices")
	public void theGraphHasVertices(Integer v) {
		assertEquals(v, rg.getNumVertices());
	}

	@Then("the graph has an edge from {int} to {int} with cap {int}")
	public void theGraphHasAnEdgeFromToWithCap(Integer v_in, Integer v_out, Integer cap) {
		try {
			v_in -= 1;
			v_out -= 1;
			List<OneEndpointEdge> adj = rg.getAdjacencyList(v_in);
			boolean found = false;
			int i = 0;
			while (!found && i < adj.size()) {
				OneEndpointEdge edge = adj.get(i++);
				if (edge.endVertex == v_out && edge.capacity == cap) {
					found = true;
				}
			}
			assertTrue(found);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e);
		}
	}
}
