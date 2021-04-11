#Author: Maximo Perez
Feature: Results table
  Scenario: Show a table of results
    Given a selection of networks and algorithms
		And a value for the flow of each one
		Then a flow results window is opened showing the results