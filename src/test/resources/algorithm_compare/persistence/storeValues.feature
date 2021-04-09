#Author: Maximo Perez Lopez
Feature: Store and delete a value for a test
	Scenario: Store, retrieve and delete dummy values for testgraph
	Given The path to testgraph
	When we store the long value 116 for algorithm "EdmondsKarp"
	Then we can retrieve that value afterwards
	When we delete the value for algorithm "EdmondsKarp"
	Then we do not have any value for the algorithm "EdmondsKarp"