/*
 * Copyright 2011 - 2016 Arne Limburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.jpasecurity.jpql.compiler;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.jpasecurity.util.Maps.entry;
import static org.jpasecurity.util.Maps.map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jpasecurity.Alias;
import org.jpasecurity.Path;
import org.jpasecurity.jpql.TypeDefinition;
import org.jpasecurity.model.MethodAccessTestBean;
import org.jpasecurity.model.MethodAccessTestBeanBuilder;
import org.jpasecurity.util.SetHashMap;
import org.jpasecurity.util.SetMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Arne Limburg
 */
public class ValueIteratorTest {

    private static final Alias BEAN1 = new Alias("bean1");
    private static final Alias BEAN2 = new Alias("bean2");
    private static final Alias PARENT1 = new Alias("parent1");
    private static final Alias PARENT2 = new Alias("parent2");
    private static final Alias CHILD1 = new Alias("child1");
    private static final Alias CHILD2 = new Alias("child2");
    private static final Alias GRANDCHILD2 = new Alias("grandchild2");

    private final MethodAccessTestBeanBuilder beanFactory = new MethodAccessTestBeanBuilder();
    private final PathEvaluatorFactory pathEvaluatorFactory = new PathEvaluatorFactory();
    private Set<TypeDefinition> typeDefinitions;
    private SetMap<Alias, Object> possibleValues;

    @Before
    public void initializeTypeDefinitions() {
        typeDefinitions = new HashSet<TypeDefinition>();
        typeDefinitions.add(new TypeDefinition(BEAN1, MethodAccessTestBean.class));
        typeDefinitions.add(new TypeDefinition(PARENT1, MethodAccessTestBean.class, new Path("bean1.parent"), true));
        typeDefinitions.add(new TypeDefinition(CHILD1, MethodAccessTestBean.class, new Path("bean1.children"), false));
        typeDefinitions.add(new TypeDefinition(BEAN2, MethodAccessTestBean.class));
        typeDefinitions.add(new TypeDefinition(PARENT2, MethodAccessTestBean.class, new Path("bean2.parent"), false));
        typeDefinitions.add(new TypeDefinition(CHILD2, MethodAccessTestBean.class, new Path("bean2.children"), true));
        possibleValues = new SetHashMap<Alias, Object>();
    }

