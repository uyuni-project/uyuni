#!/bin/bash
set -xe

src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

sudo -i podman exec uyuni-server-all-in-one-test bash -c "/usr/lib/susemanager/bin/mgr-setup -l /var/log/susemanager_setup.log -s"
sudo -i podman exec uyuni-server-all-in-one-test bash -c "/usr/bin/spacewalk-schema-upgrade -y"

# Make sure latest sql migration scripts have been executed for both the main and the reporting database
available_schemas=("spacewalk" "reportdb")
for schema in ${available_schemas[@]}; do
    specfile=$(find ${src_dir}/schema/${schema}/ -name *.spec)
    # Use Perl extended regexp and look-around assertions to extract only the values from the spec properties
    schema_name=$(grep -oP "Name:\s+\K(.*)$" ${specfile})
    schema_version=$(grep -oP "Version:\s+\K(.*)$" ${specfile})

    sudo -i podman exec uyuni-server-all-in-one-test bash -c "/testsuite/podman_runner/run_db_migrations.sh ${schema_name} ${schema_version}"
done

