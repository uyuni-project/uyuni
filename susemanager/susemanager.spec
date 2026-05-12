#
# spec file for package susemanager
#
# Copyright (c) 2026 SUSE LLC and contributors
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


%{!?productprettyname: %global productprettyname Uyuni}

%define pythonX python3

# Keep in sync with salt/salt.spec, used to set correct shebang in mgr-salt-ssh
%if 0%{?suse_version} == 1500 && 0%{?sle_version} >= 150700
%global use_python python311
%global use_python_shebang python3.11
%else
%if 0%{?suse_version} >= 1600 && 0%{?suse_version} < 1690
%global use_python python313
%global use_python_shebang python3.13
%else
%global use_python python3
%global use_python_shebang python3
%endif
%endif

%global apache_user wwwrun
%global apache_group www
%global tftp_group tftp
%global salt_user salt
%global salt_group salt
%global serverdir /srv
%global wwwroot %{serverdir}/www

%global sharedwwwroot %{_datadir}/susemanager/www
%global reporoot %{sharedwwwroot}/pub

%global debug_package %{nil}

Name:           susemanager
Version:        5.2.7
Release:        0
Summary:        %{productprettyname} specific scripts
License:        GPL-2.0-only
Group:          System/Management
URL:            https://github.com/uyuni-project/uyuni
#!CreateArchive: %{name}
Source0:        %{name}-%{version}.tar.gz
#BuildArch:      noarch - not noarch because of ifarch usage!!!!

BuildRequires:  make

BuildRequires:  python3-devel

# check section
BuildRequires:  python3-pycurl
BuildRequires:  spacewalk-backend >= 1.7.38.20
BuildRequires:  spacewalk-backend-server
BuildRequires:  spacewalk-backend-sql-postgresql

BuildRequires:  systemd-rpm-macros
BuildRequires:  fdupes
BuildRequires:  tftp
%{?systemd_ordering}
Requires(pre):  tftp
Requires(post): %fillup_prereq
Requires(post): user(%{apache_user})
Requires(pre):  salt
Requires:       cobbler
Requires:       spacewalk-admin
Requires:       spacewalk-setup
%ifarch %{ix86} x86_64
Requires:       syslinux
%endif
%ifarch s390x ppc64le
Requires:       syslinux-x86_64
%endif
Requires:       less
Requires:       rsync
Requires:       spacewalk-schema
Requires:       susemanager-tools
Recommends:     susemanager-branding
BuildRequires:  uyuni-base-server
Requires(pre):  uyuni-base-server
# yast module dependency
Requires:       postfix
Requires:       reprepro >= 5.4
%{!?python3_sitelib: %global python3_sitelib %(%{__python3} -c "import sysconfig; print(sysconfig.get_path('purelib'))")}
%global pythonsmroot %{python3_sitelib}/spacewalk

%description
A collection of scripts for managing %{productprettyname}'s initial
setup tasks, re-installation, upgrades and managing.

%package tools
Summary:        %{productprettyname} Tools
License:        GPL-2.0-only AND LGPL-2.1-only
Group:          Productivity/Other

BuildRequires:  python3-configobj
Requires:       %{use_python}
Requires:       createrepo_c
Requires:       python3-configobj
Requires:       python3-uyuni-common-libs
Requires:       spacewalk-backend >= 2.1.55.11
Requires:       spacewalk-backend-sql
Requires:       spacewalk-common
Requires:       susemanager-build-keys
Requires:       susemanager-sync-data
BuildRequires:  docbook-utils

%description tools
This package contains %{productprettyname} tools

%package tools-salt
Summary:        Salt related tools for %{productprettyname}
Group:          Productivity/Other
License:        Apache-2.0
Supplements:    susemanager-tools

%description tools-salt
This package contains %{productprettyname} tools related with Salt

%package bash-completion
Summary:        Bash completion for %{productprettyname} CLI tools
Group:          Productivity/Other
Supplements:    spacewalk-backend
Supplements:    spacewalk-utils
Supplements:    susemanager

%description bash-completion
Bash completion for %{productprettyname} CLI tools

%prep
%setup -q

%build

# Fixing shebang for Python 3
for i in `find . -type f -not -name 'mgr-salt-ssh'`;
do
    sed -i '1s=^#!/usr/bin/\(python\|env python\)[0-9.]*=#!/usr/bin/python3=' $i;
done
sed -i '1s=^#!/usr/bin/python3=#!/usr/bin/%{use_python_shebang}=' src/mgr-salt-ssh

# Bash completion
%make_build -C bash-completion

