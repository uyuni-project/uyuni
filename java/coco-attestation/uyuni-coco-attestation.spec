#
# spec file for package uyuni-coco-attestation
#
# Copyright (c) 2025 SUSE LLC and contributors
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

%global         pvattest_arch x86_64 s390x
%global         snpguest_arch x86_64

Name:           uyuni-coco-attestation
Version:        5.2.6
Release:        0
Summary:        %{productprettyname} utility for Confidential Computing Attestation
License:        GPL-2.0-only
Group:          System/Daemons
URL:            https://www.uyuni-project.org
#!CreateArchive: %{name}
Source0:        %{name}-%{version}.tar.gz
BuildRequires:  fdupes
BuildRequires:  java-devel >= 17
BuildRequires:  maven-local
BuildRequires:  mvn(com.mchange:c3p0)
BuildRequires:  mvn(com.mchange:mchange-commons-java)
BuildRequires:  mvn(ognl:ognl)
BuildRequires:  mvn(org.apache.logging.log4j:log4j-api)
BuildRequires:  mvn(org.apache.logging.log4j:log4j-core)
BuildRequires:  mvn(org.javassist:javassist)
BuildRequires:  mvn(org.mybatis:mybatis)
BuildRequires:  mvn(org.postgresql:postgresql)
BuildRequires:  mvn(org.uyuni-project:uyuni-java-common)
BuildRequires:  mvn(org.uyuni-project:uyuni-java-parent:pom:)

%description
System daemon used by %{productprettyname} to validate the results of confidential computing attestation.

%package core
Summary:        %{productprettyname} utility for Confidential Computing Attestation
BuildArch:      noarch

%description core
System daemon used by %{productprettyname} to validate the results of confidential computing attestation.

%ifarch %{pvattest_arch}
%package module-pvattest
Summary:        Confidential computing Pvattest attestation module for %{productprettyname}
Requires:       s390-tools

%description module-pvattest
Module for the %{productprettyname} Confidential Computing Attestation that uses Pvattest.
%endif

%ifarch %{snpguest_arch}
%package module-snpguest
Summary:        Confidential computing SNPGuest attestation module for %{productprettyname}
Requires:       snpguest

%description module-snpguest
Module for the %{productprettyname} Confidential Computing Attestation that uses SnpGuest.
%endif

%package module-secureboot
Summary:        Confidential computing SecureBoot attestation module for %{productprettyname}

%description module-secureboot
Module for the %{productprettyname} Confidential Computing Attestation for SecureBoot uses the output of mokutil.

%package        javadoc
Summary:        API documentation for %{name}
BuildArch:      noarch

%description    javadoc
Package containing the Javadoc API documentation for %{name}.

%prep
%setup -q

%ifnarch %{pvattest_arch}
%pom_disable_module 'attestation-module-pvattest'
%endif

%ifnarch %{snpguest_arch}
%pom_disable_module 'attestation-module-snpguest'
%endif

# Make sure there are not dependencies on the modules projects
%pom_remove_dep 'org.uyuni-project.coco-attestation.module:' attestation-core/pom.xml

# Shade is used only for developing convenience
%pom_remove_plugin -r :maven-shade-plugin

%if 0%{?suse_version} >= 1600
%pom_xpath_set 'pom:project/pom:dependencies/pom:dependency/pom:artifactId[text()="client"]' 'scram-client' attestation-core/pom.xml
%pom_xpath_set 'pom:project/pom:dependencies/pom:dependency/pom:artifactId[text()="common"]' 'scram-common' attestation-core/pom.xml
%endif

%{mvn_package} ':attestation-module-pvattest' module-pvattest

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
install -d -m 755 %{buildroot}%{_datadir}/coco-attestation/certs

# Required files
install -p -m 755 attestation-core/src/package/coco-attestation.sh %{buildroot}%{_sbindir}/coco-attestation
install -p -m 644 attestation-core/src/package/daemon.conf %{buildroot}%{_datadir}/coco-attestation/conf
install -p -m 644 attestation-core/src/package/log4j2.xml %{buildroot}%{_datadir}/coco-attestation/classes

# Create links for the jars
build-jar-repository -s -p %{buildroot}%{_datadir}/coco-attestation/lib uyuni-java-common/uyuni-common log4j/log4j-api log4j/log4j-core ongres-scram ongres-stringprep postgresql-jdbc ognl/ognl javassist mybatis mchange-commons c3p0

# Link all the attestation jars built and installed by maven
ln -s -f -r %{buildroot}%{_javadir}/uyuni-coco-attestation/*.jar %{buildroot}%{_datadir}/coco-attestation/lib

%ifarch %{snpguest_arch}
# Install snpguest certificates
cd attestation-module-snpguest/src/package/certs/snpguest
for FILE in $(find -name *.pem -type f -printf '%%P\n'); do
    echo $FILE
    install -D -p -m 644 $FILE %{buildroot}%{_datadir}/coco-attestation/certs/snpguest/$FILE
done
cd -
%endif

%ifarch %{pvattest_arch}
# Install pvattest certificate
install  -D -p -m 644 attestation-module-pvattest/src/package/certs/pvattest/DigiCertCA.pem  %{buildroot}%{_datadir}/coco-attestation/certs/pvattest/DigiCertCA.pem
%endif

%files core -f .mfiles
%dir %{_datadir}/coco-attestation/
%dir %{_datadir}/coco-attestation/conf/
%dir %{_datadir}/coco-attestation/lib/
%dir %{_datadir}/coco-attestation/certs/
%dir %{_datadir}/coco-attestation/classes/
%{_datadir}/coco-attestation/lib/*
%attr(755, root, root) %{_sbindir}/coco-attestation
%{_datadir}/coco-attestation/conf/daemon.conf
%{_datadir}/coco-attestation/classes/log4j2.xml
%license LICENSE

# Exclude all modules jars, will be part of their specific packages
%exclude %{_datadir}/coco-attestation/lib/attestation-module-*

%ifarch %{pvattest_arch}
%files module-pvattest -f .mfiles-module-pvattest
%dir %{_datadir}/coco-attestation/certs/pvattest/
%{_datadir}/coco-attestation/lib/attestation-module-pvattest.jar
%{_datadir}/coco-attestation/certs/pvattest/*
%license LICENSE
%endif

%ifarch %{snpguest_arch}
%files module-snpguest -f .mfiles-module-snpguest
%dir %{_datadir}/coco-attestation/certs/snpguest/
%{_datadir}/coco-attestation/lib/attestation-module-snpguest.jar
%{_datadir}/coco-attestation/certs/snpguest/*
%license LICENSE
%endif

%files module-secureboot -f .mfiles-module-secureboot
%{_datadir}/coco-attestation/lib/attestation-module-secureboot.jar
%license LICENSE

%files javadoc -f .mfiles-javadoc
%license LICENSE

%changelog
