package edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting;

import edu.upc.fib.inlab.imp.kse.logics.logicschema.assertions.QueryAssert;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.ConjunctiveQuery;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.Predicate;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.Query;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.mothers.QueryMother;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OBDAMappingTest {

    @Nested
    class TranslateToDBQueries {

        @Test
        void should_obtain1DBQuery_whenCQHas1AtomWith1Rule() {
            OBDAMapping obdaMapping = getOBDAMapping();

            ConjunctiveQuery query = QueryMother.createConjunctiveQuery(List.of("x", "y"), "P(x,y)", obdaMapping.getOntologicalPredicates());
            List<Query> dbQueries = obdaMapping.translateToDBQueries(query);

            assertThat(dbQueries)
                    .hasSize(1)
                    .anySatisfy(q -> QueryAssert.assertThat(q).isIsomorphicTo(List.of("x", "y"), "T_P(x,y)"));
        }

        @Test
        void should_obtain2DBQueries_whenCQHas1AtomWith2Rule() {
            OBDAMapping obdaMapping = getOBDAMapping();

            ConjunctiveQuery query = QueryMother.createConjunctiveQuery(List.of("x"), "Q(x)", obdaMapping.getOntologicalPredicates());
            List<Query> dbQueries = obdaMapping.translateToDBQueries(query);

            assertThat(dbQueries)
                    .hasSize(2)
                    .anySatisfy(q -> QueryAssert.assertThat(q).isIsomorphicTo(List.of("x"), "T_Q1(x)"))
                    .anySatisfy(q -> QueryAssert.assertThat(q).isIsomorphicTo(List.of("x"), "T_Q2(x)"));
        }

        @Test
        void should_obtainSeveralDBQueries_whenCQHasSeveralAtomsWithSeveralRules() {
            OBDAMapping obdaMapping = getOBDAMapping();

            ConjunctiveQuery query = QueryMother.createConjunctiveQuery(List.of("x"), "P(x,y), Q(x), R(y,y)", obdaMapping.getOntologicalPredicates());
            List<Query> dbQueries = obdaMapping.translateToDBQueries(query);

            assertThat(dbQueries)
                    .hasSize(2)
                    .anySatisfy(q -> QueryAssert.assertThat(q).isIsomorphicTo(List.of("x"), "T_P(x,y), T_Q1(x), T_R1(y,y), T_R2(y,y)"))
                    .anySatisfy(q -> QueryAssert.assertThat(q).isIsomorphicTo(List.of("x"), "T_P(x,y), T_Q2(x), T_R1(y,y), T_R2(y,y)"));
        }

        private OBDAMapping getOBDAMapping() {
            /*
             P(x, y) :- T_P(x, y)
             Q(x) :- T_Q1(x)
             Q(x) :- T_Q2(x)
             R(x, y) :- T_R1(x, y), T_R2(x,y)
             */
            Predicate ontoP = new Predicate("P", 2);
            Predicate ontoQ = new Predicate("Q", 1);
            Predicate ontoR = new Predicate("R", 2);

            Predicate dbP = new Predicate("T_P", 2);
            Predicate dbQ1 = new Predicate("T_Q1", 1);
            Predicate dbQ2 = new Predicate("T_Q2", 1);
            Predicate dbR1 = new Predicate("T_R1", 2);
            Predicate dbR2 = new Predicate("T_R2", 2);

            Set<Predicate> dbPredicates = Set.of(dbP, dbQ1, dbQ2, dbR1, dbR2);

            OBDAMapping.OBDAMappingBuilder obdaMappingBuilder = new OBDAMapping.OBDAMappingBuilder();

            Query pMapping = QueryMother.createConjunctiveQuery(List.of("x", "y"), "T_P(x,y)",
                    dbPredicates);
            obdaMappingBuilder.addMapping(ontoP, pMapping);

            Query qMapping1 = QueryMother.createConjunctiveQuery(List.of("x"), "T_Q1(x)", dbPredicates);
            Query qMapping2 = QueryMother.createConjunctiveQuery(List.of("x"), "T_Q2(x)", dbPredicates);
            obdaMappingBuilder.addMapping(ontoQ, qMapping1)
                    .addMapping(ontoQ, qMapping2);

            Query rMapping = QueryMother.createConjunctiveQuery(List.of("x", "y"), "T_R1(x,y),T_R2(x,y)", dbPredicates);
            obdaMappingBuilder.addMapping(ontoR, rMapping);

            return obdaMappingBuilder.build();
        }
    }
}