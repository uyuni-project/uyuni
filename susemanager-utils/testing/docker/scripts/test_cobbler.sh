#!/bin/bash

set -e

zypper --non-interactive --gpg-auto-import-keys ref

# Packages required to run the cobbler unit tests
zypper in -y  --no-recommends cobbler-tests

cp /root/cobbler-apache.conf /etc/apache2/conf.d/cobbler.conf
cp /root/modules.conf /etc/cobbler/modules.conf
cp /root/cobbler_web.conf /etc/apache2/vhosts.d/cobbler_web.conf
cp /root/apache2 /etc/sysconfig/apache2
cp /root/sample.ks /var/lib/cobbler/kickstarts/sample.ks

# migrate modules.conf
/usr/share/cobbler/bin/settings-migration-v1-to-v2.sh -s

# start apache - required by cobbler tests
/usr/sbin/start_apache2 -D SYSTEMD  -k start

# start cobbler daemon
cobblerd

# Configure DHCP
sed -i 's/DHCPD_INTERFACE=""/DHCPD_INTERFACE="ANY"/' /etc/sysconfig/dhcpd
echo "subnet 172.17.0.0 netmask 255.255.255.0 {}"  >> /etc/dhcpd.conf

# Configure PAM
useradd -p $(perl -e 'print crypt("test", "password")') test

sh /root/cobbler/setup-supervisor.sh

# execute the tests

cd /usr/share/cobbler/tests

pytest --junitxml=/reports/cobbler.xml \
    --deselect=modules/authentication/ldap_test.py::TestLdap::test_anon_bind_negative[True-test-bad] \
    --deselect=modules/authentication/ldap_test.py::TestLdap::test_anon_bind_positive[True] \
    --deselect=modules/authentication/ldap_test.py::TestLdap::test_anon_bind_positive[True-test-test] \
    --deselect=modules/authentication/ldap_test.py::TestLdap::test_cadir_positive[/etc/ssl/certs-/etc/ssl/ldap.crt-/etc/ssl/ldap.key] \
    --deselect=modules/authentication/ldap_test.py::TestLdap::test_cafile_positive[/etc/ssl/ca-slapd.crt-/etc/ssl/ldap.crt-/etc/ssl/ldap.key] \
    --deselect=modules/authentication/ldap_test.py::TestLdap::test_ldaps_positive[/etc/ssl/ca-slapd.crt-/etc/ssl/ldap.crt-/etc/ssl/ldap.key] \
    --deselect=modules/authentication/ldap_test.py::TestLdap::test_user_bind_positive[False-uid=user,dc=example,dc=com-test-test-test] \
    --deselect=modules/authentication/pam_test.py::TestPam::test_authenticate \
    --deselect=tftpgen_test.py::test_copy_single_distro_file \
    --deselect=utils_test.py::test_blender \
    --deselect=utils_test.py::test_get_file_device_path \
    --deselect=utils_test.py::test_is_safe_to_hardlink[/etc/os-release-/tmp-False] \
    --deselect=utils_test.py::test_local_get_cobbler_api_url \
    --deselect=utils_test.py::test_service_restart_supervisord \
    --deselect=xmlrpcapi/image_test.py::TestImage::test_copy_image \
    --deselect=xmlrpcapi/image_test.py::TestImage::test_find_image \
    --deselect=xmlrpcapi/image_test.py::TestImage::test_get_image \
    --deselect=xmlrpcapi/image_test.py::TestImage::test_remove_image \
    --deselect=xmlrpcapi/image_test.py::TestImage::test_rename_image \
    --deselect=xmlrpcapi/non_object_calls_test.py::test_get_item_resolved_value[interfaces-system-expected_result4-expected_exception4]
