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
package org.jpasecurity.security.authentication.jaas;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * This class can be subclassed to implement a JAAS login module that is based on
 * user name / password authentication.
 * @author Arne Limburg
 */
public abstract class AbstractLoginModule<P extends Principal> implements LoginModule {

    private Subject subject;
    private CallbackHandler callbackHandler;
    private P principal;

    /**
     * This method is called by the JAAS engine on initialization.
     * If you want to intercept the initialization process, you can override
     * {@link #postConstruct(Subject, CallbackHandler, Map, Map)}.
     */
    public final void initialize(Subject subject,
                                 CallbackHandler callbackHandler,
                                 Map<String, ?> sharedState,
                                 Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        postConstruct(subject, callbackHandler, sharedState, options);
    }

    /**
     * This implementation does nothing. However you can override this method
     * to get informed about the initialization of this login module.
     */
    protected void postConstruct(Subject subject,
                                 CallbackHandler callbackHandler,
                                 Map<String, ?> sharedState,
                                 Map<String, ?> options) {
    }

    /**
     * returns the currently authenticated principal or <tt>null</tt> if no user is yet authenticated.
     * @return the authenticated principal or <tt>null</tt>
     */
    public P getCurrentPrincipal() {
        return principal;
    }

    /**
     * This method is called by the JAAS engine on login.
     * It calls {@link #authenticate(String, String)} which has to be implemented by subclasses
     * of this class.
     */
    public final boolean login() throws LoginException {
        try {
            principal = null;
            Callback[] callbacks = new Callback [] {
                new NameCallback("Username"), new PasswordCallback("Password", false)
            };
            callbackHandler.handle(callbacks);
            String username = ((NameCallback)callbacks[0]).getName();
            String password = new String(((PasswordCallback)callbacks[1]).getPassword());
            principal = authenticate(username, password);
        } catch (IOException e) {
            throw (LoginException)new LoginException().initCause(e);
        } catch (UnsupportedCallbackException e) {
            throw (LoginException)new LoginException().initCause(e);
        }
        return principal != null;
    }

    /**
     * Implementations of this method try to authenticate the user with the specified user name and password.
     * If authentication cannot be established this method either may return <tt>null</tt>
     * or throw a {@link LoginException}.
     * @param userName
     * @param password
     * @return the authenticated principal or <tt>null</tt> if authentication could not be established
     * @throws LoginException if some error occurs
     */
    protected abstract P authenticate(String userName, String password) throws LoginException;

    /**
     * This method is called by the JAAS engine on logout.
     * If you want to intercept the logout process you may override {@link #canLogout()}.
     */
    public final boolean logout() throws LoginException {
        if (canLogout()) {
            principal = null;
        }
        return principal == null;
    }

    /**
     * This implementation always returns <tt>true</tt>. Subclasses may override this method
     * to do cleanup on logout and only return <tt>true</tt>, if all cleanup succeeds.
     * @return <tt>true</tt> if the current authenticated user can be logged out, <tt>false</tt> otherwise
     */
    protected boolean canLogout() {
        return true;
    }

    /**
     * This method is called by the JAAS engine to actually establish the authentication.
     * It calls {@link #getAdditionalPrincipals()} to enable subclasses to provide additional
     * principals such as roles.
     */
    public final boolean commit() throws LoginException {
        if (principal == null) {
            return false;
        }
        subject.getPrincipals().add(principal);
        Principal[] additionalPrincipals = getAdditionalPrincipals();
        if (additionalPrincipals != null) {
            for (Principal additionalPrincipal: getAdditionalPrincipals()) {
                subject.getPrincipals().add(additionalPrincipal);
            }
        }
        return true;
    }

    /**
     * This method may provide additional principals for the currently authenticated principal
     * such as roles or groups. If you want to provide additional roles for the currently
     * authenticated principal, you can use {@link #getCurrentPrincipal()} to actually get
     * the currently authenticated principal and return instances of {@link RolePrincipal}
     * in this method.
     * <p>
     * This implementation returns an empty array.
     */
    protected Principal[] getAdditionalPrincipals() {
        return new Principal[0];
    }

    /**
     * This method is called by the JAAS engine to indicate that some other login module
     * prevents authentication. This implementation calls {@link #login()}.
     */
    public boolean abort() throws LoginException {
        if (principal == null) {
            return false;
        }
        return logout();
    }
}
