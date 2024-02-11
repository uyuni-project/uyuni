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

package com.redhat.rhn.domain.scc.test;

import static org.jmock.AbstractExpectations.returnValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.domain.credentials.CloudRMTCredentials;
import com.redhat.rhn.domain.credentials.RemoteCredentials;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.domain.scc.SCCRepositoryCloudRmtAuth;
import com.redhat.rhn.testing.MockObjectTestCase;

import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class SCCRepositoryCloudRmtAuthTest extends MockObjectTestCase {

    @BeforeEach
    public void setUp() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @Test
    public void canCombineUrlsCorrectly() {

        SCCRepositoryCloudRmtAuth repoAuth = createAuthFor(
            "https://updates.suse.com/SUSE/Products/RES/8-CLIENT-TOOLS/x86_64/product/",
            1L, "https://smt-ec2.susecloud.net/repo"
        );

        assertEquals(
            "https://smt-ec2.susecloud.net/repo/SUSE/Products/RES/8-CLIENT-TOOLS/x86_64/product/" +
                "?credentials=mirrcred_1",
            repoAuth.getUrl()
        );
    }

    @Test
    public void doesNotDuplicateFolderWhenPathIsRepeated() {

        SCCRepositoryCloudRmtAuth repoAuth = createAuthFor(
            "https://updates.suse.com/repo/$RCE/RES7-SUSE-Manager-Tools/x86_64/",
            1L, "https://smt-ec2.susecloud.net/repo"
        );

        assertEquals(
            "https://smt-ec2.susecloud.net/repo/$RCE/RES7-SUSE-Manager-Tools/x86_64/?credentials=mirrcred_1",
            repoAuth.getUrl()
        );
    }

    @Test
    public void doesNotAlterPathWhenUrlIsExternalToCredentialBase() {

        SCCRepositoryCloudRmtAuth repoAuth = createAuthFor(
            "https://developer.download.nvidia.com/compute/cuda/repos/sles15/sbsa/",
            1L, "https://smt-ec2.susecloud.net/repo"
        );

        assertEquals(
            "https://developer.download.nvidia.com/compute/cuda/repos/sles15/sbsa/",
            repoAuth.getUrl()
        );
    }

    private SCCRepositoryCloudRmtAuth createAuthFor(String repoUrl, long credentialId, String credentialUrl) {
        SCCRepository repository = mock(SCCRepository.class);
        CloudRMTCredentials credentials = mock(CloudRMTCredentials.class);

        SCCRepositoryCloudRmtAuth auth1 = new SCCRepositoryCloudRmtAuth();
        auth1.setCredentials(credentials);
        auth1.setRepo(repository);

        checking(expectations -> {
            expectations.allowing(repository).getUrl();
            expectations.will(returnValue(repoUrl));

            expectations.allowing(credentials).getUrl();
            expectations.will(returnValue(credentialUrl));

            expectations.allowing(credentials).castAs(RemoteCredentials.class);
            expectations.will(returnValue(Optional.of(credentials)));

            expectations.allowing(credentials).getId();
            expectations.will(returnValue(credentialId));
        });
        return auth1;
    }
}
