# Copyright (c) 2008-2018 Red Hat, Inc.

%define release_name Smile
%if 0%{?suse_version}
%global postgresql postgresql >= 8.4
%else
%global postgresql /usr/bin/psql
%endif
%if !0%{?is_opensuse}
%define with_oracle     1
%endif

Name:           spacewalk
Version:        2.8.2.2
Release:        1%{?dist}
Summary:        Spacewalk Systems Management Application
URL:            https://github.com/spacewalkproject/spacewalk
Group:          Applications/Internet
License:        GPLv2
BuildRoot:      %{_tmppath}/%{name}-root-%(%{__id_u} -n)
BuildArch:      noarch

%description
Spacewalk is a systems management application that will
inventory, provision, update and control your Linux machines.

%package common
Summary: Spacewalk Systems Management Application with Oracle database backend
Group:   Applications/Internet
License: GPLv2
Obsoletes: spacewalk < 0.7.0

BuildRequires:  python
Requires:       python >= 2.3
Requires:       spacewalk-setup

# Java
Requires:       spacewalk-java
Requires:       spacewalk-taskomatic
Requires:       spacewalk-search

# Perl
Requires:       spacewalk-html
Requires:       spacewalk-base

# Python
Requires:       spacewalk-certs-tools
Requires:       spacewalk-backend
Requires:       spacewalk-backend-app
Requires:       spacewalk-backend-applet
Requires:       spacewalk-backend-config-files
Requires:       spacewalk-backend-config-files-common
Requires:       spacewalk-backend-config-files-tool
Requires:       spacewalk-backend-iss
Requires:       spacewalk-backend-iss-export
Requires:       spacewalk-backend-package-push-server
Requires:       spacewalk-backend-tools
Requires:       spacewalk-backend-server
Requires:       spacewalk-backend-sql
Requires:       spacewalk-backend-xml-export-libs
Requires:       spacewalk-backend-xmlrpc
Requires:       rhnpush


# Misc
Requires:       spacewalk-schema
Requires:       spacewalk-config
Requires:       yum-utils
Requires:       pxe-default-image

Requires:       virtual-host-gatherer
Recommends:     virtual-host-gatherer-VMware
Requires:       subscription-matcher
Requires:       susemanager-sls

# Requires:       osa-dispatcher
# Requires:       jabberpy
Obsoletes:      spacewalk-monitoring < 2.3

%if 0%{?rhel} || 0%{?fedora}
# SELinux
Requires:       osa-dispatcher-selinux
Requires:       spacewalk-selinux
Obsoletes:      spacewalk-monitoring-selinux < 2.3
%endif

%if 0%{?rhel} == 5
Requires:       jabberd-selinux
%endif
%if 0%{?rhel} == 6
Requires:       selinux-policy-base >= 3.7.19-93
%endif


%if 0%{?suse_version}
Requires:       cobbler
Requires:       susemanager-jsp_en
%else
Requires:       cobbler20
%endif

%description common
Spacewalk is a systems management application that will
inventory, provision, update and control your Linux machines.

%if 0%{?with_oracle}
%package oracle
Summary: Spacewalk Systems Management Application with Oracle database backend
Group:   Applications/Internet
License: GPLv2
Obsoletes: spacewalk < 0.7.0
Requires:  spacewalk-common = %{version}-%{release}
Conflicts: spacewalk-postgresql
Provides:  spacewalk-db-virtual = %{version}-%{release}

Requires: oracle-lib-compat
Requires: spacewalk-java-oracle
Requires: perl(DBD::Oracle)
Requires: cx_Oracle
Requires: spacewalk-backend-sql-oracle
%if 0%{?rhel} || 0%{?fedora}
Requires: oracle-instantclient-selinux
Requires: oracle-instantclient-sqlplus-selinux
%endif

Obsoletes: spacewalk-dobby < 2.7.0

%description oracle
Spacewalk is a systems management application that will
inventory, provision, update and control your Linux machines.
Version for Oracle database backend.
%endif

%package postgresql
Summary: Spacewalk Systems Management Application with PostgreSQL database backend
Group:   Applications/Internet
License: GPLv2
Obsoletes: spacewalk < 0.7.0
Requires:  spacewalk-common = %{version}-%{release}
Conflicts: spacewalk-oracle
Provides:  spacewalk-db-virtual = %{version}-%{release}

Requires: spacewalk-java-postgresql
Requires: perl(DBD::Pg)
Requires: spacewalk-backend-sql-postgresql
Requires: %{postgresql}
%if 0%{?rhel} == 5
Requires: postgresql84-contrib
%else
Requires: postgresql-contrib >= 8.4
%endif
Requires: postgresql >= 8.4
# we do not support postgresql versions > 9.6 yet
Conflicts: postgresql >= 9.7
Conflicts: postgresql-contrib >= 9.7

