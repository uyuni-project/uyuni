Name:           sm-ncc-sync-data
Version:        1.2.0
Release:        1%{?dist}
Summary:        SUSE Manager specific scripts
Group:          Productivity/Other
License:        GPLv2
URL:            http://www.novell.com
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch:      noarch

%description 
This package contains data files with NCC information

%prep
%setup -q

%build

%install

mkdir -p %{buildroot}/usr/share/susemanager
mkdir -p %{buildroot}/srv/www/htdocs/pub/
install -m 0644 channel_families.xml %{buildroot}/usr/share/susemanager/channel_families.xml
install -m 0644 channels.xml         %{buildroot}/usr/share/susemanager/channels.xml
install -m 0644 res.key              %{buildroot}/srv/www/htdocs/pub/

%clean
rm -rf %{buildroot}


%files
%defattr(-,root,root,-)
%dir /usr/share/susemanager
%dir /srv/www/htdocs/pub
/usr/share/susemanager/channel_families.xml
/usr/share/susemanager/channels.xml
/srv/www/htdocs/pub/res.key

%changelog

