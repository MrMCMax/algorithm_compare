#Author: your.email@your.domain.com
#Keywords Summary :
#Feature: List of scenarios.
#Scenario: Business rule through list of steps with arguments.
#Given: Some precondition step
#When: Some key actions
#Then: To observe outcomes or validation
#And,But: To enumerate more Given,When,Then steps
#Scenario Outline: List of steps for data-driven as an Examples and <placeholder>
#Examples: Container for s table
#Background: List of steps run before each of the scenarios
#""" (Doc Strings)
#| (Data Tables)
#@ (Tags/Labels):To group Scenarios
#<> (placeholder)
#""
## (Comments)
#Sample Feature Definition Template
Feature: Test throrough correctness of HighestVertex
	@Correctness
	Scenario: Test HighestVertexStack
	  Given the test network "10000size.max"
    And the list of algorithms "HighestVertexGapRelabelling2"
    And a default logic service
    And the debug mode is turned on
    When We run the algorithms on the network
    Then we get the result 14520 on all of them
    And the debug mode is turned off
