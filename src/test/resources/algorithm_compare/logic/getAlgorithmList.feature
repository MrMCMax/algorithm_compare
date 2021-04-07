#Author: Maximo Perez Lopez
@tag
Feature: Get Algorithm List
  Scenario: Get Algorithm list as strings
    Given A logic service
    When we ask for what algorithms are available
    Then we get a list of the algorithm names
