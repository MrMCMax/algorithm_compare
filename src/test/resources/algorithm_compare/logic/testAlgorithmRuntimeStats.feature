#Author: Maximo Perez Lopez

@tag
Feature: Runtime hard test
  @RuntimeTest
  Scenario: Run list of algorithms on network
    Given the test network "100size.max"
    And the list of algorithms "HighestVertex1"
    And a default logic service
    When We run the algorithm on the network 300 times
		Then we get the result 3649 on all of them
