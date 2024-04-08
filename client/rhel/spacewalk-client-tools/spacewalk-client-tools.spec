#
# spec file for package spacewalk-client-tools
#
# Copyright (c) 2024 SUSE LLC
# Copyright (c) 2008-2018 Red Hat, Inc.
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


%if 0%{?fedora} || 0%{?suse_version} > 1320 || 0%{?rhel} >= 8 || 0%{?mageia}
%global build_py3   1
%global default_py3 1
%global __python /usr/bin/python3
%endif

%if !(0%{?rhel} >= 8 || 0%{?sle_version} >= 150000 )
%global build_py2   1
%endif

%if "%{_vendor}" == "debbuild"
%{!?_presetdir:%global _presetdir /lib/systemd/system-preset}
# Bash constructs in scriptlets don't play nice with Debian's default shell, dash
%global _buildshell /bin/bash
%endif

%{!?__python2:%global __python2 /usr/bin/python2}
%{!?__python3:%global __python3 /usr/bin/python3}

%if %{undefined python2_version}
%global python2_version %(%{__python2} -Esc "import sys; sys.stdout.write('{0.major}.{0.minor}'.format(sys.version_info))")
%endif

%if %{undefined python3_version}
%global python3_version %(%{__python3} -Ic "import sys; sys.stdout.write(sys.version[:3])")
%endif

