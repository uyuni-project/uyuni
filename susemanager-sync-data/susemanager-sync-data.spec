Name:           susemanager-sync-data
Version:        3.2.11
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
install -m 0644 channel_families.json %{buildroot}/usr/share/susemanager/scc/channel_families.json
install -m 0644 upgrade_paths.json    %{buildroot}/usr/share/susemanager/scc/upgrade_paths.json
install -m 0644 additional_products.json    %{buildroot}/usr/share/susemanager/scc/additional_products.json
install -m 0644 additional_repositories.json    %{buildroot}/usr/share/susemanager/scc/additional_repositories.json
install -m 0644 product_tree.json    %{buildroot}/usr/share/susemanager/scc/product_tree.json


%files
%defattr(-,root,root,-)
%dir /usr/share/susemanager
%dir /usr/share/susemanager/scc
/usr/share/susemanager/scc/channel_families.json
/usr/share/susemanager/scc/upgrade_paths.json
/usr/share/susemanager/scc/additional_products.json
/usr/share/susemanager/scc/additional_repositories.json
/usr/share/susemanager/scc/product_tree.json

%changelog