%install
mkdir -p %{buildroot}/%{_prefix}/lib/susemanager/bin/
mkdir -p %{buildroot}/%{_prefix}/lib/susemanager/hooks/
install -m 0755 bin/* %{buildroot}/%{_prefix}/lib/susemanager/bin/

mkdir -p %{buildroot}/%{_datadir}/rhn/config-defaults
mkdir -p %{buildroot}/%{_sysconfdir}/logrotate.d
install -m 0644 rhn-conf/rhn_server_susemanager.conf %{buildroot}/%{_datadir}/rhn/config-defaults
install -m 0644 etc/logrotate.d/susemanager-tools %{buildroot}/%{_sysconfdir}/logrotate.d
make -C src install PREFIX=%{buildroot} PYTHON_BIN=%{pythonX} MANDIR=%{_mandir}
install -d -m 755 %{buildroot}/%{wwwroot}/os-images/
mkdir -p %{buildroot}%{_sysconfdir}/apache2/conf.d
install empty-repo.conf %{buildroot}%{_sysconfdir}/apache2/conf.d/empty-repo.conf

# empty repo for rhel base channels
mkdir -p %{buildroot}%{reporoot}/repositories/
cp -r pub/empty %{buildroot}%{reporoot}/repositories/

# empty repo for Ubuntu base fake channel
cp -r pub/empty-deb %{buildroot}%{reporoot}/repositories/

make -C po install PREFIX=%{buildroot}

%find_lang susemanager

# Bash completion
%make_install -C bash-completion

%fdupes %{buildroot}%{python3_sitelib}
%fdupes %{buildroot}/usr/share

%check
# we need to build a fake python dir. python did not work with
# two site-package/spacewalk dirs having different content
mkdir -p %{_localstatedir}/tmp/fakepython/spacewalk
cp -a %{python3_sitelib}/spacewalk/* %{_localstatedir}/tmp/fakepython/spacewalk/
cp -a %{buildroot}%{python3_sitelib}/spacewalk/* %{_localstatedir}/tmp/fakepython/spacewalk/
export PYTHONPATH=%{_localstatedir}/tmp/fakepython/:%{_datadir}/rhn
make -f Makefile.susemanager PYTHON_BIN=%{pythonX} unittest
unset PYTHONPATH
rm -rf %{_localstatedir}/tmp/fakepython

%post

%posttrans

%postun
# Cleanup
sed -i '/You can access .* via https:\/\//d' /tmp/motd 2> /dev/null ||:

%files -f susemanager.lang
%defattr(-,root,root,-)
%license COPYING
%dir %{_prefix}/lib/susemanager
%dir %{_prefix}/lib/susemanager/bin/
%dir %{_prefix}/lib/susemanager/hooks/
%{_prefix}/lib/susemanager/bin/*
%attr(775,%{salt_user},susemanager) %dir %{wwwroot}/os-images/

%files tools
%defattr(-,root,root,-)
%license COPYING COPYING.LGPL-2.1
%dir %{pythonsmroot}
%dir %{pythonsmroot}/susemanager
%dir %{_datadir}/rhn/
%dir %{_datadir}/susemanager
%dir %{wwwroot}
%dir %{sharedwwwroot}
%dir %{reporoot}
%dir %{reporoot}/repositories
%dir %{reporoot}/repositories/empty
%dir %{reporoot}/repositories/empty/repodata
%dir %{reporoot}/repositories/empty-deb
%dir %{_sysconfdir}/apache2
%dir %{_sysconfdir}/apache2/conf.d
%config(noreplace) %{_sysconfdir}/logrotate.d/susemanager-tools
%{_datadir}/rhn/config-defaults/rhn_*.conf
%attr(0755,root,root) %{_sbindir}/mgr-clean-old-patchnames
%attr(0755,root,root) %{_sbindir}/mgr-create-bootstrap-repo
%attr(0755,root,root) %{_sbindir}/mgr-delete-patch
%attr(0755,root,root) %{_sbindir}/mgr-sync
%{pythonsmroot}/susemanager/__init__.py*
%{pythonsmroot}/susemanager/mgr_clean_old_patchnames.py*
%{pythonsmroot}/susemanager/mgr_delete_patch.py*
%{pythonsmroot}/susemanager/authenticator.py*
%{pythonsmroot}/susemanager/errata_helper.py*
%{pythonsmroot}/susemanager/helpers.py*
%{pythonsmroot}/susemanager/package_helper.py*
%{pythonsmroot}/susemanager/mgr_sync
%{_datadir}/susemanager/mgr_bootstrap_data.py*
%{pythonsmroot}/susemanager/__pycache__/
%{pythonsmroot}/susemanager/mgr_sync/__pycache__/
%{_datadir}/susemanager/__pycache__/
%{_mandir}/man8/mgr-sync.8*
%{reporoot}/repositories/empty/repodata/*.xml*
%{reporoot}/repositories/empty-deb/Packages
%{reporoot}/repositories/empty-deb/Release
%{_sysconfdir}/apache2/conf.d/empty-repo.conf

%files tools-salt
%license COPYING.Apache-2.0
%attr(0755,root,root) %{_bindir}/mgr-salt-ssh

%files bash-completion
%{_datadir}/bash-completion/completions/mgr-sync
%{_datadir}/bash-completion/completions/mgr-create-bootstrap-repo
%{_datadir}/bash-completion/completions/spacewalk-common-channels
%{_datadir}/bash-completion/completions/spacewalk-remove-channel
%{_datadir}/bash-completion/completions/spacewalk-repo-sync

%changelog
