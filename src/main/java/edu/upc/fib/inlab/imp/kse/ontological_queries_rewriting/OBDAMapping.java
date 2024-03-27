package edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting;

import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.*;

import java.util.*;

/**
 * Class that represents an OBDA mapping from ontological predicates to database queries
 */
public class OBDAMapping {
    private final Map<Predicate, List<Query>> mappings;


    /**
     * Creates an immutable mapping. It is immutable in the sense
     * that we cannot add any new mapping to any of the predicates.
     *
     * @param mappings not null, might be empty
     */
    private OBDAMapping(Map<Predicate, List<Query>> mappings) {
        Map<Predicate, List<Query>> mapToUnmodifiableList = new HashMap<>();
        for (Map.Entry<Predicate, List<Query>> entry : mappings.entrySet()) {
            mapToUnmodifiableList.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        this.mappings = Collections.unmodifiableMap(mapToUnmodifiableList);
    }


    public Set<Predicate> getOntologicalPredicates() {
        return mappings.keySet();
    }

    /**
     *
     * @param ontologicalQuery not null
     * @return the given ontological query translated as a databaseQuery
     */
    public List<Query> translateToDBQueries(ConjunctiveQuery ontologicalQuery) {
        Set<Predicate> mappingsAsDerivedPredicates = createDerivedPredicates(mappings);
        Optional<ConjunctiveQuery> queryInDerivedPredicatesOpt = rewriteQuery(ontologicalQuery, mappingsAsDerivedPredicates);
        if (queryInDerivedPredicatesOpt.isEmpty()) return Collections.emptyList();
        return queryInDerivedPredicatesOpt.get().unfold();
    }

    private Set<Predicate> createDerivedPredicates(Map<Predicate, List<Query>> mappings) {
        Set<Predicate> newDerivedPredicates = new HashSet<>();
        for (Map.Entry<Predicate, List<Query>> entry : mappings.entrySet()) {
            Predicate ontologicalPredicate = entry.getKey();
            MutablePredicate newDerivedPredicate = new MutablePredicate(ontologicalPredicate.getName(), ontologicalPredicate.getArity());
            for (Query query : entry.getValue()) {
                newDerivedPredicate.addDerivationRule(query);
            }
            newDerivedPredicates.add(newDerivedPredicate);
        }
        return newDerivedPredicates;
    }

    private Optional<ConjunctiveQuery> rewriteQuery(ConjunctiveQuery queryToRewrite, Set<Predicate> predicatesToUse) {
        List<Literal> newBody = new LinkedList<>();
        for (Atom atom : queryToRewrite.getBodyAtoms()) {
            String predicateName = atom.getPredicateName();
            Predicate predicateToUse = predicatesToUse.stream()
                    .filter(p -> p.getName().equals(predicateName))
                    .findFirst()
                    .orElse(null);
            if (predicateToUse == null) return Optional.empty();
            newBody.add(new OrdinaryLiteral(new Atom(predicateToUse, atom.getTerms())));
        }
        return Optional.of(QueryFactory.createConjunctiveQuery(queryToRewrite.getHeadTerms(), newBody));
    }

    public static class OBDAMappingBuilder {
        private final Map<Predicate, List<Query>> mutableMapping = new HashMap<>();


        /**
         * Stores the mapping between the given ontologicalPredicate and dbQuery
         *
         * @param ontologicalPredicate not null
         * @param dbQuery not null
         */
        public OBDAMappingBuilder addMapping(Predicate ontologicalPredicate, Query dbQuery) {
            List<Query> queriesForPredicate = mutableMapping.getOrDefault(ontologicalPredicate, new LinkedList<>());
            queriesForPredicate.add(dbQuery);
            mutableMapping.putIfAbsent(ontologicalPredicate, queriesForPredicate);
            return this;
        }

        public OBDAMapping build() {
            return new OBDAMapping(mutableMapping);
        }
    }
}
