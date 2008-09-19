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
package net.sf.jpasecurity.contacts;

import javax.persistence.EntityManager;

import net.sf.jpasecurity.contacts.model.ContactAclEntry;

/**
 * @author Arne Limburg
 */
public class AclContactsTestData extends ContactsTestData {

	public AclContactsTestData(EntityManager entityManager) {
		super(entityManager);
	}

	protected void createTestData(EntityManager entityManager) {
		super.createTestData(entityManager);
		entityManager.merge(new ContactAclEntry(johnsContact1));
		entityManager.merge(new ContactAclEntry(johnsContact2));
		entityManager.merge(new ContactAclEntry(marysContact1));
		entityManager.merge(new ContactAclEntry(marysContact2));
	}
	
	protected void clearTestData(EntityManager entityManager) {
	    entityManager.createQuery("delete from ContactAclEntry entry").executeUpdate();
	    super.clearTestData(entityManager);
	}
}
