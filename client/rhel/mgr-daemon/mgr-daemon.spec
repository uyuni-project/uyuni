#
# spec file for package mgr-daemon
#
# Copyright (c) 2019 SUSE LINUX GmbH, Nuernberg, Germany.
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

# Macros that aren't defined in debbuild
%if %{_vendor} == "debbuild"
%global _unitdir /lib/systemd/system
%global _initrddir /etc/init.d
%global is_deb 1
%global _buildshell /bin/bash
%endif

# package renaming fun :(
%define rhn_client_tools spacewalk-client-tools
%define rhn_setup	 spacewalk-client-setup
%define rhn_check	 spacewalk-check
%define rhnsd		 spacewalksd
#
Name:           mgr-daemon
Version:        4.0.5
Release:        1%{?dist}
Summary:        Spacewalk query daemon
License:        GPL-2.0-only
%if %{_vendor} == "debbuild"
Group:          utils
Packager:       Uyuni Project <uyuni-devel@opensuse.org>
%else
Group:          System Environment/Base
%endif
Source0:        spacewalksd-%{version}.tar.gz
Source1:        %{name}-rpmlintrc
Url:            https://github.com/uyuni-project/uyuni
BuildRoot:      %{_tmppath}/%{name}-%{version}-build

%if 0%{?fedora} || 0%{?suse_version} >= 1210 || 0%{?mageia} || 0%{?ubuntu} >= 1504 || 0%{?debian} >= 8 || 0%{?rhel} >= 7
BuildArch: noarch
%endif

%if %{_vendor} != "debbuild"

# 5.0.37.2 was last version+1 of spacewalksd before renaming to mgr-daemon
Provides:       rhnsd = 5.0.38
Obsoletes:      rhnsd < 5.0.38
Provides:       spacewalksd = 5.0.38
Obsoletes:      spacewalksd < 5.0.38

%if 0%{?suse_version}
Requires(post): aaa_base
Requires(preun): aaa_base
BuildRequires:  sysconfig
%if 0%{?suse_version} >=1210
BuildRequires:  pkgconfig(systemd)
%{?systemd_requires}
%else
Requires(preun): %fillup_prereq %insserv_prereq
%endif
%else
%if 0%{?fedora}
Requires(post): chkconfig
Requires(preun): chkconfig
Requires(post): systemd-sysv
Requires(preun): systemd-sysv
Requires(post): systemd-units
Requires(preun): systemd-units
BuildRequires:  systemd-units
%else
Requires(post): chkconfig
Requires(preun): chkconfig
# This is for /sbin/service
Requires(preun): initscripts
Requires(postun): initscripts
%endif
%endif
%endif

%if %{_vendor} == "debbuild"
BuildRequires: init-system-helpers
%if 0%{?debian} >= 8 || 0%{?ubuntu} >= 1504
BuildRequires: systemd
Requires(post): systemd
Requires(preun): systemd
Requires(postun): systemd
%endif
%endif

BuildRequires:  gettext
Requires:       %{rhn_check} >= 0.0.8
BuildRequires:  gcc

%description
The Red Hat Update Agent that automatically queries the Red Hat
Network servers and determines which packages need to be updated on
your machine, and runs any actions.

%prep
%setup -q

%build
make -f Makefile.rhnsd %{?_smp_mflags} CFLAGS="-pie -fPIE -Wl,-z,relro,-z,now %{optflags}" %{?is_deb:PLATFORM=deb}

%install
make -f Makefile.rhnsd install VERSION=%{version}-%{release} PREFIX=$RPM_BUILD_ROOT MANPATH=%{_mandir} INIT_DIR=$RPM_BUILD_ROOT/%{_initrddir} %{?is_deb:PLATFORM=deb} CONFIG_DIR=$RPM_BUILD_ROOT/%{_sysconfdir}/sysconfig/rhn

