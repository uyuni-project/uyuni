#
# spec file for package spacewalk-setup
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


%if 0%{?suse_version} > 1320 || 0%{?rhel} || 0%{?fedora}
# SLE15 builds on Python 3
%global build_py3   1
%endif
%define pythonX %{?build_py3:python3}%{!?build_py3:python2}

%if 0%{?suse_version}
%define apache_user wwwrun
%define apache_group www
%define misc_path /srv/
%else
%define apache_user apache
%define apache_group apache
%define misc_path %{_var}
%endif
%{!?fedora: %global sbinpath /sbin}%{?fedora: %global sbinpath %{_sbindir}}

Name:           spacewalk-setup
Version:        4.4.6
Release:        1
Summary:        Initial setup tools for Spacewalk
License:        GPL-2.0-only
Group:          Applications/System

URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build

%if 0%{?fedora}
BuildRequires:  perl-interpreter
%else
BuildRequires:  perl
%endif
BuildRequires:  perl(ExtUtils::MakeMaker)
%if 0%{?suse_version}
BuildRequires:  python3-Sphinx
%else
BuildRequires:  python3-sphinx
%endif
## non-core
#BuildRequires:  perl(Getopt::Long), perl(Pod::Usage)
#BuildRequires:  perl(Test::Pod::Coverage), perl(Test::Pod)

BuildArch:      noarch
%if 0%{?fedora}
Requires:       perl-interpreter
%else
Requires:       perl
%endif
Requires:       perl-Params-Validate
Requires:       spacewalk-schema
Requires:       perl(Term::Completion::Path)
%if 0%{?suse_version}
Requires:       curl
Requires:       patch
Requires:       perl-Frontier-RPC
Requires:       perl-XML-LibXML
Requires:       perl-XML-SAX
Requires:       perl-libwww-perl
Requires:       policycoreutils
# to have /etc/salt/master.d
Requires(pre):  salt-master
# for salt to be generated into the thin
%if 0%{?build_py3}
Requires:       python3-certifi
%else
Requires:       python-certifi
%endif
BuildRequires:  perl-libwww-perl
%else
Requires:       %{sbinpath}/restorecon
%endif
Requires(post): cobbler
Requires:       perl-Satcon
Requires:       spacewalk-admin
Requires:       spacewalk-backend-tools
Requires:       spacewalk-certs-tools
%if 0%{?build_py3}
Requires:       (python3-PyYAML or python3-pyyaml)
%else
Requires:       (python-PyYAML or PyYAML)
%endif
Requires:       curl
Requires:       perl-Mail-RFC822-Address
%if 0%{?rhel}
Requires:       perl-Net-LibIDN2
%else
Requires:       perl-Net-LibIDN
%endif
Requires:       spacewalk-base-minimal
Requires:       spacewalk-base-minimal-config
Requires:       spacewalk-java-lib >= 2.4.5
Requires:       uyuni-setup-reportdb
%if 0%{?rhel}
Requires(post): libxslt-devel
%else
Requires(post): libxslt-tools
%endif

Provides:       salt-formulas-configuration
Conflicts:      otherproviders(salt-formulas-configuration)

%description
A collection of post-installation scripts for managing Spacewalk's initial
setup tasks, re-installation, and upgrades.

%prep
%setup -q

%build
%{__perl} Makefile.PL INSTALLDIRS=vendor
make %{?_smp_mflags}

# Fixing shebang for Python 3
%if 0%{?build_py3}
for i in $(find . -type f);
do
    sed -i '1s=^#!/usr/bin/\(python\|env python\)[0-9.]*=#!/usr/bin/python3=' $i;
done
# timestamp of Makefile must always be newer than Makefile.PL, else build will fail
touch Makefile
%endif

# Fix python executable in Perl code
%if 0%{?build_py3}
sed -i "s/'python'/'python3'/g" lib/Spacewalk/Setup.pm
%endif

# Build RST manpages
sphinx-build -b man doc/ out/

%install
make pure_install PERL_INSTALL_ROOT=%{buildroot}
find %{buildroot} -type f -name .packlist -exec rm -f {} ';'
find %{buildroot} -type d -depth -exec rmdir {} 2>/dev/null ';'

%if 0%{?rhel} == 6
cat share/tomcat.java_opts.rhel6 >>share/tomcat.java_opts
%endif
#if java -version 2>&1 | grep -q IBM ; then
#    cat share/tomcat.java_opts.ibm >>share/tomcat.java_opts
#fi
%if 0%{?suse_version}
cat share/tomcat.java_opts.suse >>share/tomcat.java_opts | tr '\n' ' '
# SLES12 tomcat has only tomcat.conf
cat share/tomcat.1 >share/tomcat.conf.1
%endif
rm -f share/tomcat.java_opts.*

