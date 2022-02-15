#  Copyright (c) 2022 SUSE LLC
#
#  This software is licensed to you under the GNU General Public License,
#  version 2 (GPLv2). There is NO WARRANTY for this software, express or
#  implied, including the implied warranties of MERCHANTABILITY or FITNESS
#  FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
#  along with this software; if not, see
#  http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
#  Red Hat trademarks are not licensed under GPLv2. No permission is
#  granted to use or replicate Red Hat trademarks that are incorporated
#  in this software or its documentation.

package Spacewalk::SchemaUpgrade::ReportDb;

use strict;
use warnings FATAL => 'all';

use constant {
    DB_NAME => 'reportdb',
    UPGRADE_PREFIX   => 'reportdb-schema',
    BACKEND_PROPERTY => 'report_db_backend',
};

sub get_schema_version_query {
    return "
     SELECT name || '-' || version || '-' || release
       FROM versioninfo
      WHERE label = 'schema';
";
}

sub get_migration_dir_query {
    return "
     SELECT label
       FROM versioninfo
      WHERE label LIKE 'schema-from%'
   ORDER BY label DESC;
";
}

sub update_migration_dir {
    shift if $_[0] eq __PACKAGE__;
    my ($migration_dir) = @_;

    return "
     UPDATE versioninfo
        SET label = '$migration_dir'
                , modified = current_timestamp
      WHERE label = 'schema';
";
}

sub insert_version_info {
    shift if $_[0] eq __PACKAGE__;
    my ($schema_name, $schema_version, $schema_release) = @_;

    return "
INSERT INTO versioninfo( label, name, version, release )
     VALUES ('schema'
                , '$schema_name'
                , '$schema_version'
                , '$schema_release'
            );
     COMMIT;
";
}

1;
