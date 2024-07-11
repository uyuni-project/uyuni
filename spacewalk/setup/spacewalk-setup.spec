#
# spec file for package spacewalk-setup
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
Version:        5.1.0
Release:        0
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
BuildRequires:  tomcat
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
Requires(post): tomcat
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

chmod -R u+w %{buildroot}/*
install -d -m 755 %{buildroot}/%{_datadir}/spacewalk/setup/
install -d -m 755 %{buildroot}/%{_sysconfdir}/salt/master.d/
install -d -m 755 %{buildroot}/%{_sysconfdir}/tomcat/conf.d/
install -m 0755 share/embedded_diskspace_check.py %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/mod_ssl.conf.* %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/tomcat_java_opts.conf %{buildroot}/%{_sysconfdir}/tomcat/conf.d/
%if 0%{?suse_version}
install -m 0644 share/tomcat_java_opts_suse.conf %{buildroot}/%{_sysconfdir}/tomcat/conf.d/
%endif
install -m 0644 share/server.xml.xsl %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/server_update.xml.xsl %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/add_appbase.xml.xsl %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/old-jvm-list %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/vhost-nossl.conf %{buildroot}/%{_datadir}/spacewalk/setup/
install -d -m 755 %{buildroot}/%{_datadir}/spacewalk/setup/defaults.d/
install -m 0644 share/defaults.d/defaults.conf %{buildroot}/%{_datadir}/spacewalk/setup/defaults.d/
install -d -m 755 %{buildroot}/%{_datadir}/spacewalk/setup/cobbler
install -m 0644 salt/susemanager.conf %{buildroot}/%{_sysconfdir}/salt/master.d/
install -m 0644 salt/salt-ssh-logging.conf %{buildroot}/%{_sysconfdir}/salt/master.d/

# create a directory for misc. Spacewalk things
install -d -m 755 %{buildroot}/%{misc_path}/spacewalk

mkdir -p $RPM_BUILD_ROOT%{_mandir}/man8
/usr/bin/pod2man --section=8 $RPM_BUILD_ROOT/%{_bindir}/spacewalk-make-mount-points | gzip > $RPM_BUILD_ROOT%{_mandir}/man8/spacewalk-make-mount-points.8.gz
/usr/bin/pod2man --section=1 $RPM_BUILD_ROOT/%{_bindir}/spacewalk-setup-httpd | gzip > $RPM_BUILD_ROOT%{_mandir}/man1/spacewalk-setup-httpd.1.gz
# Sphinx built manpage
%define SPHINX_BASE_DIR %(echo %{SOURCE0}| sed -e 's/\.tar\.gz//' | sed 's@.*/@@')
install -m 0644 %{_builddir}/%{SPHINX_BASE_DIR}/out/spacewalk-cobbler-setup.1 $RPM_BUILD_ROOT%{_mandir}/man1/spacewalk-setup-cobbler.1

# Standalone Salt formulas configuration
install -Dd -m 0755 %{buildroot}%{_prefix}/share/salt-formulas
install -Dd -m 0755 %{buildroot}%{_prefix}/share/salt-formulas/states
install -Dd -m 0755 %{buildroot}%{_prefix}/share/salt-formulas/metadata

%post
if [ ! -f /etc/rhn/rhn.conf -o $(stat -c %%s "/etc/rhn/rhn.conf") -eq 0 ]; then
    # rhn.conf does not exists or is empty, this is new installation or update of new installation
    CURRENT_DATE=$(date +"%%Y-%%m-%%dT%%H:%%M:%%S.%%3N")
    cp /etc/tomcat/server.xml /etc/tomcat/server.xml.$CURRENT_DATE
    xsltproc %{_datadir}/spacewalk/setup/server.xml.xsl /etc/tomcat/server.xml.$CURRENT_DATE > /etc/tomcat/server.xml
fi

CURRENT_DATE=$(date +"%%Y-%%m-%%dT%%H:%%M:%%S.%%3N")
cp /etc/tomcat/server.xml /etc/tomcat/server.xml.$CURRENT_DATE
xsltproc %{_datadir}/spacewalk/setup/add_appbase.xml.xsl /etc/tomcat/server.xml.$CURRENT_DATE > /etc/tomcat/server.xml

if [ -e /etc/zypp/credentials.d/SCCcredentials ]; then
    chgrp www /etc/zypp/credentials.d/SCCcredentials
    chmod g+r /etc/zypp/credentials.d/SCCcredentials
fi

if [ -d /var/cache/salt/master/thin ]; then
  # clean the thin cache
  rm -rf /var/cache/salt/master/thin
fi

if [ -f /etc/rhn/rhn.conf ]; then
    # Ensure all the apache configuration are present, also during upgrade
    /usr/bin/spacewalk-setup-httpd
fi

exit 0

%check
make test

%files
%defattr(-,root,root,-)
%doc Changes README answers.txt
%config %{_sysconfdir}/salt/master.d/susemanager.conf
%config %{_sysconfdir}/salt/master.d/salt-ssh-logging.conf
%config %{_sysconfdir}/tomcat/conf.d/tomcat_java_opts.conf
%if 0%{?suse_version}
%config %{_sysconfdir}/tomcat/conf.d/tomcat_java_opts_suse.conf
%endif
%{perl_vendorlib}/*
%{_bindir}/spacewalk-setup
%{_bindir}/spacewalk-setup-httpd
%{_bindir}/spacewalk-make-mount-points
%{_bindir}/spacewalk-setup-cobbler
%{_mandir}/man[13]/*.[13]*
%dir %attr(0755, root, root) %{_prefix}/share/salt-formulas/
%dir %attr(0755, root, root) %{_prefix}/share/salt-formulas/states/
%dir %attr(0755, root, root) %{_prefix}/share/salt-formulas/metadata/
%dir %{_datadir}/spacewalk
%{_datadir}/spacewalk/*
%attr(755, %{apache_user}, root) %{misc_path}/spacewalk
%{_mandir}/man8/spacewalk-make-mount-points*
%license LICENSE

%changelog
