Name:           susemanager-proxy
Version:        3.2.2
Release:        1%{?dist}
Summary:        SUSE Manager Proxy specific scripts
Group:          Applications/System
License:        GPLv2
URL:            http://www.suse.com
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
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

