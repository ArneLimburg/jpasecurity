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
package org.jpasecurity.jsf;

import java.io.IOException;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import org.jpasecurity.AccessType;

/**
 * @author Arne Limburg
 */
public abstract class AbstractBeanTagHandler extends TagHandler {

    private TagAttribute beanAttribute;

    public AbstractBeanTagHandler(TagConfig config) {
        super(config);
        beanAttribute = getAttribute("bean");
    }

    public void apply(FaceletContext context, UIComponent parent) throws IOException {
        ValueExpression expression = beanAttribute.getValueExpression(context, Object.class);
        Object bean = expression.getValue(context.getFacesContext().getELContext());
        if (isAccessible(bean)) {
            nextHandler.apply(context, parent);
        }
    }

    protected boolean isAccessible(Object bean) {
        return JsfAccessContext.getAccessManager().isAccessible(getAccessType(), bean);
    }

    protected abstract AccessType getAccessType();
}
