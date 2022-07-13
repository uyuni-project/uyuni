#
# spec file for package spacewalk-java
#
# Copyright (c) 2021 SUSE LLC.
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

%if 0%{?suse_version}
%define appdir          /srv/tomcat/webapps
%define jardir          /srv/tomcat/webapps/rhn/WEB-INF/lib
%define apache_group    www
%define salt_user_group salt
%define apache2         apache2
%define java_version    11
%else
%define appdir          %{_localstatedir}/lib/tomcat/webapps
%define jardir          %{_localstatedir}/lib/tomcat/webapps/rhn/WEB-INF/lib
%define apache_group    apache
%define salt_user_group salt
%define apache2         httpd
%define java_version    1:11
%endif

%define ehcache         ( mvn(net.sf.ehcache:ehcache-core) >= 2.10.1 or ehcache-core >= 2.10.1 or ehcache >= 2.10.1)
%define apache_commons_digester    (apache-commons-digester or jakarta-commons-digester)
%define apache_commons_discovery   (apache-commons-discovery or jakarta-commons-discovery)
%define apache_commons_fileupload  (apache-commons-fileupload or jakarta-commons-fileupload)
%define apache_commons_validator   (apache-commons-validator or jakarta-commons-validator)
%define log4j                      (log4j or log4j12)


%if 0%{?is_opensuse}
%define supported_locales bn_IN,ca,de,en_US,es,fr,gu,hi,it,ja,ko,pa,pt,pt_BR,ru,ta,zh_CN,zh_TW
%else
%define supported_locales en_US,ko,ja,zh_CN
%endif

Name:           spacewalk-java
Summary:        Java web application files for Spacewalk
License:        GPL-2.0-only
Group:          Applications/Internet
Version:        4.2.40
Release:        1%{?dist}
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/uyuni-project/uyuni/archive/%{name}-%{version}-1.tar.gz
Source1:        %{name}-rpmlintrc
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
ExcludeArch:    ia64

BuildRequires:  ant
BuildRequires:  ant-apache-regexp
BuildRequires:  ant-contrib
BuildRequires:  ant-junit
%if 0%{?suse_version}
BuildRequires:  ant-nodeps
%endif
BuildRequires:  antlr >= 2.7.6
BuildRequires:  apache-commons-cli
BuildRequires:  apache-commons-codec
BuildRequires:  apache-commons-collections
BuildRequires:  apache-commons-io
BuildRequires:  apache-commons-lang3 >= 3.4
BuildRequires:  apache-commons-logging
BuildRequires:  apache-commons-jexl
BuildRequires:  bcel
BuildRequires:  byte-buddy
BuildRequires:  c3p0 >= 0.9.1
BuildRequires:  cglib
%if 0%{?suse_version}
BuildRequires:  classmate
%endif
BuildRequires:  concurrent
BuildRequires:  dom4j
BuildRequires:  dwr >= 3
BuildRequires:  %{ehcache}
BuildRequires:  google-gson >= 2.2.4
BuildRequires:  hibernate-commons-annotations
BuildRequires:  hibernate5
BuildRequires:  httpcomponents-asyncclient
BuildRequires:  httpcomponents-client
BuildRequires:  ical4j
BuildRequires:  jade4j
BuildRequires:  jaf
BuildRequires:  %{apache_commons_discovery}
BuildRequires:  apache-commons-el
BuildRequires:  %{apache_commons_fileupload}
BuildRequires:  %{apache_commons_validator}
%if 0%{?rhel} >= 8 || 0%{?fedora}
BuildRequires:  (glassfish-jaxb-api or jaxb-api)
BuildRequires:  glassfish-jaxb-core
BuildRequires:  glassfish-jaxb-runtime
BuildRequires:  glassfish-jaxb-txw2
BuildRequires:  istack-commons-runtime
BuildRequires:  java-11-openjdk-devel
BuildRequires:  maven-javadoc-plugin
%else
BuildRequires:  java-devel >= %{java_version}
%endif
BuildRequires:  java-saml
BuildRequires:  javamail
BuildRequires:  javapackages-tools
BuildRequires:  javassist
BuildRequires:  jboss-logging
BuildRequires:  jcommon
BuildRequires:  jdom
BuildRequires:  joda-time
BuildRequires:  jose4j
BuildRequires:  jpa-api
BuildRequires:  jsch
BuildRequires:  jta
BuildRequires:  libxml2
%if 0%{?rhel}
BuildRequires:  libxml2-devel
%else
BuildRequires:  libxml2-tools
%endif
BuildRequires:  %{log4j}
BuildRequires:  slf4j-log4j12
BuildRequires:  netty
BuildRequires:  objectweb-asm
BuildRequires:  perl
BuildRequires:  pgjdbc-ng
BuildRequires:  postgresql-jdbc
BuildRequires:  prometheus-client-java
BuildRequires:  quartz
BuildRequires:  redstone-xmlrpc
BuildRequires:  salt-netapi-client >= 0.19
BuildRequires:  simple-core
BuildRequires:  simple-xml
BuildRequires:  sitemesh
BuildRequires:  snakeyaml
BuildRequires:  spark-core
BuildRequires:  spark-template-jade
BuildRequires:  statistics
BuildRequires:  stringtree-json
BuildRequires:  struts >= 1.2.9
BuildRequires:  tomcat >= 7
BuildRequires:  tomcat-lib >= 7
BuildRequires:  tomcat-taglibs-standard
BuildRequires:  uyuni-base-server
BuildRequires:  velocity
BuildRequires:  woodstox
BuildRequires:  xmlsec

