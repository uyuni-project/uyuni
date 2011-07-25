#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright (c) 2011 SUSE LINUX Products GmbH, Nuernberg, Germany.
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

import sys
import unittest
from StringIO import StringIO

from mock import Mock

from spacewalk.satellite_tools import reposync

RTYPE = 'yum' # a valid repotype

# kill logging
reposync.rhnLog.log_clean = Mock()

class RepoSyncTest(unittest.TestCase):

    def setUp(self):
        # catching stdout
        # this could be assertRaisesRegexp in python>=2.7. just sayin'
        self.saved_stdout = sys.stdout
        self.stdout = StringIO()
        sys.stdout = self.stdout

        # catching stderr
        self.saved_stderr = sys.stderr
        self.stderr = StringIO()
        sys.stderr = self.stderr

        reposync.os = Mock()
        reposync.rhnSQL.initDB = Mock()
        reposync.rhnSQL.commit = Mock()

        _mock_rhnsql(reposync, 'Label')

    def tearDown(self):
        self.stdout.close()
        sys.stdout = self.saved_stdout

        self.stderr.close()
        sys.stderr = self.saved_stderr
        
    def test_init_succeeds_with_correct_attributes(self):
        rs = reposync.RepoSync('Label', RTYPE)

        self.assertEqual(rs.channel_label, 'Label')

        # these should have been set automatically
        self.assertEqual(rs.fail, False)
        self.assertEqual(rs.quiet, False)
        self.assertEqual(rs.interactive, True)

    def test_init_with_custom_url(self):
        rs = reposync.RepoSync('Label', RTYPE, url='http://example.com')

        self.assertEqual(rs.urls, [{'source_url': 'http://example.com',
                                    'metadata_signed': 'N'}])

    def test_init_with_custom_flags(self):
        rs = reposync.RepoSync('Label', RTYPE, fail=True, quiet=True,
                               noninteractive=True)

        self.assertEqual(rs.fail, True)
        self.assertEqual(rs.quiet, True)
        self.assertEqual(rs.interactive, False)

    def test_init_wrong_url(self):
        """Test generates empty metadata via taskomatic and quits"""
        # the channel shouldn't be found in the database
        _mock_rhnsql(reposync, False)
        reposync.taskomatic.add_to_repodata_queue_for_channel_package_subscription = Mock()

        self.assertRaises(SystemExit, reposync.RepoSync, 'WrongLabel', RTYPE)

        self.assertTrue(reposync.taskomatic.
                        add_to_repodata_queue_for_channel_package_subscription.
                        called)

    def test_init_rhnlog(self):
        """Init rhnLog successfully"""
        reposync.rhnLog.initLOG = Mock()

        rs = reposync.RepoSync('Label', RTYPE)

        self.assertTrue(reposync.rhnLog.initLOG.called)

    def test_init_channel(self):
        reposync.rhnChannel.channel_info = Mock(return_value=
                                                {'name': 'mocked Channel'})

        rs = reposync.RepoSync('Label', RTYPE)

        self.assertEqual(rs.channel, {'name': 'mocked Channel'})
        
    def test_init_bad_channel(self):
        reposync.rhnChannel.channel_info = Mock(return_value=None)

        self.assertRaises(SystemExit, reposync.RepoSync, 'Label', RTYPE)

    def test_init_bad_repo_type(self):
        self.assertRaises(SystemExit, reposync.RepoSync, 'Label',
                          'bad-repo-type')
        self.assertEqual("Repository type bad-repo-type is not supported. "
                         "Could not import "
                         "spacewalk.satellite_tools."
                         "repo_plugins.bad-repo-type_src.\n",
                         self.stderr.getvalue())

    def test_sync_success_no_regen(self):
        rs = reposync.RepoSync("Label", RTYPE)

        rs.urls = [{"source_url": "bogus-url", "metadata_signed": "N"}]

        rs = _mock_sync(rs)
        rs.sync()

        self.assertEqual(rs.repo_plugin.call_args,
                         (('bogus-url', rs.channel_label, True, False, True),
                          {'proxy_pass': rs.mocked_proxy_pass,
                           'proxy_user': rs.mocked_proxy_user,
                           'proxy': rs.mocked_proxy}))
        self.assertEqual(rs.print_msg.call_args, (("Sync complete",), {}))

        self.assertEqual(rs.import_packages.call_args,
                         ((rs.mocked_plugin, "bogus-url"), {}))
        self.assertEqual(rs.import_updates.call_args,
                         ((rs.mocked_plugin, "bogus-url"), {}))

        # for the rest just check if they were called or not
        self.assertTrue(rs.update_date.called)
        # these aren't supposed to be called unless self.regen is True
        self.assertFalse(reposync.taskomatic.add_to_repodata_queue_for_channel_package_subscription.called)
        self.assertFalse(reposync.taskomatic.add_to_erratacache_queue.called)

    def test_sync_success_regen(self):
        rs = reposync.RepoSync("Label", RTYPE)

        rs.urls = [{"source_url": "bogus-url", "metadata_signed": "N"}]

        rs = _mock_sync(rs)
        rs.regen = True
        rs.sync()

        # don't test everything we already tested in sync_success_no_regen, just
        # see if the operation was successful
        self.assertEqual(rs.print_msg.call_args, (("Sync complete",), {}))

        self.assertEqual(reposync.taskomatic.add_to_repodata_queue_for_channel_package_subscription.call_args, ((["Label"], [], "server.app.yumreposync"), {}))
        self.assertEqual(reposync.taskomatic.add_to_erratacache_queue.call_args,
                         (("Label", ), {}))

    def test_sync_raises_channel_timeout(self):
        rs = _create_mocked_reposync()

        exception = reposync.ChannelTimeoutException("anony-error")
        rs.repo_plugin = Mock(side_effect=exception)
        rs.sendErrorMail = Mock()

        self.assertRaises(SystemExit, rs.sync)
        self.assertEqual(rs.sendErrorMail.call_args,
                         (("anony-error", ), {}))
        self.assertEqual(rs.print_msg.call_args,
                         ((exception, ), {}))

    def test_update_bugs(self):
        notice = {'references': [{'type': 'bugzilla',
                                  'id': 'id1',
                                  'title': 'title1',
                                  'href': 'href1'},
                                 {'type': 'bugzilla',
                                  'id': 'id2',
                                  'title': 'title2',
                                  'href': 'href2'},
                                 {'type': 'bugzilla',
                                  'id': 'id2',
                                  'title': 'duplicate_id',
                                  'href': 'duplicate_id'},
                                 {'type': 'godzilla',
                                  'this': 'should be skipped'}]}
        bugs = reposync._update_bugs(notice)

        bug_values = [set(['id1', 'title1', 'href1']),
                      set(['id2', 'title2', 'href2'])]

        self.assertEqual(len(bugs), 2)
        for bug in bugs:
            self.assertEqual(bug.keys(), ['bug_id', 'href', 'summary'])
            assert set(bug.values()) in bug_values, "Bug set(%s) not in %s" % (
                bug.values(), bug_values)

    def test_update_cves(self):
        notice = {'references': [{'type': 'cve',
                                  'id': 1},
                                 {'type': 'cve',
                                  'id': 2},
                                 {'type': 'cve',
                                  'id': 2},
                                 {'type': 'this should be skipped'}]}
        cves = reposync._update_cve(notice)

        self.assertEqual(cves, [1, 2])

    def test_update_keywords_reboot(self):
        notice = {'reboot_suggested': True,
                  'restart_suggested': False}

        keyword = reposync.Keyword()
        keyword.populate({'keyword': 'reboot_suggested'})
        self.assertEqual(reposync._update_keywords(notice),
                         [keyword])

    def test_update_keywords_restart(self):
        notice = {'reboot_suggested': False,
                  'restart_suggested': True}

        keyword = reposync.Keyword()
        keyword.populate({'keyword': 'restart_suggested'})
        self.assertEqual(reposync._update_keywords(notice),
                         [keyword])

    def test_update_keywords_restart_and_reboot(self):
        notice = {'reboot_suggested': True,
                  'restart_suggested': True}

        keyword_restart = reposync.Keyword()
        keyword_restart.populate({'keyword': 'restart_suggested'})
        keyword_reboot = reposync.Keyword()
        keyword_reboot.populate({'keyword': 'reboot_suggested'})
        self.assertEqual(reposync._update_keywords(notice),
                         [keyword_reboot, keyword_restart])

    def test_update_keywords_both_false(self):
        notice = {'reboot_suggested': False,
                  'restart_suggested': False}

        self.assertEqual(reposync._update_keywords(notice),
                         [])

    def test_send_error_mail(self):
        reposync.rhnMail.send = Mock()
        reposync.CFG.TRACEBACK_MAIL = 'recipient'
        reposync.HOSTNAME = 'testhost'
        rs = _create_mocked_reposync()

        rs.sendErrorMail('email body')

        self.assertEqual(reposync.rhnMail.send.call_args, (
                ({'To': 'recipient',
                  'From': 'testhost <recipient>',
                  'Subject': "SUSE Manager repository sync failed (testhost)"},
                 "Syncing Channel 'Label' failed:\n\nemail body"), {}))
        

