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
public class MethodAccessTestBeanBuilder {

    private String name;
    private boolean withParent;
    private int childCount;

    public MethodAccessTestBeanBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public MethodAccessTestBeanBuilder withParent() {
        withParent = true;
        return this;
    }

    public MethodAccessTestBeanBuilder withoutParent() {
        withParent = false;
        return this;
    }

    public MethodAccessTestBeanBuilder withChild() {
        return withChildren(1);
    }

    public MethodAccessTestBeanBuilder withChildren() {
        return withChildren(2);
    }

    public MethodAccessTestBeanBuilder withoutChildren() {
        return withChildren(0);
    }

    public MethodAccessTestBeanBuilder withChildren(int count) {
        childCount = count;
        return this;
    }

    public MethodAccessTestBean create() {
        MethodAccessTestBean bean = new MethodAccessTestBean(name);
        if (withParent) {
            MethodAccessTestBean parent = new MethodAccessTestBean(name + "Parent");
            bean.setParent(parent);
            parent.getChildren().add(bean);
        }
        for (int i = 1; i <= childCount; i++) {
            MethodAccessTestBean child = new MethodAccessTestBean(name + "Child" + i);
            child.setParent(bean);
            bean.getChildren().add(child);
        }
        return bean;
    }
}
