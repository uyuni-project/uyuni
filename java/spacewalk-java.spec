#
# spec file for package spacewalk-java
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


#!BuildIgnore:  udev-mini libudev-mini1

%define cobblerdir      %{_localstatedir}/lib/cobbler
%define cobprofdir      %{cobblerdir}/templates
%define cobprofdirup    %{cobprofdir}/upload
%define cobprofdirwiz   %{cobprofdir}/wizard
%define cobdirsnippets  %{cobblerdir}/snippets
%define spacewalksnippetsdir  %{cobdirsnippets}/spacewalk
%define run_checkstyle  0
%define omit_tests      1

%define susemanagershareddir       %{_datadir}/susemanager
%define serverdir       %{susemanagershareddir}/www
%define salt_user_group salt

%{!?java_version: %global java_version 11}
%if 0%{?suse_version}
%define userserverdir       /srv
%define apache_group    www
%define apache2         apache2
%else
%define userserverdir       %{_sharedstatedir}
%define apache_group    apache
%define apache2         httpd
%define java_version    1:%{java_version}
%endif

%define ehcache         ( mvn(net.sf.ehcache:ehcache-core) >= 2.10.1 or ehcache-core >= 2.10.1 or ehcache >= 2.10.1)
%define apache_commons_digester    (apache-commons-digester or jakarta-commons-digester)
%define apache_commons_discovery   (apache-commons-discovery or jakarta-commons-discovery)
%define apache_commons_fileupload  (apache-commons-fileupload or jakarta-commons-fileupload)
%define apache_commons_validator   (apache-commons-validator or jakarta-commons-validator)
%define apache_commons_compress    (apache-commons-compress or jakarta-commons-compress)

%if 0%{?is_opensuse}
%define supported_locales bn_IN,ca,de,en_US,es,fr,gu,hi,it,ja,ko,pa,pt,pt_BR,ru,ta,zh_CN,zh_TW
%else
%define supported_locales en_US,ko,ja,zh_CN
%endif

Name:           spacewalk-java
Summary:        Java web application files for Spacewalk
License:        GPL-2.0-only
Group:          Applications/Internet
Version:        5.0.11
Release:        0
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
Source1:        https://raw.githubusercontent.com/uyuni-project/uyuni/%{name}-%{version}-0/java/%{name}-rpmlintrc
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
ExcludeArch:    ia64

BuildRequires:  %{apache_commons_compress}
BuildRequires:  %{apache_commons_discovery}
BuildRequires:  %{apache_commons_fileupload}
BuildRequires:  %{apache_commons_validator}
BuildRequires:  %{ehcache}
BuildRequires:  ant
BuildRequires:  ant-apache-regexp
BuildRequires:  ant-contrib
BuildRequires:  ant-junit
BuildRequires:  antlr >= 2.7.6
BuildRequires:  apache-commons-cli
BuildRequires:  apache-commons-codec
BuildRequires:  apache-commons-collections
BuildRequires:  apache-commons-el
BuildRequires:  apache-commons-io >= 2.11.0
BuildRequires:  apache-commons-jexl
BuildRequires:  apache-commons-lang3 >= 3.4
BuildRequires:  apache-commons-logging
BuildRequires:  bcel
BuildRequires:  mvn(net.bytebuddy:byte-buddy) >= 1.14
BuildRequires:  mvn(net.bytebuddy:byte-buddy-dep) >= 1.14
BuildRequires:  c3p0 >= 0.9.1
BuildRequires:  cglib
BuildRequires:  classmate
BuildRequires:  concurrent
BuildRequires:  dom4j
BuildRequires:  dwr >= 3
BuildRequires:  glassfish-activation
BuildRequires:  glassfish-jaxb-api
BuildRequires:  glassfish-jaxb-runtime
BuildRequires:  glassfish-jaxb-txw2
BuildRequires:  hibernate-commons-annotations
BuildRequires:  hibernate-types
BuildRequires:  httpcomponents-asyncclient
BuildRequires:  httpcomponents-client
BuildRequires:  ical4j
BuildRequires:  istack-commons-runtime
BuildRequires:  jade4j
BuildRequires:  java-%{java_version}-openjdk-devel
BuildRequires:  java-saml
BuildRequires:  javamail
BuildRequires:  javapackages-tools
BuildRequires:  javassist
BuildRequires:  jboss-logging
BuildRequires:  jdom
BuildRequires:  joda-time
BuildRequires:  jose4j
BuildRequires:  jpa-api
BuildRequires:  jsch
BuildRequires:  jta
BuildRequires:  libxml2
BuildRequires:  log4j
BuildRequires:  log4j-jcl
BuildRequires:  log4j-slf4j
BuildRequires:  netty
BuildRequires:  objectweb-asm >= 9.2
BuildRequires:  perl
BuildRequires:  pgjdbc-ng
BuildRequires:  postgresql-jdbc
BuildRequires:  prometheus-client-java
BuildRequires:  quartz
BuildRequires:  redstone-xmlrpc
BuildRequires:  salt-netapi-client >= 0.21
BuildRequires:  simple-core
BuildRequires:  sitemesh
BuildRequires:  snakeyaml >= 1.33
BuildRequires:  spark-core
BuildRequires:  spark-template-jade
BuildRequires:  statistics
BuildRequires:  stringtree-json
BuildRequires:  struts >= 1.2.9
BuildRequires:  tomcat >= 7
BuildRequires:  tomcat-lib >= 7
BuildRequires:  tomcat-taglibs-standard
BuildRequires:  uyuni-base-server
BuildRequires:  woodstox
BuildRequires:  xalan-j2
BuildRequires:  xmlsec
BuildRequires:  (google-gson >= 2.2.4 with google-gson < 2.10.0)
BuildRequires:  mvn(org.apache.velocity:velocity-engine-core) >= 2.2
BuildRequires:  mvn(org.hibernate:hibernate-c3p0)
BuildRequires:  mvn(org.hibernate:hibernate-core)
BuildRequires:  mvn(org.hibernate:hibernate-ehcache)
%if 0%{?suse_version}
BuildRequires:  ant-nodeps
BuildRequires:  libxml2-tools
%endif
%if 0%{?rhel}
BuildRequires:  libxml2-devel
%endif

