#
# spec file for package spacewalk-java
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


%define cobblerdir      %{_localstatedir}/lib/cobbler
%define cobprofdir      %{cobblerdir}/templates
%define cobprofdirup    %{cobprofdir}/upload
%define cobprofdirwiz   %{cobprofdir}/wizard
%define cobdirsnippets  %{cobblerdir}/snippets
%define realcobsnippetsdir  %{cobdirsnippets}/spacewalk
%define run_checkstyle  1

%if 0%{?fedora} || 0%{?rhel} >= 7
%define appdir          %{_localstatedir}/lib/tomcat/webapps
%define jardir          %{_localstatedir}/lib/tomcat/webapps/rhn/WEB-INF/lib
%else
%if  0%{?suse_version}
%define appdir          /srv/tomcat/webapps
%define jardir          /srv/tomcat/webapps/rhn/WEB-INF/lib
%define run_checkstyle  0
%define omit_tests      1
%else
%define appdir          %{_localstatedir}/lib/tomcat6/webapps
%define jardir          %{_localstatedir}/lib/tomcat6/webapps/rhn/WEB-INF/lib
%endif
%endif

Name:           spacewalk-java
Summary:        Java web application files for Spacewalk
License:        GPL-2.0-only
Group:          Applications/Internet
Version:        4.0.15
Release:        1%{?dist}
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
Source1:        %{name}-rpmlintrc
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
ExcludeArch:    ia64 aarch64

Requires:       bcel
Requires:       c3p0 >= 0.9.1
Requires:       classpathx-mail
%if 0%{?suse_version}
Requires:       apache-commons-beanutils
Requires:       apache-commons-collections
Requires:       apache-commons-lang3
Requires:       cobbler >= 3.0.0
Requires:       concurrent
Requires:       google-gson >= 2.2.4
Requires:       httpcomponents-client
Requires:       jakarta-commons-digester
Requires:       java >= 11
Requires:       classmate
Requires:       ehcache >= 2.10.1
Requires:       gnu-jaf
Requires:       byte-buddy
Requires:       jpa-api
Requires:       hibernate-commons-annotations
Requires:       hibernate5
Requires:       jade4j
Requires:       javassist
Requires:       jboss-logging
Requires:       jose4j
Requires:       netty
Requires:       objectweb-asm
Requires:       /sbin/unix2_chkpwd
Requires:       prometheus-client-java
Requires:       salt-netapi-client >= 0.15.0
Requires:       snakeyaml
Requires:       spark-core
Requires:       spark-template-jade
Requires:       statistics
Requires:       sudo
Requires:       tomcat-taglibs-standard
Requires:       pgjdbc-ng
Requires:       susemanager-docs_en
BuildRequires:  apache-commons-lang
BuildRequires:  apache-commons-lang3
BuildRequires:  classmate
BuildRequires:  ehcache >= 2.10.1
BuildRequires:  google-gson >= 2.2.4
BuildRequires:  byte-buddy
BuildRequires:  jpa-api
BuildRequires:  hibernate-commons-annotations
BuildRequires:  hibernate5
BuildRequires:  java-devel >= 11
BuildRequires:  javassist
BuildRequires:  jboss-logging
BuildRequires:  jsch
BuildRequires:  netty
BuildRequires:  objectweb-asm
BuildRequires:  snakeyaml
BuildRequires:  statistics
# SUSE additional build requirements
BuildRequires:  log4j
# Spark and Salt integration
BuildRequires:  httpcomponents-client
BuildRequires:  httpcomponents-asyncclient
BuildRequires:  jade4j
BuildRequires:  jose4j
BuildRequires:  prometheus-client-java
BuildRequires:  salt-netapi-client >= 0.15.0
BuildRequires:  spark-core
BuildRequires:  spark-template-jade
BuildRequires:  velocity
BuildRequires: pgjdbc-ng
%else
Requires:       cobbler20
Requires:       jakarta-taglibs-standard
Requires:       java >= 11
Requires:       java-devel >= 11
Requires:       jpam
Requires:       oscache
BuildRequires:  java-devel >= 11
BuildRequires:  jpam
BuildRequires:  oscache

%endif # 0{?suse_version}
Requires:       jakarta-commons-el
Requires:       jakarta-commons-fileupload
Requires:       jcommon
Requires:       jdom
Requires:       jta
Requires:       log4j
Requires:       redstone-xmlrpc
Requires:       simple-core
Requires:       simple-xml
Requires:       sitemesh
Requires:       spacewalk-branding
Requires:       spacewalk-java-config
Requires:       spacewalk-java-jdbc
Requires:       spacewalk-java-lib
Requires:       stringtree-json
Requires:       xalan-j2 >= 2.6.0
Requires:       xerces-j2
%if 0%{?fedora}
Requires:       classpathx-jaf

