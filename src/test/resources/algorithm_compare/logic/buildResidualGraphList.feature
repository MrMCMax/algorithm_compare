#Author: Maximo Perez Lopez
Feature: Build Residual Graph with adjacency lists
  Scenario: Build Residual Graph for testgraph
    Given The GraphData from testgraph.txt
    When the graph is built
    Then the graph has 5 vertices
    And the graph has an edge from 5 to 3 with cap 30
		And the graph has an edge from 4 to 2 with cap 23
		And the graph has an edge from 3 to 5 with cap 16
		And the graph has an edge from 1 to 3 with cap 7
		And the graph has an edge from 2 to 4 with cap 6
		And the graph has an edge from 4 to 3 with cap 6
		And the graph has an edge from 2 to 1 with cap 25
		And the graph has an edge from 1 to 4 with cap 30
		And the graph has an edge from 4 to 1 with cap 23
		And the graph has an edge from 2 to 5 with cap 18
