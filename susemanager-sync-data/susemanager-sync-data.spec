#
# spec file for package susemanager-sync-data
#
# Copyright (c) 2025 SUSE LLC
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

# The productprettyname macros is controlled in the prjconf. If not defined, we fallback here
%{!?productprettyname: %global productprettyname Uyuni}
Name:           susemanager-sync-data
Version:        5.2.1
Release:        0
Summary:        %{productprettyname} specific scripts
License:        GPL-2.0-only
Group:          Productivity/Other
URL:            https://github.com/uyuni-project/uyuni
#!CreateArchive: %{name}
Source0:        %{name}-%{version}.tar.gz
Requires:       spacewalk-java-lib >= 2.5.59.7
BuildArch:      noarch

%description
This package contains data files with information used to channel syncing

%prep
%setup -q

%build

%install
mkdir -p %{buildroot}%{_datadir}/susemanager/scc
mkdir -p %{buildroot}%{_datadir}/susemanager/oval
install -m 0644 channel_families.json %{buildroot}%{_datadir}/susemanager/scc/channel_families.json
install -m 0644 additional_products.json    %{buildroot}%{_datadir}/susemanager/scc/additional_products.json
install -m 0644 additional_repositories.json    %{buildroot}%{_datadir}/susemanager/scc/additional_repositories.json
install -m 0644 oval.config.json    %{buildroot}%{_datadir}/susemanager/oval/oval.config.json

%files
%dir %{_datadir}/susemanager
%dir %{_datadir}/susemanager/scc
%dir %{_datadir}/susemanager/oval
%{_datadir}/susemanager/scc/channel_families.json
%{_datadir}/susemanager/scc/additional_products.json
%{_datadir}/susemanager/scc/additional_repositories.json
%{_datadir}/susemanager/oval/oval.config.json

%changelog
