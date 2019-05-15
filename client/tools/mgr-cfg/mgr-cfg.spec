#
# spec file for package mgr-cfg
#
# Copyright (c) 2019 SUSE LINUX GmbH, Nuernberg, Germany.
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


# package renaming fun :(
%define rhn_client_tools spacewalk-client-tools
%define rhn_setup	 spacewalk-client-setup
%define rhn_check	 spacewalk-check
%define rhnsd		 spacewalksd
# Old name and version+1 before renaming to mgr-cfg
%define oldname          rhncfg
%define oldversion       5.10.123
#
%global rhnroot %{_datadir}/rhn
%global rhnconf %{_sysconfdir}/sysconfig/rhn
%global client_caps_dir %{rhnconf}/clientCaps.d

%if 0%{?fedora} || 0%{?suse_version} > 1320 || 0%{?rhel} >= 8
%global build_py3   1
%global default_py3 1
%endif

%if ( 0%{?fedora} && 0%{?fedora} < 28 ) || ( 0%{?rhel} && 0%{?rhel} < 8 ) || 0%{?suse_version} || 0%{?ubuntu} || 0%{?debian}
%global build_py2   1
%endif

# ------------------------------- Python macros for debian ----------------------------------------
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
# --------------------------- End Python macros for debian ----------------------------------------

%define pythonX %{?default_py3: python3}%{!?default_py3: python2}

%if %{_vendor} == "debbuild"
# Bash constructs in scriptlets don't play nice with Debian's default shell, dash
%global _buildshell /bin/bash
%endif

Name:           mgr-cfg
Version:        4.0.7
Provides:       %{oldname} = %{oldversion}
Obsoletes:      %{oldname} < %{oldversion}
Release:        1%{?dist}
Summary:        Spacewalk Configuration Client Libraries
License:        GPL-2.0-only
%if %{_vendor} == "debbuild"
Group:      admin
Packager:   Uyuni Project <uyuni-devel@opensuse.org>
%else
Group:          Applications/System
%endif
Url:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
Source1:        %{name}-rpmlintrc
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
%if 0%{?fedora} || 0%{?rhel} || 0%{?suse_version} >= 1210
BuildArch:      noarch
%endif
BuildRequires:  docbook-utils
Requires:       %{pythonX}-%{name} = %{version}-%{release}

%if %{_vendor} != "debbuild"
%if 0%{?suse_version}
# provide rhn directories and no selinux on suse
BuildRequires:  spacewalk-client-tools
%if %{suse_version} >= 1110
# Only on SLES11
Requires:       python-selinux
%endif
%else
Requires:       libselinux-python
%endif
%endif

%if %{_vendor} == "debbuild"
%if 0%{?build_py2}
Requires: python-selinux
%endif
%if 0%{?build_py3}
Requires: python3-selinux
%endif
%endif


%description
The base libraries and functions needed by all mgr-cfg-* packages.

%if 0%{?build_py2}
%package -n python2-%{name}
Summary:        Spacewalk Configuration Client Libraries
Group:          Applications/System
Provides:       python-%{name} = %{oldversion}
Obsoletes:      python-%{name} < %{oldversion}
Provides:       python-%{oldname} = %{oldversion}
Obsoletes:      python-%{oldname} < %{oldversion}
Requires:       %{name} = %{version}-%{release}
Requires:       python
Requires:       python2-rhn-client-tools >= 2.8.4
Requires:       rhnlib >= 2.8.3
Requires:       spacewalk-usix
%if 0%{?rhel} && 0%{?rhel} <= 5
Requires:       python-hashlib
%endif
BuildRequires:  python

%if %{_vendor} == "debbuild"
# For scriptlets
Requires(preun): python-minimal
Requires(post): python-minimal
%endif

%description -n python2-%{name}
Python 2 specific files for %{name}.
%endif

