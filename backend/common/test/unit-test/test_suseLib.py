# -*- coding: utf-8 -*-
#
# Copyright (c) 2012 Novell
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#

from io import BytesIO
from contextlib import nested
import unittest2 as unittest

import mock

from spacewalk.common import suseLib

HTTPS_PROXY = "https://my.proxy.com:1234"
HTTP_PROXY = "http://my.proxy.com:1234"
PROXY_USER = "user:password"
HTTPS_PROXY_CREDS = "https://user:password@my.proxy.com:1234"


class SuseLibTest(unittest.TestCase):
    @classmethod
    def setUpClass(self):
        # clear the environ, otherwise get_proxy_url might find the
        # $https_proxy/$http_proxy envvars
        self.environ_patch = mock.patch.dict(
            "spacewalk.common.suseLib.os.environ", clear=True)
        self.environ_patch.start()

    @classmethod
    def tearDownClass(self):
        self.environ_patch.stop()

    def test_get_proxy_url_env_https(self):
        with mock.patch.dict("spacewalk.common.suseLib.os.environ",
                             values={"https_proxy": HTTPS_PROXY,
                                     "http_proxy": HTTP_PROXY},
                             clear=True):
            self.assertEqual(HTTPS_PROXY, suseLib.get_proxy_url())

    def test_get_proxy_url_env_http(self):
        with mock.patch.dict("spacewalk.common.suseLib.os.environ",
                             values={"http_proxy": HTTP_PROXY},
                             clear=True):
            self.assertEqual(HTTP_PROXY, suseLib.get_proxy_url())

    def test_get_proxy_url_sysconfig_https(self):
        with mock.patch('spacewalk.common.suseLib.config_file_to_ini',
                        return_value=BytesIO('[main]\n'
                                             'https_proxy = "%s"'
                                             % HTTPS_PROXY)):
            self.assertEqual(HTTPS_PROXY, suseLib.get_proxy_url())

    def test_get_proxy_url_sysconfig_http(self):
        with mock.patch('spacewalk.common.suseLib.config_file_to_ini',
                        return_value=BytesIO('[main]\n'
                                             'http_proxy = "%s"'
                                             % HTTP_PROXY)):
            self.assertEqual(HTTP_PROXY, suseLib.get_proxy_url())

    def test_get_proxy_url_not_in_sysconfig_but_in_yast(self):
        with mock.patch('__builtin__.open',
                        return_value=(BytesIO('--proxy "%s"' % HTTP_PROXY))):
            with mock.patch('spacewalk.common.suseLib.log_debug') as log_debug:
                with mock.patch('spacewalk.common.suseLib.config_file_to_ini',
                                return_value=BytesIO('[main]')):
                    self.assertEqual(HTTP_PROXY, suseLib.get_proxy_url())
                    self.assertIn("No HTTP_PROXY option found",
                                  log_debug.call_args[0][0])

    def test_get_proxy_url_parsing_failed(self):
        with mock.patch('__builtin__.open', return_value=BytesIO()):
            with mock.patch("spacewalk.common.suseLib.log_debug") as log_debug:
                with mock.patch('spacewalk.common.suseLib.config_file_to_ini',
                                return_value=BytesIO('[main]')):
                    self.assertIsNone(suseLib.get_proxy_url())
                    self.assertIn("Could not read proxy URL",
                                  log_debug.call_args[0][0])

    def test_get_proxy_url_credentials(self):
        with mock.patch('__builtin__.open',
                        return_value=(BytesIO('--proxy "%s"'
                                              '\n --proxy-user "%s"'
                                              % (HTTPS_PROXY, PROXY_USER)))):
            with mock.patch('spacewalk.common.suseLib.config_file_to_ini',
                            return_value=BytesIO('[main]')):
                self.assertEqual(HTTPS_PROXY_CREDS, suseLib.get_proxy_url())

    def test_get_proxy_url_without_credentials(self):
        with mock.patch('spacewalk.common.suseLib.config_file_to_ini',
                        return_value=BytesIO("[main]")):
            with mock.patch('__builtin__.open',
                            return_value=BytesIO(' --proxy "%s"\n'
                                                 ' --proxy-user "%s"'
                                                 % (HTTPS_PROXY, PROXY_USER))):
                self.assertEqual(HTTPS_PROXY,
                                 suseLib.get_proxy_url(with_creds=False))

    def test_get_proxy_url_none(self):
        with mock.patch('__builtin__.open', lambda *args: BytesIO()):
            with mock.patch('spacewalk.common.suseLib.log_debug') as log_debug:
                self.assertIsNone(suseLib.get_proxy_url())
                self.assertEqual(
                    'No https_proxy variable found in environment.',
                    log_debug.call_args_list[0][0][0])
                self.assertEqual(
                    'No http_proxy variable found in environment.',
                    log_debug.call_args_list[1][0][0])
                self.assertIn('No HTTPS_PROXY option found in ',
                              log_debug.call_args_list[2][0][0])
                self.assertIn('No HTTP_PROXY option found in ',
                              log_debug.call_args_list[3][0][0])
                self.assertIn('Could not read proxy URL from ',
                              log_debug.call_args_list[4][0][0])

    def test_get_proxy_credentials(self):
        with mock.patch('__builtin__.open',
                        return_value=BytesIO('--proxy-user "%s"'
                                             % PROXY_USER)):
            self.assertEqual(PROXY_USER, suseLib.get_proxy_credentials())

    def test_get_proxy_credentials_file_open_failed(self):
        with mock.patch('__builtin__.open', side_effect=IOError):
            with mock.patch("spacewalk.common.suseLib.log_debug") as log_debug:
                self.assertIsNone(suseLib.get_proxy_credentials())
                log_debug.assert_called()

    def test_get_proxy_credentials_parsing_failed(self):
        with mock.patch('__builtin__.open', return_value=BytesIO()):
            with mock.patch("spacewalk.common.suseLib.log_debug") as log_debug:
                self.assertIsNone(suseLib.get_proxy_credentials())
                log_debug.assert_called()
