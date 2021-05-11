package algorithm_compare.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Arrays;

import algorithm_compare.TestUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ComputeAlgorithmSteps {
	
	ILogicService logicService;
	String network;
	String[] names;
	long[] results;
	static final int N_REPETITIONS = 100;
	
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
		weRunTheAlgorithmOnTheNetworkTimes(1);
	}
	
	@Then("we get the result {long} on all of them")
	public void weGetTheResultOnAllOfThem(Long maxFlow) {
		for (int i = 0; i < results.length; i++) {
			long res = results[i];
			assertEquals(maxFlow, res, "Max flow value with algorithm " + names[i]);
		}
	}
	
	@Then("we can retrieve the times for the algorithms")
	public void weCanRetrieveTheTimesForTheAlgorithms() {
		try {
			long[] times = logicService.retrieveTimes(network, names);
			System.out.println(Arrays.toString(times));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/*
	 * RUNTIME TESTS
	 */
	
	@Given("the algorithm {string}")
	public void theAlgorithm(String algName) {
		names = new String[] {algName};
	}

	@When("We run the algorithm on the network multiple times")
	public void weRunTheAlgorithmOnTheNetworkMultipleTimes() {
		try {
			results = logicService.computeNetworkWithAlgorithms(network, names, N_REPETITIONS);
			if (results == null) {
				fail("No results given");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@When("We run the algorithm on the network {int} times")
	public void weRunTheAlgorithmOnTheNetworkTimes(Integer nreps) {
		try {
			results = logicService.computeNetworkWithAlgorithms(network, names, nreps);
			if (results == null) {
				fail("No results given");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}