%if 0%{?build_py3}
%package -n python3-%{name}
Summary:        Spacewalk Configuration Client Libraries
Group:          Applications/System
Requires:       %{name} = %{version}-%{release}
Provides:       python3-%{oldname} = %{oldversion}
Obsoletes:      python3-%{oldname} < %{oldversion}
Requires:       python3
Requires:       python3-rhn-client-tools >= 2.8.4
Requires:       python3-rhnlib >= 2.8.3
Requires:       python3-spacewalk-usix
BuildRequires:  python3
%if %{_vendor} != "debbuild"
BuildRequires:  python3-rpm-macros
%endif
%if %{_vendor} == "debbuild"
# For scriptlets
Requires(preun): python3-minimal
Requires(post): python3-minimal
%endif

%description -n python3-%{name}
Python 3 specific files for %{name}.
%endif

%package client
Summary:        Spacewalk Configuration Client
Group:          Applications/System
Provides:       %{oldname}-client = %{oldversion}
Obsoletes:      %{oldname}-client < %{oldversion}
Requires:       %{name} = %{version}-%{release}
Requires:       %{pythonX}-%{name}-client = %{version}-%{release}

%description client
A command line interface to the client features of the Spacewalk Configuration
Management system.

%if 0%{?build_py2}
%package -n python2-%{name}-client
Summary:        Spacewalk Configuration Client
Group:          Applications/System
Provides:       python-%{name}-client = %{oldversion}
Obsoletes:      python-%{name}-client < %{oldversion}
Provides:       python-%{oldname}-client = %{oldversion}
Obsoletes:      python-%{oldname}-client < %{oldversion}
Requires:       %{name}-client = %{version}-%{release}
%if %{_vendor} == "debbuild"
# For scriptlets
Requires(preun): python-minimal
Requires(post): python-minimal
%endif

%description -n python2-%{name}-client
Python 2 specific files for %{name}-client.
%endif

%if 0%{?build_py3}
%package -n python3-%{name}-client
Summary:        Spacewalk Configuration Client
Group:          Applications/System
Provides:       python3-%{oldname}-client = %{oldversion}
Obsoletes:      python3-%{oldname}-client < %{oldversion}
Requires:       %{name}-client = %{version}-%{release}
%if %{_vendor} == "debbuild"
# For scriptlets
Requires(preun): python3-minimal
Requires(post): python3-minimal
%endif

%description -n python3-%{name}-client
Python 3 specific files for %{name}-client.
%endif

%package management
Summary:        Spacewalk Configuration Management Client
Group:          Applications/System
Provides:       %{oldname}-management = %{oldversion}
Obsoletes:      %{oldname}-management < %{oldversion}
Requires:       %{name} = %{version}-%{release}
Requires:       %{pythonX}-%{name}-management = %{version}-%{release}

%description management
A command line interface used to manage Spacewalk configuration.

%if 0%{?build_py2}
%package -n python2-%{name}-management
Summary:        Spacewalk Configuration Management Client
Group:          Applications/System
Provides:       python-%{name}-management = %{oldversion}
Obsoletes:      python-%{name}-management < %{oldversion}
Provides:       python-%{oldname}-management = %{oldversion}
Obsoletes:      python-%{oldname}-management < %{oldversion}
Requires:       %{name}-management = %{version}-%{release}
%if %{_vendor} == "debbuild"
# For scriptlets
Requires(preun): python-minimal
Requires(post): python-minimal
%endif

%description -n python2-%{name}-management
Python 2 specific files for python2-%{name}-management.
%endif

%if 0%{?build_py3}
%package -n python3-%{name}-management
Summary:        Spacewalk Configuration Management Client
Group:          Applications/System
Provides:       python3-%{oldname}-management = %{oldversion}
Obsoletes:      python3-%{oldname}-management < %{oldversion}
Requires:       %{name}-management = %{version}-%{release}
%if %{_vendor} == "debbuild"
# For scriptlets
Requires(preun): python3-minimal
Requires(post): python3-minimal
%endif

%description -n python3-%{name}-management
Python 2 specific files for python3-%{name}-management.
%endif

%package actions
Summary:        Spacewalk Configuration Client Actions
Group:          Applications/System
Provides:       %{oldname}-actions = %{oldversion}
Obsoletes:      %{oldname}-actions < %{oldversion}
Requires:       %{name} = %{version}-%{release}
Requires:       %{pythonX}-%{name}-actions = %{version}-%{release}

