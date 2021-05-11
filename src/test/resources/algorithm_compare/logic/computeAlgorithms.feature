#Author: Maximo Perez Lopez
@tag
Feature: Compute algorithms

  Scenario: Run list of algorithms on network
    Given the test network "testgraph.txt"
    And the list of algorithms "EdmondsKarp, ScalingEdmondsKarp, DinicsAlgorithm"
    And a default logic service
    When We run the algorithms on the network
    Then we get the result 31 on all of them
  
#  @big  
#  Scenario: Run gigantic network
#  	Given the test network "BL06-camel-sml.max"
#  	And the list of algorithms "DinicsAlgorithm"
#  	And a default logic service
#  	When We run the algorithms on the network
#  	Then we get the result 76324399 on all of them
#  
#  @big
#  Scenario: Store and retrieve the timing of a gigantic network
#  	Given the test network "BL06-camel-sml.max"
#  	And the list of algorithms "DinicsAlgorithm"
#  	And a default logic service
#  	When We run the algorithms on the network
#  	Then we get the result 76324399 on all of them
#  	And we can retrieve the times for the algorithms
    
	@big
	Scenario: Test Vertex data structures
		Given the test network "100size.max"
		And the list of algorithms "HighestVertex1"
		And a default logic service
		When We run the algorithms on the network
		Then we get the result 3649 on all of them
		And we can retrieve the times for the algorithms