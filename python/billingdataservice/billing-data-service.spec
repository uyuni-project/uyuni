#
# spec file for package billing-data-service
#
# Copyright (c) 2024 SUSE LLC
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


Name:           billing-data-service
Version:        5.0.2
Release:        1
Summary:        Server to request billing information
License:        GPL-2.0-only
Group:          System/Daemons
URL:            https://github.com/uyuni-project/uyuni
Source:         %{name}-%{version}.tar.gz
BuildArch:      noarch
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
Requires:       apache2
Recommends:     csp-billing-adapter-service
Requires:       python3-Flask
Requires:       spacewalk-backend-sql
Requires:       spacewalk-taskomatic
Requires:       tomcat

%description
Server to provide PAYG billing information in public clouds

%prep
%setup -q

%build

%install
mkdir -p %{buildroot}%{_sysconfdir}/sysconfig
mkdir -p %{buildroot}%{_sbindir}
mkdir -p %{buildroot}%{_unitdir}
mkdir -p %{buildroot}%{_fillupdir}
mkdir -p %{buildroot}/srv/billing-data-service

install -m 0755 billing-data-service %{buildroot}/srv/billing-data-service/billing-data-service
install -m 0644 billingdataservice.py %{buildroot}/srv/billing-data-service/billingdataservice.py
install -m 0644 billing-data-service.sysconfig %{buildroot}%{_fillupdir}/sysconfig.%{name}
install -m 0644 billing-data-service.service %{buildroot}%{_unitdir}/billing-data-service.service
ln -sf service %{buildroot}%{_sbindir}/rcbilling-data-service

# when you shutdown billing-data-service, systemd shutdown also these
SYSTEMD_OVERRIDE_SERVICES="tomcat.service apache2.service taskomatic.service"
for service in ${SYSTEMD_OVERRIDE_SERVICES}; do
    mkdir -p %{buildroot}%{_unitdir}/${service}.d
    install -m 0644 payg-service-override.conf %{buildroot}%{_unitdir}/${service}.d/payg-override.conf
done

%pre
%service_add_pre billing-data-service.service

%post
%fillup_only
%service_add_post billing-data-service.service

%preun
%service_del_preun billing-data-service.service

%postun
%service_del_postun billing-data-service.service

%posttrans
if [ -f %{_unitdir}/billing-data-service.service ]; then
    /usr/bin/systemctl --quiet enable billing-data-service.service 2>&1 ||:
fi

%files
%license LICENSE
%dir %{_sysconfdir}/sysconfig
%dir /srv/billing-data-service
%{_sbindir}/rcbilling-data-service
%attr(0755, root, root) /srv/billing-data-service/billing-data-service
/srv/billing-data-service/billingdataservice.py
%{_unitdir}/*
%{_fillupdir}/sysconfig.%{name}

%changelog
