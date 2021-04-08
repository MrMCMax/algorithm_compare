#Author: Maximo Perez Lopez
@tag
Feature: Compute algorithms

  @tag1
  Scenario: Run list of algorithms on network
    Given the test network "testgraph.txt"
    And the list of algorithms "EdmondsKarp, ScalingEdmondsKarp, DinicsAlgorithm"
    And a default logic service
    When We run the algorithms on the network
    Then we get the result 31 on all of them
    
  Scenario: Run gigantic network
  	Given the test network "BL06-camel-sml.max"
  	And the list of algorithms "DinicsAlgorithm"
  	And a default logic service
  	When We run the algorithms on the network
  	Then we get the result 76324399 on all of them
    
