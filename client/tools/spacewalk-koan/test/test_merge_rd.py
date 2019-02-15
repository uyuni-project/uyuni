#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright (C) 2015 SUSE LINUX Products GmbH, Nuernberg, Germany.
#   This library is free software; you can redistribute it and/or modify
# it only under the terms of version 2.1 of the GNU Lesser General Public
# License as published by the Free Software Foundation.
#
#   This library is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
# details.
#
#   You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA

try:
    import unittest2 as unittest
except ImportError:
    import unittest
import shutil
import os
import tempfile
from spacewalkkoan.spacewalkkoan import my_popen
from . import helper

MERGE_RD_CMD = os.path.join(
    os.path.dirname(os.path.realpath(__file__)),
    "..", "merge-rd.sh"
)

class MergeRdTest(unittest.TestCase):

    def setUp(self):
        self.data_dir = tempfile.mkdtemp("", "spacewalkkoan")
        self.initrd_xz = "/root/initrd.xz"
        self.final_initrd = os.path.join(self.data_dir, "initrd.gz")

    def tearDown(self):
        shutil.rmtree(self.data_dir)

    def test_should_fail_when_tar_is_not_installed(self):
        self.ensure_package_is_not_installed("tar")
        self.ensure_package_is_installed("xz")
        (status, stdout, stderr) = my_popen([
            "/bin/sh",
            MERGE_RD_CMD,
            self.initrd_xz,
            self.final_initrd,
            helper.path_to_fixture("ks-tree-shadow/")
        ])

        errors = b"".join(stderr.readlines()).decode()

        self.assertNotEqual(0, status)
        if not "tar: command not found" in errors:
            self.fail("Command failed for a different reason: {0}".format(errors))

    def test_should_fail_when_xz_is_not_installed(self):
        self.ensure_package_is_not_installed("xz")
        (status, stdout, stderr) = my_popen([
            "/bin/sh",
            MERGE_RD_CMD,
            self.initrd_xz,
            self.final_initrd,
            helper.path_to_fixture("ks-tree-shadow/")
        ])

        errors = b"".join(stderr.readlines()).decode()

        self.assertNotEqual(0, status)
        if not "Error uncompressing" in errors:
            self.fail("Command failed for a different reason: {0}".format(errors))

    def test_should_fail_when_the_initrd_is_not_a_valid_file(self):
        (status, stdout, stderr) = my_popen([
            "/bin/sh",
            MERGE_RD_CMD,
            "/etc/fstab",
            self.final_initrd,
            helper.path_to_fixture("ks-tree-shadow/")
        ])

        errors = b"".join(stderr.readlines()).decode()

        self.assertNotEqual(0, status)
        if not "Error uncompressing" in errors:
            self.fail("Command failed for a different reason: {0}".format(errors))

    def test_should_fail_when_the_initrd_is_not_found(self):
        (status, stdout, stderr) = my_popen([
            "/bin/sh",
            MERGE_RD_CMD,
            "/fake_initrd",
            self.final_initrd,
            helper.path_to_fixture("ks-tree-shadow/")
        ])

        errors = b"".join(stderr.readlines()).decode()

        self.assertNotEqual(0, status)
        if not "Cannot find initrd" in errors:
            self.fail("Command failed for a different reason: {0}".format(errors))

    def test_should_fail_when_directory_containing_final_initrd_does_not_exist(self):
        self.ensure_package_is_installed("tar", "xz")
        (status, stdout, stderr) = my_popen([
            "/bin/sh",
            MERGE_RD_CMD,
            self.initrd_xz,
            "/new_dir/initrd.out",
            helper.path_to_fixture("ks-tree-shadow/")
        ])

        errors = b"".join(stderr.readlines()).decode()

        self.assertNotEqual(0, status)
        if not "Cannot find final destination dir" in errors:
            self.fail("Command failed for a different reason: {0}".format(errors))

    def test_should_fail_when_the_user_tree_directory_is_missing(self):
        self.ensure_package_is_installed("tar", "xz")
        (status, stdout, stderr) = my_popen([
            "/bin/sh",
            MERGE_RD_CMD,
            self.initrd_xz,
            self.final_initrd,
            "/does/not/exist/ks-tree-shadow/"
        ])

        errors = b"".join(stderr.readlines()).decode()

        self.assertNotEqual(0, status)
        if not "Cannot find user tree" in errors:
            self.fail("Command failed for a different reason: {0}".format(errors))

    def test_should_create_a_new_initrd_starting_from_a_xz_compressed_one(self):
        self.ensure_package_is_installed("tar", "xz")
        self.ensure_package_is_installed("xz")

        (status, stdout, stderr) = my_popen([
            "/bin/sh",
            MERGE_RD_CMD,
            self.initrd_xz,
            self.final_initrd,
            helper.path_to_fixture("ks-tree-shadow/")
        ])

        # ensure the new initrd has been created
        errors = b"".join(stderr.readlines()).decode()
        self.assertEqual(0, status,
            "Something wrong happened: {0}".format(errors))
        self.assertTrue(os.path.exists(self.final_initrd))

        # ensure the new initrd is compressed using gzip
        (status, stdout, stderr) = my_popen([
            "file",
            self.final_initrd,
        ])
        errors = b"".join(stderr.readlines()).decode()
        self.assertEqual(0, status,
            "Something wrong happened: {0}".format(errors))
        self.assertTrue("gzip compressed data" in b"".join(stdout.readlines()).decode())

        # uncompress the initrd
        (status, stdout, stderr) = my_popen([
            "gzip", "-d", self.final_initrd 
        ])
        errors = b"".join(stderr.readlines()).decode()
        self.assertEqual(0, status,
            "Something wrong happened: {0}".format(errors))

        # extract the contents of the initrd
        uncomp_dir = os.path.join(self.data_dir, "uncompressed")
        os.mkdir(uncomp_dir)

        os.chdir(uncomp_dir)
        (status, stdout, stderr) = my_popen([
            "cpio", "-idF", self.final_initrd.replace(".gz", "")
        ])
        errors = b"".join(stderr.readlines()).decode()
        self.assertEqual(0, status,
            "Something wrong happened: {0}".format(errors))

        test_file = os.path.join(
            uncomp_dir, "susemanager", "test_file")
        self.assertTrue(os.path.exists(test_file))

        expected_data = helper.read_data_from_fixture("ks-tree-shadow/susemanager/test_file")
        with open(test_file) as file:
            self.assertEqual(
                file.read(),
                expected_data
            )

        # the "uncomp_dir" is going to be removed at the end of the tests
        os.chdir("/")


    def ensure_package_is_installed(self, *pkgs):
        cmd = ["zypper", "in", "-y"]
        for pkg in pkgs:
            cmd.append(pkg)

        (status, stdout, stderr) = my_popen(cmd)

        if status != 0:
            self.fail(
                "Something went wrong while ensuring {0} was "
                "installed: {1}".format(pkg, b"".join(stderr.readlines()).decode())
            )

    def ensure_package_is_not_installed(self, *pkgs):
        cmd = ["zypper", "rm", "-y"]
        for pkg in pkgs:
            cmd.append(pkg)

        (status, stdout, stderr) = my_popen(cmd)

        if status != 0:
            self.fail(
                "Something went wrong while ensuring {0} was not "
                "installed: {1}".format(pkg, b"".join(stderr.readlines()).decode())
            )

