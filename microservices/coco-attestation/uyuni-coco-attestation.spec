#
# spec file for package uyuni-coco-attestation
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


Name:           uyuni-coco-attestation
Version:        5.1.2
Release:        0
Summary:        Uyuni utility for Confidential Computing Attestation
License:        GPL-2.0-only
Group:          System/Daemons
URL:            https://www.uyuni-project.org
Source0:        %{name}-%{version}.tar.gz
BuildRequires:  fdupes
BuildRequires:  java-devel >= 11
BuildRequires:  maven-local
BuildRequires:  mvn(com.mchange:c3p0)
BuildRequires:  mvn(com.mchange:mchange-commons-java)
BuildRequires:  mvn(org.apache.commons:commons-ognl)
BuildRequires:  mvn(org.apache.logging.log4j:log4j-api)
BuildRequires:  mvn(org.apache.logging.log4j:log4j-core)
BuildRequires:  mvn(org.javassist:javassist)
BuildRequires:  mvn(org.mybatis:mybatis)
BuildRequires:  mvn(org.postgresql:postgresql)
BuildRequires:  mvn(org.uyuni-project:uyuni-java-common)
BuildRequires:  mvn(org.uyuni-project:uyuni-java-parent:pom:)

%description
System daemon used by Uyuni to validate the results of confidential computing attestation.

%package core
Summary:        Uyuni utility for Confidential Computing Attestation
BuildArch:      noarch

%description core
System daemon used by Uyuni to validate the results of confidential computing attestation.

%ifarch x86_64
%package module-snpguest
Summary:        Confidential computing SNPGuest attestation module for Uyuni
Requires:       snpguest

%description module-snpguest
Module for the Uyuni Confidential Computing Attestation that uses SnpGuest.
%endif

%package module-secureboot
Summary:        Confidential computing SecureBoot attestation module for Uyuni

%description module-secureboot
Module for the Uyuni Confidential Computing Attestation for SecureBoot uses the output of mokutil.

%package        javadoc
Summary:        API documentation for %{name}
BuildArch:      noarch

%description    javadoc
Package containing the Javadoc API documentation for %{name}.

%prep
%setup -q

%ifnarch x86_64
# Disable the module snpguest as it requires x86_64
%pom_disable_module 'attestation-module-snpguest'
%endif

# Make sure there are not dependencies on the modules projects
%pom_remove_dep 'org.uyuni-project.coco-attestation.module:' attestation-core/pom.xml

# Shade is used only for developing convenience
%pom_remove_plugin -r :maven-shade-plugin

%{mvn_package} ':attestation-module-snpguest' module-snpguest

%{mvn_package} ':attestation-module-secureboot' module-secureboot

%build
%{mvn_build} -f

%install
%{mvn_install}

# Install required directories
install -d -m 755 %{buildroot}%{_sbindir}
install -d -m 755 %{buildroot}%{_datadir}/coco-attestation/
install -d -m 755 %{buildroot}%{_datadir}/coco-attestation/classes
install -d -m 755 %{buildroot}%{_datadir}/coco-attestation/conf
install -d -m 755 %{buildroot}%{_datadir}/coco-attestation/lib

# Required files
install -p -m 755 attestation-core/src/package/coco-attestation.sh %{buildroot}%{_sbindir}/coco-attestation
install -p -m 644 attestation-core/src/package/daemon.conf %{buildroot}%{_datadir}/coco-attestation/conf
install -p -m 644 attestation-core/src/package/log4j2.xml %{buildroot}%{_datadir}/coco-attestation/classes

# Create links for the jars
build-jar-repository -s -p %{buildroot}%{_datadir}/coco-attestation/lib uyuni-java-common/uyuni-common log4j/log4j-api log4j/log4j-core ongres-scram ongres-stringprep postgresql-jdbc apache-commons-ognl javassist mybatis mchange-commons c3p0

# Link all the attestation jars built and installed by maven
ln -s -f -r %{buildroot}%{_javadir}/uyuni-coco-attestation/*.jar %{buildroot}%{_datadir}/coco-attestation/lib

%ifarch x86_64
# Install snpguest certificates
cd attestation-module-snpguest/src/package/certs/
for FILE in $(find -name *.pem -type f -printf '%%P\n'); do
    echo $FILE
    install -D -p -m 644 $FILE %{buildroot}%{_datadir}/coco-attestation/certs/$FILE
done
cd -
%endif

%files core -f .mfiles
%defattr(-,root,root)
%dir %{_datadir}/coco-attestation/
%dir %{_datadir}/coco-attestation/conf/
%dir %{_datadir}/coco-attestation/lib/
%dir %{_datadir}/coco-attestation/classes/
%{_datadir}/coco-attestation/lib/*
%attr(755, root, root) %{_sbindir}/coco-attestation
%{_datadir}/coco-attestation/conf/daemon.conf
%{_datadir}/coco-attestation/classes/log4j2.xml
%license LICENSE

# Exclude all modules jars, will be part of their specific packages
%exclude %{_datadir}/coco-attestation/lib/attestation-module-*

%ifarch x86_64
%files module-snpguest -f .mfiles-module-snpguest
%defattr(-,root,root)
%dir %{_datadir}/coco-attestation/certs/
%{_datadir}/coco-attestation/lib/attestation-module-snpguest.jar
%{_datadir}/coco-attestation/certs/*
%license LICENSE
%endif

%files module-secureboot -f .mfiles-module-secureboot
%{_datadir}/coco-attestation/lib/attestation-module-secureboot.jar
%license LICENSE

%files javadoc -f .mfiles-javadoc
%license LICENSE

%changelog
