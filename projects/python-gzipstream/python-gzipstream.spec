#
# spec file for package python-gzipstream
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


%if ! (0%{?fedora} || 0%{?rhel} > 5)
%{!?python_sitelib: %define python_sitelib %(%{__python} -c "from distutils.sysconfig import get_python_lib; print get_python_lib()")}
%endif

%if 0%{?fedora} || 0%{?suse_version} > 1320
%global build_py3   1
%endif

Summary:        Streaming zlib (gzip) support for python
License:        GPL-2.0-only
Group:          Development/Languages/Python
Name:           python-gzipstream
Version:        4.0.4
Release:        1%{?dist}
Url:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/python-gzipstream-%{version}.tar.gz
%if ! (0%{?suse_version} && 0%{?suse_version} <= 1110)
BuildArch:      noarch
%endif
%if (0%{?fedora} > 27 || 0%{?rhel} > 7)
BuildRequires:  python2-devel
%else
BuildRequires:  python-devel
%endif
BuildRoot:      %{_tmppath}/%{name}-%{version}-build

%global _description\
A streaming gzip handler.\
gzipstream.GzipStream extends the functionality of the gzip.GzipFile class\
to allow the processing of streaming data.\

%description %_description

%package -n python2-gzipstream
Summary:        %summary
Group:          Development/Languages/Python
%if 0%{?fedora}
%{?python_provide:%python_provide python2-gzipstream}
%else
Provides:       python-gzipstream = %{version}-%{release}
Obsoletes:      python-gzipstream < %{version}-%{release}
%endif

%description -n python2-gzipstream %_description

%if 0%{?build_py3}
%package -n python3-gzipstream
Summary:        %summary
Group:          Development/Languages/Python
BuildRequires:  python3-devel
%if 0%{?suse_version}
BuildRequires:  python-rpm-macros
%endif

%description -n python3-gzipstream %_description

%endif

%prep
%setup -q
mkdir ../py3
cp -a . ../py3

%build
%{__python} setup.py build
%if 0%{?build_py3}
cd ../py3
%{__python3} setup.py build

%endif

%install
%{__python} setup.py install -O1 --skip-build --root $RPM_BUILD_ROOT --prefix %{_usr}
%if 0%{?build_py3}
cd ../py3
%{__python3} setup.py install -O1 --skip-build --root $RPM_BUILD_ROOT --prefix %{_usr}

%endif

%files -n python2-gzipstream
%defattr(-,root,root)
%{python_sitelib}/*
%doc html LICENSE

%if 0%{?build_py3}
%files -n python3-gzipstream
%defattr(-,root,root)
%{python3_sitelib}/*
%doc html LICENSE

%endif

%if 0%{?build_py3}
%files -n python3-gzipstream
%defattr(-,root,root)
%{python3_sitelib}/*
%doc html
%license LICENSE

%endif

%changelog
