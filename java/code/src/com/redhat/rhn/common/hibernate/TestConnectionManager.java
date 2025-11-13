/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.common.hibernate;


import com.redhat.rhn.domain.test.TestImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the lifecycle of Hibernate SessionFactory and associated
 * thread-scoped Hibernate sessions.
 */
public class TestConnectionManager extends DefaultConnectionManager {

    @Override
    protected List<Class<?>> getAnnotatedClasses() {
        List<Class<?>> classList = new ArrayList<>(AnnotationRegistry.getAnnotationClasses());
        classList.add(TestImpl.class);
        return classList;
    }
}
