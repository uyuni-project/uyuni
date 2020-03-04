package com.suse.manager.extensions;

import com.redhat.rhn.frontend.nav.NavTree;
import org.pf4j.ExtensionPoint;

public interface NavTreeExtensionPoint extends ExtensionPoint {

    boolean isNodeDisabled(NavTree node);

    void addNodes(NavTree tree);

}
