#
# spec file for package spacewalk-certs-tools
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
# needsbinariesforbuild


# package renaming fun :(
%define rhn_client_tools spacewalk-client-tools
%define rhn_setup	 spacewalk-client-setup
%define rhn_check	 spacewalk-check
%define rhnsd		 spacewalksd
#
%if 0%{?suse_version}
%global pub_bootstrap_dir /srv/www/htdocs/pub/bootstrap
%else
%global pub_bootstrap_dir /var/www/html/pub/bootstrap
%endif
%global rhnroot %{_datadir}/rhn

%if 0%{?fedora} || 0%{?suse_version} > 1320
%global build_py3   1
%global default_py3 1
%endif

%define pythonX %{?default_py3: python3}%{!?default_py3: python2}

Name:           spacewalk-certs-tools
Summary:        Spacewalk SSL Key/Cert Tool
License:        GPL-2.0-only
Group:          Applications/Internet
Version:        4.0.4
Release:        1%{?dist}
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
Requires(pre):  %{pythonX}-%{name} = %{version}-%{release}
Requires:       openssl
Requires:       rpm-build
Requires:       spacewalk-base-minimal-config
%if 0%{?suse_version} || 0%{?rhel} >= 5
Requires:       %{rhn_client_tools}
%endif
Requires:       sudo
Requires:       tar
BuildRequires:  docbook-utils
%if 0%{?suse_version}
BuildRequires:  filesystem
Requires:       susemanager-build-keys-web
%endif
%if 0%{?build_py3}
Requires(post): python3-spacewalk-backend-libs
Requires(post): python3-rhnlib
Requires(post): python3-rpm
%else
Requires(post): spacewalk-backend-libs
Requires(post): rhnlib
Requires(post): rpm-python
%endif
Obsoletes:      rhns-certs < 5.3.0
Obsoletes:      rhns-certs-tools < 5.3.0
# can not provides = %{version} since some old packages expect > 3.6.0
Provides:       rhns-certs = 5.3.0
Provides:       rhns-certs-tools = 5.3.0

%description
This package contains tools to generate the SSL certificates required by
Spacewalk.

%package -n python2-%{name}
Summary:        Spacewalk SSL Key/Cert Tool
Group:          Applications/Internet
Requires:       %{name} = %{version}-%{release}
Requires:       python2-rhn-client-tools
Requires:       spacewalk-backend-libs >= 0.8.28
%if 0%{?rhel} && 0%{?rhel} <= 5
Requires:       python-hashlib
%endif
BuildRequires:  python

%description -n python2-%{name}
Python 2 specific files for %{name}.

%if 0%{?build_py3}
%package -n python3-%{name}
Summary:        Spacewalk SSL Key/Cert Tool
Group:          Applications/Internet
Requires:       %{name} = %{version}-%{release}
Requires:       python3-rhn-client-tools
Requires:       python3-spacewalk-backend-libs
BuildRequires:  python3
BuildRequires:  python3-rpm-macros

%description -n python3-%{name}
Python 3 specific files for %{name}.
%endif

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
install -d -m 755 $RPM_BUILD_ROOT/%{rhnroot}/certs
make -f Makefile.certs install PREFIX=$RPM_BUILD_ROOT ROOT=%{rhnroot} \
    PYTHONPATH=%{python_sitelib} PYTHONVERSION=%{python_version} \
    MANDIR=%{_mandir} PUB_BOOTSTRAP_DIR=%{pub_bootstrap_dir}
%if 0%{?build_py3}
sed -i 's|#!/usr/bin/python|#!/usr/bin/python3|' rhn-ssl-tool mgr-package-rpm-certificate-osimage rhn-bootstrap
make -f Makefile.certs install PREFIX=$RPM_BUILD_ROOT ROOT=%{rhnroot} \
    PYTHONPATH=%{python3_sitelib} PYTHONVERSION=%{python3_version} \
    MANDIR=%{_mandir} PUB_BOOTSTRAP_DIR=%{pub_bootstrap_dir}
%endif

%define default_suffix %{?default_py3:-%{python3_version}}%{!?default_py3:-%{python_version}}
ln -s rhn-ssl-tool%{default_suffix} $RPM_BUILD_ROOT%{_bindir}/rhn-ssl-tool
ln -s rhn-bootstrap%{default_suffix} $RPM_BUILD_ROOT%{_bindir}/rhn-bootstrap

%if 0%{?suse_version}
ln -s rhn-bootstrap $RPM_BUILD_ROOT/%{_bindir}/mgr-bootstrap
ln -s rhn-ssl-tool $RPM_BUILD_ROOT/%{_bindir}/mgr-ssl-tool
ln -s rhn-sudo-ssl-tool $RPM_BUILD_ROOT/%{_bindir}/mgr-sudo-ssl-tool
ln -s spacewalk-push-register $RPM_BUILD_ROOT/%{_sbindir}/mgr-push-register
ln -s spacewalk-ssh-push-init $RPM_BUILD_ROOT/%{_sbindir}/mgr-ssh-push-init

%py_compile -O %{buildroot}/%{python_sitelib}
%if 0%{?build_py3}
%py3_compile -O %{buildroot}/%{python3_sitelib}
%endif
%endif

%post
case "$1" in
  2)
       if [ ! -f /usr/share/susemanager/salt/images/rhn-org-trusted-ssl-cert-osimage-1.0-1.noarch.rpm ]; then
               /usr/sbin/mgr-package-rpm-certificate-osimage
       fi
  ;;
esac

%files
%defattr(-,root,root,-)
%dir %{rhnroot}/certs
%attr(755,root,root) %{rhnroot}/certs/sign.sh
%attr(755,root,root) %{rhnroot}/certs/gen-rpm.sh
%attr(755,root,root) %{rhnroot}/certs/update-ca-cert-trust.sh
%attr(755,root,root) %{_bindir}/rhn-sudo-ssl-tool
%{_bindir}/rhn-ssl-tool
%{_bindir}/rhn-bootstrap
%attr(755,root,root) %{_sbindir}/spacewalk-push-register
%attr(755,root,root) %{_sbindir}/spacewalk-ssh-push-init
%attr(755,root,root) %{_sbindir}/mgr-package-rpm-certificate-osimage
%doc %{_mandir}/man1/rhn-*.1*
%doc %{_mandir}/man1/mgr-*.1*
%doc LICENSE
%doc ssl-howto-simple.txt ssl-howto.txt
%{pub_bootstrap_dir}/client_config_update.py*
%if 0%{?suse_version}
%dir %{rhnroot}
%dir /srv/www/htdocs/pub
%dir %{pub_bootstrap_dir}
%{_bindir}/mgr-bootstrap
%{_bindir}/mgr-ssl-tool
%{_bindir}/mgr-sudo-ssl-tool
%{_sbindir}/mgr-push-register
%{_sbindir}/mgr-ssh-push-init
%{_sbindir}/mgr-package-rpm-certificate-osimage
%endif

%files -n python2-%{name}
%{python_sitelib}/certs
%attr(755,root,root) %{_bindir}/rhn-ssl-tool-%{python_version}
%attr(755,root,root) %{_bindir}/rhn-bootstrap-%{python_version}

%if 0%{?build_py3}
%files -n python3-%{name}
%{python3_sitelib}/certs
%attr(755,root,root) %{_bindir}/rhn-ssl-tool-%{python3_version}
%attr(755,root,root) %{_bindir}/rhn-bootstrap-%{python3_version}
%endif

%changelog
