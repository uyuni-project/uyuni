/*
 * Copyright (c) 2022 SUSE LLC
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
package org.cobbler.test;

import java.util.List;

import redstone.xmlrpc.XmlRpcInvocationHandler;

public class MockXmlRpcClient implements XmlRpcInvocationHandler {
    @Override
    public Object invoke(String sIn, List listIn) {
        switch (sIn) {
            case "test_invoke_method":
                // This method doesn't exist on Cobbler. Purely for API Client testing.
                return "Test passed";
            case "test_invoke_token_method":
                // This method doesn't exist on Cobbler. Purely for API Client testing.
                if (listIn.get(listIn.size() - 1).equals("my_test_token")) {
                    return true;
                }
                throw new RuntimeException("Token should be present in argument list!");
            case "login":
                return "MyFakeToken";
            case "version":
                return 2.2;
            default:
                return null;
        }
    }
}