%description actions
The code required to run configuration actions scheduled via Spacewalk.

%if 0%{?build_py2}
%package -n python2-%{name}-actions
Summary:        Spacewalk Configuration Client Actions
Group:          Applications/System
Provides:       python-%{name}-actions = %{oldversion}
Obsoletes:      python-%{name}-actions < %{oldversion}
Provides:       python-%{oldname}-actions = %{oldversion}
Obsoletes:      python-%{oldname}-actions < %{oldversion}
Requires:       %{name}-actions = %{version}-%{release}
Requires:       python2-%{name}-client
%if %{_vendor} == "debbuild"
# For scriptlets
Requires(preun): python-minimal
Requires(post): python-minimal
%endif

%description -n python2-%{name}-actions
Python 2 specific files for python2-%{name}-actions.
%endif

%if 0%{?build_py3}
%package -n python3-%{name}-actions
Summary:        Spacewalk Configuration Client Actions
Group:          Applications/System
Provides:       python3-%{oldname}-actions = %{oldversion}
Obsoletes:      python3-%{oldname}-actions < %{oldversion}
Requires:       %{name}-actions = %{version}-%{release}
Requires:       python3-%{name}-client
%if %{_vendor} == "debbuild"
# For scriptlets
Requires(preun): python3-minimal
Requires(post): python3-minimal
%endif

%description -n python3-%{name}-actions
Python 3 specific files for python2-%{name}-actions.
%endif

%prep
%setup -q

%build
make -f Makefile.rhncfg all

%install
install -d $RPM_BUILD_ROOT/%{python_sitelib}
%if 0%{?build_py2}
make -f Makefile.rhncfg install PREFIX=$RPM_BUILD_ROOT ROOT=%{python_sitelib} \
    MANDIR=%{_mandir} PYTHONVERSION=%{python_version}
