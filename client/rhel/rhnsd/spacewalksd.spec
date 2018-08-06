#
# spec file for package spacewalksd
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


# package renaming fun :(
%define rhn_client_tools spacewalk-client-tools
%define rhn_setup	 spacewalk-client-setup
%define rhn_check	 spacewalk-check
%define rhnsd		 spacewalksd
#
Name:           spacewalksd
Version:        5.0.37.1
Release:        1%{?dist}
Summary:        Spacewalk query daemon
License:        GPL-2.0-only
Group:          System Environment/Base
Source0:        spacewalksd-%{version}.tar.gz
Source1:        %{name}-rpmlintrc
URL:            https://fedorahosted.org/spacewalk
BuildRoot:      %{_tmppath}/%{name}-%{version}-build

BuildRequires:  gettext
Provides:       rhnsd = %{version}-%{release}
Obsoletes:      rhnsd < %{version}-%{release}

Requires:       %{rhn_check} >= 0.0.8
BuildRequires:  gcc
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

%description
The Red Hat Update Agent that automatically queries the Red Hat
Network servers and determines which packages need to be updated on
your machine, and runs any actions.

%prep
%setup -q

%build
make -f Makefile.rhnsd %{?_smp_mflags} CFLAGS="-pie -fPIE -Wl,-z,relro,-z,now %{optflags}"

%install
make -f Makefile.rhnsd install VERSION=%{version}-%{release} PREFIX=$RPM_BUILD_ROOT MANPATH=%{_mandir} INIT_DIR=$RPM_BUILD_ROOT/%{_initrddir}

%if 0%{?suse_version} && 0%{?suse_version} < 1210
install -m 0755 rhnsd.init.SUSE $RPM_BUILD_ROOT/%{_initrddir}/rhnsd
# add rclink
ln -sf ../../etc/init.d/rhnsd $RPM_BUILD_ROOT/%{_sbindir}/rcrhnsd
%endif
%if 0%{?fedora} || 0%{?suse_version} >= 1210 || 0%{?rhel} >= 7
rm $RPM_BUILD_ROOT/%{_initrddir}/rhnsd
mkdir -p $RPM_BUILD_ROOT/%{_unitdir}
install -m 0644 rhnsd.service $RPM_BUILD_ROOT/%{_unitdir}/
install -m 0644 spacewalk-update-status.service $RPM_BUILD_ROOT/%{_unitdir}/
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

%find_lang rhnsd

%{!?systemd_post: %global systemd_post() if [ $1 -eq 1 ] ; then /usr/bin/systemctl enable %%{?*} >/dev/null 2>&1 || : ; fi; }
%{!?systemd_preun: %global systemd_preun() if [ $1 -eq 0 ] ; then /usr/bin/systemctl --no-reload disable %%{?*} > /dev/null 2>&1 || : ; /usr/bin/systemctl stop %%{?*} > /dev/null 2>&1 || : ; fi; }
%{!?systemd_postun_with_restart: %global systemd_postun_with_restart() /usr/bin/systemctl daemon-reload >/dev/null 2>&1 || : ; if [ $1 -ge 1 ] ; then /usr/bin/systemctl try-restart %%{?*} >/dev/null 2>&1 || : ; fi; }

%if 0%{?suse_version} >= 1210
%pre
%service_add_pre rhnsd.service
%service_add_pre spacewalk-update-status.service
%endif

%post
%if 0%{?suse_version}
%if 0%{?suse_version} >= 1210
%service_add_post rhnsd.service
%service_add_post spacewalk-update-status.service
%else
%{fillup_and_insserv rhnsd}
%endif
%else
if [ -f /etc/init.d/rhnsd ]; then
    /sbin/chkconfig --add rhnsd
fi
if [ -f %{_unitdir}/rhnsd.service ]; then
    %systemd_post rhnsd.service
    %systemd_post spacewalk-update-status.service
    if [ "$1" = "2" ]; then
        # upgrade from old init.d
        if [ -L /etc/rc2.d/S97rhnsd ]; then
            /usr/bin/systemctl enable rhnsd.service >/dev/null 2>&1
        fi
        rm -f /etc/rc?.d/[SK]??rhnsd
    fi
fi
%endif
if [ -f %{_unitdir}/spacewalk-update-status.service ]; then
    # take care that this is always enabled if it exists
    /usr/bin/systemctl --quiet enable spacewalk-update-status.service 2>&1 ||:
fi

%preun
%if 0%{?suse_version}
%if 0%{?suse_version} >= 1210
%service_del_preun rhnsd.service
%service_del_preun spacewalk-update-status.service
%else
%stop_on_removal rhnsd
exit 0
%endif
%else
if [ $1 = 0 ] ; then
    %if 0%{?fedora} || 0%{?rhel} >= 7
        %systemd_preun rhnsd.service
        %systemd_preun spacewalk-update-status.service
    %else
    service rhnsd stop >/dev/null 2>&1
    %endif
    if [ -f /etc/init.d/rhnsd ]; then
        /sbin/chkconfig --del rhnsd
    fi
fi
%endif

%postun
%if 0%{?suse_version}
%if 0%{?suse_version} >= 1210
%service_del_postun rhnsd.service
%service_del_postun spacewalk-update-status.service
%else
%restart_on_update rhnsd
%{insserv_cleanup}
%endif
%else
if [ "$1" -ge "1" ]; then
    %if 0%{?fedora} || 0%{?rhel} >= 7
    %systemd_postun_with_restart rhnsd.service
    %systemd_postun_with_restart spacewalk-update-status.service
    %else
    service rhnsd condrestart >/dev/null 2>&1 || :
    %endif
fi
%endif

%files -f rhnsd.lang
%defattr(-,root,root)
%dir %{_sysconfdir}/sysconfig/rhn
%config(noreplace) %{_sysconfdir}/sysconfig/rhn/rhnsd
%{_sbindir}/rhnsd
%if 0%{?fedora} || 0%{?suse_version} >= 1210 || 0%{?rhel} >= 7
%{_unitdir}/rhnsd.service
%{_unitdir}/spacewalk-update-status.service
%else
%{_initrddir}/rhnsd
%endif
%if 0%{?suse_version}
%{_sbindir}/rcrhnsd
%endif
%{_mandir}/man8/rhnsd.8*
%doc LICENSE

%changelog
