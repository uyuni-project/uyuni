#
# spec file for package susemanager-proxy
#
# Copyright (c) 2018 SUSE LINUX GmbH, Nuernberg, Germany.
#
# All modifications and additions to the file contributed by third parties
# remain the property of their copyright owners, unless otherwise agreed
# upon. The license for this file, and modifications and additions to the
# file, is the same license as for the pristine package itself (unless the
# license for the pristine package is not an Open Source License, in which
# case the license is the MIT License). An "Open Source License" is a
# license that conforms to the Open Source Definition (Version 1.9)
# published by the Open Source Initiative.

# Please submit bugfixes or comments via http://bugs.opensuse.org/
#


Name:           susemanager-proxy
Version:        4.0.0
Release:        1%{?dist}
Summary:        SUSE Manager Proxy specific scripts
License:        GPL-2.0-only
Group:          Applications/System
URL:            http://www.suse.com
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch

%description
A collection of scripts for SUSE Manager Proxy initial
setup tasks.

%prep
%setup -q

%build

%install
mkdir -p %{buildroot}/%{_sysconfdir}/sysconfig/SuSEfirewall2.d/services
install -m 0644 suse-manager-proxy %{buildroot}/%{_sysconfdir}/sysconfig/SuSEfirewall2.d/services/

# YaST configuration
mkdir -p %{buildroot}/etc/YaST2
install -m 0644 firstboot-susemanager-proxy.xml %{buildroot}/etc/YaST2

%files
%defattr(-,root,root,-)
%doc license.txt
%dir /etc/YaST2
%config /etc/YaST2/firstboot-susemanager-proxy.xml
%config %{_sysconfdir}/sysconfig/SuSEfirewall2.d/services/suse-manager-proxy

%changelog
