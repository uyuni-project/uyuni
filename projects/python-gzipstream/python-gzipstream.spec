%if ! (0%{?fedora} || 0%{?rhel} > 5)
%{!?python_sitelib: %define python_sitelib %(%{__python} -c "from distutils.sysconfig import get_python_lib; print get_python_lib()")}
%endif

Summary: Streaming zlib (gzip) support for python
Name: python-gzipstream
Version: 2.7.1
Release: 1%{?dist}
URL:        https://github.com/spacewalkproject/spacewalk/wiki/Projects_python-gzipstream
Source0:    https://github.com/spacewalkproject/spacewalk/archive/python-gzipstream-%{version}.tar.gz
License: GPLv2
Group: Development/Libraries
BuildRoot: %(mktemp -ud %{_tmppath}/%{name}-%{version}-%{release}-XXXXXX)
BuildArch: noarch
BuildRequires: python-devel


%global _description\
A streaming gzip handler.\
gzipstream.GzipStream extends the functionality of the gzip.GzipFile class\
to allow the processing of streaming data.\


%description %_description

%package -n python2-gzipstream
Summary: %summary
%{?python_provide:%python_provide python2-gzipstream}

%description -n python2-gzipstream %_description

%prep
%setup -q

%build
%{__python} setup.py build

%install
rm -rf $RPM_BUILD_ROOT
%{__python} setup.py install -O1 --skip-build --root $RPM_BUILD_ROOT

%clean
rm -rf $RPM_BUILD_ROOT

%files -n python2-gzipstream
%{python_sitelib}/*
%doc html
%doc LICENSE

%changelog
* Sat Aug 19 2017 Zbigniew Jędrzejewski-Szmek <zbyszek@in.waw.pl> - 2.7.1-2
- Python 2 binary package renamed to python2-gzipstream
  See https://fedoraproject.org/wiki/FinalizingFedoraSwitchtoPython3

* Mon Jul 17 2017 Jan Dobes 2.7.1-1
- Updated links to github in spec files
- Migrating Fedorahosted to GitHub
- Bumping package versions for 2.7.
- Bumping package versions for 2.6.
- Bumping package versions for 2.5.
- Bumping package versions for 2.4.

* Thu Mar 19 2015 Grant Gainey 2.3.3-1
- Updating copyright info for 2015

* Thu Feb 05 2015 Stephen Herr <sherr@redhat.com> 2.3.2-1
- Relicense python-gzipstream to be GPL only

* Thu Jan 15 2015 Matej Kollar <mkollar@redhat.com> 2.3.1-1
- Getting rid of Tabs and trailing spaces in LICENSE, COPYING, and README files
- Bumping package versions for 2.3.
- Bumping package versions for 2.2.

* Wed Jul 17 2013 Tomas Kasparek <tkasparek@redhat.com> 1.10.2-1
- updating copyright years

* Mon Jun 17 2013 Michael Mraka <michael.mraka@redhat.com> 1.10.1-1
- removed old CVS/SVN version ids
- Purging %%changelog entries preceding Spacewalk 1.0, in active packages.
- %%defattr is not needed since rpm 4.4

* Thu Feb 23 2012 Michael Mraka <michael.mraka@redhat.com> 1.7.1-1
- we are now just GPL

* Mon Oct 31 2011 Miroslav Suchý 1.6.2-1
- point to python-gzipstream specific URL
- add documentation

* Fri Jul 22 2011 Jan Pazdziora 1.6.1-1
- We only support version 14 and newer of Fedora, removing conditions for old
  versions.

* Tue Nov 30 2010 Miroslav Suchý <msuchy@redhat.com> 1.4.3-1
- 657531 - correct  condition for defining the python_sitelib macro
  (msuchy@redhat.com)

* Fri Nov 26 2010 Miroslav Suchý <msuchy@redhat.com> 1.4.2-1
- put license into doc section (msuchy@redhat.com)
- make setup quiet (msuchy@redhat.com)
- correct buildroot (msuchy@redhat.com)
- correct url and source url to point to fedorahosted (msuchy@redhat.com)

* Fri Nov 26 2010 Miroslav Suchý <msuchy@redhat.com> 1.4.1-1
- new package built with tito

