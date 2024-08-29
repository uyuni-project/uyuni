#
# spec file for package spacewalk-branding
#
# Copyright (c) 2024 SUSE LLC
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
%global susemanager_shared_path  %{_datadir}/susemanager
%global wwwroot %{susemanager_shared_path}/www
%global tomcat_path %{wwwroot}/tomcat
%global wwwdocroot %{wwwroot}/htdocs
Name:           spacewalk-branding
Version:        5.1.0
Release:        0
Summary:        Spacewalk branding data
License:        GPL-2.0-only AND OFL-1.1
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Applications/Internet
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRequires:  java-devel >= 11
Requires:       httpd
Requires:       susemanager-advanced-topics_en-pdf
Requires:       susemanager-best-practices_en-pdf
Requires:       susemanager-docs_en
Requires:       susemanager-frontend-libs
Requires:       susemanager-getting-started_en-pdf
Requires:       susemanager-reference_en-pdf
Requires(pre):  tomcat
#BuildArch:  noarch
%if 0%{?suse_version}
BuildRequires:  apache2
%endif

%description
Spacewalk specific branding, CSS, and images.

%package devel
Summary:        Spacewalk LESS source files for development use
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Applications/Internet
Requires:       %{name} = %{version}-%{release}

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
%dir %{susemanager_shared_path}
%dir %{wwwroot}
%dir %{wwwdocroot}
%attr(775,tomcat,tomcat) %dir %{tomcat_path}
%attr(775,tomcat,tomcat) %dir %{tomcat_path}/webapps
%attr(775,tomcat,tomcat) %dir %{tomcat_path}/webapps/rhn
%attr(775,tomcat,tomcat) %dir %{tomcat_path}/webapps/rhn/WEB-INF
%attr(775,tomcat,tomcat) %dir %{tomcat_path}/webapps/rhn/WEB-INF/lib/
%dir %{_datadir}/rhn
%dir %{_datadir}/rhn/lib

%changelog