Requires:       (/sbin/unix2_chkpwd or /usr/sbin/unix2_chkpwd)
Requires:       apache-commons-beanutils
Requires:       apache-commons-cli
Requires:       apache-commons-codec
Requires:       apache-commons-collections
Requires:       apache-commons-io
Requires:       apache-commons-lang3
Requires:       apache-commons-logging
Requires:       apache-commons-jexl
Requires:       bcel
Requires:       byte-buddy
Requires:       c3p0 >= 0.9.1
Requires:       cglib
%if 0%{?suse_version}
Requires:       classmate
%endif
Requires:       cobbler >= 3.0.0
Requires:       concurrent
Requires:       dwr >= 3
Requires:       %{ehcache}
Requires:       (jaf or gnu-jaf)
%if 0%{?rhel} || 0%{?fedora}
Requires:       (glassfish-jaxb-api or jaxb-api)
Requires:       glassfish-jaxb-core
Requires:       glassfish-jaxb-runtime
Requires:       glassfish-jaxb-txw2
Requires:       istack-commons-runtime
%endif
Requires:       google-gson >= 2.2.4
Requires:       hibernate-commons-annotations
Requires:       hibernate5
Requires:       httpcomponents-client
Requires:       ical4j
Requires:       jade4j
Requires:       %{apache_commons_digester}
%if 0%{?rhel} >= 8
Requires:       java-11-openjdk
%else
Requires:       java >= %{java_version}
%endif
Requires:       java-saml
Requires:       javamail
Requires:       javapackages-tools
Requires:       javassist
Requires:       jboss-logging
Requires:       joda-time
Requires:       jose4j
Requires:       jpa-api
Requires:       mgr-libmod
Requires:       netty
Requires:       objectweb-asm
Requires:       pgjdbc-ng
Requires:       prometheus-client-java
Requires:       salt-netapi-client >= 0.19
Requires:       snakeyaml
Requires:       spark-core
Requires:       spark-template-jade
Requires:       statistics
Requires:       system-lock-formula
Requires:       uyuni-cluster-provider-caasp
Requires:       sudo
Requires:       susemanager-docs_en
Requires:       tomcat-taglibs-standard
Requires(pre):  uyuni-base-server
Requires:       %{apache_commons_discovery}
Requires:       apache-commons-el
Requires:       %{apache_commons_fileupload}
Requires:       jcommon
Requires:       jdom
Requires:       jta
Requires:       %{log4j}
Requires:       slf4j-log4j12
Requires:       redstone-xmlrpc
Requires:       simple-core
Requires:       simple-xml
Requires:       sitemesh
Requires:       spacewalk-branding
Requires:       spacewalk-java-config
Requires:       spacewalk-java-jdbc
Requires:       spacewalk-java-lib = %{version}
Requires:       stringtree-json
Requires:       struts >= 1.2.9
Requires:       woodstox
Requires:       xalan-j2 >= 2.6.0
Requires:       xerces-j2
Requires:       xmlsec
Requires(pre):  tomcat >= 7
Requires:       tomcat-lib >= 7
%if 0%{?suse_version}
Requires:       mvn(org.apache.tomcat:tomcat-servlet-api) > 8
%else
Requires:       servlet >= 3.0
%endif
Requires(pre):  salt

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
Requires(post): %{apache2}
Requires(post): tomcat
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
%attr(644, tomcat, tomcat) %{jardir}/commons-lang3.jar
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

