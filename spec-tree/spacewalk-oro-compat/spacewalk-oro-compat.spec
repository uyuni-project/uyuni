
Summary: Provide of oro for new jakarta-oro packages
Name: spacewalk-oro-compat
Version: 1.0
Release: 2%{?dist}
License: GPLv2
Group: System Environment/Base
URL:          https://fedorahosted.org/spacewalk
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-buildroot
BuildArch: noarch

Provides: oro
Requires: jakarta-oro

%description
This package Provides oro for jakarta-oro packages that don't provide them.

%prep

%build

%install

%clean

%files

%changelog
* Fri Apr 20 2012 Michael Calmer <mc@suse.de> 1.0-2
- use old tito with different builder
- New spacewalk-oro-compat package

* Wed Dec 07 2011 Jan Pazdziora 1.0-1
- Initial version.

