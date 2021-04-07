package algorithm_compare.logic;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class AlgorithmListSteps {
	
	ILogicService service;
	List<String> listOfAlgorithms;
	
	@Given("A logic service")
	public void aLogicService() {
		try {
		service = new LogicService();
		} catch (IOException e) {
			System.err.println(e);
			fail(e);
		}
	}
	
	@When("we ask for what algorithms are available")
	public void weAskForWhatAlgorithmsAreAvailable() {
		listOfAlgorithms = service.getListOfAlgorithms();
	}
	
	@Then("we get a list of the algorithm names")
	public void weGetAListOfTheAlgorithmNames() {
		String edmondsKarp = "EdmondsKarp";
		assertTrue(listOfAlgorithms.contains(edmondsKarp));
	}
}

