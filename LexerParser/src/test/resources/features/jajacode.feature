# language: en
Feature: JajaCode Compilation and Interpretation
  As a developer using MiniJaja
  I want to compile MiniJaja code to JajaCode and interpret it
  So that I can execute MiniJaja programs

  # ============================================================================
  # User Story: Compilation MiniJaja
  # ============================================================================

  @compilation
  Scenario: Compile a simple MiniJaja variable declaration
    Given a MiniJaja program:
      """
      class C {
        int x = 5;
        main {
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode should contain the following sequence:
        """
        1 init
2 push(5)
3 new(x@global, int, var, 0)
4 push(0)
5 swap
6 pop
7 pop
8 jcstop
        """

  @compilation
  Scenario: Compile a MiniJaja program with arithmetic operations
    Given a MiniJaja program:
      """
      class C {
        int x = 0;
        main {
          x = 2 + 3;
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode should contain the following sequence:
        """
        1 init
2 push(0)
3 new(x@global, int, var, 0)
4 push(2)
5 push(3)
6 add
7 store(x@global)
8 push(0)
9 swap
10 pop
11 pop
12 jcstop
        """

  @compilation
  Scenario: Compile a MiniJaja program with a while loop
    Given a MiniJaja program:
      """
      class C {
        int x = 0;
        main {
          while (x > 5) {
            x = x + 1;
          };
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode should contain the following sequence:
      """
      1 init
2 push(0)
3 new(x@global, int, var, 0)
4 load(x@global)
5 push(5)
6 sup
7 not
8 if(14)
9 load(x@global)
10 push(1)
11 add
12 store(x@global)
13 goto(4)
14 push(0)
15 swap
16 pop
17 pop
18 jcstop
        """


  @compilation
  Scenario: Compile a MiniJaja program with a method
    Given a MiniJaja program:
      """
      class C {
        int f(int p) {
          return p;
        };
        main {
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode should contain the following sequence:
      """
      1 init
2 push(5)
3 new(f, int, meth, 0)
4 goto(9)
5 new(p@f@int, int, var, 1)
6 load(p@f@int)
7 swap
8 return
9 push(0)
10 swap
11 pop
12 pop
13 jcstop
        """

  @compilation
  Scenario: Compile a MiniJaja program with array declaration and assignment
    Given a MiniJaja program:
      """
      class C {
        int x[3];
        main {
          x[0] = 12;
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode should contain the following sequence:
      """
      1 init
2 push(3)
3 newarray(x@global, int)
4 push(0)
5 push(12)
6 astore(x@global)
7 push(0)
8 swap
9 pop
10 pop
11 jcstop
      """

  @compilation
  Scenario: Compile a MiniJaja program with array access
    Given a MiniJaja program:
      """
      class C {
        int x[3];
        int y = 0;
        main {
          x[0] = 12;
          y = x[0];
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode should contain the following sequence:
      """
      1 init
2 push(3)
3 newarray(x@global, int)
4 push(0)
5 new(y@global, int, var, 0)
6 push(0)
7 push(12)
8 astore(x@global)
9 push(0)
10 aload(x@global)
11 store(y@global)
12 push(0)
13 swap
14 pop
15 swap
16 pop
17 pop
18 jcstop
      """

  @compilation
  Scenario: Compile a MiniJaja program with if-else structure
    Given a MiniJaja program:
      """
      class C {
        int x = 10;
        main {
          if (x > 5) {
            x = 1;
          } else {
            x = 0;
          };
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode should contain the following sequence:
      """
1 init
2 push(10)
3 new(x@global, int, var, 0)
4 load(x@global)
5 push(5)
6 sup
7 if(11)
8 push(0)
9 store(x@global)
10 goto(13)
11 push(1)
12 store(x@global)
13 push(0)
14 swap
15 pop
16 pop
17 jcstop
      """

  @compilation
  Scenario: Compile a MiniJaja program with boolean operations
    Given a MiniJaja program:
      """
      class C {
        boolean a = true;
        boolean b = false;
        boolean c;
        main {
          c = a && b;
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode should contain the following sequence:
        """
        1 init
2 push(true)
3 new(a@global, boolean, var, 0)
4 push(false)
5 new(b@global, boolean, var, 0)
6 push(w)
7 new(c@global, boolean, var, 0)
8 load(b@global)
9 load(a@global)
10 and
11 store(c@global)
12 push(0)
13 swap
14 pop
15 swap
16 pop
17 swap
18 pop
19 pop
20 jcstop
        """

  @compilation
  Scenario: Compile a MiniJaja program with method call
    Given a MiniJaja program:
      """
      class C {
        void f() {};
        main {
          f();
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode should contain the following sequence:
      """
      1 init
2 push(5)
3 new(f, void, meth, 0)
4 goto(8)
5 push(0)
6 swap
7 return
8 invoke(f)
9 pop
10 push(0)
11 swap
12 pop
13 pop
14 jcstop
"""

  @compilation
  Scenario: Compile a MiniJaja program with constants
    Given a MiniJaja program:
      """
      class C {
        final int CST = 100;
        main {
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode should contain the following sequence:
      """
      1 init
2 push(100)
3 new(CST@global, int, cst, 0)
4 push(0)
5 swap
6 pop
7 pop
8 jcstop
        """

  @compilation
  Scenario: Compile a MiniJaja program with increment
    Given a MiniJaja program:
      """
      class C {
        int x = 0;
        main {
          x++;
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode should contain the following sequence:
      """
      1 init
2 push(0)
3 new(x@global, int, var, 0)
4 push(1)
5 inc(x@global)
6 push(0)
7 swap
8 pop
9 pop
10 jcstop
      """

  @compilation
  Scenario: Compile a MiniJaja program with sum assignment
    Given a MiniJaja program:
      """
      class C {
        int x = 10;
        main {
          x += 5;
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode should contain the following sequence:
      """
      1 init
2 push(10)
3 new(x@global, int, var, 0)
4 push(5)
5 inc(x@global)
6 push(0)
7 swap
8 pop
9 pop
10 jcstop
      """

  @compilation
  Scenario: Compile a MiniJaja program with write and writeln
    Given a MiniJaja program:
      """
      class C {
        main {
          write("Hello");
          writeln("42");
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode should contain the following sequence:
      """
      1 init
2 push("Hello")
3 write
4 push("42")
5 writeln
6 push(0)
7 pop
8 jcstop
      """

  @compilation
  Scenario: Compile a MiniJaja program with array length
    Given a MiniJaja program:
      """
      class C {
        int t[5];
        int l;
        main {
          l = length(t);
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode should contain the following sequence:
      """
      1 init
2 push(5)
3 newarray(t@global, int)
4 push(w)
5 new(l@global, int, var, 0)
6 length(t@global)
7 store(l@global)
8 push(0)
9 swap
10 pop
11 swap
12 pop
13 pop
14 jcstop
      """

  @compilation
  Scenario: Compile a MiniJaja program with method call with parameters
    Given a MiniJaja program:
      """
      class C {
        int add(int a, int b) {
          return a + b;
        };
        main {
          add(1, 2);
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode should contain the following sequence:
      """
1 init
2 push(5)
3 new(add, int, meth, 0)
4 goto(12)
5 new(b@add@int, int, var, 1)
6 new(a@add@int, int, var, 2)
7 load(a@add@int)
8 load(b@add@int)
9 add
10 swap
11 return
12 push(2)
13 push(1)
14 invoke(add)
15 swap
16 pop
17 swap
18 pop
19 pop
20 push(0)
21 swap
22 pop
23 pop
24 jcstop
      """

  @compilation
  Scenario: Compile a MiniJaja program with unary negative
    Given a MiniJaja program:
      """
      class C {
        int x;
        main {
          x = -5;
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode should contain the following sequence:
      """
        1 init
2 push(w)
3 new(x@global, int, var, 0)
4 push(5)
5 neg
6 store(x@global)
7 push(0)
8 swap
9 pop
10 pop
11 jcstop
      """

  @compilation
  Scenario: Compile a MiniJaja program with variable shadowing
    Given a MiniJaja program:
      """
      class C {
        int x = 10;
        main {
          int x = 20;
          x = x + 1;
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode should contain the following sequence:
      """
        1 init
2 push(10)
3 new(x@global, int, var, 0)
4 push(20)
5 new(x@main, int, var, 0)
6 load(x@main)
7 push(1)
8 add
9 store(x@main)
10 push(0)
11 swap
12 pop
13 swap
14 pop
15 pop
16 jcstop
        """


  @compilation
  Scenario: Compile a MiniJaja program with array increment
    Given a MiniJaja program:
      """
      class C {
        int t[2];
        main {
          t[0]++;
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode should contain the following sequence:
      """
      1 init
2 push(2)
3 newarray(t@global, int)
4 push(0)
5 push(1)
6 ainc(t@global)
7 push(0)
8 swap
9 pop
10 pop
11 jcstop
      """

  @compilation
  Scenario: Compile a MiniJaja program with array sum assignment
    Given a MiniJaja program:
      """
      class C {
        int t[2];
        main {
          t[1] += 5;
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode should contain the following sequence:
      """
      1 init
2 push(2)
3 newarray(t@global, int)
4 push(1)
5 push(5)
6 ainc(t@global)
7 push(0)
8 swap
9 pop
10 pop
11 jcstop
      """

  @compilation
  Scenario: Compile a MiniJaja program with nested if-else
    Given a MiniJaja program:
      """
      class C {
        int x = 10;
        main {
          if (x > 5) {
            if (x > 8) {
              x = 1;
            } else {
              x = 2;
            };
          } else {
            x = 0;
          };
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode should contain the following sequence:
      """
      1 init
2 push(10)
3 new(x@global, int, var, 0)
4 load(x@global)
5 push(5)
6 sup
7 if(11)
8 push(0)
9 store(x@global)
10 goto(20)
11 load(x@global)
12 push(8)
13 sup
14 if(18)
15 push(2)
16 store(x@global)
17 goto(20)
18 push(1)
19 store(x@global)
20 push(0)
21 swap
22 pop
23 pop
24 jcstop
      """

  @compilation
  Scenario: Compile a MiniJaja program with complex boolean expression
    Given a MiniJaja program:
      """
      class C {
        boolean a = true;
        boolean b = false;
        boolean c = false;
        boolean res;
        main {
          res = (a && b) || c;
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode instructions should contain:
      """
      push(true)
new(a@global, boolean, var, 0)
push(false)
new(b@global, boolean, var, 0)
push(false)
new(c@global, boolean, var, 0)
push(w)
new(res@global, boolean, var, 0)
load(c@global)
load(b@global)
load(a@global)
and
or
store(res@global)
push(0)
      """

  @compilation
  Scenario: Compile a MiniJaja program with while loop and break logic (simulated)
    Given a MiniJaja program:
      """
      class C {
        int i = 0;
        main {
          while (10 > i) {
            i++;
            if (i == 5) {
               i = 10;
            };
          };
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode instructions should contain:
      """
      push(0)
new(i@global, int, var, 0)
push(10)
load(i@global)
sup
not
if(18)
push(1)
inc(i@global)
load(i@global)
push(5)
cmp
if(15)
push(10)
store(i@global)
goto(4)
push(0)
        """

  @compilation
  Scenario: Compile a MiniJaja program with recursive method call
    Given a MiniJaja program:
      """
      class C {
        int fact(int n) {
          if (n == 0) {
            return 1;
          } else {
            return n * fact(n - 1);
          };
          return 0;
        };
        main {
          fact(5);
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode instructions should contain:
      """
      push(5)
new(fact, int, meth, 0)
goto(23)
new(n@fact@int, int, var, 1)
load(n@fact@int)
push(0)
cmp
if(19)
load(n@fact@int)
load(n@fact@int)
push(1)
sub
invoke(fact)
swap
pop
mul
goto(20)
push(1)
push(0)
swap
return
push(5)
invoke(fact)
"""

  @compilation
  Scenario: Compile a MiniJaja program with multiple parameters and local variables
    Given a MiniJaja program:
      """
      class C {
        int compute(int a, int b, int c) {
          int sum = a + b;
          return sum * c;
        };
        main {
          compute(1, 2, 3);
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode instructions should contain:
      """
      push(5)
new(compute, int, meth, 0)
goto(19)
new(c@compute@int, int, var, 1)
new(b@compute@int, int, var, 2)
new(a@compute@int, int, var, 3)
load(a@compute@int)
load(b@compute@int)
add
new(sum@compute@int, int, var, 0)
load(sum@compute@int)
load(c@compute@int)
mul
swap
pop
swap
return
push(3)
push(2)
push(1)
invoke(compute)
"""

  @compilation
  Scenario: Compile a MiniJaja program with array access in expression
    Given a MiniJaja program:
      """
      class C {
        int t[3];
        int res;
        main {
          t[0] = 1;
          t[1] = 2;
          res = t[0] + t[1];
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode instructions should contain:
      """
      push(3)
newarray(t@global, int)
push(w)
new(res@global, int, var, 0)
push(0)
push(1)
astore(t@global)
push(1)
push(2)
astore(t@global)
push(0)
aload(t@global)
push(1)
aload(t@global)
add
store(res@global)
push(0)
      """

  @compilation
  Scenario: Compile a MiniJaja program with mutual recursion (even/odd)
    Given a MiniJaja program:
      """
      class C {
        boolean even(int n) {
          if (n == 0) {
            return true;
          } else {
            return odd(n - 1);
          };
          return false;
        };
        boolean odd(int n) {
          if (n == 0) {
            return false;
          } else {
            return even(n - 1);
          };
            return true;
        };
        main {
          even(4);
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode instructions should contain:
      """
      push(5)
new(even, boolean, meth, 0)
goto(21)
new(n@even@boolean, int, var, 1)
load(n@even@boolean)
push(0)
cmp
if(17)
load(n@even@boolean)
push(1)
sub
invoke(odd)
swap
pop
goto(18)
push(true)
push(false)
swap
return
push(24)
new(odd, boolean, meth, 0)
goto(40)
new(n@odd@boolean, int, var, 1)
load(n@odd@boolean)
push(0)
cmp
if(36)
load(n@odd@boolean)
push(1)
sub
invoke(even)
swap
pop
goto(37)
push(false)
push(true)
swap
return
push(4)
invoke(even)
        """

  @compilation
  Scenario: Compile a MiniJaja program with parameter shadowing by local variable
    Given a MiniJaja program:
      """
      class C {
        int f(int x) {
          int x = 10;
          return x;
        };
        main {
          f(5);
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should fail

  @compilation
  Scenario: Compile a MiniJaja program with complex arithmetic precedence
    Given a MiniJaja program:
      """
      class C {
        int res;
        main {
          res = 1 + 2 * 3 - 4 / 2;
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should succeed
    And the JajaCode instructions should contain:
      """
      push(1)
      push(2)
      push(3)
      mul
      add
      push(4)
      push(2)
      div
      sub
      store(res@global)
      """

  @compilation
  Scenario: Compilation should fail for invalid MiniJaja syntax
    Given a MiniJaja program:
      """
      class {
        main {
        }
      }
      """
    When I compile the MiniJaja program
    Then the compilation should fail