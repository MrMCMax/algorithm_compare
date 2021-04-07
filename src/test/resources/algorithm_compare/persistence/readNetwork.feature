#Author: Maximo Perez Lopez
Feature: Read Network
  Scenario: Reading test graph
    Given The path to testgraph
		When testgraph is read
		Then no exceptions are thrown
		And the source is 1
		And the sink is 5
		And there exists an edge from 5 to 3 with cap 30
		And there exists an edge from 4 to 2 with cap 23
		And there exists an edge from 3 to 5 with cap 16
		And there exists an edge from 1 to 3 with cap 7
		And there exists an edge from 2 to 4 with cap 6
		And there exists an edge from 4 to 3 with cap 6
		And there exists an edge from 2 to 1 with cap 25
		And there exists an edge from 1 to 4 with cap 30
		And there exists an edge from 4 to 1 with cap 23
		And there exists an edge from 2 to 5 with cap 18