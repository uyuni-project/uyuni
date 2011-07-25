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

import unittest
from collections import namedtuple

from mock import Mock

from spacewalk.satellite_tools.reposync import ContentPackage
from spacewalk.satellite_tools.repo_plugins import yum_src

class YumSrcTest(unittest.TestCase):

    def _make_dummy_cs(self):
        """Create a dummy ContentSource object that only talks to a mocked yum"""
        real_yum = yum_src.yum
        yum_src.yum = Mock()

        real_setup_repo = yum_src.ContentSource.setup_repo
        yum_src.ContentSource.initgpgdir = Mock()

        cs = yum_src.ContentSource("http://example.com", "test_repo")

        yum_src.yum = real_yum
        yum_src.ContentSource.setup_repo = real_setup_repo

        return cs

    def test_content_source_init(self):
        cs = self._make_dummy_cs()

        self.assertFalse(cs.insecure)
        self.assertFalse(cs.quiet)
        self.assertTrue(cs.interactive)
        assert isinstance(cs.repo, Mock)

    def test_list_packages_empty(self):
        cs = self._make_dummy_cs()

        mocked_sack = Mock(return_value=[])
        cs.sack.returnPackages = mocked_sack

        self.assertEqual(cs.list_packages(), [])

    def test_list_packages_with_pack(self):
        cs = self._make_dummy_cs()

        package_attrs = ['name', 'version', 'release',
                         'epoch', 'arch', 'checksums']
        Package = namedtuple('Package', package_attrs)
        mocked_packs = [Package('n1', 'v1', 'r1', 'e1', 'a1', [('c1', 'cs')]),
                        Package('n2', 'v2', 'r2', 'e2', 'a2', [('c2', 'cs')])]
        cs.sack.returnPackages = Mock(return_value=mocked_packs)

        listed_packages = cs.list_packages()

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
                else:
                    self.assertEqual(getattr(pack, attr),
                                     getattr(mocked_pack, attr))
        
