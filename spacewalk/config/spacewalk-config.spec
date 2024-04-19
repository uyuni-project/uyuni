#
# spec file for package spacewalk-config
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
Version:        5.0.2
Release:        0
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/uyuni-project/uyuni/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
%if 0%{?rhel} || 0%{?fedora}
Requires(post): chkconfig
Requires(preun):chkconfig
# This is for /sbin/service
Requires(preun):initscripts
%endif
# We need package httpd to be able to assign group apache in files section
Requires(pre):  %{apachepkg}
Requires:       openssl
BuildRequires:  uyuni-base-common
Requires(pre):  uyuni-base-common

%global prepdir %{_var}/lib/rhn/rhn-satellite-prep

%if 0%{?suse_version}
BuildRequires:  sudo
%endif
Requires:       diffutils
Requires:       (apache2-mod_xsendfile or mod_xsendfile)

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

#TODO invert this logic: the default should be for suse, the if should contains directive for other distros
%if 0%{?suse_version}
export NO_BRP_STALE_LINK_ERROR=yes
mv $RPM_BUILD_ROOT/etc/httpd $RPM_BUILD_ROOT%{apacheconfdir}
%else
sed -i 's|srv/www/htdocs|var/www/html|g' $RPM_BUILD_ROOT%{apacheconfdir}/conf.d/z-public.conf
sed -i 's|/usr/share/apache2/|/usr/share/httpd/|g' $RPM_BUILD_ROOT%{apacheconfdir}/conf.d/zz-spacewalk-www.conf

%endif

touch $RPM_BUILD_ROOT/%{_sysconfdir}/rhn/rhn.conf

mkdir -p $RPM_BUILD_ROOT/etc/pki/tls/certs/
mkdir -p $RPM_BUILD_ROOT/etc/pki/tls/private/

%files
%defattr(-,root,root,-)
%attr(400,root,root) %config(noreplace) %{_sysconfdir}/rhn/spacewalk-repo-sync/uln.conf
%config %{apacheconfdir}/conf.d/zz-spacewalk-www.conf
%config %{apacheconfdir}/conf.d/os-images.conf
%config %{apacheconfdir}/conf.d/z-public.conf
%attr(440,root,root) %config %{_sysconfdir}/sudoers.d/spacewalk
%dir %{_var}/lib/cobbler/
%dir %{_var}/lib/cobbler/kickstarts/
%dir %{_var}/lib/cobbler/snippets/
%config(noreplace) %{_var}/lib/cobbler/kickstarts/spacewalk-sample.ks
%config(noreplace) %{_var}/lib/cobbler/snippets/spacewalk_file_preservation
%attr(0640,root,%{apache_group}) %config(noreplace) %{_sysconfdir}/rhn/rhn.conf
%config(noreplace) %{_sysconfdir}/satname
%dir %{_var}/lib/rhn
%dir %{_var}/lib/rhn/rhn-satellite-prep
%attr(0750,root,root) %dir %{_var}/lib/rhn/rhn-satellite-prep/etc
%attr(0750,root,%{apache_group}) %dir %{_var}/lib/rhn/rhn-satellite-prep/etc/rhn
%attr(0640,root,%{apache_group}) %{_var}/lib/rhn/rhn-satellite-prep/etc/rhn/rhn.conf
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
# Set the group to allow Apache to access the conf files ...
chgrp %{apache_group} /etc/rhn /etc/rhn/rhn.conf 2> /dev/null || :
# ... once we restrict access to some files that were too open in
# the past.
chmod o-rwx /etc/rhn/rhn.conf* /etc/sysconfig/rhn/backup-* /var/lib/rhn/rhn-satellite-prep/* 2> /dev/null || :

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
%endif

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

%changelog
