#!/usr/bin/python
import argparse
import difflib
import distutils.version as DV
import glob
import json
import os
import os.path
import re
import shutil
import subprocess
import sys


def get_all_files_from_pr(pr_file, schema_path):
    """ Get added or modified files from a JSON with PR data """
    with open(pr_file, 'r') as json_file:
        files = json.loads(json_file.read())['files']
        changed_files = []
        for file in files:
            filename = str(file['filename'])
            if not re.search(r'^schema\/spacewalk\/upgrade\/susemanager-schema-\d+\.\d+[\d|.]*-to-susemanager-schema-\d+\.\d+[\d|.]*\/[^\/]+$', filename):
                continue
            if not file['status'] in ['modified', 'added']:
                continue
            # Get namefile including the migration folder itself
            filename = '/'.join(filename.rsplit('/', 2)[1:])
            # Include all *.sql.* files produced by Makefile.schema (for Oracle and PostgreSQL)
            for copy_file in glob.glob(r'%s/%s*'% (schema_path, filename)):
                changed_files.append(copy_file)
    return changed_files


def get_all_files_since(version, schema_path):
    """ Get all SQL files since a schema version """
    files = []
    for element in os.listdir(schema_path):
        element_ver = re.search(
            r'^susemanager-schema-(\d+\.\d+[\d|.]*)-to-susemanager-schema-\d+\.\d+[\d|.]*$',
            element)
        if not element_ver:
            continue
        # StrictVersion does not work as we have some migrations with version X.Y.W.Z
        # https://hg.python.org/cpython/file/tip/Lib/distutils/version.py#l93
        if DV.LooseVersion(element_ver.group(1)) >= DV.LooseVersion(version):
            base_path = schema_path + '/' + element
            for sql_file in os.listdir(base_path):
                if re.search(r'^.*\.(sql|sql\.postgresql|sql\.oracle)$', sql_file):
                    files.append(base_path + '/' + sql_file)
    return files


def find_latest_version(schema_path):
    """ Find the latest version available for a migration path """
    latest_version = '0.0'
    for element in os.listdir(schema_path):
        element_ver = re.search(
            r'^susemanager-schema-\d+\.\d+[\d|.]*-to-susemanager-schema-(\d+\.\d+[\d|.]*)$',
            element)
        if not element_ver:
            continue
        if DV.LooseVersion(element_ver.group(1)) > DV.LooseVersion(latest_version):
            latest_version = element_ver.group(1)
    return latest_version


def find_next_version(schema_path):
    """ Find the next version that should be created for a migration path """
    last_version = find_latest_version(schema_path)
    return last_version.rsplit('.', 1)[0] + '.' + str(int(last_version.rsplit('.', 1)[1])+1)


def create_fake_migration_path(schema_path, new_version, pr_file=None, version=None):
    """ Create a fake migration path with the next version after the last, adding
        all SQL files from a PR or since the version where all scripts started to be
        idempotent """
    if pr_file:
        print("Creating migration path with the scripts from the PR")
        files = get_all_files_from_pr(pr_file, schema_path)
    if version:
        print("Creating migration path with all scripts since %s" % version)
        files = get_all_files_since(version, schema_path)
    last_version = find_latest_version(schema_path)
    fake_path = schema_path + \
        '/susemanager-schema-%s-to-susemanager-schema-%s/' % (
            last_version, new_version)
    print("Creating: " + fake_path)
    os.mkdir(fake_path)
    for migration_file in files:
        m = re.search('susemanager-schema-\d+\.\d+[\d|.]*-to-susemanager-schema-(\d+\.\d+[\d|.]*)', migration_file)
        dest_file = os.path.join(fake_path, "%s-%s" %(m.group(1), os.path.basename(migration_file)))
        print("Copying %s to %s..." %(migration_file, dest_file))
        shutil.copy(migration_file, dest_file)


def run_command(command):
    """ Run a shell command """
    try:
        subprocess.check_call(command, shell=True)
        return True
    except subprocess.CalledProcessError as error:
        print("Return code was %s " % error.returncode)
        return False
    except Exception:
        raise


def manage_postgresql(action):
    """ Manage the postgresql service, currently start or stop """
    actions = ['start', 'stop']
    if action not in actions:
        raise "Invalid action for the PostgreSQL service"
    if run_command("/usr/bin/su - postgres -c '/usr/lib/postgresql10/bin/pg_ctl %s'" % action):
        print("PostgreSQL %sed" % action)
    else:
        raise RuntimeError("Could not %s PostgreSQL!" % action)


def dump_database(dump_name, excluded_tables=None):
    """ Dump a database using pg_dump """
    if excluded_tables is None:
        excluded_tables = []
    db_name = None
    with open(sys.path[0] + '/clear-db-answers-pgsql.txt') as file:
        for line in file.read().splitlines():
            if line.split('=')[0] == "db-name":
                db_name = line.split('=')[1]
    command = "/usr/bin/su - postgres -c 'pg_dump --dbname=%s --file=%s" % (
        db_name, dump_name)
    for excluded_table in excluded_tables:
        command += " -T %s" % excluded_table
    command += "'"
    if run_command(command):
        print("%s dumped correctly to %s" % (db_name, dump_name))
    else:
        raise RuntimeError("Could not dump %s!" % db_name)


