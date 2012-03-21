%define crawl_output crawl_output

Name: spacewalk-doc-indexes
Version: 1.7.0.1
Release: 1%{?dist}
Summary: Lucene indexes of help documentation for spacewalk

Group: Applications/Internet
License: GPLv2
Source0: %{name}-%{version}.tar.gz
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildRequires: python
BuildRequires: nutch
BuildRequires: susemanager-jsp_en >= 1.2
BuildRequires: release-notes-susemanager >= 1.2
Requires: nutch
Requires: susemanager-jsp_en >= 1.2
Requires: release-notes-susemanager >= 1.2
BuildArch: noarch
Provides: doc-indexes = %{version}

%description
Lucene generated indexes used by the spacewalk search-server for
documentation/help searches

%prep
%setup -q

%build
./crawl_jsp.sh %{_localstatedir}/lib/tomcat6/webapps/rhn/help

%install
LANGS="en-US"
for lang in $LANGS; do
    install -d -m 755 $RPM_BUILD_ROOT/%{_datadir}/rhn/search/indexes/docs/$lang/segments
    cp -a %{crawl_output}/$lang/index/* $RPM_BUILD_ROOT/%{_datadir}/rhn/search/indexes/docs/$lang/
    cp -a %{crawl_output}/$lang/segments/* $RPM_BUILD_ROOT/%{_datadir}/rhn/search/indexes/docs/$lang/segments
done

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(644,root,root,755)
%{_datadir}/rhn/search/indexes/docs/
%dir %{_datadir}/rhn/
%dir %{_datadir}/rhn/search
%dir %{_datadir}/rhn/search/indexes

%changelog
* Mon Apr 19 2010 Michael Mraka <michael.mraka@redhat.com> 1.1.1-1
- bumping spec files to 1.1 packages

* Fri Jan 15 2010 Michael Mraka <michael.mraka@redhat.com> 0.8.1-1
- rebuild for spacewalk 0.8


* Wed Nov 25 2009 Miroslav Suchý <msuchy@redhat.com> 0.7.1-1
- Update doc indexes to reside in "en-US" (jmatthew@redhat.com)
- bumping versions to 0.7.0 (jmatthew@redhat.com)

* Fri Aug 07 2009 John Matthews <jmatthews@redhat.com> 0.7.0
- update indexes to reside in "en-US"

* Sat Apr 04 2009 jesus m. rodriguez <jesusr@redhat.com> 0.6.1-1
- search requires doc-indexes, sw-doc-indexes provides doc-indexes (jesusr@redhat.com)
- bump Versions to 0.6.0 (jesusr@redhat.com)

* Mon Jan 26 2009 jesus m. rodriguez <jesusr@redhat.com> 0.5.1-1
- requires nutch now

* Mon Jan 26 2009 John Matthews <jmatthews@redhat.com> 0.5.0-1
- update so compatible with search-server changes for multiple
  languages
* Thu Dec 18 2008 John Matthews <jmatthews@redhat.com> 0.4.5-1
- initial

