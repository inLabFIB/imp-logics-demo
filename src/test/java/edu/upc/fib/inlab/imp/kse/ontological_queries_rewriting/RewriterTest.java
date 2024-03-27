package edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting;

import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.domain.DependencySchema;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.domain.TGD;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.services.parser.DependencySchemaParser;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.*;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.mothers.LiteralMother;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.mothers.TermMother;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.parser.QueryParser;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.printer.LogicSchemaPrinter;
import edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.utils.normalizers.TGDNormalizerProcess;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class RewriterTest {

    @Nested
    class StocksTest {

        private final DependencySchema stockSchema = new DependencySchemaParser().parse("""
                stock_portf(x,y,z) -> company(x,v,w)
                stock_portf(x,y,z) -> stock(y,v,w)
                list_comp(x,y) -> fin_idx(y,z,w)
                list_comp(x,y) -> stock(x,z,w)
                stock_portf(x,y,z) -> has_stock(y,x)
                has_stock(x,y) -> stock_portf(x,z,w)
                stock(x,y,z) -> stock_portf(v,x,w)
                stock(x,y,z) -> fin_ins(x)
                company(x,y,z) -> legal_person(x)
                %% legal_person(x,y,z), fin_ins(x,v,w) -> false
                """);

        /**
         * Test case:
         * <ul>
         *     <li>TGD: stockSchema</li>
         *     <li>Query: q(a,b,c) <- fin_ins(a), stock_portf(b,a,d), company(b,e,f), list_comp(a,c), fin_idx(c,g,h)</li>
         * </ul>
         */
        @Disabled("Works but too time-expensive")
        @Test
        void rewriteStockSchema() {
            Set<TGD> tgds = new HashSet<>(stockSchema.getAllTGDs());
            ConjunctiveQuery originalQuery = QueryFactory.createConjunctiveQuery(
                    TermMother.createTerms("a,b,c"),
                    List.of(
                            LiteralMother.createOrdinaryLiteral(stockSchema, "fin_ins", "a"),
                            LiteralMother.createOrdinaryLiteral(stockSchema, "stock_portf", "b", "a", "d"),
                            LiteralMother.createOrdinaryLiteral(stockSchema, "company", "b", "e", "f"),
                            LiteralMother.createOrdinaryLiteral(stockSchema, "list_comp", "a", "c"),
                            LiteralMother.createOrdinaryLiteral(stockSchema, "fin_idx", "c", "g", "h")
                    ));

            List<ConjunctiveQuery> rewrite = Rewriter.rewrite(originalQuery, tgds);

            System.out.println("Number of queries: " + rewrite.size());
            for (int i = 0; i < rewrite.size(); i++) System.out.println(getQueryAsString(i, rewrite.get(i)));

            assertThat(rewrite).hasSizeGreaterThan(1);
        }

        String getQueryAsString(int i, Query query) {
            LogicSchemaPrinter printer = new LogicSchemaPrinter();
            StringBuilder result = new StringBuilder("q" + i + "(");
            for (Term t : query.getHeadTerms()) {
                if (t instanceof Variable v) result.append(printer.visit(v));
                else result.append(printer.visit((Constant) t));
            }
            result.append(") <- ");
            result.append(printer.visit(query.getBody()));
            return result.toString();
        }

    }

    @Nested
    class IMPLogicsDemoTests {

        @Test
        void studentTeachersExample() {
            String dependencySchemaString = """
                    % If a student passes a subject, the student has some evaluation
                    HasPassed(student, subject) -> Exam(teacher, student, subject, data)
                                    
                    % If a teacher teaches a subject a student is coursing, the teacher evaluates the student
                    Teaches(teacher, subject), Studies(student, subject) -> Exam(teacher, student, subject, data)
                                    
                    % If a teacher is expert in a subject from a study plan, the teacher gives the subject
                    ExpertIn(teacher, subject), ComposesPlan(subject, studyPlan) -> Teaches(teacher, subject)
                                    
                    % A subject has, at most, one teacher
                    % Teaches(teacher1, subject), Teaches(teacher2, subject) -> teacher1=teacher2
                    """;
            DependencySchema dependencySchema = new DependencySchemaParser().parse(dependencySchemaString);
            Set<Predicate> dependencySchemaPredicates = dependencySchema.getAllPredicates();

            String queryString = """
                    % Ontological Query
                    (student) :- Exam(teacher, student, subject, data)
                    """;
            Query query = new QueryParser().parse(queryString, dependencySchemaPredicates).get(0);
            assertThat(query.isConjunctiveQuery()).isTrue();
            ConjunctiveQuery ontologicalQuery = (ConjunctiveQuery) query;

            Set<TGD> ontologyTGDs = new HashSet<>(dependencySchema.getAllTGDs());
            List<ConjunctiveQuery> rewriting = Rewriter.rewrite(ontologicalQuery, ontologyTGDs);

            assertThat(rewriting).hasSize(6);
        }

        @Test
        void studentTeachersExample_withExtraNormalization() {
            String dependencySchemaString = """
                    % If a student passes a subject, the student has some evaluation
                    HasPassed(student, subject) -> Exam(teacher, student, subject, data)
                                    
                    % If a teacher teaches a subject a student is coursing, the teacher evaluates the student
                    Teaches(teacher, subject), Studies(student, subject) -> Exam(teacher, student, subject, data)
                                    
                    % If a teacher is expert in a subject from a study plan, the teacher gives the subject
                    ExpertIn(teacher, subject), ComposesPlan(subject, studyPlan) -> Teaches(teacher, subject)
                                    
                    % A subject has, at most, one teacher
                    % Teaches(teacher1, subject), Teaches(teacher2, subject) -> teacher1=teacher2
                    """;
            DependencySchema dependencySchema = new DependencySchemaParser().parse(dependencySchemaString);

            DependencySchema normalizedDependencySchema = new TGDNormalizerProcess().normalize(dependencySchema);
            Set<Predicate> dependencySchemaPredicates = normalizedDependencySchema.getAllPredicates();

            String queryString = """
                    % Ontological Query
                    (student) :- Exam(teacher, student, subject, data)
                    """;
            Query query = new QueryParser().parse(queryString, dependencySchemaPredicates).get(0);
            assertThat(query.isConjunctiveQuery()).isTrue();
            ConjunctiveQuery ontologicalQuery = (ConjunctiveQuery) query;

            Set<TGD> ontologyTGDs = new HashSet<>(normalizedDependencySchema.getAllTGDs());
            List<ConjunctiveQuery> rewriting = Rewriter.rewrite(ontologicalQuery, ontologyTGDs);

            assertThat(rewriting).hasSize(6);
        }
    }
}
