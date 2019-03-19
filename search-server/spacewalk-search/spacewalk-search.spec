#
# spec file for package spacewalk-search
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


%{!?__redhat_release:%define __redhat_release UNKNOWN}

Name:           spacewalk-search
Summary:        Spacewalk Full Text Search Server
License:        GPL-2.0-only AND Apache-2.0
Group:          Applications/Internet
Version:        4.0.4
Release:        1%{?dist}
# This src.rpm is cannonical upstream
# You can obtain it using this set of commands
# git clone https://github.com/spacewalkproject/spacewalk.git
# cd search-server
# make test-srpm
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
ExcludeArch:    aarch64

BuildRequires:  apache-mybatis
BuildRequires:  hadoop
BuildRequires:  lucene
BuildRequires:  nutch-core
BuildRequires:  picocontainer
Requires:       apache-mybatis
Requires:       hadoop
Requires:       lucene
Requires:       nutch-core
Requires:       picocontainer

BuildRequires:  junit
#Requires: apache-ibatis-sqlmap
Requires:       c3p0 >= 0.9.1
Requires:       cglib
Requires(pre): doc-indexes
Requires:       jakarta-commons-httpclient
Requires:       jakarta-oro
Requires:       javapackages-tools
#Requires: lucene
Requires:       objectweb-asm
%if 0%{?fedora} || 0%{?rhel} >=7
Requires:       mchange-commons
%endif
Requires:       quartz >= 2.0
Requires:       redstone-xmlrpc
#Requires: picocontainer
Requires:       simple-core
Obsoletes:      rhn-search < 5.3.0
BuildRequires:  ant
#BuildRequires: apache-ibatis-sqlmap
BuildRequires:  c3p0 >= 0.9.1
BuildRequires:  cglib
BuildRequires:  jakarta-commons-httpclient
BuildRequires:  jakarta-oro
BuildRequires:  java-devel >= 11
BuildRequires:  javapackages-tools
#BuildRequires: lucene
BuildRequires:  objectweb-asm
BuildRequires:  quartz >= 2.0
BuildRequires:  redstone-xmlrpc
#BuildRequires: picocontainer
BuildRequires:  simple-core
BuildRequires:  slf4j
%if 0%{?fedora} || 0%{?rhel} >=7 || 0%{?suse_version} >= 1315
Requires:       apache-commons-cli
Requires:       apache-commons-codec
Requires:       apache-commons-lang3
Requires:       apache-commons-logging
BuildRequires:  apache-commons-cli
BuildRequires:  apache-commons-codec
BuildRequires:  apache-commons-lang3
BuildRequires:  apache-commons-logging
%else
Requires:       jakarta-commons-cli
Requires:       jakarta-commons-codec
Requires:       jakarta-commons-lang
Requires:       jakarta-commons-logging
BuildRequires:  jakarta-commons-cli
BuildRequires:  jakarta-commons-codec
BuildRequires:  jakarta-commons-lang
BuildRequires:  jakarta-commons-logging
%endif
%if 0%{?fedora} && 0%{?fedora} >= 21
Requires:       log4j12
BuildRequires:  log4j12
%else
Requires:       log4j
BuildRequires:  log4j
%endif
BuildRequires:  zip
%if 0%{?rhel} && 0%{?rhel} < 7
Requires(post): chkconfig
Requires(preun): chkconfig
# This is for /sbin/service
Requires(preun): initscripts
%endif
%if 0%{?fedora} || 0%{?rhel} >=7 || 0%{?suse_version} >= 1210
BuildRequires:  systemd
%endif
%if 0%{?suse_version}
BuildRequires:  doc-indexes
Requires:       nutch-core
%endif

%description
This package contains the code for the Full Text Search Server for
Spacewalk Server.

%prep
%setup -n %{name}-%{version}

# disable crash dumps in IBM java (OpenJDK have them off by default)
#if java -version 2>&1 | grep -q IBM ; then
#    sed -i '/#wrapper\.java\.additional\.[0-9]=-Xdump:none/ { s/^#//; }' \
#        src/config/search/rhn_search_daemon.conf
#fi

