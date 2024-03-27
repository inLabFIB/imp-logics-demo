package edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting;

import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.*;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.mothers.OrdinaryLiteralMother;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.mothers.QueryMother;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QueryRewritingTest {

    @Nested
    class EqualsModuloBijectiveVariableRenamingTests {

        private final QueryRewriting queryRewriting = new QueryRewriting();

        @Test
        void shouldReturnTrue_whenComparingTheSameQuery() {
            ConjunctiveQuery q = QueryMother.createBooleanConjunctiveQuery("q()");

            boolean isEqual = queryRewriting.equalsModuloBijectiveVariableRenaming(q, q);

            assertThat(isEqual).isTrue();
        }

        @Test
        void shouldReturnFalse_whenComparingDifferentQueries() {
            ConjunctiveQuery q = QueryMother.createBooleanConjunctiveQuery("q()");
            ConjunctiveQuery p = QueryMother.createBooleanConjunctiveQuery("p()");

            boolean isEqual = queryRewriting.equalsModuloBijectiveVariableRenaming(q, p);

            assertThat(isEqual).isFalse();
        }

        @Test
        void shouldReturnTrue_whenComparingDifferentQueries_butRenamingIsPossible() {
            Predicate p = new Predicate("p", 2);
            ConjunctiveQuery q1 = QueryFactory.createConjunctiveQuery(
                    List.of(),
                    List.of(new OrdinaryLiteral(new Atom(p, List.of(new Variable("a"), new Variable("b")))))
            );
            ConjunctiveQuery q2 = QueryFactory.createConjunctiveQuery(
                    List.of(),
                    List.of(new OrdinaryLiteral(new Atom(p, List.of(new Variable("x"), new Variable("y")))))
            );

            boolean isEqual = queryRewriting.equalsModuloBijectiveVariableRenaming(q1, q2);

            assertThat(isEqual).isTrue();
        }

        @Test
        void shouldReturnFalse_whenComparingDifferentQueries_andBijectiveRenamingIsNotPossible() {
            Predicate p = new Predicate("p", 2);
            ConjunctiveQuery q1 = QueryFactory.createConjunctiveQuery(
                    List.of(),
                    List.of(new OrdinaryLiteral(new Atom(p, List.of(new Variable("a"), new Variable("a")))))
            );
            ConjunctiveQuery q2 = QueryFactory.createConjunctiveQuery(
                    List.of(),
                    List.of(new OrdinaryLiteral(new Atom(p, List.of(new Variable("x"), new Variable("y")))))
            );

            boolean isEqual = queryRewriting.equalsModuloBijectiveVariableRenaming(q1, q2);

            assertThat(isEqual).isFalse();
        }

        @Nested
        class QueryComparisonsWithHeadTermsTests {

            @Test
            void shouldReturnFalse_whenDifferentHeadTermsSizes() {
                List<Literal> body = List.of(OrdinaryLiteralMother.createOrdinaryLiteral("q(a,b)", ""));
                ConjunctiveQuery q1 = QueryFactory.createConjunctiveQuery(List.of(new Variable("a"), new Variable("a")), body);
                ConjunctiveQuery q2 = QueryFactory.createConjunctiveQuery(List.of(new Variable("a")), body);

                boolean isEqual = queryRewriting.equalsModuloBijectiveVariableRenaming(q1, q2);

                assertThat(isEqual).isFalse();
            }

            @Test
            void shouldReturnTrue_whenComparingDifferentQueries_withSameBody_andSameHead() {
                List<Literal> body = List.of(OrdinaryLiteralMother.createOrdinaryLiteral("q(a,b)", ""));
                ConjunctiveQuery q1 = QueryFactory.createConjunctiveQuery(List.of(new Variable("a")), body);
                ConjunctiveQuery q2 = QueryFactory.createConjunctiveQuery(List.of(new Variable("a")), body);

                boolean isEqual = queryRewriting.equalsModuloBijectiveVariableRenaming(q1, q2);

                assertThat(isEqual).isTrue();
            }

            @Test
            void shouldReturnFalse_whenComparingDifferentQueries_withSameBody_butDifferentHeadTerms() {
                List<Literal> body = List.of(OrdinaryLiteralMother.createOrdinaryLiteral("q(a,b)", ""));
                ConjunctiveQuery q1 = QueryFactory.createConjunctiveQuery(List.of(new Variable("a")), body);
                ConjunctiveQuery q2 = QueryFactory.createConjunctiveQuery(List.of(new Variable("b")), body);

                boolean isEqual = queryRewriting.equalsModuloBijectiveVariableRenaming(q1, q2);

                assertThat(isEqual).isFalse();
            }

            @Test
            void shouldReturnTrue_whenComparingDifferentQueries_withSameBody_butDifferentHeadTerms_butStillIsomorphic() {
                List<Literal> body = List.of(
                        OrdinaryLiteralMother.createOrdinaryLiteral("q(a)", ""),
                        OrdinaryLiteralMother.createOrdinaryLiteral("q(b)", "")
                );
                ConjunctiveQuery q1 = QueryFactory.createConjunctiveQuery(List.of(new Variable("a")), body);
                ConjunctiveQuery q2 = QueryFactory.createConjunctiveQuery(List.of(new Variable("b")), body);

                boolean isEqual = queryRewriting.equalsModuloBijectiveVariableRenaming(q1, q2);

                assertThat(isEqual).isTrue();
            }
        }

    }

    @Nested
    class QueryAdditionTests {

        @Test
        void shouldAcceptNewRewrittenQuery_ifNoDuplicatesFound() {
            QueryRewriting queryRewriting = new QueryRewriting();
            ConjunctiveQuery query = QueryMother.createBooleanConjunctiveQuery("p(x)");

            boolean hasBeenAdded = queryRewriting.addRewrittenQuery(query);

            assertThat(hasBeenAdded).isTrue();
        }

        @Test
        void shouldAcceptNewFactorizedQuery_ifNoDuplicatesFound() {
            QueryRewriting queryRewriting = new QueryRewriting();
            ConjunctiveQuery query = QueryMother.createBooleanConjunctiveQuery("p(x)");

            boolean hasBeenAdded = queryRewriting.addFactorizedQuery(query);

            assertThat(hasBeenAdded).isTrue();
        }

        @Test
        void shouldNotAcceptNewRewrittenQuery_ifDuplicatesFound() {
            QueryRewriting queryRewriting = new QueryRewriting();
            ConjunctiveQuery query = QueryMother.createBooleanConjunctiveQuery("p(x)");

            queryRewriting.addRewrittenQuery(query);
            boolean hasBeenAdded = queryRewriting.addRewrittenQuery(query);

            assertThat(hasBeenAdded).isFalse();
        }

        @Test
        void shouldAcceptNewRewrittenQuery_ifDuplicatesFound_butFactorized() {
            QueryRewriting queryRewriting = new QueryRewriting();
            ConjunctiveQuery query = QueryMother.createBooleanConjunctiveQuery("p(x)");

            queryRewriting.addFactorizedQuery(query);
            boolean hasBeenAdded = queryRewriting.addRewrittenQuery(query);

            assertThat(hasBeenAdded).isTrue();
        }

        @Test
        void shouldNotAcceptNewFactorizedQuery_ifFactorizedDuplicatesFound() {
            QueryRewriting queryRewriting = new QueryRewriting();
            ConjunctiveQuery query = QueryMother.createBooleanConjunctiveQuery("p(x)");

            queryRewriting.addFactorizedQuery(query);
            boolean hasBeenAdded = queryRewriting.addFactorizedQuery(query);

            assertThat(hasBeenAdded).isFalse();
        }

        @Test
        void shouldNotAcceptNewFactorizedQuery_ifRewrittenDuplicatesFound() {
            QueryRewriting queryRewriting = new QueryRewriting();
            ConjunctiveQuery query = QueryMother.createBooleanConjunctiveQuery("p(x)");

            queryRewriting.addRewrittenQuery(query);
            boolean hasBeenAdded = queryRewriting.addFactorizedQuery(query);

            assertThat(hasBeenAdded).isFalse();
        }
    }
}