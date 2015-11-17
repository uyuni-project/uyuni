#
# spec file for package susemanager-sls
#
# Copyright (c) 2015 SUSE LINUX GmbH, Nuernberg, Germany.
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

Name:           susemanager-sls
Version:        0.1.0
Release:        0
License:        GPL-2.0
Summary:        Static Salt state files for SUSE Manager
Group:          Applications/Internet
Source:         %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch

%description
Static Salt state files for SUSE Manager, where generic operations are
provided for the integration between infrastructure components.

%prep
%setup -q

%build

%install
mkdir -p %{buildroot}/usr/share/susemanager/salt
cp -R salt/* %{buildroot}/usr/share/susemanager/salt

%post
# HACK! Create broken link when it will be replaces with the real file
ln -s /srv/www/htdocs/pub/RHN-ORG-TRUSTED-SSL-CERT \
      /usr/share/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT 2>&1 || exit 0

%files
%defattr(-,root,root)
%dir /usr/share/susemanager
/usr/share/susemanager/salt
%ghost /usr/share/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT
