#
# spec file for package spacewalk-doc-indexes
#
# Copyright (c) 2018 SUSE LINUX GmbH, Nuernberg, Germany.
# Copyright (c) 2008-2018 Red Hat, Inc.
#
# All modifications and additions to the file contributed by third parties
# remain the property of their copyright owners, unless otherwise agreed
# upon. The license for this file, and modifications and additions to the
# file, is the same license as for the pristine package itself (unless the
# license for the pristine package is not an Open Source License, in which
# case the license is the MIT License). An "Open Source License" is a
# license that conforms to the Open Source Definition (Version 1.9)
# published by the Open Source Initiative.

# Please submit bugfixes or comments via http://bugs.opensuse.org/
#


%define crawl_output crawl_output

Name:           spacewalk-doc-indexes
Version:        2.8.5.2
Release:        1%{?dist}
Summary:        Lucene indexes of help documentation for spacewalk
License:        GPL-2.0-only AND Apache-2.0
Group:          Applications/Internet

# This src.rpm is cannonical upstream
# You can obtain it using this set of commands
# git clone https://github.com/spacewalkproject/spacewalk.git
# cd search-server/spacewalk-doc-indexes
# make test-srpm
URL:            https://fedorahosted.org/spacewalk
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildRequires:  nutch-core
BuildRequires:  python
BuildRequires:  release-notes-susemanager >= 1.2
BuildRequires:  susemanager-jsp_en >= 1.2
BuildRequires:  xerces-j2
Requires:       nutch-core
Requires:       release-notes-susemanager >= 1.2
Requires:       susemanager-jsp_en >= 1.2
BuildArch:      noarch
Provides:       doc-indexes = %{version}
ExcludeArch:    aarch64

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

%files
%{_prefix}/share/rhn/search/indexes/docs
%if 0%{?suse_version}
%dir %{_prefix}/share/rhn
%dir %{_prefix}/share/rhn/search
%dir %{_prefix}/share/rhn/search/indexes
%doc licenses/*
%endif

%changelog
