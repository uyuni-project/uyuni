#
# spec file for package uyuni-java-common
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

Name:           uyuni-java-common
Version:        5.2.0
Release:        0
Summary:        Common Java library for %{productprettyname} Java components
License:        GPL-2.0-only
Group:          Development/Libraries/Java
URL:            https://www.uyuni-project.org
#!CreateArchive: %{name}
Source0:        %{name}-%{version}.tar.gz
BuildRequires:  fdupes
BuildRequires:  java-devel >= 17
BuildRequires:  maven-local
BuildRequires:  mvn(com.mchange:c3p0)
BuildRequires:  mvn(org.apache.logging.log4j:log4j-api)
BuildRequires:  mvn(org.mybatis:mybatis)
BuildRequires:  mvn(org.uyuni-project:uyuni-java-parent:pom:)
BuildArch:      noarch

%description
A Java library containing basing utilities and functionalities shared among multiple %{productprettyname} Java components.

%package        javadoc
Summary:        API documentation for %{name}

%description    javadoc
Package containing the Javadoc %{summary}.

%prep
%setup -q

%build
%{mvn_build} -f

%install
%{mvn_install}

%files -f .mfiles
%license LICENSE

%files javadoc -f .mfiles-javadoc
%license LICENSE

%changelog
