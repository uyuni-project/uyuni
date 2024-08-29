#
# spec file for package spacewalk-reports
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


%if 0%{?suse_version} > 1320 || 0%{?rhel} || 0%{?fedora}
# SLE15 builds on Python 3
%global build_py3   1
%endif
%define pythonX %{?build_py3:python3}%{!?build_py3:python2}

Name:           spacewalk-reports
Version:        5.1.0
Release:        0
Summary:        Script based reporting
License:        GPL-2.0-only
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Applications/Internet
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
Requires:       %{pythonX}
Requires:       spacewalk-branding
BuildRequires:  /usr/bin/docbook2man
BuildArch:      noarch

%description
Script based reporting to retrieve data from Spacewalk server in CSV format.

%prep
%setup -q

%build
%{_bindir}/docbook2man *.sgml

# Fixing shebang for Python 3
%if 0%{?build_py3}
for i in $(find . -type f);
do
    sed -i '1s=^#!/usr/bin/\(python\|env python\)[0-9.]*=#!/usr/bin/python3=' $i;
done
%endif

%install
install -d %{buildroot}%{_bindir}
install -d %{buildroot}%{_datadir}/spacewalk
install -d %{buildroot}%{_datadir}/spacewalk/reports/data
install -d %{buildroot}%{_datadir}/spacewalk/reports/legacy
install -d %{buildroot}%{_mandir}/man8
install spacewalk-report %{buildroot}%{_bindir}
install reports.py %{buildroot}%{_datadir}/spacewalk
install -m 644 reports/data/* %{buildroot}%{_datadir}/spacewalk/reports/data
install -m 644 reports/legacy/* %{buildroot}%{_datadir}/spacewalk/reports/legacy
install *.8 %{buildroot}%{_mandir}/man8
chmod -x %{buildroot}%{_mandir}/man8/spacewalk-report.8*

%files
%defattr(-,root,root)
%attr(755,root,root) %{_bindir}/spacewalk-report
%dir %{_datadir}/spacewalk
%{_datadir}/spacewalk/reports.py*
%{_datadir}/spacewalk/reports
%{_mandir}/man8/spacewalk-report.8*
%license COPYING
%if 0%{?suse_version}
%dir %{_datadir}/spacewalk
%endif

%changelog
