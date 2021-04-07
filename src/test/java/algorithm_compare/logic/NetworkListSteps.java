package algorithm_compare.logic;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;

import algorithm_compare.persistence.PersistenceService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class NetworkListSteps {
	
	ILogicService logicService;
	
	List<String> networkNames;
	
	@Given("A logic service with path to the main resources")
	public void aLogicServiceWithPathToTheMainResources() {
		try {
	    String resourcePath = PersistenceService.PATH_TO_RESOURCES;
	    logicService = new LogicService(new PersistenceService());
		} catch (IOException e) {
			System.err.println(e);
			fail(e);
		}
	}
	
	@When("we ask for the network names")
	public void weAskForTheNetworkNames() {
		try {
		networkNames = logicService.getListOfNetworks();
		} catch (Exception e) {
			System.err.println(e);
			fail(e);
		}
	}
	
	@Then("a list with their names is provided")
	public void aListWithTheirNamesIsProvided() {
		System.out.println(networkNames);
	}
}

