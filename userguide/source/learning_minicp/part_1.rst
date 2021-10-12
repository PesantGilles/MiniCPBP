*****************************************************************
Part 1: Overview of CP, Filtering, Search, Consistency, Fix-point
*****************************************************************

We propose a set of exercises to extend MiniCP with useful features.
By doing these exercises you will gradually progress in your understanding of CP.
For each exercise, we ask you to implement JUnit tests to make sure that
your implementation works as expected.
If you don't test each feature independently you take the risk to
lose a lot of time finding very difficult bugs.


*We ask you not to publish your solutions on a public repository.
The instructors interested to get the source code of
our solutions can contact us.*

Slides
======

`Lectures on Youtube <https://www.youtube.com/playlist?list=PLq6RpCDkJMyoH9ujmz6TBoAwT5Ax8RwqE>`_

`Overview of CP, Filtering, Search, Consistency, Fix-point <https://www.icloud.com/keynote/0oS2SsSrew0r4aiPaPV470n4g#01-intro>`_

Theoretical questions
=====================

* `Fix-point <https://inginious.org/course/minicp/fix-point>`_
* `Consistency <https://inginious.org/course/minicp/consistencies>`_

Forking MiniCP to do the programming exercices
===============================================

`Follow this tutorial <https://inginious.org/course/minicp/minicp-install-1>`_ then clone your repository.

:ref:`Then, follow this tutorial to import it in your IDE <install>`.

Less or equal reified constraint
================================

Implement `IsLessOrEqual.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/main/java/minicp/engine/constraints/IsLessOrEqual.java?at=master>`_

This is a reified constraint for `b iff x <= c`
that is boolean variable `b` is set true if and only if `x` variable is less than or equal to value `c`.

For example, the constraint holds for

.. code-block:: java

    b = true , x = 4, c = 5
    b = false, x = 4, c = 2


but is violated for

.. code-block:: java

    b = true , x = 5, c = 4
    b = false, x = 2, c = 4


Check that your implementation passes the tests `IsLessOrEqualTest.java <https://bitbucket.org/minicp/minicp/src/HEAD/src/test/java/minicp/engine/constraints/IsEqualTest.java?at=master>`_
