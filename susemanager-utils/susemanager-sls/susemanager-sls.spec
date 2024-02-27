#
# spec file for package susemanager-sls
#
# Copyright (c) 2024 SUSE LLC
#
# All modifications and additions to the file contributed by third parties
# remain the property of their copyright owners, unless otherwise agreed
# upon. The license for this file, and modifications and additions to the
# file, is the same license as for the pristine package itself (unless the
# license for the pristine package is not an Open Source License, in which
# case the license is the MIT License). An "Open Source License" is a
# license that conforms to the Open Source Definition (Version 1.9)
# published by the Open Source Initiative.

# Please submit bugfixes or comments via https://bugs.opensuse.org/
#


%if 0%{?suse_version} > 1320 || 0%{?rhel}
# SLE15 builds on Python 3
%global build_py3   1
%endif

%if 0%{?suse_version}
%global serverdir  /srv
%global wwwpubroot %{serverdir}/www/htdocs
%else
%global serverdir  %{_localstatedir}
%global wwwpubroot %{serverdir}/www/html
%endif

Name:           susemanager-sls
Version:        5.0.4
Release:        0
Summary:        Static Salt state files for SUSE Manager
License:        Apache-2.0 AND LGPL-2.1-only
Group:          Applications/Internet
Source:         %{name}-%{version}.tar.gz
Source1:        https://raw.githubusercontent.com/uyuni-project/uyuni/%{name}-%{version}-1/susemanager-utils/susemanager-sls/%{name}-rpmlintrc
Requires(pre):  coreutils
Requires(posttrans):spacewalk-admin
Requires:       susemanager-build-keys-web >= 15.4.2
%if 0%{?build_py3}
BuildRequires:  python3-pytest
BuildRequires:  python3-salt
BuildRequires:  python3-spacewalk-certs-tools
# Different package names for SUSE and RHEL:
Requires:       (python3-PyYAML >= 5.1 or python3-pyyaml >= 5.1)
%else
BuildRequires:  python-mock
BuildRequires:  python-pytest
BuildRequires:  python-salt
Requires:       python-PyYAML >= 5.1
%endif
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch

%description
Static Salt state files for SUSE Manager, where generic operations are
provided for the integration between infrastructure components.

%package -n uyuni-config-modules
Summary:        Salt modules to configure a Server
Group:          Applications/Internet

%description -n uyuni-config-modules
This package contains Salt execution and state modules that can be used
to configure a SUSE Manager or Uyuni Server.

%prep
%setup -q

%build

