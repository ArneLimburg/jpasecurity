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
package net.sf.jpasecurity.acl;

import javax.persistence.Basic;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * This is a base class for ACL-Entries.
 * @author Arne Limburg
 */
@MappedSuperclass
public class AclEntry<E, U> {

    private int id;
	private E entity;
	private U owner;
	private boolean create;
	private boolean read;
	private boolean update;
	private boolean delete;

	protected AclEntry() {
		//for subclassing only
	}
	
	public AclEntry(E entity, U owner) {
		this.entity = entity;
		this.owner = owner;
	}
	
    @Transient
	public int getId() {
	    return id;
	}
	
	protected void setId(int id) {
	    this.id = id;
	}

	@Transient
	public E getEntity() {
		return entity;
	}
	
	protected void setEntity(E entity) {
		this.entity = entity;
	}

	@Transient
	public U getOwner() {
		return owner;
	}
	
	public void setOwner(U owner) {
		this.owner = owner;
	}

	@Basic
	public boolean isCreate() {
		return create;
	}

	public void setCreate(boolean create) {
		this.create = create;
	}

	@Basic
	public boolean isRead() {
		return read;
	}

	public void setRead(boolean canRead) {
		this.read = canRead;
	}

	@Basic
	public boolean isUpdate() {
		return update;
	}

	public void setUpdate(boolean canUpdate) {
		this.update = canUpdate;
	}

	@Basic
	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean canDelete) {
		this.delete = canDelete;
	}
	
	public int hashCode() {
		return getEntity().hashCode() ^ getOwner().hashCode();
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof AclEntry)) {
			return false;
		}
		AclEntry<E, U> entry = (AclEntry<E, U>)object;
		return getEntity().equals(entry.getEntity()) && getOwner().equals(entry.getOwner());
	}
}
