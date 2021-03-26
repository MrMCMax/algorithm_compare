#Just to make sure that Cucumber runs
@tag
Feature: Hello world
  @tag1
  Scenario: Hello World
    Given the string of Hello World
    Then the string says "Hello World!"