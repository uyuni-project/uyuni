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

%define SERVICES container-proxy-httpd container-proxy-salt-broker container-proxy-squid container-proxy-ssh container-proxy-tftpd proxy-pod

Name:           uyuni-proxy-systemd-services
Summary:        Uyuni proxy server systemd services containers
License:        GPL-2.0-only
Group:          Applications/Internet
Version:        4.3.0
Release:        1
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}-1.tar.gz
Source1:        %{name}-rpmlintrc
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
Requires:       podman
Requires(post): %{fillup_prereq}
BuildRequires:  systemd-rpm-macros

%description
This package contains systemd services to run the Uyuni proxy containers using podman.

%prep
%setup -q

%build

%install
install -d -m 755 %{buildroot}/%{_sysconfdir}/uyuni/proxy
install -d -m 755 %{buildroot}/%{_localstatedir}/lib/uyuni/proxy-squid-cache
install -d -m 755 %{buildroot}/%{_localstatedir}/lib/uyuni/proxy-rhn-cache
install -d -m 755 %{buildroot}/%{_localstatedir}/lib/uyuni/proxy-tftpboot
install -d -m 755 %{buildroot}%{_sbindir}

%if 0%{?sle_version}
sed 's/^NAMESPACE=.*$/NAMESPACE=registry.suse.com\/suse\/manager\/4.3/' -i uyuni-container-proxy-services.config
%endif
install -D -m 644 uyuni-container-proxy-services.config %{buildroot}%{_fillupdir}/sysconfig.%{name}

for service in %{SERVICES}; do
    install -D -m 644 uyuni-${service}.service %{buildroot}%{_unitdir}/uyuni-${service}.service
    ln -s /usr/sbin/service %{buildroot}%{_sbindir}/rcuyuni-${service}
done

%check

%pre
%if !0%{?rhel}
for service in %{SERVICES}; do
    %service_add_pre uyuni-${service}.service
done
%endif

%post
%fillup_only
for service in %{SERVICES}; do
    %if 0%{?rhel}
        %systemd_post uyuni-${service}.service
    %else
    %service_add_post uyuni-${service}.service
    %endif
done

%preun
for service in %{SERVICES}; do
    %if 0%{?rhel}
        %systemd_preun uyuni-${service}.service
    %else
        %service_del_preun uyuni-${service}.service
    %endif
done

%postun
for service in %{SERVICES}; do
    %if 0%{?rhel}
        %systemd_postun uyuni-${service}.service
    %else
        %service_del_postun uyuni-${service}.service
    %endif
done

%files
%defattr(-,root,root)
%{_unitdir}/*.service
%{_sbindir}/rcuyuni-*
%{_fillupdir}/sysconfig.%{name}
%{_sysconfdir}/uyuni
%{_localstatedir}/lib/uyuni


%changelog
