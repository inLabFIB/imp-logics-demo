package edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting;

import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.*;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.comparator.isomorphism.IsomorphismComparator;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.comparator.isomorphism.IsomorphismOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.QueryOrigin.FACTORIZATION;
import static edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.QueryOrigin.REWRITING;

/**
 * Mutable structure to keep result while
 */
public class QueryRewriting {

    public static final String BASE_STRING_FOR_NEW_PREDICATE_NAMES = "unused";
    private final IsomorphismComparator isomorphismComparator;

    private final List<GeneratedQuery> generatedQueries = new ArrayList<>();

    public QueryRewriting() {
        IsomorphismOptions isomorphismOptions = new IsomorphismOptions(true, true, false);
        isomorphismComparator = new IsomorphismComparator(isomorphismOptions);
    }

    public QueryRewriting(ConjunctiveQuery initialQuery) {
        this();
        generatedQueries.add(getInitialGeneratedQuery(initialQuery));
    }

    private static GeneratedQuery getInitialGeneratedQuery(ConjunctiveQuery initialQuery) {
        return new GeneratedQuery(initialQuery, REWRITING, false);
    }

    public int size() {
        return generatedQueries.size();
    }

    public List<ConjunctiveQuery> getRewritingQueries() {
        return generatedQueries.stream()
                .filter(gq -> gq.getOrigin() == REWRITING)
                .map(GeneratedQuery::getQuery)
                .toList();
    }

    public List<GeneratedQuery> getUnexploredGeneratedQueries() {
        return generatedQueries.stream()
                .filter(gq -> !gq.isExplored())
                .toList();
    }

    /**
     * @return if the query has been added
     */
    public boolean addRewrittenQuery(ConjunctiveQuery query) {
        if (notInRewritingYet(query, REWRITING)) {
            generatedQueries.add(new GeneratedQuery(query, REWRITING, false));
            return true;
        }
        return false;
    }

    /**
     * @return if the query has been added
     */
    public boolean addFactorizedQuery(ConjunctiveQuery query) {
        if (notInRewritingYet(query, FACTORIZATION)) {
            generatedQueries.add(new GeneratedQuery(query, FACTORIZATION, false));
            return true;
        }
        return false;
    }

    private boolean notInRewritingYet(ConjunctiveQuery query, QueryOrigin queryOrigin) {
        for (GeneratedQuery generatedQuery : this.generatedQueries) {
            if (queryOrigin == REWRITING && generatedQuery.getOrigin() == FACTORIZATION) continue;
            if (equalsModuloBijectiveVariableRenaming(query, generatedQuery.getQuery())) return false;
        }
        return true;
    }

    protected boolean equalsModuloBijectiveVariableRenaming(ConjunctiveQuery query1, ConjunctiveQuery query2) {
        if (query1.getHeadTerms().size() != query2.getHeadTerms().size()) return false;

        Set<Predicate> predicates = new HashSet<>();
        predicates.addAll(query1.getBodyAtoms().stream().map(Atom::getPredicate).toList());
        predicates.addAll(query2.getBodyAtoms().stream().map(Atom::getPredicate).toList());

        Predicate headNewPredicate = new Predicate(generateUnusedPredicateName(predicates), query1.getHeadTerms().size());
        List<Literal> literals1 = new ArrayList<>(query1.getBody());
        literals1.add(new OrdinaryLiteral(new Atom(headNewPredicate, query1.getHeadTerms())));
        List<Literal> literals2 = new ArrayList<>(query2.getBody());
        literals2.add(new OrdinaryLiteral(new Atom(headNewPredicate, query2.getHeadTerms())));

        return isomorphismComparator.areIsomorphic(literals1, literals2);
    }

    private String generateUnusedPredicateName(Set<Predicate> predicates) {
        //TODO: improve method
        Set<String> predicateNames = predicates.stream().map(Predicate::getName).collect(Collectors.toSet());
        int i = 0;
        do {
            String candidate = BASE_STRING_FOR_NEW_PREDICATE_NAMES + i;
            if (!predicateNames.contains(candidate)) return candidate;
            i++;
        } while (i > -1);
        throw new RuntimeException("No unused predicate found! Improve method.");
    }
}
