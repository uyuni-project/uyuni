#
# spec file for package susemanager-tftpsync-recv
#
# Copyright (c) 2024 SUSE LLC
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


Name:           susemanager-tftpsync-recv
Version:        5.1.0
Release:        0
Summary:        Reciever for SUSE Manager tftp sync
License:        GPL-2.0-only
Group:          Applications/System
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
%if 0%{?suse_version}
Requires(pre):  apache2
%else
Requires(pre):  httpd
%endif
Requires(pre):  tftp
Requires:       python3
Requires:       spacewalk-backend
Requires:       spacewalk-proxy-common
Requires:       (apache2-mod_wsgi or python3-mod_wsgi)
Requires(pre):  coreutils
BuildRequires:  uyuni-base-common
Requires(pre):  uyuni-base-common

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
if [ -f /etc/apache2/conf.d/susemanager-tftpsync-recv.conf.rpmnew ]; then
    PARENT_FQDN=$( egrep -m1 "^proxy.rhn_parent[[:space:]]*=" /etc/rhn/rhn.conf | sed 's/^proxy.rhn_parent[[:space:]]*=[[:space:]]*\(.*\)/\1/' || echo "" )
    if [ -z "$PARENT_FQDN" ]; then
            echo "Could not determine SUSE Manager Parent Hostname! Got /etc/rhn/rhn.conf vanished?" >&2;
            exit 1;
    fi;

    SUMA_IP=$(getent hosts $PARENT_FQDN | awk '{ print $1 }' || echo "")

    sed -i "s/^[[:space:]]*Allow from[[:space:]].*$/        Allow from $SUMA_IP/" /etc/apache2/conf.d/susemanager-tftpsync-recv.conf.rpmnew
    sed -i "s/^[[:space:]#]*Require ip[[:space:]].*$/        Require ip $SUMA_IP/" /etc/apache2/conf.d/susemanager-tftpsync-recv.conf.rpmnew

    mv /etc/apache2/conf.d/susemanager-tftpsync-recv.conf.rpmnew /etc/apache2/conf.d/susemanager-tftpsync-recv.conf
fi

%files
%defattr(-,wwwrun,root,-)
%dir %{_var}/log/tftpsync
%config(noreplace) %{_sysconfdir}/apache2/conf.d/susemanager-tftpsync-recv.conf

%defattr(-,root,root,-)
%doc answers.txt COPYING README
%dir /srv/www/tftpsync
%dir /%{_datadir}/rhn
/srv/www/tftpsync/add
/srv/www/tftpsync/delete
%{_sbindir}/configure-tftpsync.sh
%{_datadir}/rhn/config-defaults/rhn_tftpsync.conf

%changelog
