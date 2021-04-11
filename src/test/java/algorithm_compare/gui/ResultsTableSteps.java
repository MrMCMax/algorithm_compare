package algorithm_compare.gui;

import java.util.ArrayList;
import java.util.List;

import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class ResultsTableSteps {
	
	List<String> networks;
	List<String> algorithms;
	Long[][] results;
	
	@Given("a selection of networks and algorithms")
	public void aSelectionOfNetworksAndAlgorithms() {
		networks = new ArrayList<>();
		networks.add("testgraph.txt");
		algorithms = new ArrayList<>();
		algorithms.add("EdmondsKarp");
	}
	
	@Given("a value for the flow of each one")
	public void aValueForTheFlowOfEachOne() {
		results = new Long[1][1];
		results[0][0] = 12L;
	}
	
	@Then("a flow results window is opened showing the results")
	public void aFlowResultsWindowIsOpenedShowingTheResults() {
		throw new PendingException();
	}
}

