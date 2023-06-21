package com.redhat.rhn.manager.channel;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParentChannelException;

/**
 * ForbiddenCloneChannelPAYGException
 */
public class ForbiddenCloneChannelPAYGException extends InvalidParentChannelException {
    /**
     * Constructor
     */
    public ForbiddenCloneChannelPAYGException() {
        super();
    }
}
