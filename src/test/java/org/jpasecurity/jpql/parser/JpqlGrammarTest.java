/*
 * Copyright 2008 Arne Limburg
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
package org.jpasecurity.jpql.parser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class JpqlGrammarTest {

    private final String source;

    public JpqlGrammarTest(String source) {
        this.source = source;
    }

    @Test
    public void parseStatement() {
        try {
            new JpqlParser().parseQuery(source);
        } catch (ParseException e) {
            fail("failed to parse jpql:\n" + source + "\n\n" + e.getMessage());
        }
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"SELECT e FROM Employee e"},
                {"SELECT e FROM Employee e WHERE 1 <> 1"},
                {"SELECT DISTINCT e FROM Employee e"},
                {"SELECT c FROM Customer c, Employee e WHERE c.hatsize = e.shoesize"},
                {"SELECT c FROM Customer c JOIN c.orders o WHERE c.status = 1"},
                {"SELECT c FROM Customer c INNER JOIN c.orders o WHERE c.status = 1"},
                {"SELECT OBJECT(c) FROM Customer c, IN(c.orders) o WHERE c.status = 1"},
                //{"SELECT DISTINCT o1 FROM Order o1, Order o2 WHERE o1.quantity > o2.quantity AND o2.customer.lastname = 'Smith' AND o2.customer.firstname = 'John'"},
                {"SELECT p.vendor FROM Employee e JOIN e.contactInfo c JOIN c.phones p WHERE c.address.zipcode = '95054'"},
                //{"SELECT DISTINCT l.product FROM Order AS o JOIN o.lineItems l"},
                {"SELECT i.name, VALUE(p) FROM Item i JOIN i.photos p WHERE KEY(p) LIKE '%egret'"},
                {"DELETE FROM Employee e"},
                {"DELETE FROM Employee e WHERE 1 <> 1"},
                {"SELECT s.name, COUNT(p) FROM Suppliers s LEFT JOIN s.products p GROUP BY s.name"},
                {"SELECT s.name, COUNT(p) FROM Suppliers s LEFT JOIN s.products p ON p.status = 'inStock' GROUP BY s.name"},
                {"SELECT d FROM Department d LEFT JOIN FETCH d.employees WHERE d.deptno = 1"},
                //{"SELECT DISTINCT o FROM Order o JOIN o.lineItems l WHERE l.product.productType = 'office_supplies'"},
                //{"SELECT DISTINCT o FROM Order o, IN(o.lineItems) l WHERE l.product.productType = 'office_supplies'"},
                //{"SELECT o FROM Order AS o JOIN o.lineItems l JOIN l.product p"},
                //{"SELECT b.name, b.ISBN FROM Order o JOIN TREAT(o.product AS Book) b"},
                //{"SELECT e FROM Employee e JOIN TREAT(e.projects AS LargeProject) lp WHERE lp.budget > 1000"},
                //{"SELECT e FROM Employee e JOIN e.projects p WHERE TREAT(p AS LargeProject).budget > 1000 OR TREAT(p AS SmallProject).name LIKE 'Persist%' OR p.description LIKE 'cost overrun'"},
                //{"SELECT e FROM Employee e WHERE TREAT(e AS Exempt).vacationDays > 10 OR TREAT(e AS Contractor).hours > 100"},
                {"SELECT c FROM Customer c WHERE c.status = :stat"},
                {"SELECT t FROM CreditCard c JOIN c.transactionHistory t WHERE c.holder.name = 'John Doe' AND INDEX(t) BETWEEN 0 AND 9"},
                //{"SELECT o FROM Order o WHERE o.lineItems IS EMPTY"},
                //{"SELECT p FROM Person p WHERE 'Joe' MEMBER OF p.nicknames"},
                {"SELECT DISTINCT emp FROM Employee emp WHERE EXISTS (SELECT spouseEmp FROM Employee spouseEmp WHERE spouseEmp = emp.spouse)"},
                {"SELECT emp FROM Employee emp WHERE emp.salary > ALL (SELECT m.salary FROM Manager m WHERE m.department = emp.department)"},
                {"SELECT DISTINCT emp FROM Employee emp WHERE EXISTS (SELECT spouseEmp FROM Employee spouseEmp WHERE spouseEmp = emp.spouse)"},
                {"SELECT c FROM Customer c WHERE (SELECT AVG(o.price) FROM c.orders o) > 100"},
                //{"SELECT goodCustomer FROM Customer goodCustomer WHERE goodCustomer.balanceOwed < (SELECT AVG(c.balanceOwed)/2.0 FROM Customer c)"},
                {"SELECT w.name FROM Course c JOIN c.studentWaitlist w WHERE c.name = 'Calculus' AND INDEX(w) = 0"},
                //{"SELECT c FROM Customer c WHERE FUNCTION(‘hasGoodCredit’, c.balance, c.creditLimit)"},
                //{"UPDATE Employee e SET e.salary = CASE WHEN e.rating = 1 THEN e.salary * 1.1 WHEN e.rating = 2 THEN e.salary * 1.05 ELSE e.salary * 1.01 END"},
                //{"UPDATE Employee e SET e.salary = CASE e.rating WHEN 1 THEN e.salary * 1.1 WHEN 2 THEN e.salary * 1.05 ELSE e.salary * 1.01 END"},
                {"SELECT e.name, CASE TYPE(e) WHEN Exempt THEN 'Exempt' WHEN Contractor THEN 'Contractor' WHEN Intern THEN 'Intern' ELSE 'NonExempt' END FROM Employee e WHERE e.dept.name = 'Engineering'"},
                //{"SELECT e.name, f.name, CONCAT(CASE WHEN f.annualMiles > 50000 THEN 'Platinum' WHEN f.annualMiles > 25000 THEN 'Gold' ELSE '' END, 'Frequent Flyer') FROM Employee e JOIN e.frequentFlierPlan f"},
                //{"SELECT e FROM Employee e WHERE TYPE(e) IN (Exempt, Contractor)"},
                {"SELECT e FROM Employee e WHERE TYPE(e) IN (:empType1, :empType2)"},
                {"SELECT e FROM Employee e WHERE TYPE(e) IN :empTypes"},
                {"SELECT TYPE(e) FROM Employee e WHERE TYPE(e) <> Exempt"},
                {"SELECT c.status, AVG(c.filledOrderCount), COUNT(c) FROM Customer c GROUP BY c.status HAVING c.status IN (1, 2)"},
                {"SELECT c.country, COUNT(c) FROM Customer c GROUP BY c.country HAVING COUNT(c) > 30"},
                {"SELECT c, COUNT(o) FROM Customer c JOIN c.orders o GROUP BY c HAVING COUNT(o) >= 5"},
                {"SELECT c.id, c.status FROM Customer c JOIN c.orders o WHERE o.count > 100"},
                {"SELECT v.location.street, KEY(i).title, VALUE(i) FROM VideoStore v JOIN v.videoInventory i WHERE v.location.zipcode = '94301' AND VALUE(i) > 0"},
                {"SELECT c, COUNT(l) AS itemCount FROM Customer c JOIN c.Orders o JOIN o.lineItems l WHERE c.address.state = 'CA' GROUP BY c ORDER BY itemCount"},
                {"SELECT NEW com.acme.example.CustomerDetails(c.id, c.status, o.count) FROM Customer c JOIN c.orders o WHERE o.count > 100"},
                //{"SELECT AVG(o.quantity) FROM Order o"},
                //{"SELECT SUM(l.price) FROM Order o JOIN o.lineItems l JOIN o.customer c WHERE c.lastname = 'Smith' AND c.firstname = 'John'"},
                {"SELECT AVG(o.quantity) as q, a.zipcode FROM Customer c JOIN c.orders o JOIN c.address a WHERE a.state = 'CA' GROUP BY a.zipcode ORDER BY q DESC"},
                {"DELETE FROM Customer c WHERE c.status = 'inactive'"},
                {"DELETE FROM Customer c WHERE c.status = 'inactive' AND c.orders IS EMPTY"},
                {"UPDATE Customer c SET c.status = 'outstanding' WHERE c.balance < 10000"},
                {"UPDATE Employee e SET e.address.building = 22 WHERE e.address.building = 14 AND e.address.city = 'Santa Clara' AND e.project = 'Java EE'"},
                //{"SELECT DISTINCT o FROM Order o JOIN o.lineItems l WHERE l.product.name = ?1"},
                //{"SELECT CAST( true AS boolean ), CAST( false as boolean ), e.name FROM MyEntity e"},
                //{"SELECT o FROM EntityWith$InnerClass o LEFT JOIN FETCH o.orderPosition pos LEFT JOIN FETCH pos.product LEFT JOIN FETCH o.shippingAddress"},
                {"SELECT CASE l.id WHEN (SELECT COUNT(r.id) FROM Root r) THEN 1 ELSE 0 END FROM Leaf l"},
                {"SELECT bean FROM TestBean bean WHERE bean.id = :id"},
                {"SELECT COUNT(bean) FROM TestBean bean WHERE bean.id = :id"},
                {"SELECT COUNT( DISTINCT bean.id) FROM TestBean bean WHERE bean.id = :id"},
                {"SELECT AVG(bean) FROM TestBean bean WHERE bean.id = :id"},
                {"SELECT SUM(bean) FROM TestBean bean WHERE bean.id = :id"},
                {"SELECT bean FROM org.jpasecurity.model.TestBean bean WHERE bean.id = :id"},
                //{"SELECT bean FROM TestBean bean LEFT OUTER JOIN bean.name name WHERE bean.id = :id"},
                //{"SELECT bean FROM TestBean bean LEFT OUTER JOIN FETCH bean.name WHERE bean.id = :id"},
                //{"SELECT bean FROM TestBean bean LEFT OUTER JOIN FETCH bean.name beanName WHERE bean.id = :id"},
                {"SELECT bean FROM TestBean bean INNER JOIN bean.name name WHERE bean.id = :id"},
                {"SELECT bean FROM TestBean bean INNER JOIN FETCH bean.name WHERE bean.id = :id"},
                {"SELECT bean FROM TestBean bean INNER JOIN bean.parent parent WITH parent.name = 'Parent' WHERE bean.id = :id"},
                {"SELECT bean FROM TestBean bean WHERE (bean.id BETWEEN 5 AND 7)"},
                {"SELECT bean FROM TestBean bean WHERE (bean.id NOT BETWEEN 5 AND 7)"},
                {"SELECT DISTINCT bean, bean.id FROM TestBean bean WHERE :id = bean.id"},
                {"SELECT bean1 FROM TestBean bean1, TestBean bean2 WHERE bean1.id < bean2.id"},
                {"SELECT bean.name FROM TestBean bean WHERE (bean.name <> 'testBean')"},
                {"SELECT bean1.name FROM TestBean bean1, TestBean bean2 WHERE (bean1.id - (bean2.id - 1)) = 5"},
                {"SELECT bean.name FROM TestBean bean WHERE bean.booleanValue = true"},
                //{"SELECT bean.name FROM TestBean bean WHERE ((3 + 2) * 2) = 10.0"},
                //{"SELECT bean.name FROM TestBean bean WHERE ((3 + 2) / 2) = 10.0"},
                //{"SELECT bean.name FROM TestBean bean WHERE ((3 - 2) * 2) <= 10.0"},
                //{"SELECT bean.name FROM TestBean bean WHERE ((3 + 2) * 2) >= 10.0"},
                {"SELECT bean.id FROM TestBean bean WHERE ABS(bean.id) = 1 HAVING bean.id > 0"},
                {"SELECT bean.id FROM TestBean bean WHERE ABS(bean.id) = 1 GROUP BY bean.id HAVING COUNT(bean.id) > 0"},
                {"SELECT bean.id, bean.name, COUNT(bean.collectionProperty.id) FROM TestBean bean GROUP BY bean.id, bean.name"},
                //{"SELECT bean FROM TestBean bean WHERE TRIM(LEADING ' ' FROM bean.name) = TRIM(TRAILING FROM bean.name)"},
                {"SELECT bean FROM TestBean bean WHERE TRIM(bean.name) = TRIM(BOTH FROM bean.name)"},
                {"SELECT bean FROM TestBean bean WHERE bean.name = ALL( SELECT bean.collectionProperty.name FROM TestBean bean)"},
                {"SELECT bean FROM TestBean bean WHERE bean.name = ANY( SELECT bean.collectionProperty.name FROM TestBean bean)"},
                {"SELECT NEW org.jpasecurity.TestBean(bean.id, bean.name) FROM TestBean bean"},
                {"SELECT bean FROM TestBean bean WHERE SIZE(bean.collectionProperty) = 0"},
                {"SELECT DISTINCT bean FROM TestBean bean, TestBean bean2 INNER JOIN FETCH bean.collectionProperty"},
                {"SELECT bean FROM TestBean bean WHERE bean.dateProperty = CURRENT_DATE"},
                {"SELECT DISTINCT bean FROM TestBean bean LEFT OUTER JOIN FETCH bean.beanProperty"},
                {"SELECT bean FROM TestBean bean WHERE bean.id > 0"},
                {"SELECT bean FROM TestBean bean WHERE bean.name LIKE '%beanName%'"},
                {"SELECT bean FROM TestBean bean WHERE bean.name LIKE '%beanName%' ESCAPE '\\'"},
                {"SELECT bean FROM TestBean bean WHERE bean.name NOT LIKE '%beanName%'"},
                {"SELECT bean FROM TestBean bean WHERE (bean.collectionProperty IS NULL OR SIZE(bean.collectionProperty) = 0)"},
                {"SELECT bean FROM TestBean bean WHERE bean.name IS NOT NULL"},
                {"SELECT bean FROM TestBean bean WHERE NOT (bean.id = SQRT(2))"},
                {"SELECT bean FROM TestBean bean WHERE LOWER(bean.name) = 'test'"},
                {"SELECT bean FROM TestBean bean ORDER BY bean.id ASC, bean.name DESC"},
                {"SELECT bean FROM TestBean bean WHERE bean.id = MIN( DISTINCT bean.id)"},
                {"SELECT bean FROM TestBean bean WHERE bean.id = MAX( DISTINCT bean.id)"},
                {"SELECT bean FROM TestBean bean WHERE bean.id = MAX( DISTINCT bean.id)"},
                {"SELECT bean FROM TestBean bean WHERE bean.collectionProperty IS EMPTY"},
                {"SELECT bean FROM TestBean bean WHERE bean.collectionProperty IS NOT EMPTY"},
                {"SELECT bean FROM TestBean bean WHERE (bean.id = 0 AND bean.name = 'Test')"},
                //{"SELECT bean FROM TestBean bean WHERE = MOD(bean.id, 2) = -1"},
                {"SELECT bean FROM TestBean bean WHERE bean MEMBER OF bean.collectionProperty"},
                {"SELECT bean FROM TestBean bean WHERE bean NOT MEMBER OF bean.collectionProperty"},
                {"SELECT bean FROM TestBean bean WHERE EXISTS ( SELECT bean FROM TestBean bean WHERE bean.id = :id)"},
                {"SELECT bean FROM TestBean bean WHERE NOT EXISTS ( SELECT bean FROM TestBean bean WHERE bean.id = :id)"},
                {"SELECT bean FROM TestBean bean WHERE NOT EXISTS ( SELECT /* QUERY_OPTIMIZE_NOCACHE */ bean FROM TestBean bean WHERE bean.id = :id)"},
                {"SELECT bean FROM TestBean bean WHERE NOT EXISTS ( SELECT /* IS_ACCESSIBLE_NODB */ bean FROM TestBean bean WHERE bean.id = :id)"},
                {"SELECT bean FROM TestBean bean WHERE NOT EXISTS ( SELECT /* QUERY_OPTIMIZE_NOCACHE IS_ACCESSIBLE_NODB IS_ACCESSIBLE_NOCACHE */ bean FROM TestBean bean WHERE bean.id = :id)"},
                {"SELECT bean FROM TestBean bean WHERE SUBSTRING(bean.name, 2, 3) = 'est'"},
                {"SELECT bean FROM TestBean bean WHERE LOCATE(bean.name, 'est') = 2"},
                {"SELECT bean FROM TestBean bean WHERE CONCAT(bean.name, 'est') = 'Nameest'"},
                {"SELECT bean FROM TestBean bean WHERE LOCATE(bean.name, 'est', 2) = -1"},
                {"SELECT bean FROM TestBean bean WHERE LENGTH(bean.name) = 0"},
                {"SELECT bean FROM TestBean bean WHERE UPPER(bean.name) = 'NAME'"},
                {"SELECT bean FROM TestBean bean WHERE LOWER(bean.name) = 'name'"},
                {"SELECT bean FROM TestBean bean WHERE bean.name = ?1"},
                {"SELECT bean FROM TestBean bean WHERE bean.created < CURRENT_TIMESTAMP"},
                {"SELECT bean FROM TestBean bean WHERE bean.created < CURRENT_TIME"},
                {"SELECT bean FROM TestBean bean WHERE bean.name IN ('name 1', 'name 2')"},
                {"SELECT bean FROM TestBean bean WHERE bean.name NOT IN ('name 1', 'name 2')"},
                {"SELECT bean, 'name', CASE WHEN bean.name = 'name 1' THEN bean.name ELSE 'name 2' END FROM TestBean bean WHERE bean.name NOT IN ('name 1', 'name 2')"},
                {"SELECT bean, 'name', CASE bean.name WHEN 'name 1' THEN bean.name ELSE 'name 2' END FROM TestBean bean WHERE bean.name NOT IN ('name 1', 'name 2')"},
                {"SELECT COALESCE(parent.name, KEY(related).name, VALUE(related).name, bean.name) FROM TestBean bean LEFT OUTER JOIN bean.parent parent LEFT OUTER JOIN bean.related related"},
                {"SELECT NULLIF(bean.name, 'Test') FROM TestBean bean"},
                {"SELECT bean, COUNT(DISTINCT bean) AS beanCount FROM TestBean bean WHERE bean.name = 'name 1'"},
                {"SELECT bean FROM TestBean bean WHERE TYPE(bean) = TestBeanSubclass"},
                {"SELECT bean FROM TestBean bean INNER JOIN bean.related related WHERE KEY(related).name = 'name 1'"},
                {"SELECT bean FROM TestBean bean INNER JOIN bean.related related WHERE VALUE(related).name = 'name 1'"},
                {"SELECT bean FROM TestBean bean INNER JOIN bean.related related WHERE ENTRY(related) IS NOT NULL"},
                {"SELECT bean FROM TestBean bean INNER JOIN bean.related related WHERE ENTRY(related) IS NULL"},
                {"UPDATE TestBean bean SET bean.name = 'test', bean.id = 0"},
                {"UPDATE TestBean bean SET bean.name = 'test', bean.id = 1 WHERE bean.id = 0"},
                {"DELETE FROM TestBean bean"},
                {"DELETE FROM TestBean bean WHERE bean.id = 0"},
                {"SELECT bean FROM TestBean bean WHERE bean.value = ?1"},
                {"SELECT bean FROM TestBean bean WHERE bean.key = ?1"},
                {"SELECT bean FROM TestBean bean WHERE bean.entry = ?1"},
                {"SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Horst') IN ('Horst')"},
                //{"SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Horst') = 'Horst'"},
                //{"SELECT bean FROM TestBean bean WHERE 'Horst' = COALESCE(bean.name, 'Horst')"},
                {"SELECT bean FROM TestBean bean WHERE bean.name BETWEEN COALESCE(bean.name, 'Horst') AND bean.name"},
                {"SELECT bean FROM TestBean bean WHERE bean.name BETWEEN bean.name AND COALESCE(bean.name, 'Horst')"},
                {"SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Horst') BETWEEN COALESCE(bean.name, 'Horst') AND bean.name"},
                {"SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Horst') BETWEEN bean.name AND COALESCE(bean.name, 'Horst')"},
                {"SELECT bean FROM TestBean bean WHERE bean.name BETWEEN COALESCE(bean.name, 'Horst') AND COALESCE(bean.name, 'Horst')"},
                {"SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Horst') BETWEEN COALESCE(bean.name, 'Horst') AND COALESCE(bean.name, 'Horst')"},
                {"SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Horst') NOT BETWEEN bean.name AND bean.name"},
                {"SELECT bean FROM TestBean bean WHERE bean.name NOT BETWEEN COALESCE(bean.name, 'Horst') AND bean.name"},
                {"SELECT bean FROM TestBean bean WHERE bean.name NOT BETWEEN bean.name AND COALESCE(bean.name, 'Horst')"},
                {"SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Horst') NOT BETWEEN COALESCE(bean.name, 'Horst') AND bean.name"},
                {"SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Horst') NOT BETWEEN bean.name AND COALESCE(bean.name, 'Horst')"},
                {"SELECT bean FROM TestBean bean WHERE bean.name NOT BETWEEN COALESCE(bean.name, 'Horst') AND COALESCE(bean.name, 'Horst')"},
                //{"SELECT bean FROM TestBean bean WHERE COALESCE(bean.name, 'Horst') NOT BETWEEN COALESCE(bean.name, 'Horst') AND COALESCE(bean.name, 'Horst')"},
                //{"SELECT bean FROM TestBean bean WHERE UPPER(bean.name) IN ('Horst')"},
                //{"SELECT bean FROM TestBean bean WHERE UPPER(bean.name) = ('Horst')"}
        });
    }
}