chmod -R u+w %{buildroot}/*
install -d -m 755 %{buildroot}/%{_datadir}/spacewalk/setup/
install -d -m 755 %{buildroot}/%{_sysconfdir}/salt/master.d/
install -m 0755 share/embedded_diskspace_check.py %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/sudoers.* %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/mod_ssl.conf.* %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/tomcat.* %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/tomcat6.* %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/server.xml.xsl %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/server_update.xml.xsl %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/context.xml.xsl %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/server-external-authentication.xml.xsl %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/web.xml.patch %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/old-jvm-list %{buildroot}/%{_datadir}/spacewalk/setup/
install -d -m 755 %{buildroot}/%{_datadir}/spacewalk/setup/defaults.d/
install -m 0644 share/defaults.d/defaults.conf %{buildroot}/%{_datadir}/spacewalk/setup/defaults.d/
install -d -m 755 %{buildroot}/%{_datadir}/spacewalk/setup/cobbler
install -m 0644 salt/susemanager.conf %{buildroot}/%{_sysconfdir}/salt/master.d/
install -m 0644 salt/salt-ssh-logging.conf %{buildroot}/%{_sysconfdir}/salt/master.d/

# create a directory for misc. Spacewalk things
install -d -m 755 %{buildroot}/%{misc_path}/spacewalk

mkdir -p $RPM_BUILD_ROOT%{_mandir}/man8
/usr/bin/pod2man --section=8 $RPM_BUILD_ROOT/%{_bindir}/spacewalk-make-mount-points | gzip > $RPM_BUILD_ROOT%{_mandir}/man8/spacewalk-make-mount-points.8.gz
/usr/bin/pod2man --section=1 $RPM_BUILD_ROOT/%{_bindir}/spacewalk-setup-tomcat | gzip > $RPM_BUILD_ROOT%{_mandir}/man1/spacewalk-setup-tomcat.1.gz
/usr/bin/pod2man --section=1 $RPM_BUILD_ROOT/%{_bindir}/spacewalk-setup-sudoers| gzip > $RPM_BUILD_ROOT%{_mandir}/man1/spacewalk-setup-sudoers.1.gz
/usr/bin/pod2man --section=1 $RPM_BUILD_ROOT/%{_bindir}/spacewalk-setup-httpd | gzip > $RPM_BUILD_ROOT%{_mandir}/man1/spacewalk-setup-httpd.1.gz
/usr/bin/pod2man --section=1 $RPM_BUILD_ROOT/%{_bindir}/spacewalk-setup-sudoers| gzip > $RPM_BUILD_ROOT%{_mandir}/man1/spacewalk-setup-sudoers.1.gz
/usr/bin/pod2man --section=1 $RPM_BUILD_ROOT/%{_bindir}/spacewalk-setup-ipa-authentication| gzip > $RPM_BUILD_ROOT%{_mandir}/man1/spacewalk-setup-ipa-authentication.1.gz
# Sphinx built manpage
%define SPHINX_BASE_DIR %(echo %{SOURCE0}| sed -e 's/\.tar\.gz//' | sed 's@.*/@@')
install -m 0644 %{_builddir}/%{SPHINX_BASE_DIR}/out/spacewalk-cobbler-setup.1 $RPM_BUILD_ROOT%{_mandir}/man1/spacewalk-setup-cobbler.1

# Standalone Salt formulas configuration
install -Dd -m 0755 %{buildroot}%{_prefix}/share/salt-formulas
install -Dd -m 0755 %{buildroot}%{_prefix}/share/salt-formulas/states
install -Dd -m 0755 %{buildroot}%{_prefix}/share/salt-formulas/metadata

%post
if [ $1 == 2 -a -e /etc/tomcat/server.xml ]; then
#during upgrade, setup new connectionTimeout if the user didn't change it
    cp /etc/tomcat/server.xml /etc/tomcat/server.xml.post-script-backup
    xsltproc %{_datadir}/spacewalk/setup/server_update.xml.xsl /etc/tomcat/server.xml.post-script-backup > /etc/tomcat/server.xml
fi

%if 0%{?suse_version}
if [ $1 = 2 -a -e /etc/sysconfig/tomcat ]; then
     sed -ri '/\-\-add\-modules java\.annotation,com\.sun\.xml\.bind/!s/JAVA_OPTS="(.*)"/JAVA_OPTS="\1 --add-modules java.annotation,com.sun.xml.bind --add-exports java.annotation\/javax.annotation.security=ALL-UNNAMED --add-opens java.annotation\/javax.annotation.security=ALL-UNNAMED"/' /etc/sysconfig/tomcat
fi
%endif

if [ -e /etc/zypp/credentials.d/SCCcredentials ]; then
    chgrp www /etc/zypp/credentials.d/SCCcredentials
    chmod g+r /etc/zypp/credentials.d/SCCcredentials
fi
for name in /etc/sysconfig/tomcat{5,6,} /etc/tomcat*/tomcat*.conf; do
  test -f $name \
  && sed -i 's/\(-Dorg.xml.sax.driver\)=org.apache.xerces.parsers.SAXParser\>/\1=com.redhat.rhn.frontend.xmlrpc.util.RhnSAXParser/g' $name
done
if [ -d /var/cache/salt/master/thin ]; then
  # clean the thin cache
  rm -rf /var/cache/salt/master/thin
fi

