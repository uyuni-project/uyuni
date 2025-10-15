#
# spec file for package spacewalk
#
# Copyright (c) 2025 SUSE LLC
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

# The productprettyname macros is controlled in the prjconf. If not defined, we fallback here
%{!?productprettyname: %global productprettyname Uyuni}

Name:           spacewalk
Version:        5.2.0
Release:        0
Summary:        %{productprettyname} Systems Management Application
License:        GPL-2.0-only
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Applications/Internet
URL:            https://github.com/uyuni-project/uyuni
#!CreateArchive: %{name}
Source:         https://github.com/uyuni-project/uyuni/archive/%{name}-%{version}.tar.gz
BuildArch:      noarch

%description
%{productprettyname} is a systems management application that will
inventory, provision, update and control your Linux machines.

%package common
Summary:        %{productprettyname} Systems Management Application with postgresql database backend
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
%{productprettyname} is a systems management application that will
inventory, provision, update and control your Linux machines.

%prep
%setup -q

%build
#nothing to do here

%install
install -d %{buildroot}%{_sysconfdir}
install -d %{buildroot}%{_bindir}
%if 0%{?rhel}
ln -s %{_prefix}/pgsql-14/bin/initdb %{buildroot}%{_bindir}/initdb
%endif

%files common
%{!?_licensedir:%global license %doc}
%license LICENSE

%changelog
