Name:           oracle-lib-compat
Version:        12.1.0.2.3
Release:        1%{?dist}
Summary:        Compatibility package so that perl-DBD-Oracle will install
Group:          Applications/Multimedia
License:        GPLv2
# This src.rpm is cannonical upstream
# You can obtain it using this set of commands
# git clone git://git.fedorahosted.org/git/spacewalk.git/
# cd spec-tree/oracle-lib-compat
# make srpm
URL:            https://github.com/spacewalkproject/spacewalk
Source0:	https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-root-%(%{__id_u} -n)
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
BuildRequires:       oracle-instantclient12.1-basic >= %{icversion}
BuildRequires:       oracle-instantclient12.1-sqlplus >= %{icversion}
%else
%define icversion 12.1.0
%define icdir 12.1
%define soversion 12.1
Requires(pre):       oracle-instantclient12.1-basic >= %{icversion}
Requires(pre):       oracle-instantclient12.1-sqlplus >= %{icversion}
BuildRequires:       oracle-instantclient12.1-basic >= %{icversion}
BuildRequires:       oracle-instantclient12.1-sqlplus >= %{icversion}
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
Provides:       libocijdbc12.so%{?lib64}
Provides:       libociei.so%{?lib64}
Provides:       ojdbc14                    = %{version}
Obsoletes:      rhn-oracle-jdbc           <= 1.0
Requires:       libstdc++.so.6%{?lib64}

%description
Compatibility package so that perl-DBD-Oracle will install.

%prep
%setup -q

%build

%install
rm -rf $RPM_BUILD_ROOT
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


%clean
rm -rf $RPM_BUILD_ROOT

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
* Tue Nov 10 2015 Tomas Kasparek <tkasparek@redhat.com> 11.2.0.13-1
- don't build debug package for oracle-lib-compat

* Thu Jan 29 2015 Tomas Lestach <tlestach@redhat.com> 11.2.0.12-1
- we need to use the exact oracle instantclient version

* Thu Jan 29 2015 Tomas Lestach <tlestach@redhat.com> 11.2.0.11-1
- do not require exact version of oracle instantclient

* Wed Oct 22 2014 Michael Mraka <michael.mraka@redhat.com> 11.2.0.10-1
- oracle-instantclient11.2 requires libstdc++.so.6

* Wed Jan 22 2014 Michael Mraka <michael.mraka@redhat.com> 11.2.0.9-1
- LD_PRELOAD setup has been moved to spacewalk-setup-tomcat
- Purging %%changelog entries preceding Spacewalk 1.0, in active packages.

* Tue Dec 04 2012 Jan Pazdziora 11.2.0.8-1
- On Fedoras, start to use tomcat >= 7.

* Mon Oct 08 2012 Jan Pazdziora 11.2.0.7-1
- Require latest greatest oracle-instantclient11.2-*.
- %%defattr is not needed since rpm 4.4

* Fri Feb 03 2012 Jan Pazdziora 11.2.0.6-1
- Avoid cat: write error: Broken pipe when calling tomcat service under trap ''
  PIPE

* Mon May 16 2011 Jan Pazdziora 11.2.0.5-1
- Both tomcat5 and tomcat6 which needs the LD_PRELOAD set.

* Wed May 04 2011 Jan Pazdziora 11.2.0.4-1
- We unset LD_PRELOAD to force ldd to show the libldap line with => even if
  LD_PRELOAD was already set.

* Mon Jan 17 2011 Jan Pazdziora 11.2.0.3-1
- Set diag_adr_enabled to off.

* Mon Jan 10 2011 Jan Pazdziora 11.2.0.2-1
- On x86_64, require 64bit version of libaio for InstantClient 11g.

* Fri Jan 07 2011 Jan Pazdziora 11.2.0.1-1
- Have separate ld.so.conf.d for InstantClient and for XE server.
- InstantClient 11 contains ojdbc5 and ojdbc6, we will change the target of the
  symlink for now.
- Need to use the "11" in .so Provides as well.
- Switch to Oracle InstantClient 11 in oracle-lib-compat.

* Thu Sep 23 2010 Michael Mraka <michael.mraka@redhat.com> 10.2.0.25-1
- instantclient on s390(x) upgraded to 10.2.0.4
- switched to default VersionTagger

* Thu Sep 23 2010 Jan Pazdziora 10.2-24
- 623115 - file lookup using just the linker name (libldap.so) fails if
  openldap-devel is not installed.

* Mon Sep 13 2010 Jan Pazdziora 10.2-23
- 623115 - force tomcat to use the stock openldap, overriding the ldap_*
  symbols in libclntsh.so*.

