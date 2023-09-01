#
# spec file for package uyuni-server-systemd-services
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

Name:           uyuni-server-systemd-services
Summary:        Uyuni Server systemd services containers
License:        GPL-2.0-only
Group:          Applications/Internet
Version:        4.4.1
Release:        1
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}-1.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
Requires:       podman
%if 0%{?suse_version}
Requires(post): %fillup_prereq
%endif
BuildRequires:  systemd-rpm-macros

%description
This package contains systemd services to run the Uyuni server containers using podman.

%prep
%setup -q

%build

%install
install -d -m 755 %{buildroot}/%{_sysconfdir}/uyuni/server
install -d -m 755 %{buildroot}%{_sbindir}

#TODO currently removed but it can be useful in future
#%if "%{?susemanager_container_images_path}" != ""
#sed 's|^NAMESPACE=.*$|NAMESPACE=%{susemanager_container_images_path}|' -i uyuni-server-services.config
#%endif

%if !0%{?is_opensuse}
PRODUCT_VERSION=$(echo %{version} | sed 's/^\([0-9]\+\.[0-9]\+\).*$/\1/')
%endif
%if 0%{?rhel}
install -D -m 644 uyuni-server-services.config %{buildroot}%{_sysconfdir}/sysconfig/uyuni-server-systemd-services.config
%else
install -D -m 644 uyuni-server-services.config %{buildroot}%{_fillupdir}/sysconfig.%{name}
%endif

install -D -m 644 uyuni-server.service %{buildroot}%{_unitdir}/uyuni-server.service
ln -s /usr/sbin/service %{buildroot}%{_sbindir}/rcuyuni-server

install -m 755 uyuni-server.sh %{buildroot}%{_sbindir}/uyuni-server.sh

%check

%pre
%if !0%{?rhel}
    %service_add_pre uyuni-server.service
%endif

%post
%if 0%{?suse_version}
%fillup_only
%endif

%if 0%{?rhel}
    %systemd_post uyuni-server.service
%else
    %service_add_post uyuni-server
%endif

%preun
%if 0%{?rhel}
    %systemd_preun uyuni-server.service
%else
    %service_del_preun uyuni-server
%endif

%postun
%if 0%{?rhel}
    %systemd_postun uyuni-server.service
%else
    %service_del_postun uyuni-server
%endif

%files
%defattr(-,root,root)
%doc README.md
%{_unitdir}/*.service
%{_sbindir}/rcuyuni-*
%if 0%{?rhel}
%{_sysconfdir}/sysconfig/uyuni-server-systemd-services.config
%else
%{_fillupdir}/sysconfig.%{name}
%endif
%{_sysconfdir}/uyuni
%{_sbindir}/uyuni-server.sh

%changelog