Requires:       %{apache_commons_compress}
Requires:       %{apache_commons_digester}
Requires:       %{apache_commons_discovery}
Requires:       %{apache_commons_fileupload}
Requires:       %{ehcache}
Requires:       apache-commons-beanutils
Requires:       apache-commons-cli
Requires:       apache-commons-codec
Requires:       apache-commons-collections
Requires:       apache-commons-el
Requires:       apache-commons-io
Requires:       apache-commons-jexl
Requires:       apache-commons-lang3
Requires:       apache-commons-logging
Requires:       bcel
Requires:       mvn(net.bytebuddy:byte-buddy) >= 1.14
Requires:       mvn(net.bytebuddy:byte-buddy-dep) >= 1.14
Requires:       c3p0 >= 0.9.1
Requires:       cglib
Requires:       classmate
Requires:       cobbler
Requires:       concurrent
Requires:       dwr >= 3
Requires:       glassfish-activation
Requires:       glassfish-jaxb-api
Requires:       glassfish-jaxb-runtime
Requires:       glassfish-jaxb-txw2
Requires:       hibernate-commons-annotations
Requires:       hibernate-types
Requires:       httpcomponents-client
Requires:       ical4j
Requires:       istack-commons-runtime
Requires:       jade4j
Requires:       java-%{java_version}-openjdk
Requires:       java-saml
Requires:       javamail
Requires:       javapackages-tools
Requires:       javassist
Requires:       jboss-logging
Requires:       jdom
Requires:       joda-time
Requires:       jose4j
Requires:       jpa-api
Requires:       jta
Requires:       libsolv-tools
Requires:       log4j
Requires:       log4j-jcl
Requires:       log4j-slf4j
Requires:       mgr-libmod
Requires:       netty
Requires:       objectweb-asm >= 9.2
Requires:       pgjdbc-ng
Requires:       prometheus-client-java
Requires:       redstone-xmlrpc
Requires:       salt-netapi-client >= 0.21
Requires:       simple-core
Requires:       sitemesh
Requires:       snakeyaml >= 1.33
Requires:       spacewalk-branding
Requires:       spacewalk-java-config
Requires:       spacewalk-java-jdbc
Requires:       spacewalk-java-lib = %{version}
Requires:       spark-core
Requires:       spark-template-jade
Requires:       statistics
Requires:       stringtree-json
Requires:       struts >= 1.2.9
Requires:       sudo
Requires:       susemanager-docs_en
Requires:       system-lock-formula
Requires:       tomcat-lib >= 7
Requires:       tomcat-taglibs-standard
Requires:       woodstox
Requires:       xalan-j2 >= 2.6.0
Requires:       xerces-j2
Requires:       xmlsec
Requires:       (/sbin/unix2_chkpwd or /usr/sbin/unix2_chkpwd)
Requires:       (google-gson >= 2.2.4 with google-gson < 2.10.0)
Requires:       mvn(org.apache.tomcat:tomcat-servlet-api) > 8
Requires:       mvn(org.hibernate:hibernate-c3p0)
Requires:       mvn(org.hibernate:hibernate-core)
Requires:       mvn(org.hibernate:hibernate-ehcache)
Requires:       openssl
# libtcnative-1-0 is only recommended in tomcat.
# We want it always to prevent warnings about openssl cannot be used
Requires:       tomcat-native
Requires(pre):  salt
Requires(pre):  tomcat >= 7
Requires(pre):  uyuni-base-server

