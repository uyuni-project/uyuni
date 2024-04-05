#
# spec file for package spacewalk-web
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


%define shared_path %{_datadir}/susemanager
%define shared_www_path %{shared_path}/www
%define www_path %{shared_www_path}/htdocs

%if 0%{?suse_version}
%define apache_group www
%else
%define apache_group apache
%endif

%{!?rhel: %global sbinpath /sbin}%{?rhel: %global sbinpath %{_sbindir}}
%{!?nodejs_sitelib:%define nodejs_sitelib %{_prefix}/lib/node_modules}

Name:           spacewalk-web
Summary:        Spacewalk Web site - Perl modules
License:        GPL-2.0-only
Group:          Applications/Internet
Version:        5.0.6
Release:        0
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/uyuni-project/uyuni/archive/%{name}-%{version}.tar.gz
Source1:        node-modules.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
Requires(pre):  uyuni-base-common
BuildRequires:  gettext
BuildRequires:  make
BuildRequires:  spacewalk-backend
BuildRequires:  uyuni-base-common
BuildRequires:  perl(ExtUtils::MakeMaker)
BuildRequires:  nodejs >= 20

%if 0%{?suse_version}
BuildRequires:  apache2
%else
BuildRequires:  perl-macros
BuildRequires:  perl-srpm-macros
%endif

%description
This package contains the code for the Spacewalk Web Site.
Normally this source RPM does not generate a %{name} binary package,
but it does generate a number of sub-packages.

%package -n spacewalk-html
Summary:        HTML document files for Spacewalk
License:        (MPL-2.0 OR Apache-2.0) AND 0BSD AND BSD-3-Clause AND GPL-2.0-only AND ISC AND LGPL-3.0-or-later AND MIT AND MPL-2.0
Group:          Applications/Internet
Requires:       httpd
Requires:       spacewalk-branding
Obsoletes:      rhn-help < 5.3.0
Provides:       rhn-help = 5.3.0
Obsoletes:      rhn-html < 5.3.0
Provides:       rhn-html = 5.3.0
Obsoletes:      susemanager-web-libs < %{version}
Provides:       susemanager-web-libs = %{version}
Obsoletes:      susemanager-nodejs-sdk-devel < %{version}
Provides:       susemanager-nodejs-sdk-devel = %{version}
# files html/javascript/{builder.js,controls.js,dragdrop.js,effects.js,
# prototype-1.6.0.js,scriptaculous.js,slider.js,sound.js,unittest.js}
# are licensed under MIT license

%description -n spacewalk-html
This package contains the HTML files for the Spacewalk web site.

%package -n spacewalk-html-debug
Summary:        HTML document debug files for Spacewalk
License:        GPL-2.0-only AND MIT
Group:          Applications/Internet
Requires:       spacewalk-html

%description -n spacewalk-html-debug
This package contains the debug files for spacewalk-html.

%package -n spacewalk-base
Summary:        Programs which need to be installed for the Spacewalk Web base classes
License:        GPL-2.0-only
Group:          Applications/Internet
Provides:       spacewalk(spacewalk-base) = %{version}-%{release}
%if 0%{?suse_version}
Requires:       susemanager-frontend-libs
%if 0%{?suse_version} >= 1500
Requires:       python3-PyJWT
Requires:       python3-numpy
Requires:       python3-websockify
%else
Requires:       python-PyJWT
Requires:       python-numpy
Requires:       python-websockify
%endif
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
pushd html/src
tar xf %{S:1}
popd

