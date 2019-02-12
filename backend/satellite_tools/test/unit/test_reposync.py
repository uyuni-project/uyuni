#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright (c) 2011 SUSE LLC
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

import imp
import sys
import unittest
import json
try:
    from io import StringIO
except ImportError:
    from StringIO import StringIO
from datetime import datetime, timedelta

from mock import Mock, patch, call

import spacewalk.satellite_tools.reposync
from spacewalk.satellite_tools.repo_plugins import ContentPackage
from spacewalk.satellite_tools.repo_plugins import yum_src
from spacewalk.server.importlib import importLib

from spacewalk.common import rhn_rpm

RTYPE = 'yum' # a valid repotype

class RepoSyncTest(unittest.TestCase):

    def setUp(self):
        self.reposync = spacewalk.satellite_tools.reposync

        # kill logging
        self.reposync.rhnLog.initLOG = Mock()

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

        _mock_rhnsql(
            self.reposync,
            [
              [{'id': 'id1', 'repo_label': 'label1', 'source_url': 'http://url.one', 'metadata_signed': 'Y', 'repo_type': 'yum'}],
              [{'id': 'id2', 'repo_label': 'label2', 'source_url': 'http://url.two', 'metadata_signed': 'N', 'repo_type': 'yum'}],
            ]
        )

    def tearDown(self):
        self.stdout.close()
        sys.stdout = self.saved_stdout

        self.stderr.close()
        sys.stderr = self.saved_stderr

        imp.reload(spacewalk.satellite_tools.reposync)

    def test_init_succeeds_with_correct_attributes(self):
        rs = self._init_reposync('Label', RTYPE)

        self.assertEqual(rs.channel_label, 'Label')

        # these should have been set automatically
        self.assertEqual(rs.fail, False)
        self.assertEqual(rs.interactive, True)

    def test_init_with_custom_url(self):
        rs = self._init_reposync('Label', RTYPE, url='http://example.com')

        self.assertEqual(rs.urls, [{'source_url': 'http://example.com',
                                    'repo_label': None,
                                    'id': None,
                                    'metadata_signed': 'N',
                                    'repo_type': 'yum'
                                  }])

    def test_init_with_custom_flags(self):
        rs = self._init_reposync('Label', RTYPE, fail=True, noninteractive=True)

        self.assertEqual(rs.fail, True)
        self.assertEqual(rs.interactive, False)

    def test_init_wrong_url(self):
        """Test generates empty metadata via taskomatic and quits"""
        # the channel shouldn't be found in the database
        _mock_rhnsql(self.reposync, False)
        self.reposync.taskomatic.add_to_repodata_queue_for_channel_package_subscription = Mock()

        channel = {'org_id':1, 'id':1, 'arch': 'arch1'}
        self.reposync.RepoSync.load_channel = Mock(return_value=channel)

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
                                                 'id': 1,
                                                 'org_id': 1})
        self.reposync.RepoSync.get_compatible_arches = Mock(return_value=['arch1', 'arch2'])

        rs = self.reposync.RepoSync('Label', RTYPE)

        self.assertEqual(rs.channel, {'name': 'mocked Channel', 'id': 1, 'org_id': 1})

    def test_init_bad_channel(self):
        self.reposync.rhnChannel.channel_info = Mock(return_value=None)

        self.assertRaises(SystemExit, self.reposync.RepoSync, 'Label', RTYPE)

    def test_bad_repo_type(self):
        rs = self._init_reposync('Label', RTYPE)
        self.assertRaises(SystemExit, rs.load_plugin, 'bad-repo-type')
        self.assertIn("Repository type bad-repo-type is not supported. "
                      "Could not import "
                      "spacewalk.satellite_tools."
                      "repo_plugins.bad-repo-type_src.\n",
                      self.stderr.getvalue())

    def test_sync_success_no_regen(self):
        rs = self._init_reposync()

        rs.urls = [
          {"source_url": ["http://none.host/bogus-url"], "id": 42, "metadata_signed": "N", "repo_label": None, 'repo_type': 'yum'}]

        _mock_rhnsql(self.reposync, {})
        rs = self._mock_sync(rs)
        rs.sync()

        self.assertEqual(rs.repo_plugin.call_args[0],
                (('http://none.host/bogus-url', 'bogus-url', True, True)))

        self.assertEqual(rs.import_packages.call_args,
                ((rs.mocked_plugin, 42, "http://none.host/bogus-url", 1), {}))
        self.assertEqual(rs.import_updates.call_args,
                ((rs.mocked_plugin,), {}))
        self.assertEqual(rs.import_products.call_args,
                ((rs.mocked_plugin,), {}))

        # for the rest just check if they were called or not
        self.assertTrue(rs.update_date.called)
        # these aren't supposed to be called unless self.regen is True
        self.assertFalse(self.reposync.taskomatic.add_to_repodata_queue_for_channel_package_subscription.called)
        self.assertFalse(self.reposync.taskomatic.add_to_erratacache_queue.called)

    def test_sync_success_regen(self):
        rs = self._init_reposync()

        rs.urls = [{"source_url": ["http://none.host/bogus-url"], "id": 42, "metadata_signed": "N", "repo_label": None, 'repo_type': 'yum'}]

        _mock_rhnsql(self.reposync, {})
        rs = self._mock_sync(rs)
        rs.regen = True
        rs.sync()

        self.assertEqual(self.reposync.taskomatic.add_to_repodata_queue_for_channel_package_subscription.call_args,
                         ((["Label"], [], "server.app.yumreposync"), {}))
        self.assertEqual(self.reposync.taskomatic.add_to_erratacache_queue.call_args,
                         (("Label", ), {}))

    def test_sync_raises_channel_timeout(self):
        rs = self._create_mocked_reposync()

        exception = self.reposync.ChannelTimeoutException("anony-error")
        rs.load_plugin = Mock(return_value=Mock(side_effect=exception))
        rs.sendErrorMail = Mock()

        etime, ret = rs.sync()
        self.assertEqual(-1, ret)
        self.assertEqual(rs.sendErrorMail.call_args,
                         (("anony-error", ), {}))
        self.assertEqual(self.reposync.log.call_args[0][1], exception)

    def test_sync_raises_unexpected_error(self):
        rs = self._create_mocked_reposync()

        rs.load_plugin = Mock(return_value=Mock(side_effect=TypeError))
        rs.sendErrorMail = Mock()
        etime, ret = rs.sync()
        self.assertEqual(-1, ret)

        error_string = self.reposync.log.call_args[0][1]
        assert (error_string.startswith('Traceback') and
                'TypeError' in error_string), (
            "The error string does not contain the keywords "
            "'Traceback' and 'TypeError':\n %s\n---end of assert" % error_string)

    def test_update_bugs(self):
        notice = {'references': [{'type': 'bugzilla',
                                  'id': '12345',
                                  'title': 'title1',
                                  'href': 'href1'},
                                 {'type': 'bugzilla',
                                  'id': 'string_id',
                                  'title': 'title2',
                                  'href': 'href2',
                                  'this': 'non-integer bz ids should be skipped'},
                                 {'type': 'bugzilla',
                                  'id': 'string_id',
                                  'title': 'title3',
                                  'href': 'http://dummyhost/show_bug.cgi?id=11111',
                                  'this': 'bz id parsed from href'},
                                 {'type': 'bugzilla',
                                  'id': '54321',
                                  'title': 'title2',
                                  'href': 'href2'},
                                 {'type': 'bugzilla',
                                  'id': '54321',
                                  'title': 'duplicate_id',
                                  'href': 'duplicate_id'},
                                 {'type': 'godzilla',
                                  'this': 'should be skipped'}]}
        bugs = self.reposync.RepoSync._update_bugs(notice)

        bug_values = [set(['12345', 'title1', 'href1']),
                      set(['54321', 'title2', 'href2']),
                      set(['11111', 'title3', 'http://dummyhost/show_bug.cgi?id=11111'])]

        self.assertEqual(len(bugs), 3)
        for bug in bugs:
            self.assertCountEqual(list(bug.keys()), ['bug_id', 'href', 'summary'])
            assert set(bug.values()) in bug_values, (
                "Bug set(%s) not in %s" % (list(bug.values()), bug_values))

    def test_update_cves(self):
        notice = {'references': [{'type': 'cve',
                                  'id': "CVE-1234-5678"},
                                 {'type': 'cve',
                                  'id': "CVE-1234-123456"},
                                 {'type': 'cve',
                                  'id': "CVE-1234-5678"},
                                 {'type': 'this should be skipped'}],
                  'description': None}
        cves = self.reposync.RepoSync._update_cve(notice)

        self.assertCountEqual(cves, ["CVE-1234-5678", "CVE-1234-123456"])

    def test_update_cves_with_description(self):
        notice = {'references': [{'type': 'cve',
                                  'id': "CVE-1234-5678"},
                                 {'type': 'cve',
                                  'id': "CVE-1234-1234"},
                                 {'type': 'cve',
                                  'id': "CVE-1234-5678"},
                                 {'type': 'this should be skipped'}],
                  'description': 'This is a text with two CVE numbers CVE-1234-5678, CVE-1234-567901'}
        cves = self.reposync.RepoSync._update_cve(notice)

        self.assertCountEqual(cves, ["CVE-1234-567901", "CVE-1234-5678", "CVE-1234-1234"])


    def test_update_keywords_reboot(self):
        notice = {'reboot_suggested': True,
                  'restart_suggested': False}

        keyword = self.reposync.importLib.Keyword()
        keyword.populate({'keyword': 'reboot_suggested'})
        self.assertEqual(self.reposync.RepoSync._update_keywords(notice),
                         [keyword])

    def test_update_keywords_restart(self):
        notice = {'reboot_suggested': False,
                  'restart_suggested': True}

        keyword = self.reposync.importLib.Keyword()
        keyword.populate({'keyword': 'restart_suggested'})
        self.assertEqual(self.reposync.RepoSync._update_keywords(notice),
                         [keyword])

    def test_update_keywords_restart_and_reboot(self):
        notice = {'reboot_suggested': True,
                  'restart_suggested': True}

        keyword_restart = self.reposync.importLib.Keyword()
        keyword_restart.populate({'keyword': 'restart_suggested'})
        keyword_reboot = self.reposync.importLib.Keyword()
        keyword_reboot.populate({'keyword': 'reboot_suggested'})
        self.assertEqual(self.reposync.RepoSync._update_keywords(notice),
                         [keyword_reboot, keyword_restart])

    def test_update_keywords_both_false(self):
        notice = {'reboot_suggested': False,
                  'restart_suggested': False}

        self.assertEqual(self.reposync.RepoSync._update_keywords(notice),
                         [])

    def test_send_error_mail(self):
        rs = self._create_mocked_reposync()
        self.reposync.rhnMail.send = Mock()
        self.reposync.CFG.TRACEBACK_MAIL = 'recipient'
        self.reposync.hostname = 'testhost'

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
            self.assertTrue(isinstance(p, self.reposync.importLib.IncompletePackage))

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

        p1 = self.reposync.importLib.IncompletePackage()
        p1.populate({'package_size': None,
                     'name': 'n1',
                     'checksum_list': None,
                     'md5sum': None,
                     'org_id': 1,
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
        p2 = self.reposync.importLib.IncompletePackage()
        p2.populate({'package_size': None,
                     'name': 'n2',
                     'checksum_list': None,
                     'md5sum': None,
                     'org_id': 1,
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
        ident = "%(name)s-%(epoch)s:%(version)s-%(release)s.%(arch)s" % packages[0]
        rs.available_packages[ident] = 1

        _mock_rhnsql(self.reposync, [])
        self.assertEqual(rs._updates_process_packages(packages, 'patchy', []),
                         [])
        self.assertEqual(self.reposync.log.call_args[0][1],
                "The package n2-e2:v2-r2.arch2 "
                "which is referenced by patch patchy was not found "
                "in the database. This patch has been skipped.")

    def test_updates_process_packages_checksum_not_found_no_epoch(self):
        rs = self._create_mocked_reposync()

        packages = [{'name': 'n1',
                     'version': 'v1',
                     'release': 'r1',
                     'arch': 'arch1',
                     'channel_label': 'l1',
                     'epoch': '' }]
        ident = "%(name)s-%(epoch)s%(version)s-%(release)s.%(arch)s" % packages[0]
        rs.available_packages[ident] = 1

        _mock_rhnsql(self.reposync, [])
        self.assertEqual(rs._updates_process_packages(packages, 'patchy', []),
                         [])
        self.assertEqual(self.reposync.log.call_args[0][1],
                "The package n1-v1-r1.arch1 "
                "which is referenced by patch patchy was not found "
                "in the database. This patch has been skipped.")

    def test_updates_process_packages_checksum_not_found_but_not_available(self):
        rs = self._create_mocked_reposync()

        packages = [{'name': 'n1',
                     'version': 'v1',
                     'release': 'r1',
                     'arch': 'arch1',
                     'channel_label': 'l1',
                     'epoch': '' }]

        _mock_rhnsql(self.reposync, [])
        self.assertEqual(rs._updates_process_packages(packages, 'patchy', []),
                         [])
        self.assertEqual(self.reposync.log.call_args, None)

    # RedHat has errata with empty package list
    # they removed the check - therefor this is disabled too
    #def test_upload_updates_referenced_package_not_found(self):
    #    timestamp1 = datetime.now().isoformat(' ')
    #    notices = [{'from': 'from1',
    #                'update_id': 'update_id1',
    #                'version': 'version1',
    #                'type': 'security',
    #                'severity': 'Low',
    #                'release': 'release1',
    #                'description': 'description1',
    #                'title': 'title1',
    #                'issued': timestamp1, # we mock _to_db_date anyway
    #                'updated': timestamp1,
    #                'pkglist': [{'packages': []}],
    #                'reboot_suggested': False,
    #                'restart_suggested': False,
    #                'references': None,
    #                }]
    #    self.reposync._to_db_date = Mock(return_value=timestamp1)

    #    # no packages related to this errata makes the ErrataImport be called
    #    # with an empty list
    #    self.reposync.RepoSync._updates_process_packages = Mock(return_value=[])
    #    self.reposync.get_errata = Mock(return_value=None)

    #    mocked_backend = Mock()
    #    self.reposync.SQLBackend = Mock(return_value=mocked_backend)
    #    self.reposync.ErrataImport = Mock()

    #    rs = self._create_mocked_reposync()
    #    rs._patch_naming = Mock(return_value='package-name')

    #    rs.upload_updates(notices)

    #    self.assertEqual(self.reposync.ErrataImport.call_args, None)

    def test_associate_package(self):
        pack = ContentPackage()
        pack.setNVREA('name1', 'version1', 'release1', 'epoch1', 'arch1')
        pack.unique_id = 1
        pack.a_pkg = rhn_rpm.RPM_Package(None)
        pack.a_pkg.checksum = 'checksum1'
        pack.a_pkg.checksum_type = 'c_type1'
        pack.a_pkg.header = {'epoch': 'epoch1'}
        pack.checksums[1] = 'checksum1'

        mocked_backend = Mock()
        self.reposync.SQLBackend = Mock(return_value=mocked_backend)
        rs = self._create_mocked_reposync()
        rs._importer_run = Mock()
        rs.channel_label = 'Label1'
        rs.channel = {'id': 'channel1', 'org_id': 1}

        package = {'name': 'name1',
                   'version': 'version1',
                   'release': 'release1',
                   'epoch': 'epoch1',
                   'arch': 'arch1',
                   'checksum': 'checksum1',
                   'checksum_type': 'c_type1',
                   'org_id': 1,
                   'channels': [{'label': 'Label1', 'id': 'channel1'}]}
        refpack = importLib.IncompletePackage().populate(package)
        ipack = rs.associate_package(pack)
        self.assertEqual(ipack, refpack)

    def test_get_errata_no_advisories_found(self):
        rs = self._create_mocked_reposync()
        _mock_rhnsql(self.reposync, None)
        self.assertEqual(rs.get_errata('bogus'), None)

    def test_get_errata_advisories_but_no_channels(self):
        rs = self._create_mocked_reposync()
        _mock_rhnsql(self.reposync, [{'id': 42}, []])
        self.assertEqual(rs.get_errata('bogus'),
                         {'channels': [], 'id': 42, 'packages': []})

    def test_get_errata_success(self):
        rs = self._create_mocked_reposync()
        _mock_rhnsql(self.reposync, [{'id': 42}, ['channel1', 'channel2']])
        self.assertEqual(rs.get_errata('bogus'),
                         {'id': 42, 'channels': ['channel1', 'channel2'],
                          'packages': []})

    def test_get_compat_arches(self):
        _mock_rhnsql(self.reposync, ({'label': 'a1'}, {'label':'a2'}))
        self.assertEqual(self.reposync.RepoSync.get_compatible_arches(None),
                         ['a1', 'a2'])

    def test_set_repo_credentials_no_credentials(self):
        url = {'source_url': "http://example.com"}
        rs = self._create_mocked_reposync()

        rs.set_repo_credentials(url)
        self.assertEqual(url['source_url'], "http://example.com")

    def test_set_repo_credentials_old_default_credentials_bad(self):
        url = {
            "source_url": [
                "http://example.com/?credentials=testcreds"
            ]
        }
        rs = self._create_mocked_reposync()
        self.assertRaises(SystemExit, rs.set_repo_credentials, url)

    def test_set_repo_credentials_bad_credentials(self):
        rs = self._init_reposync()
        rs.error_msg = Mock()
        url = {
            "source_url": [
                "http://example.com/?credentials=bad_creds_with_underscore"
            ]
        }
        self.assertRaises(SystemExit, rs.set_repo_credentials, url)

    def test_set_repo_credentials_number_credentials(self):
        rs = self._create_mocked_reposync()
        url = {
            "source_url": [
                "http://example.com/?credentials=testcreds_42"
            ]
        }
        _mock_rhnsql(self.reposync, [{ 'username' : 'foo', 'password': 'c2VjcmV0' }])
        self.assertEqual(
            rs.set_repo_credentials(url), ["http://foo:secret@example.com/"])

    def test_is_old_style(self):
        """
        Test for _is_old_suse_style
        """
        notice = {'from': 'maint-coord@suse.de',
                  'version': '1111',
                  'update_id': 'sles-kernel-default'}
        self.assertTrue(self.reposync.RepoSync._is_old_suse_style(notice))

        notice = {'from': 'maint-coord@suse.de',
                  'version': '7',
                  'update_id': 'res5ct-kernel-default'}
        self.assertTrue(self.reposync.RepoSync._is_old_suse_style(notice))

        notice = {'from': 'maint-coord@suse.de',
                  'version': '1',
                  'update_id': 'sles-kernel-default'}
        self.assertFalse(self.reposync.RepoSync._is_old_suse_style(notice))

        notice = {'from': 'maint-coord@suse.de',
                  'version': '6',
                  'update_id': 'res5ct-kernel-default'}
        self.assertFalse(self.reposync.RepoSync._is_old_suse_style(notice))

    def test_to_db_date(self):
        """
        Test for _to_db_date
        """
        # Unsure datetime.fromtimestamp is always returning UTC times
        class DateTimeMock(datetime):
            @classmethod
            def fromtimestamp(cls, timestamp):
                return cls.utcfromtimestamp(timestamp)

        with patch("spacewalk.satellite_tools.reposync.datetime", DateTimeMock):
            self.assertEqual(self.reposync.RepoSync._to_db_date('2015-01-02 01:02:03'), '2015-01-02 01:02:03')
            self.assertEqual(self.reposync.RepoSync._to_db_date('1420160523'), '2015-01-02 01:02:03')
            self.assertEqual(self.reposync.RepoSync._to_db_date('2015-01-02'), '2015-01-02 00:00:00')
            self.assertEqual(self.reposync.RepoSync._to_db_date('2015-09-02 13:39:49 UTC'), '2015-09-02 13:39:49')
            self.assertEqual(self.reposync.RepoSync._to_db_date('2015-01-02T02:02:03+0100'), '2015-01-02 01:02:03')
            self.assertRaises(ValueError, self.reposync.RepoSync._to_db_date, '2015-01-02T01:02:03+nonsense')

    def _init_reposync(self, label="Label", repo_type=RTYPE, **kwargs):
        """Initialize the RepoSync object with some mocked attrs"""
        self.reposync.RepoSync.get_compatible_arches = Mock(
            return_value=['arch1', 'arch2'])
        channel = {'org_id':1, 'id':1, 'arch': 'arch1'}
        self.reposync.RepoSync.load_channel = Mock(return_value=channel)
        rs = self.reposync.RepoSync(label, repo_type, **kwargs)
        return rs

    def _create_mocked_reposync(self):
        """Create a fully mocked RepoSync"""
        rs = self._init_reposync()
        rs.urls = [{'id': None, "source_url": ["http://none.host/bogus-url"], "metadata_signed": "N", "repo_label": None, 'repo_type': 'yum'}]
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
                          'org_id': 1,
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
        rs.import_packages = Mock(return_value=0)
        rs.import_updates = Mock()
        rs.import_products = Mock()
        rs.import_susedata = Mock()
        rs.import_groups = Mock()
        rs.import_modules = Mock()
        self.reposync.taskomatic.add_to_repodata_queue_for_channel_package_subscription = Mock()
        self.reposync.taskomatic.add_to_erratacache_queue = Mock()
        self.reposync.log = Mock()

        rs.mocked_plugin = Mock()
        rs.mocked_plugin.num_packages = 0
        rs.load_plugin = Mock(return_value=Mock(return_value=rs.mocked_plugin))

        rs.update_date = Mock()

        self.reposync.initCFG = Mock()
        self.reposync.CFG = Mock()
        self.reposync.CFG.MOUNT_POINT = '/tmp'
        self.reposync.CFG.PREPENDED_DIR = ''
        self.reposync.fileutils.createPath = Mock()
        self.reposync.os.walk = Mock(return_value=[])
        return rs


class SyncTest(unittest.TestCase):

    def setUp(self):
        module_patcher = patch.multiple(
            'spacewalk.satellite_tools.reposync',
            rhnSQL=Mock(),
            initCFG=Mock()
        )
        class_patcher = patch.multiple(
            'spacewalk.satellite_tools.reposync.RepoSync',
            load_channel=Mock(
                return_value=dict(id="1", org_id=1, label="label#1")
            ),
            get_compatible_arches=Mock(),
            load_plugin=Mock(),
            import_packages=Mock(return_value=0),
            import_groups=Mock(),
            import_modules=Mock(),
            import_updates=Mock(),
            import_products=Mock(),
            import_susedata=Mock()
        )
        module_patcher.start()
        class_patcher.start()
        self.addCleanup(module_patcher.stop)
        self.addCleanup(class_patcher.stop)

    def test_pass_multiple_urls_params(self):
        from spacewalk.satellite_tools.reposync import RepoSync
        urls = ['http://some.url', 'http://some-other.url']
        repo_sync = RepoSync(
            channel_label="channel-label",
            repo_type=RTYPE,
            url=urls
        )
        repo_sync.sync()

    @patch('spacewalk.satellite_tools.reposync.RepoSync._url_with_repo_credentials')
    def test_set_repo_credentials_with_multiple_urls(self, mocked_method):
        from spacewalk.satellite_tools.reposync import RepoSync
        urls = ['http://some.url', 'http://some-other.url']
        data = {
            'metadata_signed': 'N',
            'repo_label': None,
            'id': None,
            'source_url': urls,
            'repo_type': 'yum'
        }
        repo_sync = RepoSync(
            channel_label="channel-label",
            repo_type=RTYPE,
            url=urls
        )
        repo_sync.set_repo_credentials(data)
        self.assertEqual(
            repo_sync._url_with_repo_credentials.call_args_list,
            [call(urls[0]), call(urls[1])]
        )

    def test__url_with_repo_credentials(self):
        import base64
        from spacewalk.satellite_tools.reposync import RepoSync
        credentials_id = 777
        urls = [
            'http://some.url?credentials=abc_%s' % credentials_id,
            'http://some-other.url'
        ]
        repo_sync = RepoSync(
            channel_label="channel-label",
            repo_type=RTYPE,
            url=urls
        )
        username = "user#1"
        password = "pass#1"
        config = {
            'return_value.fetchone_dict.return_value': {
                "username": "user#1",
                "password": base64.encodestring(password.encode()).decode()
            }
        }
        patcher = patch(
            'spacewalk.satellite_tools.reposync.rhnSQL.prepare', **config
        )
        with patcher as mock_prepare:
            self.assertEqual(
                repo_sync._url_with_repo_credentials(urls[0]),
                'http://{0}:{1}@some.url'.format(username, password)
            )
            mock_prepare.assert_called_once_with(
                'SELECT username, password FROM suseCredentials WHERE id = :id'
            )
            mock_prepare().execute.assert_called_once_with(id=credentials_id)

    def test_rhnSQL_should_return_source_urls_as_list(self):
        from spacewalk.satellite_tools.reposync import RepoSync
        url1 = 'http://url.one'
        url2 = 'http://url.two'
        patcher = patch(
            'spacewalk.satellite_tools.reposync.rhnSQL.prepare',
            **{
                'return_value.fetchall_dict.return_value': [
                    {
                        'metadata_signed': 'N',
                        'repo_label': 'channel-label-1',
                        'id': 508,
                        'source_url': url1,
                        'repo_type': 'yum'
                    },
                    {
                        'metadata_signed': 'Y',
                        'repo_label': 'channel-label-2',
                        'id': 509,
                        'source_url': url2,
                        'repo_type': 'yum'
                    }
                ]
            }
        )
        with patcher as mock_prepare:
            repo_sync = RepoSync(
                channel_label="channel-label",
                repo_type=RTYPE
            )
            self.assertEqual(
                repo_sync.urls,
                [
                    {
                        'metadata_signed': 'N',
                        'repo_label': 'channel-label-1',
                        'id': 508,
                        'source_url': [url1],
                        'repo_type': 'yum'
                    },
                    {
                        'metadata_signed': 'Y',
                        'repo_label': 'channel-label-2',
                        'id': 509,
                        'source_url': [url2],
                        'repo_type': 'yum'
                    }
                ]
            )


class RunScriptTest(unittest.TestCase):

    @classmethod
    def setUpClass(cls):
        cls.repo_sync = imp.load_source(
            'repo_sync',
            '/manager/backend/satellite_tools/spacewalk-repo-sync')

    def setUp(self):
        config = dict(
            os=Mock(**{'getuid.return_value': 0}),
            open=Mock(
                return_value=StringIO(
					json.dumps(
						{
							"no_errata": False,
							"sync_kickstart": False,
							"fail": True,
							"channel": {
								"chann_1": [
									"http://example.com/repo1",
									"http://example.com/repo2"
								],
								"chann_2": []
							}
						}
					)
                )
            ),
            rhnLockfile=Mock(),
            releaseLOCK=Mock(),
            OptionParser=Mock(
                **{
                    'return_value.parse_args.return_value': [
                        Mock(
                            # options
                            list=None,
                            dry_run=False,
                            config="example.conf",
                            channel_label=[],
                            parent_label=None,
                            batch_size=None
                        ),
                        []
                    ]
                }
            ),
            reposync=Mock(
                **{
                    'getCustomChannels.return_value': ['chann_1', 'chann_2'],
                    'getChannelRepo.return_value': {
                        'chann_1': 'abc',
                        'chann_2': 'def'
                    },
                    'RepoSync.return_value.sync.return_value': (timedelta(), 0)
                }
            ),
            systemExit=Mock(side_effect=[SystemExit])
        )
        patcher = patch.multiple('repo_sync', **config)
        patcher.start()
        self.addCleanup(patcher.stop)

    def test_config_parameter_channel_not_list(self):
        self.repo_sync.open.return_value = StringIO(
            json.dumps(
                {
                    "no_errata": False,
                    "sync_kickstart": False,
                    "fail": True,
                    "channel": {"chann_1": "http://example.com/repo1"}
                }
            )
        )
        self.assertRaises(SystemExit, self.repo_sync.main)
        self.repo_sync.systemExit.assert_called_once_with(
            1,
            "Configuration file is invalid, chann_1's value needs to be a list."
        )

    def test_config_parameter_channel_as_list(self):
        self.repo_sync.CFG = Mock()
        self.repo_sync.CFG.DEBUG = 3
        self.repo_sync.main()
        self.assertEqual(self.repo_sync.reposync.RepoSync.call_count, 2)


def test_channel_exceptions():
    """Test rasising all the different exceptions when syncing"""
    # the only way to write a test generator with nose is if we put it
    # outside the class, so we have to repeat all the Mocks
    repoSync = spacewalk.satellite_tools.reposync
    repoSync.rhnLog.initLOG = Mock()
    repoSync.CFG = repoSync.initCFG = Mock()
    repoSync.CFG.MOUNT_POINT = '/tmp'
    repoSync.CFG.PREPENDED_DIR = ''
    repoSync.fileutils.createPath = Mock()
    repoSync.os.walk = Mock(return_value=[])
    backup_os = repoSync.os
    repoSync.os = Mock()
    repoSync.RepoSync._format_sources = Mock()
    repoSync.RepoSync.get_compatible_arches = Mock(return_value=['arch1', 'arch2'])
    rs = repoSync.RepoSync("Label", RTYPE)
    rs.urls = [{'id': None, "source_url": ["http://none.host/bogus-url"], "metadata_signed": "N", "repo_label": None, 'repo_type': 'yum'}]
    rs.import_packages = Mock(return_value=0)
    rs.import_updates = Mock()
    rs.mocked_plugin = Mock()
    rs.log = Mock()
    rs.load_plugin = Mock(return_value=rs.mocked_plugin)
    rs.update_date = Mock()
    rs.sendErrorMail = Mock()
    repoSync.os = backup_os

    for exc_class, exc_name in [
        (repoSync.ChannelException, "ChannelException"),
        (yum_src.RepoMDError, "RepoMDError")]:
        yield check_channel_exceptions, rs, exc_class, exc_name

def check_channel_exceptions(rs, exc_class, exc_name):
    # since this isn't a subclass of unittest.TestCase we can't use
    # unittest's assertions
    from nose.tools import assert_raises, assert_equal
    rs.load_plugin = Mock(return_value=Mock(side_effect=exc_class("error msg")))

    etime, ret = rs.sync()
    assert_equal(-1, ret)
    assert_equal(rs.sendErrorMail.call_args,
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
