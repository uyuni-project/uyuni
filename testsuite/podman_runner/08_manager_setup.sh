#!/bin/bash
set -xe

src_dir=$(cd $(dirname "$0")/../.. && pwd -P)
env_file=$(dirname "$0")/env.server

sudo -i podman exec --env-file=${env_file} uyuni-server-all-in-one-test bash -c "/usr/lib/susemanager/bin/mgr-setup -l /var/log/susemanager_setup.log -s"
sudo -i podman exec uyuni-server-all-in-one-test bash -c "/usr/bin/spacewalk-schema-upgrade -y"

# Make sure latest sql migration scripts have been executed for both the main and the reporting database
available_schemas=("spacewalk" "reportdb")
for schema in "${available_schemas[@]}"; do
    specfile=$(find "${src_dir}/schema/${schema}/" -name '*.spec')
    # Get the package name from the spec using Perl extended regexp and look-around assertions to extract only its value
    schema_name=$(grep -oP "Name:\s+\K(.*)$" "${specfile}")
    # Check the version of the package installed in the podman container
    schema_version=$(sudo -i podman exec uyuni-server-all-in-one-test rpm -q --queryformat '%{version}' "${schema_name}")

    # Run the missing migrations and only those, to ensure no script is out of place
    sudo -i podman exec uyuni-server-all-in-one-test bash -c "/testsuite/podman_runner/run_db_migrations.sh ${schema_name} ${schema_version}"
done