%build
make -f Makefile.spacewalk-web PERLARGS="INSTALLDIRS=vendor" %{?_smp_mflags}
pushd html/src
mkdir -p %{buildroot}%{nodejs_sitelib}
cp -pr node_modules/* %{buildroot}%{nodejs_sitelib}
node build/yarn/yarn-1.22.17.js build:novalidate
popd
rm -rf %{buildroot}%{nodejs_sitelib}
sed -i -r "s/^(web.buildtimestamp *= *)_OBS_BUILD_TIMESTAMP_$/\1$(date +'%%Y%%m%%d%%H%%M%%S')/" conf/rhn_web.conf

%install
make -C modules install DESTDIR=$RPM_BUILD_ROOT PERLARGS="INSTALLDIRS=vendor" %{?_smp_mflags}
make -C html install PREFIX=$RPM_BUILD_ROOT INSTALL_DEST=%{www_path}
make -C po install PREFIX=$RPM_BUILD_ROOT

find $RPM_BUILD_ROOT -type f -name perllocal.pod -exec rm -f {} \;
find $RPM_BUILD_ROOT -type f -name .packlist -exec rm -f {} \;

mkdir -p $RPM_BUILD_ROOT/%{www_path}/pub
mkdir -p $RPM_BUILD_ROOT/%{_prefix}/share/rhn/config-defaults
mkdir -p $RPM_BUILD_ROOT/%{_sysconfdir}/init.d
mkdir -p $RPM_BUILD_ROOT/%{_sysconfdir}/httpd/conf
mkdir -p $RPM_BUILD_ROOT/%{_sysconfdir}/cron.daily

install -m 644 conf/rhn_web.conf $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults
install -m 644 conf/rhn_dobby.conf $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults
install -m 755 modules/dobby/scripts/check-database-space-usage.sh $RPM_BUILD_ROOT/%{_sysconfdir}/cron.daily/check-database-space-usage.sh

if grep -F 'product_name' %{_datadir}/rhn/config-defaults/rhn.conf | grep 'SUSE Manager' >/dev/null; then
  SUMA_REL=$(echo %{version} | awk -F. '{print $1"."$2}')
  SUMA_FULL_REL=$(sed -n 's/web\.version\s*=\s*\(.*\)/\1/p' $RPM_BUILD_ROOT%{_datadir}/rhn/config-defaults/rhn_web.conf)
  echo "SUSE Manager release $SUMA_REL ($SUMA_FULL_REL)" > $RPM_BUILD_ROOT/%{_sysconfdir}/susemanager-release
else
  UYUNI_REL=$(sed -n 's/web\.version.uyuni\s*=\s*\(.*\)/\1/p' $RPM_BUILD_ROOT%{_datadir}/rhn/config-defaults/rhn_web.conf)
  echo "Uyuni release $UYUNI_REL" > $RPM_BUILD_ROOT/%{_sysconfdir}/uyuni-release
fi

%{__mkdir_p} %{buildroot}/%{www_path}/css
%{__mkdir_p} %{buildroot}/%{www_path}/fonts
%{__mkdir_p} %{buildroot}/%{www_path}/img
%{__mkdir_p} %{buildroot}/%{www_path}/javascript
pushd html/src/dist
cp -pR css %{buildroot}/%{www_path}
cp -pR fonts %{buildroot}/%{www_path}
cp -pR img %{buildroot}/%{www_path}
cp -pR javascript %{buildroot}/%{www_path}
popd

# Adjust default theme for SUSE Manager
%if 0%{?sle_version} && ! (0%{?is_opensuse} || 0%{?rhel} || 0%{?fedora})
sed -i -e 's/^web.theme_default =.*$/web.theme_default = susemanager-light/' $RPM_BUILD_ROOT%{_prefix}/share/rhn/config-defaults/rhn_web.conf
%endif

%find_lang spacewalk-web

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
%{_sysconfdir}/*-release
%license LICENSE

%files -n spacewalk-base-minimal-config
%defattr(644,root,root,755)
%dir %{_prefix}/share/rhn
%attr(644,root,%{apache_group}) %{_prefix}/share/rhn/config-defaults/rhn_web.conf

%files -n spacewalk-dobby
%defattr(644,root,root,755)
%attr(755,root,root) %{_bindir}/db-control
%{_mandir}/man1/db-control.1.gz
%{perl_vendorlib}/Dobby.pm
%attr(644,root,root) %{_prefix}/share/rhn/config-defaults/rhn_dobby.conf
%attr(0755,root,root) %{_sysconfdir}/cron.daily/check-database-space-usage.sh
%{perl_vendorlib}/Dobby/
%dir %{_prefix}/share/rhn

%files -n spacewalk-html -f spacewalk-web.lang
%defattr(644,root,root,755)
%dir %{shared_path}
%dir %{shared_www_path}
%dir %{www_path}
%dir %{www_path}/css
%{www_path}/css/*.{css,js}
%dir %{www_path}/css/legacy
%{www_path}/css/legacy/*.css
%dir %{www_path}/fonts
%{www_path}/fonts/*
%dir %{www_path}/img
%{www_path}/img/*.{gif,ico,jpeg,jpg,png,svg}
%{www_path}/robots.txt
%{www_path}/pub
%dir %{www_path}/javascript
%{www_path}/javascript/*.js
%dir %{www_path}/javascript/manager
%{www_path}/javascript/manager/*.{js,js.LICENSE.txt,css}
%dir %{www_path}/javascript/legacy
%{www_path}/javascript/legacy/*.{js,js.LICENSE.txt,css}
%dir %{www_path}/javascript/legacy/ace-editor
%{www_path}/javascript/legacy/ace-editor/*
%license LICENSE

%files -n spacewalk-html-debug
%defattr(644,root,root,755)
%dir %{www_path}/css
%{www_path}/css/*.map
%dir %{www_path}/javascript
%dir %{www_path}/javascript/manager
%{www_path}/javascript/manager/*.map

%changelog
