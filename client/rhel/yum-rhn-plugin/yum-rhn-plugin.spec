#
# spec file for package yum-rhn-plugin
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


# package renaming fun :(
%define rhn_client_tools spacewalk-client-tools
%define rhn_setup	 spacewalk-client-setup
%define rhn_check	 spacewalk-check
%define rhnsd		 spacewalksd
#
Summary:        Spacewalk support for yum
License:        GPL-2.0-only
Group:          System Environment/Base
Name:           yum-rhn-plugin
Version:        4.0.3
Release:        1%{?dist}
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
URL:            https://github.com/uyuni-project/uyuni
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
%if %{?suse_version: %{?suse_version} > 1110} %{!?suse_version:1}
BuildArch:      noarch
%endif
%if 0%{?fedora} >= 28
BuildRequires:  python2
%else
BuildRequires:  python
%endif
BuildRequires:  gettext
BuildRequires:  intltool

Requires:       %{rhn_client_tools} >= 2.8.4
Requires:       yum >= 3.2.19-15
%if 0%{?suse_version}
Requires:       python-m2crypto >= 0.16-6
%else
Requires:       m2crypto >= 0.16-6
%endif
%if 0%{?fedora} >= 28
Requires:       python2-iniparse
%else
Requires:       python-iniparse
%endif

# Not really, but for upgrades we need these
Requires:       %{rhn_setup} >= 2.8.4
Obsoletes:      up2date < 5.0.0
Provides:       up2date = 5.0.0

%description
This yum plugin provides support for yum to access a Spacewalk server for
software updates.

%prep
%setup -q

%build
make -f Makefile.yum-rhn-plugin

%install
make -f Makefile.yum-rhn-plugin install VERSION=%{version}-%{release} PREFIX=$RPM_BUILD_ROOT MANPATH=%{_mandir} PYTHONPATH=%{python_sitelib}

# remove all unsupported translations
cd $RPM_BUILD_ROOT
for d in usr/share/locale/*; do
  if [ ! -d "/$d" ]; then
    rm -rfv "./$d"
  fi
done
cd -

%find_lang %{name}

%pre
# 682820 - re-enable yum-rhn-plugin after package upgrade if the system is already registered
export pluginconf='/etc/yum/pluginconf.d/rhnplugin.conf'
if [ $1 -gt 1 ] && [ -f /etc/sysconfig/rhn/systemid ] && [ -f "$pluginconf" ]; then
    if grep -q '^[[:space:]]*enabled[[:space:]]*=[[:space:]]*1[[:space:]]*$' \
       "$pluginconf"; then
        echo "1" > /etc/enable-yum-rhn-plugin
    fi
fi

%post
# 682820 - re-enable yum-rhn-plugin after package upgrade if the system is already registered
export pluginconf='/etc/yum/pluginconf.d/rhnplugin.conf'
if [ $1 -gt 1 ] && [ -f "$pluginconf" ] && [ -f "/etc/enable-yum-rhn-plugin" ]; then
    sed -i '/\[main]/,/^$/{/enabled/s/0/1/}' "$pluginconf"
    rm -f /etc/enable-yum-rhn-plugin
fi

%files -f %{name}.lang
%defattr(-,root,root,-)
%verify(not md5 mtime size) %config(noreplace) %{_sysconfdir}/yum/pluginconf.d/rhnplugin.conf
%dir /var/lib/up2date
%{_mandir}/man*/*
%{_datadir}/yum-plugins/*
%{python_sitelib}/rhn/actions/*
%doc LICENSE
%dir /etc/yum
%dir /etc/yum/pluginconf.d
%dir /usr/share/yum-plugins
%dir %{python_sitelib}/rhn
%dir %{python_sitelib}/rhn/actions

%changelog
