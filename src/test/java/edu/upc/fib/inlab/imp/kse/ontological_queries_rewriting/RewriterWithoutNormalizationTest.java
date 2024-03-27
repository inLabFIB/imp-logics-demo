package edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting;

import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.domain.DependencySchema;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.domain.TGD;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.services.parser.DependencySchemaParser;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.assertions.ImmutableLiteralsListAssert;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.ConjunctiveQuery;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.OrdinaryLiteral;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.QueryFactory;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.Variable;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.mothers.ImmutableLiteralsListMother;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.mothers.LiteralMother;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.mothers.QueryMother;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * All tests should consider TGDs already normalized
 */
class RewriterWithoutNormalizationTest {

    @Nested
    class RewritingTests {

        @Nested
        class PropertyTests {

            //TODO: parametrise test
            @Test
            void shouldAlwaysReturnInputQuery_inResultQuerySet() {
                ConjunctiveQuery inputQuery = QueryMother.createBooleanConjunctiveQuery("t()");

                String ontologySchemaString = """
                        p(x) -> q(x,y)
                        """;
                DependencySchema dependencySchema = new DependencySchemaParser().parse(ontologySchemaString);
                Set<TGD> inputTGDs = new HashSet<>(dependencySchema.getAllTGDs());

                List<ConjunctiveQuery> perfectRewriting = Rewriter.rewrite(inputQuery, inputTGDs);

                assertThat(perfectRewriting).contains(inputQuery);
            }
        }

        @Nested
        class SimpleCasesTests {

            @Nested
            class OnlyOneTGDTest {

                /**
                 * Test case:
                 * <ul>
                 *     <li>TGD: r(x) -> s(x,y)</li>
                 *     <li>Query: q() <- s(a,b)</li>
                 * </ul>
                 * Expected Rewriting Result:
                 * <ul>
                 *     <li>q() <- s(a,b)</li>
                 *     <li>q() <- r(a)</li>
                 * </ul>
                 */
                @Test
                void shouldApplyARewritingStep_ifPossible() {
                    DependencySchema schema = new DependencySchemaParser().parse("""
                            r(x) -> s(x,y)
                            """);
                    Set<TGD> tgds = new HashSet<>(schema.getAllTGDs());
                    OrdinaryLiteral s_lit = LiteralMother.createOrdinaryLiteral(schema, "s", "a", "b");
                    ConjunctiveQuery originalQuery = QueryFactory.createConjunctiveQuery(List.of(), List.of(s_lit));

                    List<ConjunctiveQuery> rewrite = Rewriter.rewrite(originalQuery, tgds);

                    assertThat(rewrite)
                            .hasSize(2)
                            .satisfiesOnlyOnce(cq -> assertThat(cq).isEqualTo(originalQuery))
                            .satisfiesOnlyOnce(cq -> ImmutableLiteralsListAssert.assertThat(cq.getBody()).containsExactlyLiteralsOf(List.of("r(a)")));
                }

                /**
                 * Test case:
                 * <ul>
                 *     <li>TGD: r(x) -> s(x,y)</li>
                 *     <li>Query: q() <- s(a,b), s(a,b)</li>
                 * </ul>
                 * Expected Rewriting Result:
                 * <ul>
                 *     <li>q() <- s(a,b)</li>
                 *     <li>q() <- r(a)</li>
                 * </ul>
                 */
                @Test
                void shouldApplyARewritingStep_afterANeededFactorizationStep() {
                    DependencySchema schema = new DependencySchemaParser().parse("""
                            r(x) -> s(x,y)
                            """);
                    Set<TGD> tgds = new HashSet<>(schema.getAllTGDs());
                    OrdinaryLiteral s_lit1 = LiteralMother.createOrdinaryLiteral(schema, "s", "a", "b");
                    OrdinaryLiteral s_lit2 = LiteralMother.createOrdinaryLiteral(schema, "s", "a", "b");
                    ConjunctiveQuery originalQuery = QueryFactory.createConjunctiveQuery(List.of(), List.of(s_lit1, s_lit2));

                    List<ConjunctiveQuery> rewrite = Rewriter.rewrite(originalQuery, tgds);

                    assertThat(rewrite)
                            .hasSize(2)
                            .satisfiesOnlyOnce(cq -> assertThat(cq).isEqualTo(originalQuery))
                            .satisfiesOnlyOnce(cq -> ImmutableLiteralsListAssert.assertThat(cq.getBody()).containsExactlyLiteralsOf(List.of("r(a)")));
                }

