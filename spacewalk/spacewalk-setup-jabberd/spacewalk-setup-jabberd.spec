#
# spec file for package spacewalk-setup-jabberd
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


Name:           spacewalk-setup-jabberd
Version:        4.0.3
Release:        1%{?dist}
Summary:        Tools to setup jabberd for Spacewalk
License:        GPL-2.0-only
Group:          Applications/System
Url:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
PreReq:         sqlite3
%if 0%{?fedora} && 0%{?fedora} > 26
BuildRequires:  perl-interpreter
%else
BuildRequires:  perl
%endif
BuildRequires:  jabberd
BuildRequires:  sqlite3
BuildRequires:  perl(ExtUtils::MakeMaker)
BuildArch:      noarch
%if 0%{?fedora} && 0%{?fedora} > 26
Requires:       perl-interpreter
%else
Requires:       perl
%endif
%if 0%{?suse_version}
Requires:       jabberd-sqlite
%endif
Requires(post): libxslt-tools

%description
Script, which sets up Jabberd for Spacewalk. Used during installation of
Spacewalk server or Spacewalk proxy.

%prep
%setup -q

%build
%{__perl} Makefile.PL INSTALLDIRS=vendor
make %{?_smp_mflags}

%install
make pure_install PERL_INSTALL_ROOT=%{buildroot}
find %{buildroot} -type f -name .packlist -exec rm -f {} ';'
find %{buildroot} -type d -depth -exec rmdir {} 2>/dev/null ';'
chmod -R u+w %{buildroot}/*
install -d -m 755 %{buildroot}/%{_datadir}/spacewalk/setup/
install -d -m 755 %{buildroot}/%{_datadir}/spacewalk/setup/jabberd
install -m 0644 share/jabberd/* %{buildroot}/%{_datadir}/spacewalk/setup/jabberd/
install -m 0744 include/* %{buildroot}/%{_datadir}/spacewalk/setup/jabberd/

# jabberd ssl cert location
install -d -m 755 %{buildroot}/%{_sysconfdir}/pki/spacewalk/jabberd

%check
make test

%files
%defattr(-,root,root,-)
%doc LICENSE
%{_bindir}/spacewalk-setup-jabberd
%{_mandir}/man1/*
%dir %{_datadir}/spacewalk
%{_datadir}/spacewalk/*
%dir %{_sysconfdir}/pki
%{_sysconfdir}/pki/spacewalk

%post
/usr/share/spacewalk/setup/jabberd/manage_database -s >/dev/null ||:

%changelog
