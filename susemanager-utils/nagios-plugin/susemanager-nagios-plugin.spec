Name:           susemanager-nagios-plugin
Version:        1.0.0
Release:        1%{?dist}
Summary:        Nagios plugins for SUSE Manager
Group:          System/Monitoring
License:        GPLv2
URL:            http://www.novell.com
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildRequires:  nagios-rpm-macros
BuildArch:      noarch

%description
Nagios plugins specific for SUSE Manager.

%prep
%setup -q

%build

%install
install -D -m 0755 check_suma_patches %{buildroot}/%{nagios_plugindir}/check_suma_patches

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%dir %{nagios_libdir}

%changelog