Requires:       (/sbin/unix2_chkpwd or /usr/sbin/unix2_chkpwd)
Requires:       apache-commons-cli
Requires:       apache-commons-codec
Requires:       apache-commons-lang3
Requires:       apache-commons-logging
Requires:       bcel
Requires:       byte-buddy
Requires:       c3p0 >= 0.9.1
Requires:       cglib
%if 0%{?suse_version}
Requires:       classmate
%endif
Requires:       cobbler >= 3.0.0
Requires:       concurrent
Requires:       %{ehcache}
Requires:       hibernate-commons-annotations
Requires:       hibernate5
Requires:       httpcomponents-client
Requires:       httpcomponents-core
%if 0%{?rhel} >= 8
Requires:	java-11-openjdk
%else
Requires:       java >= %{java_version}
%endif
Requires:       javassist
Requires:       jboss-logging
Requires:       jcommon
Requires:       jpa-api
Requires:       jsch
Requires:       %{log4j}
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
Conflicts:      quartz < 2.0
Obsoletes:      taskomatic < 5.3.0
Obsoletes:      taskomatic-sat < 5.3.0
Provides:       taskomatic = %{version}-%{release}
Provides:       taskomatic-sat = %{version}-%{release}
BuildRequires:  systemd
%if 0%{?rhel}
BuildRequires:  systemd-rpm-macros
%else
%{?systemd_requires}
%endif

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
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk/
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
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk/
%endif

export NO_BRP_STALE_LINK_ERROR=yes

%if 0%{?suse_version}
ant -Dproduct.name="'$PRODUCT_NAME'" -Dprefix=$RPM_BUILD_ROOT -Dtomcat="tomcat9" install-tomcat9-suse
install -d -m 755 $RPM_BUILD_ROOT%{appdir}/rhn/META-INF/
install -m 755 conf/rhn-tomcat9.xml $RPM_BUILD_ROOT%{appdir}/rhn/META-INF/context.xml
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
install -d $RPM_BUILD_ROOT/srv/susemanager/salt
install -d $RPM_BUILD_ROOT/srv/susemanager/salt/salt_ssh
install -d $RPM_BUILD_ROOT/srv/susemanager/salt/salt_ssh/temp_bootstrap_keys
install -d -m 775 $RPM_BUILD_ROOT/srv/susemanager/pillar_data
install -d -m 775 $RPM_BUILD_ROOT/srv/susemanager/pillar_data/images
install -d $RPM_BUILD_ROOT/srv/susemanager/formula_data
install -d $RPM_BUILD_ROOT/srv/susemanager/tmp

install -m 644 conf/default/rhn_hibernate.conf $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults/rhn_hibernate.conf
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
install -m 644 conf/log4j.properties.taskomatic $RPM_BUILD_ROOT%{_datadir}/rhn/classes/log4j.properties
install -m 644 code/src/ehcache.xml $RPM_BUILD_ROOT%{_datadir}/rhn/classes/ehcache.xml

