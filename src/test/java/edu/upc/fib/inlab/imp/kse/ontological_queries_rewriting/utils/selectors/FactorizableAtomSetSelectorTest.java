package edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.utils.selectors;

import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.domain.DependencySchema;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.domain.TGD;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.mothers.TGDMother;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.services.parser.DependencySchemaParser;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.ConjunctiveQuery;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.OrdinaryLiteral;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.QueryFactory;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.Variable;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.mothers.LiteralMother;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.mothers.QueryMother;
import edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.AtomIndexSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FactorizableAtomSetSelectorTest {

    @Test
    void shouldThrowException_whenCallingConstructorOfClass() {
        assertThatThrownBy(FactorizableAtomSetSelector::new).isInstanceOf(IllegalStateException.class);
    }

    @Nested
    class FactorizabilityTests {

        @ParameterizedTest
        @MethodSource("queryAndSelectedAtoms")
        void shouldReturnFalse_whenSelectingFactorizableSet_OfSizeLessThanTwo(String name, ConjunctiveQuery query, AtomIndexSet indexSet) {
            TGD tgd = TGDMother.createTGD("p(x) -> q(x)");
            boolean isFactorizable = FactorizableAtomSetSelector.isFactorizable(tgd, query, indexSet);

            assertThat(isFactorizable).as(name).isFalse();
        }

        private static Stream<Arguments> queryAndSelectedAtoms() {
            return Stream.of(
                    Arguments.of("Only 1 index",
                            QueryMother.createBooleanConjunctiveQuery("p(a,b), q(c,d)"),
                            new AtomIndexSet(Set.of(0))
                    ),
                    Arguments.of("0 index",
                            QueryMother.createBooleanConjunctiveQuery("p(a,b), q(c,d)"),
                            new AtomIndexSet(Set.of())
                    )
            );
        }

        /**
         * Test case:
         * <ul>
         *     <li>TGD: p(x) -> q(x,y)</li>
         *     <li>Query: q(a,b), q(a,b)</li>
         * </ul>
         */
        @Test
        void shouldReturnTrue_whenSelectingFactorizableSet_containingIdenticalAtoms() {
            DependencySchema schema = new DependencySchemaParser().parse("""
                    p(x) -> q(x,y)
                    """);
            TGD tgd = schema.getAllTGDs().get(0);
            OrdinaryLiteral p_lit = LiteralMother.createOrdinaryLiteral(schema, "q", "a", "b");
            ConjunctiveQuery query = QueryFactory.createConjunctiveQuery(List.of(), List.of(p_lit, p_lit));
            AtomIndexSet indexSet = new AtomIndexSet(0, 1);

            boolean isFactorizable = FactorizableAtomSetSelector.isFactorizable(tgd, query, indexSet);

            assertThat(isFactorizable).isTrue();
        }

        /**
         * Test case:
         * <ul>
         *     <li>TGD: p(x,y) -> q(x,y,z)</li>
         *     <li>Query: q(a,b,c), q(aa,bb,c), q(d,d,c)</li>
         * </ul>
         */
        @Test
        void shouldReturnTrue_whenSelectingFactorizableSet_containingDifferentAtoms() {
            DependencySchema schema = new DependencySchemaParser().parse("""
                    p(x,y) -> q(x,y,z)
                    """);
            TGD tgd = schema.getAllTGDs().get(0);
            OrdinaryLiteral p_lit1 = LiteralMother.createOrdinaryLiteral(schema, "q", "a", "b", "c");
            OrdinaryLiteral p_lit2 = LiteralMother.createOrdinaryLiteral(schema, "q", "aa", "bb", "c");
            OrdinaryLiteral p_lit3 = LiteralMother.createOrdinaryLiteral(schema, "q", "d", "d", "c");
            ConjunctiveQuery query = QueryFactory.createConjunctiveQuery(List.of(), List.of(p_lit1, p_lit2, p_lit3));
            AtomIndexSet indexSet = new AtomIndexSet(0, 1, 2);

            boolean isFactorizable = FactorizableAtomSetSelector.isFactorizable(tgd, query, indexSet);

            assertThat(isFactorizable).isTrue();
        }

        /**
         * Test case:
         * <ul>
         *     <li>TGD: p(x) -> q(x,y)</li>
         *     <li>Query: q(a,b), p(a)</li>
         * </ul>
         */
        @Test
        void shouldReturnFalse_whenSelectingFactorizableSet_whichDoesNotUnify() {
            DependencySchema schema = new DependencySchemaParser().parse("""
                    p(x) -> q(x,y)
                    """);
            TGD tgd = schema.getAllTGDs().get(0);
            OrdinaryLiteral p_lit = LiteralMother.createOrdinaryLiteral(schema, "p", "a");
            OrdinaryLiteral q_lit = LiteralMother.createOrdinaryLiteral(schema, "q", "a", "b");
            ConjunctiveQuery query = QueryFactory.createConjunctiveQuery(List.of(), List.of(p_lit, q_lit));
            AtomIndexSet indexSet = new AtomIndexSet(0, 1);

            boolean isFactorizable = FactorizableAtomSetSelector.isFactorizable(tgd, query, indexSet);

            assertThat(isFactorizable).isFalse();
        }

        /**
         * Test case:
         * <ul>
         *     <li>TGD: p(x) -> q(x,y)</li>
         *     <li>Query: q(1,b), q(2,b)</li>
         * </ul>
         */
        @Test
        void shouldReturnFalse_whenSelectingFactorizableSet_whichDoesNotUnify2() {
            DependencySchema schema = new DependencySchemaParser().parse("""
                    p(x) -> q(x,y)
                    """);
            TGD tgd = schema.getAllTGDs().get(0);
            OrdinaryLiteral q_lit1 = LiteralMother.createOrdinaryLiteral(schema, "q", "1", "b");
            OrdinaryLiteral q_lit2 = LiteralMother.createOrdinaryLiteral(schema, "q", "2", "b");
            ConjunctiveQuery query = QueryFactory.createConjunctiveQuery(List.of(), List.of(q_lit1, q_lit2));
            AtomIndexSet indexSet = new AtomIndexSet(0, 1);

            boolean isFactorizable = FactorizableAtomSetSelector.isFactorizable(tgd, query, indexSet);

            assertThat(isFactorizable).isFalse();
        }

        /**
         * Test case:
         * <ul>
         *     <li>TGD: p(x) -> q(x,y)</li>
         *     <li>Query: p(a), p(a)</li>
         * </ul>
         */
        @Test
        void shouldReturnFalse_whenSelectingFactorizableSet_whichUnifies_butDoesNotSharePredicateWithHead() {
            DependencySchema schema = new DependencySchemaParser().parse("""
                    p(x) -> q(x,y)
                    """);
            TGD tgd = schema.getAllTGDs().get(0);
            OrdinaryLiteral p_lit = LiteralMother.createOrdinaryLiteral(schema, "p", "a");
            ConjunctiveQuery query = QueryFactory.createConjunctiveQuery(List.of(), List.of(p_lit, p_lit));
            AtomIndexSet indexSet = new AtomIndexSet(0, 1);

            boolean isFactorizable = FactorizableAtomSetSelector.isFactorizable(tgd, query, indexSet);

            assertThat(isFactorizable).isFalse();
        }

        /**
         * Test case:
         * <ul>
         *     <li>TGD: p(x) -> q(x)</li>
         *     <li>Query: q(a), q(a)</li>
         * </ul>
         */
        @Test
        void shouldReturnFalse_whenSelectingTGDWithoutExistentialPosition() {
            DependencySchema schema = new DependencySchemaParser().parse("""
                    p(x) -> q(x)
                    """);
            TGD tgd = schema.getAllTGDs().get(0);
            OrdinaryLiteral q_lit = LiteralMother.createOrdinaryLiteral(schema, "q", "a");
            ConjunctiveQuery query = QueryFactory.createConjunctiveQuery(List.of(), List.of(q_lit, q_lit));
            AtomIndexSet indexSet = new AtomIndexSet(0, 1);

            boolean isFactorizable = FactorizableAtomSetSelector.isFactorizable(tgd, query, indexSet);

            assertThat(isFactorizable).isFalse();
        }

        /**
         * Test case:
         * <ul>
         *     <li>TGD: p(x) -> q(x,y)</li>
         *     <li>Query: q(a,1), q(a,1)</li>
         * </ul>
         */
        @Test
        void shouldReturnFalse_whenSelectingTGD_withConstant_inExistentialPositions() {
            DependencySchema schema = new DependencySchemaParser().parse("""
                    p(x) -> q(x,y)
                    """);
            TGD tgd = schema.getAllTGDs().get(0);
            OrdinaryLiteral q_lit1 = LiteralMother.createOrdinaryLiteral(schema, "q", "a", "1");
            OrdinaryLiteral q_lit2 = LiteralMother.createOrdinaryLiteral(schema, "q", "a", "1");
            ConjunctiveQuery query = QueryFactory.createConjunctiveQuery(List.of(), List.of(q_lit1, q_lit2));
            AtomIndexSet indexSet = new AtomIndexSet(0, 1);

            boolean isFactorizable = FactorizableAtomSetSelector.isFactorizable(tgd, query, indexSet);

            assertThat(isFactorizable).isFalse();
        }

        /**
         * Test case:
         * <ul>
         *     <li>TGD: p(x) -> q(x,y)</li>
         *     <li>Query: q(a,b), q(a,c)</li>
         * </ul>
         */
        @Test
        void shouldReturnFalse_whenSelectingTGD_withNoSharedVariable_inExistentialPositions() {
            DependencySchema schema = new DependencySchemaParser().parse("""
                    p(x) -> q(x,y)
                    """);
            TGD tgd = schema.getAllTGDs().get(0);
            OrdinaryLiteral q_lit1 = LiteralMother.createOrdinaryLiteral(schema, "q", "a", "b");
            OrdinaryLiteral q_lit2 = LiteralMother.createOrdinaryLiteral(schema, "q", "a", "c");
            ConjunctiveQuery query = QueryFactory.createConjunctiveQuery(List.of(), List.of(q_lit1, q_lit2));
            AtomIndexSet indexSet = new AtomIndexSet(0, 1);

            boolean isFactorizable = FactorizableAtomSetSelector.isFactorizable(tgd, query, indexSet);

            assertThat(isFactorizable).isFalse();
        }

        /**
         * Test case:
         * <ul>
         *     <li>TGD: p(x) -> q(x,y)</li>
         *     <li>Query: q(a,b), q(a,b), p(b)</li>
         * </ul>
         */
        @Test
        void shouldReturnFalse_whenSelectingTGD_withSharedVariable_inExistentialPosition_butSharedOutsideSet() {
            DependencySchema schema = new DependencySchemaParser().parse("""
                    p(x) -> q(x,y)
                    """);
            TGD tgd = schema.getAllTGDs().get(0);
            OrdinaryLiteral p_lit = LiteralMother.createOrdinaryLiteral(schema, "p", "b");
            OrdinaryLiteral q_lit = LiteralMother.createOrdinaryLiteral(schema, "q", "a", "b");
            ConjunctiveQuery query = QueryFactory.createConjunctiveQuery(List.of(), List.of(q_lit, q_lit, p_lit));
            AtomIndexSet indexSet = new AtomIndexSet(0, 1);

            boolean isFactorizable = FactorizableAtomSetSelector.isFactorizable(tgd, query, indexSet);

            assertThat(isFactorizable).isFalse();
        }

        /**
         * Test case:
         * <ul>
         *     <li>TGD: p(x) -> q(x,y)</li>
         *     <li>Query: query(b) <- q(a,b), q(a,b)</li>
         * </ul>
         */
        @Test
        void shouldReturnFalse_whenSelectingTGD_withSharedVariable_inExistentialPosition_butSharedOutsideSet_inQueryHead() {
            DependencySchema schema = new DependencySchemaParser().parse("""
                    p(x) -> q(x,y)
                    """);
            TGD tgd = schema.getAllTGDs().get(0);
            OrdinaryLiteral q_lit = LiteralMother.createOrdinaryLiteral(schema, "q", "a", "b");
            ConjunctiveQuery query = QueryFactory.createConjunctiveQuery(List.of(new Variable("b")), List.of(q_lit, q_lit));
            AtomIndexSet indexSet = new AtomIndexSet(0, 1);

            boolean isFactorizable = FactorizableAtomSetSelector.isFactorizable(tgd, query, indexSet);

            assertThat(isFactorizable).isFalse();
        }

        @Nested
        class PaperExamples {

            private final DependencySchema exampleSchema = new DependencySchemaParser().parse("""
                    s(x), r(x,y) -> t(x,y,z)
                    """);

            /**
             * Test case:
             * <ul>
             *     <li>TGD: s(x), r(x,y) -> t(x,y,z)</li>
             *     <li>Query: q1() <- t(a,b,c), t(a,e,c)</li>
             * </ul>
             */
            @Test
            void shouldReturnTrue_example1() {
                TGD tgd = exampleSchema.getAllTGDs().get(0);
                OrdinaryLiteral t_lit1 = LiteralMother.createOrdinaryLiteral(exampleSchema, "t", "a", "b", "c");
                OrdinaryLiteral t_lit2 = LiteralMother.createOrdinaryLiteral(exampleSchema, "t", "a", "e", "c");
                ConjunctiveQuery query = QueryFactory.createConjunctiveQuery(List.of(), List.of(t_lit1, t_lit2));
                AtomIndexSet indexSet = new AtomIndexSet(0, 1);

                boolean isFactorizable = FactorizableAtomSetSelector.isFactorizable(tgd, query, indexSet);

                assertThat(isFactorizable).isTrue();
            }

            /**
             * Test case:
             * <ul>
             *     <li>TGD: s(x), r(x,y) -> t(x,y,z)</li>
             *     <li>Query: q1() <- t(1,a,c), t(b,1,c)</li>
             * </ul>
             */
            @Test
            void shouldReturnTrue_example1_2() {
                TGD tgd = exampleSchema.getAllTGDs().get(0);
                OrdinaryLiteral t_lit1 = LiteralMother.createOrdinaryLiteral(exampleSchema, "t", "1", "a", "c");
                OrdinaryLiteral t_lit2 = LiteralMother.createOrdinaryLiteral(exampleSchema, "t", "b", "1", "c");
                ConjunctiveQuery query = QueryFactory.createConjunctiveQuery(List.of(), List.of(t_lit1, t_lit2));
                AtomIndexSet indexSet = new AtomIndexSet(0, 1);

                boolean isFactorizable = FactorizableAtomSetSelector.isFactorizable(tgd, query, indexSet);

                assertThat(isFactorizable).isTrue();
            }

            /**
             * Test case:
             * <ul>
             *     <li>TGD: s(x), r(x,y) -> t(x,y,z)</li>
             *     <li>Query: q2() <- s(c), t(a,b,c), t(a,e,c)</li>
             * </ul>
             */
            @Test
            void shouldReturnTrue_example2() {
                TGD tgd = exampleSchema.getAllTGDs().get(0);
                OrdinaryLiteral s_lit = LiteralMother.createOrdinaryLiteral("s", "c");
                OrdinaryLiteral t_lit1 = LiteralMother.createOrdinaryLiteral(exampleSchema, "t", "a", "b", "c");
                OrdinaryLiteral t_lit2 = LiteralMother.createOrdinaryLiteral(exampleSchema, "t", "a", "e", "c");
                ConjunctiveQuery query = QueryFactory.createConjunctiveQuery(List.of(), List.of(s_lit, t_lit1, t_lit2));
                AtomIndexSet indexSet = new AtomIndexSet(0, 1, 2);

                boolean isFactorizable = FactorizableAtomSetSelector.isFactorizable(tgd, query, indexSet);

                assertThat(isFactorizable).isFalse();
            }

            /**
             * Test case:
             * <ul>
             *     <li>TGD: s(x), r(x,y) -> t(x,y,z)</li>
             *     <li>Query: q2() <- t(a,b,c), t(a,c,c)</li>
             * </ul>
             */
            @Test
            void shouldReturnTrue_example3() {
                TGD tgd = exampleSchema.getAllTGDs().get(0);
                OrdinaryLiteral t_lit1 = LiteralMother.createOrdinaryLiteral(exampleSchema, "t", "a", "b", "c");
                OrdinaryLiteral t_lit2 = LiteralMother.createOrdinaryLiteral(exampleSchema, "t", "a", "c", "c");
                ConjunctiveQuery query = QueryFactory.createConjunctiveQuery(List.of(), List.of(t_lit1, t_lit2));
                AtomIndexSet indexSet = new AtomIndexSet(0, 1);

                boolean isFactorizable = FactorizableAtomSetSelector.isFactorizable(tgd, query, indexSet);

                assertThat(isFactorizable).isFalse();
            }

            /**
             * Test case:
             * <ul>
             *     <li>TGD: s(x), r(x,y) -> t(x,y,z)</li>
             *     <li>Query: qe1() <- s(a), t(a,b,c), t(a,e,c)</li>
             * </ul>
             */
            @Test
            void shouldReturnTrue_exampleExtra1() {
                TGD tgd = exampleSchema.getAllTGDs().get(0);
                OrdinaryLiteral s_lit = LiteralMother.createOrdinaryLiteral("s", "a");
                OrdinaryLiteral t_lit1 = LiteralMother.createOrdinaryLiteral(exampleSchema, "t", "a", "b", "c");
                OrdinaryLiteral t_lit2 = LiteralMother.createOrdinaryLiteral(exampleSchema, "t", "a", "e", "c");
                ConjunctiveQuery query = QueryFactory.createConjunctiveQuery(List.of(), List.of(s_lit, t_lit1, t_lit2));
                AtomIndexSet indexSet = new AtomIndexSet(1, 2);

                boolean isFactorizable = FactorizableAtomSetSelector.isFactorizable(tgd, query, indexSet);

                assertThat(isFactorizable).isTrue();
            }

            /**
             * Test case:
             * <ul>
             *     <li>TGD: s(x), r(x,y) -> t(x,y,z)</li>
             *     <li>Query: qe2() <- s(a), t(a,b,c), t(e,b,c)</li>
             * </ul>
             */
            @Test
            void shouldReturnTrue_exampleExtra2() {
                TGD tgd = exampleSchema.getAllTGDs().get(0);
                OrdinaryLiteral s_lit = LiteralMother.createOrdinaryLiteral("s", "a");
                OrdinaryLiteral t_lit1 = LiteralMother.createOrdinaryLiteral(exampleSchema, "t", "a", "b", "c");
                OrdinaryLiteral t_lit2 = LiteralMother.createOrdinaryLiteral(exampleSchema, "t", "e", "b", "c");
                ConjunctiveQuery query = QueryFactory.createConjunctiveQuery(List.of(), List.of(s_lit, t_lit1, t_lit2));
                AtomIndexSet indexSet = new AtomIndexSet(1, 2);

                boolean isFactorizable = FactorizableAtomSetSelector.isFactorizable(tgd, query, indexSet);

                assertThat(isFactorizable).isTrue();
            }
        }
    }

    @Nested
    class FactorizableAtomSetSelectionTests {

        @Nested
        class OnePossibleSetForSolutionTests {

            /**
             * Test case:
             * <ul>
             *     <li>TGD: p(x) -> q(x,y)</li>
             *     <li>Query: q(a,b), q(a,b)</li>
             * </ul>
             */
            @Test
            void shouldReturnSingleSet_whenFactorizableSetFound() {
                DependencySchema schema = new DependencySchemaParser().parse("""
                        p(x) -> q(x,y)
                        """);
                TGD tgd = schema.getAllTGDs().get(0);
                OrdinaryLiteral p_lit = LiteralMother.createOrdinaryLiteral(schema, "q", "a", "b");
                ConjunctiveQuery query = QueryFactory.createConjunctiveQuery(List.of(), List.of(p_lit, p_lit));

                Set<AtomIndexSet> factorizableSets = FactorizableAtomSetSelector.getFactorizableAtomSets(query, tgd);

                AtomIndexSet indexSet = new AtomIndexSet(0, 1);
                assertThat(factorizableSets)
                        .hasSize(1)
                        .first()
                        .isEqualTo(indexSet);
            }

            //TODO: add testing

        }

        @Nested
        class MultiplePossibleSetsForSolutionsTests {

            /**
             * Test case:
             * <ul>
             *     <li>TGD: p(x) -> q(x,y)</li>
             *     <li>Query: q(a,b), q(a,b), q(a,c), q(a,c)</li>
             * </ul>
             */
            @Test
            void shouldReturnSingleSet_whenFactorizableSetFound() {
                DependencySchema schema = new DependencySchemaParser().parse("""
                        p(x) -> q(x,y)
                        """);
                TGD tgd = schema.getAllTGDs().get(0);
                OrdinaryLiteral p_litWithB = LiteralMother.createOrdinaryLiteral(schema, "q", "a", "b");
                OrdinaryLiteral p_litWithC = LiteralMother.createOrdinaryLiteral(schema, "q", "a", "c");
                ConjunctiveQuery query = QueryFactory.createConjunctiveQuery(List.of(), List.of(p_litWithB, p_litWithB, p_litWithC, p_litWithC));

                Set<AtomIndexSet> factorizableSets = FactorizableAtomSetSelector.getFactorizableAtomSets(query, tgd);

                AtomIndexSet indexSet1 = new AtomIndexSet(0, 1);
                AtomIndexSet indexSet2 = new AtomIndexSet(2, 3);
                assertThat(factorizableSets)
                        .hasSize(2)
                        .satisfiesOnlyOnce(ais1 -> assertThat(ais1).isEqualTo(indexSet1))
                        .satisfiesOnlyOnce(ais2 -> assertThat(ais2).isEqualTo(indexSet2));
            }

            //TODO: add testing

        }
    }

}