def test_channel_exceptions():
    """Test rasising all the different exceptions when syncing"""
    reposync.RepoSync.sendErrorMail = backup = Mock()
    rs = _create_mocked_reposync()
    reposync.RepoSync.sendErrorMail = backup

    for exc_class, exc_name in [
        (reposync.ChannelException, "ChannelException"),
        (reposync.Errors.YumGPGCheckError, "YumGPGCheckError"),
        (reposync.Errors.RepoError, "RepoError"),
        (reposync.Errors.RepoMDError, "RepoMDError")]:
        yield check_channel_exceptions, rs, exc_class, exc_name

def check_channel_exceptions(rs, exc_class, exc_name):
    # since this isn't a subclass of unittest.TestCase we can't use
    # unittest's assertions
    from nose.tools import assert_raises, assert_equal
    rs.repo_plugin = Mock(side_effect=exc_class("error msg"))

    assert_raises(SystemExit, rs.sync)
    assert_equal(rs.sendErrorMail.call_args,
                 (("%s: %s" % (exc_name, "error msg"), ), {}))
    assert_equal(rs.print_msg.call_args,
                 (("%s: %s" % (exc_name, "error msg"), ), {}))

def _create_mocked_reposync():
    rs = reposync.RepoSync("Label", RTYPE)
    rs.urls = [{"source_url": "bogus-url", "metadata_signed": "N"}]
    rs = _mock_sync(rs)

    return rs