def run_upgrade(upgrade_script, new_version):
    """ Run a database upgrade up to the specified version """
    log_path = "/var/log/spacewalk/schema-upgrade/"
    print("Upgrading to %s..." % new_version)
    command = "export SUMA_TEST_SCHEMA_VERSION=%s; %s -y" % (
        new_version, upgrade_script)
    if run_command(command):
        print("Upgrade executed successfully")
    else:
        for element in os.listdir(log_path):
            if re.search(r'^schema-from-.*\.log$', element):
                print("Content of file %s/%s" % (log_path, element))
                with open(log_path + '/' + element) as file:
                    for line in file.read().splitlines():
                        print(line)
        raise RuntimeError(
            "Upgrade failed! Most probably the new SQL scripts are not idempotent!")


def diff_dumps(initial_dump, migrated_dump):
    """ Perform a diff of two database dumps """
    file_initial = open(initial_dump)
    file_migrated = open(migrated_dump)
    return difflib.unified_diff(file_initial.readlines(), file_migrated.readlines())


def argparser():
    """ Parse arguments from the user """
    parser = argparse.ArgumentParser(
        description="Test idempotency of SQL scripts. This script assumes a script schema_migration_test_oracle-*to*.sh ran before, and PostgreSQL is stopped")
    parser.add_argument("-s", "--schema-path", action="store", dest="schema_path",
                        help="Path where the directories with the schema upgrades are (default: /etc/sysconfig/rhn/schema-upgrade",
                        default="/etc/sysconfig/rhn/schema-upgrade")
    parser.add_argument("-p", "--from-pr", action="store", dest="pr_file",
                        help="Check files with changes from a PR by rerunning added or changed files. The value is the path to a JSON file with PR information generated by gitarro")
    parser.add_argument("-v", "--from-version", action="store", dest="version",
                        help="Check all files since a base version (for example 4.0.1)")
    parser.add_argument("-u", "--upgrade-script", action="store", dest="upgrade_script",
                        help="Path where the spacewalk-schema-upgrade script is (default: /manager/schema/spacewalk/spacewalk-schema-upgrade)",
                        default="/manager/schema/spacewalk/spacewalk-schema-upgrade")
    args = parser.parse_args()
    args.schema_path = args.schema_path.rstrip('/')
    if not os.path.isdir(args.schema_path):
        raise RuntimeError("Directory %s does not exist" % args.schema_path)
    if args.pr_file is None and args.version is None:
        raise RuntimeError(
            "One of --from-pr or --from-version must be specified")
    if args.pr_file and args.version:
        raise RuntimeError(
            "Only one of --from-pr or --from-version must be specified")
    if args.pr_file and not os.path.isfile(args.pr_file):
        raise RuntimeError("File %s does not exist" % args.pr_file)
    if not os.path.isfile(args.upgrade_script):
        raise RuntimeError(
            "The file %s for the upgrade script does not exist" % args.upgrade_script)
    # Exclude tables and sequences that are not updated by the SQL scripts (version control)
    args.excluded_tables = ["pg_catalog.setval", "public.rhnpackageevr",
                            "public.rhn_pkg_evr_seq", "public.rhnversioninfo"]
    return args


def main():
    """ Main function """
    print("============================================================")
    print("                     Idempotency test                       ")
    print("============================================================")
    args = argparser()
    # Calculate new version for fake migrations
    new_version = find_next_version(args.schema_path)
    create_fake_migration_path(
        args.schema_path, new_version, args.pr_file, args.version)
    manage_postgresql('start')
    initial_dump = "/tmp/initial_dump.sql"
    dump_database(initial_dump, args.excluded_tables)
    run_upgrade(args.upgrade_script, new_version)
    migrated_dump = "/tmp/migrated_dump.sql"
    dump_database(migrated_dump, args.excluded_tables)
    manage_postgresql('stop')
    diff = False
    for line in diff_dumps(initial_dump, migrated_dump):
        diff = True
        print(line)
    if not diff:
        print("The dumps are equal: scripts are idempotent")
        sys.exit(0)
    if args.pr_file:
        raise RuntimeError(
            "Dumps %s and %s are different! One or more scripts from the PR are not idempotent!" % (initial_dump, migrated_dump))
    if args.version:
        raise RuntimeError(
            "Dumps %s and %s are different! One or more of the scripts since %s are not idempotent!" % (initial_dump, migrated_dump, args.version))


if __name__ == "__main__":
    try:
        main()
    except RuntimeError as error:
        print("ERROR: %s" % error)
        sys.exit(1)
    except Exception as error:
        print("ERROR: %s" % error)
        raise
