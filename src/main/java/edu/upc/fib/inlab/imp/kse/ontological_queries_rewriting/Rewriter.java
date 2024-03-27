package edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting;

import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.domain.TGD;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.*;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.operations.Substitution;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.MGUFinder;
import edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.exceptions.NotFactorizableAtomSetException;
import edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.utils.normalizers.TGDNormalizer;

import java.util.*;

import static edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.utils.selectors.ApplicableAtomSetSelector.getApplicableAtomSets;
import static edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.utils.selectors.FactorizableAtomSetSelector.getFactorizableAtomSets;

public class Rewriter {

    public static final String VARIABLE_NAME_PREFIX = "unfTGD";

    public static List<ConjunctiveQuery> rewrite(ConjunctiveQuery query, Set<TGD> tgds) {
        Set<TGD> normalizedTGDs = normalizeTGDs(tgds);

        QueryRewriting queryRewriting = new QueryRewriting(query);
        int previousRewritingSize = 0;
        while (previousRewritingSize != queryRewriting.size()) {
            previousRewritingSize = queryRewriting.size();

            for (GeneratedQuery generatedQuery : queryRewriting.getUnexploredGeneratedQueries()) {
                for (TGD tgd : normalizedTGDs) {
                    //REWRITING STEP
                    Set<AtomIndexSet> applicableAtomSets = getApplicableAtomSets(generatedQuery.getQuery(), tgd);
                    for (AtomIndexSet applicableAtomSet : applicableAtomSets) {
                        ConjunctiveQuery rewrittenQuery = rewrite(generatedQuery.getQuery(), applicableAtomSet, tgd);
                        queryRewriting.addRewrittenQuery(rewrittenQuery);
                    }

                    //FACTORIZATION STEP
                    Set<AtomIndexSet> factorizableAtomSets = getFactorizableAtomSets(generatedQuery.getQuery(), tgd);
                    for (AtomIndexSet factorizableAtomSet : factorizableAtomSets) {
                        ConjunctiveQuery factorizedQuery = factorize(generatedQuery.getQuery(), factorizableAtomSet);
                        queryRewriting.addFactorizedQuery(factorizedQuery);
                    }
                }
                generatedQuery.setExplored();
            }
        }
        return queryRewriting.getRewritingQueries();
    }

    private static Set<TGD> normalizeTGDs(Set<TGD> tgds) {
        TGDNormalizer normalizer = new TGDNormalizer();
        return normalizer.normalize(tgds);
    }

    private static ConjunctiveQuery rewrite(ConjunctiveQuery query, AtomIndexSet applicableAtomSet, TGD tgd) {
        //TODO: implement method that accepts multiple atoms in a set (take context variables with care)
        if (applicableAtomSet.indexes().size() != 1) throw new RuntimeException("not supported yet");
        int toRewriteIndex = applicableAtomSet.indexes().stream().toList().get(0);
        Atom toRewriteAtom = query.getBodyAtoms().get(toRewriteIndex);

        Set<Variable> contextVariables = getContextVariables(query, toRewriteIndex);
        List<Literal> unfoldedTGDLiterals = unfoldTGDAtoms(toRewriteAtom, tgd, contextVariables);

        List<Term> newHead = query.getHeadTerms();
        List<Literal> newBody = new ArrayList<>();
        for (int p = 0; p < query.getBody().size(); p++) {
            Literal literal = query.getBody().get(p);
            if (p == toRewriteIndex) newBody.addAll(unfoldedTGDLiterals);
            else newBody.add(literal);
        }

        return QueryFactory.createConjunctiveQuery(newHead, newBody);
    }

