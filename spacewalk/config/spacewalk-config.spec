#
# spec file for package spacewalk-config
#
# Copyright (c) 2021 SUSE LLC
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


%if 0%{?suse_version}
%define apacheconfdir %{_sysconfdir}/apache2
%define apachepkg apache2
%define apache_group www
%else
%define apacheconfdir %{_sysconfdir}/httpd
%define apachepkg httpd
%define apache_group apache
%endif

Name:           spacewalk-config
Summary:        Spacewalk Configuration
License:        GPL-2.0-only
Group:          Applications/System
Version:        4.3.14
Release:        0
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/uyuni-project/uyuni/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
Requires:       perl(Satcon)
Obsoletes:      rhn-satellite-config < 5.3.0
Provides:       rhn-satellite-config = 5.3.0
%if 0%{?fedora} > 24
BuildRequires:  perl-generators
%endif
%if 0%{?rhel} || 0%{?fedora}
Requires(post): chkconfig
Requires(preun): chkconfig
# This is for /sbin/service
Requires(preun): initscripts
%endif
# We need package httpd to be able to assign group apache in files section
Requires(pre): %{apachepkg}
Requires:       openssl
BuildRequires:  uyuni-base-common
Requires(pre):  uyuni-base-common

%global prepdir %{_var}/lib/rhn/rhn-satellite-prep

%if 0%{?suse_version}
BuildRequires:  openssl
BuildRequires:  sudo
%endif
Requires:       (apache2-mod_xsendfile or mod_xsendfile)
Requires:       diffutils

%description
Common Spacewalk configuration files and templates.

%prep
%setup -q
echo "%{name} %{version}" > version

%build

%install
rm -Rf $RPM_BUILD_ROOT

mkdir -p $RPM_BUILD_ROOT
mv etc $RPM_BUILD_ROOT/
mv var $RPM_BUILD_ROOT/
mv usr $RPM_BUILD_ROOT/

%if 0%{?suse_version}
export NO_BRP_STALE_LINK_ERROR=yes
mv $RPM_BUILD_ROOT/etc/httpd $RPM_BUILD_ROOT%{apacheconfdir}
sed -i 's|var/www/html|srv/www/htdocs|g' $RPM_BUILD_ROOT%{apacheconfdir}/conf.d/zz-spacewalk-www.conf
%endif

tar -C $RPM_BUILD_ROOT%{prepdir} -cf - etc \
     | tar -C $RPM_BUILD_ROOT -xvf -

echo "" > $RPM_BUILD_ROOT/%{_sysconfdir}/rhn/rhn.conf

mkdir -p $RPM_BUILD_ROOT/etc/pki/tls/certs/
mkdir -p $RPM_BUILD_ROOT/etc/pki/tls/private/

%files
%defattr(-,root,root,-)
%attr(400,root,root) %config(noreplace) %{_sysconfdir}/rhn/spacewalk-repo-sync/uln.conf
%config %{apacheconfdir}/conf.d/zz-spacewalk-www.conf
%config %{apacheconfdir}/conf.d/os-images.conf
%config(noreplace) %{_sysconfdir}/webapp-keyring.gpg
%attr(440,root,root) %config %{_sysconfdir}/sudoers.d/spacewalk
%dir %{_var}/lib/cobbler/
%dir %{_var}/lib/cobbler/kickstarts/
%dir %{_var}/lib/cobbler/snippets/
%config(noreplace) %{_var}/lib/cobbler/kickstarts/spacewalk-sample.ks
%config(noreplace) %{_var}/lib/cobbler/snippets/spacewalk_file_preservation
%attr(0640,root,%{apache_group}) %config(noreplace) %{_sysconfdir}/rhn/rhn.conf
%attr(0750,root,%{apache_group}) %dir %{_sysconfdir}/rhn/candlepin-certs
%config %attr(644, root, root) %{_sysconfdir}/rhn/candlepin-certs/candlepin-redhat-ca.crt
%config(noreplace) %{_sysconfdir}/satname
%dir %{_var}/lib/rhn
%dir %{_var}/lib/rhn/rhn-satellite-prep
%attr(0750,root,root) %dir %{_var}/lib/rhn/rhn-satellite-prep/etc
%attr(0750,root,%{apache_group}) %dir %{_var}/lib/rhn/rhn-satellite-prep/etc/rhn
%attr(0640,root,%{apache_group}) %{_var}/lib/rhn/rhn-satellite-prep/etc/rhn/rhn.conf
%dir %{_prefix}/share/rhn
%attr(0755,root,root) %{_prefix}/share/rhn/startup.pl
%license LICENSE
%doc %{_mandir}/man5/rhn.conf.5*
%if 0%{?suse_version}
%dir %{_sysconfdir}/pki
%dir %{_sysconfdir}/pki/tls
%dir %{_sysconfdir}/pki/tls/certs
%dir %{_sysconfdir}/pki/tls/private
%dir %{_sysconfdir}/rhn/spacewalk-repo-sync
%endif

