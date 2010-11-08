Name:           susemanager
Version:        1.1.0
Release:        1%{?dist}
Summary:        SUSE Manager specific scripts
Group:          Applications/System
License:        GPLv2
URL:            http://www.novell.com
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch:      noarch
Requires:       dialog
Requires:       spacewalk-setup spacewalk-admin cobbler spacewalk-schema
Requires:       rsync
# needed for sqlplus
Requires:       oracle-xe-univ


%description
A collection of scripts for managing SUSE Manager's initial
setup tasks, re-installation, upgrades and managing.

%prep
%setup -q

%build

%install
mkdir -p %{buildroot}/%{_prefix}/lib/susemanager/bin/
install -m 0755 bin/*.sh %{buildroot}/%{_prefix}/lib/susemanager/bin/

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%doc doc/* Changes
%dir %{_prefix}/lib/susemanager
%dir %{_prefix}/lib/susemanager/bin/
%{_prefix}/lib/susemanager/bin/*

%changelog

