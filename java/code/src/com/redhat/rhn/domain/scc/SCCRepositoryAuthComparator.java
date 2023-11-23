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

package com.redhat.rhn.domain.scc;

import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsType;
import com.redhat.rhn.domain.credentials.RemoteCredentials;
import com.redhat.rhn.domain.credentials.SCCCredentials;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Sorts {@link RemoteCredentials} based on the type following the principle of locality:
 *
 * local -> rmt if valid -> scc with primary first
 *
 * When the locality is the same, the id is used for stability.
 */
public class SCCRepositoryAuthComparator implements Comparator<SCCRepositoryAuth> {

    // This list specify the order in which we want to use the credentials
    private static final List<CredentialsType> PRIORITY_LIST = Stream.of(
        null,                        // null means local mirror
        CredentialsType.CLOUD_RMT,   // Cloud RMT PAYG credentials
        CredentialsType.SCC          // Standard SCC credentials
    ).collect(Collectors.toList());

    @Override
    public int compare(SCCRepositoryAuth auth1, SCCRepositoryAuth auth2) {
        // First sort by validity and type priority
        int result = Comparator.comparing(SCCRepositoryAuthComparator::getValidity).reversed()
            .thenComparing(SCCRepositoryAuthComparator::getPriority)
            .compare(auth1, auth2);

        if (result != 0) {
            return result;
        }

        // Credentials under comparison are of the same type or both null at this point
        CredentialsType type = auth1.getOptionalCredentials().map(Credentials::getType).orElse(null);
        if (type == CredentialsType.SCC) {
            // Prefer primary credentials for SCC
            return Comparator.comparing(SCCRepositoryAuthComparator::isSCCPrimary).reversed()
                .thenComparing(SCCRepositoryAuth::getId)
                .compare(auth1, auth2);
        }

        // Use the id ordering to ensure first created takes preference
        return Comparator.comparing(SCCRepositoryAuth::getId).compare(auth1, auth2);
    }

    private static boolean getValidity(SCCRepositoryAuth auth) {
        return auth.getOptionalCredentials().map(Credentials::isValid).orElse(true);
    }

    private static int getPriority(SCCRepositoryAuth auth) {
        CredentialsType type = auth.getOptionalCredentials().map(Credentials::getType).orElse(null);

        int index = PRIORITY_LIST.indexOf(type);
        if (index == -1) {
            throw new IllegalArgumentException("Unsupported credential type " + type);
        }

        return index;
    }

    private static boolean isSCCPrimary(SCCRepositoryAuth auth) {
        return auth.getOptionalCredentials()
            .flatMap(cred -> cred.castAs(SCCCredentials.class))
            .map(SCCCredentials::isPrimary)
            .orElseThrow(() -> new IllegalStateException("We should be comparing only SCCCredentials"));
    }
}
