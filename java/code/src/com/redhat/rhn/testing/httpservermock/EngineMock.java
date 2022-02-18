/*
 * Copyright (c) 2014 SUSE LLC
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
package com.redhat.rhn.testing.httpservermock;

import simple.http.serve.Resource;
import simple.http.serve.ResourceEngine;

/**
 * Mocks a Simple framework ResourceEngine, which returns a Resource given an
 * Address.
 * @author duncan
 */
public class EngineMock implements ResourceEngine {

    /** The service. */
    private ServiceMock service;

    /**
     * Instantiates a new engine mock.
     *
     * @param serviceIn service that will be returned by resolve()
     */
    public EngineMock(ServiceMock serviceIn) {
        this.service = serviceIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource resolve(String string) {
        return this.service;
    }
}