    /**
     * Returns unfolded body of tgd with appropriate variable substitution and without collisions with context variables
     */
    private static List<Literal> unfoldTGDAtoms(Atom toRewriteAtom, TGD tgd, Set<Variable> contextVariables) {
        Substitution substitution = new Substitution();

        Atom tgdHeadAtom = tgd.getHead().get(0);
        for (int i = 0; i < tgdHeadAtom.getTerms().size(); i++) {
            Term term = tgdHeadAtom.getTerms().get(i);
            if (term instanceof Variable v) substitution.addMapping(v, toRewriteAtom.getTerms().get(i));
        }

        Set<Variable> localContextVariables = new HashSet<>(contextVariables);
        for (Literal literal : tgd.getBody()) {
            for (Term t : literal.getTerms())
                if (t instanceof Variable v && !tgdHeadAtom.getTerms().contains(v)) {
                    Variable newFreshVariable = NewFreshVariableFactory.createEnumeratedNewFreshVariable(VARIABLE_NAME_PREFIX, localContextVariables);
                    localContextVariables.add(newFreshVariable);
                    substitution.addMapping(v, newFreshVariable);
                }
        }

        List<Literal> unfoldedTGDBody = new ArrayList<>();
        for (Literal literal : tgd.getBody()) unfoldedTGDBody.add(literal.applySubstitution(substitution));
        return unfoldedTGDBody;
    }

    /**
     * Returns Variables appearing in Query but not in the selected Atom.
     */
    private static Set<Variable> getContextVariables(ConjunctiveQuery query, int atomIndex) {
        Atom selectedAtom = query.getBodyAtoms().get(atomIndex);
        Set<Variable> atomVariables = selectedAtom.getVariables();

        Set<Variable> result = new HashSet<>();
        for (Term t : query.getHeadTerms()) if (t instanceof Variable v && !atomVariables.contains(v)) result.add(v);
        for (Atom atom : query.getBodyAtoms()) {
            for (Term t : atom.getTerms()) if (t instanceof Variable v && !atomVariables.contains(v)) result.add(v);
        }
        return result;
    }

    private static ConjunctiveQuery factorize(ConjunctiveQuery query, AtomIndexSet factorizableAtomSet) {
        Set<Atom> toFactorize = getAtoms(query, factorizableAtomSet);
        Substitution unifyingSubstitution = getMGUSubstitutionFromFactorizableSet(toFactorize);

        Atom newAtom = toFactorize.stream().toList().get(0).applySubstitution(unifyingSubstitution);

        ImmutableTermList newHead = new ImmutableTermList(query.getHeadTerms()).applySubstitution(unifyingSubstitution);
        List<Atom> newBody = new ArrayList<>();
        boolean newAtomAdded = false;
        for (int p = 0; p < query.getBodyAtoms().size(); p++) {
            Atom atom = query.getBodyAtoms().get(p);
            if (!factorizableAtomSet.contains(p)) newBody.add(atom.applySubstitution(unifyingSubstitution));
            else if (!newAtomAdded) {
                newBody.add(newAtom);
                newAtomAdded = true;
            }
        }
        if (!newAtomAdded) newBody.add(newAtom);

        return QueryFactory.createConjunctiveQuery(newHead, getLiteralsFromAtoms(newBody));
    }

    private static Substitution getMGUSubstitutionFromFactorizableSet(Set<Atom> factorizableSet) {
        Optional<Substitution> unifyingSubstitutionOpt = MGUFinder.getAtomsMGU(factorizableSet.stream().toList());
        if (unifyingSubstitutionOpt.isEmpty())
            throw new NotFactorizableAtomSetException("factorizableAtomSet was not factorizable!");
        return unifyingSubstitutionOpt.get();
    }

    private static List<Literal> getLiteralsFromAtoms(List<Atom> newBody) {
        return newBody.stream().map(a -> (Literal) new OrdinaryLiteral(a)).toList();
    }

    private static Set<Atom> getAtoms(ConjunctiveQuery query, AtomIndexSet factorizableAtomSet) {
        Set<Atom> result = new HashSet<>();
        for (int p = 0; p < query.getBodyAtoms().size(); p++) {
            Atom atom = query.getBodyAtoms().get(p);
            if (factorizableAtomSet.contains(p)) result.add(atom);
        }
        return result;
    }
}