%if 0%{?rhel}
Recommends:     rng-tools
%endif

%if 0%{?run_checkstyle}
BuildRequires:  checkstyle
%endif
%if ! 0%{?omit_tests} > 0
BuildRequires:  translate-toolkit
%endif

%description
This package contains the code for the Java version of the Spacewalk Web Site.

%package config
Summary:        Configuration files for Spacewalk Java
Group:          Applications/Internet
Requires(post): %{apache2}
Requires(post): tomcat
Requires(post): salt-master

%description config
This package contains the configuration files for the Spacewalk Java web
application and taskomatic process.

%package lib
Summary:        Jar files for Spacewalk Java
Group:          Applications/Internet
Requires:       /usr/bin/sudo

%description lib
This package contains the jar files for the Spacewalk Java web application
and taskomatic process.

%package postgresql
Summary:        PostgreSQL database backend support files for Spacewalk Java
Group:          Applications/Internet
Requires:       postgresql-jdbc
Requires:       tomcat >= 7
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
%attr(644, tomcat, tomcat) %{serverdir}/tomcat/webapps/rhn/WEB-INF/lib/commons-lang3.jar
%endif

%package apidoc-sources
Summary:        Autogenerated apidoc sources for spacewalk-java
Group:          Applications/Internet

BuildRequires:  (docbook-dtds or docbook_4)

%description apidoc-sources
This package contains apidoc sources of spacewalk-java.

%files apidoc-sources
%defattr(644,root,root,775)
%docdir %{_defaultdocdir}/%{name}
%dir %{_defaultdocdir}/%{name}
%dir %{_defaultdocdir}/%{name}/xml
%{_defaultdocdir}/%{name}/xml/susemanager_api_doc.xml
%{_defaultdocdir}/%{name}/asciidoc/

%package -n spacewalk-taskomatic
Summary:        Java version of taskomatic
Group:          Applications/Internet

BuildRequires:  systemd
%if 0%{?rhel}
BuildRequires:  systemd-rpm-macros
%else
%{?systemd_requires}
%endif

Requires:       %{ehcache}
Requires:       apache-commons-cli
Requires:       apache-commons-codec
Requires:       apache-commons-lang3
Requires:       apache-commons-logging
Requires:       bcel
Requires:       mvn(net.bytebuddy:byte-buddy) >= 1.14
Requires:       mvn(net.bytebuddy:byte-buddy-dep) >= 1.14
Requires:       c3p0 >= 0.9.1
Requires:       cglib
Requires:       classmate
Requires:       cobbler
Requires:       concurrent
Requires:       hibernate-commons-annotations
Requires:       httpcomponents-client
Requires:       httpcomponents-core
Requires:       java-%{java_version}-openjdk
Requires:       javassist
Requires:       jboss-logging
Requires:       jpa-api
Requires:       jsch
Requires:       log4j
Requires:       log4j-jcl
Requires:       quartz
Requires:       simple-core
Requires:       spacewalk-java-config
Requires:       spacewalk-java-jdbc
Requires:       spacewalk-java-lib = %{version}
Requires:       statistics
Requires:       susemanager-frontend-libs >= 2.1.5
Requires:       tomcat-taglibs-standard
Requires:       xalan-j2 >= 2.6.0
Requires:       xerces-j2
Requires:       (/sbin/unix2_chkpwd or /usr/sbin/unix2_chkpwd)
Requires:       mvn(org.hibernate:hibernate-c3p0)
Requires:       mvn(org.hibernate:hibernate-core)
Requires:       mvn(org.hibernate:hibernate-ehcache)

Conflicts:      quartz < 2.0

%description -n spacewalk-taskomatic
This package contains the Java version of taskomatic.

%prep
%setup -q

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

# Adapt Apache service name in taskomatic.service
sed -i 's/apache2.service/%{apache2}.service/' scripts/taskomatic.service