                /**
                 * Test case:
                 * <ul>
                 *     <li>TGD: r(x) -> s(x,y)</li>
                 *     <li>Query: q() <- s(a,b), s(a,b)</li>
                 * </ul>
                 * Expected Rewriting Result:
                 * <ul>
                 *     <li>q() <- s(a,b)</li>
                 *     <li>q() <- r(a)</li>
                 * </ul>
                 */
                @Disabled("This currently fails. Before fixing it we should think if this cases should be considered.")
                @Test
                void shouldApplyARewritingStep_afterANeededFactorizationStep_withIdenticalLiteralsInAQuery() {
                    DependencySchema schema = new DependencySchemaParser().parse("""
                            r(x) -> s(x,y)
                            """);
                    Set<TGD> tgds = new HashSet<>(schema.getAllTGDs());
                    OrdinaryLiteral s_lit1 = LiteralMother.createOrdinaryLiteral(schema, "s", "a", "b");
                    OrdinaryLiteral s_lit2 = LiteralMother.createOrdinaryLiteral(schema, "s", "a", "b");
                    ConjunctiveQuery originalQuery = QueryFactory.createConjunctiveQuery(List.of(), List.of(s_lit1, s_lit2));

                    List<ConjunctiveQuery> rewrite = Rewriter.rewrite(originalQuery, tgds);

                    assertThat(rewrite)
                            .hasSize(2)
                            .satisfiesOnlyOnce(cq -> assertThat(cq).isEqualTo(originalQuery))
                            .satisfiesOnlyOnce(cq -> ImmutableLiteralsListAssert.assertThat(cq.getBody()).containsExactlyLiteralsOf(List.of("r(a)")));
                }

                /**
                 * Test case:
                 * <ul>
                 *     <li>TGD: r(x) -> s(x,y)</li>
                 *     <li>Query: q() <- s(a,b), s(a,b), s(a,b), s(a,b)</li>
                 * </ul>
                 * Expected Rewriting Result:
                 * <ul>
                 *     <li>q() <- s(a,b)</li>
                 *     <li>q() <- r(a)</li>
                 * </ul>
                 */
                @Test
                void shouldApplyARewritingStep_afterANeededFactorizationStep_overMultipleAtoms() {
                    DependencySchema schema = new DependencySchemaParser().parse("""
                            r(x) -> s(x,y)
                            """);
                    Set<TGD> tgds = new HashSet<>(schema.getAllTGDs());
                    OrdinaryLiteral s_lit1 = LiteralMother.createOrdinaryLiteral(schema, "s", "a", "b");
                    OrdinaryLiteral s_lit2 = LiteralMother.createOrdinaryLiteral(schema, "s", "a", "b");
                    OrdinaryLiteral s_lit3 = LiteralMother.createOrdinaryLiteral(schema, "s", "a", "b");
                    OrdinaryLiteral s_lit4 = LiteralMother.createOrdinaryLiteral(schema, "s", "a", "b");
                    ConjunctiveQuery originalQuery = QueryFactory.createConjunctiveQuery(List.of(), List.of(s_lit1, s_lit2, s_lit3, s_lit4));

                    List<ConjunctiveQuery> rewrite = Rewriter.rewrite(originalQuery, tgds);

                    assertThat(rewrite)
                            .hasSize(2)
                            .satisfiesOnlyOnce(cq -> assertThat(cq).isEqualTo(originalQuery))
                            .satisfiesOnlyOnce(cq -> ImmutableLiteralsListAssert.assertThat(cq.getBody()).containsExactlyLiteralsOf(List.of("r(a)")));
                }
            }

            @Nested
            class MultipleTGDsTests {

