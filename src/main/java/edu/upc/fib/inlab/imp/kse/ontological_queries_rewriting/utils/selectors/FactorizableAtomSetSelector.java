package edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.utils.selectors;

import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.domain.TGD;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.*;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.MGUFinder;
import edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.AtomIndexSet;

import java.util.*;

public class FactorizableAtomSetSelector {

    protected FactorizableAtomSetSelector() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @param query Query
     * @param tgd TGD in a Normal Form
     * @return a set of indexSets each one pointing at a set of atoms of query which are factorizable w.r.t tgd.
     */
    public static Set<AtomIndexSet> getFactorizableAtomSets(ConjunctiveQuery query, TGD tgd) {
        Optional<PredicatePosition> existentialPositionOpt = getExistentialPosition(tgd);
        if (existentialPositionOpt.isEmpty()) return Collections.emptySet();
        PredicatePosition existentialPosition = existentialPositionOpt.get();

        Set<IndexedAtom> candidateAtoms = getCandidateAtoms(query, existentialPosition.predicate());
        if (candidateAtoms.size() < 2) return Collections.emptySet();

        Map<Term, Set<IndexedAtom>> candidatesForTerm = divideCandidateAtomsForVariableInExistentialPosition(candidateAtoms, existentialPosition.position());

        Set<AtomIndexSet> result = new HashSet<>();
        for (Map.Entry<Term, Set<IndexedAtom>> entry : candidatesForTerm.entrySet()) {
            Integer[] indexes = entry.getValue().stream().map(IndexedAtom::index).toArray(Integer[]::new);
            AtomIndexSet atomIndexSet = new AtomIndexSet(indexes);
            if (isFactorizable(tgd, query, atomIndexSet)) result.add(atomIndexSet);
            //else not (X. optimization idea(think about it more))
        }
        return result;
    }

    private static Map<Term, Set<IndexedAtom>> divideCandidateAtomsForVariableInExistentialPosition(Set<IndexedAtom> candidateAtoms, int position) {
        Map<Term, Set<IndexedAtom>> setsForTerm = new HashMap<>();

        for (IndexedAtom indexedAtom : candidateAtoms) {
            Term varInExistentialPosition = indexedAtom.atom().getTerms().get(position);
            setsForTerm.compute(varInExistentialPosition, (k, v) -> {
                if (v == null) v = new HashSet<>();
                v.add(indexedAtom);
                return v;
            });
        }

        return setsForTerm;
    }

    private static Set<IndexedAtom> getCandidateAtoms(ConjunctiveQuery query, Predicate predicate) {
        Set<IndexedAtom> indexedAtoms = new HashSet<>();
        for (int i = 0; i < query.getBodyAtoms().size(); i++) {
            Atom atom = query.getBodyAtoms().get(i);
            if (atom.getPredicate().equals(predicate)) indexedAtoms.add(new IndexedAtom(i, atom));
        }
        return indexedAtoms;
    }


    protected static boolean isFactorizable(TGD tgd, ConjunctiveQuery query, AtomIndexSet selectedAtomsIndex) {
        // CONDITION 0 : Set S has size of at least 2
        if (selectedAtomsIndex.indexes().size() < 2) return false;

        Set<Atom> selectedAtoms = new HashSet<>();
        Set<Atom> notSelectedAtoms = new HashSet<>();

        ImmutableAtomList bodyAtoms = query.getBodyAtoms();
        for (int i = 0; i < bodyAtoms.size(); i++) {
            Atom a = bodyAtoms.get(i);
            if (selectedAtomsIndex.contains(i)) selectedAtoms.add(a);
            else notSelectedAtoms.add(a);
        }

        // CONDITION 1 : Set S unifies
        if (!MGUFinder.areAtomsUnifiable(selectedAtoms.stream().toList())) return false;

        Optional<PredicatePosition> existentialPositionOptional = getExistentialPosition(tgd);

        // CONDITION 2 : existential position exists
        if (existentialPositionOptional.isEmpty()) return false;
        PredicatePosition existentialPosition = existentialPositionOptional.get();

        // CONDITION 3 : set shares variable in existentialPosition & not found outside set
        Optional<Variable> varInExistentialPositionOpt = getCommonVariableInSet(selectedAtoms, existentialPosition);
        if (varInExistentialPositionOpt.isEmpty()) return false;
        Variable varInExistentialPosition = varInExistentialPositionOpt.get();

        for (Atom a : selectedAtoms) {
            if (!a.getPredicate().equals(existentialPosition.predicate())) return false;
            if (!a.getTerms().get(existentialPosition.position()).equals(varInExistentialPosition)) return false;

            for (int i = 0; i < a.getTerms().size(); i++) {
                if (i == existentialPosition.position()) continue;
                Term t = a.getTerms().get(i);
                if (t instanceof Variable v && v.equals(varInExistentialPosition)) return false;
            }
        }

        if (query.getHeadTerms().stream().anyMatch(t -> t.equals(varInExistentialPosition))) return false;

        return notSelectedAtoms.stream()
                .map(Atom::getTerms)
                .flatMap(Collection::stream)
                .noneMatch(t -> t.equals(varInExistentialPosition));
    }

    private static Optional<Variable> getCommonVariableInSet(Set<Atom> selectedAtoms, PredicatePosition existentialPosition) {
        Atom a1 = selectedAtoms.stream().toList().get(0);
        if (a1.getTerms().size() < existentialPosition.position() + 1) return Optional.empty();
        Term term = a1.getTerms().get(existentialPosition.position());
        if (term instanceof Variable v) return Optional.of(v);
        return Optional.empty();
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
