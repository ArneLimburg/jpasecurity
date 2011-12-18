/*
 * Copyright 2011 Arne Limburg - open knowledge GmbH
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

import static org.apache.commons.lang.Validate.notNull;

import javax.persistence.Embeddable;

import org.apache.commons.lang.ObjectUtils;

/**
 * A value object that represents names of {@link Teacher}s and {@link Student}s.
 *
 * @author Arne Limburg - open knowledge GmbH (arne.limburg@openknowledge.de)
 */
@Embeddable
public class Name {

    private String first;
    private String last;
    private String nick;

    protected Name() {
        // to satisfy @Embeddable-contract
    }

    public Name(String nick) {
        notNull(nick, "nick may not be null");
        this.nick = nick;
    }

    public Name(String nick, String first, String last) {
        this(nick);
        this.first = first;
        this.last = last;
    }

    public String getFirst() {
        return first;
    }

    public Name newFirst(String first) {
        return new Name(nick, first, last);
    }

    public String getLast() {
        return last;
    }

    public Name newLast(String last) {
        return new Name(nick, first, last);
    }

    public String getNick() {
        return nick;
    }

    public String toString() {
        StringBuilder toStringBuilder = new StringBuilder();
        if (first != null) {
            toStringBuilder.append(first).append(' ');
        }
        if (last != null) {
            toStringBuilder.append(last);
        }
        return toStringBuilder.length() == 0? nick: toStringBuilder.toString().trim();
    }

    public boolean equals(Object object) {
        if (!(object instanceof Name)) {
            return false;
        }
        Name name = (Name)object;
        return nick.equals(name.nick) && ObjectUtils.equals(first, name.first) && ObjectUtils.equals(last, name.last);
    }

    public int hashCode() {
        return nick.hashCode() ^ first.hashCode() ^ last.hashCode();
    }
}
