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
from contextlib import contextmanager
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
        self.environ = mock.patch.dict(
            "spacewalk.common.suseLib.os.environ", clear=True)
        self.environ.start()

        self.initCFG = mock.patch("spacewalk.common.suseLib.initCFG")
        self.initCFG.start()

        self.CFG = mock.patch("spacewalk.common.suseLib.CFG", http_proxy=None)
        self.CFG.start()

    @classmethod
    def tearDownClass(self):
        self.environ.stop()
        self.initCFG.stop()
        self.CFG.stop()

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
        with mock_open(suseLib.SYS_PROXY,
                       'PROXY_ENABLEd="yes"\nHTTPS_PROXY = "%s"' % HTTPS_PROXY):
            with mock_open(suseLib.YAST_PROXY):
                self.assertEqual(HTTPS_PROXY, suseLib.get_proxy_url())

    def test_get_proxy_url_sysconfig_http(self):
        with mock_open(suseLib.YAST_PROXY):
            with mock_open(suseLib.SYS_PROXY,
                           'PROXY_ENABLED="yes"\nHTTP_PROXY="%s"'
                           % HTTP_PROXY):
                    self.assertEqual(HTTP_PROXY, suseLib.get_proxy_url())

    def test_get_proxy_url_sysconfig_proxy_disabled(self):
        with mock.patch('spacewalk.common.suseLib.log_debug') as log_debug:
            with mock_open(suseLib.YAST_PROXY):
                with mock_open(suseLib.SYS_PROXY,
                               'PROXY_ENABLED="no"\nHTTP_PROXY="%s"'
                               % HTTP_PROXY):
                    self.assertIsNone(suseLib.get_proxy_url())
                    self.assertEqual("Proxy is disabled in sysconfig.",
                                     log_debug.call_args[0][0])

    def test_get_proxy_url_not_in_yast_but_in_sysconfig(self):
        with mock_open(suseLib.YAST_PROXY):
            with mock_open(suseLib.SYS_PROXY,
                           'PROXY_ENABLED="yes"\nHTTP_PROXY="%s"' % HTTP_PROXY):
                self.assertEqual(HTTP_PROXY, suseLib.get_proxy_url())

    def test_get_proxy_url_credentials(self):
        with mock_open(suseLib.YAST_PROXY,
                       '--proxy "%s"\n --proxy-user "%s"'
                       % (HTTPS_PROXY, PROXY_USER)):
            self.assertEqual(HTTPS_PROXY_CREDS, suseLib.get_proxy_url())

    def test_get_proxy_url_without_credentials(self):
        with mock_open(suseLib.YAST_PROXY,
                       ' --proxy "%s"\n --proxy-user "%s"'
                       % (HTTPS_PROXY, PROXY_USER)):
            self.assertEqual(HTTPS_PROXY,
                             suseLib.get_proxy_url(with_creds=False))

    def test_get_proxy_url_none(self):
        with mock.patch('spacewalk.common.suseLib.log_debug') as log_debug:
            with mock_open(suseLib.YAST_PROXY):
                with mock_open(suseLib.SYS_PROXY):
                    self.assertIsNone(suseLib.get_proxy_url())
                    self.assertEqual(
                        'No https_proxy variable found in environment.',
                        log_debug.call_args_list[0][0][0])
                    self.assertEqual(
                        'No http_proxy variable found in environment.',
                        log_debug.call_args_list[1][0][0])
                    self.assertIn('Could not read proxy URL from ',
                                  log_debug.call_args_list[2][0][0])
                    self.assertEqual(
                        'Proxy is disabled in sysconfig.',
                        log_debug.call_args_list[3][0][0])
                    self.assertEqual(4, log_debug.call_count)

    def test_get_proxy_url_rhn_conf_no_creds(self):
        with mock.patch('spacewalk.common.suseLib.CFG', http_proxy=HTTP_PROXY,
                        http_proxy_username=None, http_proxy_password=None):
            self.assertEqual(HTTP_PROXY, suseLib.get_proxy_url())

    def test_get_proxy_url_rhn_conf_creds(self):
        with mock.patch('spacewalk.common.suseLib.CFG', http_proxy=HTTPS_PROXY,
                        http_proxy_username='user',
                        http_proxy_password='password'):
            self.assertEqual(HTTPS_PROXY_CREDS, suseLib.get_proxy_url())

    def test_get_proxy_url_rhn_only_username(self):
        with mock.patch('spacewalk.common.suseLib.CFG', http_proxy=HTTPS_PROXY,
                        http_proxy_username='user', http_proxy_password=None):
            self.assertEqual(HTTPS_PROXY, suseLib.get_proxy_url())

    def test_get_proxy_credentials(self):
        with mock_open(suseLib.YAST_PROXY, '--proxy-user "%s"' % PROXY_USER):
            self.assertEqual(PROXY_USER, suseLib.get_proxy_credentials())

    def test_get_proxy_credentials_file_open_failed(self):
        with mock.patch('__builtin__.open', side_effect=IOError):
            with mock.patch("spacewalk.common.suseLib.log_debug") as log_debug:
                self.assertIsNone(suseLib.get_proxy_credentials())
                log_debug.assert_called()

    def test_get_proxy_credentials_parsing_failed(self):
        with mock_open(suseLib.YAST_PROXY):
            with mock.patch("spacewalk.common.suseLib.log_debug") as log_debug:
                self.assertIsNone(suseLib.get_proxy_credentials())
                log_debug.assert_called()


@contextmanager
def mock_open(filename, contents=None, complain=True):
    """Mock __builtin__.open() on a specific filename

    Let execution pass through to __builtin__.open() on other
    files. Return a BytesIO with :contents: if the file was matched. If
    the :contents: parameter is not given or if it None, a BytesIO
    instance simulating an empty file is returned.

    If :complain: is True (default). Will raise an error if a
    __builtin__.open was called with a file that was not mocked.

    """
    open_files = []  # simulate non-local scope with mutable lists
    def mock_file(*args):
        if args[0] == filename:
            r = BytesIO(contents)
            r.name = filename
        else:
            mocked_file.stop()
            r = open(*args)
            mocked_file.start()

        open_files.append(r)
        return r

    mocked_file = mock.patch('__builtin__.open', mock_file)
    mocked_file.start()
    try:
        yield
    except NotMocked as e:
        if e.filename != filename:
            raise
    mocked_file.stop()

    found = False
    for f in open_files:
        f.close()
        if f.name == filename:
            found = f
        elif complain:
            raise NotMocked(f.name)

    if not found:
        raise AssertionError("The file %s was not opened." % filename)

class NotMocked(Exception):
    def __init__(self, filename):
        super(NotMocked, self).__init__(
            "The file %s was opened, but not mocked." % filename)
        self.filename = filename
