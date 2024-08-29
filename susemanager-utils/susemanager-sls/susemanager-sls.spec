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
Version:        5.1.0
Release:        0
Summary:        Static Salt state files for SUSE Manager
License:        Apache-2.0 AND LGPL-2.1-only
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Applications/Internet
Source:         %{name}-%{version}.tar.gz
Source1:        https://raw.githubusercontent.com/uyuni-project/uyuni/%{name}-%{version}-0/susemanager-utils/susemanager-sls/%{name}-rpmlintrc
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
BuildArch:      noarch

%description
Static Salt state files for SUSE Manager, where generic operations are
provided for the integration between infrastructure components.

%package -n uyuni-config-modules
Summary:        Salt modules to configure a Server
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Applications/Internet

%description -n uyuni-config-modules
This package contains Salt execution and state modules that can be used
to configure a SUSE Manager or Uyuni Server.

%prep
%setup -q

%build

%install
mkdir -p %{buildroot}%{_datadir}/susemanager/salt/_grains
mkdir -p %{buildroot}%{_datadir}/susemanager/salt/_beacons
mkdir -p %{buildroot}%{_datadir}/susemanager/salt/_modules
mkdir -p %{buildroot}%{_datadir}/susemanager/salt/_states
mkdir -p %{buildroot}%{_datadir}/susemanager/salt-ssh
mkdir -p %{buildroot}%{_datadir}/susemanager/modules/pillar
mkdir -p %{buildroot}%{_datadir}/susemanager/modules/tops
mkdir -p %{buildroot}%{_datadir}/susemanager/modules/runners
mkdir -p %{buildroot}%{_datadir}/susemanager/modules/engines
mkdir -p %{buildroot}%{_datadir}/susemanager/modules/roster
mkdir -p %{buildroot}%{_datadir}/susemanager/pillar_data
mkdir -p %{buildroot}%{_datadir}/susemanager/formulas
mkdir -p %{buildroot}%{_datadir}/susemanager/formulas/metadata
mkdir -p %{buildroot}%{_datadir}/susemanager/reactor
mkdir -p %{buildroot}%{_datadir}/susemanager/scap
mkdir -p %{buildroot}/srv/formula_metadata
cp -R salt/* %{buildroot}%{_datadir}/susemanager/salt
cp -R salt-ssh/* %{buildroot}%{_datadir}/susemanager/salt-ssh
cp -R modules/pillar/* %{buildroot}%{_datadir}/susemanager/modules/pillar
cp -R modules/tops/* %{buildroot}%{_datadir}/susemanager/modules/tops
cp -R modules/runners/* %{buildroot}%{_datadir}/susemanager/modules/runners
cp -R modules/engines/* %{buildroot}%{_datadir}/susemanager/modules/engines
cp -R modules/roster/* %{buildroot}%{_datadir}/susemanager/modules/roster
cp -R formulas/* %{buildroot}%{_datadir}/susemanager/formulas
cp -R formula_metadata/* %{buildroot}/srv/formula_metadata
cp -R reactor/* %{buildroot}%{_datadir}/susemanager/reactor
cp -R scap/* %{buildroot}%{_datadir}/susemanager/scap

# Manually install Python part to already prepared structure
cp src/beacons/*.py %{buildroot}%{_datadir}/susemanager/salt/_beacons
cp src/grains/*.py %{buildroot}%{_datadir}/susemanager/salt/_grains/
rm %{buildroot}%{_datadir}/susemanager/salt/_grains/__init__.py
cp src/modules/*.py %{buildroot}%{_datadir}/susemanager/salt/_modules
rm %{buildroot}%{_datadir}/susemanager/salt/_modules/__init__.py
cp src/states/*.py %{buildroot}%{_datadir}/susemanager/salt/_states
rm %{buildroot}%{_datadir}/susemanager/salt/_states/__init__.py

# Install doc, examples
mkdir -p %{buildroot}%{_docdir}/uyuni-config-modules/examples/ldap
cp src/doc/* %{buildroot}%{_docdir}/uyuni-config-modules/
cp src/examples/uyuni_config_hardcode.sls %{buildroot}%{_docdir}/uyuni-config-modules/examples
cp src/examples/ldap/* %{buildroot}%{_docdir}/uyuni-config-modules/examples/ldap

%check
cd test
# Run py.test-3 for rhel
py.test%{?rhel:-3} test_pillar_suma_minion.py
cd ../src/tests
py.test%{?rhel:-3}

# Check that SLS files don't contain any call to "module.run" which has
# been replaced by "mgrcompat.module_run" calls.
! grep --include "*.sls" -r "module\.run" %{buildroot}%{_datadir}/susemanager/salt || exit 1

%pre
# change /usr/share/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT
# from symlink into a real file
if [ -L %{_datadir}/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT ]; then
  rm -f %{_datadir}/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT
  if [ -f %{_sysconfdir}/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT ]; then
    cp %{_sysconfdir}/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT \
       %{_datadir}/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT
  elif [ -f %{_sysconfdir}/pki/ca-trust/source/anchors/RHN-ORG-TRUSTED-SSL-CERT ]; then
    cp %{_sysconfdir}/pki/ca-trust/source/anchors/RHN-ORG-TRUSTED-SSL-CERT \
       %{_datadir}/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT
  fi
fi

%post
# when uyuni roster module has changed, we need to remove the cache
rm -f %{_localstatedir}/cache/salt/master/roster/uyuni/minions.p

# this will be filled with content when a certificate gets deployed
if [ ! -e %{_datadir}/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT ]; then
  touch %{_datadir}/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT
fi

%posttrans
# Run JMX exporter as Java Agent (bsc#1184617)
grep -q 'prometheus_monitoring_enabled\s*=\s*1\s*$' %{_sysconfdir}/rhn/rhn.conf
if [[ $? == 0 ]]; then
  %{_sbindir}/mgr-monitoring-ctl enable
fi

%files
%defattr(-,root,root)
%dir %{_datadir}/susemanager
%{_datadir}/susemanager/salt
%{_datadir}/susemanager/salt-ssh
%{_datadir}/susemanager/pillar_data
%{_datadir}/susemanager/modules
%{_datadir}/susemanager/modules/pillar
%{_datadir}/susemanager/modules/tops
%{_datadir}/susemanager/modules/runners
%{_datadir}/susemanager/modules/engines
%{_datadir}/susemanager/modules/roster
%{_datadir}/susemanager/formulas
%{_datadir}/susemanager/reactor
%{_datadir}/susemanager/scap
/srv/formula_metadata
%exclude %{_datadir}/susemanager/salt/_modules/uyuni_config.py
%exclude %{_datadir}/susemanager/salt/_states/uyuni_config.py
%ghost %{_datadir}/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT

%files -n uyuni-config-modules
%defattr(-,root,root)
%dir %{_datadir}/susemanager
%{_datadir}/susemanager/salt/_modules/uyuni_config.py
%{_datadir}/susemanager/salt/_states/uyuni_config.py
%dir %{_docdir}/uyuni-config-modules
%doc %{_docdir}/uyuni-config-modules/*
%doc %{_docdir}/uyuni-config-modules/examples/*
%doc %{_docdir}/uyuni-config-modules/examples/ldap/*

%changelog
