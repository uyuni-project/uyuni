#
# Copyright (c) 2013 SUSE LINUX Products GmbH, Nuernberg, Germany.
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

%global bootstrap_version 3.0.3
%global jquery_version 1.10.2
%global less_version 1.6.0

Name:           susemanager-frontend-libs
Version:        2.1
Release:        0
# Make sure you review this when adding more libraries
License:        MIT; Apache-2.0
Summary:        Web libraries used by SUSE Manager
Url:            https://www.suse.com/products/suse-manager
Group:          Applications/Internet
Source0:        http://code.jquery.com/jquery-%{jquery_version}.min.js
Source1:        https://github.com/twbs/bootstrap/archive/v%{bootstrap_version}.tar.gz
Source2:        https://raw.github.com/less/less.js/master/dist/less-%{less_version}.min.js
Provides:       susemanager(jquery) = %{jquery_version}
Provides:       susemanager(less) = %{less_version}
Provides:       susemanager(twitter-bootstrap) = %{bootstrap_version}
BuildRoot:      %{_tmppath}/%{name}-%{version}-build

%description
This package contains all the front-end dependencies of SUSE Manager
like web/javascript libraries.

%package devel
Requires:       %{name} = %{version}-%{release}
Summary:        LESS files to build SUSE Manager CSS and others
%description devel

%prep
tar xpvf %{SOURCE1}

%build

%install
%{__mkdir_p} %{buildroot}/srv/www/htdocs/javascript
%{__install} -m 644 %{SOURCE0} %{buildroot}/srv/www/htdocs/javascript/jquery.js

# we don't install bootstrap (dist/ directory) as SUSE Manager only depends on bootstrap
# at compile time (spacewalk.less includes bootstrap).
# We put the bootstrap less files in a devel sub-package
%{__mkdir_p} %{buildroot}%{_datadir}/%{name}/bootstrap
cp -r bootstrap-%{bootstrap_version}/less %{buildroot}%{_datadir}/%{name}/bootstrap

# but we do require the javascript part of bootstrap
%{__install} -m 644 bootstrap-%{bootstrap_version}/dist/js/bootstrap.min.js %{buildroot}/srv/www/htdocs/javascript/bootstrap.js

# less.js is intended for building only. but we need it in the htdocs path for SUSE Manager development mode
# where less is rendered on the client side.
%{__install} -m 644 %{SOURCE2} %{buildroot}/srv/www/htdocs/javascript/less.js
%{__mkdir_p} %{buildroot}%{_datadir}/%{name}/less
%{__ln_s} /srv/www/htdocs/javascript/less.js %{buildroot}%{_datadir}/%{name}/less/less.js


%files
%defattr(-,root,root)
/srv/www/htdocs/javascript/bootstrap.js
/srv/www/htdocs/javascript/jquery.js
/srv/www/htdocs/javascript/less.js

%files devel
%defattr(-,root,root)
%{_datadir}/%{name}

%changelog
