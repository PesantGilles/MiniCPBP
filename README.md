

# README #

* MiniCPBP: Replacing classic propagation by belief propagation in MiniCPv1.0
* A paper describing the CP-BP framework  <https://jair.org/index.php/jair/article/view/11487>.
* Infos on MiniCP can be found <http://minicp.org>.
* MiniCPBP is a Java project built with Maven (<https://maven.apache.org>).




System Requirements
-------------------

* JDK:
 1.8 or above (this is to execute Maven; it still allows you to build against 1.3
 and prior JDKs).
* Memory:
 No minimum requirement.
* Disk:
 Approximately 10MB is required for the Maven installation itself. Additional disk space will be used for your local Maven repository. The size
 of your local repository will vary depending on usage, but expect at least 500MB.
* Operating system: 
    * Windows: Windows 2000 or above.
    * Unix-based operating systems (Linux, Solaris, and macOS) and others: No minimum requirement.

Installing Maven
----------------

1. Unpack the archive where you would like to store the binaries, e.g.:

    - Unix-based operating systems (Linux, Solaris, and macOS):
      ```
      tar zxvf apache-maven-3.x.y.tar.gz 
      ```
    - Windows:
      ```
      unzip apache-maven-3.x.y.zip
      ```

    A directory called `apache-maven-3.x.y` will be created.

2. Add the bin directory to your PATH, e.g.:

    - Unix-based operating systems (Linux, Solaris, and macOS):
      ```
      export PATH=/usr/local/apache-maven-3.x.y/bin:$PATH
      ```
    - Windows:
      ```
      set PATH="v:\program files\apache-maven-3.x.y\bin";%PATH%
      ```

3. Make sure `JAVA_HOME` is set to the location of your JDK.

4. Run `mvn --version` to verify that it is correctly installed.


For the complete documentation, see
<https://maven.apache.org/download.html#Installation>.


Commands for executing a model and running the test suite
---------------------------------------------------------

```
 cd minicpbp/
 mvn compile                                                # compile the project
 mvn exec:java -Dexec.mainClass="minicpbp.examples.NQueens" # execute the n-queens model
 mvn test                                                   # run the test suite
```

Using the IntelliJ IDEA editor
--------------------------------------------------

We recommend IntelliJ IDEA (<https://www.jetbrains.com/idea/download>).

Do > `File | Open Project (Alt + F + O)` and specify the path to `pom.xml`
as explained at
<https://blog.jetbrains.com/idea/2008/03/opening-maven-projects-is-easy-as-pie>.

Content
-------------

```
./src/main/java/                     # the implementation of MiniCP
./src/main/java/minicpbp/examples/   # model examples
./src/test/java/                     # the test suite
./data/                              # input instances
```


# Solving XCSP or FZN instances using MiniCPBP

## Building MiniCPBP

We build *MiniCPBP* using Maven. simply go to the MiniCPBP directory, and run the build command:

```
$ cd MiniCPBP
$  mvn install -Dmaven.test.skip=true
```

The output of build process ends with lines like this, which indicates a successful build:

```
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 3.036 s
[INFO] Finished at: 2020-02-04T17:57:01-05:00
[INFO] Final Memory: 22M/84M
[INFO] ------------------------------------------------------------------------

```

## Running MiniCPBP

Let's firts test the built code:

```
$ java -jar target/minicpbp-1.0.jar
```

This should print the help message:

```
Missing required options: input, branching, search-type, timeout
usage: solve-XCSP
    --branching <STRATEGY>                      branching strategy.
                                                Valid branching strategies
                                                are:
                                                "first-fail-random-value",
                                                "max-marginal",
                                                "max-marginal-strength",
                                                "min-marginal",
                                                "min-marginal-strength"
    --damp-messages                             damp messages
    --damping-factor <LAMBDA>                   the damping factor used
                                                for damping the messages
    --input <FILE>                              input XCSP or FZN file
    --max-iter <ITERATIONS>                     maximum number of belief
                                                propagation iterations
    --search-type <SEARCH>                      search type.
                                                Valid search types are:
                                                "dfs",
                                                "lds"
    --solution <FILE>                           file for storing the
                                                solution
    --stats <FILE>                              file for storing the
                                                statistics
    --timeout <SECONDS>                         timeout in seconds
    --trace-bp                                  trace the belief
                                                propagation progress
    --trace-search                              trace the search progress
    --verify                                    check the correctness of
                                                obtained solution
```

As the first line of the help message indicates, we should have at least four arguments:

- `input`: the path to the XCSP instance 
- `branching`: the branching strategy
- `search-type`: the type of search 
- `timeout`: the maximum time budget (in seconds)

Run this command:

```
$ java -jar target/minicpbp-0.5.jar --input ../instances/Basic/Basic-m1-s1/LabeledDice.xml --branching max-marginal --search-type dfs --timeout 100
```

This should output an output similar to the following:

```
Warning: method updateBelief not implemented yet for Cardinality constraint. Using uniform belief instead.
solution found
status: SAT
failures: 3
nodes: 13
runtime (ms): 82
```

To understand how to interpret the output, go to the later sections in this document. 

## Optional arguments

You can ask the runner to verify the solutions, or print the solution or search statistics to some file instead of standard output. 

### Damping

In order to add message damping, you should add the argument `damp-messages`. The default damping factor is 0.5 and can be changed using the argument `damping-factor`. 

### Belief propagation iterations

You can control the number of iterations using the argument `max-iter`. The default value for this parameter is 5. 

### Verifying the solution

The flag `verify` asks the solver to check the solution if it finds one. Run the following command:

```
$ java -jar target/minicpbp-0.5.jar --input ../instances/Basic/Basic-m1-s1/LabeledDice.xml --branching max-marginal --timeout 100 --verify
```

In the output you see these added lines:

```
verifying the solution (begin)
LOG: Check variables
LOG: Check constraints
OK	
VALID SOLUTION
verifying the solution (end)
```

These lines indicate that the solution has been successfully verified by the XCSP checker. 

### Printing the traces
The flags `trace-bp` and `trace-search` instruct the solver to print the traces of the belief propagation and the search procedure, respectively. 

### Printing to file

The arguments `solution` and `stats` can be used to give paths for storing the solution or search statistics. Running the following command will create two files where the solution and search statistics are stored.  

```
java -jar target/minicpbp-0.5.jar --input ../instances/Basic/Basic-m1-s1/LabeledDice.xml --branching max-marginal --timeout 100 --solution LabeledDice.sol --stats LabeledDice.stats
```

## Interpreting the search statistics

The search statistics consist of these entries:

- `status`: has one of these three values:
  - `SAT`: a solution was found
  - `TIMEOUT`: search was terminated before any solution was found by hitting the timeout
  - `UNSAT`: the problem does not have a solution (it is *unsatisfiable*)
- `failures`: the number of failures during the search
- `nodes`: the number of search tree nodes explored during the search
- `runtime (ms)`: time spent during the search (in milliseconds)




