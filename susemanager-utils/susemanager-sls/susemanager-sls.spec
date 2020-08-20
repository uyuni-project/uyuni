#
# spec file for package susemanager-sls
#
# Copyright (c) 2019 SUSE LINUX GmbH, Nuernberg, Germany.
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

%if 0%{?suse_version} > 1320
# SLE15 builds on Python 3
%global build_py3   1
%endif

Name:           susemanager-sls
Version:        4.2.0
Release:        1
Summary:        Static Salt state files for SUSE Manager
License:        GPL-2.0-only
Group:          Applications/Internet
Source:         %{name}-%{version}.tar.gz
Requires(pre):  coreutils
Requires:       susemanager-build-keys-web >= 12.0.1
%if 0%{?build_py3}
BuildRequires:  python3-pytest
BuildRequires:  python3-mock
BuildRequires:  python3-salt
Requires:       python3-PyYAML >= 5.1
%else
BuildRequires:  python-pytest
BuildRequires:  python-mock
BuildRequires:  python-salt
Requires:       python-PyYAML >= 5.1
%endif
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch

%description
Static Salt state files for SUSE Manager, where generic operations are
provided for the integration between infrastructure components.

%package -n susemanager-config-modules
Summary:        Salt modules to configure a Server
Group:          Applications/Internet

%description -n susemanager-config-modules
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
mkdir -p %{buildroot}/usr/share/susemanager/modules/pillar
mkdir -p %{buildroot}/usr/share/susemanager/modules/tops
mkdir -p %{buildroot}/usr/share/susemanager/modules/runners
mkdir -p %{buildroot}/usr/share/susemanager/modules/engines
mkdir -p %{buildroot}/usr/share/susemanager/pillar_data
mkdir -p %{buildroot}/usr/share/susemanager/formulas
mkdir -p %{buildroot}/usr/share/susemanager/formulas/metadata
mkdir -p %{buildroot}/usr/share/susemanager/reactor
mkdir -p %{buildroot}/usr/share/susemanager/scap
mkdir -p %{buildroot}/srv/formula_metadata
cp -R salt/* %{buildroot}/usr/share/susemanager/salt
cp -R modules/pillar/* %{buildroot}/usr/share/susemanager/modules/pillar
cp -R modules/tops/* %{buildroot}/usr/share/susemanager/modules/tops
cp -R modules/runners/* %{buildroot}/usr/share/susemanager/modules/runners
cp -R modules/engines/* %{buildroot}/usr/share/susemanager/modules/engines
cp -R pillar_data/* %{buildroot}/usr/share/susemanager/pillar_data
cp -R formulas/* %{buildroot}/usr/share/susemanager/formulas
cp -R formula_metadata/* %{buildroot}/srv/formula_metadata
cp -R reactor/* %{buildroot}/usr/share/susemanager/reactor
cp -R scap/* %{buildroot}/usr/share/susemanager/scap

# Manually install Python part to already prepared structure
cp src/beacons/pkgset.py %{buildroot}/usr/share/susemanager/salt/_beacons
cp src/beacons/virtpoller.py %{buildroot}/usr/share/susemanager/salt/_beacons
cp src/grains/cpuinfo.py %{buildroot}/usr/share/susemanager/salt/_grains/
cp src/grains/public_cloud.py %{buildroot}/usr/share/susemanager/salt/_grains/
cp src/modules/sumautil.py %{buildroot}/usr/share/susemanager/salt/_modules
cp src/modules/mainframesysinfo.py %{buildroot}/usr/share/susemanager/salt/_modules
cp src/modules/udevdb.py %{buildroot}/usr/share/susemanager/salt/_modules
cp src/modules/mgractionchains.py %{buildroot}/usr/share/susemanager/salt/_modules
cp src/modules/kiwi_info.py %{buildroot}/usr/share/susemanager/salt/_modules
cp src/modules/kiwi_source.py %{buildroot}/usr/share/susemanager/salt/_modules
cp src/modules/mgrclusters.py %{buildroot}/usr/share/susemanager/salt/_modules
cp src/modules/mgr_caasp_manager.py %{buildroot}/usr/share/susemanager/salt/_modules
cp src/modules/ssh_agent.py %{buildroot}/usr/share/susemanager/salt/_modules
cp src/modules/uyuni_users.py %{buildroot}/usr/share/susemanager/salt/_modules
cp src/states/product.py %{buildroot}/usr/share/susemanager/salt/_states
cp src/states/mgrcompat.py %{buildroot}/usr/share/susemanager/salt/_states
cp src/states/uyuni_users.py %{buildroot}/usr/share/susemanager/salt/_states

%check
cd test
py.test test_pillar_suma_minion.py
cd ../src/tests
py.test

# Check that SLS files don't contain any call to "module.run" which has
# been replaced by "mgrcompat.module_run" calls.
! grep --include "*.sls" -r "module\.run" %{buildroot}/usr/share/susemanager/salt || exit 1

%post
# HACK! Create broken link when it will be replaces with the real file
ln -sf /srv/www/htdocs/pub/RHN-ORG-TRUSTED-SSL-CERT \
   /usr/share/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT 2>&1 ||:
# Pre-create top.sls to suppress empty/absent top.sls warning/error (bsc#1017754)
USERLAND="/srv/salt"
TOP="$USERLAND/top.sls"
if [ -d "$USERLAND" ]; then
    if [ ! -f "$TOP" ]; then
	cat <<EOF >> $TOP
# This only calls no-op statement from
# /usr/share/susemanager/salt/util/noop.sls state
# Feel free to change it.

base:
  '*':
    - util.noop
EOF
    fi
fi

%files
%defattr(-,root,root)
%dir /usr/share/susemanager
/usr/share/susemanager/salt
/usr/share/susemanager/pillar_data
/usr/share/susemanager/modules
/usr/share/susemanager/modules/pillar
/usr/share/susemanager/modules/tops
/usr/share/susemanager/modules/runners
/usr/share/susemanager/modules/engines
/usr/share/susemanager/formulas
/usr/share/susemanager/reactor
/usr/share/susemanager/scap
/srv/formula_metadata
%exclude /usr/share/susemanager/salt/_modules/uyuni_users.py
%exclude /usr/share/susemanager/salt/_states/uyuni_users.py
%ghost /usr/share/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT

%files -n susemanager-config-modules
%defattr(-,root,root)
%dir /usr/share/susemanager
/usr/share/susemanager/salt/_modules/uyuni_users.py
/usr/share/susemanager/salt/_states/uyuni_users.py

%changelog
