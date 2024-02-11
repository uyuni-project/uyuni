#  pylint: disable=missing-module-docstring
#
# Copyright (c) 2008--2015 Red Hat, Inc.
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
#
# This file implements teh Sequence class
#

from spacewalk.common.rhnException import rhnException

from . import sql_base


# A class to handle sequences
# XXX: this is still Oracle specific, but it shouldn't be
# pylint: disable-next=missing-class-docstring
class Sequence:
    def __init__(self, db, seq):
        if not seq or not isinstance(seq, str):
            raise rhnException("First argument needs to be a sequence name", seq)
        self.__seq = seq
        if not isinstance(db, sql_base.Database):
            raise rhnException("Argument db is not a database instance", db)
        self.__db = db

    def next(self):
        # pylint: disable-next=consider-using-f-string
        sql = "select sequence_nextval('%s') as ID from dual" % self.__seq
        cursor = self.__db.prepare(sql)
        cursor.execute()
        ret = cursor.fetchone_dict()
        if ret is None:  # how the hell can this happen?
            return ret
        return int(ret["id"])

    def next_many(self, n):
        # pylint: disable-next=consider-using-f-string
        sql = "SELECT sequence_nextval('%s') FROM generate_series(1, %s)" % (
            self.__seq,
            n,
        )
        cursor = self.__db.prepare(sql)
        cursor.execute()
        result = cursor.fetchall()
        return [r[0] for r in result]

    def __call__(self):
        return self.next()

    def __del__(self):
        self.__seq = self.__db = None
