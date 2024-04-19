#
# spec file for package uyuni-java-parent
#
# Copyright (c) 2024 SUSE LLC
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

Name:           uyuni-java-parent
Version:        5.0.3
Release:        0
Summary:        Parent POM for all Uyuni Maven components
License:        GPL-2.0-only
Group:          Development/Libraries/Java
URL:            https://www.uyuni-project.org
Source0:        %{name}-%{version}.tar.gz
BuildRequires:  fdupes
BuildRequires:  java-devel >= 11
BuildRequires:  maven-local
BuildArch:      noarch

%description
Package that contains the parent POM used by all Uyuni Maven components.

%prep
%setup -q

%build
%{mvn_build} -j

%install
%mvn_install

%files -f .mfiles
%defattr(-,root,root)
%license LICENSE

%changelog
