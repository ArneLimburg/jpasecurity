/*
 * Copyright 2011 Arne Limburg
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
package net.sf.jpasecurity.jpql.compiler;

import static net.sf.jpasecurity.util.Maps.entry;
import static net.sf.jpasecurity.util.Maps.map;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
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

import net.sf.jpasecurity.mapping.Alias;
import net.sf.jpasecurity.mapping.TypeDefinition;
import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.model.FieldAccessAnnotationTestBeanFactory;
import net.sf.jpasecurity.util.SetHashMap;
import net.sf.jpasecurity.util.SetMap;

import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;


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

    private final FieldAccessAnnotationTestBeanFactory beanFactory = new FieldAccessAnnotationTestBeanFactory();
    private final PathEvaluatorFactory pathEvaluatorFactory = new PathEvaluatorFactory();
    private Set<TypeDefinition> typeDefinitions;
    private SetMap<Alias, Object> possibleValues;

    @Before
    public void initializeTypeDefinitions() {
        typeDefinitions = new HashSet<TypeDefinition>();
        typeDefinitions.add(new TypeDefinition(BEAN1, FieldAccessAnnotationTestBean.class));
        typeDefinitions.add(new TypeDefinition(PARENT1, FieldAccessAnnotationTestBean.class, "bean1.parent", true));
        typeDefinitions.add(new TypeDefinition(CHILD1, FieldAccessAnnotationTestBean.class, "bean1.children", false));
        typeDefinitions.add(new TypeDefinition(BEAN2, FieldAccessAnnotationTestBean.class));
        typeDefinitions.add(new TypeDefinition(PARENT2, FieldAccessAnnotationTestBean.class, "bean2.parent", false));
        typeDefinitions.add(new TypeDefinition(CHILD2, FieldAccessAnnotationTestBean.class, "bean2.children", true));
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
        FieldAccessAnnotationTestBean bean2 = beanFactory.withName("bean2").withParent().withChildren().create();
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBean(bean2).create();
        possibleValues.getNotNull(BEAN1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        // still no such element, since no value for bean1
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void innerJoinOnParent() {
        FieldAccessAnnotationTestBean bean1 = beanFactory.withName("bean1").withoutParent().withChildren().create();
        FieldAccessAnnotationTestBean bean2 = beanFactory.withName("bean2").withParent().withChildren().create();
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1, bean2).create();
        possibleValues.add(BEAN1, bean1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        // still no such element, since bean1 will be removed by inner join to parent
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void innerJoinOnChildren() {
        FieldAccessAnnotationTestBean bean1 = beanFactory.withName("bean1").withParent().withChildren().create();
        FieldAccessAnnotationTestBean bean2 = beanFactory.withName("bean2").withParent().withoutChildren().create();
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
        FieldAccessAnnotationTestBean bean1 = beanFactory.withName("bean1").withParent().withChild().create();
        FieldAccessAnnotationTestBean bean2 = beanFactory.withName("bean2").withParent().withChild().create();
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1, bean2).create();
        possibleValues.add(BEAN1, bean1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParentBean()),
                       entry(CHILD1, (Object)bean1.getChildBeans().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParentBean()),
                       entry(CHILD2, (Object)bean2.getChildBeans().iterator().next())),
                   valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void initializeWithOuterJoinOnChildren() {
        FieldAccessAnnotationTestBean bean1 = beanFactory.withName("bean1").withParent().withoutChildren().create();
        FieldAccessAnnotationTestBean bean2 = beanFactory.withName("bean2").withParent().withChild().create();
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1, bean2).create();
        possibleValues.add(BEAN1, bean1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParentBean()),
                       entry(CHILD1, null),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParentBean()),
                       entry(CHILD2, (Object)bean2.getChildBeans().iterator().next())),
                   valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void initializeWithOuterJoinOnParent() {
        FieldAccessAnnotationTestBean bean1 = beanFactory.withName("bean1").withParent().withChild().create();
        FieldAccessAnnotationTestBean bean2 = beanFactory.withName("bean2").withoutParent().withChild().create();
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1, bean2).create();
        possibleValues.add(BEAN1, bean1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParentBean()),
                       entry(CHILD1, (Object)bean1.getChildBeans().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, null),
                       entry(CHILD2, (Object)bean2.getChildBeans().iterator().next())),
                   valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void initializeWithOuterJoinOnParentAndChildren() {
        FieldAccessAnnotationTestBean bean1 = beanFactory.withName("bean1").withParent().withoutChildren().create();
        FieldAccessAnnotationTestBean bean2 = beanFactory.withName("bean2").withoutParent().withChild().create();
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1, bean2).create();
        possibleValues.add(BEAN1, bean1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParentBean()),
                       entry(CHILD1, null),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, null),
                       entry(CHILD2, (Object)bean2.getChildBeans().iterator().next())),
                   valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void next() {
        FieldAccessAnnotationTestBean bean1 = beanFactory.withName("bean1").withParent().withChildren().create();
        FieldAccessAnnotationTestBean bean2 = beanFactory.withName("bean2").withParent().withChild().create();
        FieldAccessAnnotationTestBean bean1Child1 = bean1.getChildBeans().get(0);
        FieldAccessAnnotationTestBean bean1Child2 = bean1.getChildBeans().get(1);
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1, bean2).create();
        possibleValues.add(BEAN1, bean1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParentBean()),
                       entry(CHILD1, (Object)bean1Child1),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParentBean()),
                       entry(CHILD2, (Object)bean2.getChildBeans().iterator().next())),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParentBean()),
                       entry(CHILD1, (Object)bean1Child2),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParentBean()),
                       entry(CHILD2, (Object)bean2.getChildBeans().iterator().next())),
                    valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void nextWithInnerJoin() {
        FieldAccessAnnotationTestBean bean1a = beanFactory.withName("bean1a").withParent().withChild().create();
        FieldAccessAnnotationTestBean bean1b = beanFactory.withName("bean1b").withoutParent().withChild().create();
        FieldAccessAnnotationTestBean bean2 = beanFactory.withName("bean2").withParent().withChild().create();
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1a, bean1b, bean2).create();
        possibleValues.add(BEAN1, bean1a);
        possibleValues.add(BEAN1, bean1b);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1a),
                       entry(PARENT1, (Object)bean1a.getParentBean()),
                       entry(CHILD1, (Object)bean1a.getChildBeans().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParentBean()),
                       entry(CHILD2, (Object)bean2.getChildBeans().iterator().next())),
                   valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void nextWithOuterJoinOnChildren() {
        FieldAccessAnnotationTestBean bean1 = beanFactory.withName("bean1").withParent().withoutChildren().create();
        FieldAccessAnnotationTestBean bean2 = beanFactory.withName("bean2").withParent().withChildren().create();
        FieldAccessAnnotationTestBean bean2Child1 = bean2.getChildBeans().get(0);
        FieldAccessAnnotationTestBean bean2Child2 = bean2.getChildBeans().get(1);
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1, bean2).create();
        possibleValues.add(BEAN1, bean1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParentBean()),
                       entry(CHILD1, null),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParentBean()),
                       entry(CHILD2, (Object)bean2Child1)),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParentBean()),
                       entry(CHILD1, null),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParentBean()),
                       entry(CHILD2, (Object)bean2Child2)),
                    valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void nextWithOuterJoinOnParent() {
        FieldAccessAnnotationTestBean bean1 = beanFactory.withName("bean1").withParent().withChildren().create();
        FieldAccessAnnotationTestBean bean2 = beanFactory.withName("bean2").withoutParent().withChild().create();
        FieldAccessAnnotationTestBean bean1Child1 = bean1.getChildBeans().get(0);
        FieldAccessAnnotationTestBean bean1Child2 = bean1.getChildBeans().get(1);
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1, bean2).create();
        possibleValues.add(BEAN1, bean1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParentBean()),
                       entry(CHILD1, (Object)bean1Child1),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, null),
                       entry(CHILD2, (Object)bean2.getChildBeans().iterator().next())),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParentBean()),
                       entry(CHILD1, (Object)bean1Child2),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, null),
                       entry(CHILD2, (Object)bean2.getChildBeans().iterator().next())),
                    valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void nextWithOuterJoinOnParentAndChildren() {
        FieldAccessAnnotationTestBean bean1 = beanFactory.withName("bean1").withParent().withoutChildren().create();
        FieldAccessAnnotationTestBean bean2 = beanFactory.withName("bean2").withoutParent().withChildren().create();
        FieldAccessAnnotationTestBean bean2Child1 = bean2.getChildBeans().get(0);
        FieldAccessAnnotationTestBean bean2Child2 = bean2.getChildBeans().get(1);
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1, bean2).create();
        possibleValues.add(BEAN1, bean1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParentBean()),
                       entry(CHILD1, null),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, null),
                       entry(CHILD2, (Object)bean2Child1)),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParentBean()),
                       entry(CHILD1, null),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, null),
                       entry(CHILD2, (Object)bean2Child2)),
                    valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void nextWithTwoPossibleValues() {
        FieldAccessAnnotationTestBean bean1a = beanFactory.withName("bean1a").withParent().withChild().create();
        FieldAccessAnnotationTestBean bean1b = beanFactory.withName("bean1b").withParent().withChild().create();
        FieldAccessAnnotationTestBean bean2 = beanFactory.withName("bean2").withParent().withChild().create();
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1a, bean1b, bean2).create();
        possibleValues.add(BEAN1, bean1a);
        possibleValues.add(BEAN1, bean1b);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1a),
                       entry(PARENT1, (Object)bean1a.getParentBean()),
                       entry(CHILD1, (Object)bean1a.getChildBeans().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParentBean()),
                       entry(CHILD2, (Object)bean2.getChildBeans().iterator().next())),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1b),
                       entry(PARENT1, (Object)bean1b.getParentBean()),
                       entry(CHILD1, (Object)bean1b.getChildBeans().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParentBean()),
                       entry(CHILD2, (Object)bean2.getChildBeans().iterator().next())),
                   valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void nextWithTwoPossibleValuesAndChildren() {
        FieldAccessAnnotationTestBean bean1a = beanFactory.withName("bean1a").withParent().withChild().create();
        FieldAccessAnnotationTestBean bean1b = beanFactory.withName("bean1b").withParent().withChild().create();
        FieldAccessAnnotationTestBean bean2 = beanFactory.withName("bean2").withParent().withChildren().create();
        FieldAccessAnnotationTestBean bean2Child1 = bean2.getChildBeans().get(0);
        FieldAccessAnnotationTestBean bean2Child2 = bean2.getChildBeans().get(1);
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1a, bean1b, bean2).create();
        possibleValues.add(BEAN1, bean1a);
        possibleValues.add(BEAN1, bean1b);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1a),
                       entry(PARENT1, (Object)bean1a.getParentBean()),
                       entry(CHILD1, (Object)bean1a.getChildBeans().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParentBean()),
                       entry(CHILD2, (Object)bean2Child1)),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1a),
                       entry(PARENT1, (Object)bean1a.getParentBean()),
                       entry(CHILD1, (Object)bean1a.getChildBeans().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParentBean()),
                       entry(CHILD2, (Object)bean2Child2)),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1b),
                       entry(PARENT1, (Object)bean1b.getParentBean()),
                       entry(CHILD1, (Object)bean1b.getChildBeans().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParentBean()),
                       entry(CHILD2, (Object)bean2Child1)),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1b),
                       entry(PARENT1, (Object)bean1b.getParentBean()),
                       entry(CHILD1, (Object)bean1b.getChildBeans().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParentBean()),
                       entry(CHILD2, (Object)bean2Child2)),
                   valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void nextWithChildrenAndTwoPossibleValues() {
        FieldAccessAnnotationTestBean bean1a = beanFactory.withName("bean1a").withParent().withChildren().create();
        FieldAccessAnnotationTestBean bean1b = beanFactory.withName("bean1b").withParent().withChild().create();
        FieldAccessAnnotationTestBean bean2 = beanFactory.withName("bean2").withParent().withChild().create();
        FieldAccessAnnotationTestBean bean1aChild1 = bean1a.getChildBeans().get(0);
        FieldAccessAnnotationTestBean bean1aChild2 = bean1a.getChildBeans().get(1);
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1a, bean1b, bean2).create();
        possibleValues.add(BEAN1, bean1a);
        possibleValues.add(BEAN1, bean1b);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1a),
                       entry(PARENT1, (Object)bean1a.getParentBean()),
                       entry(CHILD1, (Object)bean1aChild1),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParentBean()),
                       entry(CHILD2, (Object)bean2.getChildBeans().iterator().next())),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1a),
                       entry(PARENT1, (Object)bean1a.getParentBean()),
                       entry(CHILD1, (Object)bean1aChild2),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParentBean()),
                       entry(CHILD2, (Object)bean2.getChildBeans().iterator().next())),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1b),
                       entry(PARENT1, (Object)bean1b.getParentBean()),
                       entry(CHILD1, (Object)bean1b.getChildBeans().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParentBean()),
                       entry(CHILD2, (Object)bean2.getChildBeans().iterator().next())),
                   valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void nextWithFourPossibleValues() {
        FieldAccessAnnotationTestBean bean1a = beanFactory.withName("bean1a").withParent().withChild().create();
        FieldAccessAnnotationTestBean bean1b = beanFactory.withName("bean1b").withParent().withChild().create();
        FieldAccessAnnotationTestBean bean2a = beanFactory.withName("bean2a").withParent().withChild().create();
        FieldAccessAnnotationTestBean bean2b = beanFactory.withName("bean2b").withParent().withChild().create();
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1a, bean1b, bean2a, bean2b).create();
        possibleValues.add(BEAN1, bean1a);
        possibleValues.add(BEAN1, bean1b);
        possibleValues.add(BEAN2, bean2a);
        possibleValues.add(BEAN2, bean2b);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1a),
                       entry(PARENT1, (Object)bean1a.getParentBean()),
                       entry(CHILD1, (Object)bean1a.getChildBeans().iterator().next()),
                       entry(BEAN2, (Object)bean2a),
                       entry(PARENT2, (Object)bean2a.getParentBean()),
                       entry(CHILD2, (Object)bean2a.getChildBeans().iterator().next())),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1b),
                       entry(PARENT1, (Object)bean1b.getParentBean()),
                       entry(CHILD1, (Object)bean1b.getChildBeans().iterator().next()),
                       entry(BEAN2, (Object)bean2a),
                       entry(PARENT2, (Object)bean2a.getParentBean()),
                       entry(CHILD2, (Object)bean2a.getChildBeans().iterator().next())),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1a),
                       entry(PARENT1, (Object)bean1a.getParentBean()),
                       entry(CHILD1, (Object)bean1a.getChildBeans().iterator().next()),
                       entry(BEAN2, (Object)bean2b),
                       entry(PARENT2, (Object)bean2b.getParentBean()),
                       entry(CHILD2, (Object)bean2b.getChildBeans().iterator().next())),
                    valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1b),
                       entry(PARENT1, (Object)bean1b.getParentBean()),
                       entry(CHILD1, (Object)bean1b.getChildBeans().iterator().next()),
                       entry(BEAN2, (Object)bean2b),
                       entry(PARENT2, (Object)bean2b.getParentBean()),
                       entry(CHILD2, (Object)bean2b.getChildBeans().iterator().next())),
                   valueIterator);
        assertNoSuchElementException(valueIterator);
    }

    @Test
    public void nextWithDoubleJoin() {
        typeDefinitions.add(new TypeDefinition(GRANDCHILD2,
                                               FieldAccessAnnotationTestBean.class,
                                               "child2.children",
                                               false));
        FieldAccessAnnotationTestBean bean1 = beanFactory.withName("bean1").withParent().withChild().create();
        FieldAccessAnnotationTestBean bean2 = beanFactory.withName("bean2").withParent().withChildren().create();
        FieldAccessAnnotationTestBean child2a = bean2.getChildBeans().get(0);
        FieldAccessAnnotationTestBean child2b = bean2.getChildBeans().get(1);
        FieldAccessAnnotationTestBean grandchild2a
            = beanFactory.withName("grandchild2a").withoutParent().withoutChildren().create();
        FieldAccessAnnotationTestBean grandchild2b
            = beanFactory.withName("grandchild2b").withoutParent().withoutChildren().create();
        child2b.getChildBeans().add(grandchild2a);
        child2b.getChildBeans().add(grandchild2b);
        grandchild2a.setParentBean(child2b);
        grandchild2b.setParentBean(child2b);
        PathEvaluator pathEvaluator = pathEvaluatorFactory.withBeans(bean1, bean2, child2a, child2b).create();
        possibleValues.add(BEAN1, bean1);
        possibleValues.add(BEAN2, bean2);
        ValueIterator valueIterator = new ValueIterator(possibleValues, typeDefinitions, pathEvaluator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParentBean()),
                       entry(CHILD1, (Object)bean1.getChildBeans().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParentBean()),
                       entry(CHILD2, (Object)child2a),
                       entry(GRANDCHILD2, null)),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParentBean()),
                       entry(CHILD1, (Object)bean1.getChildBeans().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParentBean()),
                       entry(CHILD2, (Object)child2b),
                       entry(GRANDCHILD2, (Object)grandchild2a)),
                   valueIterator);
        assertNext(map(entry(BEAN1, (Object)bean1),
                       entry(PARENT1, (Object)bean1.getParentBean()),
                       entry(CHILD1, (Object)bean1.getChildBeans().iterator().next()),
                       entry(BEAN2, (Object)bean2),
                       entry(PARENT2, (Object)bean2.getParentBean()),
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

        private FieldAccessAnnotationTestBean[] beans;

        public PathEvaluatorFactory withBeans(FieldAccessAnnotationTestBean... beans) {
            this.beans = beans;
            return this;
        }

        public PathEvaluatorFactory withNoBeans() {
            return withBeans();
        }

        public PathEvaluatorFactory withBean(FieldAccessAnnotationTestBean bean) {
            return withBeans(bean);
        }

        public PathEvaluator create() {
            PathEvaluator pathEvaluator = createMock(PathEvaluator.class);
            for (FieldAccessAnnotationTestBean bean: beans) {
                expect(pathEvaluator.evaluateAll(eq(Collections.singleton(bean)), eq("parent")))
                    .andAnswer(new ParentAnswer(bean)).anyTimes();
                expect(pathEvaluator.evaluateAll(eq(Collections.singleton(bean)), eq("children")))
                    .andAnswer(new ChildrenAnswer(bean)).anyTimes();
            }
            replay(pathEvaluator);
            return pathEvaluator;
        }
    }

    private class ParentAnswer implements IAnswer<List<Object>> {

        private FieldAccessAnnotationTestBean bean;

        public ParentAnswer(FieldAccessAnnotationTestBean bean) {
            this.bean = bean;
        }

        public List<Object> answer() throws Throwable {
            return bean.getParentBean() == null
                   ? Collections.emptyList()
                   : Collections.<Object>singletonList(bean.getParentBean());
        }
    }

    private class ChildrenAnswer implements IAnswer<List<Object>> {

        private FieldAccessAnnotationTestBean bean;

        public ChildrenAnswer(FieldAccessAnnotationTestBean bean) {
            this.bean = bean;
        }

        public List<Object> answer() throws Throwable {
            return bean.getChildBeans() == null || bean.getChildBeans().isEmpty()
                   ? Collections.emptyList()
                   : new ArrayList<Object>(bean.getChildBeans());
        }
    }
}
