#Author: Maximo Perez Lopez

@tag
Feature: Runtime hard test
  @RuntimeTest
  Scenario: Run list of algorithms on network
    Given the test network "BL06-camel-sml.max"
    And the list of algorithms "FIFOPushRelabelVertexGlR, FIFOPR_Gl_Gap"
    And a default logic service
    When We run the algorithm on the network 20 times
		Then we get the result 76324399 on all of them
