#
# spec file for package uyuni-java-common
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

Name:           uyuni-java-common
Version:        5.0.5
Release:        0
Summary:        Common Java library for Uyuni Java components
License:        GPL-2.0-only
Group:          Development/Libraries/Java
URL:            https://www.uyuni-project.org
Source0:        %{name}-%{version}.tar.gz
BuildRequires:  fdupes
BuildRequires:  java-devel >= 11
BuildRequires:  maven-local
BuildRequires:  mvn(org.uyuni-project:uyuni-java-parent:pom:)
BuildRequires:  mvn(org.apache.logging.log4j:log4j-api)
BuildRequires:  mvn(org.mybatis:mybatis)
BuildRequires:  mvn(com.mchange:c3p0)

BuildArch:      noarch

%description
A Java library containing basing utilities and functionalities shared among multiple Uyuni Java components.

%package        javadoc
Summary:        API documentation for %{name}

%description    javadoc
%{summary}.

%prep
%setup -q

%build
%{mvn_build} -f

%install
%mvn_install

%files -f .mfiles
%defattr(-,root,root)
%license LICENSE

%files javadoc -f .mfiles-javadoc
%license LICENSE

%changelog
