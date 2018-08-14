#
# spec file for package oracle-lib-compat
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


Name:           oracle-lib-compat
Version:        12.1.0.2.7
Release:        1%{?dist}
Summary:        Compatibility package so that perl-DBD-Oracle will install
# This src.rpm is cannonical upstream
# You can obtain it using this set of commands
# git clone https://github.com/spacewalkproject/spacewalk.git
# cd spec-tree/oracle-lib-compat
# make srpm
License:        GPL-2.0-only
Group:          Applications/Multimedia
URL:            https://github.com/spacewalkproject/spacewalk
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
ExclusiveArch:  %ix86 x86_64 s390x ppc64le

%define debug_package %{nil}

%ifarch s390 s390x ppc64le
%define icversion 12.1.0
%define icdir 12.1
%define soversion 12.1
# For s390x and ppc64le Oracle provide only a zip file.
# The customer should extract it to
#   /usr/lib/oracle/%{icdir}/client64/lib/
# but we use our package as BuildRequires only
BuildRequires:  oracle-instantclient12.1-basic >= %{icversion}
BuildRequires:  oracle-instantclient12.1-sqlplus >= %{icversion}
%else
%define icversion 12.1.0
%define icdir 12.1
%define soversion 12.1
Requires(pre):       oracle-instantclient12.1-basic >= %{icversion}
Requires(pre):       oracle-instantclient12.1-sqlplus >= %{icversion}
BuildRequires:  oracle-instantclient12.1-basic >= %{icversion}
BuildRequires:  oracle-instantclient12.1-sqlplus >= %{icversion}
%endif

%if 0%{?suse_version}
Requires(post): file
Requires(post): findutils
%else
Requires(post): ldconfig
Requires(post): /usr/bin/execstack
Requires(post): /usr/bin/file
Requires(post): /usr/bin/xargs
%endif

%ifarch x86_64 s390x ppc64le
%define lib64 ()(64bit)
Requires:       libaio.so.1%{lib64}
%endif
Provides:       libclntsh.so.%{soversion}%{?lib64}
Provides:       libclntshcore.so.%{soversion}%{?lib64}
Provides:       libnnz12.so%{?lib64}
Provides:       libocci.so.%{soversion}%{?lib64}
Provides:       libociei.so%{?lib64}
Provides:       libocijdbc12.so%{?lib64}
Provides:       ojdbc14                    = %{version}
Obsoletes:      rhn-oracle-jdbc           <= 1.0
Requires:       libstdc++.so.6%{?lib64}

%description
Compatibility package so that perl-DBD-Oracle will install.

%prep
%setup -q

%build

%install
mkdir -p $RPM_BUILD_ROOT

mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/ld.so.conf.d
echo %{_libdir}/oracle/%{icdir}/client/lib >>$RPM_BUILD_ROOT%{_sysconfdir}/ld.so.conf.d/oracle-instantclient-%{icdir}.conf

# do not replace /usr/lib with _libdir macro here
# XE server is 32bit even on 64bit platforms
#echo /usr/lib/oracle/xe/app/oracle/product/10.2.0/server/lib >>$RPM_BUILD_ROOT%{_sysconfdir}/ld.so.conf.d/oracle-xe.conf

%ifarch x86_64 s390x ppc64le
mkdir -p $RPM_BUILD_ROOT%{_bindir}
ln -s ../lib/oracle/%{icdir}/client64/bin/sqlplus $RPM_BUILD_ROOT%{_bindir}/sqlplus

mkdir -p $RPM_BUILD_ROOT%{_libdir}/oracle/%{icdir}
ln -sf ../../../lib/oracle/%{icdir}/client64 $RPM_BUILD_ROOT%{_libdir}/oracle/%{icdir}/client

mkdir -p $RPM_BUILD_ROOT/usr/lib/oracle/%{icdir}/client64/lib/network/admin
echo 'diag_adr_enabled = off' > $RPM_BUILD_ROOT/usr/lib/oracle/%{icdir}/client64/lib/network/admin/sqlnet.ora

mkdir -p $RPM_BUILD_ROOT/%{_javadir}
ln -s ../../lib/oracle/%{icdir}/client64/lib/ojdbc7.jar $RPM_BUILD_ROOT/%{_javadir}/ojdbc14.jar
%endif

%files
%defattr(-,root,root,-)
%ifarch x86_64 s390x ppc64le
%{_bindir}/sqlplus
%{_libdir}/oracle
%dir /usr/lib/oracle/%{icdir}/client64/lib/network
%dir /usr/lib/oracle/%{icdir}/client64/lib/network/admin
/usr/lib/oracle/%{icdir}/client64/lib/network/admin/sqlnet.ora
%endif
%config %{_sysconfdir}/ld.so.conf.d/oracle-instantclient-%{icdir}.conf
%{_javadir}/ojdbc14.jar

%post
ldconfig

%changelog
