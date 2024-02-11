#!/usr/bin/env python
# pylint: disable=missing-module-docstring
# -*- coding: utf-8 -*-
#
# Copyright (C) 2014 Novell, Inc.
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

import os

try:
    import unittest2 as unittest
except ImportError:
    import unittest

from spacewalk.susemanager.mgr_sync.config import Config


class ConfigTest(unittest.TestCase):
    def setUp(self):
        self.fake_rhn_file = os.path.join(os.getcwd(), "fake_rhn_file")
        Config.RHNFILE = self.fake_rhn_file

        self.fake_user_file = os.path.join(os.getcwd(), "fake_user_file")
        Config.DOTFILE = self.fake_user_file

    def tearDown(self):
        if os.path.exists(self.fake_rhn_file):
            os.unlink(self.fake_rhn_file)
        if os.path.exists(self.fake_user_file):
            os.unlink(self.fake_user_file)

    def test_merging_different_configuration_files(self):
        """
        Test Config can merge different configuration files giving higher
        priority to user's settings.
        """

        rhn_user = "rhn_user"
        rhn_token = "rhn_token"

        self._create_fake_config_file(
            self.fake_rhn_file, user=rhn_user, token=rhn_token
        )

        # only rhn config is in place
        config = Config()

        self.assertEqual(rhn_user, config.user)
        self.assertEqual(rhn_token, config.token)

        # add user config, should get higher priority at parsing time

        local_user = "local_user"
        local_password = "local_password"
        self._create_fake_config_file(
            self.fake_user_file, user=local_user, password=local_password
        )

        config = Config()

        self.assertEqual(local_user, config.user)
        self.assertEqual(local_password, config.password)
        self.assertEqual(rhn_token, config.token)

    def _create_fake_config_file(
        self,
        filename,
        user=None,
        password=None,
        host=None,
        port=None,
        uri=None,
        token=None,
    ):
        # pylint: disable-next=unspecified-encoding
        with open(filename, "w") as file:
            if user:
                # pylint: disable-next=consider-using-f-string
                file.write("{0} = {1}\n".format(Config.USER, user))
            if password:
                # pylint: disable-next=consider-using-f-string
                file.write("{0} = {1}\n".format(Config.PASSWORD, password))
            if host:
                # pylint: disable-next=consider-using-f-string
                file.write("{0} = {1}\n".format(Config.HOST, host))
            if port:
                # pylint: disable-next=consider-using-f-string
                file.write("{0} = {1}\n".format(Config.PORT, port))
            if uri:
                # pylint: disable-next=consider-using-f-string
                file.write("{0} = {1}\n".format(Config.URI, uri))
            if token:
                # pylint: disable-next=consider-using-f-string
                file.write("{0} = {1}\n".format(Config.TOKEN, token))
