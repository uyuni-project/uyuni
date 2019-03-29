#
# spec file for package susemanager-schema
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


%{!?fedora: %global sbinpath /sbin}%{?fedora: %global sbinpath %{_sbindir}}

Name:           susemanager-schema
Summary:        SQL schema for Spacewalk server
License:        GPL-2.0-only
Group:          Applications/Internet

Version:        4.0.10
Release:        1%{?dist}
Source0:        %{name}-%{version}.tar.gz
Source1:        %{name}-rpmlintrc

URL:            https://github.com/uyuni-project/uyuni
BuildArch:      noarch
BuildRoot:      %{_tmppath}/%{name}-%{version}-build

BuildRequires:  /usr/bin/pod2man
BuildRequires:  fdupes
BuildRequires:  python
BuildRequires:  perl(Digest::SHA)
Requires:       %{sbinpath}/restorecon

Provides:       spacewalk-schema = %{version}
Obsoletes:      rhn-satellite-schema <= 5.1.0

%if 0%{?suse_version}
BuildRequires:  fdupes
%endif

%define rhnroot /etc/sysconfig/rhn/
%define oracle %{rhnroot}/oracle
%define postgres %{rhnroot}/postgres

%description
susemanager-schema is the SQL schema for the SUSE Manager server.

%package sanity
Summary:        Schema source sanity check for Spacewalk database scripts.
Group:          Applications/Internet

Requires:       perl(Digest::SHA)

%description sanity
Provides schema-source-sanity-check.pl script for external usage.

%prep

%setup -q

%build
%if 0%{?fedora} || 0%{?rhel} >= 7 || 0%{?suse_version} >= 1110
find . -name '*.91' | while read i ; do mv $i ${i%%.91} ; done
%endif
make -f Makefile.schema SCHEMA=%{name} VERSION=%{version} RELEASE=%{release}
pod2man spacewalk-schema-upgrade spacewalk-schema-upgrade.1
pod2man spacewalk-sql spacewalk-sql.1

%install
install -m 0755 -d $RPM_BUILD_ROOT%{rhnroot}
install -m 0755 -d $RPM_BUILD_ROOT%{oracle}
install -m 0755 -d $RPM_BUILD_ROOT%{postgres}
install -m 0644 oracle/main.sql $RPM_BUILD_ROOT%{oracle}
install -m 0644 postgres/main.sql $RPM_BUILD_ROOT%{postgres}
install -m 0644 oracle/end.sql $RPM_BUILD_ROOT%{oracle}/upgrade-end.sql
install -m 0644 postgres/end.sql $RPM_BUILD_ROOT%{postgres}/upgrade-end.sql
install -m 0755 -d $RPM_BUILD_ROOT%{_bindir}
install -m 0755 spacewalk-schema-upgrade $RPM_BUILD_ROOT%{_bindir}
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

%if 0%{?suse_version}
%post
if [ $1 -eq 2 ] ; then
    cp /usr/share/susemanager/update-messages.txt /var/adm/update-messages/%{name}-%{version}-%{release}
else
    # new install: empty messages are not shown
    touch /var/adm/update-messages/%{name}-%{version}-%{release}
fi
%endif

%files
%defattr(-,root,root)
%dir %{rhnroot}
%{oracle}
%{postgres}
%{rhnroot}/schema-upgrade
%{_bindir}/spacewalk-schema-upgrade
%{_bindir}/spacewalk-sql
%{_mandir}/man1/spacewalk-schema-upgrade*
%{_mandir}/man1/spacewalk-sql*
%if 0%{?suse_version}
%dir /usr/share/susemanager
/usr/share/susemanager/update-messages.txt
%ghost /var/adm/update-messages/%{name}-%{version}-%{release}
%endif

%files sanity
%defattr(-,root,root)
%attr(755,root,root) %{_bindir}/schema-source-sanity-check.pl

%changelog