%endif # 0{?fedora}
# EL5 = Struts 1.2 and Tomcat 5, EL6+/recent Fedoras = 1.3 and Tomcat 6
%if 0%{?fedora} || 0%{?rhel} >= 7
Requires:       servlet >= 3.0
Requires:       struts >= 1.3.0
Requires:       tomcat >= 7
Requires:       tomcat-lib >= 7
BuildRequires:  struts >= 1.3.0
BuildRequires:  tomcat >= 7
BuildRequires:  tomcat-lib >= 7
%else
%if 0%{?suse_version}
Requires:       struts >= 1.2.9
Requires(pre): tomcat >= 8
Requires:       tomcat-lib >= 8
Requires:       mvn(org.apache.tomcat:tomcat-servlet-api) > 8
BuildRequires:  struts >= 1.2.9
BuildRequires:  tomcat >= 8
BuildRequires:  tomcat-lib >= 8
%else
Requires:       struts >= 1.3.0
Requires:       struts-taglib >= 1.3.0
Requires:       tomcat6
Requires:       tomcat6-lib
Requires:       tomcat6-servlet-2.5-api
BuildRequires:  struts >= 1.3.0
BuildRequires:  struts-taglib >= 1.3.0
BuildRequires:  tomcat6
BuildRequires:  tomcat6-lib
%endif # 0{?suse_version}
%endif # 0{?fedora} || 0{?rhel} >= 7
%if 0%{?fedora} || 0%{?rhel} >=7
Requires:       apache-commons-cli
Requires:       apache-commons-codec
Requires:       apache-commons-discovery
Requires:       apache-commons-el
Requires:       apache-commons-io
Requires:       apache-commons-lang
Requires:       apache-commons-logging
Requires:       hibernate3 >= 3.6.10
Requires:       hibernate3-c3p0 >= 3.6.10
Requires:       hibernate3-ehcache >= 3.6.10
Requires:       javapackages-tools
Requires:       javassist
Requires:       tomcat-taglibs-standard
BuildRequires:  apache-commons-cli
BuildRequires:  apache-commons-codec
BuildRequires:  apache-commons-collections
BuildRequires:  apache-commons-discovery
BuildRequires:  apache-commons-el
BuildRequires:  apache-commons-io
BuildRequires:  apache-commons-logging
BuildRequires:  apache-commons-validator
# spelling checker is only for Fedoras (no aspell in RHEL6)
BuildRequires:  aspell
BuildRequires:  aspell-en
BuildRequires:  ehcache-core
BuildRequires:  hibernate3 >= 3.6.10
BuildRequires:  hibernate3-c3p0 >= 3.6.10
BuildRequires:  hibernate3-ehcache >= 3.6.10
BuildRequires:  javapackages-tools
BuildRequires:  javassist
BuildRequires:  libxslt
BuildRequires:  tomcat-taglibs-standard
BuildRequires:  mvn(ant-contrib:ant-contrib)
%else
%if 0%{?suse_version}
Requires:       apache-commons-cli
Requires:       apache-commons-codec
Requires:       apache-commons-io
Requires:       apache-commons-lang3
Requires:       apache-commons-logging
Requires:       jakarta-commons-discovery
Requires:       javapackages-tools
BuildRequires:  ant-contrib
BuildRequires:  ant-nodeps
BuildRequires:  apache-commons-cli
BuildRequires:  apache-commons-codec
BuildRequires:  apache-commons-collections
BuildRequires:  apache-commons-io
BuildRequires:  apache-commons-logging
BuildRequires:  jakarta-commons-discovery
BuildRequires:  jakarta-commons-validator
BuildRequires:  javapackages-tools
%else
Requires:       jakarta-commons-cli
Requires:       jakarta-commons-codec
Requires:       jakarta-commons-discovery
Requires:       jakarta-commons-io
Requires:       jakarta-commons-lang >= 2.1
Requires:       jakarta-commons-logging
Requires:       jpackage-utils
BuildRequires:  ant-contrib
BuildRequires:  ant-nodeps
BuildRequires:  jakarta-commons-cli
BuildRequires:  jakarta-commons-codec
BuildRequires:  jakarta-commons-collections
BuildRequires:  jakarta-commons-discovery
BuildRequires:  jakarta-commons-io
BuildRequires:  jakarta-commons-logging
BuildRequires:  jakarta-commons-validator
BuildRequires:  jpackage-utils
%endif # 0{?suse_version}
%endif # 0{?fedora} || 0{?rhel} >=7

# for RHEL6 we need to filter out several package versions
%if  0%{?rhel} && 0%{?rhel} >= 6
# cglib is not compatible with hibernate and asm from RHEL6
Requires:       cglib < 2.2
%else
Requires:       cglib
%endif
Requires:       dwr >= 3

%if 0%{?suse_version}
BuildRequires:  libxml2
BuildRequires:  libxml2-tools
BuildRequires:  perl
BuildRequires:  tomcat-taglibs-standard
%else
BuildRequires:  /usr/bin/perl
BuildRequires:  /usr/bin/xmllint
BuildRequires:  jakarta-taglibs-standard
%endif # 0{?suse_version}
BuildRequires:  ant
BuildRequires:  ant-apache-regexp
BuildRequires:  ant-junit
BuildRequires:  antlr >= 2.7.6
BuildRequires:  bcel
BuildRequires:  c3p0 >= 0.9.1
BuildRequires:  cglib
BuildRequires:  classpathx-mail
BuildRequires:  concurrent
BuildRequires:  dom4j
BuildRequires:  dwr >= 3
BuildRequires:  jaf
BuildRequires:  jakarta-commons-el
BuildRequires:  jakarta-commons-fileupload
BuildRequires:  jcommon
BuildRequires:  jdom
BuildRequires:  jta
BuildRequires:  postgresql-jdbc
BuildRequires:  quartz
BuildRequires:  redstone-xmlrpc
BuildRequires:  simple-core
BuildRequires:  simple-xml
BuildRequires:  sitemesh
BuildRequires:  stringtree-json
%if 0%{?run_checkstyle}
BuildRequires:  checkstyle
%endif
%if ! 0%{?omit_tests} > 0
BuildRequires:  translate-toolkit
%endif
Obsoletes:      rhn-java < 5.3.0
Obsoletes:      rhn-java-sat < 5.3.0
Provides:       rhn-java = %{version}-%{release}
Provides:       rhn-java-sat = %{version}-%{release}

