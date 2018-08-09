#
# spec file for package mgr-cfg
#
# Copyright (c) 2018 SUSE LINUX GmbH, Nuernberg, Germany.
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

# Please submit bugfixes or comments via http://bugs.opensuse.org/
#


# package renaming fun :(
%define rhn_client_tools spacewalk-client-tools
%define rhn_setup	 spacewalk-client-setup
%define rhn_check	 spacewalk-check
%define rhnsd		 spacewalksd
# Old name and version+1 before renaming to mgr-cfg
%define oldname          rhncfg
%define oldversion       5.10.122.3
#
%global rhnroot %{_datadir}/rhn
%global rhnconf %{_sysconfdir}/sysconfig/rhn
%global client_caps_dir %{rhnconf}/clientCaps.d

%if 0%{?fedora} || 0%{?suse_version} > 1320 || 0%{?rhel} >= 8
%global build_py3   1
%global default_py3 1
%endif

%if ( 0%{?fedora} && 0%{?fedora} < 28 ) || ( 0%{?rhel} && 0%{?rhel} < 8 ) || 0%{?suse_version}
%global build_py2   1
%endif

%define pythonX %{?default_py3: python3}%{!?default_py3: python2}

Name:           mgr-cfg
Version:        4.0.0
Provides:       %{oldname} = %{oldversion}
Obsoletes:      %{oldname} < %{oldversion}
Release:        1%{?dist}
Summary:        Spacewalk Configuration Client Libraries
License:        GPL-2.0-only
Group:          Applications/System
URL:            https://github.com/spacewalkproject/spacewalk
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
Source1:        %{name}-rpmlintrc
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
%if 0%{?fedora} || 0%{?rhel} || 0%{?suse_version} >= 1210
BuildArch:      noarch
%endif
BuildRequires:  docbook-utils
Requires:       %{pythonX}-%{name} = %{version}-%{release}

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
BuildRequires:  python3-rpm-macros

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

%if 0%{?suse_version}
ln -s rhncfg-manager $RPM_BUILD_ROOT/%{_bindir}/mgrcfg-manager
ln -s rhncfg-client $RPM_BUILD_ROOT/%{_bindir}/mgrcfg-client
ln -s rhn-actions-control $RPM_BUILD_ROOT/%{_bindir}/mgr-actions-control
%py_compile -O %{buildroot}/%{python_sitelib}
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
%if 0%{?suse_version}
%{_bindir}/mgrcfg-client
%endif
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
%if 0%{?suse_version}
%{_bindir}/mgrcfg-manager
%endif
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
%if 0%{?suse_version}
%{_bindir}/mgr-actions-control
%endif
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