                /**
                 * Test case:
                 * <ul>
                 *     <li>TGD: student(name) -> person(name, age) | employee(name) -> person(name, age)</li>
                 *     <li>Query: q() <- person(name, age)</li>
                 * </ul>
                 * Expected Rewriting Result:
                 * <ul>
                 *     <li>q() <- person(name, age)</li>
                 *     <li>q() <- student(name)</li>
                 *     <li>q() <- employee(name)</li>
                 * </ul>
                 */
                @Test
                void shouldApplyARewritingStep_ifPossible() {
                    DependencySchema schema = new DependencySchemaParser().parse("""
                            student(name) -> person(name, age)
                            employee(name) -> person(name, age)
                            """);
                    Set<TGD> tgds = new HashSet<>(schema.getAllTGDs());
                    OrdinaryLiteral s_lit = LiteralMother.createOrdinaryLiteral(schema, "person", "name", "age");
                    ConjunctiveQuery originalQuery = QueryFactory.createConjunctiveQuery(List.of(), List.of(s_lit));

                    List<ConjunctiveQuery> rewrite = Rewriter.rewrite(originalQuery, tgds);

                    assertThat(rewrite)
                            .hasSize(3)
                            .satisfiesOnlyOnce(cq -> assertThat(cq).isEqualTo(originalQuery))
                            .satisfiesOnlyOnce(cq -> ImmutableLiteralsListAssert.assertThat(cq.getBody()).containsExactlyLiteralsOf(List.of("student(name)")))
                            .satisfiesOnlyOnce(cq -> ImmutableLiteralsListAssert.assertThat(cq.getBody()).containsExactlyLiteralsOf(List.of("employee(name)")));
                }
            }
        }


        //TODO: ADD TESTS

        @Nested
        class PaperExamples {

            /**
             * Test case:
             * <ul>
             *     <li>TGD: r(x) -> s(x,y)</li>
             *     <li>Query: p() <- s(a,b), s(c,b), s(c,d), t(a,c)</li>
             * </ul>
             */
            @Test
            void shouldReturnTrue_example1() {
                DependencySchema schema = new DependencySchemaParser().parse("""
                        r(x) -> s(x,y)
                        """);
                Set<TGD> tgds = new HashSet<>(schema.getAllTGDs());
                OrdinaryLiteral s_lit1 = LiteralMother.createOrdinaryLiteral(schema, "s", "a", "b");
                OrdinaryLiteral s_lit2 = LiteralMother.createOrdinaryLiteral(schema, "s", "c", "b");
                OrdinaryLiteral s_lit3 = LiteralMother.createOrdinaryLiteral(schema, "s", "c", "d");
                OrdinaryLiteral t_lit = LiteralMother.createOrdinaryLiteral("t", "a", "c");
                ConjunctiveQuery originalQuery = QueryFactory.createConjunctiveQuery(List.of(), List.of(s_lit1, s_lit2, s_lit3, t_lit));

                List<ConjunctiveQuery> rewrite = Rewriter.rewrite(originalQuery, tgds);

                assertThat(rewrite)
                        .hasSizeGreaterThan(1)
                        .satisfiesOnlyOnce(cq -> assertThat(cq).isEqualTo(originalQuery));
                //TODO: asserts
            }

            /**
             * Test case:
             * <ul>
             *     <li>TGD: project(x), inArea(x,y) -> hasCollaborator(z,y,x)</li>
             *     <li>Query: p() <- hasCollaborator(a, "db", b)</li>
             * </ul>
             */
            @Test
            void shouldRewrite_example2_1() {
                DependencySchema schema = new DependencySchemaParser().parse("""
                        project(x), inArea(x,y) -> hasCollaborator(z,y,x)
                        """);
                Set<TGD> tgds = new HashSet<>(schema.getAllTGDs());
                OrdinaryLiteral lit = LiteralMother.createOrdinaryLiteral(schema, "hasCollaborator", "a", "\"db\"", "b");
                ConjunctiveQuery originalQuery = QueryFactory.createConjunctiveQuery(List.of(), List.of(lit));

                List<ConjunctiveQuery> rewrite = Rewriter.rewrite(originalQuery, tgds);

                assertThat(rewrite)
                        .hasSize(2)
                        .satisfiesOnlyOnce(cq -> assertThat(cq).isEqualTo(originalQuery))
                        .satisfiesOnlyOnce(cq -> ImmutableLiteralsListAssert.assertThat(cq.getBody()).containsExactlyLiteralsOf(List.of("project(b)", "inArea(b,\"db\")")));
            }

