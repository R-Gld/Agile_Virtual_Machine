# language: en
Feature: JajaCode debug
  As a developer using JajaCode
  I want to debug JajaCode programs
  So that I can identify and fix issues in the code (ouais)


  @debug
  Scenario: Step-by-step execution of JajaCode
    Given a JajaCode program:
      """
      1 init
      2 push(5)
      3 new(x@global, int, var, 0)
      4 jcstop
      """
    When I initialize the debugger
    And I execute one step
    Then the program counter should be 2
    When I execute one step
    Then the program counter should be 3
    When I execute one step
    Then the program counter should be 4
    When I execute one step
    Then the execution should be finished

  @debug
  Scenario: Capture memory snapshot during debug
    Given a JajaCode program:
      """
      1 init
      2 push(10)
      3 new(x@global, int, var, 0)
      4 push(20)
      5 store(x@global)
      6 jcstop
      """
    When I initialize the debugger
    And I execute steps until address 4
    And I capture the debug memory snapshot
    Then the snapshot should show variable "x@global" with value 10
    When I execute steps until address 6
    And I capture the debug memory snapshot
    Then the snapshot should show variable "x@global" with value 20

  @debug
  Scenario: Debug shows current instruction
    Given a JajaCode program:
      """
      1 init
      2 push(42)
      3 jcstop
      """
    When I initialize the debugger
    And I execute one step
    Then the current instruction should contain "push"

  @debug
  Scenario: Reset debugger and restart execution
    Given a JajaCode program:
      """
      1 init
      2 push(5)
      3 jcstop
      """
    When I initialize the debugger
    And I execute one step
    And I execute one step
    And I reset the debugger
    And I execute one step
    Then the program counter should be 2

  @debug @memory
  Scenario: Verify memory state in conditional branches (True case)
    Given a JajaCode program:
      """
      1 init
      2 push(true)
      3 if(6)
      4 push(20)
      5 goto(7)
      6 push(10)
      7 new(res@global, int, var, 0)
      8 jcstop
      """
    When I initialize the debugger
    And I execute steps until address 8
    And I capture the debug memory snapshot
    Then the snapshot should show variable "res@global" with value 10

  @debug @memory
  Scenario: Verify stack depth and content during method call
    Given a JajaCode program:
      """
      1 init
      2 push(5)
      3 new(f, int, meth, 0)
      4 goto(9)
      5 new(p@f, int, var, 1)
      6 push(99)
      7 swap
      8 return
      9 push(10)
      10 invoke(f)
      11 swap
      12 pop
      13 new(res@global, int, var, 0)
      14 jcstop
      """
    When I initialize the debugger
    And I execute steps until address 10
    And I capture the debug memory snapshot
    Then the stack should not contain variable "p@f"
    When I execute steps until address 6
    And I capture the debug memory snapshot
    Then the snapshot should show variable "p@f" with value 10
    When I execute steps until address 14
    And I capture the debug memory snapshot
    Then the stack should not contain variable "p@f"
    And the snapshot should show variable "res@global" with value 99
