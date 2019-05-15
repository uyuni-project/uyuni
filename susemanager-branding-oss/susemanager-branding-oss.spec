#
# spec file for package susemanager-branding-oss
#
# Copyright (c) 2019 SUSE LINUX GmbH, Nuernberg, Germany.
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


Name:           susemanager-branding-oss
Version:        4.0.3
Release:        1%{?dist}
Summary:        SUSE Manager branding oss specific files
License:        GPL-2.0-only
Group:          Applications/System
Url:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
Provides:       susemanager-branding = %{version}
Conflicts:      otherproviders(susemanager-branding)
Conflicts:      oracle-server

%description
A collection of files which are specific for
SUSE Manager oss flavors.


%prep
%setup -q

%build

%install
mkdir -p $RPM_BUILD_ROOT/srv/www/htdocs/help/
mkdir -p $RPM_BUILD_ROOT/%_defaultdocdir/susemanager/
# final license
install -m 644 eula.html $RPM_BUILD_ROOT/srv/www/htdocs/help/
install -m 644 license.txt $RPM_BUILD_ROOT/%_defaultdocdir/susemanager/

%files
%defattr(-,root,root,-)
%docdir %_defaultdocdir/susemanager
%dir %_defaultdocdir/susemanager
%_defaultdocdir/susemanager/license.txt
%dir /srv/www/htdocs/help
/srv/www/htdocs/help/eula.html

%changelog
