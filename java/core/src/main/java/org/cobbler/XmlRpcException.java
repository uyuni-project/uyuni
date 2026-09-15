/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package org.cobbler;


/**
 * Exception that signals that the remote server has thrown an exception.
 *
 * @see <a href="http://xmlrpc.com/spec.md#fault-example">XML-RPC Spec - Fault example</a>
 * @author paji
 */
public class XmlRpcException extends RuntimeException {
    /**
     * This is important for serializing objects back into the memory in many cases. Define this to not use the default
     * computation.
     *
     * @see <a href="https://stackoverflow.com/a/285809/4730773">Stackoverflow</a>
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param messageIn exception message
     */
    public XmlRpcException(String messageIn) {
        super(messageIn);
    }

    /**
     * @param causeIn cause
     */
    public XmlRpcException(Throwable causeIn) {
        super(causeIn);
    }

    /**
     * @param messageIn exception message
     * @param causeIn cause
     */
    public XmlRpcException(String messageIn, Throwable causeIn) {
        super(messageIn, causeIn);
    }

}
