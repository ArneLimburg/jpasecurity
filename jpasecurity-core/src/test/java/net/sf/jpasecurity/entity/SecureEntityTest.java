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
package net.sf.jpasecurity.entity;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import net.sf.jpasecurity.AlwaysPermittingAccessManager;
import net.sf.jpasecurity.BeanStore;
import net.sf.jpasecurity.DefaultSecurityUnit;
import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.SecurityUnit;
import net.sf.jpasecurity.configuration.AccessRulesProvider;
import net.sf.jpasecurity.configuration.Configuration;
import net.sf.jpasecurity.configuration.ConfigurationReceiver;
import net.sf.jpasecurity.configuration.SecurityContextReceiver;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.MappingInformationReceiver;
import net.sf.jpasecurity.mapping.bean.JavaBeanSecurityUnitParser;
import net.sf.jpasecurity.model.MethodAccessTestBean;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class SecureEntityTest {

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
            = new DefaultSecureObjectCache(mapping, beanStore, new AlwaysPermittingAccessManager(), configuration);

        unsecureBean = new MethodAccessTestBean();
        beanStore.persist(unsecureBean);

        secureBean = objectManager.getSecureObject(unsecureBean);
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
