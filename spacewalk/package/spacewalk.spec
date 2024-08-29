#
# spec file for package spacewalk
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


Name:           spacewalk
Version:        5.1.0
Release:        0
Summary:        Spacewalk Systems Management Application
License:        GPL-2.0-only
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Applications/Internet
URL:            https://github.com/uyuni-project/uyuni
Source:         https://github.com/uyuni-project/uyuni/archive/%{name}-%{version}.tar.gz
BuildArch:      noarch

%description
Spacewalk is a systems management application that will
inventory, provision, update and control your Linux machines.

%package common
Summary:        Spacewalk Systems Management Application with postgresql database backend
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Applications/Internet
BuildRequires:  python3
BuildRequires:  spacewalk-backend
BuildRequires:  spacewalk-base-minimal-config
Requires:       python3
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
%if 0%{?opensuse}
Requires:       pxe-default-image
%endif
Requires:       spacewalk-config
Requires:       spacewalk-schema

Requires:       virtual-host-gatherer
Recommends:     virtual-host-gatherer-VMware
Requires:       subscription-matcher
Requires:       susemanager-sls

Requires:       cobbler
Requires:       susemanager-jsp_en

# weakremover used on SUSE to get rid of orphan packages which are
# unsupported and do not have a dependency anymore
Provides:       weakremover(jabberd)
Provides:       weakremover(jabberd-db)
Provides:       weakremover(jabberd-sqlite)
Provides:       weakremover(mgr-osa-dispatcher)
Provides:       weakremover(python3-jabberpy)
Provides:       weakremover(python3-mgr-osa-common)
Provides:       weakremover(python3-mgr-osa-dispatcher)
Provides:       weakremover(spacewalk-setup-jabberd)

%description common
Spacewalk is a systems management application that will
inventory, provision, update and control your Linux machines.

%package postgresql
Summary:        Spacewalk Systems Management Application with PostgreSQL database backend
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Applications/Internet
Requires:       spacewalk-common = %{version}-%{release}
Conflicts:      spacewalk-oracle
Provides:       spacewalk-db-virtual = %{version}-%{release}

Requires:       spacewalk-backend-sql-postgresql
Requires:       spacewalk-java-postgresql
Requires:       perl(DBD::Pg)
%if 0%{?suse_version}
# Actual version set by prjconf, default is 14
%{!?postgresql_version_min: %global postgresql_version_min 14}
%{!?postgresql_version_max: %global postgresql_version_max 15}
Requires:       postgresql-contrib-implementation >= %{postgresql_version_min}
Requires:       postgresql-implementation >= %{postgresql_version_min}
Conflicts:      postgresql-contrib-implementation > %{postgresql_version_max}
Conflicts:      postgresql-implementation > %{postgresql_version_max}
%else # not a supported SUSE version or alternative OS.
Requires:       postgresql14
Requires:       postgresql14-contrib
# we do not support postgresql versions > 14.x yet
# Hardcoded v15 conflict due to PostgreSQL bug 17507 (instead of >= 15)
Conflicts:      postgresql15
Conflicts:      postgresql15-contrib
%endif # if sle_Version

%description postgresql
Spacewalk is a systems management application that will
inventory, provision, update and control your Linux machines.
Version for PostgreSQL database backend.

%prep
%setup -q

%build
#nothing to do here

%install
RDBMS="postgresql"
install -d %{buildroot}%{_sysconfdir}
install -d %{buildroot}%{_datadir}/spacewalk/setup/defaults.d
for i in ${RDBMS} ; do
    cat <<EOF >%{buildroot}%{_datadir}/spacewalk/setup/defaults.d/$i-backend.conf
# database backend to be used by spacewalk
db-backend = $i
EOF
done
install -d %{buildroot}%{_bindir}
%if 0%{?rhel}
ln -s %{_prefix}/pgsql-14/bin/initdb %{buildroot}%{_bindir}/initdb
%endif

%files common
%{!?_licensedir:%global license %doc}
%license LICENSE
%if 0%{?suse_version}
%dir %{_datadir}/spacewalk
%dir %{_datadir}/spacewalk/setup
%dir %{_datadir}/spacewalk/setup/defaults.d
%endif

%files postgresql
%{_datadir}/spacewalk/setup/defaults.d/postgresql-backend.conf
%if 0%{?rhel}
%{_bindir}/initdb
%endif

%changelog
