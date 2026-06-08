/*
 * Copyright (c) 2026 SUSE LLC
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

package com.redhat.rhn.frontend.xmlrpc.admin.gpg;

import com.redhat.rhn.frontend.xmlrpc.BaseHandlerTestCase;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AdminGpgHandlerTest extends BaseHandlerTestCase {

    private static final String GPG_KEY = """
-----BEGIN PGP PUBLIC KEY BLOCK-----

mQGNBGoQPzgBDAC1Khdb5ArmeeA1LYkl/VPoa3e58cJhO/DAZWAQml1fpd0YNKgG
yLLvx0t1qvLRUQvTgw/p56kdLMI30BnkHs0gRVhZUEaNJ92IRWNs64oJe9hx9d0I
Df4+uEdEkCDvVDqjP4UgSOrTG4+Ue8f9IH6omAO5o9Mh2TF1Tzdq0fGStUKEEfXA
VRWrtCxjzh/nPiXguNcVB/Vs8uYk/7k1OkA/fJ2z6iUNPkHRZDt6eHq7sepLdwAm
rPUVh6Uac4G7L38gTERfU63p3Ydj+cySUHlqkInaw/rfLbxhuNeHuh3hgn9gEWsj
cUt2tlcXj5aVPie82lHDdmtNChCCDkv2avGGkKR/v3ULb74RZbzWp//qVPpGOVL2
jfz9FST6xTWYPOxlmoyfTy8kHOlp4HA1XN7MqWI4INRB08UKcQsU2UscL7Dm2Zu/
KVqgESSr/HESaFNO0IATK57l12nMisTeE4ID3IlYyEeiSjy38i6QaoePAVeuS4KQ
CAFkxO84vaX00oEAEQEAAbQXVGVzdCBGb28gPHRlc3RAZm9vLm9yZz6JAe0EEwEI
AFcWIQRNi4tu3ulFWvxWIIM8he4Ln8ORiAUCahA/OBsUgAAAAAAEAA5tYW51Miwy
LjUrMS4xMiwyLDICGwEFCwkIBwICIgIGFQoJCAsCBBYCAwECHgcCF4AACgkQPIXu
C5/DkYhv8gv/SG3wlXHV1gN8IkT1eSAMJSpfgxM4vzptSOe8K1DRGSIP9Wy+8gsR
hq0iTjMI1v2rF+43LOFCse1fJLMOUtIk94bggREGG4W/H3VD7sHnTVmZVhmb8pEt
WLp7Pc1ZQ7lQBD7Xkh1GAbJxmBh6qXfGrdvitD6LkjmCLWv3zvbSiQHXg0hRQhTp
LW5vnoAl6Q9FOgLkzWVO6+ofkaFY5BHYbXyAn1XqEQlzp6DS8XpE7e2maD4+G+0m
s2YlUGhWPW7zIq7hLG42ZfUe7GXnuzED4PY712uILRENkUeRyuCJv4VFPys/HVep
veSPT6ixhvkLYd6YNpAOUtuq6lF+0vkUti/TshmSGcPEdWkDb++51lRr92u4PuW7
pcX2hLtS3u1sWJXCcuDk0C3+Fh9Fl+pApZONajOSh8N7D5Kr2dYg/Xy6cWo2gbt4
0PrauVUWcugBl54ac9tF0E5eACjXP5ulpoFe/64qXXpD+zBTKtL5aZsAHdWBTR8M
3hBjht7vw06F
=fTJ4
-----END PGP PUBLIC KEY BLOCK-----""";

    private AdminGpgHandler handler;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        handler = new AdminGpgHandler();
    }

    @Test
    public void testUploadGpgKeyNoPermission() {
        Assertions.assertThrows(PermissionCheckFailureException.class,
                () -> handler.uploadGpgKey(regular, GPG_KEY));
    }

    @Test
    public void testListGpgKeysNoPermission() {
        AdminGpgHandler handlerWithProvider = new AdminGpgHandler();

        Assertions.assertThrows(PermissionCheckFailureException.class,
                () -> handlerWithProvider.listGpgKeys(regular));
    }
}
