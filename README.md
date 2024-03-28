# IMP-Logics-demo

This project holds a simple demo program to show the main capabilities of
the [IMP-Logics](https://github.com/inLabFIB/imp-logics) tool, a java metamodel of Datalog and its extension to
Datalog+/-.

This demo is given in two possible formats:

- As a jupyter notebook in the `/demo` directory. More information on how to run it
  in [demo/DEMO_GUIDE.md](demo/DEMO_GUIDE.md).
- Secondarily, it can also be run as the main Java
  class [MainDemo.java](src/main/java/edu/upc/fib/inlab/imp/kse/imp_logic_demo/MainDemo.java)

In particular, the demo shows:
1. The capabilities of IMP-Logics for manipulating a Datalog schema. That is, parsing, printing, and basic operations over the Datalog metamodel.
2. The capabilities of IMP-Logics for manipulating a Dependency schema (Datalog+/-). That is, parsing, printing, and basic operations over the Dependency metamodel.
3. How to use the previous to implement, for instance, an OBDA query-rewriting algorithm.
   Our implementation (which can be found in `/src/main/java/edu/upc/fib/inlab/imp/kse/ontological_queries_rewriting` is
   based on the rewriting strategy of the following
   paper: [Gottlob, G., Orsi, G., & Pieris, A. (2014). Query rewriting and optimization for ontological databases. ACM Transactions on Database Systems (TODS), 39(3), 1-46](https://dl.acm.org/doi/abs/10.1145/2638546).

