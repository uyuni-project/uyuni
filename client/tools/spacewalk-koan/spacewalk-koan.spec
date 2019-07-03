#
# spec file for package spacewalk-koan
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
%define rhn_check	 spacewalk-check
#
%if 0%{?fedora} || 0%{?suse_version} > 1320 || 0%{?rhel} >= 8
%global build_py3   1
%global default_py3 1
%endif

%if ( 0%{?fedora} && 0%{?fedora} < 28 ) || ( 0%{?rhel} && 0%{?rhel} < 8 ) || 0%{?suse_version}
%global build_py2   1
%endif

%define pythonX %{?default_py3: python3}%{!?default_py3: python2}

Summary:        Support package for spacewalk koan interaction
License:        GPL-2.0-only
Group:          System Environment/Kernel
Name:           spacewalk-koan
Version:        4.0.5
Release:        1%{?dist}
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
Source1:        %{name}-rpmlintrc
Url:            https://github.com/uyuni-project/uyuni
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
%if 0%{?fedora} || 0%{?rhel} || 0%{?suse_version} >= 1210
BuildArch:      noarch
%endif
Requires:       %{pythonX}-%{name} = %{version}-%{release}
Requires:       koan
Requires:       xz
Conflicts:      rhn-kickstart
Conflicts:      rhn-kickstart-common
Conflicts:      rhn-kickstart-virtualization

Requires:       %{rhn_check}

%description
Support package for spacewalk koan interaction.

%if 0%{?build_py2}
%package -n python2-%{name}
Summary:        Support package for spacewalk koan interaction
Group:          System Environment/Kernel
BuildRequires:  python
Requires:       %{name} = %{version}-%{release}
Requires:       python

%description -n python2-%{name}
Python 2 specific files for %{name}.
%endif

%if 0%{?build_py3}
%package -n python3-%{name}
Summary:        Support package for spacewalk koan interaction
Group:          System Environment/Kernel
BuildRequires:  python3
BuildRequires:  python3-rpm-macros
Requires:       %{name} = %{version}-%{release}
Requires:       python3

%description -n python3-%{name}
Python 3 specific files for %{name}.
%endif

%prep
%setup -q

%build
make -f Makefile.spacewalk-koan all

%install
%if 0%{?build_py2}
make -f Makefile.spacewalk-koan install PREFIX=$RPM_BUILD_ROOT ROOT=%{python_sitelib} \
    MANDIR=%{_mandir}
%endif

%if 0%{?build_py3}
make -f Makefile.spacewalk-koan install PREFIX=$RPM_BUILD_ROOT ROOT=%{python3_sitelib} \
    MANDIR=%{_mandir}
%endif

%if 0%{?suse_version} && 0%{?build_py2}
%py_compile -O %{buildroot}/%{python_sitelib}
%endif
%if 0%{?suse_version} && 0%{?build_py3}
%py3_compile -O %{buildroot}/%{python3_sitelib}
%endif

%files
%defattr(-,root,root,-)
%doc COPYING
%dir %{_sysconfdir}/sysconfig/rhn
%dir %{_sysconfdir}/sysconfig/rhn/clientCaps.d
%config(noreplace)  %{_sysconfdir}/sysconfig/rhn/clientCaps.d/kickstart
%{_sbindir}/*

%if 0%{?build_py2}
%files -n python2-%{name}
%defattr(-,root,root,-)
%dir %{python_sitelib}/rhn
%{python_sitelib}/spacewalkkoan/
%{python_sitelib}/rhn/actions/
%if 0%{?suse_version}
%dir %{python_sitelib}/rhn
%endif
%endif

%if 0%{?build_py3}
%files -n python3-%{name}
%defattr(-,root,root,-)
%dir %{python3_sitelib}/rhn
%{python3_sitelib}/spacewalkkoan/
%{python3_sitelib}/rhn/actions/
%if 0%{?suse_version}
%dir %{python3_sitelib}/rhn
%endif
%endif

%changelog
