Name:           susemanager-branding-oss
Version:        1.7.1
Release:        1%{?dist}
Summary:        SUSE Manager branding oss specific files
Group:          Applications/System
License:        GPLv2
URL:            http://www.novell.com
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch:      noarch
Provides:       susemanager-branding = %{version}-%{release}
Conflicts:      susemanager-branding-non-oss

%description
A collection of files which are specific for
SUSE Manager oss flavors.


%prep
%setup -q

%build

%install
mkdir -p $RPM_BUILD_ROOT/srv/www/htdocs/help/
install -m 644 eula.pxt $RPM_BUILD_ROOT/srv/www/htdocs/help/

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%doc license.txt
%dir /srv/www/htdocs/help
/srv/www/htdocs/help/eula.pxt

%changelog

