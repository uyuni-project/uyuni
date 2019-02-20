Name:           susemanager-sync-data
Version:        3.1.18
Release:        1%{?dist}
Summary:        SUSE Manager specific scripts
Group:          Productivity/Other
License:        GPLv2
URL:            http://www.suse.com
Source0:        %{name}-%{version}.tar.gz
Requires:       spacewalk-java-lib >= 2.5.59.7
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch:      noarch

%description 
This package contains data files with information used to channel syncing

%prep
%setup -q

%build

%install

mkdir -p %{buildroot}/usr/share/susemanager/scc
install -m 0644 channel_families.xml %{buildroot}/usr/share/susemanager/scc/channel_families.xml
install -m 0644 channels.xml         %{buildroot}/usr/share/susemanager/scc/channels.xml
install -m 0644 upgrade_paths.xml    %{buildroot}/usr/share/susemanager/scc/upgrade_paths.xml

%clean
rm -rf %{buildroot}


%files
%defattr(-,root,root,-)
%dir /usr/share/susemanager
%dir /usr/share/susemanager/scc
/usr/share/susemanager/scc/channel_families.xml
/usr/share/susemanager/scc/channels.xml
/usr/share/susemanager/scc/upgrade_paths.xml

%changelog

