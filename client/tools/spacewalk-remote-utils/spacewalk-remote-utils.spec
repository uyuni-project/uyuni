#
# spec file for package spacewalk-remote-utils
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


%if 0%{?fedora} || 0%{?suse_version} > 1320 || 0%{?rhel} >= 8
%global build_py3   1
%endif

Name:           spacewalk-remote-utils
Version:        4.0.4
Release:        1%{?dist}
Summary:        Utilities to interact with a Spacewalk server remotely.
License:        GPL-2.0-only
Group:          Applications/System

Url:            https://github.com/uyuni-project/uyuni
Source:         https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildArch:      noarch
BuildRoot:      %{_tmppath}/%{name}-%{version}-build

%if 0%{?build_py3}
BuildRequires:  python3-devel
Requires:       python3-rhnlib
%else
BuildRequires:  python-devel
Requires:       rhnlib >= 2.8.4
%endif
BuildRequires:  docbook-utils

%description
Utilities to interact with a Spacewalk server remotely over XMLRPC.

%prep
%setup -q

%build
docbook2man ./spacewalk-create-channel/doc/spacewalk-create-channel.sgml -o ./spacewalk-create-channel/doc/
%if 0%{?build_py3}
    sed -i 's|#!/usr/bin/python|#!/usr/bin/python3|' ./spacewalk-create-channel/spacewalk-create-channel
    sed -i 's|#!/usr/bin/python|#!/usr/bin/python3|' ./spacewalk-add-providers/spacewalk-add-providers
%endif

%install
%{__mkdir_p} %{buildroot}/%{_bindir}
%{__install} -p -m0755 spacewalk-add-providers/spacewalk-add-providers %{buildroot}/%{_bindir}/
%{__install} -p -m0755 spacewalk-create-channel/spacewalk-create-channel %{buildroot}/%{_bindir}/

%{__mkdir_p} %{buildroot}/%{_datadir}/rhn/channel-data
%{__install} -p -m0644 spacewalk-create-channel/data/* %{buildroot}/%{_datadir}/rhn/channel-data/

%{__mkdir_p} %{buildroot}/%{_mandir}/man1
%{__gzip} -c ./spacewalk-create-channel/doc/spacewalk-create-channel.1 > %{buildroot}/%{_mandir}/man1/spacewalk-create-channel.1.gz

%files
%defattr(-,root,root,-)
%{_bindir}/spacewalk-add-providers
%{_bindir}/spacewalk-create-channel
%{_datadir}/rhn/channel-data/
%if 0%{?suse_version}
%dir %{_datadir}/rhn
%endif
%doc spacewalk-create-channel/doc/README spacewalk-create-channel/doc/COPYING
%doc %{_mandir}/man1/spacewalk-create-channel.1.gz

%changelog
