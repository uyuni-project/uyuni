Name:           susemanager-nagios-plugin
Version:        3.2.2
Release:        1%{?dist}
Summary:        Nagios plugins for SUSE Manager
Group:          System/Monitoring
License:        GPLv2
URL:            http://www.suse.com
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch:      noarch

%description
Nagios plugins specific for SUSE Manager.

%prep
%setup -q

%build

%install
install -D -m 0755 check_suma_patches %{buildroot}/usr/lib/nagios/plugins/check_suma_patches
install -D -m 0755 check_suma_lastevent %{buildroot}/usr/lib/nagios/plugins/check_suma_lastevent
install -D -m 0755 check_suma_common.py %{buildroot}/usr/lib/nagios/plugins/check_suma_common.py

%files
%defattr(-,root,root,-)
%dir /usr/lib/nagios
%dir /usr/lib/nagios/plugins
/usr/lib/nagios/plugins/check_suma_patches
/usr/lib/nagios/plugins/check_suma_lastevent
/usr/lib/nagios/plugins/check_suma_common.py

%changelog
