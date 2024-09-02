#
# spec file for package spacewalk-search
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

%{!?java_version: %global java_version 11}
%if 0%{?suse_version}
%global apache_group www
%else
%define java_version   1:%{java_version}
%global apache_group apache
%endif

%define oro (oro or jakarta-oro)

Name:           spacewalk-search
Version:        5.1.0
Release:        0
Summary:        Spacewalk Full Text Search Server
License:        Apache-2.0 AND GPL-2.0-only
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Applications/Internet
# This src.rpm is cannonical upstream
# You can obtain it using this set of commands
# git clone https://github.com/spacewalkproject/spacewalk.git
# cd search-server
# make test-srpm
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
BuildRequires:  %{oro}
BuildRequires:  ant
BuildRequires:  apache-commons-cli
BuildRequires:  apache-commons-codec
BuildRequires:  apache-commons-lang3
BuildRequires:  apache-commons-logging
BuildRequires:  c3p0 >= 0.9.1
BuildRequires:  cglib
BuildRequires:  javapackages-tools
BuildRequires:  junit
BuildRequires:  lucene == 2.4.1
BuildRequires:  objectweb-asm
BuildRequires:  picocontainer
BuildRequires:  quartz >= 2.0
BuildRequires:  redstone-xmlrpc
BuildRequires:  simple-core
BuildRequires:  slf4j
BuildRequires:  systemd
BuildRequires:  (java-devel >= %{java_version} or java-%{java_version}-openjdk-devel)
BuildRequires:  mvn(javassist:javassist)
BuildRequires:  mvn(org.apache.commons:commons-ognl)
BuildRequires:  mvn(org.mybatis:mybatis)
%if 0%{?rhel}
BuildRequires:  systemd-rpm-macros
%endif
BuildRequires:  uyuni-base-common
BuildRequires:  zip
Requires(pre):  uyuni-base-common
Requires:       %{oro}
Requires:       apache-commons-cli
Requires:       apache-commons-codec
Requires:       apache-commons-lang3
Requires:       apache-commons-logging
Requires:       c3p0 >= 0.9.1
Requires:       cglib
Requires:       javapackages-tools
Requires:       lucene == 2.4.1
Requires:       objectweb-asm
Requires:       picocontainer
Requires:       quartz >= 2.0
Requires:       redstone-xmlrpc
Requires:       simple-core
Requires:       mvn(javassist:javassist)
Requires:       mvn(org.apache.commons:commons-ognl)
Requires:       mvn(org.mybatis:mybatis)
Obsoletes:      rhn-search < 5.3.0
Requires:       log4j
BuildRequires:  log4j
BuildArch:      noarch

%description
This package contains the code for the Full Text Search Server for
Spacewalk Server.

%prep
%setup -n %{name}-%{version}

%install
%if 0%{?rhel}
export JAVA_HOME=/usr/lib/jvm/java-%{java_version}-openjdk/
%endif
ant -Djar.version=%{version} install
install -d -m 755 %{buildroot}%{_datadir}/rhn/config-defaults
install -d -m 755 %{buildroot}%{_datadir}/rhn/search
install -d -m 755 %{buildroot}%{_datadir}/rhn/search/lib
install -d -m 755 %{buildroot}%{_datadir}/rhn/search/classes
install -d -m 755 %{buildroot}%{_var}/lib/rhn/search
install -d -m 755 %{buildroot}%{_var}/lib/rhn/search/indexes
install -d -m 755 %{buildroot}%{_sbindir}
install -d -m 755 %{buildroot}%{_unitdir}
install -d -m 755 %{buildroot}%{_bindir}
install -d -m 755 %{buildroot}%{_var}/log/rhn/search
install -d -m 755 %{buildroot}%{_sysconfdir}/logrotate.d
install -p -m 644 dist/%{name}-%{version}.jar %{buildroot}%{_datadir}/rhn/search/lib/
# using install -m does not preserve the symlinks
cp -d lib/* %{buildroot}%{_datadir}/rhn/search/lib

install -p -m 644 src/config%{_sysconfdir}/logrotate.d/rhn-search %{buildroot}%{_sysconfdir}/logrotate.d/rhn-search
install -p -m 755 src/config/rhn-search %{buildroot}%{_sbindir}
install -p -m 644 src/config/rhn-search.service %{buildroot}%{_unitdir}
install -p -m 644 src/config/search/rhn_search.conf %{buildroot}%{_datadir}/rhn/config-defaults/rhn_search.conf
install -p -m 644 src/config/search/rhn_search_daemon.conf %{buildroot}%{_datadir}/rhn/config-defaults/rhn_search_daemon.conf
install -p -m 644 src/config/log4j2.xml %{buildroot}%{_datadir}/rhn/search/classes/log4j2.xml
ln -s -f %{_datadir}/rhn/search/lib/spacewalk-search-%{version}.jar %{buildroot}%{_datadir}/rhn/search/lib/spacewalk-search.jar

# add rc link
mkdir -p  %{buildroot}%{_sbindir}/
ln -sf service %{buildroot}/%{_sbindir}/rcrhn-search

# cleanup unwanted jar
rm -f %{buildroot}%{_datadir}/rhn/search/lib/junit.jar

%post
%if 0%{?rhel}
%{systemd_post} rhn-search.service
%else
%service_add_post rhn-search.service
%endif

%preun
%if 0%{?rhel}
%systemd_preun rhn-search.service
%else
%service_del_preun rhn-search.service
%endif

%postun
%if 0%{?rhel}
%systemd_preun rhn-search.service
%else
%service_del_postun rhn-search.service
%endif

%pre
%if !0%{?rhel}
%service_add_pre rhn-search.service
%endif

%files
%defattr(644,root,root,755)
%attr(755, root, root) %{_var}/log/rhn/search
%dir %{_datadir}/rhn/search/classes/
%{_datadir}/rhn/search/lib/*
%attr(755, root, root) %{_sbindir}/rhn-search
%attr(644, root, root) %{_unitdir}/rhn-search.service
%{_datadir}/rhn/config-defaults/rhn_search.conf
%{_datadir}/rhn/config-defaults/rhn_search_daemon.conf
%{_datadir}/rhn/search/classes/log4j2.xml
%{_sysconfdir}/logrotate.d/rhn-search
%dir %attr(755, root, root) %{_var}/lib/rhn/search
%dir %attr(755, root, root) %{_var}/lib/rhn/search/indexes
%dir %attr(755, root, root) %{_var}/lib/rhn
%dir %{_datadir}/rhn
%dir %{_datadir}/rhn/search
%dir %{_datadir}/rhn/search/lib
%if 0%{?rhel}
%dir %{_var}/log/rhn
%else
%attr(770,root,%{apache_group}) %dir %{_var}/log/rhn
%endif
%{_sbindir}/rcrhn-search

%changelog
