#
# spec file for package susemanager-sync-data
#
# Copyright (c) 2019 SUSE LINUX GmbH, Nuernberg, Germany.
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


Name:           susemanager-sync-data
Version:        4.0.8
Release:        1%{?dist}
Summary:        SUSE Manager specific scripts
License:        GPL-2.0-only
Group:          Productivity/Other
Url:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
Requires:       spacewalk-java-lib >= 2.5.59.7
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
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
