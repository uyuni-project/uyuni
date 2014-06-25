Name:           select2-bootstrap-css
Version:        1.3.0
Release:        4%{?dist}
Summary:        CSS to make Select2 fit in with Bootstrap 3.

Group:          Applications/Internet
License:        MIT
URL:            http://fk.github.io/select2-bootstrap-css/
Source0:        https://github.com/t0m/%{name}/archive/v%{version}.tar.gz#/%{name}-%{version}.tar.gz
BuildRoot:      %(mktemp -ud %{_tmppath}/%{name}-%{version}-%{release}-XXXXXX)
BuildArch:      noarch

%description
CSS to make Select2 fit in with Bootstrap 3 – ready for use in original, LESS, Sass and Compass flavors.

%prep
%setup -q

%build

%install
rm -rf %{buildroot}
install -d -m 755 %{buildroot}%{_var}/www/html/javascript/select2
install -m 644 select2-bootstrap.css %{buildroot}%{_var}/www/html/javascript/select2

%clean
rm -rf %{buildroot}


%files
%defattr(-,root,root,-)
%{_var}/www/html/javascript/select2/select2-bootstrap.css


%changelog
* Fri May 30 2014 Milan Zazrivec <mzazrivec@redhat.com> 1.3.0-4
- fix file path

* Fri May 30 2014 Milan Zazrivec <mzazrivec@redhat.com> 1.3.0-3
- typo fix

* Fri May 30 2014 Milan Zazrivec <mzazrivec@redhat.com> 1.3.0-2
- typo fix

* Fri May 30 2014 Milan Zazrivec <mzazrivec@redhat.com> 1.3.0-1
- initial select2-bootstrap-css build

