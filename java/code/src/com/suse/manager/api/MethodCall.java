/*
 * Copyright (c) 2022 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.api;

import com.redhat.rhn.domain.user.User;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * An immutable class that packs together a {@link Method} and an array of arguments with which the method should be
 * invoked
 */
public class MethodCall {
    private final Method method;
    private final Object[] args;

    /**
     * Constructs a {@link MethodCall} instance
     * @param methodIn the method
     * @param argsIn the arguments of the method
     */
    public MethodCall(Method methodIn, Object[] argsIn) {
        this.method = methodIn;
        this.args = argsIn;
    }

    /**
     * Invokes the method with the predefined arguments, on the specified object
     * @param obj the object the underlying method is invoked on
     * @return the result of the invocation
     * @throws InvocationTargetException if the underlying method throws an exception
     * @throws IllegalAccessException if the underlying method is inaccessible
     */
    public Object invoke(Object obj) throws InvocationTargetException, IllegalAccessException,
            UserNotPermittedException {
        ensureUserAccess();
        return method.invoke(obj, args);
    }

    public Method getMethod() {
        return method;
    }

    private void ensureUserAccess() throws UserNotPermittedException {
        for (Object arg : args) {
            if (arg instanceof User) {
                User user = (User) arg;
                if (user.isReadOnly() && !method.isAnnotationPresent(ReadOnly.class)) {
                    throw new UserNotPermittedException("The method is not available to read-only API users");
                }
            }
        }
    }
}
