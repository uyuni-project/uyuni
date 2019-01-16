#
# spec file for package spacewalk-setup
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


%if 0%{?suse_version} > 1320
# SLE15 builds on Python 3
%global build_py3   1
%endif
%define pythonX %{?build_py3:python3}%{!?build_py3:python2}

%if 0%{?suse_version}
%{!?pylint_check: %global pylint_check 0}
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
Version:        4.0.4
Release:        1%{?dist}
Summary:        Initial setup tools for Spacewalk
License:        GPL-2.0-only
Group:          Applications/System

URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build

%if 0%{?fedora} && 0%{?fedora} > 26
BuildRequires:  perl-interpreter
%else
BuildRequires:  perl
%endif
BuildRequires:  perl(ExtUtils::MakeMaker)
## non-core
#BuildRequires:  perl(Getopt::Long), perl(Pod::Usage)
#BuildRequires:  perl(Test::Pod::Coverage), perl(Test::Pod)

BuildArch:      noarch
%if 0%{?fedora} && 0%{?fedora} > 26
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
Requires:       perl-DateTime
Requires:       perl-Frontier-RPC
Requires:       perl-Mail-RFC822-Address
Requires:       perl-Net-LibIDN
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
%if 0%{?pylint_check}
BuildRequires:  spacewalk-%{pythonX}-pylint
BuildRequires:  %{pythonX}-setuptools
%endif
Requires:       cobbler >= 2.0.0
Requires:       perl-Satcon
Requires:       spacewalk-admin
Requires:       spacewalk-backend-tools
Requires:       spacewalk-certs-tools
%if 0%{?suse_version}
%if 0%{?build_py3}
Requires:       python3-PyYAML
%else
Requires:       python-PyYAML
%endif
%else
%if 0%{?fedora} >= 22
Recommends:     cobbler20
%endif
Requires:       PyYAML
%endif
Requires:       /usr/bin/gpg
Requires:       curl
Requires:       perl-DateTime
Requires:       perl-Mail-RFC822-Address
Requires:       perl-Net-LibIDN
Requires:       spacewalk-base-minimal
Requires:       spacewalk-base-minimal-config
Requires:       spacewalk-java-lib >= 2.4.5
Requires:       spacewalk-setup-jabberd

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
rm -f share/tomcat.java_opts.*
%if 0%{?suse_version}
# SLES12 tomcat has only tomcat.conf
cat share/tomcat.1 >share/tomcat.conf.1
%endif

