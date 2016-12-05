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
package org.jpasecurity.entity;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.jpasecurity.AccessManager;
import org.jpasecurity.AlwaysPermittingAccessManager;
import org.jpasecurity.BeanStore;
import org.jpasecurity.DefaultSecurityUnit;
import org.jpasecurity.SecureEntity;
import org.jpasecurity.SecurityUnit;
import org.jpasecurity.configuration.AccessRulesProvider;
import org.jpasecurity.configuration.Configuration;
import org.jpasecurity.configuration.ConfigurationReceiver;
import org.jpasecurity.configuration.SecurityContextReceiver;
import org.jpasecurity.mapping.MappingInformation;
import org.jpasecurity.mapping.MappingInformationReceiver;
import org.jpasecurity.mapping.bean.JavaBeanSecurityUnitParser;
import org.jpasecurity.model.MethodAccessTestBean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class SecureEntityTest {

    private AccessManager accessManager = new AlwaysPermittingAccessManager();

    private MethodAccessTestBean unsecureBean;
    private MethodAccessTestBean secureBean;

    @Before
    public void initialize() {
        SecurityUnit securityUnit = new DefaultSecurityUnit("test");
        securityUnit.getManagedClassNames().add(MethodAccessTestBean.class.getName());
        MappingInformation mapping = new JavaBeanSecurityUnitParser(securityUnit).parse();
        BeanStore beanStore = new DefaultBeanStore();
        Configuration configuration = new Configuration();
        AccessRulesProvider accessRulesProvider = configuration.getAccessRulesProvider();
        if (accessRulesProvider instanceof MappingInformationReceiver) {
            ((MappingInformationReceiver)accessRulesProvider).setMappingInformation(mapping);
        }
        if (accessRulesProvider instanceof ConfigurationReceiver) {
            ((ConfigurationReceiver)accessRulesProvider).setConfiguration(configuration);
        }
        if (accessRulesProvider instanceof SecurityContextReceiver) {
            ((SecurityContextReceiver)accessRulesProvider).setSecurityContext(configuration.getSecurityContext());
        }
        SecureObjectManager objectManager
            = new DefaultSecureObjectCache(mapping, beanStore, accessManager, configuration);

        unsecureBean = new MethodAccessTestBean();
        beanStore.persist(unsecureBean);

        secureBean = objectManager.getSecureObject(unsecureBean);
        AccessManager.Instance.register(accessManager);
    }

    @After
    public void unregisterAccessManager() {
        AccessManager.Instance.unregister(accessManager);
    }

    @Test
    public void flush() {
        ((SecureEntity)secureBean).flush();

        assertThat(unsecureBean.wasSetNameCalled(), is(false));

        secureBean.setName("Test");
        ((SecureEntity)secureBean).flush();

        assertThat(unsecureBean.wasSetNameCalled(), is(true));
    }

    @Test
    public void flushCollection() {
        assertThat(unsecureBean.getChildren().isEmpty(), is(true));

        secureBean.getChildren().add(secureBean);
        ((SecureEntity)secureBean).flush();

        assertThat(unsecureBean.wasSetNameCalled(), is(false));
        assertThat(unsecureBean.getChildren().size(), is(1));
        assertThat(unsecureBean.getChildren().iterator().next(), is(unsecureBean));

        secureBean.getChildren().clear();
        secureBean.setName("test");
        ((SecureEntity)secureBean).flush();

        assertThat(unsecureBean.wasSetNameCalled(), is(true));
        assertThat(unsecureBean.getChildren().isEmpty(), is(true));
    }
}
