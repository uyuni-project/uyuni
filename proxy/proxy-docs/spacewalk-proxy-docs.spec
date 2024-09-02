#
# spec file for package spacewalk-proxy-docs
#
# Copyright (c) 2024 SUSE LLC
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

# Please submit bugfixes or comments via https://bugs.opensuse.org/
#


Name:           spacewalk-proxy-docs
Version:        5.1.0
Release:        0
Summary:        Spacewalk Proxy Server Documentation
License:        OPL-1.0
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Applications/Internet
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
BuildRequires:  susemanager-advanced-topics_en-pdf
BuildRequires:  susemanager-reference_en-pdf
BuildRequires:  xerces-j2
Obsoletes:      rhns-proxy-docs < 5.3.0
Provides:       rhns-proxy-docs = 5.3.0
BuildArch:      noarch

%description
This package includes the SUSE Manager Proxy Quick Start guide
Also included are the Client Configuration and Enterprise
User Reference guides.

%prep
%setup -q

%build
#nothing to do here

%install
install -m 755 -d %{buildroot}
mkdir -p %{buildroot}/%{_defaultdocdir}/%{name}

for book in reference advanced-topics; do
  pdf="%{_datadir}/doc/manual/susemanager-${book}_en-pdf/susemanager-${book}_en.pdf"
  test -f $pdf && \
    cp -av $pdf %{buildroot}/%{_defaultdocdir}/%{name}/
done

install -m 644 LICENSE %{buildroot}/%{_defaultdocdir}/%{name}/
install -m 644 squid.conf.sample %{buildroot}/%{_defaultdocdir}/%{name}/

%files
%defattr(-,root,root)
%docdir %{_defaultdocdir}/%{name}
%dir %{_defaultdocdir}/%{name}
%{_defaultdocdir}/%{name}/*

%changelog
