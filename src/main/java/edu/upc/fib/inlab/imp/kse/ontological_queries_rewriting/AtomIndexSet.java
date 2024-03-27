package edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public record AtomIndexSet(Set<Integer> indexes) {
    public AtomIndexSet(Integer... index) {
        this(new HashSet<>(Arrays.asList(index)));
    }

    public boolean contains(int index) {
        return indexes.contains(index);
    }
}
