package algorithm_compare.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import algorithm_compare.persistence.GraphData.TwoEndpointEdge;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ReadNetworkSteps {
	
	String testGraph;
	IPersistenceService persistenceService;
	GraphData gd;
	boolean hadException;
	
	@Given("The path to testgraph")
	public void thePathToTestgraph() {
		try {
		persistenceService = new PersistenceService();
		testGraph = "testgraph.txt";
		List<String> networks = persistenceService.getAllNetworkNames();
		assertTrue(networks.contains(testGraph));
		} catch (Exception e) {
			System.err.println(e);
			fail(e);
		}
	}
	
	@When("testgraph is read")
	public void testgraphIsRead() {
		try {
			gd = persistenceService.loadNetwork(testGraph);
			hadException = false;
		} catch (Exception e) {
			hadException = true;
			e.printStackTrace();
		}
	}
	
	@Then("no exceptions are thrown")
	public void noExceptionsAreThrown() {
		assertFalse(hadException);
	}
	
	@Then("the source is {int}")
	public void theSourceIs(Integer int1) {
		assertEquals(int1 - 1, gd.s);
	}
	
	@Then("the sink is {int}")
	public void theSinkIs(Integer int1) {
		assertEquals(int1 - 1, gd.t);
	}
	
	@Then("there exists an edge from {int} to {int} with cap {int}")
	public void thereExistsAnEdgeFromToWithCap(Integer v_in, Integer v_out, Integer cap) {
		TwoEndpointEdge[] edges = gd.edges;
		v_in -= 1;
		v_out -= 1;
		boolean found = false;
		int i = 0;
		while (!found && i < edges.length) {
			if (edges[i].v_in == v_in && edges[i].v_out == v_out && edges[i].capacity == cap) {
				found = true;
			} else {
				i++;
			}
		}
		assertTrue(found);
	}
}

