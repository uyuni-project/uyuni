%define crawl_output crawl_output

Name: spacewalk-doc-indexes
Version: 2.7.0.3
Release: 1%{?dist}
Summary: Lucene indexes of help documentation for spacewalk

Group: Applications/Internet
License: GPL-2.0 and Apache-2.0
Source0: %{name}-%{version}.tar.gz
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildRequires: python
BuildRequires: nutch-core >= 1.0.1
BuildRequires: susemanager-jsp_en >= 1.2
BuildRequires: release-notes-susemanager >= 1.2
BuildRequires: xerces-j2
Requires: nutch-core >= 1.0.1
Requires: susemanager-jsp_en >= 1.2
Requires: release-notes-susemanager >= 1.2
BuildArch: noarch
Provides: doc-indexes = %{version}
ExcludeArch: aarch64

%description
Lucene generated indexes used by the spacewalk search-server for
documentation/help searches

%prep
%setup -q


%build
./crawl_jsp.sh /srv/tomcat/webapps/rhn/help

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
%{_prefix}/share/rhn/search/indexes/docs
%if 0%{?suse_version}
%dir %{_prefix}/share/rhn
%dir %{_prefix}/share/rhn/search
%dir %{_prefix}/share/rhn/search/indexes
%doc licenses/*
%endif

%changelog
* Fri May 20 2016 Grant Gainey 2.5.2-1
- spacewalk-doc-indexes: build on openSUSE

* Thu Oct 15 2015 Jan Dobes 2.5.1-1
- updating doc indexes
- updating doc URLs
- Bumping package versions for 2.5.
- Bumping package versions for 2.4.

* Thu Mar 19 2015 Grant Gainey 2.3.3-1
- update crawl setting for Spacewalk 2.3

* Thu Jan 15 2015 Matej Kollar <mkollar@redhat.com> 2.3.2-1
- Getting rid of trailing spaces in XML

* Tue Nov 25 2014 Michael Mraka <michael.mraka@redhat.com> 2.3.1-1
- no need to store search logs in git

* Thu Feb 27 2014 Matej Kollar <mkollar@redhat.com> 2.2.1-1
- Updating search index
- Update urls for search reindexing
- Bumping package versions for 2.2.

* Mon Nov 18 2013 Tomas Lestach <tlestach@redhat.com> 2.1.1-1
- updated documentation indexes
- index actual documentation
- Bumping package versions for 2.1.

* Wed Jul 17 2013 Tomas Kasparek <tkasparek@redhat.com> 2.0.1-1
- Bumping package versions for 2.0.

* Mon Mar 18 2013 Michael Mraka <michael.mraka@redhat.com> 1.10.1-1
- %%defattr is not needed since rpm 4.4

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
