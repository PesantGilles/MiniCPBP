*****************************************************************
Part 11: Modeling
*****************************************************************

*We ask you not to publish your solutions on a public repository.
The instructors interested to get the source code of
our solutions can contact us.*

Slides
======

`Lectures on Youtube <https://youtube.com/playlist?list=PLq6RpCDkJMyqp1npAakjuvqWjU7bz6Rfu>`_

* `Modeling <https://www.icloud.com/keynote/0bduxg7nHWOfdqcedJH7dNTdA#11-modeling-bin-packing>`_

Theoretical questions
=====================

* `Modeling <https://inginious.org/course/minicp/modeling>`_



The Logical Clause Constraint and Watched Literals
=======================================================


* Implement the constraint `Or.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/Or.java?at=master>`_
  for modeling the logical clause constraint: `(x[0] or x[1] or x[2] or ... or x[n-1])`.
* Test your implementation in `OrTest.java. <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/OrTest.java?at=master>`_.
* The implementation should use the watched-literals technique.


A reminder about the watched-literals technique:

*  The constraint should only listen to the changes of two unbound variables with `propagateOnBind(this)` and dynamically listen to other ones whenever one of these two become bound. Keep in mind that
  any call to `x[i].propagateOnBind(this)` has a stateful effect on backtrack.
* Why two ? Because as long as there is one unbound one, the constraint is still satisfiable and nothing needs to be propagated,
  and whenever it is detected that only one is unbound and all the other ones are set to false,
  the last one must be set to true (this is called unit propagation in SAT solvers).
* The two unbound variables
  should be at indices `wL` (watched left) and `wR` (watched right).
  As depicted below, `wL` (`wR`) is the leftmost (rightmost) unbound variable.
* Those indices are stored in `StateInt` such that they can only increase during search (incrementality).
* When `propagate` is called, it means that one of the two watched variables is bound (`x[wL] or x[wR]`) and
  consequently the two pointers must be updated.
* If during the update a variable bound to `true` is detected, then the constraint can be deactivated since it will always be satisfied.


.. image:: ../_static/watched-literals.svg
    :scale: 50
    :width: 600
    :alt: watched literals
    :align: center


The Reified Logical Clause Constraint
=======================================================


* Implement the constraint `IsOr.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/IsOr.java?at=master>`_
  for modeling the reified logical clause constraint: `b iff (x[0] or x[1] or x[2] or ... or x[n-1])`.
* Test your implementation in `IsOrTest.java. <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/IsOrTest.java?at=master>`_.
* In case `b` is true, you can post your previous `Or` constraint
(create it once and forall and post it when needed to avoid creating objects during search that would trigger garbage collection).


Steel Mill Slab Problem: Modeling, redundant constraints and symmetry breaking
======================================================================================

A number of TODO tasks must be completed in `Steel.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/examples/Steel.java?at=master>`_
that will gradually improve the performance for solving this problem optimally:

1. Model the objective function denoting the total loss to be minimized. You should use element constraints to denote the loss
   in each slab. The precomputed array `loss` gives for each load (index) the loss
   that would be induced. It is precomputed as the difference between the smallest capacity that can accommodate
   the load and the load value. A sum constraint constraint can then be used to denote the total loss.

2. Model a boolean variable reflecting the presence or not of each color in each slab.
   The color is present if at least one order with this color is present.
   The `IsOr` constraint previously implemented can be used for that.
3. Restrict the number of colors present in slab `j` to be at most 2.
   Your model can now be run, although it will not be able to solve optimally yet the easiest instance `data/steel/bench_20_0` in reasonable time.
4. Add a redundant constraint for the bin packing, stating that the sum of the loads is equal to the sum of the elements.
   Do you observe an improvement in the solving complexity?
5. Add static symmetry-breaking constraints. Two possibilities: the load of slabs must be decreasing or the losses must be decreasing.
   Do you observe an improvement in the solving complexity?
6. Implement a dynamic symmetry-breaking during search. Select an order `x` representing the slab where this order is placed.
   Assume that the maximum index of a slab containing an order is `m`.
   Then create `m+1` branches with `x=0 ,x=1, ..., x=m, x=m+1` since all the decisions `x=m+2, x=m+3, ...` would be subproblems symmetrical to `x=m+1`.
   You should now be able to quickly solve optimally the instance 'data/steel/bench_20_0', by reaching a zero-loss solution.


