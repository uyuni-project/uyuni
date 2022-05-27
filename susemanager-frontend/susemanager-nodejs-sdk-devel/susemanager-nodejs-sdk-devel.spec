#
# Copyright (c) 2019 SUSE LLC
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

Name:           susemanager-nodejs-sdk-devel
Version:        4.1.13
Release:        1%{?dist}

License:        Apache-2.0 and BSD-2-Clause and BSD-3-Clause and MIT and CC-BY-3.0 and ISC and SUSE-Public-Domain and WTFPL

Summary:        Node.js software used by SUSE Manager at build time
Url:            https://www.suse.com/products/suse-manager
Group:          Development/Languages/Other

Source0:        susemanager-nodejs-sdk-devel.tar.gz
Source1:        susemanager-nodejs-modules.tar.gz

BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildRequires:  fdupes
BuildRequires:  nodejs-packaging

%description
This package contains Node.js software needed by SUSE Manager at build time.

%prep
%setup -q
tar xfv %{S:1}

%build
find . -type f -exec sed -i -e 's/#!\/usr\/bin\/env node/#!\/usr\/bin\/node/g' {} \;

%install
mkdir -p %{buildroot}%{nodejs_sitelib}
mkdir -p %{buildroot}%{_bindir}
cp -pr node_modules/* %{buildroot}%{nodejs_sitelib}

chmod +x %{buildroot}%{nodejs_sitelib}/webpack/bin/*
ln -sf ./webpack.js %{buildroot}/%{nodejs_sitelib}/webpack/bin/webpack

find %{buildroot}%{nodejs_sitelib} -name "*~" -delete
find %{buildroot}%{nodejs_sitelib} -name ".*" -type d -exec rm -rf {} +
find %{buildroot}%{nodejs_sitelib} -name ".*" -delete
%fdupes %{buildroot}%{nodejs_sitelib}

# Compress all the node modules to reduce the huge number of files to package
pushd %{buildroot}%{nodejs_sitelib}
tar czf ../all_modules.tar.gz *
tar tf ../all_modules.tar.gz | cut -d '/' -f1 | sort | uniq | while read -r MODULE; do rm -rf $MODULE; done
mv ../all_modules.tar.gz .
popd

%files
%defattr(-,root,root,-)
%dir %{nodejs_modulesdir}
%{nodejs_sitelib}/*

%changelog
