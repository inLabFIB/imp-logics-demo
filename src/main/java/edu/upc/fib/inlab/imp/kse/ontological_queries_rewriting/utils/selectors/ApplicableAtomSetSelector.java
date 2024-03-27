package edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.utils.selectors;

import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.domain.TGD;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.*;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.MGUFinder;
import edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.AtomIndexSet;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ApplicableAtomSetSelector {

    protected ApplicableAtomSetSelector() {
        throw new IllegalStateException("Utility class");
    }


    /**
     * Trivial implementation returning sets of arity 1
     */
    public static Set<AtomIndexSet> getApplicableAtomSets(ConjunctiveQuery query, TGD tgd) {
        Set<AtomIndexSet> result = new HashSet<>();

        //TODO: implement better version: return sets described in paper so as to reduce final rewriting size

        for (int i = 0; i < query.getBodyAtoms().size(); i++)
            if (isApplicable(tgd, query, i)) result.add(new AtomIndexSet(i));

        return result;
    }

    public static boolean isApplicable(TGD tgd, ConjunctiveQuery query, int atomIndex) {
        Atom head = tgd.getHead().get(0);
        Atom selectedAtom = query.getBodyAtoms().get(atomIndex);

        // CONDITION 1 : Atom unifies with head
        if (!MGUFinder.areAtomsUnifiable(selectedAtom, head)) return false;

        Optional<PredicatePosition> existentialPositionOpt = getExistentialPosition(tgd);
        if (existentialPositionOpt.isEmpty()) return true;
        PredicatePosition existentialPosition = existentialPositionOpt.get();

        // CONDITION 2 : Unshared variable in existential position
        return unsharedVariableInExistentialPosition(query, selectedAtom, existentialPosition);
    }

    private static boolean unsharedVariableInExistentialPosition(ConjunctiveQuery query, Atom atom, PredicatePosition predicatePosition) {
        Term existentialPositionTerm = atom.getTerms().get(predicatePosition.position());

        if (existentialPositionTerm instanceof Constant) return false;

        for (Atom a : query.getBodyAtoms()) {
            for (int p = 0; p < a.getTerms().size(); p++) {
                if (a.equals(atom) && p == predicatePosition.position()) continue;
                Term term = a.getTerms().get(p);
                if (term.equals(existentialPositionTerm)) return false;
            }
        }

        return true;
    }

    /**
     * Assuming tgd normal form.
     */
    private static Optional<PredicatePosition> getExistentialPosition(TGD tgd) {
        Set<Variable> existentialVariables = tgd.getExistentialVariables();
        if (existentialVariables.isEmpty()) return Optional.empty();
        Term existentialVariable = existentialVariables.stream().toList().get(0);
        Atom atom = tgd.getHead().get(0);
        ImmutableTermList terms = atom.getTerms();
        for (int i = 0; i < terms.size(); i++) {
            Term t = terms.get(i);
            if (t.equals(existentialVariable)) return Optional.of(new PredicatePosition(atom.getPredicate(), i));
        }
        return Optional.empty();
    }
}
