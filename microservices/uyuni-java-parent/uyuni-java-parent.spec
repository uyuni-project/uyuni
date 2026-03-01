#
# spec file for package uyuni-java-parent
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

Name:           uyuni-java-parent
Version:        5.2.3
Release:        0
Summary:        Parent POM for all Uyuni Maven components
License:        GPL-2.0-only
Group:          Development/Libraries/Java
URL:            https://www.uyuni-project.org
#!CreateArchive: %{name}
Source0:        %{name}-%{version}.tar.gz
BuildRequires:  fdupes
BuildRequires:  java-devel >= 17
BuildRequires:  maven-local
BuildArch:      noarch

%description
Package that contains the parent POM used by all %{productprettyname} Maven components.

%prep
%setup -q

%build
%{mvn_build} -j -- --non-recursive

%install
%{mvn_install}

%files -f .mfiles
%license LICENSE

%changelog
