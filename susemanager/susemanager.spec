#
# spec file for package susemanager
#
# Copyright (c) 2025 SUSE LLC
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

## The productprettyname macros is controlled in the prjconf. If not defined, we fallback here
%{!?productprettyname: %global productprettyname Uyuni}

%if 0%{?suse_version} > 1320 || 0%{?rhel} >= 8
# SLE15 and RHEL8 build on Python 3
%global build_py3   1
%endif
%define pythonX %{?build_py3:python3}%{!?build_py3:python2}

# Keep in sync with salt/salt.spec, used to set correct shebang in mgr-salt-ssh
%if 0%{?suse_version} == 1500 && 0%{?sle_version} >= 150700
%global use_python python311
%global use_python_shebang python3.11
%else
%global use_python python3
%global use_python_shebang python3
%endif

%if 0%{?rhel}
%global apache_user root
%global apache_group root
%global tftp_group root
%global salt_user root
%global salt_group root
%global serverdir %{_sharedstatedir}
%global wwwroot %{_localstatedir}/www
%endif

%if 0%{?suse_version}
%global apache_user wwwrun
%global apache_group www
%global tftp_group tftp
%global salt_user salt
%global salt_group salt
%global serverdir /srv
%global wwwroot %{serverdir}/www
%endif

%global sharedwwwroot %{_datadir}/susemanager/www
%global reporoot %{sharedwwwroot}/pub

%global debug_package %{nil}

Name:           susemanager
Version:        5.1.7
Release:        0
Summary:        %{productprettyname} specific scripts
License:        GPL-2.0-only
# FIXME: use correct group or remove it, see "https://en.opensuse.org/openSUSE:Package_group_guidelines"
Group:          Applications/System
URL:            https://github.com/uyuni-project/uyuni
Source0:        %{name}-%{version}.tar.gz
#BuildArch:      noarch - not noarch because of ifarch usage!!!!

%if 0%{?rhel}
BuildRequires:  gettext
%endif

BuildRequires:  make

%if 0%{?build_py3}
BuildRequires:  python3-devel
%else
BuildRequires:  python-devel
%endif

# check section
%if 0%{?build_py3}
BuildRequires:  python3-pycurl
%else
BuildRequires:  python-curl
BuildRequires:  python-mock
%endif
%if !0%{?rhel}
BuildRequires:  python2
BuildRequires:  pyxml
%endif
BuildRequires:  spacewalk-backend >= 1.7.38.20
BuildRequires:  spacewalk-backend-server
BuildRequires:  spacewalk-backend-sql-postgresql