# sudoers file is now in /etc/sudoers.d/spacewalk
if [ -f /etc/sudoers.d/spacewalk -a -f /etc/sudoers.d/susemanager ]; then
    # do not fail if one is just a link to the other one
    cp /etc/sudoers.d/spacewalk /etc/sudoers.d/spacewalk.tmp
    rm -f /etc/sudoers.d/spacewalk /etc/sudoers.d/susemanager
    mv /etc/sudoers.d/spacewalk.tmp /etc/sudoers.d/spacewalk
fi

if grep 'authn_spacewalk' /etc/cobbler/modules.conf > /dev/null 2>&1; then
    sed -i 's/module = authn_spacewalk/module = authentication.spacewalk/' /etc/cobbler/modules.conf
fi

# When upgrading to Cobbler 3.3.3, the old /etc/cobbler/settings config file from previous Cobbler version
# is removed as it not existing anymore in the new version, but a copy is kept with the local changes done
# at /etc/cobbler/settings.rpmsave. If this file exists, it means we need to perform the migration of these
# settings and also trigger the migration of stored Cobbler collections.
if [ ! -f /etc/cobbler/settings -a -f /etc/cobbler/settings.rpmsave ]; then
    cp /etc/cobbler/settings.rpmsave /etc/cobbler/settings
    echo "* Creating a backup from old Cobbler settings to /etc/cobbler/settings.before-migration-backup before migrating settings"
    cp /etc/cobbler/settings /etc/cobbler/settings.before-migration-backup
    echo "* Migrating old Cobbler settings to new /etc/cobbler/settings.yaml file and executing migration of stored Cobbler collections"
    echo "  (a backup of the collections will be created at /var/lib/cobbler/)"
    /usr/share/cobbler/bin/migrate-data-v2-to-v3.py -c /var/lib/cobbler/collections --noconfigs --noapi || exit 1
    touch /var/lib/cobbler/v2_migration_done
    cobbler-settings -c /etc/cobbler/settings migrate -t /etc/cobbler/settings.yaml || exit 1
    echo "* Disabling Cobbler settings automigration"
    cobbler-settings automigrate -d || exit 1
    echo "* Change group to Apache for /etc/cobbler/settings.yaml file"
    chgrp %{apache_group} /etc/cobbler/settings.yaml
    echo "* Readjust settings needed for spacewalk"
    spacewalk-setup-cobbler || exit 1
    echo "* Done"
    # At this point, the migration finished successfully, so we can remove
    # the old /etc/cobbler/settings.rpmsave to prevent migration to run again.
    rm /etc/cobbler/settings.rpmsave
fi

# Migration to Cobbler 3.3.3 already performed but not the migration of Cobbler v2 collections to v3
if [ ! -f /etc/cobbler/settings.rpmsave -a -f /etc/cobbler/settings.before-migration-backup -a ! -f /var/lib/cobbler/v2_migration_done ]; then
    echo "* Migrating old stored Cobbler version 2 collections"
    echo "  (a backup of the collections will be created at /var/lib/cobbler/)"
    /usr/share/cobbler/bin/migrate-data-v2-to-v3.py -c /var/lib/cobbler/collections --noconfigs --noapi || exit 1
    cobbler-settings -c /etc/cobbler/settings.before-migration-backup migrate || exit 1
    touch /var/lib/cobbler/v2_migration_done
fi

# Wrong execution of v2 script happened, so we fix autoinstall attribute of collections.
if test -f /var/lib/cobbler/v2_migration_done && ! grep -q autoinstall_fixed /var/lib/cobbler/v2_migration_done; then
    echo "* Check and fix autoinstall attributes from Cobbler collections"
    echo "  (a backup of the collections will be created at /var/lib/cobbler/)"
    /usr/share/cobbler/bin/migrate-data-v2-to-v3.py -c /var/lib/cobbler/collections --only-fix-autoinstall || exit 1
    echo "autoinstall_fixed" >> /var/lib/cobbler/v2_migration_done
fi

exit 0

%check
make test

%files
%defattr(-,root,root,-)
%doc Changes README answers.txt
%config %{_sysconfdir}/salt/master.d/susemanager.conf
%config %{_sysconfdir}/salt/master.d/salt-ssh-logging.conf
%{perl_vendorlib}/*
%{_bindir}/spacewalk-setup
%{_bindir}/spacewalk-setup-httpd
%{_bindir}/spacewalk-make-mount-points
%{_bindir}/spacewalk-setup-cobbler
%{_bindir}/spacewalk-setup-tomcat
%{_bindir}/spacewalk-setup-sudoers
%{_bindir}/spacewalk-setup-ipa-authentication
%{_mandir}/man[13]/*.[13]*
%dir %attr(0755, root, root) %{_prefix}/share/salt-formulas/
%dir %attr(0755, root, root) %{_prefix}/share/salt-formulas/states/
%dir %attr(0755, root, root) %{_prefix}/share/salt-formulas/metadata/
%dir %{_datadir}/spacewalk
%{_datadir}/spacewalk/*
%if 0%{?rhel} || 0%{?fedora}
%{misc_path}/spacewalk
%else
%attr(755, %{apache_user}, root) %{misc_path}/spacewalk
%endif
%{_mandir}/man8/spacewalk-make-mount-points*
%license LICENSE

%changelog
