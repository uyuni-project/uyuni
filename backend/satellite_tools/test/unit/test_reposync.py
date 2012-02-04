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
from datetime import datetime

from mock import Mock

import spacewalk.satellite_tools.reposync
from spacewalk.common import rhn_rpm

RTYPE = 'yum' # a valid repotype

class RepoSyncTest(unittest.TestCase):

    def setUp(self):
        self.reposync = spacewalk.satellite_tools.reposync

        # kill logging
        self.reposync.rhnLog.initLOG = Mock()

        # don't read configs
        self.reposync.initCFG = Mock()
        self.reposync.CFG = Mock()

        # catching stdout
        # this could be assertRaisesRegexp in python>=2.7. just sayin'
        self.saved_stdout = sys.stdout
        self.stdout = StringIO()
        sys.stdout = self.stdout

        # catching stderr
        self.saved_stderr = sys.stderr
        self.stderr = StringIO()
        sys.stderr = self.stderr

        self.reposync.os = Mock()
        self.reposync.rhnSQL.initDB = Mock()
        self.reposync.rhnSQL.commit = Mock()

        _mock_rhnsql(self.reposync, 'Label')

    def tearDown(self):
        self.stdout.close()
        sys.stdout = self.saved_stdout

        self.stderr.close()
        sys.stderr = self.saved_stderr

        reload(spacewalk.satellite_tools.reposync)
        
    def test_init_succeeds_with_correct_attributes(self):
        rs = self._init_reposync('Label', RTYPE)

        self.assertEqual(rs.channel_label, 'Label')

        # these should have been set automatically
        self.assertEqual(rs.fail, False)
        self.assertEqual(rs.quiet, False)
        self.assertEqual(rs.interactive, True)

    def test_init_with_custom_url(self):
        rs = self._init_reposync('Label', RTYPE, url='http://example.com')

        self.assertEqual(rs.urls, [{'source_url': 'http://example.com',
                                    'id': None,
                                    'metadata_signed': 'N'}])

    def test_init_with_custom_flags(self):
        rs = self._init_reposync('Label', RTYPE, fail=True, quiet=True,
                               noninteractive=True)

        self.assertEqual(rs.fail, True)
        self.assertEqual(rs.quiet, True)
        self.assertEqual(rs.interactive, False)

    def test_init_wrong_url(self):
        """Test generates empty metadata via taskomatic and quits"""
        # the channel shouldn't be found in the database
        _mock_rhnsql(self.reposync, False)
        self.reposync.taskomatic.add_to_repodata_queue_for_channel_package_subscription = Mock()

        self.assertRaises(SystemExit, self.reposync.RepoSync, 'WrongLabel', RTYPE)

        self.assertTrue(self.reposync.taskomatic.
                        add_to_repodata_queue_for_channel_package_subscription.
                        called)

    def test_init_rhnlog(self):
        """Init rhnLog successfully"""
        rs = self._init_reposync('Label', RTYPE)

        self.assertTrue(self.reposync.rhnLog.initLOG.called)

    def test_init_channel(self):
        self.reposync.rhnChannel.channel_info = Mock(return_value=
                                                {'name': 'mocked Channel',
                                                 'id': 1})
        self.reposync.get_compatible_arches = Mock(return_value=['arch1', 'arch2'])

        rs = self.reposync.RepoSync('Label', RTYPE)

        self.assertEqual(rs.channel, {'name': 'mocked Channel', 'id': 1})
        
    def test_init_bad_channel(self):
        self.reposync.rhnChannel.channel_info = Mock(return_value=None)

        self.assertRaises(SystemExit, self.reposync.RepoSync, 'Label', RTYPE)

    def test_init_bad_repo_type(self):
        self.assertRaises(SystemExit, self.reposync.RepoSync, 'Label',
                          'bad-repo-type')
        self.assertEqual("Repository type bad-repo-type is not supported. "
                         "Could not import "
                         "spacewalk.satellite_tools."
                         "repo_plugins.bad-repo-type_src.\n",
                         self.stderr.getvalue())

    def test_sync_success_no_regen(self):
        rs = self._init_reposync()

        rs.urls = [{"source_url": "bogus-url", "id": 42, "metadata_signed": "N"}]

        rs = self._mock_sync(rs)
        rs.sync()

        self.assertEqual(rs.repo_plugin.call_args,
                         (('bogus-url', rs.channel_label, True, False, True),
                          {}))
        self.assertEqual(rs.print_msg.call_args, (("Total time: 0:00:00",), {}))

        self.assertEqual(rs.import_packages.call_args,
                         ((rs.mocked_plugin, 42, "bogus-url"), {}))
        self.assertEqual(rs.import_updates.call_args,
                         ((rs.mocked_plugin, "bogus-url"), {}))
        self.assertEqual(rs.import_products.call_args,
                         ((rs.mocked_plugin,), {}))

        # for the rest just check if they were called or not
        self.assertTrue(rs.update_date.called)
        # these aren't supposed to be called unless self.regen is True
        self.assertFalse(self.reposync.taskomatic.add_to_repodata_queue_for_channel_package_subscription.called)
        self.assertFalse(self.reposync.taskomatic.add_to_erratacache_queue.called)

    def test_sync_success_regen(self):
        rs = self._init_reposync()

        rs.urls = [{"source_url": "bogus-url", "id": 42, "metadata_signed": "N"}]

        rs = self._mock_sync(rs)
        rs.regen = True
        rs.sync()

        # don't test everything we already tested in sync_success_no_regen, just
        # see if the operation was successful
        self.assertEqual(rs.print_msg.call_args, (("Total time: 0:00:00",), {}))

        self.assertEqual(self.reposync.taskomatic.add_to_repodata_queue_for_channel_package_subscription.call_args,
                         ((["Label"], [], "server.app.yumreposync"), {}))
        self.assertEqual(self.reposync.taskomatic.add_to_erratacache_queue.call_args,
                         (("Label", ), {}))

    def test_sync_raises_channel_timeout(self):
        rs = self._create_mocked_reposync()

        exception = self.reposync.ChannelTimeoutException("anony-error")
        rs.repo_plugin = Mock(side_effect=exception)
        rs.sendErrorMail = Mock()

        self.assertRaises(SystemExit, rs.sync)
        self.assertEqual(rs.sendErrorMail.call_args,
                         (("anony-error", ), {}))
        self.assertEqual(rs.print_msg.call_args,
                         ((exception, ), {}))

    def test_sync_raises_unexpected_error(self):
        rs = self._create_mocked_reposync()

        rs.repo_plugin = Mock(side_effect=TypeError)
        rs.sendErrorMail = Mock()
        self.assertRaises(SystemExit, rs.sync)

        error_string = rs.print_msg.call_args[0][0]
        assert (error_string.startswith('Traceback') and
                'TypeError' in error_string), (
            "The error string does not contain the keywords "
            "'Traceback' and 'TypeError':\n %s\n---end of assert" % error_string)
        
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
        bugs = self.reposync._update_bugs(notice)

        bug_values = [set(['id1', 'title1', 'href1']),
                      set(['id2', 'title2', 'href2'])]

        self.assertEqual(len(bugs), 2)
        for bug in bugs:
            self.assertEqual(bug.keys(), ['bug_id', 'href', 'summary'])
            assert set(bug.values()) in bug_values, (
                "Bug set(%s) not in %s" % (bug.values(), bug_values))

    def test_update_cves(self):
        notice = {'references': [{'type': 'cve',
                                  'id': 1},
                                 {'type': 'cve',
                                  'id': 2},
                                 {'type': 'cve',
                                  'id': 2},
                                 {'type': 'this should be skipped'}]}
        cves = self.reposync._update_cve(notice)

        self.assertEqual(cves, [1, 2])

    def test_update_keywords_reboot(self):
        notice = {'reboot_suggested': True,
                  'restart_suggested': False}

        keyword = self.reposync.Keyword()
        keyword.populate({'keyword': 'reboot_suggested'})
        self.assertEqual(self.reposync._update_keywords(notice),
                         [keyword])

    def test_update_keywords_restart(self):
        notice = {'reboot_suggested': False,
                  'restart_suggested': True}

        keyword = self.reposync.Keyword()
        keyword.populate({'keyword': 'restart_suggested'})
        self.assertEqual(self.reposync._update_keywords(notice),
                         [keyword])

    def test_update_keywords_restart_and_reboot(self):
        notice = {'reboot_suggested': True,
                  'restart_suggested': True}

        keyword_restart = self.reposync.Keyword()
        keyword_restart.populate({'keyword': 'restart_suggested'})
        keyword_reboot = self.reposync.Keyword()
        keyword_reboot.populate({'keyword': 'reboot_suggested'})
        self.assertEqual(self.reposync._update_keywords(notice),
                         [keyword_reboot, keyword_restart])

    def test_update_keywords_both_false(self):
        notice = {'reboot_suggested': False,
                  'restart_suggested': False}

        self.assertEqual(self.reposync._update_keywords(notice),
                         [])

    def test_send_error_mail(self):
        self.reposync.rhnMail.send = Mock()
        self.reposync.CFG.TRACEBACK_MAIL = 'recipient'
        self.reposync.hostname = 'testhost'
        rs = self._create_mocked_reposync()

        rs.sendErrorMail('email body')

        self.assertEqual(self.reposync.rhnMail.send.call_args, (
                ({'To': 'recipient',
                  'From': 'testhost <recipient>',
                  'Subject': "SUSE Manager repository sync failed (testhost)"},
                 "Syncing Channel 'Label' failed:\n\nemail body"), {}))

    def test_updates_process_packages_simple(self):
        rs = self._create_mocked_reposync()

        packages = [{'name': 'n1',
                     'version': 'v1',
                     'release': 'r1',
                     'arch': 'arch1',
                     'channel_label': 'l1',
                     'epoch': []},
                    {'name': 'n2',
                     'version': 'v2',
                     'release': 'r2',
                     'arch': 'arch2',
                     'channel_label': 'l2',
                     'epoch': 'e2'}]
        checksum = {'epoch': None,
                    'checksum_type': None,
                    'checksum': None,
                    'id': None}
        
        _mock_rhnsql(self.reposync, checksum)
        processed = rs._updates_process_packages(packages, 'a name', [])
        for p in processed:
            self.assertTrue(isinstance(p, self.reposync.IncompletePackage))

    def test_updates_process_packages_returns_the_right_values(self):
        rs = self._create_mocked_reposync()

        packages = [{'name': 'n1',
                     'version': 'v1',
                     'release': 'r1',
                     'arch': 'arch1',
                     'epoch': []},
                    {'name': 'n2',
                     'version': 'v2',
                     'release': 'r2',
                     'arch': 'arch2',
                     'epoch': 'e2'}]

        checksum = {'epoch': 'cs_epoch',
                    'checksum_type': 'md5',
                    'checksum': '12345',
                    'id': 'cs_package_id'}

        _mock_rhnsql(self.reposync, checksum)
        processed = rs._updates_process_packages(packages, 'patchy', [])

        p1 = self.reposync.IncompletePackage()
        p1.populate({'package_size': None,
                     'name': 'n1',
                     'checksum_list': None,
                     'md5sum': None,
                     'org_id': 'org',
                     'epoch': 'cs_epoch',
                     'channels': None,
                     'package_id': 'cs_package_id',
                     'last_modified': None,
                     'version': 'v1',
                     'checksum_type': 'md5',
                     'release': 'r1',
                     'checksums': {'md5': '12345'},
                     'checksum': '12345',
                     'arch': 'arch1'})
        p2 = self.reposync.IncompletePackage()
        p2.populate({'package_size': None,
                     'name': 'n2',
                     'checksum_list': None,
                     'md5sum': None,
                     'org_id': 'org',
                     'epoch': 'cs_epoch',
                     'channels': None,
                     'package_id': 'cs_package_id',
                     'last_modified': None,
                     'version': 'v2',
                     'checksum_type': 'md5',
                     'release': 'r2',
                     'checksums': {'md5': '12345'},
                     'checksum': '12345',
                     'arch': 'arch2'})
        fixtures = [p1, p2]
        for pkg, fix in zip(processed, fixtures):
            self.assertEqual(pkg, fix)
        
    def test_updates_process_packages_checksum_not_found(self):
        rs = self._create_mocked_reposync()

        packages = [{'name': 'n2',
                     'version': 'v2',
                     'release': 'r2',
                     'arch': 'arch2',
                     'channel_label': 'l2',
                     'epoch': 'e2'}]

        _mock_rhnsql(self.reposync, [])
        self.assertEqual(rs._updates_process_packages(packages, 'patchy', []),
                         [])
        self.assertEqual(rs.print_msg.call_args, (
                ("The package n2-e2:v2-r2.arch2 "
                 "which is referenced by patch patchy was not found "
                 "in the database. This patch has been skipped.", ),{}))

    def test_updates_process_packages_checksum_not_found_no_epoch(self):
        rs = self._create_mocked_reposync()

        packages = [{'name': 'n1',
                     'version': 'v1',
                     'release': 'r1',
                     'arch': 'arch1',
                     'channel_label': 'l1',
                     'epoch': []}]

        _mock_rhnsql(self.reposync, [])
        self.assertEqual(rs._updates_process_packages(packages, 'patchy', []),
                         [])
        self.assertEqual(rs.print_msg.call_args, (
                ("The package n1:v1-r1.arch1 "
                 "which is referenced by patch patchy was not found "
                 "in the database. This patch has been skipped.", ),{}))

    def test_upload_updates_referenced_package_not_found(self):
        timestamp1 = datetime.now().isoformat(' ')
        notices = [{'from': 'from1',
                    'update_id': 'update_id1',
                    'version': 'version1',
                    'type': 'security',
                    'release': 'release1',
                    'description': 'description1',
                    'title': 'title1',
                    'issued': timestamp1, # we mock _to_db_date anyway
                    'updated': timestamp1,
                    'pkglist': [{'packages': []}],
                    'reboot_suggested': False
                    }]
        self.reposync._to_db_date = Mock(return_value=timestamp1)

        # no packages related to this errata makes the ErrataImport be called
        # with an empty list
        self.reposync.RepoSync._updates_process_packages = Mock(return_value=[])
        self.reposync.get_errata = Mock(return_value=None)

        mocked_backend = Mock()
        self.reposync.SQLBackend = Mock(return_value=mocked_backend)
        self.reposync.ErrataImport = Mock()

        rs = self._create_mocked_reposync()
        rs._patch_naming = Mock(return_value='package-name')

        rs.upload_updates(notices)

        self.assertEqual(self.reposync.ErrataImport.call_args,
                         (([], mocked_backend), {}))
        
    def test_associate_package(self):
        pack = self.reposync.ContentPackage()
        pack.setNVREA('name1', 'version1', 'release1', 'epoch1', 'arch1')
        pack.unique_id = 1
        pack.a_pkg = rhn_rpm.RPM_Package(None)
        pack.a_pkg.checksum = 'checksum1'
        pack.a_pkg.checksum_type = 'c_type1'
        pack.checksums[1] = 'checksum1'

        mocked_backend = Mock()
        self.reposync.SQLBackend = Mock(return_value=mocked_backend)
        rs = self._create_mocked_reposync()
        rs._importer_run = Mock()
        rs.channel_label = 'Label1'
        rs.channel = {'id': 'channel1', 'org_id': 'org1'}

        package = {'name': 'name1',
                   'version': 'version1',
                   'release': 'release1',
                   'epoch': 'epoch1',
                   'arch': 'arch1',
                   'checksum': 'checksum1',
                   'checksum_type': 'c_type1',
                   'org_id': 'org1',
                   'channels': [{'label': 'Label1', 'id': 'channel1'}]}

        rs.associate_package(pack)
        self.assertEqual(rs._importer_run.call_args,
                         ((package, 'server.app.yumreposync', mocked_backend),
                          {}))
    def test_best_checksum_item_unknown(self):
        checksums = {'no good checksum': None}

        self.assertEqual(self.reposync._best_checksum_item(checksums),
                         ('md5', None, None))

    def test_best_checksum_item_md5(self):
        checksums = {'md5': '12345'}
        self.assertEqual(self.reposync._best_checksum_item(checksums),
                         ('md5', 'md5', '12345'))

    def test_best_checksum_item_sha1(self):
        checksums = {'sha1': '12345'}
        self.assertEqual(self.reposync._best_checksum_item(checksums),
                         ('sha1', 'sha1', '12345'))

    def test_best_checksum_item_sha(self):
        checksums = {'sha': '12345'}
        self.assertEqual(self.reposync._best_checksum_item(checksums),
                         ('sha1', 'sha', '12345'))

    def test_best_checksum_item_sha256(self):
        checksums = {'sha256': '12345'}
        self.assertEqual(self.reposync._best_checksum_item(checksums),
                         ('sha256', 'sha256', '12345'))

    def test_best_checksum_item_all(self):
        checksums = {'sha1': 'xxx',
                     'sha': 'xxx',
                     'md5': 'xxx',
                     'sha256': '12345'}
        self.assertEqual(self.reposync._best_checksum_item(checksums),
                         ('sha256', 'sha256', '12345'))

    def test_get_errata_no_advisories_found(self):
        _mock_rhnsql(self.reposync, None)
        self.assertEqual(self.reposync.get_errata('bogus'), None)

    def test_get_errata_advisories_but_no_channels(self):
        _mock_rhnsql(self.reposync, [{'id': 42}, []])
        self.assertEqual(self.reposync.get_errata('bogus'),
                         {'channels': [], 'id': 42, 'packages': []})

    def test_get_errata_success(self):
        _mock_rhnsql(self.reposync, [{'id': 42}, ['channel1', 'channel2']])
        self.assertEqual(self.reposync.get_errata('bogus'),
                         {'id': 42, 'channels': ['channel1', 'channel2'],
                          'packages': []})

    def test_get_compat_arches(self):
        _mock_rhnsql(self.reposync, ({'label': 'a1'}, {'label':'a2'}))
        self.assertEqual(self.reposync.get_compatible_arches(None),
                         ['a1', 'a2'])

    def _init_reposync(self, label="Label", repo_type=RTYPE, **kwargs):
        """Initialize the RepoSync object with some mocked attrs"""
        self.reposync.get_compatible_arches = Mock(
            return_value=['arch1', 'arch2'])
        channel = {'org_id':'org', 'id':1, 'arch': 'arch1'}
        self.reposync.RepoSync.load_channel = Mock(return_value=channel)
        rs = self.reposync.RepoSync(label, repo_type, **kwargs)
        return rs

    def _create_mocked_reposync(self):
        """Create a fully mocked RepoSync"""
        rs = self._init_reposync()
        rs.urls = [{"source_url": "bogus-url", "metadata_signed": "N"}]
        rs = self._mock_sync(rs)

        return rs

    def _mock_sync(self, rs):
        """Mock a lot of the methods that are called during sync()

        erratum = reposync.Erratum()
        erratum.populate({'advisory_name': 'update_id1-version1-arch',
                          'advisory': 'update_id1-version1-arch',
                          'product': 'release1',
                          'description': 'description1',
                          'errata_from': 'from1',
                          'locally_modified': None,
                          'refers_to': '',
                          'solution': ' ',
                          'topic': ' ',
                          'last_modified': None,
                          'keywords': [],
                          'packages': [True],
                          'files': [],
                          'advisory_type': 'Security Advisory',
                          'issue_date': timestamp1,
                          'notes': '',
                          'org_id': 'org',
                          'bugs': [],
                          'advisory_rel': 'version1',
                          'synopsis': 'title1',
                          'cve': [],
                          'update_date': timestamp2,
                          'channels': [{'label': 'Label'}]})
        self.assertEqual(reposync.ErrataImport.call_args,
                         (([erratum], mocked_backend), {}))
        :rs: RepoSync object on which we're going to call sync() later

        """
        rs.import_packages = Mock()
        rs.import_updates = Mock()
        rs.import_products = Mock()
        self.reposync.taskomatic.add_to_repodata_queue_for_channel_package_subscription = Mock()
        self.reposync.taskomatic.add_to_erratacache_queue = Mock()

        rs.print_msg = Mock()

        rs.mocked_plugin = Mock()
        rs.mocked_plugin.num_packages = 0
        rs.repo_plugin = Mock(return_value=rs.mocked_plugin)

        rs.update_date = Mock()

        self.reposync.initCFG = Mock()
        return rs

