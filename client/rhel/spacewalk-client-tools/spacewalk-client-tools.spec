#
# spec file for package spacewalk-client-tools
#
# Copyright (c) 2026 SUSE LLC
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

# The productprettyname macros is controlled in the prjconf. If not defined, we fallback here
%{!?productprettyname: %global productprettyname Uyuni}

%if 0%{?fedora} || 0%{?suse_version} > 1320 || 0%{?rhel} >= 8 || 0%{?mageia}
%global build_py3   1
%global default_py3 1
%global __python %{_bindir}/python3
%endif

%if !(0%{?rhel} >= 8 || 0%{?suse_version} >= 1500 )
%global build_py2   1
%endif

%if "%{_vendor}" == "debbuild"
%{!?_presetdir:%global _presetdir /lib/systemd/system-preset}
# Bash constructs in scriptlets don't play nice with Debian's default shell, dash
%global _buildshell /bin/bash
%endif

%{!?__python2:%global __python2 %{_bindir}/python2}
%{!?__python3:%global __python3 %{_bindir}/python3}
%if %{undefined python2_version}
%global python2_version %(python2 -Esc "import sys; sys.stdout.write('{0.major}.{0.minor}'.format(sys.version_info))")
%endif

%if %{undefined python3_version}
%global python3_version %(python3 -Ic "import sys; sys.stdout.write(sys.version[:3])")
%endif

%if %{undefined python2_sitelib}
%global python2_sitelib %(python2 -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")
%endif

%if %{undefined python3_sitelib}
%global python3_sitelib %(python3 -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")
%endif

%if "%{_vendor}" == "debbuild"
# For making sure we can set the right args for deb distros
%global is_deb 1
%endif

%define pythonX %{?default_py3: python3}%{!?default_py3: python2}

# package renaming fun :(
%define rhn_client_tools spacewalk-client-tools
#
%bcond_with    test

Name:           spacewalk-client-tools
Version:        5.3.0
Release:        0
Summary:        Support programs and libraries for %{productprettyname}
License:        GPL-2.0-only
URL:            https://github.com/uyuni-project/uyuni
#!CreateArchive: %{name}
Source0:        %{name}-%{version}.tar.gz
Source1:        https://raw.githubusercontent.com/uyuni-project/uyuni/%{name}-%{version}-0/client/rhel/%{name}/%{name}-rpmlintrc

BuildRequires:  desktop-file-utils
BuildRequires:  gettext
BuildRequires:  intltool
BuildRequires:  make
BuildRequires:  rpm
Requires:       %{pythonX}-%{name} = %{version}-%{release}

Conflicts:      rhn-kickstart < 5.4.3-1
Conflicts:      rhncfg < 5.9.23-1
Conflicts:      up2date < 5.0.0
Conflicts:      yum-rhn-plugin < 1.6.4-1
Provides:       rhn-client-tools = %{version}-%{release}
Obsoletes:      rhn-client-tools < %{version}-%{release}
%if "%{_vendor}" == "debbuild"
Packager:       Uyuni Project <devel@lists.uyuni-project.org>
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          admin
%else
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          System Environment/Base
%endif
%if 0%{?fedora} || 0%{?rhel} || 0%{?suse_version} >= 1210 || 0%{?mageia} >= 6
BuildArch:      noarch
%endif
%if 0%{?debian} || 0%{?ubuntu} || (!0%{?is_opensuse} && 0%{?suse_version} >= 1600 && 0%{?suse_version} < 1699)
ExclusiveArch:  do_not_build
%endif
%if 0%{?suse_version}
BuildRequires:  update-desktop-files
%endif
%if "%{_vendor}" != "debbuild"
Requires:       coreutils
Requires:       gnupg
Requires:       rpm >= 4.2.3-24_nonptl

%if 0%{?suse_version}
Requires:       zypper
%else
%if 0%{?fedora} || 0%{?rhel} >= 8
Requires:       dnf
%else
Requires:       yum
%endif # 0{?fedora}
%endif # 0{?suse_version}
%endif # {_vendor} != "debbuild"

%if "%{_vendor}" == "debbuild"
Requires:       apt
Requires:       coreutils
%if 0%{?ubuntu} >= 1804
Requires:       gpg
%else
Requires:       gnupg
%endif
%endif

%if 0%{?fedora}
BuildRequires:  dnf
BuildRequires:  fedora-logos
%endif

%if 0%{?mageia} >= 6
BuildRequires:  dnf
%endif

%if 0%{?rhel}
BuildRequires:  redhat-logos
%if 0%{?rhel} >= 8
BuildRequires:  dnf
%else
BuildRequires:  yum
%endif
%endif

# For the systemd presets
%if 0%{?fedora} || 0%{?mageia} || 0%{?debian} >= 8 || 0%{?ubuntu} >= 1504 || 0%{?sle_version} >= 120000 || 0%{?rhel} >= 7
BuildRequires:  systemd
Requires:       systemd
%endif

%description
Spacewalk Client Tools provides programs and libraries to allow your
system to receive software updates from %{productprettyname}.

%if 0%{?build_py2}
