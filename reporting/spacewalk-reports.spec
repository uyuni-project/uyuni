#
# spec file for package spacewalk-reports
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


%if 0%{?suse_version} > 1320
# SLE15 builds on Python 3
%global build_py3   1
%endif
%define pythonX %{?build_py3:python3}%{!?build_py3:python2}

Name:           spacewalk-reports
Summary:        Script based reporting
License:        GPL-2.0-only
Group:          Applications/Internet
Version:        4.0.4
Release:        1%{?dist}
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
Requires:       %{pythonX}
Requires:       spacewalk-branding
Requires:       salt
BuildRequires:  /usr/bin/docbook2man

%description
Script based reporting to retrieve data from Spacewalk server in CSV format.

%prep
%setup -q

%build
/usr/bin/docbook2man *.sgml

# Fixing shebang for Python 3
%if 0%{?build_py3}
for i in $(find . -type f);
do
    sed -i '1s=^#!/usr/bin/\(python\|env python\)[0-9.]*=#!/usr/bin/python3=' $i;
done
%endif

%install
install -d $RPM_BUILD_ROOT/%{_bindir}
install -d $RPM_BUILD_ROOT/%{_prefix}/share/spacewalk
install -d $RPM_BUILD_ROOT/%{_prefix}/share/spacewalk/reports/data
install -d $RPM_BUILD_ROOT/%{_mandir}/man8
install spacewalk-report $RPM_BUILD_ROOT/%{_bindir}
install reports.py $RPM_BUILD_ROOT/%{_prefix}/share/spacewalk
install -m 644 reports/data/* $RPM_BUILD_ROOT/%{_prefix}/share/spacewalk/reports/data
install *.8 $RPM_BUILD_ROOT/%{_mandir}/man8
chmod -x $RPM_BUILD_ROOT/%{_mandir}/man8/spacewalk-report.8*

%files
%defattr(-,root,root)
%attr(755,root,root) %{_bindir}/spacewalk-report
%dir %{_datadir}/spacewalk
%{_datadir}/spacewalk/reports.py*
%{_datadir}/spacewalk/reports
%{_mandir}/man8/spacewalk-report.8*
%doc COPYING
%if 0%{?suse_version}
%dir %{_datadir}/spacewalk
%endif

%changelog
