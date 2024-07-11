#
# spec file for package uyuni-payg-timer
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


%global debug_package %{nil}

#Compat macro for new _fillupdir macro introduced in Nov 2017
%if ! %{defined _fillupdir}
  %define _fillupdir /var/adm/fillup-templates
%endif

Name:           uyuni-payg-timer
Version:        5.1.0
Release:        0
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
Source1:        uyuni-payg-timer-rpmlintrc
Summary:        Uyuni PAYG Timer Package
License:        GPL-2.0-only
Group:          System/Fhs
BuildRequires:  systemd-rpm-macros
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
Requires:       mgradm
%systemd_requires

%description
Uyuni is a systems management application that will
inventory, provision, update and control your Linux machines.
This package provide a timer for Cloud PAYG usage.

%prep
%setup -q

%build
# nothing to do here

%install
mkdir -p %{buildroot}/%{_unitdir}
mkdir -p %{buildroot}/%{_sbindir}
install -m 644 uyuni-payg-timer.timer  %{buildroot}/%{_unitdir}/
install -m 644 uyuni-payg-timer.service  %{buildroot}/%{_unitdir}/
install -m 755 uyuni-payg-extract-data.py %{buildroot}/%{_sbindir}/uyuni-payg-extract-data

%files
%defattr(-,root,root)
%{!?_licensedir:%global license %doc}
%license LICENSE
%{_sbindir}/uyuni-payg-extract-data
%{_unitdir}/uyuni-payg-timer.timer
%{_unitdir}/uyuni-payg-timer.service

%pre
%service_add_pre uyuni-payg-timer.service uyuni-payg-timer.timer

%post
%service_add_post uyuni-payg-timer.service uyuni-payg-timer.timer

%preun
%service_del_preun uyuni-payg-timer.service uyuni-payg-timer.timer

%postun
%service_del_postun uyuni-payg-timer.service uyuni-payg-timer.timer

%posttrans
if [ -f %{_unitdir}/uyuni-payg-timer.timer ]; then
    /usr/bin/systemctl --quiet enable uyuni-payg-timer.timer 2>&1 ||:
fi

%changelog