install -d -m 755 $RPM_BUILD_ROOT%{spacewalksnippetsdir}
install -m 644 conf/cobbler/snippets/default_motd  $RPM_BUILD_ROOT%{spacewalksnippetsdir}/default_motd
install -m 644 conf/cobbler/snippets/keep_system_id  $RPM_BUILD_ROOT%{spacewalksnippetsdir}/keep_system_id
install -m 644 conf/cobbler/snippets/post_reactivation_key  $RPM_BUILD_ROOT%{spacewalksnippetsdir}/post_reactivation_key
install -m 644 conf/cobbler/snippets/post_delete_system  $RPM_BUILD_ROOT%{spacewalksnippetsdir}/post_delete_system
install -m 644 conf/cobbler/snippets/redhat_register  $RPM_BUILD_ROOT%{spacewalksnippetsdir}/redhat_register
install -m 644 conf/cobbler/snippets/redhat_register_using_salt    $RPM_BUILD_ROOT%{spacewalksnippetsdir}/redhat_register_using_salt
install -m 644 conf/cobbler/snippets/minion_script    $RPM_BUILD_ROOT%{spacewalksnippetsdir}/minion_script
install -m 644 conf/cobbler/snippets/sles_register    $RPM_BUILD_ROOT%{spacewalksnippetsdir}/sles_register
install -m 644 conf/cobbler/snippets/sles_register_script $RPM_BUILD_ROOT%{spacewalksnippetsdir}/sles_register_script
install -m 644 conf/cobbler/snippets/sles_no_signature_checks $RPM_BUILD_ROOT%{spacewalksnippetsdir}/sles_no_signature_checks
install -m 644 conf/cobbler/snippets/wait_for_networkmanager_script $RPM_BUILD_ROOT%{spacewalksnippetsdir}/wait_for_networkmanager_script

ln -s -f %{_javadir}/dwr.jar $RPM_BUILD_ROOT%{jardir}/dwr.jar

# special links for taskomatic
TASKOMATIC_BUILD_DIR=%{_prefix}/share/spacewalk/taskomatic
rm -f $RPM_BUILD_ROOT$TASKOMATIC_BUILD_DIR/slf4j*nop.jar
rm -f $RPM_BUILD_ROOT$TASKOMATIC_BUILD_DIR/slf4j*simple.jar

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
%{jardir}/tomcat/webapps/rhn/WEB-INF/lib/ongres-stringprep_stringprep.jar
%{jardir}/tomcat/webapps/rhn/WEB-INF/lib/ongres-stringprep_saslprep.jar
%{_prefix}/share/rhn/search/lib/ongres-stringprep_stringprep.jar
%{_prefix}/share/rhn/search/lib/ongres-stringprep_saslprep.jar
    " > .mfiles-postgresql
else
    touch .mfiles-postgresql
fi


# install apidoc sources
mkdir -p $RPM_BUILD_ROOT%{_docdir}/%{name}/xml
install -m 644 build/reports/apidocs/docbook/susemanager_api_doc.xml $RPM_BUILD_ROOT%{_docdir}/%{name}/xml/susemanager_api_doc.xml
cp -R build/reports/apidocs/asciidoc/ $RPM_BUILD_ROOT%{_docdir}/%{name}/asciidoc/

# delete JARs which must not be deployed
rm -rf $RPM_BUILD_ROOT%{jardir}/jspapi.jar
rm -rf $RPM_BUILD_ROOT%{jardir}/jasper5-compiler.jar
rm -rf $RPM_BUILD_ROOT%{jardir}/jasper5-runtime.jar
rm -rf $RPM_BUILD_ROOT%{jardir}/tomcat*.jar
rm -rf $RPM_BUILD_ROOT%{jardir}/google-gson_google-gsongson-extras.jar
%if 0%{?omit_tests} > 0
rm -rf $RPM_BUILD_ROOT%{_datadir}/rhn/lib/rhn-test.jar
rm -rf $RPM_BUILD_ROOT/classes/com/redhat/rhn/common/conf/test/conf
rm -rf $RPM_BUILD_ROOT%{_datadir}/rhn/unittest.xml
%endif

# Prettifying symlinks
mv $RPM_BUILD_ROOT%{jardir}/jboss-loggingjboss-logging.jar $RPM_BUILD_ROOT%{jardir}/jboss-logging.jar