%install
mkdir -p %{buildroot}/usr/share/susemanager/salt/_grains
mkdir -p %{buildroot}/usr/share/susemanager/salt/_beacons
mkdir -p %{buildroot}/usr/share/susemanager/salt/_modules
mkdir -p %{buildroot}/usr/share/susemanager/salt/_states
mkdir -p %{buildroot}/usr/share/susemanager/salt-ssh
mkdir -p %{buildroot}/usr/share/susemanager/modules/pillar
mkdir -p %{buildroot}/usr/share/susemanager/modules/tops
mkdir -p %{buildroot}/usr/share/susemanager/modules/runners
mkdir -p %{buildroot}/usr/share/susemanager/modules/engines
mkdir -p %{buildroot}/usr/share/susemanager/modules/roster
mkdir -p %{buildroot}/usr/share/susemanager/pillar_data
mkdir -p %{buildroot}/usr/share/susemanager/formulas
mkdir -p %{buildroot}/usr/share/susemanager/formulas/metadata
mkdir -p %{buildroot}/usr/share/susemanager/reactor
mkdir -p %{buildroot}/usr/share/susemanager/scap
mkdir -p %{buildroot}/srv/formula_metadata
cp -R salt/* %{buildroot}/usr/share/susemanager/salt
cp -R salt-ssh/* %{buildroot}/usr/share/susemanager/salt-ssh
cp -R modules/pillar/* %{buildroot}/usr/share/susemanager/modules/pillar
cp -R modules/tops/* %{buildroot}/usr/share/susemanager/modules/tops
cp -R modules/runners/* %{buildroot}/usr/share/susemanager/modules/runners
cp -R modules/engines/* %{buildroot}/usr/share/susemanager/modules/engines
cp -R modules/roster/* %{buildroot}/usr/share/susemanager/modules/roster
cp -R formulas/* %{buildroot}/usr/share/susemanager/formulas
cp -R formula_metadata/* %{buildroot}/srv/formula_metadata
cp -R reactor/* %{buildroot}/usr/share/susemanager/reactor
cp -R scap/* %{buildroot}/usr/share/susemanager/scap

# Manually install Python part to already prepared structure
cp src/beacons/*.py %{buildroot}/usr/share/susemanager/salt/_beacons
cp src/grains/*.py %{buildroot}/usr/share/susemanager/salt/_grains/
rm %{buildroot}/usr/share/susemanager/salt/_grains/__init__.py
cp src/modules/*.py %{buildroot}/usr/share/susemanager/salt/_modules
rm %{buildroot}/usr/share/susemanager/salt/_modules/__init__.py
cp src/states/*.py %{buildroot}/usr/share/susemanager/salt/_states
rm %{buildroot}/usr/share/susemanager/salt/_states/__init__.py

# Install doc, examples
mkdir -p %{buildroot}/usr/share/doc/packages/uyuni-config-modules/examples/ldap
cp src/doc/* %{buildroot}/usr/share/doc/packages/uyuni-config-modules/
cp src/examples/uyuni_config_hardcode.sls %{buildroot}/usr/share/doc/packages/uyuni-config-modules/examples
cp src/examples/ldap/* %{buildroot}/usr/share/doc/packages/uyuni-config-modules/examples/ldap

%check
cd test
# Run py.test-3 for rhel
py.test%{?rhel:-3} test_pillar_suma_minion.py
cd ../src/tests
py.test%{?rhel:-3}

# Check that SLS files don't contain any call to "module.run" which has
# been replaced by "mgrcompat.module_run" calls.
! grep --include "*.sls" -r "module\.run" %{buildroot}/usr/share/susemanager/salt || exit 1

%pre
# change /usr/share/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT
# from symlink into a real file
if [ -L /usr/share/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT ]; then
  rm -f /usr/share/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT
  if [ -f /etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT ]; then
    cp /etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT \
       /usr/share/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT
  elif [ -f /etc/pki/ca-trust/source/anchors/RHN-ORG-TRUSTED-SSL-CERT ]; then
    cp /etc/pki/ca-trust/source/anchors/RHN-ORG-TRUSTED-SSL-CERT \
       /usr/share/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT
  fi
fi

%post
# this will be filled with content when a certificate gets deployed
if [ ! -e /usr/share/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT ]; then
  touch /usr/share/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT
fi

%posttrans
# Run JMX exporter as Java Agent (bsc#1184617)
grep -q 'prometheus_monitoring_enabled\s*=\s*1\s*$' /etc/rhn/rhn.conf
if [[ $? == 0 ]]; then
  /usr/sbin/mgr-monitoring-ctl enable
fi

%files
%defattr(-,root,root)
%dir /usr/share/susemanager
/usr/share/susemanager/salt
/usr/share/susemanager/salt-ssh
/usr/share/susemanager/pillar_data
/usr/share/susemanager/modules
/usr/share/susemanager/modules/pillar
/usr/share/susemanager/modules/tops
/usr/share/susemanager/modules/runners
/usr/share/susemanager/modules/engines
/usr/share/susemanager/modules/roster
/usr/share/susemanager/formulas
/usr/share/susemanager/reactor
/usr/share/susemanager/scap
/srv/formula_metadata
%exclude /usr/share/susemanager/salt/_modules/uyuni_config.py
%exclude /usr/share/susemanager/salt/_states/uyuni_config.py
%ghost /usr/share/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT

%files -n uyuni-config-modules
%defattr(-,root,root)
%dir /usr/share/susemanager
/usr/share/susemanager/salt/_modules/uyuni_config.py
/usr/share/susemanager/salt/_states/uyuni_config.py
%dir /usr/share/doc/packages/uyuni-config-modules
%doc /usr/share/doc/packages/uyuni-config-modules/*
%doc /usr/share/doc/packages/uyuni-config-modules/examples/*
%doc /usr/share/doc/packages/uyuni-config-modules/examples/ldap/*

%changelog
