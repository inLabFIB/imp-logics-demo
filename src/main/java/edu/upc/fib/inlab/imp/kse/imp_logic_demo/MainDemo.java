package edu.upc.fib.inlab.imp.kse.imp_logic_demo;

import edu.upc.fib.inlab.imp.kse.imp_logic_demo.utils.LogFormatter;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.domain.DependencySchema;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.domain.TGD;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.services.analyzers.DatalogPlusMinusAnalyzer;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.services.analyzers.egds.NonConflictingEGDsAnalyzer;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.services.parser.DependencySchemaParser;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.services.printer.DependencySchemaPrinter;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.assertions.LogicSchemaAssertions;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.*;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.parser.LogicSchemaWithIDsParser;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.parser.QueryParser;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.printer.LogicSchemaPrinter;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.printer.QueryPrinter;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.processes.EqualityReplacer;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.processes.LogicProcessPipeline;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.processes.SchemaUnfolder;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.processes.SingleDerivationRuleTransformer;
import edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.OBDAMapping;
import edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.Rewriter;
import edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.utils.normalizers.TGDNormalizerProcess;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

public class MainDemo {

    private static final Logger LOGGER;

    static {
        LOGGER = Logger.getLogger(MainDemo.class.getName());
        LOGGER.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        Formatter formatter = new LogFormatter();
        handler.setFormatter(formatter);
        LOGGER.addHandler(handler);
    }

    private static void printWithHeader(String logicConstraintUsedVariables, String content) {
        LOGGER.info(logicConstraintUsedVariables + ": ");
        LOGGER.info(content);
    }


