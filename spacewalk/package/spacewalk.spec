#
# spec file for package spacewalk
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
Version:        4.0.2
Release:        1%{?dist}
Summary:        Spacewalk Systems Management Application
License:        GPL-2.0-only
Group:          Applications/Internet
URL:            https://github.com/uyuni-project/uyuni
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch

%description
Spacewalk is a systems management application that will
inventory, provision, update and control your Linux machines.

%package common
Summary:        Spacewalk Systems Management Application with Oracle database backend
Group:          Applications/Internet
Obsoletes:      spacewalk < 0.7.0

BuildRequires:  python
Requires:       python >= 2.3
Requires:       spacewalk-setup

# Java
Requires:       spacewalk-java
Requires:       spacewalk-search
Requires:       spacewalk-taskomatic

# Perl
Requires:       spacewalk-base
Requires:       spacewalk-html

# Python
Requires:       mgr-push
Requires:       spacewalk-backend
Requires:       spacewalk-backend-app
Requires:       spacewalk-backend-applet
Requires:       spacewalk-backend-config-files
Requires:       spacewalk-backend-config-files-common
Requires:       spacewalk-backend-config-files-tool
Requires:       spacewalk-backend-iss
Requires:       spacewalk-backend-iss-export
Requires:       spacewalk-backend-package-push-server
Requires:       spacewalk-backend-server
Requires:       spacewalk-backend-sql
Requires:       spacewalk-backend-tools
Requires:       spacewalk-backend-xml-export-libs
Requires:       spacewalk-backend-xmlrpc
Requires:       spacewalk-certs-tools

# Misc
Requires:       pxe-default-image
Requires:       spacewalk-config
Requires:       spacewalk-schema

Requires:       virtual-host-gatherer
Recommends:     virtual-host-gatherer-VMware
Requires:       subscription-matcher
Requires:       susemanager-sls

# Requires:       mgr-osa-dispatcher
# Requires:       jabberpy
Obsoletes:      spacewalk-monitoring < 2.3

%if 0%{?rhel} || 0%{?fedora}
# SELinux
Requires:       mgr-osa-dispatcher-selinux
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
Summary:        Spacewalk Systems Management Application with Oracle database backend
Group:          Applications/Internet
Obsoletes:      spacewalk < 0.7.0
Requires:       spacewalk-common = %{version}-%{release}
Conflicts:      spacewalk-postgresql
Provides:       spacewalk-db-virtual = %{version}-%{release}

Requires:       cx_Oracle
Requires:       oracle-lib-compat
Requires:       spacewalk-backend-sql-oracle
Requires:       spacewalk-java-oracle
Requires:       perl(DBD::Oracle)
%if 0%{?rhel} || 0%{?fedora}
Requires:       oracle-instantclient-selinux
Requires:       oracle-instantclient-sqlplus-selinux
%endif

Obsoletes:      spacewalk-dobby < 2.7.0

%description oracle
Spacewalk is a systems management application that will
inventory, provision, update and control your Linux machines.
Version for Oracle database backend.
%endif

%package postgresql
Summary:        Spacewalk Systems Management Application with PostgreSQL database backend
Group:          Applications/Internet
Obsoletes:      spacewalk < 0.7.0
Requires:       spacewalk-common = %{version}-%{release}
Conflicts:      spacewalk-oracle
Provides:       spacewalk-db-virtual = %{version}-%{release}

Requires:       %{postgresql}
Requires:       spacewalk-backend-sql-postgresql
Requires:       spacewalk-java-postgresql
Requires:       perl(DBD::Pg)
%if 0%{?rhel} == 5
Requires:       postgresql84-contrib
%else
Requires:       postgresql-contrib >= 8.4
%endif
Requires:       postgresql >= 9.6
# we do not support postgresql versions > 10.x yet
Conflicts:      postgresql >= 11
Conflicts:      postgresql-contrib >= 11

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