%if %{undefined python2_sitelib}
%global python2_sitelib %(%{__python2} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")
%endif

%if %{undefined python3_sitelib}
%global python3_sitelib %(%{__python3} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")
%endif

%if "%{_vendor}" == "debbuild"
# For making sure we can set the right args for deb distros
%global is_deb 1
%endif

%define pythonX %{?default_py3: python3}%{!?default_py3: python2}

# package renaming fun :(
%define rhn_client_tools spacewalk-client-tools
%define rhn_setup	 spacewalk-client-setup
%define rhn_check	 spacewalk-check
#
%bcond_with    test

Name:           spacewalk-client-tools
Summary:        Support programs and libraries for Spacewalk
License:        GPL-2.0-only
%if "%{_vendor}" == "debbuild"
Packager:       Uyuni Project <devel@lists.uyuni-project.org>
Group:          admin
%else
Group:          System Environment/Base
%endif
Version:        5.0.4
Source0:        %{name}-%{version}.tar.gz
Source1:        https://raw.githubusercontent.com/uyuni-project/uyuni/%{name}-%{version}-1/client/rhel/%{name}/%{name}-rpmlintrc
URL:            https://github.com/uyuni-project/uyuni
Release:        0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
%if 0%{?fedora} || 0%{?rhel} || 0%{?suse_version} >= 1210 || 0%{?mageia} >= 6
BuildArch:      noarch
%endif
%if 0%{?suse_version}
BuildRequires:  update-desktop-files
%endif
Provides:       rhn-client-tools = %{version}-%{release}
Obsoletes:      rhn-client-tools < %{version}-%{release}
Requires:       %{pythonX}-%{name} = %{version}-%{release}
%if "%{_vendor}" != "debbuild"
Requires:       coreutils
Requires:       gnupg
Requires:       rpm >= 4.2.3-24_nonptl

%if 0%{?suse_version}
Requires:       zypper
%else
%if 0%{?fedora} || 0%{?rhel} >= 8
Requires:       dnf
%else
Requires:       yum
%endif # 0{?fedora}
%endif # 0{?suse_version}
%endif # {_vendor} != "debbuild"

%if "%{_vendor}" == "debbuild"
Requires:       apt
%if 0%{?ubuntu} >= 1804
Requires:       gpg
%else
Requires:       gnupg
%endif
Requires:       coreutils
%endif
BuildRequires:  rpm

Conflicts:      rhn-kickstart < 5.4.3-1
Conflicts:      rhncfg < 5.9.23-1
Conflicts:      up2date < 5.0.0
Conflicts:      yum-rhn-plugin < 1.6.4-1

BuildRequires:  desktop-file-utils
BuildRequires:  gettext
BuildRequires:  intltool
BuildRequires:  make

%if 0%{?fedora}
BuildRequires:  dnf
BuildRequires:  fedora-logos
%endif

%if 0%{?mageia} >= 6
BuildRequires:  dnf
%endif

%if 0%{?rhel}
BuildRequires:  redhat-logos
%if 0%{?rhel} >= 8
BuildRequires:  dnf
%else
BuildRequires:  yum
%endif
%endif

# For the systemd presets
%if 0%{?fedora} || 0%{?mageia} || 0%{?debian} >= 8 || 0%{?ubuntu} >= 1504 || 0%{?sle_version} >= 120000 || 0%{?rhel} >= 7
BuildRequires:  systemd
Requires:       systemd
%endif

%description
Spacewalk Client Tools provides programs and libraries to allow your
system to receive software updates from Spacewalk.

%if 0%{?build_py2}
%package -n python2-%{name}
Summary:        Support programs and libraries for Spacewalk
%if "%{_vendor}" == "debbuild"
Group:          python
%else
Group:          System Environment/Base
%endif
Provides:       python-%{name} = %{version}-%{release}
Obsoletes:      python-%{name} < %{version}-%{release}
Provides:       python2-rhn-client-tools = %{version}-%{release}
Obsoletes:      python2-rhn-client-tools < %{version}-%{release}
Requires:       %{name} = %{version}-%{release}
Requires:       rhnlib >= 4.2.2

%if "%{_vendor}" != "debbuild"
Requires:       python2-uyuni-common-libs
Requires:       rpm-python
%ifnarch s390 s390x
Requires:       python-dmidecode
%endif
Requires:       python-ethtool >= 0.4
BuildRequires:  python-devel
%if 0%{?fedora}
Requires:       libgudev
Requires:       pygobject2
Requires:       python-hwdata
%else
%if 0%{?suse_version} >= 1140
Requires:       python-hwdata
Requires:       python-pyudev
%else
%if 0%{?rhel} > 5
Requires:       python-gudev
Requires:       python-hwdata
%else
Requires:       hal >= 0.5.8.1-52
%endif # 0{?rhel} > 5
%endif # 0{?suse_version} >= 1140
%endif # 0{?fedora}

%if 0%{?rhel} == 5
Requires:       newt
%endif

%if 0%{?rhel} > 5 || 0%{?fedora}
Requires:       newt-python
%endif

%if 0%{?suse_version}
Requires:       dbus-1-python
Requires:       python-newt
%else
Requires:       dbus-python
%endif # 0{?suse_version}
Requires:       logrotate

%if %{with test} && 0%{?rhel} != 6
# The following BuildRequires are for check only
BuildRequires:  python-coverage
BuildRequires:  rpm-python
%endif
%endif # if {_vendor} != "debbuild"

%if "%{_vendor}" == "debbuild"
Requires:       python-dmidecode
Requires:       python-ethtool >= 0.4
Requires:       python-rpm
BuildRequires:  python-dev
Requires:       python2-hwdata
BuildRequires:  python-coverage
BuildRequires:  python-rpm
Requires:       gir1.2-gudev-1.0
Requires:       python-dbus
Requires:       python-gi
Requires:       python-newt
Requires:       python-pyudev
Requires(preun):python-minimal
Requires(post): python-minimal
%endif

%description -n python2-%{name}
Python 2 specific files of %{name}.
%endif

%if 0%{?build_py3}
%package -n python3-%{name}
Summary:        Support programs and libraries for Spacewalk
%if "%{_vendor}" == "debbuild"
Group:          python
%else
Group:          System Environment/Base
%endif
Provides:       python3-rhn-client-tools = %{version}-%{release}
Obsoletes:      python3-rhn-client-tools < %{version}-%{release}
Requires:       %{name} = %{version}-%{release}
%if "%{_vendor}" != "debbuild"
%if 0%{?suse_version}
%if 0%{?suse_version} >= 1500
Requires:       python3-dbus-python
%else
Requires:       dbus-1-python3
%endif
Requires:       libgudev-1_0-0
Requires:       python3-newt
Requires:       python3-pyudev
%else
Requires:       libgudev
Requires:       newt-python3
Requires:       python3-dbus
Requires:       python3-gobject-base
%endif
BuildRequires:  python3-devel
BuildRequires:  python3-rpm-macros
%endif

%ifnarch s390 s390x
Requires:       python3-dmidecode
%endif
Requires:       python3-hwdata
Requires:       python3-netifaces
Requires:       python3-rhnlib >= 4.2.2
Requires:       python3-rpm
Requires:       python3-uyuni-common-libs

%if "%{_vendor}" == "debbuild"
BuildRequires:  python3-dev
Requires:       gir1.2-gudev-1.0
Requires:       python3-dbus
Requires:       python3-gi
Requires:       python3-newt
Requires:       python3-pyudev
Requires(preun):python3-minimal
Requires(post): python3-minimal
%endif

%if %{with test} && 0%{?rhel} != 6
# The following BuildRequires are for check only
BuildRequires:  python3-coverage
BuildRequires:  python3-rpm
%endif

%description -n python3-%{name}
Python 3 specific files of %{name}.
%endif

%package -n spacewalk-check
Summary:        Check for Spacewalk actions
Provides:       rhn-check = %{version}-%{release}
Obsoletes:      rhn-check < %{version}-%{release}
Requires:       %{name} = %{version}-%{release}
Requires:       %{pythonX}-spacewalk-check = %{version}-%{release}
%if "%{_vendor}" != "debbuild"
Group:          System Environment/Base
%if 0%{?suse_version}
Requires:       zypp-plugin-spacewalk >= 1.0.2
%else
%if 0%{?fedora} || 0%{?rhel} >= 8
Requires:       dnf-plugin-spacewalk >= 2.4.0
%else
Requires:       yum-rhn-plugin >= 2.8.2
%endif
%endif
%endif

%if "%{_vendor}" == "debbuild"
Requires:       apt-transport-spacewalk
%endif

%description -n spacewalk-check
spacewalk-check polls a SUSE Manager or Spacewalk server to find and execute
scheduled actions.

%if 0%{?build_py2}
%package -n python2-spacewalk-check
Summary:        Check for RHN actions
Group:          System Environment/Base
Provides:       python-spacewalk-check = %{version}-%{release}
Obsoletes:      python-spacewalk-check < %{version}-%{release}
Provides:       python2-rhn-check = %{version}-%{release}
Obsoletes:      python2-rhn-check < %{version}-%{release}
Requires:       spacewalk-check = %{version}-%{release}

%if "%{_vendor}" == "debbuild"
Requires(preun):python-minimal
Requires(post): python-minimal
%endif

%description -n python2-spacewalk-check
Python 2 specific files for rhn-check.
%endif

%if 0%{?build_py3}
%package -n python3-spacewalk-check
Summary:        Support programs and libraries for Spacewalk
Group:          System Environment/Base
Provides:       python3-rhn-check = %{version}-%{release}
Obsoletes:      python3-rhn-check < %{version}-%{release}
Requires:       spacewalk-check = %{version}-%{release}

%if "%{_vendor}" == "debbuild"
Requires(preun):python3-minimal
Requires(post): python3-minimal
%endif

%description -n python3-spacewalk-check
Python 3 specific files for spacewalk-check.
%endif

%package -n spacewalk-client-setup
Summary:        Configure and register an Spacewalk client
Group:          System Environment/Base
Provides:       rhn-setup = %{version}-%{release}
Obsoletes:      rhn-setup < %{version}-%{release}
Requires:       %{pythonX}-spacewalk-client-setup
%if 0%{?fedora} || 0%{?rhel} || 0%{?debian} || 0%{?ubuntu}
Requires:       usermode >= 1.36
%endif
%if 0%{?mageia}
Requires:       usermode-consoleonly >= 1.36
%endif
Requires:       %{name} = %{version}-%{release}

%description -n spacewalk-client-setup
spacewalk-client-setup contains programs and utilities to configure a system to use
SUSE Manager or Spacewalk.

%if 0%{?build_py2}
%package -n python2-spacewalk-client-setup
Summary:        Configure and register an Spacewalk client
Group:          System Environment/Base
Provides:       python-spacewalk-client-setup = %{version}-%{release}
Obsoletes:      python-spacewalk-client-setup < %{version}-%{release}
Provides:       python2-rhn-setup = %{version}-%{release}
Obsoletes:      python2-rhn-setup < %{version}-%{release}
Requires:       spacewalk-client-setup = %{version}-%{release}
%if 0%{?rhel} == 5
Requires:       newt
%endif
%if 0%{?fedora} || 0%{?rhel} > 5
Requires:       newt-python
%endif
%if 0%{?suse_version} || 0%{?mageia} || 0%{?debian} || 0%{?ubuntu}
Requires:       python-newt
%endif

%if "%{_vendor}" == "debbuild"
Requires(preun):python-minimal
Requires(post): python-minimal
%endif

%description -n python2-spacewalk-client-setup
Python 2 specific files for spacewalk-client-setup.
%endif

%if 0%{?build_py3}
%package -n python3-spacewalk-client-setup
Summary:        Configure and register an Spacewalk client
Group:          System Environment/Base
Provides:       python3-rhn-setup = %{version}-%{release}
Obsoletes:      python3-rhn-setup < %{version}-%{release}
Requires:       spacewalk-client-setup = %{version}-%{release}
%if 0%{?suse_version} || 0%{?mageia} || 0%{?debian} || 0%{?ubuntu}
Requires:       python3-newt
%else
Requires:       newt-python3
%endif

%if "%{_vendor}" == "debbuild"
Requires(preun):python3-minimal
Requires(post): python3-minimal
%endif

%description -n python3-spacewalk-client-setup
Python 3 specific files for spacewalk-client-setup.
%endif

%prep
%setup -q

%build
make -f Makefile.rhn-client-tools %{?is_deb:PLATFORM=deb}

%install
%if 0%{?build_py2}
make -f Makefile.rhn-client-tools install VERSION=%{version}-%{release} \
        PYTHONPATH=%{python_sitelib} PYTHONVERSION=%{python_version} \
        PREFIX=$RPM_BUILD_ROOT MANPATH=%{_mandir} %{?is_deb:PLATFORM=deb}
%endif
%if 0%{?build_py3}
sed -i 's|#!/usr/bin/python|#!/usr/bin/python3|' src/actions/*.py src/bin/*.py test/*.py
make -f Makefile.rhn-client-tools %{?is_deb:PLATFORM=deb}
make -f Makefile.rhn-client-tools install VERSION=%{version}-%{release} \
        PYTHONPATH=%{python3_sitelib} PYTHONVERSION=%{python3_version} \
        PREFIX=$RPM_BUILD_ROOT MANPATH=%{_mandir} %{?is_deb:PLATFORM=deb}
%endif

mkdir -p $RPM_BUILD_ROOT/var/lib/up2date
mkdir -pm700 $RPM_BUILD_ROOT%{_localstatedir}/spool/up2date
touch $RPM_BUILD_ROOT%{_localstatedir}/spool/up2date/loginAuth.pkl
%if 0%{?fedora} || 0%{?mageia} || 0%{?debian} >= 8 || 0%{?ubuntu} >= 1504 || 0%{?sle_version} >= 120000 || 0%{?rhel} >= 7
mkdir -p $RPM_BUILD_ROOT/%{_presetdir}
%endif

%if 0%{?suse_version}
# zypp-plugin-spacewalk has its own action/errata.py
rm -f $RPM_BUILD_ROOT%{_datadir}/rhn/actions/errata.py*
%endif

%if 0%{?build_py2}
%if 0%{?fedora} || 0%{?rhel} > 5 || 0%{?suse_version} >= 1140 || 0%{?mageia} || 0%{?debian} || 0%{?ubuntu}
rm $RPM_BUILD_ROOT%{python_sitelib}/up2date_client/hardware_hal.*
%else
rm $RPM_BUILD_ROOT%{python_sitelib}/up2date_client/hardware_gudev.*
rm $RPM_BUILD_ROOT%{python_sitelib}/up2date_client/hardware_udev.*
%endif
%endif

%if 0%{?rhel} == 5
%if 0%{?build_py2}
rm -rf $RPM_BUILD_ROOT%{python_sitelib}/up2date_client/firstboot
%endif
%endif
%if 0%{?rhel} == 6
rm -rf $RPM_BUILD_ROOT%{_datadir}/firstboot/modules/rhn_*_*.*
%endif
%if ! 0%{?rhel} || 0%{?rhel} > 6
%if 0%{?build_py2}
rm -rf $RPM_BUILD_ROOT%{python_sitelib}/up2date_client/firstboot
%endif
rm -rf $RPM_BUILD_ROOT%{_datadir}/firstboot/
%endif
%if 0%{?build_py3}
rm -rf $RPM_BUILD_ROOT%{python3_sitelib}/up2date_client/firstboot
%endif

# create mgr_check symlink
ln -sf rhn_check $RPM_BUILD_ROOT/%{_sbindir}/mgr_check

# remove all unsupported translations
cd $RPM_BUILD_ROOT
for d in usr/share/locale/*; do
  if [ ! -d "/$d" ]; then
    rm -rfv "./$d"
  fi
done
cd -

%if "%{_vendor}" != "debbuild"
%find_lang rhn-client-tools
%endif

# create links to default script version
%define default_suffix %{?default_py3:-%{python3_version}}%{!?default_py3:-%{python_version}}
for i in \
    /usr/sbin/rhn_check \
    /usr/sbin/rhnreg_ks \
; do
    ln -s $(basename "$i")%{default_suffix} "$RPM_BUILD_ROOT$i"
done

rm -rf $RPM_BUILD_ROOT/etc/pam.d
rm -rf $RPM_BUILD_ROOT/etc/security/console.apps
rm -rf $RPM_BUILD_ROOT/%{python_sitelib}/up2date_client/firstboot

%if 0%{?suse_version}
%if 0%{?build_py2}
%py_compile -O %{buildroot}/%{python_sitelib}
%endif
%if 0%{?build_py3}
%py3_compile -O %{buildroot}/%{python3_sitelib}
%endif
%endif

%post
rm -f %{_localstatedir}/spool/up2date/loginAuth.pkl

%if %{with test} && 0%{?fedora}
%check

make -f Makefile.rhn-client-tools test
%endif

%if "%{_vendor}" == "debbuild"
%files
# No find_lang on Debian systems
%{_datadir}/locale/
/var/lib/up2date/
%else

%files -f rhn-client-tools.lang
%endif
%defattr(-,root,root,-)
# some info about mirrors
%doc doc/mirrors.txt
%doc doc/AUTHORS
%{!?_licensedir:%global license %doc}
%license doc/LICENSE
%{_mandir}/man5/up2date.5*

%dir %{_sysconfdir}/sysconfig/rhn
%dir %{_sysconfdir}/sysconfig/rhn/clientCaps.d
%dir %{_sysconfdir}/sysconfig/rhn/allowed-actions
%dir %{_sysconfdir}/sysconfig/rhn/allowed-actions/configfiles
%dir %{_sysconfdir}/sysconfig/rhn/allowed-actions/script
%config(noreplace) %{_sysconfdir}/sysconfig/rhn/up2date
%config(noreplace) %{_sysconfdir}/logrotate.d/up2date

# dirs
%dir %{_localstatedir}/spool/up2date

%ghost %attr(600,root,root) %{_localstatedir}/spool/up2date/loginAuth.pkl

%if 0%{?build_py2}
%files -n python2-%{name}
%defattr(-,root,root,-)
%dir %{python_sitelib}/up2date_client/
%{python_sitelib}/up2date_client/__init__.*
%{python_sitelib}/up2date_client/config.*
%{python_sitelib}/up2date_client/haltree.*
%{python_sitelib}/up2date_client/hardware*
%{python_sitelib}/up2date_client/up2dateUtils.*
%{python_sitelib}/up2date_client/up2dateLog.*
%{python_sitelib}/up2date_client/up2dateErrors.*
%{python_sitelib}/up2date_client/up2dateAuth.*
%{python_sitelib}/up2date_client/rpcServer.*
%{python_sitelib}/up2date_client/rhnserver.*
%{python_sitelib}/up2date_client/pkgUtils.*
%{python_sitelib}/up2date_client/rpmUtils.*
%{python_sitelib}/up2date_client/debUtils.*
%{python_sitelib}/up2date_client/rhnPackageInfo.*
%{python_sitelib}/up2date_client/rhnChannel.*
%{python_sitelib}/up2date_client/rhnHardware.*
%{python_sitelib}/up2date_client/transaction.*
%{python_sitelib}/up2date_client/clientCaps.*
%{python_sitelib}/up2date_client/capabilities.*
%{python_sitelib}/up2date_client/rhncli.*
%{python_sitelib}/up2date_client/pkgplatform.*
%endif

%if 0%{?build_py3}
%files -n python3-%{name}
%defattr(-,root,root,-)
%dir %{python3_sitelib}/up2date_client/
%{python3_sitelib}/up2date_client/__init__.*
%{python3_sitelib}/up2date_client/config.*
%{python3_sitelib}/up2date_client/haltree.*
%{python3_sitelib}/up2date_client/hardware*
%{python3_sitelib}/up2date_client/up2dateUtils.*
%{python3_sitelib}/up2date_client/up2dateLog.*
%{python3_sitelib}/up2date_client/up2dateErrors.*
%{python3_sitelib}/up2date_client/up2dateAuth.*
%{python3_sitelib}/up2date_client/rpcServer.*
%{python3_sitelib}/up2date_client/rhnserver.*
%{python3_sitelib}/up2date_client/pkgUtils.*
%{python3_sitelib}/up2date_client/rpmUtils.*
%{python3_sitelib}/up2date_client/debUtils.*
%{python3_sitelib}/up2date_client/rhnPackageInfo.*
%{python3_sitelib}/up2date_client/rhnChannel.*
%{python3_sitelib}/up2date_client/rhnHardware.*
%{python3_sitelib}/up2date_client/transaction.*
%{python3_sitelib}/up2date_client/clientCaps.*
%{python3_sitelib}/up2date_client/capabilities.*
%{python3_sitelib}/up2date_client/rhncli.*
%{python3_sitelib}/up2date_client/pkgplatform.*

%if "%{_vendor}" != "debbuild"
%dir %{python3_sitelib}/up2date_client/__pycache__/
%{python3_sitelib}/up2date_client/__pycache__/__init__.*
%{python3_sitelib}/up2date_client/__pycache__/config.*
%{python3_sitelib}/up2date_client/__pycache__/haltree.*
%{python3_sitelib}/up2date_client/__pycache__/hardware*
%{python3_sitelib}/up2date_client/__pycache__/up2dateUtils.*
%{python3_sitelib}/up2date_client/__pycache__/up2dateLog.*
%{python3_sitelib}/up2date_client/__pycache__/up2dateErrors.*
%{python3_sitelib}/up2date_client/__pycache__/up2dateAuth.*
%{python3_sitelib}/up2date_client/__pycache__/rpcServer.*
%{python3_sitelib}/up2date_client/__pycache__/rhnserver.*
%{python3_sitelib}/up2date_client/__pycache__/pkgUtils.*
%{python3_sitelib}/up2date_client/__pycache__/rpmUtils.*
%{python3_sitelib}/up2date_client/__pycache__/debUtils.*
%{python3_sitelib}/up2date_client/__pycache__/rhnPackageInfo.*
%{python3_sitelib}/up2date_client/__pycache__/rhnChannel.*
%{python3_sitelib}/up2date_client/__pycache__/rhnHardware.*
%{python3_sitelib}/up2date_client/__pycache__/transaction.*
%{python3_sitelib}/up2date_client/__pycache__/clientCaps.*
%{python3_sitelib}/up2date_client/__pycache__/capabilities.*
%{python3_sitelib}/up2date_client/__pycache__/rhncli.*
%{python3_sitelib}/up2date_client/__pycache__/pkgplatform.*
%endif
%endif

%files -n spacewalk-check
%defattr(-,root,root,-)
%{_mandir}/man8/rhn_check.8*
%{_sbindir}/rhn_check
%{_sbindir}/mgr_check

%if 0%{?build_py2}
%files -n python2-spacewalk-check
%defattr(-,root,root,-)
%{_sbindir}/rhn_check-%{python_version}
%dir %{python_sitelib}/rhn
%dir %{python_sitelib}/rhn/actions
%{python_sitelib}/up2date_client/getMethod.*
# actions for rhn_check to run
%{python_sitelib}/rhn/actions/__init__.*
%{python_sitelib}/rhn/actions/hardware.*
%{python_sitelib}/rhn/actions/systemid.*
%{python_sitelib}/rhn/actions/reboot.*
%{python_sitelib}/rhn/actions/up2date_config.*
%endif

%if 0%{?build_py3}
%files -n python3-spacewalk-check
%defattr(-,root,root,-)
%{_sbindir}/rhn_check-%{python3_version}
%dir %{python3_sitelib}/rhn
%dir %{python3_sitelib}/rhn/actions
%{python3_sitelib}/up2date_client/getMethod.*
%{python3_sitelib}/rhn/actions/__init__.*
%{python3_sitelib}/rhn/actions/hardware.*
%{python3_sitelib}/rhn/actions/systemid.*
%{python3_sitelib}/rhn/actions/reboot.*
%{python3_sitelib}/rhn/actions/up2date_config.*

%if "%{_vendor}" != "debbuild"
%dir %{python3_sitelib}/rhn/actions/__pycache__/
%{python3_sitelib}/up2date_client/__pycache__/getMethod.*
%{python3_sitelib}/rhn/actions/__pycache__/__init__.*
%{python3_sitelib}/rhn/actions/__pycache__/hardware.*
%{python3_sitelib}/rhn/actions/__pycache__/systemid.*
%{python3_sitelib}/rhn/actions/__pycache__/reboot.*
%{python3_sitelib}/rhn/actions/__pycache__/up2date_config.*
%endif
%endif

%files -n spacewalk-client-setup
%defattr(-,root,root,-)
%{_mandir}/man8/rhnreg_ks.8*

%{_sbindir}/rhnreg_ks

%if 0%{?build_py2}
%files -n python2-spacewalk-client-setup
%defattr(-,root,root,-)
%{_sbindir}/rhnreg_ks-%{python_version}
%{python2_sitelib}/up2date_client/rhnreg.*
%{python2_sitelib}/up2date_client/pmPlugin.*
%{python2_sitelib}/up2date_client/tui.*
%{python2_sitelib}/up2date_client/rhnreg_constants.*
%endif

%if 0%{?build_py3}
%files -n python3-spacewalk-client-setup
%defattr(-,root,root,-)
%{_sbindir}/rhnreg_ks-%{python3_version}
%{python3_sitelib}/up2date_client/rhnreg.*
%{python3_sitelib}/up2date_client/pmPlugin.*
%{python3_sitelib}/up2date_client/tui.*
%{python3_sitelib}/up2date_client/rhnreg_constants.*

%if "%{_vendor}" != "debbuild"
%{python3_sitelib}/up2date_client/__pycache__/rhnreg.*
%{python3_sitelib}/up2date_client/__pycache__/pmPlugin.*
%{python3_sitelib}/up2date_client/__pycache__/tui.*
%{python3_sitelib}/up2date_client/__pycache__/rhnreg_constants.*
%endif
%endif

%if "%{_vendor}" == "debbuild"

%if 0%{?build_py2}
%post -n python2-%{name}
# Do late-stage bytecompilation, per debian policy
pycompile -p python2-%{name} -V -3.0

%preun -n python2-%{name}
# Ensure all *.py[co] files are deleted, per debian policy
pyclean -p python2-%{name}

%post -n python2-rhn-check
# Do late-stage bytecompilation, per debian policy
pycompile -p python2-rhn-check -V -3.0

%preun -n python2-rhn-check
# Ensure all *.py[co] files are deleted, per debian policy
pyclean -p python2-rhn-check

%post -n python2-rhn-setup
# Do late-stage bytecompilation, per debian policy
pycompile -p python2-rhn-setup -V -3.0

%preun -n python2-rhn-setup
# Ensure all *.py[co] files are deleted, per debian policy
pyclean -p python2-rhn-setup

%endif

%if 0%{?build_py3}
%post -n python3-%{name}
# Do late-stage bytecompilation, per debian policy
py3compile -p python3-%{name} -V -4.0

%preun -n python3-%{name}
# Ensure all *.py[co] files are deleted, per debian policy
py3clean -p python3-%{name}

%post -n python3-rhn-check
# Do late-stage bytecompilation, per debian policy
py3compile -p python3-rhn-check -V -4.0

%preun -n python3-rhn-check
# Ensure all *.py[co] files are deleted, per debian policy
py3clean -p python3-rhn-check

%post -n python3-rhn-setup
# Do late-stage bytecompilation, per debian policy
py3compile -p python3-rhn-setup -V -4.0

%preun -n python3-rhn-setup
# Ensure all *.py[co] files are deleted, per debian policy
py3clean -p python3-rhn-setup

%endif
%endif

%changelog
