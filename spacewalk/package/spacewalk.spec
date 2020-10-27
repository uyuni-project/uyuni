#
# spec file for package spacewalk
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


Name:           spacewalk
Version:        4.2.1
Release:        1%{?dist}
Summary:        Spacewalk Systems Management Application
License:        GPL-2.0-only
Group:          Applications/Internet
Url:            https://github.com/uyuni-project/uyuni
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch

%description
Spacewalk is a systems management application that will
inventory, provision, update and control your Linux machines.

%package common
Summary:        Spacewalk Systems Management Application with postgresql database backend
Group:          Applications/Internet
Obsoletes:      spacewalk < 0.7.0

BuildRequires:  python3
BuildRequires:  spacewalk-base-minimal-config
BuildRequires:  spacewalk-backend
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

%if 0%{?rhel} == 6
Requires:       selinux-policy-base >= 3.7.19-93
%endif

Requires:       cobbler >= 3
Requires:       susemanager-jsp_en

%description common
Spacewalk is a systems management application that will
inventory, provision, update and control your Linux machines.

%package postgresql
Summary:        Spacewalk Systems Management Application with PostgreSQL database backend
Group:          Applications/Internet
Obsoletes:      spacewalk < 0.7.0
Requires:       spacewalk-common = %{version}-%{release}
Conflicts:      spacewalk-oracle
Provides:       spacewalk-db-virtual = %{version}-%{release}

Requires:       spacewalk-backend-sql-postgresql
Requires:       spacewalk-java-postgresql
Requires:       perl(DBD::Pg)
%if 0%{?suse_version}
%if 0%{?sle_version} >= 150200
Requires:       postgresql12
Requires:       postgresql12-contrib
# we do not support postgresql versions > 12.x yet
Conflicts:      postgresql-implementation >= 13
Conflicts:      postgresql-contrib-implementation >= 13
%else
# mainly for openSUSE Leap 15.1
Requires:       postgresql10
Requires:       postgresql10-contrib
Conflicts:      postgresql-implementation >= 12
Conflicts:      postgresql-contrib-implementation >= 12
%endif
%else
Requires:       postgresql >= 12
Requires:       postgresql-contrib >= 12
# we do not support postgresql versions > 12.x yet
Conflicts:      postgresql >= 13
Conflicts:      postgresql-contrib >= 13
%endif

%description postgresql
Spacewalk is a systems management application that will 
inventory, provision, update and control your Linux machines.
Version for PostgreSQL database backend.

%prep
#nothing to do here

%build
#nothing to do here

%install
RDBMS="postgresql"
install -d $RPM_BUILD_ROOT/%{_sysconfdir}
SUMA_REL=$(echo %{version} | awk -F. '{print $1"."$2}')
UYUNI_REL=$(grep -F 'web.version.uyuni' %{_datadir}/rhn/config-defaults/rhn_web.conf | sed 's/^.*= *\([[:digit:]\.]\+\) *$/\1/')
echo "Uyuni release $UYUNI_REL" > $RPM_BUILD_ROOT/%{_sysconfdir}/uyuni-release
if grep -F 'product_name' %{_datadir}/rhn/config-defaults/rhn.conf | grep 'SUSE Manager' >/dev/null; then
  echo "SUSE Manager release $SUMA_REL ($UYUNI_REL)" > $RPM_BUILD_ROOT/%{_sysconfdir}/susemanager-release
fi
install -d $RPM_BUILD_ROOT/%{_datadir}/spacewalk/setup/defaults.d
for i in ${RDBMS} ; do
    cat <<EOF >$RPM_BUILD_ROOT/%{_datadir}/spacewalk/setup/defaults.d/$i-backend.conf
# database backend to be used by spacewalk
db-backend = $i
EOF
done

%files common
%{_sysconfdir}/*-release
%if 0%{?suse_version}
%dir %{_datadir}/spacewalk
%dir %{_datadir}/spacewalk/setup
%dir %{_datadir}/spacewalk/setup/defaults.d
%endif

%files postgresql
%{_datadir}/spacewalk/setup/defaults.d/postgresql-backend.conf

%changelog