%build
PRODUCT_NAME="SUSE Manager"
%if !0%{?sle_version} || 0%{?is_opensuse} || 0%{?rhel} || 0%{?fedora}
PRODUCT_NAME="Uyuni"
%endif

%if 0%{?rhel}
export JAVA_HOME=/usr/lib/jvm/java-%{java_version}-openjdk/
%endif

# compile only java sources (no packing here)
ant -Dprefix=$RPM_BUILD_ROOT -Dproduct.name="'$PRODUCT_NAME'" init-install compile

%if 0%{?run_checkstyle}
echo "Running checkstyle on java main sources"
export CLASSPATH="build/classes:build/build-lib/*"
export ADDITIONAL_OPTIONS="-Djavadoc.method.scope=public \
-Djavadoc.type.scope=package \
-Djavadoc.var.scope=package \
-Dcheckstyle.cache.file=build/checkstyle.cache.src \
-Djavadoc.lazy=false \
-Dcheckstyle.header.file=buildconf/LICENSE.txt"
find . -name *.java | grep -vE '(/test/|/jsp/)' | \
xargs checkstyle -c buildconf/checkstyle.xml

echo "Running checkstyle on java test sources"
export ADDITIONAL_OPTIONS="-Djavadoc.method.scope=nothing \
-Djavadoc.type.scope=nothing \
-Djavadoc.var.scope=nothing \
-Dcheckstyle.cache.file=build/checkstyle.cache.test \
-Djavadoc.lazy=false \
-Dcheckstyle.header.file=buildconf/LICENSE.txt"
find . -name *.java | grep -E '/test/' | grep -vE '/jsp/' | \
xargs checkstyle -c buildconf/checkstyle.xml
%endif

# catch macro name errors
find . -type f -name '*.xml' | xargs perl -CSAD -lne '
          for (grep { $_ ne "PRODUCT_NAME" && $_ ne "VENDOR_NAME" && $_ ne "ENTERPRISE_LINUX_NAME" && $_ ne "VENDOR_SERVICE_NAME" } /\@\@(\w+)\@\@/g) {
              print;
              $exit = 1;
          }
          @r = /((..)?(PRODUCT_NAME|VENDOR_NAME|ENTERPRISE_LINUX_NAME|VENDOR_SERVICE_NAME)(..)?)/g ;
          while (@r) {
              $s = shift(@r); $f = shift(@r); $skip = shift(@r); $l = shift(@r);
              if ($f ne "@@" or $l ne "@@") {
                  print $s;
                  $exit = 1;
              }
          }
          END { exit $exit }'

echo "Building apidoc docbook sources"
ant -Dproduct.name="'$PRODUCT_NAME'" -Dprefix=$RPM_BUILD_ROOT init-install apidoc-docbook
pushd build/reports/apidocs/docbook
/usr/bin/xmllint --xinclude --postvalid book.xml > susemanager_api_doc.xml
popd

echo "Building apidoc asciidoc sources"
ant -Dproduct.name="'$PRODUCT_NAME'" -Dprefix=$RPM_BUILD_ROOT init-install apidoc-asciidoc

# Don't use Java module com.sun.xml.bind if it isn't available. (only SUSE has it)
if [[ ! `java --list-modules | grep com.sun.xml.bind` ]]; then
    sed -i 's/--add-modules java.annotation,com.sun.xml.bind//' conf/default/rhn_taskomatic_daemon.conf
fi

%install
PRODUCT_NAME="SUSE Manager"
%if !0%{?sle_version} || 0%{?is_opensuse} || 0%{?rhel} || 0%{?fedora}
PRODUCT_NAME="Uyuni"
%endif

%if 0%{?rhel}
export JAVA_HOME=/usr/lib/jvm/java-%{java_version}-openjdk/
%endif

export NO_BRP_STALE_LINK_ERROR=yes

mkdir -p $RPM_BUILD_ROOT%{serverdir}/tomcat/webapps/rhn/WEB-INF/lib
%if 0%{?suse_version}
ant -Dproduct.name="'$PRODUCT_NAME'" -Dprefix=$RPM_BUILD_ROOT -Dtomcat="tomcat9" install-tomcat9-suse
install -d -m 755 $RPM_BUILD_ROOT%{serverdir}/tomcat/webapps/rhn/META-INF/
install -m 755 conf/rhn-tomcat9.xml $RPM_BUILD_ROOT%{serverdir}/tomcat/webapps/rhn/META-INF/context.xml
%else
ant -Dproduct.name="'$PRODUCT_NAME'" -Dprefix=$RPM_BUILD_ROOT install-tomcat
install -d -m 755 $RPM_BUILD_ROOT%{_sysconfdir}/tomcat/Catalina/localhost/
install -m 644 conf/rhn-tomcat9.xml $RPM_BUILD_ROOT%{_sysconfdir}/tomcat/Catalina/localhost/rhn.xml
%endif

