#
# spec file for package spacewalk-certs-tools
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
# needsbinariesforbuild


%if 0%{?suse_version}
%global pub_dir /srv/www/htdocs/pub
%else
%global pub_dir %{_localstatedir}/www/html/pub
%endif

%global pub_bootstrap_dir %{pub_dir}/bootstrap
%global rhnroot %{_datadir}/rhn
%global __python %{_bindir}/python3

Name:           spacewalk-certs-tools
Version:        5.1.0
Release:        0
Summary:        Spacewalk SSL Key/Cert Tool
License:        GPL-2.0-only
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Applications/Internet
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/uyuni-project/uyuni/archive/%{name}-%{version}.tar.gz
BuildRequires:  docbook-utils
BuildRequires:  make
Requires:       openssl
Requires:       rpm-build
Requires:       spacewalk-base-minimal-config
Requires:       sudo
Requires:       tar
Requires(post): python3-rhnlib
Requires(post): python3-rpm
Requires(post): python3-uyuni-common-libs
Requires(pre):  python3-%{name} = %{version}-%{release}
BuildArch:      noarch
%if 0%{?suse_version}
BuildRequires:  filesystem
Requires:       susemanager-build-keys-web
%endif

%description
This package contains tools to generate the SSL certificates required by
Spacewalk.

%package -n python3-%{name}
Summary:        Spacewalk SSL Key/Cert Tool
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Applications/Internet
BuildRequires:  python3
BuildRequires:  python3-rpm-macros
Requires:       %{name} = %{version}-%{release}
Requires:       python3-uyuni-common-libs
Requires:       spacewalk-backend

%description -n python3-%{name}
Python 3 specific files for %{name}.

%prep
%setup -q

%build
#nothing to do here

%if 0%{?suse_version}
# we need to rewrite etc/httpd/conf => etc/apache2
sed -i 's|etc/httpd/conf|etc/apache2|g' rhn_ssl_tool.py
sed -i 's|etc/httpd/conf|etc/apache2|g' sslToolConfig.py
sed -i 's|etc/httpd/conf|etc/apache2|g' sign.sh
sed -i 's|etc/httpd/conf|etc/apache2|g' ssl-howto.txt
%endif

%install
install -d -m 755 %{buildroot}%{rhnroot}/certs

sed -i '1s|python\b|python3|' rhn-ssl-tool mgr-package-rpm-certificate-osimage rhn-bootstrap
make -f Makefile.certs install PREFIX=%{buildroot} ROOT=%{rhnroot} \
    PYTHONPATH=%{python3_sitelib} PYTHONVERSION=%{python3_version} \
    MANDIR=%{_mandir} PUB_BOOTSTRAP_DIR=%{pub_bootstrap_dir}

ln -s rhn-ssl-tool-%{python3_version} %{buildroot}%{_bindir}/rhn-ssl-tool
ln -s mgr-ssl-cert-setup-%{python3_version} %{buildroot}%{_bindir}/mgr-ssl-cert-setup
ln -s rhn-bootstrap-%{python3_version} %{buildroot}%{_bindir}/rhn-bootstrap
ln -s mgr-ssl-tool.1.gz %{buildroot}%{_mandir}/man1/rhn-ssl-tool.1.gz
ln -s mgr-bootstrap.1.gz %{buildroot}%{_mandir}/man1/rhn-bootstrap.1.gz

ln -s rhn-bootstrap %{buildroot}%{_bindir}/mgr-bootstrap
ln -s rhn-ssl-tool %{buildroot}%{_bindir}/mgr-ssl-tool
ln -s rhn-sudo-ssl-tool %{buildroot}%{_bindir}/mgr-sudo-ssl-tool

%if 0%{?suse_version}
%py3_compile -O %{buildroot}/%{python3_sitelib}
%endif

%files
%defattr(-,root,root,-)
%dir %{rhnroot}/certs
%attr(755,root,root) %{rhnroot}/certs/sign.sh
%attr(755,root,root) %{rhnroot}/certs/gen-rpm.sh
%attr(755,root,root) %{rhnroot}/certs/update-ca-cert-trust.sh
%attr(755,root,root) %{_bindir}/rhn-sudo-ssl-tool
%{_bindir}/rhn-ssl-tool
%{_bindir}/mgr-ssl-cert-setup
%{_bindir}/rhn-bootstrap
%attr(755,root,root) %{_sbindir}/mgr-package-rpm-certificate-osimage
%doc %{_mandir}/man1/rhn-*.1*
%doc %{_mandir}/man1/mgr-*.1*
%doc ssl-howto-simple.txt ssl-howto.txt
%license LICENSE
%dir %{rhnroot}
%dir %{pub_dir}
%dir %{pub_bootstrap_dir}
%{_bindir}/mgr-bootstrap
%{_bindir}/mgr-ssl-tool
%{_bindir}/mgr-sudo-ssl-tool

%files -n python3-%{name}
%{python3_sitelib}/certs
%attr(755,root,root) %{_bindir}/rhn-ssl-tool-%{python3_version}
%attr(755,root,root) %{_bindir}/rhn-bootstrap-%{python3_version}
%attr(755,root,root) %{_bindir}/mgr-ssl-cert-setup-%{python3_version}

%changelog
