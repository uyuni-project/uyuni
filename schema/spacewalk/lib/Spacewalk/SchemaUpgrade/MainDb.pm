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

package Spacewalk::SchemaUpgrade::MainDb;

use strict;
use warnings FATAL => 'all';

use constant {
    DB_NAME => 'postgres',
    UPGRADE_PREFIX   => 'schema',
    BACKEND_PROPERTY => 'db_backend',
};

sub get_schema_version_query {
    return "
     SELECT pn.name || '-' || (pe.evr).version || '-' || (pe.evr).release
       FROM rhnVersionInfo vi
               INNER JOIN rhnPackageName pn ON vi.name_id = pn.id
               INNER JOIN rhnPackageEVR pe ON vi.evr_id = pe.id
      WHERE vi.label = 'schema';
";
}

sub get_migration_dir_query {
    return "
     SELECT label
       FROM rhnVersionInfo
      WHERE label LIKE 'schema-from%'
   ORDER BY label DESC;
";
}

sub update_migration_dir {
    shift if $_[0] eq __PACKAGE__;
    my ($migration_dir) = @_;

    return "
     UPDATE rhnVersionInfo
        SET label = '$migration_dir'
                , modified = current_timestamp
      WHERE label = 'schema';
";
}

sub insert_version_info {
    shift if $_[0] eq __PACKAGE__;
    my ($schema_name, $schema_version, $schema_release) = @_;

    return "
INSERT INTO rhnVersionInfo( label, name_id, evr_id, created, modified )
     VALUES ('schema'
                , lookup_package_name('$schema_name')
                , lookup_evr(null, '$schema_version' , '$schema_release', 'rpm' )
                , current_timestamp
                , current_timestamp
            );
     COMMIT;
";
}

1;
