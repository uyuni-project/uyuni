/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.oval.manager;

import com.suse.oval.ovaltypes.ObjectType;
import com.suse.oval.ovaltypes.OvalRootType;
import com.suse.oval.ovaltypes.StateType;
import com.suse.oval.ovaltypes.TestType;

import java.util.Optional;

public class OVALLookupHelper {
    private final OvalStateManager stateManager;
    private final OvalTestManager testManager;
    private final OvalObjectManager objectManager;

    /**
     * Standard constructor
     *
     * @param rootType the root to get OVAL resources from
     * */
    public OVALLookupHelper(OvalRootType rootType) {
        this.stateManager = new OvalStateManager(rootType.getStates());
        this.testManager = new OvalTestManager(rootType.getTests());
        this.objectManager = new OvalObjectManager(rootType.getObjects());
    }

    /**
     * Looks up an OVAL test with an id of {@code testId}
     *
     * @param testId the test id to look up
     * @return the cached {@link TestType} object that correspond to the test id
     * */
    public Optional<TestType> lookupTestById(String testId) {
        // TODO: testManager#get throws an exception if testId is invalid
        return Optional.ofNullable(testManager.get(testId));
    }

    /**
     * Looks up an OVAL state with an id of {@code stateId}
     *
     * @param stateId the object id to look up
     * @return the cached StateType object that correspond to the state id
     * */
    public Optional<StateType> lookupStateById(String stateId) {
        return Optional.ofNullable(stateManager.get(stateId));
    }

    /**
     * Looks up an OVAL object with an id of {@code objectID}
     *
     * @param objectId the object id to look up
     * @return the cached ObjectType object that correspond to the object id
     * */
    public Optional<ObjectType> lookupObjectById(String objectId) {
        return Optional.ofNullable(objectManager.get(objectId));
    }
}
