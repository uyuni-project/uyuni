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

PROXY_ADDR = "my.proxy.com:1234"
HTTPS_PROXY = "https://my.proxy.com:1234"
HTTP_PROXY = "http://my.proxy.com:1234"
PROXY_USER = "proxy_user"
PROXY_PASS = "proxy_pass"
HTTPS_PROXY_CREDS = "https://user:password@my.proxy.com:1234"


class SuseLibTest(unittest.TestCase):
    @classmethod
    def setUpClass(self):
        self.initCFG = mock.patch("spacewalk.common.suseLib.initCFG")
        self.initCFG.start()

    @classmethod
    def tearDownClass(self):
        self.initCFG.stop()

    def test_get_proxy_from_yast(self):
        with mock.patch('spacewalk.common.suseLib.CFG', http_proxy=None,
                        http_proxy_username=None, http_proxy_password=None):
            with mock_open(suseLib.YAST_PROXY,
                           ' --proxy "%s"\n --proxy-user "%s:%s"'
                           % (HTTPS_PROXY, PROXY_USER, PROXY_PASS)):
                self.assertEqual((HTTPS_PROXY, PROXY_USER, PROXY_PASS),
                                 suseLib.get_proxy())

    def test_get_proxy_from_yast_no_creds(self):
        with mock.patch('spacewalk.common.suseLib.CFG', http_proxy=None,
                        http_proxy_username=None, http_proxy_password=None):
            with mock_open(suseLib.YAST_PROXY,
                           ' --proxy "%s"' % HTTPS_PROXY):
                self.assertEqual((HTTPS_PROXY, None, None), suseLib.get_proxy())

    def test_get_proxy_none(self):
        with mock.patch('spacewalk.common.suseLib.CFG', http_proxy=None,
                        http_proxy_username=None, http_proxy_password=None):
            with mock.patch('spacewalk.common.suseLib.log_debug') as log_debug:
                with mock_open(suseLib.YAST_PROXY):
                    self.assertEqual((None, None, None), suseLib.get_proxy())
                    self.assertIn('Could not read proxy URL from rhn config',
                                  log_debug.call_args_list[0][0][1])
                    self.assertEqual(
                        'Could not read proxy URL from /root/.curlrc',
                        log_debug.call_args_list[1][0][1])
                    self.assertEqual(2, log_debug.call_count)

    def test_get_proxy_rhn_conf_no_creds(self):
        with mock.patch('spacewalk.common.suseLib.CFG', http_proxy=PROXY_ADDR,
                        http_proxy_username=None, http_proxy_password=None):
            self.assertEqual((HTTP_PROXY, None, None), suseLib.get_proxy())

    def test_get_proxy_rhn_conf_creds(self):
        with mock.patch('spacewalk.common.suseLib.CFG', http_proxy=PROXY_ADDR,
                        http_proxy_username=PROXY_USER,
                        http_proxy_password=PROXY_PASS):
            self.assertEqual((HTTP_PROXY, PROXY_USER, PROXY_PASS),
                             suseLib.get_proxy())

    def test_get_proxy_rhn_only_username(self):
        with mock.patch('spacewalk.common.suseLib.CFG', http_proxy=PROXY_ADDR,
                        http_proxy_username='user', http_proxy_password=None):
            self.assertEqual((HTTP_PROXY, 'user', None), suseLib.get_proxy())


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