chmod -R u+w %{buildroot}/*
install -d -m 755 %{buildroot}/%{_datadir}/spacewalk/setup/
install -d -m 755 %{buildroot}/%{_sysconfdir}/salt/master.d/
install -m 0755 share/embedded_diskspace_check.py %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/sudoers.* %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/mod_ssl.conf.* %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/tomcat.* %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/tomcat6.* %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/server.xml.xsl %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/context.xml.xsl %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/server-external-authentication.xml.xsl %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/web.xml.patch %{buildroot}/%{_datadir}/spacewalk/setup/
install -m 0644 share/old-jvm-list %{buildroot}/%{_datadir}/spacewalk/setup/
install -d -m 755 %{buildroot}/%{_datadir}/spacewalk/setup/defaults.d/
install -m 0644 share/defaults.d/defaults.conf %{buildroot}/%{_datadir}/spacewalk/setup/defaults.d/
install -d -m 755 %{buildroot}/%{_datadir}/spacewalk/setup/cobbler
install -m 0644 share/cobbler/* %{buildroot}/%{_datadir}/spacewalk/setup/cobbler/
install -m 0644 salt/susemanager.conf %{buildroot}/%{_sysconfdir}/salt/master.d/

# create a directory for misc. Spacewalk things
install -d -m 755 %{buildroot}/%{misc_path}/spacewalk

mkdir -p $RPM_BUILD_ROOT%{_mandir}/man8
/usr/bin/pod2man --section=8 $RPM_BUILD_ROOT/%{_bindir}/spacewalk-make-mount-points | gzip > $RPM_BUILD_ROOT%{_mandir}/man8/spacewalk-make-mount-points.8.gz
/usr/bin/pod2man --section=1 $RPM_BUILD_ROOT/%{_bindir}/spacewalk-setup-cobbler | gzip > $RPM_BUILD_ROOT%{_mandir}/man1/spacewalk-setup-cobbler.1.gz
/usr/bin/pod2man --section=1 $RPM_BUILD_ROOT/%{_bindir}/spacewalk-setup-tomcat | gzip > $RPM_BUILD_ROOT%{_mandir}/man1/spacewalk-setup-tomcat.1.gz
/usr/bin/pod2man --section=1 $RPM_BUILD_ROOT/%{_bindir}/spacewalk-setup-sudoers| gzip > $RPM_BUILD_ROOT%{_mandir}/man1/spacewalk-setup-sudoers.1.gz
/usr/bin/pod2man --section=1 $RPM_BUILD_ROOT/%{_bindir}/spacewalk-setup-httpd | gzip > $RPM_BUILD_ROOT%{_mandir}/man1/spacewalk-setup-httpd.1.gz
/usr/bin/pod2man --section=1 $RPM_BUILD_ROOT/%{_bindir}/spacewalk-setup-sudoers| gzip > $RPM_BUILD_ROOT%{_mandir}/man1/spacewalk-setup-sudoers.1.gz
/usr/bin/pod2man --section=1 $RPM_BUILD_ROOT/%{_bindir}/spacewalk-setup-ipa-authentication| gzip > $RPM_BUILD_ROOT%{_mandir}/man1/spacewalk-setup-ipa-authentication.1.gz

%post
if [ $1 = 2 -a -e /etc/tomcat6/tomcat6.conf ]; then
    # in case of upgrade
    # fix the old LD_LIBRARY_PATH in tomcat6.conf
    # it has to point to the new Oracle Home
    # this step is only relevant when Oracle version changes and the
    # path written by spacewalk-setup is not valid anymore
    cp /etc/tomcat6/tomcat6.conf /etc/tomcat6/tomcat6.conf.post-script-backup
    . /etc/tomcat6/tomcat6.conf
    NEW_LD_PATH=""

    # in case oracle is not updated yet, we hardcode the oracle version of 1.7 here
    # not really nice
    export ORACLE_HOME="/usr/lib/oracle/11.2/client64"

    if ! grep "$ORACLE_HOME" /etc/tomcat6/tomcat6.conf >/dev/null; then
        # our current ORACLE_HOME is not in LD_LIBRARY_PATH
        if [ "x$LD_LIBRARY_PATH" != "x" ]; then
            # the LD_LIBRARY_PATH is not empty, so we have to fix it
            for p in `echo $LD_LIBRARY_PATH|awk --field-separator=: '{ for(i = 1; i <= NF; i++){print $i; } }'`; do
                if [ -d $p ]; then
                    if [ "x$NEW_LD_PATH" == "x" ]; then
                        NEW_LD_PATH="$p"
                    else
                        NEW_LD_PATH="$NEW_LD_PATH:$p";
                    fi;
                fi;
            done
            NEW_LD_PATH="$NEW_LD_PATH:$ORACLE_HOME/lib"
            sed -i "s@^LD_LIBRARY_PATH.*@LD_LIBRARY_PATH=$NEW_LD_PATH@" /etc/tomcat6/tomcat6.conf
        fi
    fi
    if ! grep -F '\-Dorg.apache.tomcat.util.http.Parameters.MAX_COUNT' /etc/tomcat6/tomcat6.conf > /dev/null; then
        sed -i 's/-XX:MaxNewSize=256/-Dorg.apache.tomcat.util.http.Parameters.MAX_COUNT=1024 -XX:MaxNewSize=256/' /etc/tomcat6/tomcat6.conf
    fi
    if ! grep '\[tftpd\]' /etc/cobbler/modules.conf > /dev/null 2>&1; then
        echo                                                >> /etc/cobbler/modules.conf
        echo '# added by susemanager-setup RPM post-script' >> /etc/cobbler/modules.conf
        echo '[tftpd]'                                      >> /etc/cobbler/modules.conf
        echo 'module = manage_in_tftpd'                     >> /etc/cobbler/modules.conf
    fi
fi
if [ -e /etc/zypp/credentials.d/NCCcredentials ]; then
    chgrp www /etc/zypp/credentials.d/NCCcredentials
    chmod g+r /etc/zypp/credentials.d/NCCcredentials
fi
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
exit 0

%check
make test
%if 0%{?pylint_check}
# check coding style
pylint --rcfile /etc/spacewalk-python3-pylint.rc \
    $RPM_BUILD_ROOT%{_datadir}/spacewalk/setup/*.py \
    $RPM_BUILD_ROOT%{_bindir}/cobbler20-setup
%endif

%files
%defattr(-,root,root,-)
%doc Changes README answers.txt
%config %{_sysconfdir}/salt/master.d/susemanager.conf
%{perl_vendorlib}/*
%{_bindir}/spacewalk-setup
%{_bindir}/spacewalk-setup-httpd
%{_bindir}/spacewalk-make-mount-points
%{_bindir}/spacewalk-setup-cobbler
%{_bindir}/spacewalk-setup-tomcat
%{_bindir}/spacewalk-setup-sudoers
%{_bindir}/spacewalk-setup-ipa-authentication
%{_bindir}/spacewalk-setup-db-ssl-certificates
%{_bindir}/cobbler20-setup
%{_mandir}/man[13]/*.[13]*
%dir %{_datadir}/spacewalk
%{_datadir}/spacewalk/*
%attr(755, %{apache_user}, root) %{misc_path}/spacewalk
%{_mandir}/man8/spacewalk-make-mount-points*
%doc LICENSE

%changelog