            /**
             * Test case:
             * <ul>
             *     <li>TGD: project(x), inArea(x,y) -> hasCollaborator(z,y,x)</li>
             *     <li>Query: p() <- hasCollaborator("c", "db", b)</li>
             * </ul>
             */
            @Test
            void shouldNotRewrite_example2_2() {
                DependencySchema schema = new DependencySchemaParser().parse("""
                        project(x), inArea(x,y) -> hasCollaborator(z,y,x)
                        """);
                Set<TGD> tgds = new HashSet<>(schema.getAllTGDs());
                OrdinaryLiteral lit = LiteralMother.createOrdinaryLiteral(schema, "hasCollaborator", "\"c\"", "\"db\"", "b");
                ConjunctiveQuery originalQuery = QueryFactory.createConjunctiveQuery(List.of(), List.of(lit));

                List<ConjunctiveQuery> rewrite = Rewriter.rewrite(originalQuery, tgds);

                assertThat(rewrite)
                        .hasSize(1)
                        .first()
                        .isEqualTo(originalQuery);
            }

            /**
             * Test case:
             * <ul>
             *     <li>TGD: project(x), inArea(x,y) -> hasCollaborator(z,y,x)</li>
             *     <li>Query: p() <- hasCollaborator(b, "db", b)</li>
             * </ul>
             */
            @Test
            void shouldNotRewrite_example2_3() {
                DependencySchema schema = new DependencySchemaParser().parse("""
                        project(x), inArea(x,y) -> hasCollaborator(z,y,x)
                        """);
                Set<TGD> tgds = new HashSet<>(schema.getAllTGDs());
                OrdinaryLiteral lit = LiteralMother.createOrdinaryLiteral(schema, "hasCollaborator", "b", "\"db\"", "b");
                ConjunctiveQuery originalQuery = QueryFactory.createConjunctiveQuery(List.of(), List.of(lit));

                List<ConjunctiveQuery> rewrite = Rewriter.rewrite(originalQuery, tgds);

                assertThat(rewrite)
                        .hasSize(1)
                        .first()
                        .isEqualTo(originalQuery);
            }

            /**
             * Test case:
             * <ul>
             *     <li>TGD: project(x), inArea(x,y) -> hasCollaborator(z,y,x) | hasCollaborator(x,y,z) -> collaborator(x)</li>
             *     <li>Query: p(b,c) <- hasCollaborator(a,b,c), collaborator(a)</li>
             * </ul>
             */
            @Test
            void shouldRewriteAndFactorize_example2_4() {
                DependencySchema schema = new DependencySchemaParser().parse("""
                        project(x), inArea(x,y) -> hasCollaborator(z,y,x)
                        hasCollaborator(x,y,z) -> collaborator(x)
                        """);
                Set<TGD> tgds = new HashSet<>(schema.getAllTGDs());
                OrdinaryLiteral hc_lit = LiteralMother.createOrdinaryLiteral(schema, "hasCollaborator", "a", "b", "c");
                OrdinaryLiteral c_lit = LiteralMother.createOrdinaryLiteral(schema, "collaborator", "a");
                ConjunctiveQuery originalQuery = QueryFactory.createConjunctiveQuery(
                        List.of(new Variable("b"), new Variable("c")), List.of(hc_lit, c_lit));

                List<ConjunctiveQuery> rewrite = Rewriter.rewrite(originalQuery, tgds);

                assertThat(rewrite)
                        .hasSize(3)
                        .satisfiesOnlyOnce(cq -> assertThat(cq).isEqualTo(originalQuery))
                        .satisfiesOnlyOnce(cq -> {
                            ImmutableLiteralsListAssert.assertThat(cq.getBody())
                                    .isLogicallyEquivalentTo(ImmutableLiteralsListMother.create("hasCollaborator(a,b,c), hasCollaborator(a,x,y)"));
                        })
                        .satisfiesOnlyOnce(cq -> {
                            ImmutableLiteralsListAssert.assertThat(cq.getBody())
                                    .isLogicallyEquivalentTo(ImmutableLiteralsListMother.create("project(x), inArea(x,y)"));
                        });
            }

        }
    }

    //TODO: add tests

}