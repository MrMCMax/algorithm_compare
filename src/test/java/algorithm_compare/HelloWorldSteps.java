package algorithm_compare;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class HelloWorldSteps {
	
	String helloMessage;
	
	@Given("the string of Hello World")
	public void theStringOfHelloWorld() {
		helloMessage = HelloWorldMain.sayHello();
	}
	
	@Then("the string says {string}")
	public void theStringSays(String string) {
		assertEquals(helloMessage, string);
	}
}