%description postgresql
Spacewalk is a systems management application that will 
inventory, provision, update and control your Linux machines.
Version for PostgreSQL database backend.

%prep
#nothing to do here

%build
#nothing to do here

%install
%if 0%{?with_oracle}
RDBMS="oracle postgresql"
%else
RDBMS="postgresql"
%endif
install -d $RPM_BUILD_ROOT/%{_sysconfdir}
SW_REL=$(echo %{version} | awk -F. '{print $1"."$2}')
echo "Spacewalk release $SW_REL (%{release_name})" > $RPM_BUILD_ROOT/%{_sysconfdir}/spacewalk-release
install -d $RPM_BUILD_ROOT/%{_datadir}/spacewalk/setup/defaults.d
for i in ${RDBMS} ; do
        cat <<EOF >$RPM_BUILD_ROOT/%{_datadir}/spacewalk/setup/defaults.d/$i-backend.conf
# database backend to be used by spacewalk
db-backend = $i
EOF
done


%files common
%{_sysconfdir}/spacewalk-release
%if 0%{?suse_version}
%dir %{_datadir}/spacewalk
%dir %{_datadir}/spacewalk/setup
%dir %{_datadir}/spacewalk/setup/defaults.d
%endif

%if 0%{?with_oracle}
%files oracle
%{_datadir}/spacewalk/setup/defaults.d/oracle-backend.conf
%endif

%files postgresql
%{_datadir}/spacewalk/setup/defaults.d/postgresql-backend.conf

%changelog
* Fri Feb 09 2018 Michael Mraka <michael.mraka@redhat.com> 2.8.2-1
- remove install/clean section initial cleanup
- removed Group from specfile
- removed BuildRoot from specfiles

* Wed Sep 06 2017 Michael Mraka <michael.mraka@redhat.com> 2.8.1-1
- purged changelog entries for Spacewalk 2.0 and older
- Bumping package versions for 2.8.

* Wed Aug 09 2017 Michael Mraka <michael.mraka@redhat.com> 2.7.4-1
- obsolete only older (oracle) version of dobby

* Wed Aug 02 2017 Tomas Kasparek <tkasparek@redhat.com> 2.7.3-1
- 1449124 - db-control no longer works with oracle, let's add
  conflicts/obsoletes

* Tue May 09 2017 Tomas Kasparek <tkasparek@redhat.com> 2.7.2-1
- 1447591 - pull in spacewalk-dobby just on postgresql

* Wed May 03 2017 Tomas Kasparek <tkasparek@redhat.com> 2.7.1-1
- 1447591 - install spacewalk-dobby
- Migrating Fedorahosted to GitHub
- Bumping package versions for 2.7.

* Mon Sep 12 2016 Jan Dobes 2.6.1-1
- Fixing spacewalk, spacewalk-common, spacewalk-oracle and spacewalk-postgresql
  package descriptions. Solaris support has been dropped so I removed mentions
  of Solaris. I also mentioned that the -oracle and -postgresql packages differ
  in database backend used.
- Bumping package versions for 2.6.

* Tue May 10 2016 Grant Gainey 2.5.1-1
- spacewalk: build on openSUSE
- Bumping package versions for 2.5.

* Fri Jul 24 2015 Tomas Kasparek <tkasparek@redhat.com> 2.4.2-1
- require cobbler20 - Spacewalk is not working with upstream cobbler anyway

* Wed Jun 24 2015 Tomas Kasparek <tkasparek@redhat.com> 2.4.1-1
- Recommend cobbler20 with all packages requiring cobbler on Fedora 22
- Bumping package versions for 2.4.

* Wed Mar 25 2015 Tomas Lestach <tlestach@redhat.com> 2.3.4-1
- 1205113 - obsoleting spacewalk-monitoring and spacewalk-monitoring-selinux
  packages

* Tue Mar 17 2015 Tomas Lestach <tlestach@redhat.com> 2.3.3-1
- removing spacewalk-pxt completelly

* Mon Mar 09 2015 Tomas Lestach <tlestach@redhat.com> 2.3.2-1
- removing spacewalk-grail as they are not needed any more
- removing spacewalk-sniglets as they are not needed any more

* Fri Jan 16 2015 Tomas Lestach <tlestach@redhat.com> 2.3.1-1
- remove Monitoring and Solaris dependencies
- Bumping package versions for 2.3.

* Fri Apr 25 2014 Michael Mraka <michael.mraka@redhat.com> 2.2.2-1
- editarea has been replaced with ace-editor

* Thu Mar 20 2014 Matej Kollar <mkollar@redhat.com> 2.2.1-1
- Remove unnecessary dependency
- Bumping package versions for 2.2.
- fixed tito build warning
- Bumping package versions for 2.1.