def test_channel_exceptions():
    """Test rasising all the different exceptions when syncing"""
    # the only way to write a test generator with nose is if we put it
    # outside the class, so we have to repeat all the Mocks
    repoSync = spacewalk.satellite_tools.reposync
    repoSync.rhnLog.initLOG = Mock()
    repoSync.CFG = repoSync.initCFG = Mock()
    backup_os = repoSync.os
    repoSync.os = Mock()
    rs = repoSync.RepoSync("Label", RTYPE)
    rs.urls = [{"source_url": "bogus-url", "metadata_signed": "N"}]
    rs.import_packages = Mock()
    rs.import_updates = Mock()
    rs.print_msg = Mock()
    rs.mocked_plugin = Mock()
    rs.repo_plugin = Mock(return_value=rs.mocked_plugin)
    rs.update_date = Mock()
    rs.sendErrorMail = Mock()
    repoSync.os = backup_os

    for exc_class, exc_name in [
        (repoSync.ChannelException, "ChannelException"),
        (repoSync.Errors.YumGPGCheckError, "YumGPGCheckError"),
        (repoSync.Errors.RepoError, "RepoError"),
        (repoSync.Errors.RepoMDError, "RepoMDError")]:
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

        
def _mock_rhnsql(module, return_values):
    """Method to mock the rhnSQL to return something for us

    :module: the module where rhnSQL is called from
    :return_values: a list of the consecutive values that rhnSQL
    fetchall_dict/fetchone_dict will return. If it is just one value,
    then that value will be returned for all calls to those two methods.
    :return_value2: the second rhnSQL fetch will return this value

    rhnSQL's calls are a often a bit more complex. It usually goes
    like this: first an sql statement is prepared, then it is
    executed and then the result is fetched. We need to mock all
    that.

    Here's an example usage:

    query = rhnSQL.prepare('some sql statement')
    query.execute()
    result = query.fetchall_dict()

    """
    def side_effect(*args):
        # Raises or returns each of the values in return_values until exhausted
        # if return_values is not a list, the same value is returned ad infinitum
        if isinstance(return_values, list) and return_values:
            result = return_values.pop(0)
        else:
            result = return_values

        if isinstance(result, Exception):
            raise result
        return result

    # we're making prepare() return an object with methods that
    # return our desired return value
    query = Mock()
    returned_obj = Mock(side_effect=side_effect)
    query.fetchall_dict = query.fetchone_dict = returned_obj

    module.rhnSQL.prepare = Mock(return_value=query)
