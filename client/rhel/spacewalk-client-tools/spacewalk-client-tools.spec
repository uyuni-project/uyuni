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


%{!?__python2:%global __python2 %{_bindir}/python2}
%{!?__python3:%global __python3 %{_bindir}/python3}
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
#
%bcond_with    test

Name:           spacewalk-client-tools
Version:        5.1.0
Release:        0
Summary:        Support programs and libraries for Spacewalk
License:        GPL-2.0-only
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
Source1:        https://raw.githubusercontent.com/uyuni-project/uyuni/%{name}-%{version}-0/client/rhel/%{name}/%{name}-rpmlintrc
%if "%{_vendor}" == "debbuild"
Packager:       Uyuni Project <devel@lists.uyuni-project.org>
Group:          admin
%else
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          System Environment/Base
%endif
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
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
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

%if 0%{?suse_version}
Requires:       dbus-1-python
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
BuildRequires:  python-coverage
BuildRequires:  python-dev
BuildRequires:  python-rpm
Requires:       gir1.2-gudev-1.0
Requires:       python-dbus
Requires:       python-dmidecode
Requires:       python-ethtool >= 0.4
Requires:       python-gi
Requires:       python-pyudev
Requires:       python-rpm
Requires:       python2-hwdata
Requires(post): python-minimal
Requires(preun): python-minimal
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
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
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
Requires:       python3-pyudev
%else
Requires:       libgudev
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
Requires:       python3-pyudev
Requires(post): python3-minimal
Requires(preun): python3-minimal
%endif

%if %{with test} && 0%{?rhel} != 6
# The following BuildRequires are for check only
BuildRequires:  python3-coverage
BuildRequires:  python3-rpm
%endif

%description -n python3-%{name}
Python 3 specific files of %{name}.
%endif

%prep
%setup -q

%build
make -f Makefile.rhn-client-tools %{?is_deb:PLATFORM=deb}

%install
%if 0%{?build_py2}
make -f Makefile.rhn-client-tools install VERSION=%{version}-%{release} \
        PYTHONPATH=%{python_sitelib} PYTHONVERSION=%{python_version} \
        PREFIX=%{buildroot} %{?is_deb:PLATFORM=deb}
%endif
%if 0%{?build_py3}
sed -i 's|#!%{_bindir}/python|#!%{_bindir}/python3|' test/*.py
make -f Makefile.rhn-client-tools %{?is_deb:PLATFORM=deb}
make -f Makefile.rhn-client-tools install VERSION=%{version}-%{release} \
        PYTHONPATH=%{python3_sitelib} PYTHONVERSION=%{python3_version} \
        PREFIX=%{buildroot} %{?is_deb:PLATFORM=deb}
%endif

mkdir -p %{buildroot}%{_localstatedir}/lib/up2date
mkdir -pm700 %{buildroot}%{_localstatedir}/spool/up2date
touch %{buildroot}%{_localstatedir}/spool/up2date/loginAuth.pkl
%if 0%{?fedora} || 0%{?mageia} || 0%{?debian} >= 8 || 0%{?ubuntu} >= 1504 || 0%{?sle_version} >= 120000 || 0%{?rhel} >= 7
mkdir -p %{buildroot}/%{_presetdir}
%endif

# remove all unsupported translations
cd %{buildroot}
for d in usr/share/locale/*; do
  if [ ! -d "/$d" ]; then
    rm -rfv "./$d"
  fi
done
cd -

%if "%{_vendor}" != "debbuild"
%find_lang rhn-client-tools
%endif

rm -rf %{buildroot}%{_sysconfdir}/pam.d
rm -rf %{buildroot}%{_sysconfdir}/security/console.apps

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
%{_localstatedir}/lib/up2date/
%else
%files -f rhn-client-tools.lang
%endif
%defattr(-,root,root,-)
%doc doc/AUTHORS
%{!?_licensedir:%global license %doc}
%license doc/LICENSE

%dir %{_sysconfdir}/sysconfig/rhn
%dir %{_sysconfdir}/sysconfig/rhn/clientCaps.d
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
%{python_sitelib}/up2date_client/up2dateUtils.*
%{python_sitelib}/up2date_client/up2dateLog.*
%{python_sitelib}/up2date_client/up2dateErrors.*
%{python_sitelib}/up2date_client/rpcServer.*
%{python_sitelib}/up2date_client/transaction.*
%{python_sitelib}/up2date_client/pkgplatform.*
%endif

%if 0%{?build_py3}
%files -n python3-%{name}
%defattr(-,root,root,-)
%dir %{python3_sitelib}/up2date_client/
%{python3_sitelib}/up2date_client/__init__.*
%{python3_sitelib}/up2date_client/config.*
%{python3_sitelib}/up2date_client/up2dateUtils.*
%{python3_sitelib}/up2date_client/up2dateLog.*
%{python3_sitelib}/up2date_client/up2dateErrors.*
%{python3_sitelib}/up2date_client/rpcServer.*
%{python3_sitelib}/up2date_client/transaction.*
%{python3_sitelib}/up2date_client/pkgplatform.*

%if "%{_vendor}" != "debbuild"
%dir %{python3_sitelib}/up2date_client/__pycache__/
%{python3_sitelib}/up2date_client/__pycache__/__init__.*
%{python3_sitelib}/up2date_client/__pycache__/config.*
%{python3_sitelib}/up2date_client/__pycache__/up2dateUtils.*
%{python3_sitelib}/up2date_client/__pycache__/up2dateLog.*
%{python3_sitelib}/up2date_client/__pycache__/up2dateErrors.*
%{python3_sitelib}/up2date_client/__pycache__/rpcServer.*
%{python3_sitelib}/up2date_client/__pycache__/transaction.*
%{python3_sitelib}/up2date_client/__pycache__/pkgplatform.*
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
%endif

%if 0%{?build_py3}
%post -n python3-%{name}
# Do late-stage bytecompilation, per debian policy
py3compile -p python3-%{name} -V -4.0

%preun -n python3-%{name}
# Ensure all *.py[co] files are deleted, per debian policy
py3clean -p python3-%{name}
%endif
%endif

%changelog
