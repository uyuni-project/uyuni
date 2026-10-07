#!/usr/bin/env python
#  pylint: disable=missing-module-docstring,missing-class-docstring
# Copyright (c) 2026 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation.
#

import sys
import unittest
from unittest.mock import MagicMock, Mock, patch

try:
    import OpenSSL  # noqa: F401  pylint: disable=unused-import
except ImportError:
    # rhn.SSL only needs OpenSSL at call time; stub it so the proxy
    # handler module stays importable on minimal test environments.
    sys.modules["OpenSSL"] = MagicMock(name="OpenSSL")
    sys.modules["OpenSSL.crypto"] = MagicMock(name="OpenSSL.crypto")

try:
    import rpm  # noqa: F401  pylint: disable=unused-import
except ImportError:
    # rhnChannel only needs the rpm bindings at call time and every
    # rpm-touching function is mocked below; stub it so the module
    # stays importable where the bindings are unavailable.
    sys.modules["rpm"] = MagicMock(name="rpm")

try:
    import pycurl  # noqa: F401  pylint: disable=unused-import
except ImportError:
    # suseLib only needs pycurl at call time; stub it so the module
    # stays importable where the bindings are unavailable.
    sys.modules["pycurl"] = MagicMock(name="pycurl")

try:
    from defusedxml import xmlrpc  # noqa: F401  pylint: disable=unused-import
except ImportError:
    # rhn.transports only needs defusedxml at call time (pulled in via
    # the handlers.xmlrpc package init); stub it so the module stays
    # importable where the dependency is unavailable.
    defusedxml_stub = MagicMock(name="defusedxml")
    sys.modules["defusedxml"] = defusedxml_stub
    sys.modules["defusedxml.xmlrpc"] = defusedxml_stub.xmlrpc

from spacewalk.common.rhnConfig import CFG
from spacewalk.common.rhnException import rhnFault
from spacewalk.server.handlers.xmlrpc import proxy as proxy_module


class GetKickstartChildChannelTest(unittest.TestCase):
    def _make_handler(self):
        """Build a Proxy handler without running __init__ and stub auth."""
        handler = proxy_module.Proxy.__new__(proxy_module.Proxy)
        handler.auth_system = Mock()
        return handler

    def _restrict_child_channels(self):
        """Enable KS_RESTRICT_CHILD_CHANNELS, auto-restored on exit so the
        shared CFG singleton is never polluted for other tests."""
        return patch.object(CFG, "KS_RESTRICT_CHILD_CHANNELS", 1, create=True)

    def test_restricted_returns_base_channel(self):
        """With KS_RESTRICT_CHILD_CHANNELS set, the child argument is
        ignored and the base kickstart channel info is returned."""
        handler = self._make_handler()
        base_channel = {"label": "base-channel", "last_modified": "20260101000000"}
        with self._restrict_child_channels(), patch.object(
            proxy_module.rhnChannel,
            "getChannelInfoForKickstart",
            return_value=base_channel,
        ) as base_lookup, patch.object(
            proxy_module.rhnChannel, "getChildChannelInfoForKickstart"
        ) as child_lookup:
            ret = handler.getKickstartChildChannel("ks-tree", "child-channel", 123)
        self.assertEqual(base_channel, ret)
        base_lookup.assert_called_once_with("ks-tree")
        child_lookup.assert_not_called()

    def test_unrestricted_uses_child_lookup(self):
        """Without the flag, the requested child channel info is returned."""
        handler = self._make_handler()
        child_channel = {"label": "child-channel", "last_modified": "20260101000000"}
        with patch.object(
            proxy_module.rhnChannel,
            "getChildChannelInfoForKickstart",
            return_value=child_channel,
        ) as child_lookup:
            ret = handler.getKickstartChildChannel("ks-tree", "child-channel", 123)
        self.assertEqual(child_channel, ret)
        child_lookup.assert_called_once_with("ks-tree", "child-channel")

    def test_missing_channel_raises_fault(self):
        """A falsy lookup result raises rhnFault(40) like the siblings."""
        handler = self._make_handler()
        with self._restrict_child_channels(), patch.object(
            proxy_module.rhnChannel, "getChannelInfoForKickstart", return_value=None
        ):
            with self.assertRaises(rhnFault) as ctx:
                handler.getKickstartChildChannel("ks-tree", "child-channel", 123)
        self.assertEqual(40, ctx.exception.code)


if __name__ == "__main__":
    unittest.main()
