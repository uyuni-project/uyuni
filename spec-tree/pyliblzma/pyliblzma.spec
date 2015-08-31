%if ! (0%{?fedora} > 12 || 0%{?rhel} > 5)
%{!?python_sitelib: %global python_sitelib %(%{__python} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")}
%{!?python_sitearch: %global python_sitearch %(%{__python} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib(1))")}
%endif


Summary:    Python bindings for lzma
Name:       pyliblzma
Version:    0.5.3
Release:    14%{?dist}
License:    LGPLv3+
URL:        https://launchpad.net/pyliblzma
Source0:    http://pypi.python.org/packages/source/p/pyliblzma/%{name}-%{version}.tar.bz2
Patch0:     no-script-liblzma.patch

BuildRequires:    xz-devel python-setuptools python2-devel
BuildRequires:    python-test
BuildRoot:    %{_tmppath}/%{name}-%{version}-%{release}-buildroot

%description
PylibLZMA provides a python interface for the liblzma library
to read and write data that has been compressed or can be decompressed
by Lasse Collin's lzma utils.

%prep
%setup -qn %{name}-%{version}

%patch0 -p1 

%build
%{__python} setup.py build

%check
%{__python} setup.py test

%install
rm -rf %{buildroot}
%{__python} setup.py install --root=%{buildroot}

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root)
%{!?_licensedir:%global license %%doc}
%license COPYING
%doc README THANKS ChangeLog NEWS
%attr(0755,-,-) %{python_sitearch}/lzma.so
%{python_sitearch}/liblzma.py*
%{python_sitearch}/%{name}*.egg-info

%changelog
* Mon Aug 31 2015 Tomas Lestach <tlestach@redhat.com> 0.5.3-14
- new package built with tito

* Sun Aug 17 2014 Fedora Release Engineering <rel-eng@lists.fedoraproject.org> - 0.5.3-13
- Rebuilt for https://fedoraproject.org/wiki/Fedora_21_22_Mass_Rebuild

* Thu Jul 31 2014 Tom Callaway <spot@fedoraproject.org> - 0.5.3-12
- fix license handling

* Sat Jun 07 2014 Fedora Release Engineering <rel-eng@lists.fedoraproject.org> - 0.5.3-11
- Rebuilt for https://fedoraproject.org/wiki/Fedora_21_Mass_Rebuild

* Wed Aug 28 2013 Dennis Gilmore <dennis@ausil.us> - 0.5.3-10
- Buildrequire python-test to fix FTBFS bz#992848

* Sun Aug 04 2013 Fedora Release Engineering <rel-eng@lists.fedoraproject.org> - 0.5.3-9
- Rebuilt for https://fedoraproject.org/wiki/Fedora_20_Mass_Rebuild

* Thu Feb 14 2013 Fedora Release Engineering <rel-eng@lists.fedoraproject.org> - 0.5.3-8
- Rebuilt for https://fedoraproject.org/wiki/Fedora_19_Mass_Rebuild

* Sat Jul 21 2012 Fedora Release Engineering <rel-eng@lists.fedoraproject.org> - 0.5.3-7
- Rebuilt for https://fedoraproject.org/wiki/Fedora_18_Mass_Rebuild

* Sat Jan 14 2012 Fedora Release Engineering <rel-eng@lists.fedoraproject.org> - 0.5.3-6
- Rebuilt for https://fedoraproject.org/wiki/Fedora_17_Mass_Rebuild

* Tue Feb 08 2011 Fedora Release Engineering <rel-eng@lists.fedoraproject.org> - 0.5.3-5
- Rebuilt for https://fedoraproject.org/wiki/Fedora_15_Mass_Rebuild

* Tue Jul 27 2010 David Malcolm <dmalcolm@redhat.com> - 0.5.3-4
- Rebuilt for https://fedoraproject.org/wiki/Features/Python_2.7/MassRebuild

* Fri Jun  4 2010 Seth Vidal <skvidal at fedoraproject.org> - 0.5.3-3
- set perms on lzma.so to 0755 so wonky umasks don't impact it

* Fri May 14 2010 Seth Vidal <skvidal at fedoraproject.org> - 0.5.3-1
- cleanup from upstream spec to fedora-ish style/reqs

