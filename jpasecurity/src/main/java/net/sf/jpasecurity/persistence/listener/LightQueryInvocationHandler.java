package net.sf.jpasecurity.persistence.listener;

import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import net.sf.jpasecurity.jpql.compiler.PathEvaluator;
import net.sf.jpasecurity.mapping.TypeDefinition;
import net.sf.jpasecurity.util.ProxyInvocationHandler;

public class LightQueryInvocationHandler  extends ProxyInvocationHandler<Query> {

    private List<String> selectedPaths;
    private Set<TypeDefinition> types;
    private PathEvaluator pathEvaluator;

    public LightQueryInvocationHandler(
                                  Query query,
                                  List<String> selectedPaths,
                                  Set<TypeDefinition> types,
                                  PathEvaluator pathEvaluator) {
        super(query);
        this.selectedPaths = selectedPaths;
        this.types = types;
        this.pathEvaluator = pathEvaluator;
    }
}
