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

%define SERVICES uyuni-proxy-httpd uyuni-proxy-salt-broker uyuni-proxy-squid uyuni-proxy-ssh uyuni-proxy-tftpd uyuni-proxy-pod

Name:           uyuni-proxy-systemd-services
Summary:        Uyuni proxy server systemd services containers
License:        GPL-2.0-only
Group:          Applications/Internet
Version:        4.3.12
Release:        0
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}-0.tar.gz
Source1:        https://raw.githubusercontent.com/uyuni-project/uyuni/%{name}-%{version}-0/containers/proxy-systemd-services/%{name}-rpmlintrc
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
Requires:       podman
%if 0%{?suse_version}
Requires(post): %fillup_prereq
%endif
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

%if "%{?susemanager_container_images_path}" != ""
sed 's|^NAMESPACE=.*$|NAMESPACE=%{susemanager_container_images_path}|' -i uyuni-proxy-services.config
%endif

%if !0%{?is_opensuse}
PRODUCT_VERSION=$(echo %{version} | sed 's/^\([0-9]\+\.[0-9]\+\).*$/\1/')
%endif
%if 0%{?rhel}
install -D -m 644 uyuni-proxy-services.config %{buildroot}%{_sysconfdir}/sysconfig/uyuni-proxy-systemd-services.config
%else
install -D -m 644 uyuni-proxy-services.config %{buildroot}%{_fillupdir}/sysconfig.%{name}
%endif

for service in %{SERVICES}; do
    install -D -m 644 ${service}.service %{buildroot}%{_unitdir}/${service}.service
    ln -s /usr/sbin/service %{buildroot}%{_sbindir}/rc${service}
done

install -m 755 uyuni-proxy.sh %{buildroot}%{_sbindir}/uyuni-proxy.sh

%check

%pre
%if !0%{?rhel}
%service_add_pre %{SERVICES}
%endif

%post
%if 0%{?suse_version}
%fillup_only
%endif

%if 0%{?rhel}
for service in %{SERVICES}; do
    %systemd_post ${service}.service
done
%else
%service_add_post %{SERVICES}
%endif

%preun
%if 0%{?rhel}
for service in %{SERVICES}; do
    %systemd_preun ${service}.service
done
%else
%service_del_preun %{SERVICES}
%endif

%postun
%if 0%{?rhel}
for service in %{SERVICES}; do
    %systemd_postun ${service}.service
done
%else
%service_del_postun %{SERVICES}
%endif

%files
%defattr(-,root,root)
%doc README.md
%{_unitdir}/*.service
%{_sbindir}/rcuyuni-*
%if 0%{?rhel}
%{_sysconfdir}/sysconfig/uyuni-proxy-systemd-services.config
%else
%{_fillupdir}/sysconfig.%{name}
%endif
%{_sysconfdir}/uyuni
%{_localstatedir}/lib/uyuni
%{_sbindir}/uyuni-proxy.sh

%changelog
