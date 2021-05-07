#!/usr/bin/python3
import psycopg2
from psycopg2 import sql

from argparse import ArgumentParser

db_config = {
    "dbname": "susemanager",
    "user": "username",
    "password": "password",
    "host": "localhost",
    "port": 5432,
}


def get_cursor(db_config):
    """Returns a cursor to query the database."""
    conn_string = "dbname='{dbname}' user='{user}' host='{host}' port='{port}' password='{password}'".format(
        **db_config)
    connection = psycopg2.connect(conn_string)
    return connection.cursor()


def get_dependencies(cursor, tablename, dependencies):
    """Returns a list of tables that a table depends on """
    if tablename in dependencies:
        sql = f"""
        SELECT cl2.relname AS ref_table
        FROM   pg_constraint AS co
               JOIN pg_class AS cl1
             ON co.conrelid = cl1.oid
               JOIN pg_class AS cl2
             ON co.confrelid = cl2.oid
        WHERE  co.contype = 'f'
               AND cl1.relname = '{tablename}'
        ORDER  BY cl2.relname; 
        """
        cursor.execute(sql)
        rows = cursor.fetchall()
        for row in rows:
            if row[0] not in dependencies:
                dependencies.append(row[0])
                get_dependencies(cursor, row[0], dependencies)


def main():
    parser = ArgumentParser(description='Script to create pg_dump statement for all the supplied tables including '
                                        'their dependecies')
    requiredNamed = parser.add_argument_group('Required named arguments')
    requiredNamed.add_argument('-t', '--table', action='append', help='The name of the table to include in pg_dump.'
                                                                      ' It can be used multiple times.', required=True)
    args = parser.parse_args()

    cursor = get_cursor(db_config)
    dependencies = args.table
    for table in args.table:
        get_dependencies(cursor, table, dependencies)
    tables_to_dump = ' '.join(['-t ' + dep for dep in dependencies])
    pg_dump_query = f"""
                     su - postgres -c "pg_dump {tables_to_dump} --create `grep -oP 'db_name ?= ?\K.*' /etc/rhn/rhn.conf` | gzip > /tmp/backup/pg_dump.gz"
                     """
    print(pg_dump_query)


if __name__ == '__main__':
    main()