%if 0%{?suse_version}
BuildRequires:  %fillup_prereq
BuildRequires:  %insserv_prereq
BuildRequires:  tftp
Requires(pre):  %fillup_prereq %insserv_prereq tftp
Requires(preun):%fillup_prereq %insserv_prereq tftp
Requires(post): user(%{apache_user})
%endif
Requires(pre):  salt
Requires:       cobbler
Requires:       openslp-server
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
# migration.sh need either sqlplus or psql
Recommends:     susemanager-branding
BuildRequires:  uyuni-base-server
Requires(pre):  uyuni-base-server
# yast module dependency
%if 0%{?suse_version} > 1320
Requires:       firewalld
%endif
Requires:       postfix
Requires:       reprepro >= 5.4
# mgr-setup want to call mksubvolume for btrfs filesystems
Recommends:     snapper
# mgr-setup calls dig
Requires:       bind-utils
%define python_sitelib %(%{pythonX} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")
%global pythonsmroot %{python_sitelib}/spacewalk

%description
A collection of scripts for managing %{productprettyname}'s initial
setup tasks, re-installation, upgrades and managing.

%package tools
Summary:        %{productprettyname} Tools
Group:          Productivity/Other

%if 0%{?build_py3}
BuildRequires:  python3-configobj
Requires:       createrepo_c
Requires:       %{use_python}
Requires:       python3-configobj
Requires:       python3-uyuni-common-libs
%else
Requires:       createrepo
Requires:       python
Requires:       python-argparse
Requires:       python-configobj
Requires:       python2-uyuni-common-libs
BuildRequires:  python-configobj
Requires:       python-enum34
BuildRequires:  python-enum34
%endif
Requires:       spacewalk-backend >= 2.1.55.11
Requires:       spacewalk-backend-sql
Requires:       spacewalk-common
Requires:       susemanager-build-keys
Requires:       susemanager-sync-data
BuildRequires:  docbook-utils

%description tools
This package contains %{productprettyname} tools

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
%if 0%{?build_py3}
for i in `find . -type f -not -name 'mgr-salt-ssh'`;
do
    sed -i '1s=^#!/usr/bin/\(python\|env python\)[0-9.]*=#!/usr/bin/python3=' $i;
done
sed -i '1s=^#!/usr/bin/python3=#!/usr/bin/%{use_python_shebang}=' src/mgr-salt-ssh
%endif

# Bash completion
%make_build -C bash-completion

%install
mkdir -p %{buildroot}/%{_prefix}/lib/susemanager/bin/
mkdir -p %{buildroot}/%{_prefix}/lib/susemanager/hooks/
install -m 0755 bin/* %{buildroot}/%{_prefix}/lib/susemanager/bin/
ln -s mgr-setup %{buildroot}/%{_prefix}/lib/susemanager/bin/migration.sh
ln -s pg-migrate-94-to-96.sh %{buildroot}/%{_prefix}/lib/susemanager/bin/pg-migrate.sh

mkdir -p %{buildroot}/%{_datadir}/rhn/config-defaults
mkdir -p %{buildroot}/%{_sysconfdir}/slp.reg.d
mkdir -p %{buildroot}/%{_sysconfdir}/logrotate.d
install -m 0644 rhn-conf/rhn_server_susemanager.conf %{buildroot}/%{_datadir}/rhn/config-defaults
install -m 0644 etc/logrotate.d/susemanager-tools %{buildroot}/%{_sysconfdir}/logrotate.d
install -m 0644 etc/slp.reg.d/susemanager.reg %{buildroot}/%{_sysconfdir}/slp.reg.d
make -C src install PREFIX=%{buildroot} PYTHON_BIN=%{pythonX} MANDIR=%{_mandir}
install -d -m 755 %{buildroot}/%{wwwroot}/os-images/
mkdir -p %{buildroot}%{_sysconfdir}/apache2/conf.d
install empty-repo.conf %{buildroot}%{_sysconfdir}/apache2/conf.d/empty-repo.conf

# empty repo for rhel base channels
mkdir -p %{buildroot}%{reporoot}/repositories/
cp -r pub/empty %{buildroot}%{reporoot}/repositories/

# empty repo for Ubuntu base fake channel
cp -r pub/empty-deb %{buildroot}%{reporoot}/repositories/

%if 0%{?suse_version} > 1320
mkdir -p %{buildroot}/%{_prefix}/lib/firewalld/services
install -m 0644 etc/firewalld/services/suse-manager-server.xml %{buildroot}/%{_prefix}/lib/firewalld/services
%else
mkdir -p %{buildroot}/%{_sysconfdir}/firewalld/services
install -m 0644 etc/firewalld/services/suse-manager-server.xml %{buildroot}/%{_sysconfdir}/firewalld/services
%endif

%if 0%{?sle_version} && !0%{?is_opensuse}
# this script migrate the server to Uyuni. It should not be available on SUSE Multi-Linux Manager
rm -f %{buildroot}/%{_prefix}/lib/susemanager/bin/server-migrator.sh
%endif

make -C po install PREFIX=%{buildroot}

%find_lang susemanager

# Bash completion
%make_install -C bash-completion

%check
# we need to build a fake python dir. python did not work with
# two site-package/spacewalk dirs having different content
mkdir -p %{_localstatedir}/tmp/fakepython/spacewalk
cp -a %{python_sitelib}/spacewalk/* %{_localstatedir}/tmp/fakepython/spacewalk/
cp -a %{buildroot}%{python_sitelib}/spacewalk/* %{_localstatedir}/tmp/fakepython/spacewalk/
export PYTHONPATH=%{_localstatedir}/tmp/fakepython/:%{_datadir}/rhn
make -f Makefile.susemanager PYTHON_BIN=%{pythonX} unittest
unset PYTHONPATH
rm -rf %{_localstatedir}/tmp/fakepython
%if !0%{?build_py3}
pushd %{buildroot}
find -name '*.py' -print0 | xargs -0 python %{py_libdir}/py_compile.py
popd
%endif

%post
%if !0%{?suse_version}
sed -i 's/su wwwrun www/su apache apache/' %{_sysconfdir}/logrotate.d/susemanager-tools
%endif

%posttrans

%postun
%if 0%{?suse_version}
%insserv_cleanup
%endif
# Cleanup
sed -i '/You can access .* via https:\/\//d' /tmp/motd 2> /dev/null ||:

%files -f susemanager.lang
%defattr(-,root,root,-)
%license COPYING
%dir %{_prefix}/lib/susemanager
%dir %{_prefix}/lib/susemanager/bin/
%dir %{_prefix}/lib/susemanager/hooks/
%dir %{_sysconfdir}/slp.reg.d
%{_prefix}/lib/susemanager/bin/*
%config %{_sysconfdir}/slp.reg.d/susemanager.reg
%attr(775,%{salt_user},susemanager) %dir %{wwwroot}/os-images/
%if 0%{?suse_version} > 1320
%{_prefix}/lib/firewalld/services/suse-manager-server.xml
%else
%{_sysconfdir}/firewalld/services/suse-manager-server.xml
%endif

%files tools
%defattr(-,root,root,-)
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
%attr(0755,root,root) %{_bindir}/mgr-salt-ssh
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
%if 0%{?rhel} || 0%{?build_py3}
%{pythonsmroot}/susemanager/__pycache__/
%{pythonsmroot}/susemanager/mgr_sync/__pycache__/
%{_datadir}/susemanager/__pycache__/
%endif
%{_mandir}/man8/mgr-sync.8*
%{reporoot}/repositories/empty/repodata/*.xml*
%{reporoot}/repositories/empty-deb/Packages
%{reporoot}/repositories/empty-deb/Release
%{_sysconfdir}/apache2/conf.d/empty-repo.conf

%files bash-completion
%{_datadir}/bash-completion/completions/mgr-sync
%{_datadir}/bash-completion/completions/mgr-create-bootstrap-repo
%{_datadir}/bash-completion/completions/spacewalk-common-channels
%{_datadir}/bash-completion/completions/spacewalk-remove-channel
%{_datadir}/bash-completion/completions/spacewalk-repo-sync

%changelog