# check spelling errors in all resources for English if aspell installed
[ -x "$(which aspell)" ] && scripts/spelling/check_java.sh .. en_US

install -d -m 755 $RPM_BUILD_ROOT%{_sbindir}
install -d -m 755 $RPM_BUILD_ROOT%{_unitdir}
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
install -d -m 755 $RPM_BUILD_ROOT/%{_localstatedir}/lib/spacewalk/scc
install -d -m 755 $RPM_BUILD_ROOT/%{_localstatedir}/lib/spacewalk/subscription-matcher

install -d -m 755 $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d
install -d $RPM_BUILD_ROOT%{userserverdir}/susemanager/salt
install -d $RPM_BUILD_ROOT%{userserverdir}/susemanager/salt/salt_ssh
install -d $RPM_BUILD_ROOT%{userserverdir}/susemanager/tmp

install -m 644 conf/default/rhn_hibernate.conf $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults/rhn_hibernate.conf
install -m 644 conf/default/rhn_reporting_hibernate.conf $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults/rhn_reporting_hibernate.conf
install -m 644 conf/default/rhn_taskomatic_daemon.conf $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults/rhn_taskomatic_daemon.conf
install -m 644 conf/default/taskomatic.conf $RPM_BUILD_ROOT%{_sysconfdir}/rhn/taskomatic.conf
install -m 644 conf/default/rhn_org_quartz.conf $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults/rhn_org_quartz.conf
install -m 644 conf/rhn_java.conf $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults
install -m 644 conf/rhn_java_sso.conf $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults

# Adjust product tree tag
%if 0%{?is_opensuse}
sed -i -e 's/^java.product_tree_tag =.*$/java.product_tree_tag = Uyuni/' $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults/rhn_java.conf
%endif
# Adjust languages
sed -i -e '/# NOTE: for the RPMs this is defined at the SPEC!/d' $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults/rhn_java.conf
sed -i -e 's/^java.supported_locales=.*$/java.supported_locales=%{supported_locales}/' $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults/rhn_java.conf
install -m 644 conf/logrotate/rhn_web_api $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d/rhn_web_api
install -m 644 conf/logrotate/gatherer $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d/gatherer
# LOGROTATE >= 3.8 requires extra permission config
sed -i 's/#LOGROTATE-3.8#//' $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d/rhn_web_api
install -m 755 scripts/taskomatic $RPM_BUILD_ROOT%{_sbindir}
install -m 644 scripts/taskomatic.service $RPM_BUILD_ROOT%{_unitdir}
# add rc link
ln -sf service $RPM_BUILD_ROOT/%{_sbindir}/rctaskomatic

install -m 644 scripts/unittest.xml $RPM_BUILD_ROOT/%{_datadir}/rhn/
install -m 644 build/webapp/rhnjava/WEB-INF/lib/rhn.jar $RPM_BUILD_ROOT%{_datadir}/rhn/lib
%if ! 0%{?omit_tests} > 0
install -m 644 build/webapp/rhnjava/WEB-INF/lib/rhn-test.jar $RPM_BUILD_ROOT%{_datadir}/rhn/lib
cp -a build/classes/com/redhat/rhn/common/conf/test/conf $RPM_BUILD_ROOT%{_datadir}/rhn/unit-tests/
%endif
install -m 644 conf/log4j2.xml.taskomatic $RPM_BUILD_ROOT%{_datadir}/rhn/classes/log4j2.xml
install -m 644 code/src/ehcache.xml $RPM_BUILD_ROOT%{_datadir}/rhn/classes/ehcache.xml

