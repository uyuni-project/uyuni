#
# spec file for package susemanager-tftpsync
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
%define pythonX %{?build_py3:python3}%{!?build_py3:python2}
%define python_sitelib %(%{pythonX} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")

Name:           susemanager-tftpsync
Version:        4.0.4
Release:        1%{?dist}
Summary:        Sync cobbler created tftp enviroment to SUSE Manager Proxies
License:        LGPL-2.1-only
Group:          Applications/System
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build

Requires(pre):  cobbler
%if 0%{?build_py3}
Requires:       python3
Requires:       python3-six
BuildRequires:  python3-devel
%else
Requires:       python
Requires:       python-six
BuildRequires:  python-devel
%endif

%description
Add a cobbler trigger module which sync the cobbler created tftp enviroment
to the configured proxies.

%prep
%setup -q

%build
# Fixing shebang for Python 3
%if 0%{?build_py3}
for i in `find . -type f`;
do
    sed -i '1s=^#!/usr/bin/\(python\|env python\)[0-9.]*=#!/usr/bin/python3=' $i;
done
%endif

%install
install -p -D -m 644 sync_post_tftpd_proxies.py %{buildroot}%{python_sitelib}/cobbler/modules/sync_post_tftpd_proxies.py
install -p -D -m 644 MultipartPostHandler.py %{buildroot}%{python_sitelib}/cobbler/MultipartPostHandler.py
install -p -D -m 755 configure-tftpsync.sh  %{buildroot}%{_sbindir}/configure-tftpsync.sh

%if 0%{?suse_version}
%if 0%{?build_py3}
%py3_compile %{buildroot}/
%py3_compile -O %{buildroot}/
%else
%py_compile %{buildroot}/
%py_compile -O %{buildroot}/
%endif
%endif

%post
if [ -f "/etc/cobbler/settings" ]; then
  if ! grep "tftpsync_timeout:" /etc/cobbler/settings >/dev/null; then
    echo "" >> /etc/cobbler/settings
    echo "tftpsync_timeout: 15" >> /etc/cobbler/settings
    echo "" >> /etc/cobbler/settings
  fi
fi

%files
%defattr(-,root,root,-)
%doc COPYING.LIB README
%{python_sitelib}/cobbler/modules/sync_post_tftpd_proxies.py*
%{python_sitelib}/cobbler/MultipartPostHandler.py*
%{_sbindir}/configure-tftpsync.sh
%if 0%{?build_py3}
%{python_sitelib}/cobbler/__pycache__/*
%{python_sitelib}/cobbler/modules/__pycache__/*
%endif

%changelog
