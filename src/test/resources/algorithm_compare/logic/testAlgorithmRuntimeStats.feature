#Author: Maximo Perez Lopez

@tag
Feature: Runtime hard test
  @RuntimeTest
  Scenario: Run list of algorithms on network
    Given the test network "ac1000.txt.max"
    And the list of algorithms "DinicsAlgorithm, FIFOPushRelabelVertexGlR"
    And a default logic service
    When We run the algorithm on the network 300 times
		Then we get the result 48204 on all of them
