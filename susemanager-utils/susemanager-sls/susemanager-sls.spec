#
# spec file for package susemanager-sls
#
# Copyright (c) 2021 SUSE LLC
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


%if 0%{?suse_version} > 1320 || 0%{?rhel}
# SLE15 builds on Python 3
%global build_py3   1
%endif

%if 0%{?suse_version}
%global wwwdocroot /srv/www/htdocs
%else
%global wwwdocroot %{_var}/www/html
%endif


Name:           susemanager-sls
Version:        4.3.2
Release:        1
Summary:        Static Salt state files for SUSE Manager
License:        Apache-2.0 AND LGPL-2.1-only
Group:          Applications/Internet
Source:         %{name}-%{version}.tar.gz
Requires(pre):  coreutils
Requires:       susemanager-build-keys-web >= 12.0.1
%if 0%{?build_py3}
BuildRequires:  python3-mock
BuildRequires:  python3-pytest
BuildRequires:  python3-salt
# Different package names for SUSE and RHEL:
Requires:       (python3-PyYAML >= 5.1 or python3-pyyaml >= 5.1)
%else
BuildRequires:  python-mock
BuildRequires:  python-pytest
BuildRequires:  python-salt
Requires:       python-PyYAML >= 5.1
%endif
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch

%description
Static Salt state files for SUSE Manager, where generic operations are
provided for the integration between infrastructure components.

%package -n uyuni-config-modules
Summary:        Salt modules to configure a Server
Group:          Applications/Internet

%description -n uyuni-config-modules
This package contains Salt execution and state modules that can be used
to configure a SUSE Manager or Uyuni Server.

%prep
%setup -q

%build