    @Test
    public void emptyPossibleValues() {
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withNoBeans().create();
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void emptyPossibleValuesEntry() {
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withNoBeans().create();
        possibleValues.getNotNull(BEAN1); //creates empty entry
        assertTrue(possibleValues.containsKey(BEAN1));
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void emptyBean1() {
        MethodAccessTestBean bean2 = beanFactory.withName("bean2").withParent().withChildren().create();
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBean(bean2).create();
        possibleValues.getNotNull(BEAN1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        // still no such element, since no value for bean1
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void innerJoinOnParent() {
        MethodAccessTestBean bean1 = beanFactory.withName("bean1").withoutParent().withChildren().create();
        MethodAccessTestBean bean2 = beanFactory.withName("bean2").withParent().withChildren().create();
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1, bean2).create();
        possibleValues.add(BEAN1, bean1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        // still no such element, since bean1 will be removed by inner join to parent
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void innerJoinOnChildren() {
        MethodAccessTestBean bean1 = beanFactory.withName("bean1").withParent().withChildren().create();
        MethodAccessTestBean bean2 = beanFactory.withName("bean2").withParent().withoutChildren().create();
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1, bean2).create();
        possibleValues.add(BEAN1, bean1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        // still no such element, since bean2 will be removed by inner join to children
        assertNoSuchElementException(valueIterator);
    }

    /**
     * First call to next() initializes the iterator
     */
    @Test
    public void initialize() {
        MethodAccessTestBean bean1 = beanFactory.withName("bean1").withParent().withChild().create();
        MethodAccessTestBean bean2 = beanFactory.withName("bean2").withParent().withChild().create();
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1, bean2).create();
        possibleValues.add(BEAN1, bean1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParent()),
                       entry(CHILD1, (Object)bean1.getChildren().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParent()),
                       entry(CHILD2, (Object)bean2.getChildren().iterator().next())),
                   valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void initializeWithOuterJoinOnChildren() {
        MethodAccessTestBean bean1 = beanFactory.withName("bean1").withParent().withoutChildren().create();
        MethodAccessTestBean bean2 = beanFactory.withName("bean2").withParent().withChild().create();
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1, bean2).create();
        possibleValues.add(BEAN1, bean1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParent()),
                       entry(CHILD1, null),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParent()),
                       entry(CHILD2, (Object)bean2.getChildren().iterator().next())),
                   valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void initializeWithOuterJoinOnParent() {
        MethodAccessTestBean bean1 = beanFactory.withName("bean1").withParent().withChild().create();
        MethodAccessTestBean bean2 = beanFactory.withName("bean2").withoutParent().withChild().create();
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1, bean2).create();
        possibleValues.add(BEAN1, bean1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParent()),
                       entry(CHILD1, (Object)bean1.getChildren().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, null),
                       entry(CHILD2, (Object)bean2.getChildren().iterator().next())),
                   valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void initializeWithOuterJoinOnParentAndChildren() {
        MethodAccessTestBean bean1 = beanFactory.withName("bean1").withParent().withoutChildren().create();
        MethodAccessTestBean bean2 = beanFactory.withName("bean2").withoutParent().withChild().create();
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1, bean2).create();
        possibleValues.add(BEAN1, bean1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParent()),
                       entry(CHILD1, null),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, null),
                       entry(CHILD2, (Object)bean2.getChildren().iterator().next())),
                   valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void next() {
        MethodAccessTestBean bean1 = beanFactory.withName("bean1").withParent().withChildren().create();
        MethodAccessTestBean bean2 = beanFactory.withName("bean2").withParent().withChild().create();
        MethodAccessTestBean bean1Child1 = bean1.getChildren().get(0);
        MethodAccessTestBean bean1Child2 = bean1.getChildren().get(1);
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1, bean2).create();
        possibleValues.add(BEAN1, bean1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParent()),
                       entry(CHILD1, (Object)bean1Child1),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParent()),
                       entry(CHILD2, (Object)bean2.getChildren().iterator().next())),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParent()),
                       entry(CHILD1, (Object)bean1Child2),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParent()),
                       entry(CHILD2, (Object)bean2.getChildren().iterator().next())),
                    valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void nextWithInnerJoin() {
        MethodAccessTestBean bean1a = beanFactory.withName("bean1a").withParent().withChild().create();
        MethodAccessTestBean bean1b = beanFactory.withName("bean1b").withoutParent().withChild().create();
        MethodAccessTestBean bean2 = beanFactory.withName("bean2").withParent().withChild().create();
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1a, bean1b, bean2).create();
        possibleValues.add(BEAN1, bean1a);
        possibleValues.add(BEAN1, bean1b);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1a),
                       entry(PARENT1, (Object)bean1a.getParent()),
                       entry(CHILD1, (Object)bean1a.getChildren().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParent()),
                       entry(CHILD2, (Object)bean2.getChildren().iterator().next())),
                   valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void nextWithOuterJoinOnChildren() {
        MethodAccessTestBean bean1 = beanFactory.withName("bean1").withParent().withoutChildren().create();
        MethodAccessTestBean bean2 = beanFactory.withName("bean2").withParent().withChildren().create();
        MethodAccessTestBean bean2Child1 = bean2.getChildren().get(0);
        MethodAccessTestBean bean2Child2 = bean2.getChildren().get(1);
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1, bean2).create();
        possibleValues.add(BEAN1, bean1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParent()),
                       entry(CHILD1, null),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParent()),
                       entry(CHILD2, (Object)bean2Child1)),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParent()),
                       entry(CHILD1, null),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParent()),
                       entry(CHILD2, (Object)bean2Child2)),
                    valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void nextWithOuterJoinOnParent() {
        MethodAccessTestBean bean1 = beanFactory.withName("bean1").withParent().withChildren().create();
        MethodAccessTestBean bean2 = beanFactory.withName("bean2").withoutParent().withChild().create();
        MethodAccessTestBean bean1Child1 = bean1.getChildren().get(0);
        MethodAccessTestBean bean1Child2 = bean1.getChildren().get(1);
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1, bean2).create();
        possibleValues.add(BEAN1, bean1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParent()),
                       entry(CHILD1, (Object)bean1Child1),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, null),
                       entry(CHILD2, (Object)bean2.getChildren().iterator().next())),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParent()),
                       entry(CHILD1, (Object)bean1Child2),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, null),
                       entry(CHILD2, (Object)bean2.getChildren().iterator().next())),
                    valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void nextWithOuterJoinOnParentAndChildren() {
        MethodAccessTestBean bean1 = beanFactory.withName("bean1").withParent().withoutChildren().create();
        MethodAccessTestBean bean2 = beanFactory.withName("bean2").withoutParent().withChildren().create();
        MethodAccessTestBean bean2Child1 = bean2.getChildren().get(0);
        MethodAccessTestBean bean2Child2 = bean2.getChildren().get(1);
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1, bean2).create();
        possibleValues.add(BEAN1, bean1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParent()),
                       entry(CHILD1, null),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, null),
                       entry(CHILD2, (Object)bean2Child1)),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParent()),
                       entry(CHILD1, null),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, null),
                       entry(CHILD2, (Object)bean2Child2)),
                    valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void nextWithTwoPossibleValues() {
        MethodAccessTestBean bean1a = beanFactory.withName("bean1a").withParent().withChild().create();
        MethodAccessTestBean bean1b = beanFactory.withName("bean1b").withParent().withChild().create();
        MethodAccessTestBean bean2 = beanFactory.withName("bean2").withParent().withChild().create();
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1a, bean1b, bean2).create();
        possibleValues.add(BEAN1, bean1a);
        possibleValues.add(BEAN1, bean1b);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1a),
                       entry(PARENT1, (Object)bean1a.getParent()),
                       entry(CHILD1, (Object)bean1a.getChildren().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParent()),
                       entry(CHILD2, (Object)bean2.getChildren().iterator().next())),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1b),
                       entry(PARENT1, (Object)bean1b.getParent()),
                       entry(CHILD1, (Object)bean1b.getChildren().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParent()),
                       entry(CHILD2, (Object)bean2.getChildren().iterator().next())),
                   valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void nextWithTwoPossibleValuesAndChildren() {
        MethodAccessTestBean bean1a = beanFactory.withName("bean1a").withParent().withChild().create();
        MethodAccessTestBean bean1b = beanFactory.withName("bean1b").withParent().withChild().create();
        MethodAccessTestBean bean2 = beanFactory.withName("bean2").withParent().withChildren().create();
        MethodAccessTestBean bean2Child1 = bean2.getChildren().get(0);
        MethodAccessTestBean bean2Child2 = bean2.getChildren().get(1);
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1a, bean1b, bean2).create();
        possibleValues.add(BEAN1, bean1a);
        possibleValues.add(BEAN1, bean1b);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1a),
                       entry(PARENT1, (Object)bean1a.getParent()),
                       entry(CHILD1, (Object)bean1a.getChildren().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParent()),
                       entry(CHILD2, (Object)bean2Child1)),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1a),
                       entry(PARENT1, (Object)bean1a.getParent()),
                       entry(CHILD1, (Object)bean1a.getChildren().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParent()),
                       entry(CHILD2, (Object)bean2Child2)),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1b),
                       entry(PARENT1, (Object)bean1b.getParent()),
                       entry(CHILD1, (Object)bean1b.getChildren().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParent()),
                       entry(CHILD2, (Object)bean2Child1)),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1b),
                       entry(PARENT1, (Object)bean1b.getParent()),
                       entry(CHILD1, (Object)bean1b.getChildren().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParent()),
                       entry(CHILD2, (Object)bean2Child2)),
                   valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void nextWithChildrenAndTwoPossibleValues() {
        MethodAccessTestBean bean1a = beanFactory.withName("bean1a").withParent().withChildren().create();
        MethodAccessTestBean bean1b = beanFactory.withName("bean1b").withParent().withChild().create();
        MethodAccessTestBean bean2 = beanFactory.withName("bean2").withParent().withChild().create();
        MethodAccessTestBean bean1aChild1 = bean1a.getChildren().get(0);
        MethodAccessTestBean bean1aChild2 = bean1a.getChildren().get(1);
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1a, bean1b, bean2).create();
        possibleValues.add(BEAN1, bean1a);
        possibleValues.add(BEAN1, bean1b);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1a),
                       entry(PARENT1, (Object)bean1a.getParent()),
                       entry(CHILD1, (Object)bean1aChild1),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParent()),
                       entry(CHILD2, (Object)bean2.getChildren().iterator().next())),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1a),
                       entry(PARENT1, (Object)bean1a.getParent()),
                       entry(CHILD1, (Object)bean1aChild2),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParent()),
                       entry(CHILD2, (Object)bean2.getChildren().iterator().next())),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1b),
                       entry(PARENT1, (Object)bean1b.getParent()),
                       entry(CHILD1, (Object)bean1b.getChildren().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParent()),
                       entry(CHILD2, (Object)bean2.getChildren().iterator().next())),
                   valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void nextWithFourPossibleValues() {
        MethodAccessTestBean bean1a = beanFactory.withName("bean1a").withParent().withChild().create();
        MethodAccessTestBean bean1b = beanFactory.withName("bean1b").withParent().withChild().create();
        MethodAccessTestBean bean2a = beanFactory.withName("bean2a").withParent().withChild().create();
        MethodAccessTestBean bean2b = beanFactory.withName("bean2b").withParent().withChild().create();
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1a, bean1b, bean2a, bean2b).create();
        possibleValues.add(BEAN1, bean1a);
        possibleValues.add(BEAN1, bean1b);
        possibleValues.add(BEAN2, bean2a);
        possibleValues.add(BEAN2, bean2b);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1a),
                       entry(PARENT1, (Object)bean1a.getParent()),
                       entry(CHILD1, (Object)bean1a.getChildren().iterator().next()),
                       entry(BEAN2, (Object)bean2a),
                       entry(PARENT2, (Object)bean2a.getParent()),
                       entry(CHILD2, (Object)bean2a.getChildren().iterator().next())),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1b),
                       entry(PARENT1, (Object)bean1b.getParent()),
                       entry(CHILD1, (Object)bean1b.getChildren().iterator().next()),
                       entry(BEAN2, (Object)bean2a),
                       entry(PARENT2, (Object)bean2a.getParent()),
                       entry(CHILD2, (Object)bean2a.getChildren().iterator().next())),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1a),
                       entry(PARENT1, (Object)bean1a.getParent()),
                       entry(CHILD1, (Object)bean1a.getChildren().iterator().next()),
                       entry(BEAN2, (Object)bean2b),
                       entry(PARENT2, (Object)bean2b.getParent()),
                       entry(CHILD2, (Object)bean2b.getChildren().iterator().next())),
                    valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1b),
                       entry(PARENT1, (Object)bean1b.getParent()),
                       entry(CHILD1, (Object)bean1b.getChildren().iterator().next()),
                       entry(BEAN2, (Object)bean2b),
                       entry(PARENT2, (Object)bean2b.getParent()),
                       entry(CHILD2, (Object)bean2b.getChildren().iterator().next())),
                   valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void nextWithDoubleJoin() {
        typeDefinitions.add(new TypeDefinition(GRANDCHILD2,
                                               MethodAccessTestBean.class,
                                               new Path("child2.children"),
                                               false));
        MethodAccessTestBean bean1 = beanFactory.withName("bean1").withParent().withChild().create();
        MethodAccessTestBean bean2 = beanFactory.withName("bean2").withParent().withChildren().create();
        MethodAccessTestBean child2a = bean2.getChildren().get(0);
        MethodAccessTestBean child2b = bean2.getChildren().get(1);
        MethodAccessTestBean grandchild2a
            = beanFactory.withName("grandchild2a").withoutParent().withoutChildren().create();
        MethodAccessTestBean grandchild2b
            = beanFactory.withName("grandchild2b").withoutParent().withoutChildren().create();
        child2b.getChildren().add(grandchild2a);
        child2b.getChildren().add(grandchild2b);
        grandchild2a.setParent(child2b);
        grandchild2b.setParent(child2b);
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1, bean2, child2a, child2b).create();
        possibleValues.add(BEAN1, bean1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParent()),
                       entry(CHILD1, (Object)bean1.getChildren().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParent()),
                       entry(CHILD2, (Object)child2a),
                       entry(GRANDCHILD2, null)),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParent()),
                       entry(CHILD1, (Object)bean1.getChildren().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParent()),
                       entry(CHILD2, (Object)child2b),
                       entry(GRANDCHILD2, (Object)grandchild2a)),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParent()),
                       entry(CHILD1, (Object)bean1.getChildren().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParent()),
                       entry(CHILD2, (Object)child2b),
                       entry(GRANDCHILD2, (Object)grandchild2b)),
                    valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    private void assertNext(Map<Alias, Object> values, Iterator<Map<Alias, Object>> i) {
        assertTrue(i.hasNext());
        Map<Alias, Object> result = i.next();
        for (Map.Entry<Alias, Object> entry: values.entrySet()) {
            assertEquals(entry.getValue(), result.get(entry.getKey()));
        }
    }

    private void assertNoSuchElementException(Iterator<?> i) {
        assertFalse(i.hasNext());
        try {
            i.next();
            fail();
        } catch (NoSuchElementException e) {
            //expected
        }
    }

    private class PathEvaluatorFactory {

        private MethodAccessTestBean[] beans;

        public PathEvaluatorFactory withBeans(MethodAccessTestBean... beans) {
            this.beans = beans;
            return this;
        }

        public PathEvaluatorFactory withNoBeans() {
            return withBeans();
        }

        public PathEvaluatorFactory withBean(MethodAccessTestBean bean) {
            return withBeans(bean);
        }

        public PathEvaluator create() {
            PathEvaluator pathEvaluator = mock(PathEvaluator.class);
            for (MethodAccessTestBean bean: beans) {
                when(pathEvaluator.evaluateAll(eq(Collections.singleton(bean)), eq("parent")))
                    .thenAnswer(new ParentAnswer(bean));
                when(pathEvaluator.evaluateAll(eq(Collections.singleton(bean)), eq("children")))
                    .thenAnswer(new ChildrenAnswer(bean));
            }
            return pathEvaluator;
        }
    }

    private class ParentAnswer implements Answer<List<Object>> {

        private MethodAccessTestBean bean;

        ParentAnswer(MethodAccessTestBean bean) {
            this.bean = bean;
        }

        public List<Object> answer(InvocationOnMock invocation) throws Throwable {
            return bean.getParent() == null
                   ? Collections.emptyList()
                   : Collections.<Object>singletonList(bean.getParent());
        }
    }

    private class ChildrenAnswer implements Answer<List<Object>> {

        private MethodAccessTestBean bean;

        ChildrenAnswer(MethodAccessTestBean bean) {
            this.bean = bean;
        }

        public List<Object> answer(InvocationOnMock invocation) throws Throwable {
            return bean.getChildren() == null || bean.getChildren().isEmpty()
                   ? Collections.emptyList()
                   : new ArrayList<Object>(bean.getChildren());
        }
    }
}
