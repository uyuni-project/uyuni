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

import os
import solv
import xml.etree.ElementTree as etree
import unittest
try:
    from io import StringIO
except ImportError:
    from StringIO import StringIO
from collections import namedtuple

import mock
from mock import Mock, MagicMock

from spacewalk.satellite_tools.repo_plugins import yum_src, ContentPackage

class YumSrcTest(unittest.TestCase):

    def _make_dummy_cs(self):
        """Create a dummy ContentSource object that only talks to a mocked yum"""
        real_setup_repo = yum_src.ContentSource.setup_repo
        yum_src.ContentSource.get_groups = Mock(return_value=None)

        # don't read configs
        mock.patch('spacewalk.common.suseLib.initCFG').start()
        mock.patch('spacewalk.common.suseLib.CFG').start()
        yum_src.initCFG = Mock()
        yum_src.CFG = Mock()
        yum_src.CFG.MOUNT_POINT = ''
        yum_src.CFG.PREPENDED_DIR = ''
        yum_src.fileutils.makedirs = Mock()
        yum_src.os.makedirs = Mock()
        yum_src.os.path.isdir = Mock()

        yum_src.get_proxy = Mock(return_value=(None, None, None))

        cs = yum_src.ContentSource("http://example.com", "test_repo", org='')
        mockReturnPackages = MagicMock()
        mockReturnPackages.returnPackages = MagicMock(name="returnPackages")
        mockReturnPackages.returnPackages.return_value = []
        cs.repo.is_configured = True
        cs.repo.includepkgs = []
        cs.repo.exclude = []

        yum_src.ContentSource.setup_repo = real_setup_repo

        return cs

    def test_content_source_init(self):
        cs = self._make_dummy_cs()

        self.assertFalse(cs.insecure)
        self.assertTrue(cs.interactive)
        assert isinstance(cs.repo, Mock)

    def test_list_packages_empty(self):
        cs = self._make_dummy_cs()

        os.path.isfile = Mock(return_value=True)
        solv.Pool = Mock()

        self.assertEqual(cs.list_packages(filters=None, latest=False), [])

    def test_list_packages_with_pack(self):
        cs = self._make_dummy_cs()

        package_attrs = ['name', 'version', 'release',
                         'epoch', 'arch', 'checksums', 'repoid', 'pkgtup']
        Package = namedtuple('Package', package_attrs)
        mocked_packs = [Package('n1', 'v1', 'r1', 'e1', 'a1', [('c1', 'cs')], 'rid1', ('n1', 'a1', 'e1', 'v1', 'r1')),
                        Package('n2', 'v2', 'r2', 'e2', 'a2', [('c2', 'cs')], 'rid2', ('n2', 'a2', 'e2', 'v2', 'r2'))]

        os.path.isfile = Mock(return_value=True)
        solv.Pool = Mock()

        listed_packages = cs.list_packages(filters=None, latest=False)

        self.assertEqual(len(listed_packages), 2)
        for pack, mocked_pack in zip(listed_packages, mocked_packs):
            # listed_packages should return ContentPackages
            self.assertTrue(isinstance(pack, ContentPackage))

            # all the attributes should be rightly imported from yum's
            # returnPackages which we've mocked above
            for attr in package_attrs:
                if attr == 'checksums':
                    # checksums are transformed by list_packages from
                    # yum's list of tuples to ContentSource's dictionary
                    self.assertEqual(pack.checksums,
                                     {mocked_pack.checksums[0][0]:
                                          mocked_pack.checksums[0][1]})
                elif attr in ['repoid', 'pkgtup']:
                    continue
                else:
                    self.assertEqual(getattr(pack, attr),
                                     getattr(mocked_pack, attr))
        
    def test_get_updates_suse_patches(self):
        cs = self._make_dummy_cs()

        patches_xml = StringIO(u"""<?xml version="1.0" encoding="UTF-8"?>
                <patches xmlns="http://novell.com/package/metadata/suse/patches">
                  <patch id="smcl3-cobbler-7778">
                    <checksum type="sha">ec34048ebda707a83190056d832d43c9fbb55ca6</checksum>
                    <location href="/patch-smcl3-cobbler-7778.xml"/>
                    <someother>weird element</someother>
                  </patch>
                  <patch id="smcl3-code11-update-stack-7779">
                    <checksum type="sha">51a736a468ebf53d7a4084cf0ca72a87427cdeba</checksum>
                    <location href="/patch-smcl3-code11-update-stack-7779.xml"/>
                  </patch>
                </patches>
                """)
        cs._md_exists = Mock(return_value=True)
        cs._md_retrieve_md_path = Mock(return_value="patches.xml")
        etree.iterparse = Mock(return_value=patches_xml)
        os.path.join = Mock(side_effect=lambda *args: StringIO(u"<xml></xml>"))

        patches = cs.get_updates()
        self.assertEqual(patches[0], 'patches')
        self.assertEqual(len(patches[1]), 2)