%pre
# This section is needed here because previous versions of spacewalk-config
# (and rhn-satellite-config) "owned" the satellite-httpd service. We need
# to keep this section here indefinitely, because Satellite 5.2 could
# be upgraded directly to our version of Spacewalk.
if [ -f /etc/init.d/satellite-httpd ] ; then
    /sbin/service satellite-httpd stop >/dev/null 2>&1
    /sbin/chkconfig --del satellite-httpd
    %{__perl} -i -ne 'print unless /satellite-httpd\.pid/' /etc/logrotate.d/httpd
fi

# Set the group to allow Apache to access the conf files ...
chgrp %{apache_group} /etc/rhn /etc/rhn/rhn.conf 2> /dev/null || :
# ... once we restrict access to some files that were too open in
# the past.
chmod o-rwx /etc/rhn/rhn.conf* /etc/sysconfig/rhn/backup-* /var/lib/rhn/rhn-satellite-prep/* 2> /dev/null || :

# we want to remove the cert from the package.
# copy the cert to a backup place to restore them later
if [ -L /etc/pki/tls/certs/spacewalk.crt ]; then
  cp /etc/pki/tls/certs/spacewalk.crt /etc/pki/tls/certs/uyuni.crt
fi
if [ -L /etc/pki/tls/private/spacewalk.key ]; then
  cp /etc/pki/tls/private/spacewalk.key /etc/pki/tls/private/uyuni.key
fi


%post
%if 0%{?suse_version}
sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES version
sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES proxy
sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES proxy_ajp
sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES proxy_wstunnel
sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES rewrite
sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES headers
sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES xsendfile
sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES filter
sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES deflate
sysconf_addword /etc/sysconfig/apache2 APACHE_SERVER_FLAGS SSL
sysconf_addword /etc/sysconfig/apache2 APACHE_SERVER_FLAGS ISSUSE
sysconf_addword -r /etc/sysconfig/apache2 APACHE_MODULES access_compat
%endif

# sudo is reading every file here! So ensure we do not have duplicate definitions!
if [ -e /etc/sudoers.d/spacewalk.rpmsave ]; then
  mv /etc/sudoers.d/spacewalk.rpmsave /root/sudoers-spacewalk.save
fi
rm -f /etc/sudoers.d/spacewalk.{rpmnew,rpmorig,rpmsave}

### TO-REMOVE AFTER: 2023-12-01
if egrep -m1 "^taskomatic.com.redhat.rhn.taskomatic.task.SSHMinionActionExecutor.parallel_threads[[:space:]]*=" /etc/rhn/rhn.conf >/dev/null; then
    sed -i "s/taskomatic.com.redhat.rhn.taskomatic.task.SSHMinionActionExecutor.parallel_threads[[:space:]]*=\(.\+\)/taskomatic.sshminion_action_executor.parallel_threads =\1/" /etc/rhn/rhn.conf
fi
if egrep -m1 "^taskomatic.com.redhat.rhn.taskomatic.task.MinionActionExecutor.parallel_threads[[:space:]]*=" /etc/rhn/rhn.conf >/dev/null; then
    sed -i "s/taskomatic.com.redhat.rhn.taskomatic.task.MinionActionExecutor.parallel_threads[[:space:]]*=\(.\+\)/taskomatic.minion_action_executor.parallel_threads =\1/" /etc/rhn/rhn.conf
fi
if egrep -m1 "^taskomatic.com.redhat.rhn.taskomatic.task" /etc/rhn/rhn.conf >/dev/null; then
    echo "WARNING: Found deprecated configuration items in /etc/rhn/rhn.conf"
fi
### END


%posttrans
# restore the cert if we lost it
if [ -e /etc/pki/tls/certs/uyuni.crt ]; then
  if [ -f /etc/pki/tls/certs/spacewalk.crt ]; then
    rm -f /etc/pki/tls/certs/uyuni.crt
  else
    mv /etc/pki/tls/certs/uyuni.crt /etc/pki/tls/certs/spacewalk.crt
  fi
fi
if [ -e /etc/pki/tls/private/uyuni.key ]; then
  if [ -f /etc/pki/tls/private/spacewalk.key ]; then
    rm -f /etc/pki/tls/private/uyuni.key
  else
    mv /etc/pki/tls/private/uyuni.key /etc/pki/tls/private/spacewalk.key
  fi
fi

if [ -f /srv/www/htdocs/pub/RHN-ORG-TRUSTED-SSL-CERT ] ; then
  if [ ! -f /etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT ] ; then
    if diff -qs /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT /srv/www/htdocs/pub/RHN-ORG-TRUSTED-SSL-CERT ; then
      mv /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT /etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT
    else
      cp /srv/www/htdocs/pub/RHN-ORG-TRUSTED-SSL-CERT /etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT
    fi
  fi
fi

%changelog
