#
# spec file for package spacewalk-proxy-html
#
# Copyright (c) 2019 SUSE LINUX GmbH, Nuernberg, Germany.
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
%global htmldir /srv/www/htdocs
%else
%global htmldir %{_var}/www/html
%endif

Name:           spacewalk-proxy-html
Summary:        The HTML component for Spacewalk Proxy
License:        GPL-2.0-only
Group:          Applications/Internet
Version:        4.2.1
Release:        1%{?dist}
Url:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
Obsoletes:      rhns-proxy-html < 5.3.0
Provides:       rhns-proxy-html = 5.3.0
Requires:       httpd

%description
This package contains placeholder html pages, which the Spacewalk Server
displays, if you navigate to it using your browser.


%if 0%{?sle_version} && !0%{?is_opensuse}
%define proxy_dir_name suse_proxy
%else
%define proxy_dir_name uyuni_proxy
%endif

%prep
%setup -q

%build
#nothing to do here

%install
install -m 755 -d $RPM_BUILD_ROOT%{htmldir}
install -m 755 -d $RPM_BUILD_ROOT%{htmldir}/proxy
install -m 644 %{proxy_dir_name}/* $RPM_BUILD_ROOT%{htmldir}/proxy/

%files
%defattr(-,root,root)
%dir %{htmldir}/proxy
%config %{htmldir}/proxy/index.html
%{htmldir}/proxy/*.ico
%{htmldir}/proxy/*.png
%doc LICENSE
%if 0%{?suse_version}
%dir %dir %{htmldir}/proxy
%endif

%changelog
