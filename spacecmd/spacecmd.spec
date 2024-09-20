#
# spec file for package spacecmd
#
# Copyright (c) 2021 SUSE LLC
# Copyright (c) 2008-2018 Red Hat, Inc.
# Copyright (c) 2011 Aron Parsons <aronparsons@gmail.com>
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


%if ! (0%{?fedora} || 0%{?rhel})
%if "%{_vendor}" == "debbuild"
%global __python /usr/bin/python3
%endif
%{!?python_sitelib: %global python_sitelib %(%{__python} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")}
%{!?python_sitearch: %global python_sitearch %(%{__python} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib(1))")}
%endif

%if 0%{?fedora} || 0%{?rhel} >= 8
%{!?pylint_check: %global pylint_check 0}
%endif

%if 0%{?fedora} || 0%{?suse_version} > 1320 || 0%{?rhel} >= 8 || "%{_vendor}" == "debbuild"
%global build_py3   1
%if "%{_vendor}" != "debbuild"
%global python_sitelib %{python3_sitelib}
%endif
%endif

%if 0%{?fedora} || 0%{?rhel} >= 8
%global python2prefix python2
%else
%global python2prefix python
%endif

Name:           spacecmd
Version:        4.3.29
Release:        0
Summary:        Command-line interface to Spacewalk and Red Hat Satellite servers
License:        GPL-3.0-or-later
%if "%{_vendor}" == "debbuild"
Packager:       Uyuni packagers <uyuni-devel@lists.opensuse.org>
Group:          admin
%else
Group:          Applications/System
%endif
URL:            https://github.com/uyuni-project/uyuni
Source:         https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
%if 0%{?fedora} || 0%{?rhel} || 0%{?suse_version} >= 1210 || "%{_vendor}" == "debbuild"
BuildArch:      noarch
%endif

%if "%{_vendor}" == "debbuild" || 0%{?rhel} 
BuildRequires:  gettext
%endif
%if "%{_vendor}" == "debbuild"
BuildRequires:  intltool
%endif

%if 0%{?pylint_check}
%if 0%{?build_py3}
BuildRequires:  spacewalk-python3-pylint
%else
BuildRequires:  spacewalk-python2-pylint
%endif
%endif
%if 0%{?build_py3}
BuildRequires:  python3
%if "%{_vendor}" == "debbuild"
BuildRequires:  python3-dev
%else
BuildRequires:  python3-devel
BuildRequires:  python3-rpm-macros
%endif
Requires:       python3
Requires:       python3-rpm
Requires:       python3-dateutil
%else
BuildRequires:  %{python2prefix}
%if "%{_vendor}" == "debbuild"
BuildRequires:  %{python2prefix}-dev
%else
BuildRequires:  %{python2prefix}-devel
%endif
Requires:       %{python2prefix}-simplejson
Requires:       %{python2prefix}-dateutil
%if "%{_vendor}" == "debbuild"
Requires:       python-rpm
%else
Requires:       rpm-python
%endif
Requires:       %{python2prefix}
%if 0%{?suse_version}
BuildRequires:  python-xml
Requires:       python-xml
%endif
%endif
Requires:       file

%description
spacecmd is a command-line interface to Spacewalk and Red Hat Satellite servers

%prep
%setup -q

%build
# nothing to build

%install
%{__mkdir_p} %{buildroot}/%{_bindir}

%if 0%{?build_py3}
    sed -i 's|#!/usr/bin/python|#!/usr/bin/python3|' ./src/bin/spacecmd
%endif
%{__install} -p -m0755 src/bin/spacecmd %{buildroot}/%{_bindir}/

%{__mkdir_p} %{buildroot}/%{_sysconfdir}
touch %{buildroot}/%{_sysconfdir}/spacecmd.conf

%{__mkdir_p} %{buildroot}/%{_sysconfdir}/bash_completion.d
%{__install} -p -m0644 src/misc/spacecmd-bash-completion %{buildroot}/%{_sysconfdir}/bash_completion.d/spacecmd

%{__mkdir_p} %{buildroot}/%{python_sitelib}/spacecmd
%{__install} -p -m0644 src/spacecmd/*.py %{buildroot}/%{python_sitelib}/spacecmd/

%{__mkdir_p} %{buildroot}/%{_mandir}/man1
%{__gzip} -c src/doc/spacecmd.1 > %{buildroot}/%{_mandir}/man1/spacecmd.1.gz

touch %{buildroot}/%{python_sitelib}/spacecmd/__init__.py
%{__chmod} 0644 %{buildroot}/%{python_sitelib}/spacecmd/__init__.py

%if 0%{?suse_version}
%if 0%{?build_py3}
%py3_compile -O %{buildroot}/%{python_sitelib}
%else
%py_compile -O %{buildroot}/%{python_sitelib}
%endif
%endif

make -C po install PREFIX=$RPM_BUILD_ROOT
%find_lang spacecmd

%check
%if 0%{?pylint_check}
%if 0%{?build_py3}
PYTHONPATH=$RPM_BUILD_ROOT%{python_sitelib} \
	  spacewalk-python3-pylint $RPM_BUILD_ROOT%{python_sitelib}/spacecmd
%else
PYTHONPATH=$RPM_BUILD_ROOT%{python_sitelib} \
	  spacewalk-python2-pylint $RPM_BUILD_ROOT%{python_sitelib}/spacecmd
%endif
%endif

%files -f spacecmd.lang
%defattr(-,root,root)
%{_bindir}/spacecmd
%{python_sitelib}/spacecmd/
%ghost %config %{_sysconfdir}/spacecmd.conf
%dir %{_sysconfdir}/bash_completion.d
%{_sysconfdir}/bash_completion.d/spacecmd
%doc src/doc/README src/doc/COPYING
%doc %{_mandir}/man1/spacecmd.1.gz

%changelog
