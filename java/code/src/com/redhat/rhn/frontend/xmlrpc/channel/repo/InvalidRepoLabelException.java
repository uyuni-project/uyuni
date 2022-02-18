/*
 * Copyright (c) 2010 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.channel.repo;

import com.redhat.rhn.FaultException;


/**
 * InvalidRepoLabelException
 */
public class InvalidRepoLabelException extends FaultException {

    /**
     * Indicates why the repository label is invalid.
     */
    private Reason reason;

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -8506595413724954752L;

    /**
     * Constructor
     * @param repoLabel Repository label already in use
     */
    public InvalidRepoLabelException(String repoLabel) {
        super(2, "Repo label already in use", "edit.channel.repo.repolabelinuse",
                new Object[] {repoLabel});
        this.label = repoLabel;
        this.reason = Reason.LABEL_IN_USE;
    }

    /**
     * Creates a new indication that a given repository label is invalid
     *
     * @param repoLabel label the user attempted to give the repository
     * @param reasonIn flag indicating why the repository name is invalid; cannot be
     *                 <code>null</code>
     * @param messageIdIn the string resource message ID
     * @param argIn an optional argument that is associated with messageId.  If there
     * is no argument, pass in an empty string.
     */
    public InvalidRepoLabelException(String repoLabel, Reason reasonIn,
            String messageIdIn, String argIn) {
        super(2, "invalidRepositoryLabel", messageIdIn, new Object[] {argIn});

        this.label = repoLabel;
        this.reason = reasonIn;
    }

    /**
     * @return invalid label that caused this exception; may be <code>null</code>
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return flag indicating what made the label returned from {@link #getLabel()}
     *         invalid; may be <code>null</code>
     */
    public Reason getReason() {
        return reason;
    }

    /**
     * Flags indicating the different reasons that may have caused a repository label
     * to be invalid.
     */
    public enum Reason {
        REGEX_FAILS,
        LABEL_IN_USE
    }

}