    public static void main(String[] args) {
        LOGGER.info("DEMO START");

        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("\n -- LOADING LOGIC SCHEMA UTILS & TOOLS -- ");
        LogicSchemaWithIDsParser logicSchemaParser = new LogicSchemaWithIDsParser();
        LogicSchemaPrinter logicSchemaPrinter = new LogicSchemaPrinter();
        QueryParser queryParser = new QueryParser();
        QueryPrinter queryPrinter = new QueryPrinter();

        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("\n -- PARSING & PRINTING LOGIC SCHEMA (CONTAINS LOGIC CONSTRAINTS AND MAPPINGS) -- ");
        String logicSchemaString = """
                %% Schema Logic Constraints
                % AcademicRecord reference key to Student
                @AcademicRecordFKToStudent :- DB_AcademicRecord(studentName, subject, eval), not(IsStudent(studentName))
                IsStudent(studentName) :- DB_Student(studentName, age)
                              
                % Teacher must be over 18
                @TeachersMustBeOver18 :- Teacher_view(name, age), age < 18
                Teacher_view(name, age) :- DB_AssistantTeacher(name, age)
                Teacher_view(name, age) :- DB_TenuredTeacher(name, age)
                                
                % A teacher cannot teach himself
                @TeacherCannotTeachHimself :- DB_Teaches(teacherName, subject), DB_Studies(studentName, subject), teacherName=studentName
                """;
        Set<Predicate> extraPredicates = Set.of(
                new Predicate("DB_ComposesPlan", 2),
                new Predicate("DB_PublishesAbout", 3)
        );
        LogicSchema logicSchema = logicSchemaParser.parse(logicSchemaString, extraPredicates);
        Set<Predicate> logicSchemaPredicates = logicSchema.getAllPredicates();

        printWithHeader("Logic Schema", logicSchemaPrinter.print(logicSchema));
        LOGGER.info("\nPredicates:");
        for (Predicate p : logicSchemaPredicates) LOGGER.info(logicSchemaPrinter.visit(p));

        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("\n -- LOGIC SCHEMA OBJECTS MANIPULATION -- ");
        LOGGER.info("We will focus on constraint @AcademicRecordFKToStudent");
        LogicConstraint selectedConstraint = logicSchema.getLogicConstraintByID(new ConstraintID("AcademicRecordFKToStudent"));
        printWithHeader("Selected Logic Constraint", logicSchemaPrinter.visit(selectedConstraint));

        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("\nWe can check the used variables in the constraint body");
        Set<Variable> usedVariables = selectedConstraint.getBody().getUsedVariables();
        LOGGER.info("Used Variables in Body: ");
        for (Variable v : usedVariables) LOGGER.info(logicSchemaPrinter.visit(v));

        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("\nWe can select a literal of the constraint");
        OrdinaryLiteral olit = (OrdinaryLiteral) selectedConstraint.getBody().get(1);
        printWithHeader("Selected Ordinary Literal", logicSchemaPrinter.visit(olit));

        LOGGER.info("Ordinary Literal is negative: " + olit.isNegative());
        assertThat(olit.isNegative()).isTrue();

        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("\nThe predicate of an ordinary literal can be obtained");
        Predicate olitPredicate = olit.getPredicate();
        printWithHeader("Obtained Predicate", logicSchemaPrinter.visit(olitPredicate));
        LOGGER.info("Predicate Literal is base: " + olitPredicate.isBase());
        assertThat(olitPredicate.isBase()).isFalse();
        LOGGER.info("Predicate Literal is derived: " + olitPredicate.isDerived());
        assertThat(olitPredicate.isDerived()).isTrue();

        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("\nFrom a derived predicate we can access it's definition rules");
        List<DerivationRule> derivationRules = olitPredicate.getDerivationRules();
        LOGGER.info("Predicate's derivation rules:");
        for (DerivationRule dr : derivationRules) LOGGER.info(logicSchemaPrinter.visit(dr));

        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("\n -- LOGIC SCHEMA PROPERTY CHECKING -- ");
        LOGGER.info("\nOther schema property checks can be performed");
        LOGGER.info("Selected Constraint(" + selectedConstraint.getID() + ") is safe: " + selectedConstraint.isSafe());
        assertThat(selectedConstraint.isSafe()).isTrue();
        LOGGER.info("Selected Ordinary Literal(" + logicSchemaPrinter.visit(olit) + ") is ground: " + olit.isGround());
        assertThat(olit.isGround()).isFalse();

        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("\n -- LOGIC SCHEMA TRANSFORMATIONS -- ");
        LOGGER.info("Now we will apply a transformation pipeline over the original schema");
        LOGGER.info("precisely, applying the EqualityReplacer, SchemaUnfolder & SingleDerivationRuleTransformer processes");
        LogicProcessPipeline pipeline = new LogicProcessPipeline(List.of(
                new EqualityReplacer(),
                new SchemaUnfolder(false),
                new SingleDerivationRuleTransformer()
        ));
        LogicSchema modifiedLogicSchema = pipeline.execute(logicSchema);
        printWithHeader("Modified schema", logicSchemaPrinter.print(modifiedLogicSchema));

        /* ---------------------------------------------------------------------------------------------------- */
        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("\n -- LOADING DEPENDENCY SCHEMA UTILS & TOOLS -- ");
        DependencySchemaParser dependencySchemaParser = new DependencySchemaParser();
        DependencySchemaPrinter dependencySchemaPrinter = new DependencySchemaPrinter();

        LOGGER.info("\n -- PARSING & PRINTING DEPENDENCY SCHEMA -- ");
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
        DependencySchema dependencySchema = dependencySchemaParser.parse(dependencySchemaString);
        Set<Predicate> dependencySchemaPredicates = logicSchema.getAllPredicates();

        printWithHeader("Dependency Schema", dependencySchemaPrinter.print(dependencySchema));
        LOGGER.info("\nPredicates:");
        for (Predicate p : dependencySchemaPredicates) LOGGER.info(logicSchemaPrinter.visit(p));

        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("\n -- DEPENDENCY PROPERTY CHECKING -- ");
        TGD tgd = dependencySchema.getAllTGDs().get(0);
        printWithHeader("Selected TGD", dependencySchemaPrinter.visit(tgd));

        LOGGER.info("TGD is linear: " + tgd.isLinear());
        LOGGER.info("TGD is guarded: " + tgd.isGuarded());
        assertThat(tgd.isLinear()).isTrue();
        assertThat(tgd.isGuarded()).isTrue();

        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("\n -- DEPENDENCY SCHEMA PROPERTY CHECKING -- ");
        LOGGER.info("First, we will check that no EGD is conflicting with the TGDs in the dependency schema");
        NonConflictingEGDsAnalyzer nonConflictingEGDsAnalyzer = new NonConflictingEGDsAnalyzer();
        boolean separable = nonConflictingEGDsAnalyzer.areEGDsNonConflictingWithTGDs(dependencySchema);
        LOGGER.info("EGDs of schema are non conflicting / separable: " + separable);
        assertThat(separable).isTrue();

        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("\nWe will now analyze which Datalog+/- languages this dependency schema satisfies");
        DatalogPlusMinusAnalyzer analyzer = new DatalogPlusMinusAnalyzer();
        Set<DatalogPlusMinusAnalyzer.DatalogPlusMinusLanguage> languages = analyzer.getDatalogPlusMinusLanguages(dependencySchema);
        LOGGER.info("This dependency schema is: ");
        for (DatalogPlusMinusAnalyzer.DatalogPlusMinusLanguage dl : languages) LOGGER.info(dl.name());

        /* ---------------------------------------------------------------------------------------------------- */
        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("\n -- REWRITING PROCESS -- ");
        LOGGER.info("Now let's perform a rewriting of an ontological query.");
        LOGGER.info("The loaded Logic Schema describes de relational database with with its Logic Constraints and Predicates.");
        LOGGER.info("The loaded Dependency Schema describes de ontology with its TGDs and ontological predicates.");
        LOGGER.info("Now, we will parse and load the mapping which links the ontology and relational database as well as the to be rewritten ontological query.");

        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("The TGDs need to be normalized before the rewriting process. We normalize the dependencySchema.");
        DependencySchema normalizedDependencySchema = new TGDNormalizerProcess().normalize(dependencySchema);
        Set<Predicate> normalizedDependencySchemaPredicates = normalizedDependencySchema.getAllPredicates();

        printWithHeader("Normalized Dependency Schema", dependencySchemaPrinter.print(normalizedDependencySchema));
        LOGGER.info("\nPredicates:");
        for (Predicate p : normalizedDependencySchemaPredicates) LOGGER.info(logicSchemaPrinter.visit(p));

        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("The loaded Logic Schema will provide the mappings between the ontology and the relational tables");
        String mappingDBQueriesString = """
                %HasPassed(student, subject)
                (student, subject) :- DB_AcademicRecord(student, subject, mark), mark > 5
                                
                %Exam(teacher, student, subject, data)
                (teacher, student, subject, data) :- DB_Exam(teacher, student, subject, data)
                                
                %Teaches(teacher, subject)
                (teacher, subject) :- DB_Teaches(teacher, subject)
                                
                %Studies(student, subject)
                (student, subject) :- DB_Studies(student, subject)
                                
                %ExpertIn(teacher, subject)
                (teacher, subject) :- DB_PublishesAbout(teacher, paper, subject), DB_PublishesAbout(teacher, paper2, subject), paper<>paper2
                                
                %ComposesPlan(subject, studyPlan)
                (subject, studyPlan) :- DB_ComposesPlan(subject, studyPlan)
                """;
        List<Query> mappingDBQueries = queryParser.parse(mappingDBQueriesString, logicSchemaPredicates);
        OBDAMapping mapping = new OBDAMapping.OBDAMappingBuilder()
                .addMapping(normalizedDependencySchema.getPredicateByName("HasPassed"), mappingDBQueries.get(0))
                .addMapping(normalizedDependencySchema.getPredicateByName("Exam"), mappingDBQueries.get(1))
                .addMapping(normalizedDependencySchema.getPredicateByName("Teaches"), mappingDBQueries.get(2))
                .addMapping(normalizedDependencySchema.getPredicateByName("Studies"), mappingDBQueries.get(3))
                .addMapping(normalizedDependencySchema.getPredicateByName("ExpertIn"), mappingDBQueries.get(4))
                .addMapping(normalizedDependencySchema.getPredicateByName("ComposesPlan"), mappingDBQueries.get(5))
                .build();

        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("Let's parse the ontological query:");
        String queryString = """
                % Ontological Query
                (student) :- Exam(teacher, student, subject, data)
                """;
        Query query = queryParser.parse(queryString, normalizedDependencySchemaPredicates).get(0);
        assertThat(query.isConjunctiveQuery()).isTrue();
        ConjunctiveQuery ontologicalQuery = (ConjunctiveQuery) query;

        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("\n -- APPLYING REWRITING PROCEDURE -- \n");
        Set<TGD> ontologyTGDs = new HashSet<>(normalizedDependencySchema.getAllTGDs());
        List<ConjunctiveQuery> rewriting = Rewriter.rewrite(ontologicalQuery, ontologyTGDs);

        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("\n -- PRINTING PARTIAL REWRITING -- \n");
        for (int i = 0; i < rewriting.size(); i++) {
            Query queryToPrint = rewriting.get(i);
            printWithHeader("Query " + i, queryPrinter.print(queryToPrint));
        }

        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("\n -- APPLYING MAPPINGS -- \n");
        List<Query> finalRewriting = rewriting.stream()
                .map(mapping::translateToDBQueries)
                .flatMap(Collection::stream)
                .toList();

        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("\n -- PRINTING REWRITING -- \n");
        for (int i = 0; i < finalRewriting.size(); i++) {
            Query queryToPrint = finalRewriting.get(i);
            printWithHeader("Query " + i, queryPrinter.print(queryToPrint));
        }

        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("\n -- ASSERT REWRITING IS EXPECTED -- \n");
        String expectedRewritingString = """
                % Expected Rewriting
                (student) :- DB_Exam(teacher, student, subject, data)
                (student) :- DB_AcademicRecord(student, subject, mark), mark > 5
                (student) :- DB_Teaches(teacher, subject), DB_Studies(student, subject)
                (student) :- DB_PublishesAbout(teacher, paper, subject), DB_PublishesAbout(teacher, paper2, subject), paper<>paper2, DB_ComposesPlan(subject, studyPlan), DB_Studies(student, subject)
                """;
        List<Query> expectedRewriting = queryParser.parse(expectedRewritingString, normalizedDependencySchemaPredicates);
        assertThat(finalRewriting)
                .satisfiesOnlyOnce(q -> LogicSchemaAssertions.assertThat(q).isIsomorphicTo(expectedRewriting.get(0)))
                .satisfiesOnlyOnce(q -> LogicSchemaAssertions.assertThat(q).isIsomorphicTo(expectedRewriting.get(1)))
                .satisfiesOnlyOnce(q -> LogicSchemaAssertions.assertThat(q).isIsomorphicTo(expectedRewriting.get(2)))
                .satisfiesOnlyOnce(q -> LogicSchemaAssertions.assertThat(q).isIsomorphicTo(expectedRewriting.get(3)));

        /* ---------------------------------------------------------------------------------------------------- */

        LOGGER.info("\n DEMO END");
    }
}