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

    public OVALLookupHelper(OvalRootType rootType) {
        this.stateManager = new OvalStateManager(rootType.getStates().getStates());
        this.testManager = new OvalTestManager(rootType.getTests().getTests());
        this.objectManager = new OvalObjectManager(rootType.getObjects().getObjects());
    }

    public Optional<TestType> lookupTestById(String testId) {
        // TODO: testManager#get throws an exception if testId is invalid
        return Optional.ofNullable(testManager.get(testId));
    }

    public Optional<StateType> lookupStateById(String stateId) {
        return Optional.ofNullable(stateManager.get(stateId));
    }

    public Optional<ObjectType> lookupObjectById(String objectId) {
        return Optional.ofNullable(objectManager.get(objectId));
    }
}
