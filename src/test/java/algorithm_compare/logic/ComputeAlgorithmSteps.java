package algorithm_compare.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import algorithm_compare.TestUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ComputeAlgorithmSteps {
	
	ILogicService logicService;
	String network;
	String[] names;
	long[] results;
	
	@Given("the test network {string}")
	public void theTestNetwork(String net) {
		network = net;
	}
	
	@Given("the list of algorithms {string}")
	public void theListOfAlgorithms(String raw) {
		names = TestUtils.level0Separation(raw);
	}
	
	@Given("a default logic service")
	public void aDefaultLogicService() {
		try {
		logicService = new LogicService();
		} catch (IOException e) {
			System.err.println(e);
			fail(e);
		}
	}
	
	@When("We run the algorithms on the network")
	public void weRunTheAlgorithmsOnTheNetwork() {
		try {
			results = logicService.computeNetworkWithAlgorithms(network, names);
			if (results == null) {
				fail("No results given");
			}
		} catch (Exception e) {
			System.err.println(e);
			fail(e.getMessage());
		}
	}
	
	@Then("we get the result {long} on all of them")
	public void weGetTheResultOnAllOfThem(Long maxFlow) {
		for (long res : results) {
			assertEquals(maxFlow, res, "Max flow value");
		}
	}
}