install -d -m 755 $RPM_BUILD_ROOT%{spacewalksnippetsdir}
install -m 644 conf/cobbler/snippets/default_motd  $RPM_BUILD_ROOT%{spacewalksnippetsdir}/default_motd
install -m 644 conf/cobbler/snippets/keep_system_id  $RPM_BUILD_ROOT%{spacewalksnippetsdir}/keep_system_id
install -m 644 conf/cobbler/snippets/post_reactivation_key  $RPM_BUILD_ROOT%{spacewalksnippetsdir}/post_reactivation_key
install -m 644 conf/cobbler/snippets/post_delete_system  $RPM_BUILD_ROOT%{spacewalksnippetsdir}/post_delete_system
install -m 644 conf/cobbler/snippets/redhat_register_using_salt    $RPM_BUILD_ROOT%{spacewalksnippetsdir}/redhat_register_using_salt
install -m 644 conf/cobbler/snippets/minion_script    $RPM_BUILD_ROOT%{spacewalksnippetsdir}/minion_script
install -m 644 conf/cobbler/snippets/sles_no_signature_checks $RPM_BUILD_ROOT%{spacewalksnippetsdir}/sles_no_signature_checks
install -m 644 conf/cobbler/snippets/wait_for_networkmanager_script $RPM_BUILD_ROOT%{spacewalksnippetsdir}/wait_for_networkmanager_script

ln -s -f %{_javadir}/dwr.jar $RPM_BUILD_ROOT%{serverdir}/tomcat/webapps/rhn/WEB-INF/lib/dwr.jar

# special links for rhn-search
RHN_SEARCH_BUILD_DIR=%{_prefix}/share/rhn/search/lib
ln -s -f %{_javadir}/postgresql-jdbc.jar $RPM_BUILD_ROOT$RHN_SEARCH_BUILD_DIR/postgresql-jdbc.jar
ln -s -f %{_javadir}/ongres-scram/client.jar $RPM_BUILD_ROOT$RHN_SEARCH_BUILD_DIR/ongres-scram_client.jar
ln -s -f %{_javadir}/ongres-scram/common.jar $RPM_BUILD_ROOT$RHN_SEARCH_BUILD_DIR/ongres-scram_common.jar

# write an include file for the filelist
if [ -e %{_javadir}/ongres-stringprep/stringprep.jar ]; then
    ln -s -f %{_javadir}/ongres-stringprep/stringprep.jar $RPM_BUILD_ROOT$RHN_SEARCH_BUILD_DIR/ongres-stringprep_stringprep.jar
    ln -s -f %{_javadir}/ongres-stringprep/saslprep.jar $RPM_BUILD_ROOT$RHN_SEARCH_BUILD_DIR/ongres-stringprep_saslprep.jar
    echo "
%{serverdir}/tomcat/webapps/rhn/WEB-INF/lib/ongres-scram_client.jar
%{serverdir}/tomcat/webapps/rhn/WEB-INF/lib/ongres-scram_common.jar
%{serverdir}/tomcat/webapps/rhn/WEB-INF/lib/ongres-stringprep_stringprep.jar
%{serverdir}/tomcat/webapps/rhn/WEB-INF/lib/ongres-stringprep_saslprep.jar
%{_prefix}/share/rhn/search/lib/ongres-scram_client.jar
%{_prefix}/share/rhn/search/lib/ongres-scram_common.jar
%{_prefix}/share/rhn/search/lib/ongres-stringprep_stringprep.jar
%{_prefix}/share/rhn/search/lib/ongres-stringprep_saslprep.jar
    " > .mfiles-postgresql
else
    echo "
%{serverdir}/tomcat/webapps/rhn/WEB-INF/lib/ongres-scram_client.jar
%{serverdir}/tomcat/webapps/rhn/WEB-INF/lib/ongres-scram_common.jar
%{_prefix}/share/rhn/search/lib/ongres-scram_client.jar
%{_prefix}/share/rhn/search/lib/ongres-scram_common.jar
    " > .mfiles-postgresql
fi

# install apidoc sources
mkdir -p $RPM_BUILD_ROOT%{_docdir}/%{name}/xml
install -m 644 build/reports/apidocs/docbook/susemanager_api_doc.xml $RPM_BUILD_ROOT%{_docdir}/%{name}/xml/susemanager_api_doc.xml
cp -R build/reports/apidocs/asciidoc/ $RPM_BUILD_ROOT%{_docdir}/%{name}/asciidoc/
# delete JARs which must not be deployed
rm -rf $RPM_BUILD_ROOT%{serverdir}/tomcat/webapps/rhn/WEB-INF/lib/jspapi.jar
rm -rf $RPM_BUILD_ROOT%{serverdir}/tomcat/webapps/rhn/WEB-INF/lib/jasper5-compiler.jar
rm -rf $RPM_BUILD_ROOT%{serverdir}/tomcat/webapps/rhn/WEB-INF/lib/jasper5-runtime.jar
rm -rf $RPM_BUILD_ROOT%{serverdir}/tomcat/webapps/rhn/WEB-INF/lib/tomcat*.jar
%if 0%{?omit_tests} > 0
rm -rf $RPM_BUILD_ROOT%{_datadir}/rhn/lib/rhn-test.jar
rm -rf $RPM_BUILD_ROOT/classes/com/redhat/rhn/common/conf/test/conf
rm -rf $RPM_BUILD_ROOT%{_datadir}/rhn/unittest.xml
%endif