%install
rm -fr ${RPM_BUILD_ROOT}
ant -Djar.version=%{version} install
install -d -m 755 $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults
install -d -m 755 $RPM_BUILD_ROOT%{_prefix}/share/rhn/search
install -d -m 755 $RPM_BUILD_ROOT%{_prefix}/share/rhn/search/lib
install -d -m 755 $RPM_BUILD_ROOT%{_var}/lib/rhn/search
install -d -m 755 $RPM_BUILD_ROOT%{_var}/lib/rhn/search/indexes
ln -s -f %{_prefix}/share/rhn/search/indexes/docs $RPM_BUILD_ROOT%{_var}/lib/rhn/search/indexes/docs
install -d -m 755 $RPM_BUILD_ROOT%{_sbindir}
%if 0%{?fedora} || 0%{?rhel} >=7 || 0%{?suse_version} >= 1210
install -d -m 755 $RPM_BUILD_ROOT%{_unitdir}
%else
install -d -m 755 $RPM_BUILD_ROOT%{_initrddir}
%endif
install -d -m 755 $RPM_BUILD_ROOT%{_bindir}
install -d -m 755 $RPM_BUILD_ROOT%{_var}/log/rhn/search
install -d -m 755 $RPM_BUILD_ROOT%{_prefix}/share/rhn/search/nutch
install -d -m 755 $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d
install -p -m 644 dist/%{name}-%{version}.jar $RPM_BUILD_ROOT%{_prefix}/share/rhn/search/lib/
# using install -m does not preserve the symlinks
cp -d lib/* $RPM_BUILD_ROOT/%{_prefix}/share/rhn/search/lib

install -p -m 644 src/config/etc/logrotate.d/rhn-search $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d/rhn-search
install -p -m 755 src/config/rhn-search $RPM_BUILD_ROOT%{_sbindir}
%if 0%{?fedora} || 0%{?rhel} >=7 || 0%{?suse_version} >= 1210
install -p -m 644 src/config/rhn-search.service $RPM_BUILD_ROOT%{_unitdir}
%else
install -p -m 755 src/config/rhn-search.init $RPM_BUILD_ROOT%{_initrddir}/rhn-search
%endif
install -p -m 644 src/config/search/rhn_search.conf $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults/rhn_search.conf
install -p -m 644 src/config/search/rhn_search_daemon.conf $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults/rhn_search_daemon.conf
ln -s -f %{_prefix}/share/rhn/search/lib/spacewalk-search-%{version}.jar $RPM_BUILD_ROOT%{_prefix}/share/rhn/search/lib/spacewalk-search.jar
%if 0%{?fedora} && 0%{?fedora} >= 21
sed -i 's/log4j.jar/log4j-1.jar/' $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults/rhn_search_daemon.conf
%endif
%if 0%{?suse_version} >= 1315
sed -i 's/^wrapper.java.classpath.19=.*/wrapper.java.classpath.19=\/usr\/share\/nutch\/nutch-core-1.0.jar/' $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults/rhn_search_daemon.conf
%endif

# add rc link
mkdir -p  $RPM_BUILD_ROOT/%{_sbindir}/
%if 0%{?suse_version} >= 1210
ln -sf service $RPM_BUILD_ROOT/%{_sbindir}/rcrhn-search
%else
ln -sf ../../etc/init.d/rhn-search $RPM_BUILD_ROOT/%{_sbindir}/rcrhn-search
%endif

# mybatis is build with newer API. This statements needs to stay, until we have a build with 1.5
export NO_BRP_CHECK_BYTECODE_VERSION=true

%post
was_running=0
%if 0%{?suse_version} >= 1210
%service_add_post rhn-search.service
%else
if [ -f /etc/init.d/rhn-search ]; then
   # This adds the proper /etc/rc*.d links for the script
   /sbin/chkconfig --add rhn-search

   if /sbin/service rhn-search status > /dev/null 2>&1 ; then
       was_running=1
   fi
fi
%endif

# Migrate original /usr/share/rhn/search/indexes/*
# to /var/lib/rhn/search/indexes
cd %{_prefix}/share/rhn/search/indexes && /bin/ls | /bin/grep -v docs | while read i ; do
    if [ ! -e %{_var}/lib/rhn/search/indexes/$i ] ; then
        if [ $was_running -eq 1 ] ; then
            if [ -f /etc/init.d/rhn-search ]; then
               /sbin/service rhn-search stop > /dev/null 2>&1
               was_running=2
            fi
        fi
        /bin/mv $i %{_var}/lib/rhn/search/indexes/$i
        # If the mv failed for whatever reason, symlink
        if [ -e $i ] ; then
            /bin/rm -rf %{_var}/lib/rhn/search/indexes/$i
            /bin/ln -s -f %{_prefix}/share/rhn/search/indexes/$i %{_var}/lib/rhn/search/indexes/$i
        fi
    fi
done

if [ -f /etc/init.d/rhn-search ]; then
   if [ $was_running -eq 1 ] ; then
       /sbin/service rhn-search status > /dev/null 2>&1 || /sbin/service rhn-search start > /dev/null 2>&1
   fi
fi

%preun
%if 0%{?suse_version} >= 1210
%service_del_preun rhn-search.service
%else
if [ $1 = 0 ] ; then
    if [ -f /etc/init.d/rhn-search ]; then
       /sbin/service rhn-search stop >/dev/null 2>&1
       /sbin/chkconfig --del rhn-search
    fi
fi
%endif

%if 0%{?suse_version} >= 1210
%postun
%service_del_postun rhn-search.service

%pre
%service_add_pre rhn-search.service
%endif

%files
%defattr(644,root,root,755)
%attr(755, root, root) %{_var}/log/rhn/search
%{_prefix}/share/rhn/search/lib/*
%attr(755, root, root) %{_sbindir}/rhn-search
%if 0%{?fedora} || 0%{?rhel} >=7 || 0%{?suse_version} >= 1210
%attr(644, root, root) %{_unitdir}/rhn-search.service
%else
%attr(755, root, root) %{_initrddir}/rhn-search
%endif
%{_bindir}/rhnsearchd
%{_prefix}/share/rhn/config-defaults/rhn_search.conf
%{_prefix}/share/rhn/config-defaults/rhn_search_daemon.conf
%{_sysconfdir}/logrotate.d/rhn-search
%dir %attr(755, root, root) %{_var}/lib/rhn/search
%dir %attr(755, root, root) %{_var}/lib/rhn/search/indexes
%{_var}/lib/rhn/search/indexes/docs
%if 0%{?suse_version}
%dir %attr(755, root, root) %{_var}/lib/rhn
%dir %attr(755,root,www) %{_prefix}/share/rhn/config-defaults
%dir /usr/share/rhn
%dir /usr/share/rhn/search
%dir /usr/share/rhn/search/lib
%attr(770,root,www) %dir /var/log/rhn
%{_sbindir}/rcrhn-search
%endif

%changelog
