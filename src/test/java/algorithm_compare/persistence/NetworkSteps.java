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

public class NetworkSteps {
	
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
	
	/*
	 * FOR STORE, RETRIEVE AND DELETE VALUES FEATURE
	 */
	
	long storedTime;
	long retrievedTime;
	String algorithm;
	
	@When("we store the long value {long} for algorithm {string}")
	public void weStoreTheLongValueForAlgorithm(Long time, String alg) {
		this.storedTime = time;
		this.algorithm = alg;
		try {
			persistenceService.storeTimes(testGraph, new String[] {alg}, new long[] {time});
		} catch (Exception e) {
			e.printStackTrace();
			fail(e);
		}
	}

	@Then("we can retrieve that value afterwards")
	public void weCanRetrieveThatValueAfterwards() {
		try {
			retrievedTime = persistenceService.retrieveTimes(testGraph, new String[] {algorithm})[0];
			assertEquals(storedTime, retrievedTime);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e);
		}
	}

	@When("we delete the value for algorithm {string}")
	public void weDeleteTheValueForAlgorithm(String alg) {
		try {
			persistenceService.deleteTimes(testGraph, new String[] {alg});
		} catch (Exception e) {
			e.printStackTrace();
			fail(e);
		}
	}

	@Then("we do not have any value for the algorithm {string}")
	public void weDoNotHaveAnyValueForTheAlgorithm(String alg) {
		try {
			long[] result = persistenceService.retrieveTimes(testGraph, new String[] {alg});
			assertEquals((long) -1, result[0]);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e);
		}
	}
}