# create log dir
mkdir -p $RPM_BUILD_ROOT%{_var}/log/rhn

# Prettifying symlinks
mv $RPM_BUILD_ROOT%{serverdir}/tomcat/webapps/rhn/WEB-INF/lib/jboss-loggingjboss-logging.jar $RPM_BUILD_ROOT%{serverdir}/tomcat/webapps/rhn/WEB-INF/lib/jboss-logging.jar

# Removing unused symlinks.
%if 0%{?rhel}
rm -rf $RPM_BUILD_ROOT%{serverdir}/tomcat/webapps/rhn/WEB-INF/lib/javamailmail.jar
%endif

# show all JAR symlinks
echo "#### SYMLINKS START ####"
find $RPM_BUILD_ROOT%{serverdir}/tomcat/webapps/rhn/WEB-INF/lib -name *.jar
echo "#### SYMLINKS END ####"

%pre -n spacewalk-taskomatic
%if !0%{?rhel}
%service_add_pre taskomatic.service
%endif

%post
%if 0%{?rhel}
echo "Trying to start optional rngd service..."
systemctl start rngd ||:
%endif

%post -n spacewalk-taskomatic
%if 0%{?rhel}
%systemd_post taskomatic.service
%else
%service_add_post taskomatic.service
%endif

%preun -n spacewalk-taskomatic
%if 0%{?rhel}
%systemd_preun taskomatic.service
%else
%service_del_preun taskomatic.service
%endif

%postun -n spacewalk-taskomatic
%if 0%{?rhel}
%systemd_postun taskomatic.service
%else
%service_del_postun taskomatic.service
%endif

%post config
if [ ! -e /var/log/rhn/rhn_web_api.log ]; then
    touch /var/log/rhn/rhn_web_api.log
fi
chown tomcat:%{apache_group} /var/log/rhn/rhn_web_api.log

if [ ! -e /var/log/rhn/gatherer.log ]; then
    touch /var/log/rhn/gatherer.log
fi
chown tomcat:%{apache_group} /var/log/rhn/gatherer.log

if [ ! -d /var/lib/salt/.ssh ]; then
    mkdir -p /var/lib/salt/.ssh
    chown %{salt_user_group}:%{salt_user_group} -R /var/lib/salt/.ssh
    chmod 700 /var/lib/salt/.ssh
fi

if [ -e /srv/susemanager/salt/salt_ssh/mgr_ssh_id ]; then
    mv /srv/susemanager/salt/salt_ssh/mgr_ssh_id /var/lib/salt/.ssh/mgr_ssh_id
    cp /srv/susemanager/salt/salt_ssh/mgr_ssh_id.pub /var/lib/salt/.ssh/mgr_ssh_id.pub
    chown %{salt_user_group}:%{salt_user_group} /var/lib/salt/.ssh/mgr_ssh_id.pub
fi

