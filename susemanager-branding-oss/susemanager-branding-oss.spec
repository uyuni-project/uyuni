Name:           susemanager-branding-oss
Version:        1.7.4
Release:        1%{?dist}
Summary:        SUSE Manager branding oss specific files
Group:          Applications/System
License:        GPLv2
URL:            http://www.novell.com
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
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
install -m 644 eula.pxt $RPM_BUILD_ROOT/srv/www/htdocs/help/
install -m 644 license.txt $RPM_BUILD_ROOT/%_defaultdocdir/susemanager/


%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%docdir %_defaultdocdir/susemanager
%dir %_defaultdocdir/susemanager
%_defaultdocdir/susemanager/license.txt
%dir /srv/www/htdocs/help
/srv/www/htdocs/help/eula.pxt

%changelog

