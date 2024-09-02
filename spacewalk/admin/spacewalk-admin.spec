#
# spec file for package spacewalk-admin
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


Name:           spacewalk-admin
Version:        5.1.0
Release:        0
Summary:        Various utility scripts and data files for Spacewalk installations
License:        GPL-2.0-only
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Applications/Internet
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/uyuni-project/uyuni/archive/%{name}-%{version}.tar.gz
Requires:       lsof
Requires:       procps
Requires:       python3
Requires:       python3-websockify
Requires:       spacewalk-base
Requires:       perl(MIME::Base64)
BuildRequires:  /usr/bin/pod2man
BuildRequires:  make
BuildRequires:  systemd
BuildRequires:  spacewalk-config
BuildRequires:  uyuni-base-common
Requires(pre):  uyuni-base-common
Requires:       susemanager-schema-utility
Requires:       uyuni-setup-reportdb
BuildArch:      noarch

%description
Various utility scripts and data files for Spacewalk installations.

%prep
%setup -q

%build

%install

%if 0%{?rhel}
sed -i 's/apache2.service/httpd.service/g' spacewalk.target
sed -i 's/apache2.service/httpd.service/g' spacewalk-wait-for-tomcat.service
sed -i 's/apache2.service/httpd.service/g' uyuni-check-database.service
%endif

make -f Makefile.admin install PREFIX=%{buildroot}

mkdir -p %{buildroot}%{_mandir}/man8/
%{_bindir}/pod2man --section=8 rhn-config-schema.pl > %{buildroot}%{_mandir}/man8/rhn-config-schema.pl.8
%{_bindir}/pod2man --section=8 man/spacewalk-service.pod > %{buildroot}%{_mandir}/man8/spacewalk-service.8
%{_bindir}/pod2man --section=8 man/rhn-sat-restart-silent.pod > %{buildroot}%{_mandir}/man8/rhn-sat-restart-silent.8
%{_bindir}/pod2man --section=8 rhn-config-satellite.pl > %{buildroot}%{_mandir}/man8/rhn-config-satellite.pl.8
%{_bindir}/pod2man --section=8 man/rhn-deploy-ca-cert.pl.pod > %{buildroot}%{_mandir}/man8/rhn-deploy-ca-cert.pl.8
%{_bindir}/pod2man --section=8 man/rhn-install-ssl-cert.pl.pod > %{buildroot}%{_mandir}/man8/rhn-install-ssl-cert.pl.8
chmod 0644 %{buildroot}%{_mandir}/man8/*.8*

%post
if [ -x %{_bindir}/systemctl ]; then
    %{_bindir}/systemctl daemon-reload || :
fi

%files
%license LICENSE
%{_sbindir}/spacewalk-startup-helper
%{_sbindir}/spacewalk-service
%{_sbindir}/uyuni-update-config
%{_sbindir}/import-suma-build-keys
%{_bindir}/rhn-config-satellite.pl
%{_bindir}/rhn-config-schema.pl
%{_bindir}/rhn-deploy-ca-cert.pl
%{_bindir}/rhn-install-ssl-cert.pl
%{_bindir}/salt-secrets-config.py
%{_sbindir}/rhn-sat-restart-silent
%{_sbindir}/mgr-monitoring-ctl
%{_mandir}/man8/rhn-config-schema.pl.8*
%{_mandir}/man8/spacewalk-service.8*
%{_mandir}/man8/rhn-sat-restart-silent.8*
%{_mandir}/man8/rhn-config-satellite.pl.8*
%{_mandir}/man8/rhn-deploy-ca-cert.pl.8*
%{_mandir}/man8/rhn-install-ssl-cert.pl.8*
%{_unitdir}/spacewalk.target
%{_unitdir}/spacewalk-wait-for-tomcat.service
%{_unitdir}/spacewalk-wait-for-salt.service
%{_unitdir}/spacewalk-wait-for-taskomatic.service
%{_unitdir}/salt-secrets-config.service
%{_unitdir}/cobbler-refresh-mkloaders.service
%{_unitdir}/mgr-websockify.service
%{_unitdir}/uyuni-check-database.service
%{_unitdir}/uyuni-update-config.service
%{_unitdir}/*.service.d

%changelog
