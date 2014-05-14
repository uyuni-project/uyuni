#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright (c) 2014 SUSE LINUX Products GmbH, Nuernberg, Germany.
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#

import hashlib
import unittest
import time
from spacewalk.server import rhnSQL, suseEula
import misc_functions

import os

class SUSEEulaTest(unittest.TestCase):

    def setUp(self):
        misc_functions.setup_db_connection()
        rhnSQL.clear_log_id()
        self.__transaction_name = 'eula_test_%d' % (int(time.time()))
        rhnSQL.transaction(self.__transaction_name)

    def tearDown(self):
        # Roll back any unsaved data
        rhnSQL.rollback(self.__transaction_name)

    def test_create_eulas(self):
        text, _ = self.__generate_fake_eula()

        self.assertEqual(0, self.__count_eulas())
        suseEula.find_or_create_eula(text)
        self.assertEqual(1, self.__count_eulas())

        # try creating another EULA with the same text
        suseEula.find_or_create_eula(text)
        self.assertEqual(1, self.__count_eulas())

    def test_get_eula_by_id(self):
        text, _ = self.__generate_fake_eula()

        self.assertEqual(0, self.__count_eulas())
        self.assertEqual(None, suseEula.get_eula_by_id(1))

        eula_id = suseEula.find_or_create_eula(text)
        self.assertEqual(text, suseEula.get_eula_by_id(eula_id))

    def test_get_eula_by_checksum(self):
        text, checksum = self.__generate_fake_eula()

        self.assertEqual(0, self.__count_eulas())
        self.assertEqual(None, suseEula.get_eula_by_checksum('foo'))

        eula_id = suseEula.find_or_create_eula(text)
        self.assertEqual(text, suseEula.get_eula_by_checksum(checksum))

    def __count_eulas(self):
        count_eulas_query = "SELECT COUNT(id) AS eulas from suseEula"
        h = rhnSQL.prepare(count_eulas_query)
        h.execute()
        return h.fetchone_dict()['eulas']

    def __generate_fake_eula(self):
        text     = "Fake EULA %d" % int(time.time())
        checksum = hashlib.new("sha256", text).hexdigest()
        return text, checksum

