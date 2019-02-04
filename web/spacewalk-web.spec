#
# spec file for package spacewalk-web
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


%if 0%{?suse_version}
%define www_path /srv/
%define apache_user wwwrun
%define apache_group www
%else
%define www_path %{_var}
%define apache_user apache
%define apache_group apache
%endif
%{!?fedora: %global sbinpath /sbin}%{?fedora: %global sbinpath %{_sbindir}}

Name:           spacewalk-web
Summary:        Spacewalk Web site - Perl modules
License:        GPL-2.0-only
Group:          Applications/Internet
Version:        4.0.5
Release:        1%{?dist}
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
BuildRequires:  perl(ExtUtils::MakeMaker)
%if 0%{?suse_version}
BuildRequires:  apache2
BuildRequires:  nodejs-packaging
BuildRequires:  susemanager-nodejs-sdk-devel
BuildRequires:  nodejs

%endif

%description
This package contains the code for the Spacewalk Web Site.
Normally this source RPM does not generate a %{name} binary package,
but it does generate a number of sub-packages.

%package -n susemanager-web-libs
Summary:        Vendor bundles for spacewalk-web
License:		BSD-3-Clause and MIT
Group:          Applications/Internet

BuildArch:      noarch
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildRequires:  nodejs-packaging
BuildRequires:  susemanager-nodejs-sdk-devel
BuildRequires:  nodejs

%description -n susemanager-web-libs
This package contains Vendor bundles needed for spacewalk-web

%package -n spacewalk-html
Summary:        HTML document files for Spacewalk
License:        GPL-2.0-only AND MIT
Group:          Applications/Internet
Requires:       httpd
Requires:       spacewalk-branding
Requires:       susemanager-web-libs
Obsoletes:      rhn-help < 5.3.0
Provides:       rhn-help = 5.3.0
Obsoletes:      rhn-html < 5.3.0
Provides:       rhn-html = 5.3.0
# files html/javascript/{builder.js,controls.js,dragdrop.js,effects.js,
# prototype-1.6.0.js,scriptaculous.js,slider.js,sound.js,unittest.js}
# are licensed under MIT license

%description -n spacewalk-html
This package contains the HTML files for the Spacewalk web site.


%package -n spacewalk-base
Summary:        Programs which need to be installed for the Spacewalk Web base classes
License:        GPL-2.0-only
Group:          Applications/Internet
Provides:       spacewalk(spacewalk-base) = %{version}-%{release}
%if 0%{?suse_version}
Requires:       susemanager-frontend-libs
%endif
Requires:       httpd
Requires:       sudo
Requires:       perl(Params::Validate)
Requires:       perl(XML::LibXML)
Obsoletes:      rhn-base < 5.3.0
Obsoletes:      spacewalk-grail < %{version}
Obsoletes:      spacewalk-pxt < %{version}
Obsoletes:      spacewalk-sniglets < %{version}
Provides:       rhn-base = 5.3.0

%description -n spacewalk-base
This package includes the core RHN:: packages necessary to manipulate the
database.  This includes RHN::* and RHN::DB::*.


%package -n spacewalk-base-minimal
Summary:        Core of Perl modules for %{name} package
License:        GPL-2.0-only
Group:          Applications/Internet
Provides:       spacewalk(spacewalk-base-minimal) = %{version}-%{release}
Obsoletes:      rhn-base-minimal < 5.3.0
Provides:       rhn-base-minimal = 5.3.0
Requires:       perl(DBI)
Requires:       perl(Params::Validate)

%description -n spacewalk-base-minimal
Independent Perl modules in the RHN:: name-space.
These are very basic modules needed to handle configuration files, database,
sessions and exceptions.

%package -n spacewalk-base-minimal-config
Summary:        Configuration for %{name} package
License:        GPL-2.0-only
Group:          Applications/Internet
Provides:       spacewalk(spacewalk-base-minimal-config) = %{version}-%{release}
Requires:       httpd
Requires:       spacewalk-base-minimal = %{version}-%{release}

%description -n spacewalk-base-minimal-config
Configuration file for spacewalk-base-minimal package.


%package -n spacewalk-dobby
Summary:        Perl modules and scripts to administer a PostgreSQL database
License:        GPL-2.0-only
Group:          Applications/Internet
Requires:       perl-Filesys-Df
Obsoletes:      rhn-dobby < 5.3.0
Provides:       rhn-dobby = 5.3.0
Requires:       %{sbinpath}/runuser
Conflicts:      spacewalk-oracle

%description -n spacewalk-dobby
Dobby is collection of Perl modules and scripts to administer a PostgreSQL
database.


