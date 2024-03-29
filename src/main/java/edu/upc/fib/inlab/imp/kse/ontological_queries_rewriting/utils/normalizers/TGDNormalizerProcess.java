package edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting.utils.normalizers;

import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.domain.DependencySchema;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.domain.TGD;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.services.creation.DependencySchemaBuilder;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.services.creation.spec.helpers.DependencySchemaToSpecHelper;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.services.processes.DependencyProcessPipeline;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.services.processes.SingleExistentialVarTGDTransformer;
import edu.upc.fib.inlab.imp.kse.logics.dependencyschema.services.processes.SingleHeadTGDTransformer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class normalizes a TGD to a set of TGDs in normal form with one head atom and one existentially quantified variable.
 * The approach to obtain such normalized TGDs is based on "Ontological Queries: Rewriting and Optimization (Extended Version)"
 * by Gottlob et al.
 */
public class TGDNormalizerProcess {

    /**
     * @deprecated Use {@link TGDNormalizerProcess#normalize(DependencySchema)} instead
     * @param originalTGDs not null, might be empty
     * @return a new set of normalized TGDs, with new predicate references.
     */
    @Deprecated
    public Set<TGD> normalize(Set<TGD> originalTGDs) {
        DependencySchemaBuilder builder = new DependencySchemaBuilder();
        for (TGD originalTGD : originalTGDs) {
            builder.addDependency(DependencySchemaToSpecHelper.buildTGDSpec(originalTGD));
        }
        return new HashSet<>(normalize(builder.build()).getAllTGDs());
    }

    /**
     * @param schema not null, might be empty
     * @return a new dependencySchema where all TGDs have been normalized
     */
    public DependencySchema normalize(DependencySchema schema) {
        DependencyProcessPipeline pipeline = new DependencyProcessPipeline(List.of(
                new SingleHeadTGDTransformer(),
                new SingleExistentialVarTGDTransformer()));
        return pipeline.execute(schema);
    }
}
