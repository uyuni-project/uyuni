#
# spec file for package susemanager-sls
#
# Copyright (c) 2015-2018 SUSE LINUX GmbH, Nuernberg, Germany.
#
# All modifications and additions to the file contributed by third parties
# remain the property of their copyright owners, unless otherwise agreed
# upon. The license for this file, and modifications and additions to the
# file, is the same license as for the pristine package itself (unless the
# license for the pristine package is not an Open Source License, in which
# case the license is the MIT License). An "Open Source License" is a
# license that conforms to the Open Source Definition (Version 1.9)
# published by the Open Source Initiative.

# Please submit bugfixes or comments via http://bugs.opensuse.org/
#

Name:           susemanager-sls
Version:        3.2.20
Release:        1
License:        GPL-2.0
Summary:        Static Salt state files for SUSE Manager
Group:          Applications/Internet
Source:         %{name}-%{version}.tar.gz
Requires(pre):  coreutils
Requires:       susemanager-build-keys-web >= 12.0.1
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch

%description
Static Salt state files for SUSE Manager, where generic operations are
provided for the integration between infrastructure components.

%prep
%setup -q

%build

%install
mkdir -p %{buildroot}/usr/share/susemanager/salt/_grains
mkdir -p %{buildroot}/usr/share/susemanager/salt/_beacons
mkdir -p %{buildroot}/usr/share/susemanager/salt/_modules
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
cp src/modules/sumautil.py %{buildroot}/usr/share/susemanager/salt/_modules
cp src/modules/mainframesysinfo.py %{buildroot}/usr/share/susemanager/salt/_modules
cp src/modules/udevdb.py %{buildroot}/usr/share/susemanager/salt/_modules
cp src/modules/mgractionchains.py %{buildroot}/usr/share/susemanager/salt/_modules
cp src/modules/kiwi_info.py %{buildroot}/usr/share/susemanager/salt/_modules
cp src/modules/kiwi_source.py %{buildroot}/usr/share/susemanager/salt/_modules

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
%ghost /usr/share/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT
