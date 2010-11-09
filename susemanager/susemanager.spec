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
PreReq:         %insserv_prereq %fillup_prereq
Requires:       dialog
Requires:       spacewalk-setup spacewalk-admin cobbler spacewalk-schema
Requires:       rsync less coreutils
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
mkdir -p %{buildroot}/%{_datadir}/doc/licenses
install -m 0644 usr/share/doc/licenses/SUSE_MANAGER_LICENSE %{buildroot}/%{_datadir}/doc/licenses/
mkdir -p %{buildroot}/%{_sysconfdir}/init.d
install -m 0755 etc/init.d/spacewalk_firstboot %{buildroot}/%{_sysconfdir}/init.d

%clean
rm -rf %{buildroot}

%post
%{fillup_and_insserv -y spacewalk_firstboot}

%files
%defattr(-,root,root,-)
%doc doc/* Changes
%dir %{_prefix}/lib/susemanager
%dir %{_prefix}/lib/susemanager/bin/
%dir %{_datadir}/doc/licenses
%{_prefix}/lib/susemanager/bin/*
%attr(0755,root,root) %{_sysconfdir}/init.d/spacewalk_firstboot
%{_datadir}/doc/licenses/SUSE_MANAGER_LICENSE

%changelog

