#
# spec file for package spacecmd
#
# Copyright (c) 2018 SUSE LINUX GmbH, Nuernberg, Germany.
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

# Please submit bugfixes or comments via http://bugs.opensuse.org/
#


%if ! (0%{?fedora} || 0%{?rhel} > 5)
%{!?python_sitelib: %global python_sitelib %(%{__python} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")}
%{!?python_sitearch: %global python_sitearch %(%{__python} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib(1))")}
%endif

%if 0%{?fedora} || 0%{?rhel} >= 8
%{!?pylint_check: %global pylint_check 1}
%endif

%if 0%{?fedora} || 0%{?suse_version} > 1320
%global build_py3   1
%global python_sitelib %{python3_sitelib}
%endif

Name:           spacecmd
Version:        4.0.4
Release:        1%{?dist}
Summary:        Command-line interface to Spacewalk and Red Hat Satellite servers
License:        GPL-3.0-or-later
Group:          Applications/System

URL:            https://github.com/uyuni-project/uyuni
Source:         https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
%if 0%{?fedora} || 0%{?rhel} || 0%{?suse_version} >= 1210
BuildArch:      noarch
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
BuildRequires:  python3-devel
BuildRequires:  python3-rpm
BuildRequires:  python3-simplejson
Requires:       python3
%else
BuildRequires:  python
BuildRequires:  python-devel
BuildRequires:  python-simplejson
BuildRequires:  rpm-python
Requires:       python
%if 0%{?suse_version}
BuildRequires:  python-xml
Requires:       python-xml
%endif
%endif
%if 0%{?rhel} == 5
BuildRequires:  python-json
%endif

%if 0%{?rhel} == 5
Requires:       python-simplejson
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
%{__install} -p -m0644 src/lib/*.py %{buildroot}/%{python_sitelib}/spacecmd/

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

%files
%defattr(-,root,root)
%{_bindir}/spacecmd
%{python_sitelib}/spacecmd/
%ghost %config %{_sysconfdir}/spacecmd.conf
%dir %{_sysconfdir}/bash_completion.d
%{_sysconfdir}/bash_completion.d/spacecmd
%doc src/doc/README src/doc/COPYING
%doc %{_mandir}/man1/spacecmd.1.gz

%changelog
