#!/bin/bash
set -xe

src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

sudo -i podman exec uyuni-server bash -c "/testsuite/podman_runner/provide-db-schema.sh"
sudo -i podman exec -e CERT_O="test_org" -e CERT_OU="test_ou" -e CERT_CITY="test_city" -e CERT_STATE="test_state" -e CERT_COUNTRY="DE" -e CERT_EMAIL="a@b.com" -e CERT_CNAMES="uyuni-server" -e CERT_PASS="spacewalk" -e UYUNI_FQDN="uyuni-server" -e MANAGER_USER="admin" -e MANAGER_PASS="spacewalk" -e MANAGER_ADMIN_EMAIL="a@b.com" -e MANAGER_MAIL_FROM="a@b.com" -e MANAGER_ENABLE_TFTP="n" -e MANAGER_DB_NAME="manager" -e MANAGER_DB_HOST="localhost" -e MANAGER_DB_PORT="5432" -e MANAGER_DB_USER="manager" -e MANAGER_DB_PASS="manager" -e MANAGER_DB_PROTOCOL="TCP" -e REPORT_DB_NAME="reportdb" -e REPORT_DB_USER="pythia_susemanager" -e REPORT_DB_PASS="pythia_susemanager" -e EXTERNALDB_ADMIN_USER="" -e EXTERNALDB_ADMIN_PASS="" -e EXTERNALDB_PROVIDER="" -e ISS_PARENT="" -e ACTIVATE_SLP="" -e SCC_USER="" -e SCC_PASS="" uyuni-server bash -c "/usr/lib/susemanager/bin/mgr-setup -l /var/log/susemanager_setup.log -s"
sudo -i podman exec uyuni-server bash -c "/usr/bin/spacewalk-schema-upgrade -y"

# Make sure latest sql migration scripts have been executed for both the main and the reporting database
available_schemas=("spacewalk" "reportdb")
for schema in "${available_schemas[@]}"; do
    specfile=$(find "${src_dir}/schema/${schema}/" -name '*.spec')
    # Get the package name from the spec using Perl extended regexp and look-around assertions to extract only its value
    schema_name=$(grep -oP "Name:\s+\K(.*)$" "${specfile}")
    # Check the version of the package installed in the podman container
    schema_version=$(sudo -i podman exec uyuni-server rpm -q --queryformat '%{version}' "${schema_name}")

    # Run the missing migrations and only those, to ensure no script is out of place
    sudo -i podman exec uyuni-server bash -c "/testsuite/podman_runner/run_db_migrations.sh ${schema_name} ${schema_version}"
done
