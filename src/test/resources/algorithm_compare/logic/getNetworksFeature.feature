#Author: Maximo Perez Lopez
@tag
Feature: Retrieve the name of the networks available
  Scenario: Retrieve network names
    Given A logic service with path to the main resources
    When we ask for the network names
    Then a list with their names is provided