%install
mkdir -p %{buildroot}/usr/share/susemanager/salt/_grains
mkdir -p %{buildroot}/usr/share/susemanager/salt/_beacons
mkdir -p %{buildroot}/usr/share/susemanager/salt/_modules
mkdir -p %{buildroot}/usr/share/susemanager/salt/_states
mkdir -p %{buildroot}/usr/share/susemanager/modules/pillar
mkdir -p %{buildroot}/usr/share/susemanager/modules/tops
mkdir -p %{buildroot}/usr/share/susemanager/modules/runners
mkdir -p %{buildroot}/usr/share/susemanager/modules/engines
mkdir -p %{buildroot}/usr/share/susemanager/pillar_data
mkdir -p %{buildroot}/usr/share/susemanager/formulas
mkdir -p %{buildroot}/usr/share/susemanager/formulas/metadata
mkdir -p %{buildroot}/usr/share/susemanager/reactor
mkdir -p %{buildroot}/usr/share/susemanager/scap
mkdir -p %{buildroot}/srv/formula_metadata
cp -R salt/* %{buildroot}/usr/share/susemanager/salt
cp -R modules/pillar/* %{buildroot}/usr/share/susemanager/modules/pillar
cp -R modules/tops/* %{buildroot}/usr/share/susemanager/modules/tops
cp -R modules/runners/* %{buildroot}/usr/share/susemanager/modules/runners
cp -R modules/engines/* %{buildroot}/usr/share/susemanager/modules/engines
cp -R pillar_data/* %{buildroot}/usr/share/susemanager/pillar_data
cp -R formulas/* %{buildroot}/usr/share/susemanager/formulas
cp -R formula_metadata/* %{buildroot}/srv/formula_metadata
cp -R reactor/* %{buildroot}/usr/share/susemanager/reactor
cp -R scap/* %{buildroot}/usr/share/susemanager/scap

# Manually install Python part to already prepared structure
cp src/beacons/pkgset.py %{buildroot}/usr/share/susemanager/salt/_beacons
cp src/grains/*.py %{buildroot}/usr/share/susemanager/salt/_grains/
rm %{buildroot}/usr/share/susemanager/salt/_grains/__init__.py
cp src/modules/*.py %{buildroot}/usr/share/susemanager/salt/_modules
rm %{buildroot}/usr/share/susemanager/salt/_modules/__init__.py
cp src/states/*.py %{buildroot}/usr/share/susemanager/salt/_states
rm %{buildroot}/usr/share/susemanager/salt/_states/__init__.py

# Install doc, examples
mkdir -p %{buildroot}/usr/share/doc/packages/uyuni-config-modules/examples/ldap
cp src/doc/* %{buildroot}/usr/share/doc/packages/uyuni-config-modules/
cp src/examples/uyuni_config_hardcode.sls %{buildroot}/usr/share/doc/packages/uyuni-config-modules/examples
cp src/examples/ldap/* %{buildroot}/usr/share/doc/packages/uyuni-config-modules/examples/ldap

%check
cd test
# Run py.test-3 for rhel
py.test%{?rhel:-3} test_pillar_suma_minion.py
cd ../src/tests
py.test%{?rhel:-3}

# Check that SLS files don't contain any call to "module.run" which has
# been replaced by "mgrcompat.module_run" calls.
! grep --include "*.sls" -r "module\.run" %{buildroot}/usr/share/susemanager/salt || exit 1

%post
# HACK! Create broken link when it will be replaces with the real file
ln -sf %{wwwdocroot}/pub/RHN-ORG-TRUSTED-SSL-CERT \
   /usr/share/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT 2>&1 ||:
# Pre-create top.sls to suppress empty/absent top.sls warning/error (bsc#1017754)
USERLAND="/srv/salt"
TOP="$USERLAND/top.sls"
if [ -d "$USERLAND" ]; then
    if [ ! -f "$TOP" ]; then
	cat <<EOF >> $TOP
# This only calls no-op statement from
# /usr/share/susemanager/salt/util/noop.sls state
# Feel free to change it.

base:
  '*':
    - util.noop
EOF
    fi
fi
# Run JMX exporter as Java Agent (bsc#1184617)
set_up_java_agent()
{
  JMXREMOTE_OPT='-Dcom\.sun\.management\.jmxremote\.'
  RMI_SERVER_HOSTNAME='-Djava\.rmi\.server\.hostname='
  JMX_EXPORTER_JAVA_AGENT='jmx_prometheus_javaagent.jar'
  service_name='prometheus-jmx_exporter@'${1}'.service'
  systemctl is-enabled $service_name > /dev/null 2>&1
  jmx_exporter_enabled=$?
  grep -q -- $JMXREMOTE_OPT ${2}
  jmxremote_opt_configured=$?
  grep -q -- $JMX_EXPORTER_JAVA_AGENT ${2}
  jmx_exporter_java_agent_configured=$?
  if [ $jmx_exporter_enabled -eq 0 ]; then
    systemctl disable $service_name
    if [ $jmxremote_opt_configured -eq 0 ]; then
      sed -ri "s/[[:blank:]]*${JMXREMOTE_OPT}[[:alpha:]]+=[[:alnum:]]+//g" ${2}
      sed -ri "s/[[:blank:]]*${RMI_SERVER_HOSTNAME}[[:alpha:]]+//g" ${2}
    fi
    if [ ! -f /etc/prometheus-jmx_exporter/${1}/uyuni.yml ]; then
      cat > /etc/prometheus-jmx_exporter/${1}/uyuni.yml << EOF
whitelistObjectNames:
  - java.lang:type=Threading,*
  - java.lang:type=Memory,*
  - Catalina:type=ThreadPool,name=*
rules:
  - pattern: ".*"
EOF
    fi
    if [ $jmx_exporter_java_agent_configured -eq 1 ]; then
      sed -ri "s/JAVA_OPTS=\"(.*)\"/JAVA_OPTS=\"\1\ -javaagent:\/usr\/share\/java\/jmx_prometheus_javaagent.jar=${3}:\/etc\/prometheus-jmx_exporter\/${1}\/uyuni.yml\"/" ${2}
    fi
  fi
  if [ ${1} == 'taskomatic' ]; then
    sed -ri "s/JAVA_OPTS/export\ JAVA_OPTS/" ${2}
  fi
}
tomcat_config=/etc/sysconfig/tomcat
tomcat_jmx_exporter_port=5556
taskomatic_config=/etc/rhn/taskomatic.conf
taskomatic_jmx_exporter_port=5557
if [ $1 -gt 1 ] && [ -e $tomcat_config ]; then
  set_up_java_agent tomcat $tomcat_config $tomcat_jmx_exporter_port
fi
if [ $1 -gt 1 ] && [ -e $taskomatic_config ]; then
  set_up_java_agent taskomatic $taskomatic_config $taskomatic_jmx_exporter_port
fi

%files
%defattr(-,root,root)
%dir /usr/share/susemanager
/usr/share/susemanager/salt
/usr/share/susemanager/pillar_data
/usr/share/susemanager/modules
/usr/share/susemanager/modules/pillar
/usr/share/susemanager/modules/tops
/usr/share/susemanager/modules/runners
/usr/share/susemanager/modules/engines
/usr/share/susemanager/formulas
/usr/share/susemanager/reactor
/usr/share/susemanager/scap
/srv/formula_metadata
%exclude /usr/share/susemanager/salt/_modules/uyuni_config.py
%exclude /usr/share/susemanager/salt/_states/uyuni_config.py
%ghost /usr/share/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT

%files -n uyuni-config-modules
%defattr(-,root,root)
%dir /usr/share/susemanager
/usr/share/susemanager/salt/_modules/uyuni_config.py
/usr/share/susemanager/salt/_states/uyuni_config.py
%dir /usr/share/doc/packages/uyuni-config-modules
%doc /usr/share/doc/packages/uyuni-config-modules/*
%doc /usr/share/doc/packages/uyuni-config-modules/examples/*
%doc /usr/share/doc/packages/uyuni-config-modules/examples/ldap/*

%changelog