%description
This package contains the code for the Java version of the Spacewalk Web Site.

%package config
Summary:        Configuration files for Spacewalk Java
Group:          Applications/Internet
%if 0%{?suse_version}
Requires(post): apache2
Requires(post): tomcat
%endif
Obsoletes:      rhn-java-config < 5.3.0
Obsoletes:      rhn-java-config-sat < 5.3.0
Provides:       rhn-java-config = %{version}-%{release}
Provides:       rhn-java-config-sat = %{version}-%{release}

%description config
This package contains the configuration files for the Spacewalk Java web
application and taskomatic process.

%package lib
Summary:        Jar files for Spacewalk Java
Group:          Applications/Internet
Obsoletes:      rhn-java-lib < 5.3.0
Obsoletes:      rhn-java-lib-sat < 5.3.0
Provides:       rhn-java-lib = %{version}-%{release}
Provides:       rhn-java-lib-sat = %{version}-%{release}
Requires:       /usr/bin/sudo

%description lib
This package contains the jar files for the Spacewalk Java web application
and taskomatic process.

%package postgresql
Summary:        PostgreSQL database backend support files for Spacewalk Java
Group:          Applications/Internet
Requires:       postgresql-jdbc
%if 0%{?fedora} || 0%{?rhel} >=7
Requires:       tomcat >= 7
%else
%if 0%{?suse_version}
Requires:       tomcat >= 8
%else
Requires:       tomcat6
%endif
%endif
Provides:       spacewalk-java-jdbc = %{version}-%{release}

%description postgresql
This package contains PostgreSQL database backend files for the Spacewalk Java.


%if ! 0%{?omit_tests} > 0
%package tests
Summary:        Test Classes for testing spacewalk-java
Group:          Applications/Internet

BuildRequires:  jmock < 2.0
Requires:       ant-junit
Requires:       jmock < 2.0

%description tests
This package contains testing files of spacewalk-java.

