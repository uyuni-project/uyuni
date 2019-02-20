#
# spec file for package spacewalk-branding
#
# Copyright (c) 2018 SUSE LINUX GmbH, Nuernberg, Germany.
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

# Please submit bugfixes or comments via http://bugs.opensuse.org/
#


%if  0%{?rhel} && 0%{?rhel} < 6
%global tomcat_path %{_var}/lib/tomcat5
%global wwwdocroot %{_var}/www/html
%else
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
%endif

Name:           spacewalk-branding
Version:        4.0.5
Release:        1%{?dist}
Summary:        Spacewalk branding data
License:        GPL-2.0-only
Group:          Applications/Internet

URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
#BuildArch:  noarch
BuildRequires:  java-devel >= 11
BuildRequires:  nodejs
BuildRequires:  nodejs-less
Requires:       httpd
%if 0%{?suse_version}
Requires(pre): tomcat
BuildRequires:  apache2
BuildRequires:  susemanager-frontend-libs-devel
Requires:       susemanager-advanced-topics_en-pdf
Requires:       susemanager-best-practices_en-pdf
Requires:       susemanager-docs_en
Requires:       susemanager-getting-started_en-pdf
Requires:       susemanager-reference_en-pdf
Requires:       susemanager(bootstrap-datepicker)
Requires:       susemanager(font-awesome) = 4.4.0
Requires:       susemanager(jquery-timepicker) = 1.11.14
Requires:       susemanager(jquery-ui)
Requires:       susemanager(momentjs)
Requires:       susemanager(pwstrength-bootstrap)
Requires:       susemanager(roboto) = 1.2
Requires:       susemanager(select2)
Requires:       susemanager(select2-bootstrap-css)
Requires:       susemanager(twitter-bootstrap-js)
%else
BuildRequires:  patternfly1
Requires:       bootstrap <= 3.0.0
Requires:       bootstrap-datepicker
Requires:       font-awesome >= 4.0.0
Requires:       jquery-timepicker >= 1.3.2
Requires:       jquery-ui
Requires:       momentjs
Requires:       patternfly1
Requires:       pwstrength-bootstrap
Requires:       roboto >= 1.2
Requires:       select2
Requires:       select2-bootstrap-css
%endif

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

# Compile less into css
ln -s /srv/www/htdocs/css/bootstrap css/bootstrap
ln -s /srv/www/htdocs/css/patternfly1 css/patternfly1
lessc css/spacewalk.less > css/spacewalk.css
rm -f css/bootstrap
rm -f css/patternfly1

%install
install -d -m 755 %{buildroot}%{wwwdocroot}
install -d -m 755 %{buildroot}%{wwwdocroot}/css
install -d -m 755 %{buildroot}%{_datadir}/spacewalk
install -d -m 755 %{buildroot}%{_datadir}/spacewalk/web
install -d -m 755 %{buildroot}%{_datadir}/rhn/lib/
install -d -m 755 %{buildroot}%{tomcat_path}/webapps/rhn/WEB-INF/lib/
install -d -m 755 %{buildroot}/%{_sysconfdir}/rhn
install -d -m 755 %{buildroot}/%{_prefix}/share/rhn/config-defaults
cp -pR css/* %{buildroot}/%{wwwdocroot}/css
cp -pR fonts %{buildroot}/%{wwwdocroot}/
cp -pR img %{buildroot}/%{wwwdocroot}/
# Appplication expects two favicon's for some reason, copy it so there's just
# one in source:
cp -p img/favicon.ico %{buildroot}/%{wwwdocroot}/
cp -pR java-branding.jar %{buildroot}%{_datadir}/rhn/lib/
ln -s %{_datadir}/rhn/lib/java-branding.jar %{buildroot}%{tomcat_path}/webapps/rhn/WEB-INF/lib/java-branding.jar

%if  0%{?suse_version}
cat > %{buildroot}/%{_prefix}/share/rhn/config-defaults/rhn_docs.conf <<-ENDOFCONFIG
docs.getting_started_guide=/rhn/help/getting-started/index.jsp
docs.reference_guide=/rhn/help/reference/index.jsp
docs.best_practices_guide=/rhn/help/best-practices/index.jsp
docs.advanced_topics_guide=/rhn/help/advanced-topics/index.jsp
docs.release_notes=/rhn/help/release-notes/manager/en-US/index.jsp
docs.proxy_release_notes=http://www.novell.com/linux/releasenotes/%{_arch}/SUSE-MANAGER/3.0/
ENDOFCONFIG
%else
cp -p conf/rhn_docs.conf %{buildroot}/%{_prefix}/share/rhn/config-defaults/rhn_docs.conf
ln -s %{_datadir}/patternfly1/resources/fonts/* %{buildroot}%{wwwdocroot}/fonts/
%endif

%files
%dir %{wwwdocroot}/css
%{wwwdocroot}/css/*.css
%dir %{wwwdocroot}/fonts
%{wwwdocroot}/fonts/*
%dir /%{wwwdocroot}/img
%{wwwdocroot}/img/*
%{wwwdocroot}/favicon.ico
%{_datadir}/spacewalk/
%{_datadir}/rhn/lib/java-branding.jar
%{tomcat_path}/webapps/rhn/WEB-INF/lib/java-branding.jar
%{_prefix}/share/rhn/config-defaults/rhn_docs.conf
%doc LICENSE
%if 0%{?suse_version}
%attr(775,tomcat,tomcat) %dir %{tomcat_path}/webapps/rhn
%attr(775,tomcat,tomcat) %dir %{tomcat_path}/webapps/rhn/WEB-INF
%attr(775,tomcat,tomcat) %dir %{tomcat_path}/webapps/rhn/WEB-INF/lib/
%dir %{_prefix}/share/rhn
%dir %{_prefix}/share/rhn/lib
%attr(755,root,www) %dir %{_prefix}/share/rhn/config-defaults
%endif

%files devel
%defattr(-,root,root)
%{wwwdocroot}/css/*.less

%changelog
