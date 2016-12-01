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
package org.jpasecurity.persistence;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jpasecurity.model.MethodAccessAnnotationTestBean;

/**
 * @author Arne Limburg
 */
public class ParentChildTestData {

    private EntityManager entityManager;

    public ParentChildTestData(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<MethodAccessAnnotationTestBean> createPermutations(String... names) {
        List<MethodAccessAnnotationTestBean> children = new ArrayList<MethodAccessAnnotationTestBean>();
        for (String parentName: names) {
            for (String childName: names) {
                children.add(createChild(parentName, childName));
            }
        }
        return children;
    }

    public MethodAccessAnnotationTestBean createChild(String parentName, String childName) {
        MethodAccessAnnotationTestBean parent = new MethodAccessAnnotationTestBean(parentName);
        MethodAccessAnnotationTestBean child = new MethodAccessAnnotationTestBean(childName);
        child.setParent(parent);
        parent.getChildren().add(child);
        entityManager.persist(child);
        entityManager.persist(parent);
        return child;
    }
}
