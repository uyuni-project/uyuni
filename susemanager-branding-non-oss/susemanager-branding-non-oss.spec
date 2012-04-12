Name:           susemanager-branding-non-oss
Version:        1.7.2
Release:        1%{?dist}
Summary:        SUSE Manager non-oss branding specific files
Group:          Applications/System
License:        GPLv2
URL:            http://www.novell.com
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch:      noarch
Provides:       susemanager-branding = %{version}
Conflicts:      otherproviders(susemanager-branding)
Supplements:    oracle-server
Requires:       oracle-server

%description
A collection of files which are specific for
SUSE Manager non-oss flavors.


%prep
%setup -q

%build

%install
mkdir -p $RPM_BUILD_ROOT/srv/www/htdocs/help/
mkdir -p $RPM_BUILD_ROOT/%_defaultdocdir/susemanager/
install -m 644 eula121.pxt $RPM_BUILD_ROOT/srv/www/htdocs/help/eula.pxt
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

