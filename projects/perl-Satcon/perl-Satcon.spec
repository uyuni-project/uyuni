#
# spec file for package perl-Satcon
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


%{!?fedora: %global sbinpath /sbin}%{?fedora: %global sbinpath %{_sbindir}}

Name:           perl-Satcon
Summary:        Framework for configuration files
License:        GPL-2.0-only
Group:          Applications/System
Version:        4.0.3
Release:        1%{?dist}
Url:            https://github.com/uyuni-project/uyuni
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
Requires:       perl(:MODULE_COMPAT_%(eval "`%{__perl} -V:version`"; echo $version))
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRequires:  perl(ExtUtils::MakeMaker)
%if 0%{?suse_version}
Requires:       policycoreutils
%else
%if 0%{?fedora} && 0%{?fedora} >= 24
BuildRequires:  coreutils
BuildRequires:  findutils
BuildRequires:  make
%if 0%{?fedora} && 0%{?fedora} > 26
BuildRequires:  perl-interpreter
%else
BuildRequires:  perl
%endif
BuildRequires:  perl-generators
# Run-time:
# bytes not used at tests
# Data::Dumper not used at tests
# File::Find not used at tests
# File::Path not used at tests
# Getopt::Long not used at tests
BuildRequires:  perl(strict)
# Tests:
BuildRequires:  perl(Test)
%endif
Requires:       %{sbinpath}/restorecon
%endif

%description
Framework for generating config files during installation.
This package include Satcon perl module and supporting applications.

%prep
%setup -q

%build
%{__perl} Makefile.PL INSTALLDIRS=vendor
make %{?_smp_mflags}

%install

make pure_install PERL_INSTALL_ROOT=$RPM_BUILD_ROOT

find $RPM_BUILD_ROOT -type f -name .packlist -exec rm -f {} \;
find $RPM_BUILD_ROOT -depth -type d -exec rmdir {} 2>/dev/null \;

%{_fixperms} $RPM_BUILD_ROOT/*

%check
make test

%files
%doc README LICENSE
%{perl_vendorlib}/*
%{_bindir}/*

%changelog
