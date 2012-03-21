Name:           suseRegisterInfo
Version:        1.7.1
Release:        1%{?dist}
Summary:        Tool to get informations from the local system
Group:          Productivity/Other
License:        GPLv2
URL:            http://www.novell.com
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
#BuildArch:      noarch
BuildRequires:  python-devel

Requires:       perl
Requires:       python
%{!?python_sitelib: %define python_sitelib %(%{__python} -c "from distutils.sysconfig import get_python_lib; print get_python_lib()")}

%if 0%{?suse_version}
Requires: suseRegister >= 1.4
%else
Requires: suseRegisterRES
%endif

%description
This tool read data from the local system required
for a registration

%prep
%setup -q

%build

%install
make -C suseRegister install PREFIX=$RPM_BUILD_ROOT
mkdir -p %{buildroot}/usr/lib/suseRegister/bin/
install -m 0755 suseRegister/suse_register_info.pl %{buildroot}/usr/lib/suseRegister/bin/suse_register_info

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%dir /usr/lib/suseRegister
%dir /usr/lib/suseRegister/bin
/usr/lib/suseRegister/bin/suse_register_info
%{python_sitelib}/suseRegister

%changelog