# Prettifying symlinks for RHEL
%if 0%{?rhel}
mv $RPM_BUILD_ROOT%{jardir}/jafjakarta.activation.jar $RPM_BUILD_ROOT%{jardir}/jaf.jar
mv $RPM_BUILD_ROOT%{jardir}/javamailjavax.mail.jar $RPM_BUILD_ROOT%{jardir}/javamail.jar
mv $RPM_BUILD_ROOT%{jardir}/jta.jar $RPM_BUILD_ROOT%{jardir}/geronimo-jta-1.1-api.jar
# Removing unused symlinks.
rm -rf $RPM_BUILD_ROOT%{jardir}/jafjakarta.activation-api.jar
rm -rf $RPM_BUILD_ROOT%{jardir}/javamaildsn.jar
rm -rf $RPM_BUILD_ROOT%{jardir}/javamailgimap.jar
rm -rf $RPM_BUILD_ROOT%{jardir}/javamailimap.jar
rm -rf $RPM_BUILD_ROOT%{jardir}/javamailjavax.mail-api.jar
rm -rf $RPM_BUILD_ROOT%{jardir}/javamailmail.jar
rm -rf $RPM_BUILD_ROOT%{jardir}/javamailmailapi.jar
rm -rf $RPM_BUILD_ROOT%{jardir}/javamailpop3.jar
rm -rf $RPM_BUILD_ROOT%{jardir}/javamailsmtp.jar
%endif

# show all JAR symlinks
echo "#### SYMLINKS START ####"
find $RPM_BUILD_ROOT%{jardir} -name *.jar
echo "#### SYMLINKS END ####"

%pre -n spacewalk-taskomatic
%if !0%{?rhel}
%service_add_pre taskomatic.service
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
if [ ! -d /var/log/rhn ]; then
    mkdir /var/log/rhn
    chown root:%{apache_group} /var/log/rhn
    chmod 770 /var/log/rhn
fi
if [ ! -e /var/log/rhn/rhn_web_api.log ]; then
    touch /var/log/rhn/rhn_web_api.log
fi
chown tomcat:%{apache_group} /var/log/rhn/rhn_web_api.log

if [ ! -e /var/log/rhn/gatherer.log ]; then
    touch /var/log/rhn/gatherer.log
fi
chown tomcat:%{apache_group} /var/log/rhn/gatherer.log


%files
%defattr(-,root,root)
%dir %{_localstatedir}/lib/spacewalk
%defattr(644,tomcat,tomcat,775)
%attr(775, %{salt_user_group}, %{salt_user_group}) %dir /srv/susemanager/salt/salt_ssh
%attr(775, %{salt_user_group}, %{salt_user_group}) %dir /srv/susemanager/salt/salt_ssh/temp_bootstrap_keys
%attr(775, root, tomcat) %dir %{appdir}
%dir /srv/susemanager
%dir /srv/susemanager/salt
%attr(775,tomcat,susemanager) %dir /srv/susemanager/pillar_data
%attr(775,tomcat,susemanager) %dir /srv/susemanager/pillar_data/images
%dir /srv/susemanager/formula_data
%attr(750, tomcat, %{salt_user_group}) %dir /srv/susemanager/tmp
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
%if 0%{?fedora} || 0%{?sle_version} >= 150200 || 0%{?rhel}
%{jardir}/cglib_cglib.jar
%else
%{jardir}/cglib.jar
%endif
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

%{jardir}/snakeyaml.jar
# SUSE extra runtime dependencies: spark, jade4j, salt API client + dependencies
%{jardir}/apache-commons-jexl_commons-jexl.jar
%{jardir}/commons-lang3.jar
%{jardir}/google-gson_google-gsongson.jar
%{jardir}/httpcomponents_httpclient.jar
%{jardir}/httpcomponents_httpcore.jar
%{jardir}/httpcomponents_httpcore-nio.jar
%{jardir}/httpasyncclient.jar
%{jardir}/ical4j.jar
%{jardir}/jade4j.jar
%{jardir}/jose4j.jar
%{jardir}/netty*.jar
%{jardir}/salt-netapi-client.jar
%{jardir}/slf4j_api.jar
%{jardir}/slf4j_log4j12*.jar
%{jardir}/spark-core.jar
%{jardir}/spark-template-jade.jar
%{jardir}/spy.jar
%{jardir}/simpleclient*.jar
%{jardir}/pgjdbc-ng.jar
%{jardir}/java-saml-core.jar
%{jardir}/java-saml.jar
%{jardir}/joda-time.jar
%{jardir}/stax-api.jar
%{jardir}/stax2-api.jar
%{jardir}/woodstox-core-asl.jar
%{jardir}/xmlsec.jar
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

