#
# spec file for package susemanager-branding-oss
#
# Copyright (c) 2024 SUSE LLC
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


%global susemanager_shared_path /usr/share/susemanager
%global wwwroot %{susemanager_shared_path}/www
%global wwwdocroot %{wwwroot}/htdocs

Name:           susemanager-branding-oss
Version:        5.0.3
Release:        1
Summary:        SUSE Manager branding oss specific files
License:        GPL-2.0-only
Group:          Applications/System
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
# This package is not needed for Uyuni, so we do not build it if
# the OS is openSUSE or anything else that is not SLE
%if 0%{?is_opensuse} || (!0%{?sle_version} && !0%{?is_opensuse})
ExcludeArch:    i586 x86_64 ppc64le s390x aarch64
%endif
Provides:       susemanager-branding = %{version}
Conflicts:      oracle-server
Conflicts:      otherproviders(susemanager-branding)

%description
A collection of files which are specific for
SUSE Manager oss flavors.

%prep
%setup -q

%build
echo "dummy" > license.txt
echo "<html><head><title>dummy</title></head><body>dummy</body></html>" > eula.html
#cp /usr/share/licenses/product/SUSE-Manager-Server/license.txt license.txt
#echo "<p>" > eula.html
#cat license.txt | sed 's/^$/<\/p><p>/' >> eula.html
#echo "</p>" >> eula.html

%install
mkdir -p $RPM_BUILD_ROOT/%{wwwdocroot}/help/
mkdir -p $RPM_BUILD_ROOT/%_defaultdocdir/susemanager/
# final license
install -m 644 eula.html $RPM_BUILD_ROOT/%{wwwdocroot}/help/
install -m 644 license.txt $RPM_BUILD_ROOT/%_defaultdocdir/susemanager/

%files
%defattr(-,root,root,-)
%docdir %_defaultdocdir/susemanager
%dir %_defaultdocdir/susemanager
%_defaultdocdir/susemanager/license.txt
%dir %{susemanager_shared_path}
%dir %{wwwroot}
%dir %{wwwdocroot}
%dir %{wwwdocroot}/help
%{wwwdocroot}/help/eula.html

%changelog
