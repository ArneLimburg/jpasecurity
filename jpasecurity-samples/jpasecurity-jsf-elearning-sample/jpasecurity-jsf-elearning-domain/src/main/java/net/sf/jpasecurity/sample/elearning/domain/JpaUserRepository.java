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
package net.sf.jpasecurity.sample.elearning.domain;

import java.util.List;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;

/**
 * @author Arne Limburg
 */
@Named("userRepository")
public class JpaUserRepository implements UserRepository, TeacherRepository, StudentRepository {

    @Inject
    private EntityManager entityManager;

    @Produces
    @Named("teacher")
    @Typed(Teacher.class)
    public Teacher findTeacher(@Parameter("teacher") Integer id) {
        return getEntityManager().find(Teacher.class, id);
    }

    public Student findStudent(Integer id) {
        return getEntityManager().find(Student.class, id);
    }

    public <U extends User> U findUser(Name name) {
        try {
            return (U)getEntityManager().createNamedQuery(User.BY_NAME, User.class)
                                        .setParameter("nick", name.getNick())
                                        .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public boolean authenticate(Name name, Password password) {
        return getEntityManager().createQuery("SELECT COUNT(user.name.nick) FROM User user "
                                            + "WHERE user.name.nick = :nick "
                                            + "AND user.password.text = :password", Number.class)
                                 .setParameter("nick", name.getNick())
                                 .setParameter("password", password.getText())
                                 .getSingleResult().intValue() > 0;
    }

    public List<Student> getAllStudents() {
        CriteriaQuery<Student> allStudents = getEntityManager().getCriteriaBuilder().createQuery(Student.class);
        allStudents.from(Student.class);
        return getEntityManager().createQuery(allStudents).getResultList();
    }

    public List<Teacher> getAllTeachers() {
        CriteriaQuery<Teacher> allTeachers = getEntityManager().getCriteriaBuilder().createQuery(Teacher.class);
        allTeachers.from(Teacher.class);
        return getEntityManager().createQuery(allTeachers).getResultList();
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }
}