%endif
%if 0%{?build_py3}
    install -d $RPM_BUILD_ROOT/%{python3_sitelib}
    sed -i 's|#!/usr/bin/python|#!/usr/bin/python3|' config_*/*.py actions/*.py
    make -f Makefile.rhncfg install PREFIX=$RPM_BUILD_ROOT ROOT=%{python3_sitelib} \
        MANDIR=%{_mandir} PYTHONVERSION=%{python3_version}
%endif
mkdir -p $RPM_BUILD_ROOT/%{_localstatedir}/spool/rhn
mkdir -p $RPM_BUILD_ROOT/%{_localstatedir}/log
touch $RPM_BUILD_ROOT/%{_localstatedir}/log/rhncfg-actions

# create links to default script version
%define default_suffix %{?default_py3:-%{python3_version}}%{!?default_py3:-%{python_version}}
for i in \
    /usr/bin/rhncfg-client \
    /usr/bin/rhncfg-manager \
    /usr/bin/rhn-actions-control \
; do
    ln -s $(basename "$i")%{default_suffix} "$RPM_BUILD_ROOT$i"
done

ln -s rhncfg-manager $RPM_BUILD_ROOT/%{_bindir}/mgrcfg-manager
ln -s rhncfg-client $RPM_BUILD_ROOT/%{_bindir}/mgrcfg-client
ln -s rhn-actions-control $RPM_BUILD_ROOT/%{_bindir}/mgr-actions-control

%if 0%{?suse_version}
%if 0%{?build_py2}
%py_compile -O %{buildroot}/%{python_sitelib}
%endif
%if 0%{?build_py3}
%py3_compile -O %{buildroot}/%{python3_sitelib}
%endif
%endif

%post
if [ -f %{_localstatedir}/log/rhncfg-actions ]
then 
chown root %{_localstatedir}/log/rhncfg-actions
chmod 600 %{_localstatedir}/log/rhncfg-actions
fi

%if %{_vendor} == "debbuild"
# Debian requires:
# post: Do bytecompilation after install
# preun: Remove any *.py[co] files

%if 0%{?build_py2}
%post -n python2-%{name}
pycompile python2-%{name} -V -3.0

%preun -n python2-%{name}
pyclean -p python2-%{name}

%post -n python2-%{name}-client
pycompile python2-%{name}-client -V -3.0

%preun -n python2-%{name}-client
pyclean -p python2-%{name}-client

%post -n python2-%{name}-management
pycompile python2-%{name}-management -V -3.0

%preun -n python2-%{name}-management
pyclean -p python2-%{name}-management

%post -n python2-%{name}-actions
pycompile python2-%{name}-actions -V -3.0

%preun -n python2-%{name}-actions
pyclean -p python2-%{name}-actions
%endif

%if 0%{?build_py3}
%post -n python3-%{name}
py3compile python3-%{name} -V -4.0

%preun -n python3-%{name}
py3clean -p python3-%{name}

%post -n python3-%{name}-client
py3compile python3-%{name}-client -V -4.0

%preun -n python3-%{name}-client
py3clean -p python3-%{name}-client

%post -n python3-%{name}-management
py3compile python3-%{name}-management -V -4.0

%preun -n python3-%{name}-management
py3clean -p python3-%{name}-management

%post -n python3-%{name}-actions
py3compile python3-%{name}-actions -V -4.0

%preun -n python3-%{name}-actions
py3clean -p python3-%{name}-actions
%endif
%endif


%files
%defattr(-,root,root,-)
%dir %{_localstatedir}/spool/rhn
%doc LICENSE

%if 0%{?build_py2}
%files -n python2-%{name}
%defattr(-,root,root,-)
%{python_sitelib}/config_common
%endif

%if 0%{?build_py3}
%files -n python3-%{name}
%defattr(-,root,root,-)
%{python3_sitelib}/config_common
%endif

%files client
%defattr(-,root,root,-)
%{_bindir}/rhncfg-client
%{_bindir}/mgrcfg-client
%attr(644,root,root) %config(noreplace) %{rhnconf}/rhncfg-client.conf
%{_mandir}/man8/rhncfg-client.8*

%if 0%{?build_py2}
%files -n python2-%{name}-client
%defattr(-,root,root,-)
%{python_sitelib}/config_client
%{_bindir}/rhncfg-client-%{python_version}
%endif

%if 0%{?build_py3}
%files -n python3-%{name}-client
%defattr(-,root,root,-)
%{python3_sitelib}/config_client
%{_bindir}/rhncfg-client-%{python3_version}
%endif

%files management
%defattr(-,root,root,-)
%{_bindir}/mgrcfg-manager
%{_bindir}/rhncfg-manager
%attr(644,root,root) %config(noreplace) %{rhnconf}/rhncfg-manager.conf
%{_mandir}/man8/rhncfg-manager.8*

%if 0%{?build_py2}
%files -n python2-%{name}-management
%defattr(-,root,root,-)
%{python_sitelib}/config_management
%{_bindir}/rhncfg-manager-%{python_version}
%endif

%if 0%{?build_py3}
%files -n python3-%{name}-management
%defattr(-,root,root,-)
%{python3_sitelib}/config_management
%{_bindir}/rhncfg-manager-%{python3_version}
%endif

%files actions
%defattr(-,root,root,-)
%{_bindir}/mgr-actions-control
%{_bindir}/rhn-actions-control
%config(noreplace) %{client_caps_dir}/*
%{_mandir}/man8/rhn-actions-control.8*
%ghost %attr(600,root,root) %{_localstatedir}/log/rhncfg-actions

%if 0%{?build_py2}
%files -n python2-%{name}-actions
%defattr(-,root,root,-)
%dir %{python_sitelib}/rhn
%{python_sitelib}/rhn/actions
%{_bindir}/rhn-actions-control-%{python_version}
%if 0%{?suse_version}
%dir %{python_sitelib}/rhn
%endif
%endif

%if 0%{?build_py3}
%files -n python3-%{name}-actions
%defattr(-,root,root,-)
%dir %{python3_sitelib}/rhn
%{python3_sitelib}/rhn/actions
%{_bindir}/rhn-actions-control-%{python3_version}
%if 0%{?suse_version}
%dir %{python3_sitelib}/rhn
%endif
%endif

%changelog
