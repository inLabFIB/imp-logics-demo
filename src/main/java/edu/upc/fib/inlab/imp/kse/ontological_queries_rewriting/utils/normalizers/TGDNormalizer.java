package edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.utils.normalizers;

import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.domain.TGD;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class normalizes a TGD to a set of TGDs in normal form with one head atom and one existentially quantified variable.
 * The approach to obtain such normalized TGDs is based on "Ontological Queries: Rewriting and Optimization (Extended Version)"
 * by Gottlob et al.
 *
 * @deprecated
 *
 * <p> Use {@link TGDNormalizerProcess} instead
 */
@Deprecated
public class TGDNormalizer {

    //todo: change this aux string for using original predicate name adding numbering at the end (collaborator should be
    // created)
    private static final String AUX_PREDICATE_NAME = "AUX";
    private int auxPredicateNameIndex = 1;

    public Set<TGD> normalize(Set<TGD> originalTGDs) {
        Set<String> usedPredicateNames = originalTGDs.stream()
                .map(this::obtainPredicateNames)
                .flatMap(Set::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return originalTGDs.stream()
                .map(tgd -> normalize(tgd, usedPredicateNames))
                .flatMap(Set::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<TGD> normalize(TGD tgd) {
        Set<String> usedPredicateNames = obtainPredicateNames(tgd);
        return normalize(tgd, usedPredicateNames);
    }

    private Set<TGD> normalize(TGD originalTGD, Set<String> usedPredicateNames) {
        if (isNormalForm(originalTGD)) {
            return Set.of(originalTGD);
        }
        Set<TGD> tgdWithOneAtom = normalizeWithOneHeadAtom(originalTGD, usedPredicateNames);
        return tgdWithOneAtom.stream()
                .map(tgd -> normalizeExistentiallyQuantifiedVariables(tgd, usedPredicateNames))
                .flatMap(Set::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<TGD> normalizeWithOneHeadAtom(TGD originalTGD) {
        return normalizeWithOneHeadAtom(originalTGD, obtainPredicateNames(originalTGD));
    }

    private Set<TGD> normalizeWithOneHeadAtom(TGD originalTGD, Set<String> usedPredicateNames) {
        if (containsOneHeadAtom(originalTGD)) {
            return Set.of(originalTGD);
        }

        Set<TGD> result = new LinkedHashSet<>();
        TGD firstTGD = createNewTGD(originalTGD, usedPredicateNames);
        result.add(firstTGD);

        Atom firstHeadAtom = firstTGD.getHead().stream().findFirst().orElseThrow();
        ImmutableLiteralsList newBody = new ImmutableLiteralsList(new OrdinaryLiteral(firstHeadAtom));
        for (Atom atom : originalTGD.getHead()) {
            TGD newTGD = new TGD(newBody, new ImmutableAtomList(atom));
            result.add(newTGD);
        }
        return result;
    }

    public Set<TGD> normalizeExistentiallyQuantifiedVariables(TGD originalTGD) {
        return normalizeExistentiallyQuantifiedVariables(originalTGD, obtainPredicateNames(originalTGD));
    }

    private Set<TGD> normalizeExistentiallyQuantifiedVariables(TGD originalTGD, Set<String> usedPredicateNames) {
        if (containsOneExistentiallyQuantifiedVariable(originalTGD)) {
            return Set.of(originalTGD);
        }

        Set<TGD> result = new LinkedHashSet<>();
        Set<Variable> allExistentiallyQuantifiedVariables = originalTGD.getExistentialVariables();

        Set<Variable> allVariablesOfBodyAndHead = originalTGD.getFrontierVariables();
        List<Term> previousTermList = new ArrayList<>(allVariablesOfBodyAndHead);
        List<Literal> nextBody = originalTGD.getBody();

        for (Variable nextVariable : allExistentiallyQuantifiedVariables) {
            List<Term> newTermList = new ArrayList<>(previousTermList);
            newTermList.add(nextVariable);
            ImmutableAtomList newHead = createNewHead(newTermList, usedPredicateNames);
            TGD newTgd = new TGD(nextBody, newHead);
            result.add(newTgd);
            previousTermList = newTermList;
            nextBody = createBodyFromHead(newTgd.getHead());
        }

        //Case with same head
        TGD newTgd = new TGD(nextBody, originalTGD.getHead());
        result.add(newTgd);
        return result;
    }

    private TGD createNewTGD(TGD originalTGD, Set<String> usedPredicateNames) {
        Set<Term> allVariablesOfHead = obtainVariablesOfHead(originalTGD);
        Predicate auxPredicate = new MutablePredicate(createNewAuxPredicateName(usedPredicateNames), allVariablesOfHead.size());
        Atom newHeadAtom = new Atom(auxPredicate, allVariablesOfHead.stream().toList());
        ImmutableAtomList newHead = new ImmutableAtomList(newHeadAtom);
        return new TGD(originalTGD.getBody(), newHead);
    }

    private static Set<Term> obtainVariablesOfHead(TGD tgd) {
        return tgd.getHead().stream()
                .map(Atom::getVariables)
                .flatMap(Set::stream)
                //cast to Term
                .map(t -> (Term) t)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private ImmutableAtomList createNewHead(List<Term> termList, Set<String> usedPredicateNames) {
        Predicate nextPredicate = new MutablePredicate(createNewAuxPredicateName(usedPredicateNames), termList.size());
        Atom nextHeadAtom = new Atom(nextPredicate, termList);
        return new ImmutableAtomList(nextHeadAtom);
    }

    private static List<Literal> createBodyFromHead(ImmutableAtomList head) {
        return head.stream().map(a -> (Literal) new OrdinaryLiteral(a)).toList();
    }

    public static boolean isNormalForm(TGD tgd) {
        return containsOneHeadAtom(tgd) && containsOneExistentiallyQuantifiedVariable(tgd);
    }

    public static boolean containsOneHeadAtom(TGD tgd) {
        return tgd.getHead().size() == 1;
    }

    public static boolean containsOneExistentiallyQuantifiedVariable(TGD tgd) {
        return tgd.getExistentialVariables().size() == 1;
    }

    private String createNewAuxPredicateName(Set<String> usedPredicateNames) {
        String newAuxPredicate = AUX_PREDICATE_NAME + auxPredicateNameIndex++;
        while (usedPredicateNames.contains(newAuxPredicate)) {
            newAuxPredicate = AUX_PREDICATE_NAME + auxPredicateNameIndex++;
        }
        return newAuxPredicate;
    }

    private Set<String> obtainPredicateNames(TGD tgd) {
        Set<String> predicateNames = new LinkedHashSet<>();
        Set<String> headPredicateNames = tgd.getHead().stream()
                .map(Atom::getPredicate).
                map(Predicate::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> bodyPredicateNames = tgd.getBody().stream()
                .filter(OrdinaryLiteral.class::isInstance)
                .map(OrdinaryLiteral.class::cast)
                .map(OrdinaryLiteral::getAtom)
                .map(Atom::getPredicate)
                .map(Predicate::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        predicateNames.addAll(headPredicateNames);
        predicateNames.addAll(bodyPredicateNames);
        return predicateNames;
    }

}
