#  pylint: disable=missing-module-docstring
#
# Copyright (c) 2008--2016 Red Hat, Inc.
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
import os
import sys
import cx_Oracle


def main():
    if len(sys.argv) != 2:
        # pylint: disable-next=consider-using-f-string
        sys.stdout.write("Usage: %s <connectstring>\n" % sys.argv[0])
        return 1
    dbh = cx_Oracle.connect(sys.argv[1])

    # pylint: disable-next=consider-using-f-string
    table_name = "test_%d" % os.getpid()
    # pylint: disable-next=using-constant-test
    if 1:
        test2(dbh, table_name)
    else:
        test1(dbh, table_name)
        test1(dbh, table_name)


def test1(dbh, table_name):
    tn = table_name.upper()
    create_table(dbh, table_name)
    # pylint: disable-next=consider-using-f-string
    assert tn in list_tables(dbh), "Table %s not created" % table_name
    drop_table(dbh, table_name)
    # pylint: disable-next=consider-using-f-string
    assert tn not in list_tables(dbh), "Table %s not dropped" % table_name


def test2(dbh, table_name):
    tn = table_name.upper()
    h1 = create_table(dbh, table_name)
    # pylint: disable-next=consider-using-f-string
    assert tn in list_tables(dbh), "Table %s not created" % table_name
    h2 = drop_table(dbh, table_name)
    # pylint: disable-next=consider-using-f-string
    assert tn not in list_tables(dbh), "Table %s not dropped" % table_name

    h1.execute(None)
    # pylint: disable-next=consider-using-f-string
    assert tn in list_tables(dbh), "Table %s not created" % table_name
    h2.execute(None)
    # pylint: disable-next=consider-using-f-string
    assert tn not in list_tables(dbh), "Table %s not dropped" % table_name


def create_table(dbh, table_name):
    h = dbh.cursor()
    # pylint: disable-next=consider-using-f-string
    h.execute("create table %s (id int)" % table_name)
    return h


def drop_table(dbh, table_name):
    h = dbh.cursor()
    # pylint: disable-next=consider-using-f-string
    h.execute("drop table %s" % table_name)
    return h


def list_tables(dbh):
    h = dbh.cursor()
    h.execute("select table_name from user_tables")
    return [x[0].upper() for x in h.fetchall()]


if __name__ == "__main__":
    sys.exit(main() or 0)