%prep
%setup -q

%build
make -f Makefile.spacewalk-web PERLARGS="INSTALLDIRS=vendor" %{?_smp_mflags}
%if 0%{?suse_version}
pushd html/src
ln -sf %{nodejs_sitelib} .
node build
popd
%endif

%install
make -C modules install DESTDIR=$RPM_BUILD_ROOT PERLARGS="INSTALLDIRS=vendor" %{?_smp_mflags}
make -C html install PREFIX=$RPM_BUILD_ROOT

find $RPM_BUILD_ROOT -type f -name perllocal.pod -exec rm -f {} \;
find $RPM_BUILD_ROOT -type f -name .packlist -exec rm -f {} \;

mkdir -p $RPM_BUILD_ROOT/%{www_path}/www/htdocs/pub
mkdir -p $RPM_BUILD_ROOT/%{_prefix}/share/rhn/config-defaults
mkdir -p $RPM_BUILD_ROOT/%{_sysconfdir}/init.d
mkdir -p $RPM_BUILD_ROOT/%{_sysconfdir}/httpd/conf
mkdir -p $RPM_BUILD_ROOT/%{_sysconfdir}/cron.daily
mkdir -p %{buildroot}/%{_sysconfdir}/sysconfig/SuSEfirewall2.d/services

install -m 644 conf/rhn_web.conf $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults
install -m 644 conf/rhn_dobby.conf $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults
install -m 755 modules/dobby/scripts/check-database-space-usage.sh $RPM_BUILD_ROOT/%{_sysconfdir}/cron.daily/check-database-space-usage.sh
install -m 0644 etc/sysconfig/SuSEfirewall2.d/services/susemanager-database %{buildroot}/%{_sysconfdir}/sysconfig/SuSEfirewall2.d/services/

%{__mkdir_p} %{buildroot}/srv/www/htdocs/javascript/manager
cp -r html/src/dist/javascript/manager %{buildroot}/srv/www/htdocs/javascript

%{__mkdir_p} %{buildroot}/srv/www/htdocs/vendors
cp html/src/dist/vendors/vendors.bundle.js %{buildroot}/srv/www/htdocs/vendors/vendors.bundle.js
cp html/src/dist/vendors/vendors.bundle.js.LICENSE %{buildroot}/srv/www/htdocs/vendors/vendors.bundle.js.LICENSE

%files -n susemanager-web-libs
%defattr(644,root,root,755)
%dir %{www_path}/www/htdocs/vendors
%{www_path}/www/htdocs/vendors/*

%files -n spacewalk-base
%defattr(644,root,root,755)
%dir %{perl_vendorlib}/RHN
%{perl_vendorlib}/RHN.pm

%files -n spacewalk-base-minimal
%defattr(644,root,root,755)
%dir %{perl_vendorlib}/RHN
%dir %{perl_vendorlib}/PXT
%{perl_vendorlib}/RHN/SimpleStruct.pm
%{perl_vendorlib}/RHN/Exception.pm
%{perl_vendorlib}/RHN/DB.pm
%{perl_vendorlib}/RHN/DBI.pm
%{perl_vendorlib}/PXT/Config.pm
%doc LICENSE

%files -n spacewalk-base-minimal-config
%defattr(644,root,root,755)
%dir %{_prefix}/share/rhn
%dir %attr(755,root,%{apache_group}) %{_prefix}/share/rhn/config-defaults
%attr(644,root,%{apache_group}) %{_prefix}/share/rhn/config-defaults/rhn_web.conf

%files -n spacewalk-dobby
%defattr(644,root,root,755)
%attr(755,root,root) %{_bindir}/db-control
%{_mandir}/man1/db-control.1.gz
%{perl_vendorlib}/Dobby.pm
%attr(644,root,root) %{_prefix}/share/rhn/config-defaults/rhn_dobby.conf
%attr(0755,root,root) %{_sysconfdir}/cron.daily/check-database-space-usage.sh
%config %{_sysconfdir}/sysconfig/SuSEfirewall2.d/services/susemanager-database
%{perl_vendorlib}/Dobby/
%dir %{_prefix}/share/rhn
%dir %attr(755,root,%{apache_group}) %{_prefix}/share/rhn/config-defaults

%files -n spacewalk-html
%defattr(644,root,root,755)
%dir %{www_path}/www/htdocs/pub
%dir %{www_path}/www/htdocs/javascript
%{www_path}/www/htdocs/robots.txt
%{www_path}/www/htdocs/pub
%{www_path}/www/htdocs/javascript/*
%doc LICENSE

%changelog
