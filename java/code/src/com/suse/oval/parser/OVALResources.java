/*
 * Copyright (c) 2024 SUSE LLC
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

package com.suse.oval.parser;

import com.suse.oval.ovaltypes.ObjectType;
import com.suse.oval.ovaltypes.StateType;
import com.suse.oval.ovaltypes.TestType;

import java.util.ArrayList;
import java.util.List;

public class OVALResources {
    private List<ObjectType> objects = new ArrayList<>();
    private List<StateType> states = new ArrayList<>();
    private List<TestType> tests = new ArrayList<>();

    public List<ObjectType> getObjects() {
        return objects;
    }

    public List<StateType> getStates() {
        return states;
    }

    public List<TestType> getTests() {
        return tests;
    }

    public void setObjects(List<ObjectType> objectsIn) {
        this.objects = objectsIn;
    }

    public void setStates(List<StateType> statesIn) {
        this.states = statesIn;
    }

    public void setTests(List<TestType> testsIn) {
        this.tests = testsIn;
    }
}
