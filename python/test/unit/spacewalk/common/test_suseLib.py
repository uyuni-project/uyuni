#  pylint: disable=missing-module-docstring,invalid-name
# -*- coding: utf-8 -*-
#
# Copyright (c) 2012 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#

import unittest
import mock

from spacewalk.common import suseLib

PROXY_ADDR = "my.proxy.com:1234"
HTTPS_PROXY = "https://my.proxy.com:1234"
HTTP_PROXY = "http://my.proxy.com:1234"
PROXY_USER = "proxy_user"
PROXY_PASS = "proxy_pass"
HTTPS_PROXY_CREDS = "https://user:password@my.proxy.com:1234"


class SuseLibTest(unittest.TestCase):
    @classmethod
    # pylint: disable-next=bad-classmethod-argument
    def setUpClass(self):
        self.initCFG = mock.patch("spacewalk.common.suseLib.initCFG")
        self.initCFG.start()

    @classmethod
    # pylint: disable-next=bad-classmethod-argument
    def tearDownClass(self):
        self.initCFG.stop()

    @mock.patch("spacewalk.common.suseLib.CFG")
    @mock.patch("builtins.open")
    def test_get_proxy_from_yast(self, mocked_open, mocked_CFG):
        mocked_CFG.http_proxy = None
        mocked_CFG.http_proxy_username = None
        mocked_CFG.http_proxy_password = None

        mocked_file = mock.MagicMock()
        # pylint: disable-next=consider-using-f-string
        mocked_file.read.return_value = ' --proxy "%s"\n --proxy-user "%s:%s"' % (
            HTTPS_PROXY,
            PROXY_USER,
            PROXY_PASS,
        )
        mocked_file.close.return_value = True

        mocked_open.return_value = mocked_file

        self.assertEqual((HTTPS_PROXY, PROXY_USER, PROXY_PASS), suseLib.get_proxy())

    @mock.patch("spacewalk.common.suseLib.CFG")
    @mock.patch("builtins.open")
    def test_get_proxy_from_yast_no_creds(self, mocked_open, mocked_CFG):
        mocked_CFG.http_proxy = None
        mocked_CFG.http_proxy_username = None
        mocked_CFG.http_proxy_password = None

        mocked_file = mock.MagicMock()
        # pylint: disable-next=consider-using-f-string
        mocked_file.read.return_value = ' --proxy "%s"' % HTTPS_PROXY
        mocked_file.close.return_value = True

        mocked_open.return_value = mocked_file

        self.assertEqual((HTTPS_PROXY, None, None), suseLib.get_proxy())

    @mock.patch("spacewalk.common.suseLib.CFG")
    @mock.patch("spacewalk.common.suseLib.log_debug")
    @mock.patch("builtins.open")
    def test_get_proxy_none(self, mocked_open, mocked_log_debug, mocked_CFG):
        mocked_CFG.http_proxy = None
        mocked_CFG.http_proxy_username = None
        mocked_CFG.http_proxy_password = None

        mocked_file = mock.MagicMock()
        mocked_file.read.return_value = ""
        mocked_file.close.return_value = True

        mocked_open.return_value = mocked_file

        self.assertEqual((None, None, None), suseLib.get_proxy())
        self.assertIn(
            "Could not read proxy URL from rhn config",
            mocked_log_debug.call_args_list[0][0][1],
        )
        self.assertEqual(
            "Could not read proxy URL from /root/.curlrc",
            mocked_log_debug.call_args_list[1][0][1],
        )
        self.assertEqual(2, mocked_log_debug.call_count)

    @mock.patch("spacewalk.common.suseLib.CFG")
    def test_get_proxy_rhn_conf_no_creds(self, mocked_CFG):
        mocked_CFG.http_proxy = PROXY_ADDR
        mocked_CFG.http_proxy_username = None
        mocked_CFG.http_proxy_password = None

        self.assertEqual((HTTP_PROXY, None, None), suseLib.get_proxy())

    @mock.patch("spacewalk.common.suseLib.CFG")
    def test_get_proxy_rhn_conf_creds(self, mocked_CFG):
        mocked_CFG.http_proxy = PROXY_ADDR
        mocked_CFG.http_proxy_username = PROXY_USER
        mocked_CFG.http_proxy_password = PROXY_PASS

        self.assertEqual((HTTP_PROXY, PROXY_USER, PROXY_PASS), suseLib.get_proxy())

    @mock.patch("spacewalk.common.suseLib.CFG")
    def test_get_proxy_rhn_only_username(self, mocked_CFG):
        mocked_CFG.http_proxy = PROXY_ADDR
        mocked_CFG.http_proxy_username = "user"
        mocked_CFG.http_proxy_password = None

        self.assertEqual((HTTP_PROXY, "user", None), suseLib.get_proxy())

    def test_URL_getURL_with_stipPw(self):
        self.assertEqual(
            suseLib.URL("https://example.org/path/to/repo").getURL(stripPw=True),
            "https://example.org/path/to/repo",
        )
        self.assertEqual(
            suseLib.URL("https://example.org/path/to/repo?someTokenToHide").getURL(
                stripPw=True
            ),
            "https://example.org/path/to/repo?<token>",
        )
        self.assertEqual(
            suseLib.URL("https://user:paSSw0rd@example.org/path/to/repo").getURL(
                stripPw=True
            ),
            "https://user:<secret>@example.org/path/to/repo",
        )
        self.assertEqual(
            suseLib.URL(
                "https://user:paSSw0rd@example.org/path/to/repo?someTokenToHide"
            ).getURL(stripPw=True),
            "https://user:<secret>@example.org/path/to/repo?<token>",
        )
