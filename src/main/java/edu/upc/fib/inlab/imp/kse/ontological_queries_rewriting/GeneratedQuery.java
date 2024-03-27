package edu.upc.fib.inlab.imp.kse.ontological_queries_rewriting;


import edu.upc.fib.inlab.imp.kse.logics.logicschema.domain.ConjunctiveQuery;

//TODO: second and third parameter should be set correctly
public final class GeneratedQuery {

    private final ConjunctiveQuery query;
    private final QueryOrigin origin;
    private Boolean explored;

    public GeneratedQuery(ConjunctiveQuery query, QueryOrigin origin, Boolean explored) {
        this.query = query;
        this.origin = origin;
        this.explored = explored;
    }

    public ConjunctiveQuery getQuery() {
        return query;
    }

    public QueryOrigin getOrigin() {
        return origin;
    }

    public Boolean isExplored() {
        return explored;
    }

    public void setExplored() {
        this.explored = true;
    }

}
