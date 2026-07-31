/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.common.hibernate;

import java.util.EventListener;

@FunctionalInterface
public interface HibernateCommitListener extends EventListener {
    /**
     * listens to commit transactions in HibernateFactory
     */
    void onCommitTransaction();
}

