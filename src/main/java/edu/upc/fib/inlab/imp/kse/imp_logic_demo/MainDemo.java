package edu.upc.fib.inlab.imp.kse.imp_logic_demo;

import edu.upc.fib.inlab.imp.kse.imp_logic_demo.utils.LogFormatter;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.domain.DependencySchema;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.domain.EGD;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.domain.TGD;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.services.analyzers.DatalogPlusMinusAnalyzer;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.services.analyzers.egds.NonConflictingEGDsAnalyzer;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.services.parser.DependencySchemaParser;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.services.printer.DependencySchemaPrinter;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.services.processes.DependencyProcessPipeline;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.services.processes.SingleExistentialVarTGDTransformer;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.services.processes.SingleHeadTGDTransformer;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.assertions.QueryAssert;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.*;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.parser.LogicSchemaWithIDsParser;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.parser.QueryParser;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.printer.LogicSchemaPrinter;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.printer.QueryPrinter;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.processes.EqualityReplacer;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.processes.LogicProcessPipeline;
import edu.upc.fib.inlab.imp.kse.logics.logicschema.services.processes.SchemaUnfolder;
import edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.OBDAMapping;
import edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.Rewriter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Logger;


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

    private static void print(String content) {
        System.out.print(content);
    }

    private static void println(String content) {
        System.out.println(content);
    }

    private static void printWithHeader(String logicConstraintUsedVariables, String content) {
        System.out.print("\u001B[1m" + logicConstraintUsedVariables + ":\033[0m \n");
        System.out.println(content);
    }

    private static void printWithHeaderInline(String logicConstraintUsedVariables, String content) {
        System.out.print("\u001B[1m" + logicConstraintUsedVariables + ":\033[0m ");
        System.out.println(content);
    }

    private static void printHeaderInline(String logicConstraintUsedVariables) {
        System.out.print("\u001B[1m" + logicConstraintUsedVariables + ":\033[0m ");
    }


    public static void main(String[] args) {
        print("DEMO START");

        /* ---------------------------------------------------------------------------------------------------- */

        print("\n### PART 1: LogicSchema, the Datalog metamodel");
        print("\n#### Parsing a LogicSchema");
        print("\nWe start by parsing a logic schema regarding some university.");

        Set<Predicate> predicates = Set.of(
                new Predicate("DB_AcademicRecord", 3),
                new Predicate("DB_Student", 2),
                new Predicate("DB_Studies", 2),
                new Predicate("DB_Subject", 1),
                new Predicate("DB_AssistantTeacher", 2),
                new Predicate("DB_TenuredTeacher", 2),
                new Predicate("DB_Teaches", 2),
                new Predicate("DB_ComposesPlan", 2),
                new Predicate("DB_StudiesPlan", 1),
                new Predicate("DB_PublishesAbout", 3)
        );

        print("""
                Assume that we want to define:
                - 3 constraints (written as denial constraints -aka negative constraints-)
                - Some derived predicates.
                              
                The easiest way is by parsing them.
                """);
        print("\nLet's first define the constraints and derivation rules as Strings:");

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

        print("\nNow we can parse them into a LogicSchema object.\n");

        LogicSchemaWithIDsParser logicSchemaParser = new LogicSchemaWithIDsParser();
        LogicSchema logicSchema = logicSchemaParser.parse(logicSchemaString, predicates);

        print("\nWe can print the schema:\n");

        LogicSchemaPrinter logicSchemaPrinter = new LogicSchemaPrinter();
        printWithHeader("Logic Schema", logicSchemaPrinter.print(logicSchema));

        print("\nWhat else can we do?\n");

        /* ---------------------------------------------------------------------------------------------------- */
        print("\n#### LogicSchema navigation\n");
        print("""
                To show the navigation capabilities, we will pick the logic constraint `@AcademicRecordFKToStudent`.
                From there, we will start visiting its literals, variables, predicates, etc.
                """);

        LogicConstraint selectedConstraint = logicSchema.getLogicConstraintByID(new ConstraintID("AcademicRecordFKToStudent"));
        printWithHeaderInline("Selected Logic Constraint", logicSchemaPrinter.visit(selectedConstraint));

        print("\nWe can check the used variables in the constraint body\n");

        Set<Variable> usedVariables = selectedConstraint.getBody().getUsedVariables();
        printHeaderInline("Used Variables in Body");
        for (Variable v : usedVariables) print(v.getName() + " ");

        print("\nWe can also navigate to its literals, predicates, and derivation rules it depends on.\n");
        print("\nWe can select a literal of the constraint, and check its positive/negative polarity, whether it is ground or not, or if it is base or derived.\n");

        OrdinaryLiteral olit = (OrdinaryLiteral) selectedConstraint.getBody().get(1);
        printWithHeaderInline("Selected Ordinary Literal", olit.toString());

        println("Ordinary Literal is negative: " + olit.isNegative());
        println("Ordinary Literal is ground: " + olit.isGround());
        println("Ordinary Literal is base: " + olit.isBase());

        print("\nThe predicate of an ordinary literal can be obtained, and we can check whether it is base or derived.\n");
        Predicate olitPredicate = olit.getPredicate();

        print("\nFrom a derived predicate we can access its definition rules.\n");
        List<DerivationRule> derivationRules = olitPredicate.getDerivationRules();
        printHeaderInline("Predicate's derivation rules:");
        for (DerivationRule dr : derivationRules) print(logicSchemaPrinter.visit(dr));

        /* ---------------------------------------------------------------------------------------------------- */
        print("\n#### LogicSchema operations\n ");
        print("""
                We refer as operations to those methods already available in the main metamodel classes.
                For instance, given an atom we can unfold it:
                """);

        Predicate isStudentPredicate = logicSchema.getPredicateByName("IsStudent");
        Atom johnAtom = new Atom(isStudentPredicate, List.of(new Constant("John")));
        printWithHeaderInline("Original atom", johnAtom.toString());
        printWithHeaderInline("Derivation rules it has", johnAtom.getPredicate().getDerivationRules().toString());
        printWithHeaderInline("Atom after unfolding", johnAtom.unfold().toString());

        print("\nThe unfold is also available for list of literals, and it takes care of avoiding variable name clashing:\n");
        OrdinaryLiteral johnStudent = new OrdinaryLiteral(johnAtom);
        OrdinaryLiteral maryStudent = new OrdinaryLiteral(new Atom(isStudentPredicate, List.of(new Constant("Mary"))));
        ImmutableLiteralsList literalsList = new ImmutableLiteralsList(List.of(johnStudent, maryStudent));
        printWithHeaderInline("Original literalsList", literalsList.toString());
        printWithHeaderInline("Unfolding the second literal", literalsList.unfold(1).toString());
        printWithHeaderInline("Unfolding both literals", literalsList.unfold(1).get(0).unfold(0).toString());
        print("\nDo note that the unfolding has avoided a variable name clash with age.\n ");

        /* ---------------------------------------------------------------------------------------------------- */
        print("\n#### LogicSchema services\n");
        print("""
                We refer as services to those operations that are not inside the main class diagram.

                Just for example, we will show some transformation services. Transformation services receives as input a logic schema and outputs a new logic schema
                after applying some transformation into it. Such processes can be executed in a pipeline.

                For our demo, we will use the `EqualityReplacer`, `SchemaUnfolder` processes.
                """);

        LogicProcessPipeline pipeline = new LogicProcessPipeline(List.of(
                new EqualityReplacer(),
                new SchemaUnfolder(false)
        ));
        LogicSchema modifiedLogicSchema = pipeline.execute(logicSchema);
        printWithHeader("Modified schema", logicSchemaPrinter.print(modifiedLogicSchema));

        /* ---------------------------------------------------------------------------------------------------- */
        print("\n### PART 2: DependencySchema, the Datalog+/- metamodel");
        print("""
                #### Parsing a DependencySchema
                We will now parse an ontology over the same university domain.
                """);

        String dependencySchemaString = """
                % If a student passes a subject, the student has some evaluation
                HasPassed(student, subject) -> Exam(teacher, student, subject, data)
                                
                % If a teacher teaches a subject a student is coursing, the teacher evaluates the student
                Teaches(teacher, subject), Studies(student, subject) -> Exam(teacher, student, subject, data)
                                
                % If a teacher is expert in a subject from a study plan, the teacher gives the subject
                ExpertIn(teacher, subject), ComposesPlan(subject, studyPlan) -> Teaches(teacher, subject)
                                
                % A subject has, at most, one exam per day
                Exam(teacher, student, subject, data), Exam(teacher2, student2, subject, data) -> teacher = teacher2
                Exam(teacher, student, subject, data), Exam(teacher2, student2, subject, data) -> student = student2
                """;

        DependencySchemaParser dependencySchemaParser = new DependencySchemaParser();
        DependencySchema dependencySchema = dependencySchemaParser.parse(dependencySchemaString);
        print("\nWe can, for instance, print the dependency schema\n");
        DependencySchemaPrinter dependencySchemaPrinter = new DependencySchemaPrinter();
        printWithHeader("Dependency Schema", dependencySchemaPrinter.print(dependencySchema));

        print("\nLet's see what else can we do\n");

        /* ---------------------------------------------------------------------------------------------------- */
        print("\n#### DependencySchema navigation\n");
        print("\nWe can pick the TGDs and EGDs of the schema, and similarly as before, navigate through the metamodel.\n");
        TGD tgd = dependencySchema.getAllTGDs().get(0);
        printWithHeaderInline("Selected TGD", tgd.toString());

        EGD egd = dependencySchema.getAllEGDs().get(0);
        printWithHeaderInline("Selected EGD", egd.toString());

        EqualityComparisonBuiltInLiteral equality = egd.getHead();
        printWithHeaderInline("Selected equality", equality.toString());

        /* ---------------------------------------------------------------------------------------------------- */
        print("\n#### DependencySchema operations\n");
        print("\nFor instance, we can check whether the previous TGD is linear, or guarded.\n");
        println("TGD is linear: " + tgd.isLinear());
        println("TGD is guarded: " + tgd.isGuarded());

        /* ---------------------------------------------------------------------------------------------------- */
        print("\n#### DependencySchema services\n");
        print("\nAmong other services, we can check whether the EGDs are conflicting with the TGDs:\n");

        NonConflictingEGDsAnalyzer nonConflictingEGDsAnalyzer = new NonConflictingEGDsAnalyzer();
        boolean separable = nonConflictingEGDsAnalyzer.areEGDsNonConflictingWithTGDs(dependencySchema);
        println("EGDs of schema are non conflicting / separable: " + separable);

        print("\nWe will now analyze which Datalog+/- languages this dependency schema satisfies\n");
        DatalogPlusMinusAnalyzer analyzer = new DatalogPlusMinusAnalyzer();
        Set<DatalogPlusMinusAnalyzer.DatalogPlusMinusLanguage> languages = analyzer.getDatalogPlusMinusLanguages(dependencySchema);
        printHeaderInline("This dependency schema is: ");
        for (DatalogPlusMinusAnalyzer.DatalogPlusMinusLanguage dl : languages) print(dl.name() + " ");
        print("\nThere are also some transformation services, but we will take them a look on the thirt part of the demonstration.\n");

        /* ---------------------------------------------------------------------------------------------------- */
        print("\n### PART 3: Using IMP-Logics for OBDA\n");
        print("""
                We will show how IMP-Logics can be used to implement OBDA concepts such as an ontology query-rewritting.
                We will assume that:
                - Our logicSchema is a relational database
                - Our dependencySchema is an ontology defined on top of the previous database
                                
                We start by "normalizing" the dependencySchema. That is, we need to obtain a new dependencySchema where each TGD head has at most one atom with at most one existential variable.
                                
                We can easily implement such normalization by concatenating two DependencySchema services from IMP-Logics
                """);

        DependencyProcessPipeline dependencyProcessPipeline = new DependencyProcessPipeline(List.of(
                new SingleHeadTGDTransformer(),                      //provided by IMP-Logics
                new SingleExistentialVarTGDTransformer()));          //provided by IMP-Logics
        DependencySchema normalizedDependencySchema = dependencyProcessPipeline.execute(dependencySchema);

        printWithHeader("Normalized Dependency Schema", dependencySchemaPrinter.print(normalizedDependencySchema));
        print("\nWe now define some mappings from the predicates of the dependencySchema (the ontology) to the predicates of the logicSchema (the database). To do so, we reuse the Query class of IMP-Logics, and define our new class OBDAMapping.\n");

        String mappingDBQueriesString = """
                % HasPassed(student, subject)
                (student, subject) :- DB_AcademicRecord(student, subject, mark), mark > 5
                    
                % Exam(teacher, student, subject, data)
                (teacher, student, subject, data) :- DB_Exam(teacher, student, subject, data)
                    
                % Teaches(teacher, subject)
                (teacher, subject) :- DB_Teaches(teacher, subject)
                    
                % Studies(student, subject)
                (student, subject) :- DB_Studies(student, subject)
                    
                % ExpertIn(teacher, subject)
                (teacher, subject) :- DB_PublishesAbout(teacher, paper, subject), DB_PublishesAbout(teacher, paper2, subject), paper<>paper2
                    
                % ComposesPlan(subject, studyPlan)
                (subject, studyPlan) :- DB_ComposesPlan(subject, studyPlan)
                """;

        QueryParser queryParser = new QueryParser();                    //Provided by IMP-Logics
        List<Query> mappingDBQueries = queryParser.parse(mappingDBQueriesString, predicates);
        OBDAMapping mapping = new OBDAMapping.OBDAMappingBuilder()
                .addMapping(normalizedDependencySchema.getPredicateByName("HasPassed"), mappingDBQueries.get(0))
                .addMapping(normalizedDependencySchema.getPredicateByName("Exam"), mappingDBQueries.get(1))
                .addMapping(normalizedDependencySchema.getPredicateByName("Teaches"), mappingDBQueries.get(2))
                .addMapping(normalizedDependencySchema.getPredicateByName("Studies"), mappingDBQueries.get(3))
                .addMapping(normalizedDependencySchema.getPredicateByName("ExpertIn"), mappingDBQueries.get(4))
                .addMapping(normalizedDependencySchema.getPredicateByName("ComposesPlan"), mappingDBQueries.get(5))
                .build();

        print("\nWe now define a Conjunctive Query over the ontology\n");
        String queryString = """
                % Ontological Query
                (student) :- Exam(teacher, student, subject, data)
                """;
        ConjunctiveQuery ontologicalQuery = (ConjunctiveQuery) queryParser.parse(queryString, normalizedDependencySchema.getAllPredicates()).get(0); //Provided by IMP-Logics

        /* ---------------------------------------------------------------------------------------------------- */
        print("\n#### Rewriting the query\n");
        print("\nWe have defined a new class Rewriter, using the metamodel of DependencySchema, that applies a FO-rewritting algorithm.");

        Set<TGD> ontologyTGDs = new HashSet<>(normalizedDependencySchema.getAllTGDs());
        List<ConjunctiveQuery> rewriting = Rewriter.rewrite(ontologicalQuery, ontologyTGDs);
        print("\nWe can now print the query");
        QueryPrinter queryPrinter = new QueryPrinter();
        for (int i = 0; i < rewriting.size(); i++) {
            Query queryToPrint = rewriting.get(i);
            printWithHeader("Query " + i, queryPrinter.print(queryToPrint));
        }
        /* ---------------------------------------------------------------------------------------------------- */
        print("\n#### Rewriting the query over the database\n");
        print("\nTo finish the implementation of the query-rewritting, we need to translate the queries in terms of the database tables.\n");
        List<Query> finalRewriting = rewriting.stream()
                .map(mapping::translateToDBQueries)
                .flatMap(Collection::stream)
                .toList();
        print("\nWe can now print the query\n");
        for (int i = 0; i < finalRewriting.size(); i++) {
            Query queryToPrint = finalRewriting.get(i);
            printWithHeader("Query " + i, queryPrinter.print(queryToPrint));
        }
        /* ---------------------------------------------------------------------------------------------------- */
        print("\n#### We can use IMP-Logics asserts to check its validity\n");
        print("""
                IMP-Logics is not only useful for developing the code, but also for checking its validity.
                To validate the developed code, IMP-Logics also offers several testing facilities, such as the definition of several asserts.
                
                In this example, we can check whether the 2nd query obtained query is isomorphic (i.e., the same up to variable-renaming) to an expected one:
                 """);

        QueryAssert.assertThat(finalRewriting.get(2))
                .isIsomorphicTo(List.of("st"), " DB_PublishesAbout(t, p, s), DB_PublishesAbout(t, p2, s), p<>p2, DB_ComposesPlan(s, sP), DB_Studies(st, s)");
        print("\nDo note that the check fails if the actual query is not isomorphic to the expectation (here we change 'st' to 's' to make them non-isomorphic):\n");
        QueryAssert.assertThat(finalRewriting.get(2))
                .isIsomorphicTo(List.of("s"), " DB_PublishesAbout(t, p, s), DB_PublishesAbout(t, p2, s), p<>p2, DB_ComposesPlan(s, sP), DB_Studies(s, s)");

        print("\nDEMO END");
    }
}