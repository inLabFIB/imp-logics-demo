package edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.utils.selectors;

import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.domain.DependencySchema;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.domain.TGD;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.services.parser.DependencySchemaParser;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.ConjunctiveQuery;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.OrdinaryLiteral;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.QueryFactory;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.Variable;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.mothers.LiteralMother;
import edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.AtomIndexSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplicableAtomSetSelectorTest {

    @Test
    void shouldThrowException_whenCallingConstructorOfClass() {
        assertThatThrownBy(ApplicableAtomSetSelector::new).isInstanceOf(IllegalStateException.class);
    }

    @Nested
    class ApplicabilityTests {

        //TODO: add tests

        @Nested
        class PaperExamples {
            /**
             * Test case:
             * <ul>
             *     <li>TGD: s(x) -> r(x,y)</li>
             *     <li>Query: p(a) <- r(a,b), r(c,b), r(b,e)</li>
             * </ul>
             */
            @Test
            void shouldReturnTrue_example1() {
                DependencySchema schema = new DependencySchemaParser().parse("""
                        s(x) -> r(x,y)
                        """);
                TGD tgd = schema.getAllTGDs().get(0);
                OrdinaryLiteral r_lit1 = LiteralMother.createOrdinaryLiteral(schema, "r", "a", "b");
                OrdinaryLiteral r_lit2 = LiteralMother.createOrdinaryLiteral(schema, "r", "c", "b");
                OrdinaryLiteral r_lit3 = LiteralMother.createOrdinaryLiteral(schema, "r", "b", "e");
                ConjunctiveQuery query = QueryFactory.createConjunctiveQuery(List.of(new Variable("a")), List.of(r_lit1, r_lit2, r_lit3));

                boolean isApplicable = ApplicableAtomSetSelector.isApplicable(tgd, query, 0);
                assertThat(isApplicable).isFalse();

                isApplicable = ApplicableAtomSetSelector.isApplicable(tgd, query, 1);
                assertThat(isApplicable).isFalse();

                isApplicable = ApplicableAtomSetSelector.isApplicable(tgd, query, 2);
                assertThat(isApplicable).isTrue();
            }

        }
    }

    @Nested
    class ApplicableAtomSetSelectionTests {

        //TODO: add tests

        @Nested
        class PaperExamples {
            /**
             * Test case:
             * <ul>
             *     <li>TGD: s(x) -> r(x,y)</li>
             *     <li>Query: p(a) <- r(a,b), r(c,b), r(b,e)</li>
             * </ul>
             */
            @Test
            void shouldReturnTrue_example1() {
                DependencySchema schema = new DependencySchemaParser().parse("""
                        s(x) -> r(x,y)
                        """);
                TGD tgd = schema.getAllTGDs().get(0);
                OrdinaryLiteral r_lit1 = LiteralMother.createOrdinaryLiteral(schema, "r", "a", "b");
                OrdinaryLiteral r_lit2 = LiteralMother.createOrdinaryLiteral(schema, "r", "c", "b");
                OrdinaryLiteral r_lit3 = LiteralMother.createOrdinaryLiteral(schema, "r", "b", "e");
                ConjunctiveQuery query = QueryFactory.createConjunctiveQuery(List.of(new Variable("a")), List.of(r_lit1, r_lit2, r_lit3));

                Set<AtomIndexSet> result = ApplicableAtomSetSelector.getApplicableAtomSets(query, tgd);

                assertThat(result)
                        .hasSize(1)
                        .first()
                        .isEqualTo(new AtomIndexSet(2));

            }
        }
    }

}