%files tests
%defattr(644,root,root,775)
%dir %{_datadir}/rhn/unit-tests
%{_datadir}/rhn/lib/rhn-test.jar
%{_datadir}/rhn/unit-tests/*
%{_datadir}/rhn/unittest.xml
%attr(644, tomcat, tomcat) %{jardir}/commons-lang3.jar
%endif

%package apidoc-sources
Summary:        Autogenerated apidoc-docbook xml sources for spacewalk-java
Group:          Applications/Internet

BuildRequires:  docbook_4

%description apidoc-sources
This package contains apidoc-docbook xml sources of spacewalk-java.

%files apidoc-sources
%defattr(644,root,root,775)
%docdir %{_defaultdocdir}/%{name}
%dir %{_defaultdocdir}/%{name}
%dir %{_defaultdocdir}/%{name}/xml
%{_defaultdocdir}/%{name}/xml/susemanager_api_doc.xml

%package -n spacewalk-taskomatic
Summary:        Java version of taskomatic
Group:          Applications/Internet

# for RHEL6 we need to filter out several package versions
%if  0%{?rhel} && 0%{?rhel} >= 6
# cglib is not compatible with hibernate and asm from RHEL6
Requires:       cglib < 2.2
%else
Requires:       cglib
%endif

Requires:       bcel
Requires:       c3p0 >= 0.9.1
%if 0%{?suse_version}
Requires:       cobbler >= 3.0.0
Requires:       java >= 11
Requires:       jsch
Requires:       /sbin/unix2_chkpwd
Requires:       tomcat-taglibs-standard
%else
Requires:       cobbler20
Requires:       jakarta-taglibs-standard
Requires:       java >= 11
Requires:       java-devel >= 11
Requires:       jpam
Requires:       oscache
%endif
Requires:       concurrent
Requires:       jcommon
Requires:       log4j
Requires:       quartz
Requires:       simple-core
Requires:       spacewalk-java-config
Requires:       spacewalk-java-jdbc
Requires:       spacewalk-java-lib
Requires:       xalan-j2 >= 2.6.0
Requires:       xerces-j2
%if 0%{?suse_version}
Requires:       classmate
Requires:       ehcache >= 2.10.1
Requires:       hibernate-commons-annotations
Requires:       hibernate5
Requires:       javassist
Requires:       jboss-logging
Requires:       statistics
Requires:       byte-buddy
Requires:       jpa-api
%else
Requires:       hibernate3 >= 3.2.4
%endif
%if 0%{?fedora} || 0%{?rhel} >= 7
Requires:       apache-commons-cli
Requires:       apache-commons-codec
Requires:       apache-commons-dbcp
Requires:       apache-commons-lang
Requires:       apache-commons-logging
Requires:       hibernate3 >= 3.6.10
Requires:       hibernate3-c3p0 >= 3.6.10
Requires:       hibernate3-ehcache >= 3.6.10
Requires:       javassist
Requires:       tomcat-taglibs-standard
%else
%if 0%{?suse_version}
Requires:       apache-commons-cli
Requires:       apache-commons-codec
Requires:       apache-commons-lang3
Requires:       apache-commons-logging
%else
Requires:       jakarta-commons-cli
Requires:       jakarta-commons-codec
Requires:       jakarta-commons-lang
Requires:       jakarta-commons-logging
%endif # 0{?suse_version}
%endif # 0{?fedora} || 0{?rhel} >= 7
Conflicts:      quartz < 2.0
Obsoletes:      taskomatic < 5.3.0
Obsoletes:      taskomatic-sat < 5.3.0
Provides:       taskomatic = %{version}-%{release}
Provides:       taskomatic-sat = %{version}-%{release}
%if 0%{?suse_version}
BuildRequires:  systemd
%{?systemd_requires}
Requires:       httpcomponents-client
Requires:       httpcomponents-core
Requires:       susemanager-frontend-libs >= 2.1.5
%else
Requires(post): chkconfig
Requires(preun): chkconfig
# This is for /sbin/service
Requires(preun): initscripts
%endif

%description -n spacewalk-taskomatic
This package contains the Java version of taskomatic.

%prep
%setup -q

# missing tomcat juli JAR (needed for JSP precompilation) - bug 661244
if test -d /usr/share/tomcat6; then
    mkdir -p build/build-lib
    if test ! -h /usr/share/java/tomcat6/tomcat-juli.jar; then
        ln -s /usr/share/tomcat6/bin/tomcat-juli.jar \
            build/build-lib/tomcat-juli.jar
    else
        ln -s /usr/share/java/tomcat6/tomcat-juli.jar \
                build/build-lib/tomcat-juli.jar
    fi
fi

%if 0%{?fedora}
%define skip_xliff  1
%endif

%if ! 0%{?omit_tests} > 0 && ! 0%{?skip_xliff}
find . -name 'StringResource_*.xml' |      while read i ;
    do echo $i
    # check for common localizations issues
    ln -s $(basename $i) $i.xliff
    CONTENT=$(pofilter --progress=none --nofuzzy --gnome \
                       --excludefilter=untranslated \
                       --excludefilter=purepunc \
                       $i.xliff 2>&1)
    if [ -n "$CONTENT" ]; then
        echo ERROR - pofilter errors: "$CONTENT"
        exit 1
    fi
    rm -f $i.xliff

    #check duplicate message keys in StringResource_*.xml files
    CONTENT=$(/usr/bin/xmllint --format "$i" | /usr/bin/perl -lne 'if (/<trans-unit( id=".+?")?/) { print $1 if $X{$1}++ }' )
    if [ -n "$CONTENT" ]; then
        echo ERROR - duplicate message keys: $CONTENT
        exit 1
    fi
done
%endif

%build
# compile only java sources (no packing here)
%if 0%{?suse_version} >= 1500
ant -Dprefix=$RPM_BUILD_ROOT -Dtomcat="tomcat9" init-install compile
%else
ant -Dprefix=$RPM_BUILD_ROOT -Dtomcat="tomcat8" init-install compile
%endif

%if 0%{?run_checkstyle}
echo "Running checkstyle on java main sources"
export CLASSPATH="build/classes:build/build-lib/*"
export ADDITIONAL_OPTIONS="-Djavadoc.method.scope=public \
-Djavadoc.type.scope=package \
-Djavadoc.var.scope=package \
-Dcheckstyle.cache.file=build/checkstyle.cache.src \
-Djavadoc.lazy=false \
-Dcheckstyle.header.file=buildconf/LICENSE.txt"
find . -name *.java | grep -vE '(/test/|/jsp/|/playpen/)' | \
xargs checkstyle -c buildconf/checkstyle.xml

echo "Running checkstyle on java test sources"
export ADDITIONAL_OPTIONS="-Djavadoc.method.scope=nothing \
-Djavadoc.type.scope=nothing \
-Djavadoc.var.scope=nothing \
-Dcheckstyle.cache.file=build/checkstyle.cache.test \
-Djavadoc.lazy=false \
-Dcheckstyle.header.file=buildconf/LICENSE.txt"
find . -name *.java | grep -E '/test/' | grep -vE '(/jsp/|/playpen/)' | \
xargs checkstyle -c buildconf/checkstyle.xml

# catch macro name errors
find . -type f -name '*.xml' | xargs perl -CSAD -lne '
          for (grep { $_ ne "PRODUCT_NAME" } /\@\@(\w+)\@\@/g) {
              print;
              $exit = 1;
          }
          @r = /((..)?PRODUCT_NAME(..)?)/g ;
          while (@r) {
              $s = shift(@r); $f = shift(@r); $l = shift(@r);
              if ($f ne "@@" or $l ne "@@") {
                  print $s;
                  $exit = 1;
              }
          }
          END { exit $exit }'
%endif

echo "Building apidoc docbook sources"
%if 0%{?suse_version} >= 1500
ant -Dprefix=$RPM_BUILD_ROOT -Dtomcat="tomcat9" init-install apidoc-docbook
%else
ant -Dprefix=$RPM_BUILD_ROOT -Dtomcat="tomcat8" init-install apidoc-docbook
%endif
cd build/reports/apidocs/docbook
/usr/bin/xmllint --xinclude --postvalid book.xml > susemanager_api_doc.xml
cd $RPM_BUILD_ROOT

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
export NO_BRP_STALE_LINK_ERROR=yes

# on Fedora 19 some jars are named differently
%if 0%{?fedora} || 0%{?rhel} >= 7
ant -Dprefix=$RPM_BUILD_ROOT install-tomcat
install -d -m 755 $RPM_BUILD_ROOT%{appdir}/rhn/META-INF/

# Need to use 2 versions of context.xml, Tomcat 8 changed syntax
%if 0%{?fedora} >= 23
install -m 644 conf/rhn-tomcat8.xml $RPM_BUILD_ROOT%{appdir}/rhn/META-INF/context.xml
%else
install -m 644 conf/rhn-tomcat5.xml $RPM_BUILD_ROOT%{appdir}/rhn/META-INF/context.xml
%endif # 0{?fedora} >= 23

%else
%if 0%{?suse_version}
%if 0%{?suse_version} < 1500
ant -Dprefix=$RPM_BUILD_ROOT -Dtomcat="tomcat8" install-tomcat8-suse
install -d -m 755 $RPM_BUILD_ROOT%{appdir}/rhn/META-INF/
install -m 755 conf/rhn-tomcat8.xml $RPM_BUILD_ROOT%{appdir}/rhn/META-INF/context.xml
%else
ant -Dprefix=$RPM_BUILD_ROOT -Dtomcat="tomcat9" install-tomcat9-suse
install -d -m 755 $RPM_BUILD_ROOT%{appdir}/rhn/META-INF/
install -m 755 conf/rhn-tomcat9.xml $RPM_BUILD_ROOT%{appdir}/rhn/META-INF/context.xml
%endif # 0{?suse_version} < 1500
%else
ant -Dprefix=$RPM_BUILD_ROOT install-tomcat6
install -d -m 755 $RPM_BUILD_ROOT%{appdir}/rhn/META-INF/
install -m 644 conf/rhn-tomcat5.xml $RPM_BUILD_ROOT%{appdir}/rhn/META-INF/context.xml
%endif # 0{?suse_version}
%endif # 0{?fedora} || 0{?rhel} >= 7

# check spelling errors in all resources for English if aspell installed
[ -x "$(which aspell)" ] && scripts/spelling/check_java.sh .. en_US

%if 0%{?fedora} || 0%{?rhel} >= 7 || 0%{?suse_version} >= 1310
install -d -m 755 $RPM_BUILD_ROOT%{_sbindir}
install -d -m 755 $RPM_BUILD_ROOT%{_unitdir}
%else
install -d -m 755 $RPM_BUILD_ROOT%{_initrddir}
%endif
install -d -m 755 $RPM_BUILD_ROOT%{_bindir}
install -d -m 755 $RPM_BUILD_ROOT%{_sysconfdir}/rhn
install -d -m 755 $RPM_BUILD_ROOT%{_prefix}/share/rhn
install -d -m 755 $RPM_BUILD_ROOT%{_prefix}/share/rhn/unit-tests
install -d -m 755 $RPM_BUILD_ROOT%{_prefix}/share/rhn/lib
install -d -m 755 $RPM_BUILD_ROOT%{_prefix}/share/rhn/classes
install -d -m 755 $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults
install -d -m 755 $RPM_BUILD_ROOT%{_prefix}/share/rhn/search
install -d -m 755 $RPM_BUILD_ROOT%{_prefix}/share/rhn/search/lib
install -d -m 755 $RPM_BUILD_ROOT%{_prefix}/share/spacewalk/taskomatic
install -d -m 755 $RPM_BUILD_ROOT%{cobprofdir}
install -d -m 755 $RPM_BUILD_ROOT%{cobprofdirup}
install -d -m 755 $RPM_BUILD_ROOT%{cobprofdirwiz}
install -d -m 755 $RPM_BUILD_ROOT%{cobdirsnippets}
%if 0%{?suse_version}
install -d -m 755 $RPM_BUILD_ROOT/%{_localstatedir}/lib/spacewalk/scc
install -d -m 755 $RPM_BUILD_ROOT/%{_localstatedir}/lib/spacewalk/subscription-matcher
%else
install -d -m 755 $RPM_BUILD_ROOT/%{_var}/spacewalk/systemlogs
%endif

install -d -m 755 $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d
install -d $RPM_BUILD_ROOT/srv/susemanager/salt
install -d $RPM_BUILD_ROOT/srv/susemanager/salt/salt_ssh
install -d -m 775 $RPM_BUILD_ROOT/srv/susemanager/pillar_data
install -d -m 775 $RPM_BUILD_ROOT/srv/susemanager/pillar_data/images
install -d $RPM_BUILD_ROOT/srv/susemanager/formula_data
install -d $RPM_BUILD_ROOT/srv/susemanager/tmp

%if 0%{?fedora} || 0%{?rhel} >= 7
echo "hibernate.cache.region.factory_class=net.sf.ehcache.hibernate.SingletonEhCacheRegionFactory" >> conf/default/rhn_hibernate.conf
%else
echo "hibernate.cache.provider_class=org.hibernate.cache.OSCacheProvider" >> conf/default/rhn_hibernate.conf
%endif

#######################################
# this overwrite all the setting above!
#######################################
%if 0%{suse_version}
cp conf/default/rhn_hibernate.conf.SUSE conf/default/rhn_hibernate.conf
%endif

install -m 644 conf/default/rhn_hibernate.conf $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults/rhn_hibernate.conf
install -m 644 conf/default/rhn_taskomatic_daemon.conf $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults/rhn_taskomatic_daemon.conf
install -m 644 conf/default/taskomatic.conf $RPM_BUILD_ROOT%{_sysconfdir}/rhn/taskomatic.conf
install -m 644 conf/default/rhn_org_quartz.conf $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults/rhn_org_quartz.conf
install -m 644 conf/rhn_java.conf $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults
install -m 644 conf/rhn_java_sso.conf $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults

# Adjust product tree tag
%if 0%{?sle_version} && !0%{?is_opensuse}
sed -i -e 's/^java.product_tree.tag =.*$/java.product_tree.tag = SUMA4.0/' $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults/rhn_java.conf
%else
sed -i -e 's/^java.product_tree.tag =.*$/java.product_tree.tag = Uyuni/' $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults/rhn_java.conf
%endif
install -m 644 conf/logrotate/rhn_web_api $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d/rhn_web_api
install -m 644 conf/logrotate/gatherer $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d/gatherer
%if 0%{?fedora} || 0%{?rhel} >= 7 || 0%{?suse_version} >= 1310
# LOGROTATE >= 3.8 requires extra permission config
sed -i 's/#LOGROTATE-3.8#//' $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d/rhn_web_api
install -m 755 scripts/taskomatic $RPM_BUILD_ROOT%{_sbindir}
install -m 644 scripts/taskomatic.service $RPM_BUILD_ROOT%{_unitdir}
%else
install -m 755 scripts/taskomatic $RPM_BUILD_ROOT%{_initrddir}
%endif
# add rc link
ln -sf service $RPM_BUILD_ROOT/%{_sbindir}/rctaskomatic

install -m 644 scripts/unittest.xml $RPM_BUILD_ROOT/%{_datadir}/rhn/
install -m 644 build/webapp/rhnjava/WEB-INF/lib/rhn.jar $RPM_BUILD_ROOT%{_datadir}/rhn/lib
%if ! 0%{?omit_tests} > 0
install -m 644 build/webapp/rhnjava/WEB-INF/lib/rhn-test.jar $RPM_BUILD_ROOT%{_datadir}/rhn/lib
cp -a build/classes/com/redhat/rhn/common/conf/test/conf $RPM_BUILD_ROOT%{_datadir}/rhn/unit-tests/
%endif
install -m 644 conf/log4j.properties.taskomatic $RPM_BUILD_ROOT%{_datadir}/rhn/classes/log4j.properties
install -m 644 code/src/ehcache.xml $RPM_BUILD_ROOT%{_datadir}/rhn/classes/ehcache.xml

install -d -m 755 $RPM_BUILD_ROOT%{realcobsnippetsdir}
install -m 644 conf/cobbler/snippets/default_motd  $RPM_BUILD_ROOT%{realcobsnippetsdir}/default_motd
install -m 644 conf/cobbler/snippets/keep_system_id  $RPM_BUILD_ROOT%{realcobsnippetsdir}/keep_system_id
install -m 644 conf/cobbler/snippets/post_reactivation_key  $RPM_BUILD_ROOT%{realcobsnippetsdir}/post_reactivation_key
install -m 644 conf/cobbler/snippets/post_delete_system  $RPM_BUILD_ROOT%{realcobsnippetsdir}/post_delete_system
install -m 644 conf/cobbler/snippets/redhat_register  $RPM_BUILD_ROOT%{realcobsnippetsdir}/redhat_register
install -m 644 conf/cobbler/snippets/sles_register    $RPM_BUILD_ROOT%{realcobsnippetsdir}/sles_register
install -m 644 conf/cobbler/snippets/sles_register_script $RPM_BUILD_ROOT%{realcobsnippetsdir}/sles_register_script
install -m 644 conf/cobbler/snippets/sles_no_signature_checks $RPM_BUILD_ROOT%{realcobsnippetsdir}/sles_no_signature_checks
install -m 644 conf/cobbler/snippets/wait_for_networkmanager_script $RPM_BUILD_ROOT%{realcobsnippetsdir}/wait_for_networkmanager_script

ln -s -f %{_javadir}/dwr.jar $RPM_BUILD_ROOT%{jardir}/dwr.jar
%if 0%{?suse_version}
install -d -m 755 $RPM_BUILD_ROOT%{_datadir}/spacewalk/audit
install -m 644 conf/audit/auditlog-config.yaml $RPM_BUILD_ROOT%{_datadir}/spacewalk/audit/auditlog-config.yaml
%else
touch $RPM_BUILD_ROOT%{_var}/spacewalk/systemlogs/audit-review.log
%endif

# special links for taskomatic
TASKOMATIC_BUILD_DIR=%{_prefix}/share/spacewalk/taskomatic
rm -f $RPM_BUILD_ROOT$TASKOMATIC_BUILD_DIR/slf4j*nop.jar
rm -f $RPM_BUILD_ROOT$TASKOMATIC_BUILD_DIR/slf4j*simple.jar

# special links for rhn-search
RHN_SEARCH_BUILD_DIR=%{_prefix}/share/rhn/search/lib
ln -s -f %{_javadir}/postgresql-jdbc.jar $RPM_BUILD_ROOT$RHN_SEARCH_BUILD_DIR/postgresql-jdbc.jar

# install docbook sources
mkdir -p $RPM_BUILD_ROOT%{_docdir}/%{name}/xml
install -m 644 build/reports/apidocs/docbook/susemanager_api_doc.xml $RPM_BUILD_ROOT%{_docdir}/%{name}/xml/susemanager_api_doc.xml
# delete JARs which must not be deployed
rm -rf $RPM_BUILD_ROOT%{jardir}/jspapi.jar
rm -rf $RPM_BUILD_ROOT%{jardir}/jasper5-compiler.jar
rm -rf $RPM_BUILD_ROOT%{jardir}/jasper5-runtime.jar
rm -rf $RPM_BUILD_ROOT%{jardir}/tomcat*.jar
%if 0%{?omit_tests} > 0
rm -rf $RPM_BUILD_ROOT%{_datadir}/rhn/lib/rhn-test.jar
rm -rf $RPM_BUILD_ROOT/classes/com/redhat/rhn/common/conf/test/conf
rm -rf $RPM_BUILD_ROOT%{_datadir}/rhn/unittest.xml
%endif

# show all JAR symlinks
echo "#### SYMLINKS START ####"
find $RPM_BUILD_ROOT%{jardir} -name *.jar
echo "#### SYMLINKS END ####"

%if 0%{?suse_version}
%pre -n spacewalk-taskomatic
%service_add_pre taskomatic.service

%post -n spacewalk-taskomatic
%service_add_post taskomatic.service

%preun -n spacewalk-taskomatic
%service_del_preun taskomatic.service

%postun -n spacewalk-taskomatic
%service_del_postun taskomatic.service

%post config
if [ ! -d /var/log/rhn ]; then
    mkdir /var/log/rhn
    chown root:www /var/log/rhn
    chmod 770 /var/log/rhn
fi
if [ ! -e /var/log/rhn/rhn_web_api.log ]; then
    touch /var/log/rhn/rhn_web_api.log
fi
chown tomcat:www /var/log/rhn/rhn_web_api.log

if [ ! -e /var/log/rhn/gatherer.log ]; then
    touch /var/log/rhn/gatherer.log
fi
chown tomcat:www /var/log/rhn/gatherer.log

%else
%post -n spacewalk-taskomatic
if [ -f /etc/init.d/taskomatic ]; then
   # This adds the proper /etc/rc*.d links for the script
   /sbin/chkconfig --add taskomatic
fi

%preun -n spacewalk-taskomatic
if [ $1 = 0 ] ; then
   if [ -f /etc/init.d/taskomatic ]; then
      /sbin/service taskomatic stop >/dev/null 2>&1
      /sbin/chkconfig --del taskomatic
   fi
fi
%endif

%post
if [ $1 -gt 1 ]; then
   if [ -f %{_sysconfdir}/tomcat6/Catalina/localhost/rhn.xml ]; then
      mv %{_sysconfdir}/tomcat6/Catalina/localhost/rhn.xml %{appdir}/rhn/META-INF/context.xml
   elif [ -f %{_sysconfdir}/tomcat/Catalina/localhost/rhn.xml ]; then
      mv %{_sysconfdir}/tomcat/Catalina/localhost/rhn.xml %{appdir}/rhn/META-INF/context.xml
   fi
fi

%files
%if 0%{?suse_version}
%defattr(-,root,root)
%dir %{_localstatedir}/lib/spacewalk
%endif
%defattr(644,tomcat,tomcat,775)
%attr(775, salt, salt) %dir /srv/susemanager/salt/salt_ssh
%attr(775, root, tomcat) %dir %{appdir}
%dir /srv/susemanager
%dir /srv/susemanager/salt
%attr(775,tomcat,susemanager) %dir /srv/susemanager/pillar_data
%attr(775,tomcat,susemanager) %dir /srv/susemanager/pillar_data/images
%dir /srv/susemanager/formula_data
%attr(750, tomcat, salt) %dir /srv/susemanager/tmp
%dir %{appdir}/rhn/
%{appdir}/rhn/apidoc/
%{appdir}/rhn/css/
%{appdir}/rhn/errata/
%{appdir}/rhn/img/
%{appdir}/rhn/META-INF/
%{appdir}/rhn/schedule/
%{appdir}/rhn/systems/
%{appdir}/rhn/users/
%{appdir}/rhn/errors/
%{appdir}/rhn/*.jsp
%{appdir}/rhn/WEB-INF/classes
%{appdir}/rhn/WEB-INF/decorators
%{appdir}/rhn/WEB-INF/includes
%{appdir}/rhn/WEB-INF/nav
%{appdir}/rhn/WEB-INF/pages
%{appdir}/rhn/WEB-INF/*.xml
# list of all jar symlinks without any version numbers
# and wildcards (except non-symlink velocity)
%{jardir}/antlr.jar
%{jardir}/bcel.jar
%{jardir}/c3p0*.jar
%{jardir}/cglib.jar
%{jardir}/commons-beanutils.jar
%{jardir}/commons-cli.jar
%{jardir}/commons-codec.jar
%{jardir}/commons-collections.jar
%{jardir}/commons-digester.jar
%{jardir}/commons-discovery.jar
%{jardir}/commons-el.jar
%{jardir}/commons-fileupload.jar
%{jardir}/commons-io.jar
%{jardir}/commons-logging.jar
%{jardir}/*commons-validator.jar
%{jardir}/concurrent*.jar
%{jardir}/dom4j.jar
%{jardir}/dwr.jar
%if 0%{?fedora} || 0%{?rhel} >= 7
%{jardir}/ehcache-core.jar
%{jardir}/*_hibernate-commons-annotations.jar
%{jardir}/hibernate-jpa-2.0-api*.jar
%{jardir}/javassist.jar
%{jardir}/mchange-commons*.jar
%{jardir}/slf4j_api.jar
%{jardir}/slf4j_log4j12*.jar
%{jardir}/*jboss-logging.jar

%endif
%if 0%{?suse_version}
%{jardir}/google-gson.jar
%{jardir}/snakeyaml.jar
# SUSE extra runtime dependencies: spark, jade4j, salt API client + dependencies
%{jardir}/commons-jexl.jar
%{jardir}/commons-lang3.jar
%{jardir}/httpclient.jar
%{jardir}/httpcore.jar
%{jardir}/httpcore-nio.jar
%{jardir}/httpasyncclient.jar
%{jardir}/jade4j.jar
%{jardir}/jose4j.jar
%{jardir}/netty*.jar
%{jardir}/salt-netapi-client.jar
%{jardir}/slf4j_api.jar
%{jardir}/slf4j_log4j12*.jar
%{jardir}/spark-core.jar
%{jardir}/spark-template-jade.jar
%{jardir}/simpleclient*.jar
%{jardir}/pgjdbc-ng.jar

%{jardir}/byte-buddy.jar
%{jardir}/jakarta-persistence-api.jar

# Hibernate and related
%{jardir}/hibernate-core-5.jar
%{jardir}/hibernate-c3p0-5.jar
%{jardir}/hibernate-ehcache-5.jar
%{jardir}/hibernate-commons-annotations.jar
%{jardir}/ehcache-core.jar
%{jardir}/classmate.jar
%{jardir}/javassist.jar
%{jardir}/jboss-logging.jar
%{jardir}/statistics.jar
%else
%{jardir}/hibernate3*
%{jardir}/jpam.jar
%{jardir}/oscache.jar
%endif
%{jardir}/jaf.jar
%{jardir}/javamail.jar
%{jardir}/jcommon*.jar
%{jardir}/jdom.jar
%{jardir}/jta.jar
%{jardir}/log4j*.jar
%{jardir}/oro.jar
%{jardir}/quartz.jar
%{jardir}/redstone-xmlrpc-client.jar
%{jardir}/redstone-xmlrpc.jar
%{jardir}/rhn.jar
%{jardir}/simple-core.jar
%{jardir}/simple-xml.jar
%{jardir}/sitemesh.jar
%{jardir}/stringtree-json.jar
%{jardir}/xalan-j2.jar
%{jardir}/xalan-j2-serializer.jar
%{jardir}/xerces-j2.jar
%{jardir}/xml-commons-apis.jar

%if 0%{suse_version}
%{jardir}/struts.jar
%{jardir}/objectweb-asm_asm.jar
%{jardir}/taglibs-standard-impl.jar
%{jardir}/taglibs-standard-jstlel.jar
%{jardir}/taglibs-standard-spec.jar
%else
%{jardir}/asm_asm.jar
%{jardir}/struts*.jar
%{jardir}/commons-chain.jar
%{jardir}/taglibs-core.jar
%{jardir}/taglibs-standard.jar
%endif

%dir %{cobprofdir}
%dir %{cobprofdirup}
%dir %{cobprofdirwiz}
%dir %{cobdirsnippets}
%dir %{realcobsnippetsdir}
%config %{realcobsnippetsdir}/default_motd
%config %{realcobsnippetsdir}/keep_system_id
%config %{realcobsnippetsdir}/post_reactivation_key
%config %{realcobsnippetsdir}/post_delete_system
%config %{realcobsnippetsdir}/redhat_register
%config %{realcobsnippetsdir}/sles_register
%config %{realcobsnippetsdir}/sles_register_script
%config %{realcobsnippetsdir}/sles_no_signature_checks
%config %{realcobsnippetsdir}/wait_for_networkmanager_script
%if 0%{?fedora} || 0%{?rhel} >= 7
%config(noreplace) %{appdir}/rhn/META-INF/context.xml
%else
%if  0%{?suse_version}
%config(noreplace) %{appdir}/rhn/META-INF/context.xml
%attr(755,root,root) %dir %{cobblerdir}
%else
%config(noreplace) %{appdir}/rhn/META-INF/context.xml
%endif
%endif

%if 0%{?suse_version}
%attr(755, tomcat, root) %dir %{_localstatedir}/lib/spacewalk/scc
%attr(755, tomcat, root) %dir %{_localstatedir}/lib/spacewalk/subscription-matcher
%dir %{appdir}/rhn/WEB-INF
%dir %{jardir}
%else
%dir %attr(755, tomcat, root) %{_var}/spacewalk/systemlogs
%ghost %attr(644, tomcat, root) %{_var}/spacewalk/systemlogs/audit-review.log
%endif

%files -n spacewalk-taskomatic
%defattr(644,root,root,775)
%if 0%{?fedora} || 0%{?rhel} >= 7 || 0%{?suse_version} >= 1310
%attr(755, root, root) %{_sbindir}/taskomatic
%attr(644, root, root) %{_unitdir}/taskomatic.service
%else
%attr(755, root, root) %{_initrddir}/taskomatic
%endif
%{_datarootdir}/spacewalk/taskomatic
%{_sbindir}/rctaskomatic

%files config
%defattr(644,root,root,755)
%attr(755,root,www) %dir %{_prefix}/share/rhn/config-defaults
%attr(0750,root,www) %dir /etc/rhn
%{_prefix}/share/rhn/config-defaults/rhn_hibernate.conf
%{_prefix}/share/rhn/config-defaults/rhn_taskomatic_daemon.conf
%config(noreplace) %{_sysconfdir}/rhn/taskomatic.conf
%{_prefix}/share/rhn/config-defaults/rhn_org_quartz.conf
%{_prefix}/share/rhn/config-defaults/rhn_java.conf
%{_prefix}/share/rhn/config-defaults/rhn_java_sso.conf
%config %{_sysconfdir}/logrotate.d/rhn_web_api
%config %{_sysconfdir}/logrotate.d/gatherer
%dir %{_datadir}/spacewalk
%dir %{_datadir}/spacewalk/audit
%config %{_datadir}/spacewalk/audit/auditlog-config.yaml

%files lib
%defattr(644,root,root,755)
%dir %{_datadir}/rhn
%dir %{_datadir}/rhn/lib
%dir %{_datadir}/rhn/classes
%{_datadir}/rhn/classes/log4j.properties
%{_datadir}/rhn/classes/ehcache.xml
%{_datadir}/rhn/lib/rhn.jar

%files postgresql
%defattr(644,root,root,755)
%dir %{_prefix}/share/rhn/search
%dir %{_prefix}/share/rhn/search/lib
%{jardir}/postgresql-jdbc.jar
%{_prefix}/share/rhn/search/lib/postgresql-jdbc.jar

%changelog
