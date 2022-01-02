*****************************************************************
Part 10: Disjunctive Scheduling
*****************************************************************

*We ask you not to publish your solutions on a public repository.
The instructors interested to get the source code of
our solutions can contact us.*

Slides
======
`Lectures on Youtube <https://youtube.com/playlist?list=PLq6RpCDkJMyrAHSnNczQgftZO83TNJG_k>`_


* `Disjunctive Scheduling <https://www.icloud.com/keynote/0jR5krj0fNao6euSqBNODWPmQ#10-disjunctive-scheduling>`_


Theoretical questions
=====================

* `Disjunctive Scheduling <https://inginious.org/course/minicp/disjunctive>`_

Decomposing the Disjunctive Constraint
=======================================================

Your task is to make the disjunctive constraint more efficient than by using the cumulative constraint with unary capacity:

* Implement the constraint `IsLessOrEqualVar.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/IsLessOrEqualVar.java?at=master>`_
  for the reification `b iff x <= y`.
  This will be useful implementing the decomposition for the disjunctive constraint.
* Test your implementation in `IsLessOrEqualVarTest.java. <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/IsLessOrEqualVarTest.java?at=master>`_
* Implement the decomposition with reified constraints for `Disjunctive.java. <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Disjunctive.java?at=master>`_.
* Test if (as expected) this decomposition prunes more than the formulation with TimeTable filtering for the cumulative constraint.
  Observe on the `JobShop.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/examples/JobShop.java?at=master>`_ problem if the number of backtracks is reduced with the decomposition instead of the formulation with the cumulative.
  Test for instance on the small instance `data/jobshop/sascha/jobshop-4-4-2` with 4 jobs, 4 machines, and 16 activities.


The Global Disjunctive Constraint (Overload Checker, Detectable Precedence and Not-First-Not-Last
=========================================================================================================================

* Read and make sure you understand the implementation  `ThetaTree.java. <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/ThetaTree.java?at=master>`_.
  Some unit tests are implemented in `ThetaTreeTest.java. <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/ThetaTreeTest.java?at=master>`_.
  To make sure you understand it, add a unit test with 4 activities and compare the results with a manual computation.
* Overlad-checker, detectable precedences, not-first, edge-finding only filter one side of the activities.
  To get the symmetrical filtering implement the mirroring activities trick similarly to `Cumulative.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Cumulative.java?at=master>`_.
* Implement the overload checker in `Disjunctive.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Disjunctive.java?at=master>`_.
* The overload checker should already make a big difference to prune the search tree. Make sure that larger job-shop instances are now accessible; for instance, `data/jobshop/sascha/jobshop-6-6-0` should now become easy to solve.
* Implement the detectable-precedence in `Disjunctive.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Disjunctive.java?at=master>`_.
* Implement the not-first-not last in `Disjunctive.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Disjunctive.java?at=master>`_.
* Make sure your implementation passes the tests `DisjunctiveTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/DisjunctiveTest.java?at=master>`_.
* (optional for a bonus) Implement the edge-finding in `Disjunctive.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Disjunctive.java?at=master>`_ (you will also need to implement the ThetaLambdaTree data-structure).
