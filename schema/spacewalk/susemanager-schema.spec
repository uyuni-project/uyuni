#
# spec file for package susemanager-schema
#
# Copyright (c) 2021 SUSE LLC
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


%{!?fedora: %global sbinpath /sbin}%{?fedora: %global sbinpath %{_sbindir}}

Name:           susemanager-schema
Summary:        SQL schema for Spacewalk server
License:        GPL-2.0-only
Group:          Applications/Internet

Version:        4.3.25
Release:        0
Source0:        https://github.com/uyuni-project/uyuni/archive/%{name}-%{version}-0.tar.gz
Source1:        https://raw.githubusercontent.com/uyuni-project/uyuni/%{name}-%{version}-0/schema/spacewalk/%{name}-rpmlintrc

URL:            https://github.com/uyuni-project/uyuni
BuildArch:      noarch
BuildRoot:      %{_tmppath}/%{name}-%{version}-build

BuildRequires:  /usr/bin/pod2man
BuildRequires:  fdupes
BuildRequires:  python3
BuildRequires:  perl(Digest::SHA)
BuildRequires:  perl(File::Find)
BuildRequires:  perl-macros
Requires:       %{sbinpath}/restorecon
Requires:       %{name}-utility

Provides:       spacewalk-schema = %{version}
Obsoletes:      rhn-satellite-schema <= 5.1.0

%if 0%{?suse_version}
BuildRequires:  fdupes
%endif

%define rhnroot /etc/sysconfig/rhn/
%define postgres %{rhnroot}/postgres
%define spacewalk_folder Spacewalk
%define schema_upgrade_folder %{spacewalk_folder}/SchemaUpgrade

%description
susemanager-schema is the SQL schema for the SUSE Manager server.

%package sanity
Summary:        Schema source sanity check for Spacewalk database scripts
Group:          Applications/Internet

Requires:       perl(Digest::SHA)

%package utility
Summary:        Utility used by any DB schema in Spacewalk
Group:          Applications/Internet

%description sanity
Provides schema-source-sanity-check.pl script for external usage.

%description utility
Provides spacewalk-schema-upgrade and spacewalk-sql.

%prep

%setup -q

%build
make -f Makefile.schema SCHEMA=%{name} VERSION=%{version} RELEASE=%{release}
pod2man spacewalk-schema-upgrade spacewalk-schema-upgrade.1
pod2man spacewalk-sql spacewalk-sql.1

%install
install -m 0755 -d $RPM_BUILD_ROOT%{rhnroot}
install -m 0755 -d $RPM_BUILD_ROOT%{postgres}
install -m 0644 postgres/main.sql $RPM_BUILD_ROOT%{postgres}
install -m 0644 postgres/end.sql $RPM_BUILD_ROOT%{postgres}/upgrade-end.sql
install -m 0755 -d $RPM_BUILD_ROOT%{_bindir}
install -m 0755 spacewalk-schema-upgrade $RPM_BUILD_ROOT%{_bindir}
install -m 0755 -d $RPM_BUILD_ROOT%{perl_vendorlib}/%{schema_upgrade_folder}
install -m 0755 lib/%{schema_upgrade_folder}/MainDb.pm $RPM_BUILD_ROOT%{perl_vendorlib}/%{schema_upgrade_folder}
install -m 0755 lib/%{schema_upgrade_folder}/ReportDb.pm $RPM_BUILD_ROOT%{perl_vendorlib}/%{schema_upgrade_folder}

install -m 0755 spacewalk-sql $RPM_BUILD_ROOT%{_bindir}
install -m 0755 -d $RPM_BUILD_ROOT%{rhnroot}/schema-upgrade
( cd upgrade && tar cf - --exclude='*.sql' . | ( cd $RPM_BUILD_ROOT%{rhnroot}/schema-upgrade && tar xf - ) )
mkdir -p $RPM_BUILD_ROOT%{_mandir}/man1
cp -p spacewalk-schema-upgrade.1 $RPM_BUILD_ROOT%{_mandir}/man1
cp -p spacewalk-sql.1 $RPM_BUILD_ROOT%{_mandir}/man1

%if 0%{?suse_version}
mkdir -p $RPM_BUILD_ROOT/usr/share/susemanager/
install -m 0644 update-messages.txt $RPM_BUILD_ROOT/usr/share/susemanager/
%fdupes %{buildroot}/%{rhnroot}
%endif

install -m 755 schema-source-sanity-check.pl $RPM_BUILD_ROOT%{_bindir}/schema-source-sanity-check.pl
install -m 755 blend $RPM_BUILD_ROOT%{_bindir}/blend

%if 0%{?suse_version}
%post
if [ $1 -eq 2 ] ; then
    cp /usr/share/susemanager/update-messages.txt /var/adm/update-messages/%{name}-%{version}-%{release}
else
    # new install: empty messages are not shown
    touch /var/adm/update-messages/%{name}-%{version}-%{release}
fi
%endif

%posttrans
systemctl is-active --quiet uyuni-check-database.service && {
  echo "  Running DB schema upgrade. This may take a while."
  echo "  Call the following command to see progress: journalctl -f -u uyuni-check-database.service"
} ||:
systemctl try-restart uyuni-check-database.service ||:

%files
%defattr(-,root,root)
%dir %{rhnroot}
%{postgres}
%{rhnroot}/schema-upgrade
%if 0%{?suse_version}
%dir /usr/share/susemanager
/usr/share/susemanager/update-messages.txt
%ghost /var/adm/update-messages/%{name}-%{version}-%{release}
%endif

%files utility
%defattr(-,root,root)
%dir %{perl_vendorlib}/%{spacewalk_folder}
%dir %{perl_vendorlib}/%{schema_upgrade_folder}
%{perl_vendorlib}/%{schema_upgrade_folder}/MainDb.pm
%{perl_vendorlib}/%{schema_upgrade_folder}/ReportDb.pm
%{_bindir}/spacewalk-schema-upgrade
%{_bindir}/spacewalk-sql
%{_mandir}/man1/spacewalk-schema-upgrade*
%{_mandir}/man1/spacewalk-sql*

%files sanity
%defattr(-,root,root)
%attr(755,root,root) %{_bindir}/schema-source-sanity-check.pl
%attr(755,root,root) %{_bindir}/blend

%changelog