def _mock_sync(rs):
    """Mock a lot of the methods that are called during sync()

    :rs: RepoSync object on which we're going to call sync() later

    """
    rs.import_packages = Mock()
    rs.import_updates = Mock()
    reposync.taskomatic.add_to_repodata_queue_for_channel_package_subscription = Mock()
    reposync.taskomatic.add_to_erratacache_queue = Mock()
    rs.print_msg = Mock()

    rs.mocked_plugin = Mock()
    rs.repo_plugin = Mock(return_value=rs.mocked_plugin)

    rs.update_date = Mock()
    reposync.initCFG = Mock()
    reposync.CFG.HTTP_PROXY = rs.mocked_proxy = Mock()
    reposync.CFG.HTTP_PROXY_USERNAME = rs.mocked_proxy_user = Mock()
    reposync.CFG.HTTP_PROXY_PASSWORD = rs.mocked_proxy_pass = Mock()
    return rs

def _mock_rhnsql(module, return_value):
    """Method to mock the rhnSQL to return something for us

    :module: the module where rhnSQL is called from
    :return_value: the value or object that rhnSQL's fetches should return

    rhnSQL's calls are a often a bit more complex. It usually goes
    like this: first an sql statement is prepared, then it is
    executed and then the result is fetched. We need to mock all
    that.

    Here's an example usage:

    query = rhnSQL.prepare('some sql statement')
    query.execute()
    result = query.fetchall_dict()

    """
    # we're making prepare() return an object with methods that
    # return our desired return value
    query = Mock()
    returned_obj = Mock(return_value=return_value)
    query.fetchall_dict = query.fetchone_dict = returned_obj

    module.rhnSQL.prepare = Mock(return_value=query)
