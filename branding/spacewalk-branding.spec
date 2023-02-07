#
# spec file for package spacewalk-branding
#
# Copyright (c) 2022 SUSE LLC
# Copyright (c) 2008-2018 Red Hat, Inc.
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


%global debug_package %{nil}

%if 0%{?fedora} || 0%{?rhel} >= 7
%global tomcat_path %{_var}/lib/tomcat
%global wwwdocroot %{_var}/www/html
%else
%if 0%{?suse_version}
%global tomcat_path /srv/tomcat
%global wwwdocroot /srv/www/htdocs
%else
%global tomcat_path %{_var}/lib/tomcat6
%global wwwdocroot %{_var}/www/html
%endif
%endif

Name:           spacewalk-branding
Version:        4.4.1
Release:        1
Summary:        Spacewalk branding data
License:        GPL-2.0-only AND OFL-1.1
Group:          Applications/Internet

URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#BuildArch:  noarch
BuildRequires:  java-devel >= 11
Requires:       httpd
%if 0%{?suse_version}
Requires(pre):  tomcat
BuildRequires:  apache2
Requires:       susemanager-advanced-topics_en-pdf
Requires:       susemanager-best-practices_en-pdf
Requires:       susemanager-docs_en
Requires:       susemanager-getting-started_en-pdf
Requires:       susemanager-reference_en-pdf
%endif
Requires:       susemanager-frontend-libs

%description
Spacewalk specific branding, CSS, and images.

%package devel
Requires:       %{name} = %{version}-%{release}
Summary:        Spacewalk LESS source files for development use
Group:          Applications/Internet

%description devel
This package contains LESS source files corresponding to Spacewalk's
CSS files.

%prep
%setup -q

%build

javac java/code/src/com/redhat/rhn/branding/strings/StringPackage.java
rm -f java/code/src/com/redhat/rhn/branding/strings/StringPackage.java
jar -cf java-branding.jar -C java/code/src com

%install
install -d -m 755 %{buildroot}%{wwwdocroot}
install -d -m 755 %{buildroot}%{_datadir}/spacewalk
install -d -m 755 %{buildroot}%{_datadir}/spacewalk/web
install -d -m 755 %{buildroot}%{_datadir}/rhn/lib/
install -d -m 755 %{buildroot}%{tomcat_path}/webapps/rhn/WEB-INF/lib/
install -d -m 755 %{buildroot}/%{_sysconfdir}/rhn
cp -pR java-branding.jar %{buildroot}%{_datadir}/rhn/lib/
ln -s %{_datadir}/rhn/lib/java-branding.jar %{buildroot}%{tomcat_path}/webapps/rhn/WEB-INF/lib/java-branding.jar

%files
%{_datadir}/spacewalk/
%{_datadir}/rhn/lib/java-branding.jar
%{tomcat_path}/webapps/rhn/WEB-INF/lib/java-branding.jar
%license LICENSE
%if 0%{?suse_version}
%attr(775,tomcat,tomcat) %dir %{tomcat_path}/webapps/rhn
%attr(775,tomcat,tomcat) %dir %{tomcat_path}/webapps/rhn/WEB-INF
%attr(775,tomcat,tomcat) %dir %{tomcat_path}/webapps/rhn/WEB-INF/lib/
%dir %{_prefix}/share/rhn
%dir %{_prefix}/share/rhn/lib
%endif

%changelog
