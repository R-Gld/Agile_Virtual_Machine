# language: en
Feature: JajaCode Interpretation
  As a developer using JajaCode
  I want to interpret JajaCode in order to see it's memory
  So that I can verify the correctness of the interpretation

  @interpretation
  Scenario: Interpret a simple JajaCode program
    Given a JajaCode program:
      """
      1 init
      2 push(5)
      3 new(x@global, int, var, 0)
      4 jcstop
      """
    When I interpret the JajaCode program
    Then the interpretation should succeed
    And the variable "x@global" should have value 5

  @interpretation
  Scenario: Interpret JajaCode with arithmetic operations
    Given a JajaCode program:
      """
      1 init
      2 push(10)
      3 push(5)
      4 add
      5 new(result@global, int, var, 0)
      6 jcstop
      """
    When I interpret the JajaCode program
    Then the interpretation should succeed
    And the variable "result@global" should have value 15

  @interpretation
  Scenario: Interpret JajaCode with control flow (if/goto)
    Given a JajaCode program:
      """
      1 init
      2 push(0)
      3 new(x@global, int, var, 0)
      4 push(true)
      5 if(7)
      6 goto(9)
      7 push(42)
      8 store(x@global)
      9 jcstop
      """
    When I interpret the JajaCode program
    Then the interpretation should succeed
    And the variable "x@global" should have value 42

  @interpretation
  Scenario: Interpret JajaCode with store and load
    Given a JajaCode program:
      """
      1 init
      2 push(0)
      3 new(x@global, int, var, 0)
      4 push(99)
      5 store(x@global)
      6 jcstop
      """
    When I interpret the JajaCode program
    Then the interpretation should succeed
    And the variable "x@global" should have value 99

  @interpretation
  Scenario: Interpret JajaCode with array operations
    Given a JajaCode program:
      """
      1 init
      2 push(3)
      3 newarray(t@global, int)
      4 push(0)
      5 push(42)
      6 astore(t@global)
      7 push(0)
      8 aload(t@global)
      9 new(res@global, int, var, 0)
      10 jcstop
      """
    When I interpret the JajaCode program
    Then the interpretation should succeed
    And the variable "res@global" should have value 42

  @interpretation
  Scenario: Interpret JajaCode with boolean logic
    Given a JajaCode program:
      """
      1 init
      2 push(true)
      3 push(false)
      4 or
      5 new(res_or@global, boolean, var, 0)
      6 push(true)
      7 not
      8 new(res_not@global, boolean, var, 0)
      9 jcstop
      """
    When I interpret the JajaCode program
    Then the interpretation should succeed
    And the variable "res_or@global" should have value true
    And the variable "res_not@global" should have value false

  @interpretation
  Scenario: Interpret JajaCode with increment
    Given a JajaCode program:
      """
      1 init
      2 push(10)
      3 new(x@global, int, var, 0)
      4 push(5)
      5 inc(x@global)
      6 jcstop
      """
    When I interpret the JajaCode program
    Then the interpretation should succeed
    And the variable "x@global" should have value 15

  @interpretation
  Scenario: Interpret JajaCode with array length
    Given a JajaCode program:
      """
      1 init
      2 push(3)
      3 newarray(t@global, int)
      4 length(t@global)
      5 new(len@global, int, var, 0)
      6 jcstop
      """
    When I interpret the JajaCode program
    Then the interpretation should succeed
    And the variable "len@global" should have value 3


  @interpretation
  Scenario: Interpret JajaCode with division by zero
    Given a JajaCode program:
      """
      1 init
      2 push(10)
      3 push(0)
      4 div
      5 jcstop
      """
    When I interpret the JajaCode program
    Then the interpretation should fail

  @interpretation
  Scenario: Interpret JajaCode with array index out of bounds
    Given a JajaCode program:
      """
      1 init
      2 push(3)
      3 newarray(t@global, int)
      4 push(5)
      5 aload(t@global)
      6 jcstop
      """
    When I interpret the JajaCode program
    Then the interpretation should fail

  @interpretation
  Scenario: Interpret JajaCode with negative array size
    Given a JajaCode program:
      """
      1 init
      2 push(-5)
      3 newarray(t@global, int)
      4 jcstop
      """
    When I interpret the JajaCode program
    Then the interpretation should fail

  @interpretation
  Scenario: Interpretation should fail for invalid JajaCode
    Given a JajaCode program:
      """
      1 invalid_instruction
      2 jcstop
      """
    When I interpret the JajaCode program
    Then the interpretation should fail


  @interpretation @memory
  Scenario: Verify stack cleanup after local variable usage
    Given a JajaCode program:
      """
      1 init
      2 push(10)
      3 new(x@global, int, var, 0)
      4 push(20)
      5 new(y@local, int, var, 0)
      6 load(y@local)
      7 store(x@global)
      8 push(0)
      9 swap
      10 pop
      11 pop
      12 jcstop
      """
    When I interpret the JajaCode program
    And I capture the memory state
    Then the interpretation should succeed
    And the variable "x@global" should have value 20
    And the stack should contain variable "x@global"
    And the stack should not contain variable "y@local"

  @interpretation @memory
  Scenario: Verify parameter cleanup after method call
    Given a JajaCode program:
      """
      1 init
      2 push(5)
      3 new(f, int, meth, 0)
      4 goto(11)
      5 new(param@f, int, var, 1)
      6 load(param@f)
      7 push(1)
      8 add
      9 swap
      10 return
      11 push(10)
      12 invoke(f)
      13 swap
      14 pop
      15 new(res@global, int, var, 0)
      16 jcstop
      """
    When I interpret the JajaCode program
    And I capture the memory state
    Then the interpretation should succeed
    And the variable "res@global" should have value 11
    And the stack should not contain variable "param@f"

  @interpretation @memory
  Scenario: Verify swap and pop logic for scope exit
    Given a JajaCode program:
      """
      1 init
      2 push(10)
      3 new(x, int, var, 0)
      4 push(20)
      5 new(y, int, var, 0)
      6 push(0)
      7 swap
      8 pop
      9 store(x)
      10 jcstop
      """
    When I interpret the JajaCode program
    And I capture the memory state
    Then the interpretation should succeed
    And the variable "x" should have value 0
    And the stack should not contain variable "y"

  @debug @memory
  Scenario: Verify local variable lifecycle (creation and destruction)
    Given a JajaCode program:
      """
      1 init
      2 push(10)
      3 new(global, int, var, 0)
      4 push(20)
      5 new(local, int, var, 0)
      6 push(0)
      7 swap
      8 pop
      9 pop
      10 jcstop
      """
    When I initialize the debugger
    And I execute steps until address 6
    And I capture the debug memory snapshot
    Then the snapshot should show variable "local" with value 20
    When I execute steps until address 10
    And I capture the debug memory snapshot
    Then the snapshot should show variable "global" with value 10
    And the stack should not contain variable "local"


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
  Scenario: Verify complex logic and memory state at specific address
    Given a JajaCode program:
      """
      1 init
      2 push(w)
      3 new(x@global, int, var, 0)
      4 push(1)
      5 new(z@global, int, var, 0)
      6 push(10)
      7 new(y@global, int, var, 0)
      8 push(5)
      9 new(VAL@global, int, cst, 0)
      10 push(true)
      11 new(b1@global, boolean, var, 0)
      12 push(false)
      13 new(b2@global, boolean, var, 0)
      14 load(b2@global)
      15 not
      16 load(b1@global)
      17 or
      18 load(b1@global)
      19 and
      20 if(24)
      21 push(1)
      22 inc(x@global)
      23 goto(30)
      24 load(VAL@global)
      25 load(y@global)
      26 add
      27 push(0)
      28 sub
      29 store(x@global)
      30 load(y@global)
      31 load(z@global)
      32 mul
      33 store(x@global)
      34 load(x@global)
      35 load(z@global)
      36 div
      37 store(x@global)
      38 push(1)
      39 inc(x@global)
      40 push(0)
      41 swap
      42 pop
      43 swap
      44 pop
      45 swap
      46 pop
      47 swap
      48 pop
      49 swap
      50 pop
      51 swap
      52 pop
      53 pop
      54 jcstop
      """
    When I initialize the debugger
    And I execute steps until address 40
    And I capture the debug memory snapshot
    Then the snapshot should show variable "b2@global" with value false
    And the snapshot should show variable "b1@global" with value true
    And the snapshot should show variable "VAL@global" with value 5
    And the snapshot should show variable "y@global" with value 10
    And the snapshot should show variable "z@global" with value 1
    And the snapshot should show variable "x@global" with value 11


  @debug @memory
  Scenario: Verify factorial with pause method and memory state at address 50
    Given a JajaCode program:
      """
      1 init
      2 push(7)
      3 new(x@global, int, var, 0)
      4 push(7)
      5 new(pause, void, meth, 0)
      6 goto(14)
      7 push(w)
      8 new(i@pause@void, int, var, 0)
      9 push(0)
      10 swap
      11 pop
      12 swap
      13 return
      14 push(17)
      15 new(fact, int, meth, 0)
      16 goto(41)
      17 new(x@fact@int, int, var, 1)
      18 push(w)
      19 new(retour@fact@int, int, var, 0)
      20 load(x@fact@int)
      21 push(0)
      22 cmp
      23 if(34)
      24 load(x@fact@int)
      25 load(x@fact@int)
      26 push(1)
      27 sub
      28 invoke(fact)
      29 swap
      30 pop
      31 mul
      32 store(retour@fact@int)
      33 goto(36)
      34 push(1)
      35 store(retour@fact@int)
      36 load(retour@fact@int)
      37 swap
      38 pop
      39 swap
      40 return
      41 load(x@global)
      42 invoke(fact)
      43 swap
      44 pop
      45 new(res@main, int, var, 0)
      46 push("x(5040) = ")
      47 write
      48 load(res@main)
      49 writeln
      50 invoke(pause)
      51 pop
      52 push(0)
      53 swap
      54 pop
      55 swap
      56 pop
      57 swap
      58 pop
      59 swap
      60 pop
      61 pop
      62 jcstop
      """
    When I initialize the debugger
    And I execute steps until address 50
    And I capture the debug memory snapshot
    Then the snapshot should show variable "res@main" with value 5040
    And the snapshot should show variable "fact" with value 17
    And the snapshot should show variable "pause" with value 7
    And the snapshot should show variable "x@global" with value 7