%files
%defattr(-,root,root)
%dir %{serverdir}
%dir %{_localstatedir}/lib/spacewalk
%defattr(644,tomcat,tomcat,775)
%attr(775, %{salt_user_group}, %{salt_user_group}) %dir %{userserverdir}/susemanager/salt/salt_ssh
%attr(775, tomcat, tomcat) %dir %{serverdir}/tomcat/webapps
%dir %{userserverdir}/susemanager
%dir %{userserverdir}/susemanager/salt
%attr(770, tomcat, %{salt_user_group}) %dir %{userserverdir}/susemanager/tmp
%dir %{serverdir}/tomcat/webapps/rhn/
%{serverdir}/tomcat/webapps/rhn/apidoc/
%{serverdir}/tomcat/webapps/rhn/css/
%{serverdir}/tomcat/webapps/rhn/errata/
%{serverdir}/tomcat/webapps/rhn/img/
%{serverdir}/tomcat/webapps/rhn/META-INF/
%{serverdir}/tomcat/webapps/rhn/schedule/
%{serverdir}/tomcat/webapps/rhn/systems/
%{serverdir}/tomcat/webapps/rhn/users/
%{serverdir}/tomcat/webapps/rhn/errors/
%{serverdir}/tomcat/webapps/rhn/*.jsp
%{serverdir}/tomcat/webapps/rhn/WEB-INF/classes
%{serverdir}/tomcat/webapps/rhn/WEB-INF/decorators
%{serverdir}/tomcat/webapps/rhn/WEB-INF/includes
%{serverdir}/tomcat/webapps/rhn/WEB-INF/nav
%{serverdir}/tomcat/webapps/rhn/WEB-INF/pages
%{serverdir}/tomcat/webapps/rhn/WEB-INF/*.xml


# all jars in WEB-INF/lib/
%dir %{serverdir}/tomcat
%dir %{serverdir}/tomcat/webapps
%{serverdir}/tomcat/webapps/rhn/WEB-INF/lib
%exclude %{serverdir}/tomcat/webapps/rhn/WEB-INF/lib/postgresql-jdbc.jar
%exclude %{serverdir}/tomcat/webapps/rhn/WEB-INF/lib/ongres-*.jar

# owned by cobbler needs cobbler permissions
%attr(755,root,root) %dir %{cobprofdir}
%attr(755,root,root) %dir %{cobdirsnippets}
# owned by uyuni
%dir %{cobprofdirup}
%dir %{cobprofdirwiz}
%dir %{spacewalksnippetsdir}
%config %{spacewalksnippetsdir}/default_motd
%config %{spacewalksnippetsdir}/keep_system_id
%config %{spacewalksnippetsdir}/post_reactivation_key
%config %{spacewalksnippetsdir}/post_delete_system
%config %{spacewalksnippetsdir}/redhat_register_using_salt
%config %{spacewalksnippetsdir}/minion_script
%config %{spacewalksnippetsdir}/sles_no_signature_checks
%config %{spacewalksnippetsdir}/wait_for_networkmanager_script
%if 0%{?suse_version}
%config(noreplace) %{serverdir}/tomcat/webapps/rhn/META-INF/context.xml
%else
%config(noreplace) %{_sysconfdir}/tomcat/Catalina/localhost/rhn.xml
%endif
%attr(755,root,root) %dir %{cobblerdir}

%attr(755, tomcat, root) %dir %{_localstatedir}/lib/spacewalk/scc
%attr(755, tomcat, root) %dir %{_localstatedir}/lib/spacewalk/subscription-matcher
%dir %{serverdir}/tomcat/webapps/rhn/WEB-INF

%files -n spacewalk-taskomatic
%defattr(644,root,root,775)
%attr(755, root, root) %{_sbindir}/taskomatic
%attr(644, root, root) %{_unitdir}/taskomatic.service
%{_datarootdir}/spacewalk/taskomatic
%{_sbindir}/rctaskomatic

%files config
%defattr(644,root,root,755)
%{_prefix}/share/rhn/config-defaults/rhn_hibernate.conf
%{_prefix}/share/rhn/config-defaults/rhn_reporting_hibernate.conf
%{_prefix}/share/rhn/config-defaults/rhn_taskomatic_daemon.conf
%config(noreplace) %{_sysconfdir}/rhn/taskomatic.conf
%{_prefix}/share/rhn/config-defaults/rhn_org_quartz.conf
%{_prefix}/share/rhn/config-defaults/rhn_java.conf
%{_prefix}/share/rhn/config-defaults/rhn_java_sso.conf
%config %{_sysconfdir}/logrotate.d/rhn_web_api
%config %{_sysconfdir}/logrotate.d/gatherer
%dir %{_datadir}/spacewalk
%if 0%{?rhel}
%dir %{_var}/log/rhn
%else
%attr(770,root,%{apache_group}) %dir %{_var}/log/rhn
%endif

%files lib
%defattr(644,root,root,755)
%dir %{_datadir}/rhn
%dir %{_datadir}/rhn/lib
%dir %{_datadir}/rhn/classes
%{_datadir}/rhn/classes/log4j2.xml
%{_datadir}/rhn/classes/ehcache.xml
%{_datadir}/rhn/lib/rhn.jar

%files postgresql -f .mfiles-postgresql
%defattr(644,root,root,755)
%dir %{_prefix}/share/rhn/search
%dir %{_prefix}/share/rhn/search/lib
%dir %{serverdir}
%dir %{susemanagershareddir}
%{serverdir}/tomcat/webapps/rhn/WEB-INF/lib/postgresql-jdbc.jar
%{_prefix}/share/rhn/search/lib/postgresql-jdbc.jar
%defattr(644,tomcat,tomcat,775)
%dir %{serverdir}/tomcat
%dir %{serverdir}/tomcat/webapps

%changelog
