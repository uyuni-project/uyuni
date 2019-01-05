#
# spec file for package susemanager-tftpsync-recv
#
# Copyright (c) 2018 SUSE LINUX GmbH, Nuernberg, Germany.
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

Name:           susemanager-tftpsync-recv
Version:        4.0.2
Release:        1%{?dist}
Summary:        Reciever for SUSE Manager tftp sync
License:        GPL-2.0-only
Group:          Applications/System
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
Requires(pre):  apache2
Requires(pre):  tftp(server)
%if 0%{?build_py3}
Requires:       python3
Requires:       apache2-mod_wsgi-python3
%else
Requires:       python
Requires:       apache2-mod_wsgi
%endif
Requires:       spacewalk-backend
Requires:       spacewalk-proxy-common
Requires(pre):  coreutils

%description
Use SUSE Manager Proxy as installation server. Provide the capability
that cobbler on a SUSE Manager Server distribute PXE config and images
to the proxies.


%prep
%setup -q

%build

%install
mkdir -p %{buildroot}/%{_var}/log/tftpsync
install -p -D -m 644 susemanager-tftpsync-recv.conf %{buildroot}%{_sysconfdir}/apache2/conf.d/susemanager-tftpsync-recv.conf
install -p -D -m 644 add.wsgi    %{buildroot}/srv/www/tftpsync/add
install -p -D -m 644 delete.wsgi %{buildroot}/srv/www/tftpsync/delete
install -p -D -m 755 configure-tftpsync.sh %{buildroot}%{_sbindir}/configure-tftpsync.sh
install -p -D -m 644 rhn_tftpsync.conf %{buildroot}/%{_datadir}/rhn/config-defaults/rhn_tftpsync.conf

%post
sysconf_addword /etc/sysconfig/apache2 APACHE_MODULES wsgi
if [ -d /srv/tftpboot ]; then
    chmod 750 /srv/tftpboot
    chown wwwrun:tftp /srv/tftpboot
fi

%files
%defattr(-,wwwrun,root,-)
%dir %{_var}/log/tftpsync
%config(noreplace) %{_sysconfdir}/apache2/conf.d/susemanager-tftpsync-recv.conf

%defattr(-,root,root,-)
%doc answers.txt COPYING README
%dir /srv/www/tftpsync
%dir /%{_datadir}/rhn
%attr(755,root,www) %dir %{_datadir}/rhn/config-defaults
/srv/www/tftpsync/add
/srv/www/tftpsync/delete
%{_sbindir}/configure-tftpsync.sh
%{_datadir}/rhn/config-defaults/rhn_tftpsync.conf

%changelog
