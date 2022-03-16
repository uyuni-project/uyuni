#
# spec file for package uyuni-proxy-systemd-services 
#
# Copyright (c) 2022 SUSE LLC
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

%define SERVICES container-proxy-httpd container-proxy-salt-broker container-proxy-squid container-proxy-ssh container-proxy-tftpd pod-proxy-pod

Name:           uyuni-proxy-systemd-services 
Summary:        Uyuni proxy server systemd services containers
License:        GPL-2.0-only
Group:          Applications/Internet
Version:        4.3.1
Release:        1
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
Requires:       podman
BuildRequires:  systemd-rpm-macros

%description
This package contains systemd services to run the Uyuni proxy containers using podman.

%prep
%setup -q

%build

%install
for service in $(ls *.service); do
    install -D -m 644 $service %{buildroot}%{_unitdir}/$service
done

%check

%pre
%if !0%{?rhel}
for service in %{SERVICES}; do
    %service_add_pre ${service}.service
done
%endif

%post
for service in %{SERVICES}; do
    %if 0%{?rhel}
        %systemd_post ${service}.service
    %else
    %service_add_post ${service}.service
    %endif
    systemctl enable ${service}.service > /dev/null 2>&1 || :
    systemctl start ${service}.service > /dev/null 2>&1 || :
done

%preun
for service in %{SERVICES}; do
    %if 0%{?rhel}
        %systemd_preun ${service}.service
    %else
        %service_del_preun ${service}.service
    %endif
done

%postun
for service in %{SERVICES}; do
    %if 0%{?rhel}
        %systemd_postun ${service}.service
    %else
        %service_del_postun ${service}.service
    %endif
done

%files
%defattr(-,root,root)
%{_unitdir}/*.service


%changelog
