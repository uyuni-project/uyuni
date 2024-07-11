#
# spec file for package spacewalk-proxy-html
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
%global htmldir /srv/www/htdocs
%else
%global htmldir %{_var}/www/html
%endif

Name:           spacewalk-proxy-html
Summary:        The HTML component for Spacewalk Proxy
License:        GPL-2.0-only
Group:          Applications/Internet
Version:        5.1.0
Release:        0
URL:            https://github.com/uyuni-project/uyuni
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
install -d -m 755 $RPM_BUILD_ROOT%{htmldir}/sources
install -d -m 755 %{proxy_dir_name}/sources/css $RPM_BUILD_ROOT%{htmldir}/sources/css
install -d -m 755 %{proxy_dir_name}/sources/fonts $RPM_BUILD_ROOT%{htmldir}/sources/fonts
install -d -m 755 %{proxy_dir_name}/sources/img $RPM_BUILD_ROOT%{htmldir}/sources/img
cp -pR %{proxy_dir_name}/sources/css/* $RPM_BUILD_ROOT%{htmldir}/sources/css/
cp -pR %{proxy_dir_name}/sources/fonts/* $RPM_BUILD_ROOT%{htmldir}/sources/fonts/
cp -pR %{proxy_dir_name}/sources/img/* $RPM_BUILD_ROOT%{htmldir}/sources/img/
cp -pR %{proxy_dir_name}/*.html $RPM_BUILD_ROOT%{htmldir}/

%files
%defattr(-,root,root)
%dir %{htmldir}
%{htmldir}/index.html
%{htmldir}/sources
%license LICENSE

%changelog
