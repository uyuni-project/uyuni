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
package com.redhat.rhn.frontend.xmlrpc.channel.software;

import static com.redhat.rhn.common.ExceptionMessage.NOT_INSTANTIABLE;

/**
 * ChannelSoftwareHandler documentation related constants
 */
public class ChannelSoftwareDoc {

    public static final class ChannelSoftwareService {

        public static final class SyncErrataWithPackages {

            public static final String ADVISORY_NAMES = "advisoryNames";
            public static final String START_DATE = "startDate";
            public static final String END_DATE = "endDate";
            public static final String ALIGN_MODULES = "alignPackageModules";

            private SyncErrataWithPackages() {
                throw new UnsupportedOperationException(NOT_INSTANTIABLE);
            }
        }

        private ChannelSoftwareService() {
            throw new UnsupportedOperationException(NOT_INSTANTIABLE);
        }
    }

    private ChannelSoftwareDoc() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }
}