%{jardir}/jaf.jar
%if 0%{?sle_version} >= 150200
%{jardir}/javax.mail.jar
%else
%{jardir}/javamail.jar
%endif
%{jardir}/jcommon*.jar
%{jardir}/jdom.jar
%{jardir}/geronimo-jta-1.1-api.jar
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

%{jardir}/struts.jar
%{jardir}/objectweb-asm_asm.jar
%{jardir}/taglibs-standard-impl.jar
%{jardir}/taglibs-standard-jstlel.jar
%{jardir}/taglibs-standard-spec.jar
%if !0%{?suse_version}
%{jardir}/glassfish-jaxb_jaxb-core.jar
%{jardir}/glassfish-jaxb_jaxb-runtime.jar
%{jardir}/glassfish-jaxb_txw2.jar
%{jardir}/istack-commons-runtime.jar
%{jardir}/jaxb-api.jar
%endif

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
%config %{spacewalksnippetsdir}/redhat_register
%config %{spacewalksnippetsdir}/redhat_register_using_salt
%config %{spacewalksnippetsdir}/minion_script
%config %{spacewalksnippetsdir}/sles_register
%config %{spacewalksnippetsdir}/sles_register_script
%config %{spacewalksnippetsdir}/sles_no_signature_checks
%config %{spacewalksnippetsdir}/wait_for_networkmanager_script
%if 0%{?suse_version}
%config(noreplace) %{appdir}/rhn/META-INF/context.xml
%else
%config(noreplace) %{_sysconfdir}/tomcat/Catalina/localhost/rhn.xml
%endif
%attr(755,root,root) %dir %{cobblerdir}

%attr(755, tomcat, root) %dir %{_localstatedir}/lib/spacewalk/scc
%attr(755, tomcat, root) %dir %{_localstatedir}/lib/spacewalk/subscription-matcher
%dir %{appdir}/rhn/WEB-INF
%dir %{jardir}

%files -n spacewalk-taskomatic
%defattr(644,root,root,775)
%attr(755, root, root) %{_sbindir}/taskomatic
%attr(644, root, root) %{_unitdir}/taskomatic.service
%{_datarootdir}/spacewalk/taskomatic
%{_sbindir}/rctaskomatic

%files config
%defattr(644,root,root,755)
%{_prefix}/share/rhn/config-defaults/rhn_hibernate.conf
%{_prefix}/share/rhn/config-defaults/rhn_taskomatic_daemon.conf
%config(noreplace) %{_sysconfdir}/rhn/taskomatic.conf
%{_prefix}/share/rhn/config-defaults/rhn_org_quartz.conf
%{_prefix}/share/rhn/config-defaults/rhn_java.conf
%{_prefix}/share/rhn/config-defaults/rhn_java_sso.conf
%config %{_sysconfdir}/logrotate.d/rhn_web_api
%config %{_sysconfdir}/logrotate.d/gatherer
%dir %{_datadir}/spacewalk

%files lib
%defattr(644,root,root,755)
%dir %{_datadir}/rhn
%dir %{_datadir}/rhn/lib
%dir %{_datadir}/rhn/classes
%{_datadir}/rhn/classes/log4j.properties
%{_datadir}/rhn/classes/ehcache.xml
%{_datadir}/rhn/lib/rhn.jar

%files postgresql -f .mfiles-postgresql
%defattr(644,root,root,755)
%dir %{_prefix}/share/rhn/search
%dir %{_prefix}/share/rhn/search/lib
%{jardir}/postgresql-jdbc.jar
%{jardir}/ongres-scram_client.jar
%{jardir}/ongres-scram_common.jar
%{_prefix}/share/rhn/search/lib/postgresql-jdbc.jar
%{_prefix}/share/rhn/search/lib/ongres-scram_client.jar
%{_prefix}/share/rhn/search/lib/ongres-scram_common.jar

%changelog