%if %{_vendor} != "debbuild"
%if 0%{?suse_version} && 0%{?suse_version} < 1210
install -m 0755 rhnsd.init.SUSE $RPM_BUILD_ROOT/%{_initrddir}/rhnsd
# add rclink
ln -sf ../../etc/init.d/rhnsd $RPM_BUILD_ROOT/%{_sbindir}/rcrhnsd
%endif
%endif
%if %{_vendor} == "debbuild"
install -m 0755 rhnsd.init.Debian $RPM_BUILD_ROOT/%{_initrddir}/rhnsd
%endif
%if 0%{?fedora} || 0%{?suse_version} >= 1210 || 0%{?mageia} || 0%{?ubuntu} >= 1504 || 0%{?debian} >= 8 || 0%{?rhel} >= 7
rm $RPM_BUILD_ROOT/%{_initrddir}/rhnsd
mkdir -p $RPM_BUILD_ROOT/%{_unitdir}
install -m 0644 rhnsd.service $RPM_BUILD_ROOT/%{_unitdir}/
install -m 0644 spacewalk-update-status.service $RPM_BUILD_ROOT/%{_unitdir}/
install -m 0644 rhnsd.timer $RPM_BUILD_ROOT/%{_unitdir}/
%endif
%if 0%{?suse_version}
# remove all unsupported translations
cd $RPM_BUILD_ROOT
for d in usr/share/locale/*; do
  if [ ! -d "/$d" ]; then
    rm -rfv "./$d"
  fi
done
cd -
%if 0%{?suse_version} >= 1210
rm -f $RPM_BUILD_ROOT/%{_sbindir}/rcrhnsd
ln -s %{_sbindir}/service %{buildroot}%{_sbindir}/rcrhnsd
%endif
%endif
# find_lang not available on debbuild; we'll work around this below
%if %{_vendor} != "debbuild"
%find_lang rhnsd
%endif

# These will not work with debbuild
%if %{_vendor} != "debbuild"
%{!?systemd_post: %global systemd_post() if [ $1 -eq 1 ] ; then /usr/bin/systemctl enable %%{?*} >/dev/null 2>&1 || : ; fi; }
%{!?systemd_preun: %global systemd_preun() if [ $1 -eq 0 ] ; then /usr/bin/systemctl --no-reload disable %%{?*} > /dev/null 2>&1 || : ; /usr/bin/systemctl stop %%{?*} > /dev/null 2>&1 || : ; fi; }
%{!?systemd_postun_with_restart: %global systemd_postun_with_restart() /usr/bin/systemctl daemon-reload >/dev/null 2>&1 || : ; if [ $1 -ge 1 ] ; then /usr/bin/systemctl try-restart %%{?*} >/dev/null 2>&1 || : ; fi; }
%endif

%if 0%{?fedora} || 0%{?suse_version} >= 1210 || 0%{?mageia} || 0%{?ubuntu} >= 1504 || 0%{?debian} >= 8 || 0%{?rhel} >= 7
rm -f $RPM_BUILD_ROOT/%{_sysconfdir}/sysconfig/rhn/rhnsd
rm -f $RPM_BUILD_ROOT/%{_sbindir}/rhnsd
rm -rf $RPM_BUILD_ROOT/%{_datadir}/locale
%endif

%if 0%{?suse_version} >= 1210
%pre
%service_add_pre rhnsd.timer
%service_add_pre spacewalk-update-status.service
%endif

%post
%if %{_vendor} != "debbuild"
%if 0%{?suse_version}
%if 0%{?suse_version} >= 1210
%service_add_post rhnsd.timer
%service_add_post spacewalk-update-status.service
%else
%{fillup_and_insserv rhnsd}
%endif
%else
if [ -f /etc/init.d/rhnsd ]; then
    /sbin/chkconfig --add rhnsd
fi
if [ -f %{_unitdir}/rhnsd.service ]; then
    %systemd_post rhnsd.timer
    %systemd_post spacewalk-update-status.service
    if [ "$1" = "2" ]; then
        # upgrade from old init.d
        if [ -L /etc/rc2.d/S97rhnsd ]; then
            /usr/bin/systemctl enable rhnsd.timer >/dev/null 2>&1
        fi
        rm -f /etc/rc?.d/[SK]??rhnsd
    fi
fi
if [ -f %{_unitdir}/spacewalk-update-status.service ]; then
    # take care that this is always enabled if it exists
    /usr/bin/systemctl --quiet enable spacewalk-update-status.service 2>&1 ||:
fi
%endif
%endif
%if %{_vendor} == "debbuild"
if [ -f %{_initrddir}/rhnsd ] && ( [ "$1" == "configure" ] || [ "$1" == "abort-upgrade" ] ); then
        update-rc.d rhnsd defaults >/dev/null 2>&1 || :
fi
if [ -f %{_unitdir}/rhnsd.service ] && [ "$1" == "configure" ]; then
    systemctl preset rhnsd.timer >/dev/null 2>&1 || :
fi
%endif


%preun
%if %{_vendor} != "debbuild"
%if 0%{?suse_version}
%if 0%{?suse_version} >= 1210
%service_del_preun rhnsd.timer
%service_del_preun spacewalk-update-status.service
%else
%stop_on_removal rhnsd
exit 0
%endif
%else
if [ $1 = 0 ] ; then
    %if 0%{?fedora} || 0%{?rhel} >= 7
        %systemd_preun rhnsd.timer
        %systemd_preun spacewalk-update-status.service
    %else
    service rhnsd stop >/dev/null 2>&1
    %endif
    if [ -f /etc/init.d/rhnsd ]; then
        /sbin/chkconfig --del rhnsd
    fi
fi
%endif
%endif

%if %{_vendor} == "debbuild"
if [ -f %{_initrddir}/rhnsd ] || [ -e "/etc/init/rhnsd.conf" ]; then
    update-rc.d -f rhnsd remove || exit $?
fi
if [ -f %{_unitdir}/rhnsd.service ] && ( [ "$1" == "remove" ] || [ "$1" == "purge" ] ); then
    systemctl --no-reload disable rhnsd.timer >/dev/null 2>&1 || :
    systemctl stop rhnsd.timer >/dev/null 2>&1 || :
fi
%endif

%postun
%if %{_vendor} != "debbuild"
%if 0%{?suse_version}
%if 0%{?suse_version} >= 1210
%service_del_postun rhnsd.timer
%service_del_postun spacewalk-update-status.service
%else
%restart_on_update rhnsd
%{insserv_cleanup}
%endif
%else
if [ "$1" -ge "1" ]; then
    %if 0%{?fedora} || 0%{?rhel} >= 7
    %systemd_postun_with_restart rhnsd.timer
    %systemd_postun_with_restart spacewalk-update-status.service
    %else
    service rhnsd condrestart >/dev/null 2>&1 || :
    %endif
fi
%endif
%endif

%if %{_vendor} == "debbuild"
if [ -f {_initrddir}/rhnsd ] && [ "$1" == "purge" ]; then
    update-rc.d rhnsd remove >/dev/null
fi
if [ -f %{_unitdir}/rhnsd.service ]; then
    systemctl daemon-reload >/dev/null 2>&1 || :
fi
%endif

%if 0%{?fedora} || 0%{?suse_version} >= 1210 || 0%{?mageia} || 0%{?ubuntu} >= 1504 || 0%{?debian} >= 8 || 0%{?rhel} >= 7
%files
%defattr(-,root,root)
%{_unitdir}/rhnsd.service
%{_unitdir}/rhnsd.timer
%{_unitdir}/spacewalk-update-status.service
%else
%files -f rhnsd.lang
%defattr(-,root,root)
%dir %{_sysconfdir}/sysconfig/rhn
%config(noreplace) %{_sysconfdir}/sysconfig/rhn/rhnsd
%{_sbindir}/rhnsd
%{_initrddir}/rhnsd
%endif
%if 0%{?suse_version}
%{_sbindir}/rcrhnsd
%endif
%{_mandir}/man8/rhnsd.8*
%doc LICENSE
%if %{_vendor} == "debbuild"
%{_datadir}/locale/
%endif

%changelog
