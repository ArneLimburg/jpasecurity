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
package org.jpasecurity.model;

/**
 * @author Arne Limburg
 */
public class FieldAccessAnnotationTestBeanFactory {

    private String name;
    private boolean withParent;
    private int childCount;

    public FieldAccessAnnotationTestBeanFactory withName(String name) {
        this.name = name;
        return this;
    }

    public FieldAccessAnnotationTestBeanFactory withParent() {
        withParent = true;
        return this;
    }

    public FieldAccessAnnotationTestBeanFactory withoutParent() {
        withParent = false;
        return this;
    }

    public FieldAccessAnnotationTestBeanFactory withChild() {
        return withChildren(1);
    }

    public FieldAccessAnnotationTestBeanFactory withChildren() {
        return withChildren(2);
    }

    public FieldAccessAnnotationTestBeanFactory withoutChildren() {
        return withChildren(0);
    }

    public FieldAccessAnnotationTestBeanFactory withChildren(int count) {
        childCount = count;
        return this;
    }

    public FieldAccessAnnotationTestBean create() {
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(name);
        if (withParent) {
            FieldAccessAnnotationTestBean parent = new FieldAccessAnnotationTestBean(name + "Parent");
            bean.setParentBean(parent);
            parent.getChildBeans().add(bean);
        }
        for (int i = 1; i <= childCount; i++) {
            FieldAccessAnnotationTestBean child = new FieldAccessAnnotationTestBean(name + "Child" + i);
            child.setParentBean(bean);
            bean.getChildBeans().add(child);
        }
        return bean;
    }
}
