#
# spec file for package spacewalk-search
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

%if 0%{?suse_version}
%define java_version   11
%else
%define java_version   1:11
%endif

Name:           spacewalk-search
Summary:        Spacewalk Full Text Search Server
License:        GPL-2.0-only AND Apache-2.0
Group:          Applications/Internet
Version:        4.2.1
Release:        1%{?dist}
# This src.rpm is cannonical upstream
# You can obtain it using this set of commands
# git clone https://github.com/spacewalkproject/spacewalk.git
# cd search-server
# make test-srpm
Url:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch

BuildRequires:  ant
BuildRequires:  apache-commons-cli
BuildRequires:  apache-commons-codec
BuildRequires:  apache-commons-lang3
BuildRequires:  apache-commons-logging
BuildRequires:  apache-mybatis
BuildRequires:  c3p0 >= 0.9.1
BuildRequires:  cglib
BuildRequires:  doc-indexes
BuildRequires:  hadoop
BuildRequires:  jakarta-commons-httpclient
BuildRequires:  jakarta-oro
BuildRequires:  java-devel >= %{java_version}
BuildRequires:  javapackages-tools
BuildRequires:  junit
BuildRequires:  lucene == 2.4.1
BuildRequires:  nutch-core
BuildRequires:  objectweb-asm
BuildRequires:  picocontainer
BuildRequires:  quartz >= 2.0
BuildRequires:  redstone-xmlrpc
BuildRequires:  simple-core
BuildRequires:  slf4j
BuildRequires:  systemd
BuildRequires:  uyuni-base-common
BuildRequires:  zip
Requires(pre):  doc-indexes
Requires(pre):  uyuni-base-common
Requires:       apache-commons-cli
Requires:       apache-commons-codec
Requires:       apache-commons-lang3
Requires:       apache-commons-logging
Requires:       apache-mybatis
Requires:       c3p0 >= 0.9.1
Requires:       cglib
Requires:       hadoop
Requires:       jakarta-commons-httpclient
Requires:       jakarta-oro
Requires:       javapackages-tools
Requires:       lucene == 2.4.1
Requires:       nutch-core
Requires:       objectweb-asm
Requires:       picocontainer
Requires:       quartz >= 2.0
Requires:       redstone-xmlrpc
Requires:       simple-core
Obsoletes:      rhn-search < 5.3.0
%if 0%{?fedora} || 0%{?rhel} >=7
Requires:       mchange-commons
%endif
%if 0%{?fedora} >= 21 || 0%{?sle_version} >= 150200
Requires:       log4j12
BuildRequires:  log4j12
%else
Requires:       log4j
BuildRequires:  log4j
%endif

%description
This package contains the code for the Full Text Search Server for
Spacewalk Server.

%prep
%setup -n %{name}-%{version}

%install
rm -fr ${RPM_BUILD_ROOT}
ant -Djar.version=%{version} install
install -d -m 755 $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults
install -d -m 755 $RPM_BUILD_ROOT%{_prefix}/share/rhn/search
install -d -m 755 $RPM_BUILD_ROOT%{_prefix}/share/rhn/search/lib
install -d -m 755 $RPM_BUILD_ROOT%{_prefix}/share/rhn/search/classes
install -d -m 755 $RPM_BUILD_ROOT%{_var}/lib/rhn/search
install -d -m 755 $RPM_BUILD_ROOT%{_var}/lib/rhn/search/indexes
ln -s -f %{_prefix}/share/rhn/search/indexes/docs $RPM_BUILD_ROOT%{_var}/lib/rhn/search/indexes/docs
install -d -m 755 $RPM_BUILD_ROOT%{_sbindir}
install -d -m 755 $RPM_BUILD_ROOT%{_unitdir}
install -d -m 755 $RPM_BUILD_ROOT%{_bindir}
install -d -m 755 $RPM_BUILD_ROOT%{_var}/log/rhn/search
install -d -m 755 $RPM_BUILD_ROOT%{_prefix}/share/rhn/search/nutch
install -d -m 755 $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d
install -p -m 644 dist/%{name}-%{version}.jar $RPM_BUILD_ROOT%{_prefix}/share/rhn/search/lib/
# using install -m does not preserve the symlinks
cp -d lib/* $RPM_BUILD_ROOT/%{_prefix}/share/rhn/search/lib

install -p -m 644 src/config/etc/logrotate.d/rhn-search $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d/rhn-search
install -p -m 755 src/config/rhn-search $RPM_BUILD_ROOT%{_sbindir}
install -p -m 644 src/config/rhn-search.service $RPM_BUILD_ROOT%{_unitdir}
install -p -m 644 src/config/search/rhn_search.conf $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults/rhn_search.conf
install -p -m 644 src/config/search/rhn_search_daemon.conf $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults/rhn_search_daemon.conf
install -p -m 644 src/config/log4j.properties $RPM_BUILD_ROOT%{_prefix}/share/rhn/search/classes/log4j.properties
ln -s -f %{_prefix}/share/rhn/search/lib/spacewalk-search-%{version}.jar $RPM_BUILD_ROOT%{_prefix}/share/rhn/search/lib/spacewalk-search.jar

# add rc link
mkdir -p  $RPM_BUILD_ROOT/%{_sbindir}/
ln -sf service $RPM_BUILD_ROOT/%{_sbindir}/rcrhn-search

%post
%service_add_post rhn-search.service

%preun
%service_del_preun rhn-search.service

%postun
%service_del_postun rhn-search.service

%pre
%service_add_pre rhn-search.service

%files
%defattr(644,root,root,755)
%attr(755, root, root) %{_var}/log/rhn/search
%dir /usr/share/rhn/search/classes/
%{_prefix}/share/rhn/search/lib/*
%attr(755, root, root) %{_sbindir}/rhn-search
%attr(644, root, root) %{_unitdir}/rhn-search.service
%{_prefix}/share/rhn/config-defaults/rhn_search.conf
%{_prefix}/share/rhn/config-defaults/rhn_search_daemon.conf
%{_prefix}/share/rhn/search/classes/log4j.properties
%{_sysconfdir}/logrotate.d/rhn-search
%dir %attr(755, root, root) %{_var}/lib/rhn/search
%dir %attr(755, root, root) %{_var}/lib/rhn/search/indexes
%{_var}/lib/rhn/search/indexes/docs
%dir %attr(755, root, root) %{_var}/lib/rhn
%dir /usr/share/rhn
%dir /usr/share/rhn/search
%dir /usr/share/rhn/search/lib
%attr(770,root,www) %dir /var/log/rhn
%{_sbindir}/rcrhn-search

%changelog
