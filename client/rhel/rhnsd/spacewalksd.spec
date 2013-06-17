# package renaming fun :(
%define rhn_client_tools spacewalk-client-tools
%define rhn_setup	 spacewalk-client-setup
%define rhn_check	 spacewalk-check
%define rhnsd		 spacewalksd
#
Summary: Spacewalk query daemon
License: GPLv2
Group: System Environment/Base
Source0: https://fedorahosted.org/releases/s/p/spacewalk/rhnsd-%{version}.tar.gz
URL:     https://fedorahosted.org/spacewalk
Name: spacewalksd
Version: 5.0.13
Release: 1%{?dist}
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

BuildRequires: gettext
Provides: rhnsd = %{version}-%{release}
Obsoletes: rhnsd < %{version}-%{release}

Requires: %{rhn_check} >= 0.0.8
%if 0%{?suse_version}
Requires(post): aaa_base
Requires(preun): aaa_base
BuildRequires: sysconfig
Requires(preun): %fillup_prereq %insserv_prereq
%else
Requires(post): chkconfig
Requires(preun): chkconfig
# This is for /sbin/service
Requires(preun): initscripts
Requires(postun): initscripts
%endif

%description
The Red Hat Update Agent that automatically queries the Red Hat
Network servers and determines which packages need to be updated on
your machine, and runs any actions.

%prep
%setup -q

%build
make -f Makefile.rhnsd %{?_smp_mflags} CFLAGS="%{optflags}"

%install
rm -rf $RPM_BUILD_ROOT
make -f Makefile.rhnsd install VERSION=%{version}-%{release} PREFIX=$RPM_BUILD_ROOT MANPATH=%{_mandir} INIT_DIR=$RPM_BUILD_ROOT/%{_initrddir}

%if 0%{?suse_version}
install -m 0755 rhnsd.init.SUSE $RPM_BUILD_ROOT/%{_initrddir}/rhnsd
# remove all unsupported translations
cd $RPM_BUILD_ROOT
for d in usr/share/locale/*; do
  if [ ! -d "/$d" ]; then
    rm -rfv "./$d"
  fi
done
cd -
%endif

# add rclink
ln -sf ../../etc/init.d/rhnsd $RPM_BUILD_ROOT/%{_sbindir}/rcrhnsd

%{!?systemd_post: %global systemd_post() if [ $1 -eq 1 ] ; then /usr/bin/systemctl enable %%{?*} >/dev/null 2>&1 || : ; fi; }
%{!?systemd_preun: %global systemd_preun() if [ $1 -eq 0 ] ; then /usr/bin/systemctl --no-reload disable %%{?*} > /dev/null 2>&1 || : ; /usr/bin/systemctl stop %%{?*} > /dev/null 2>&1 || : ; fi; }
%{!?systemd_postun_with_restart: %global systemd_postun_with_restart() /usr/bin/systemctl daemon-reload >/dev/null 2>&1 || : ; if [ $1 -ge 1 ] ; then /usr/bin/systemctl try-restart %%{?*} >/dev/null 2>&1 || : ; fi; }


%find_lang rhnsd


%post
%if 0%{?suse_version}
%{fillup_and_insserv rhnsd}
%else
if [ -f /etc/init.d/rhnsd ]; then
    /sbin/chkconfig --add rhnsd
fi
if [ -f %{_unitdir}/rhnsd.service ]; then
    %systemd_post rhnsd.service
    if [ "$1" = "2" ]; then
        # upgrade from old init.d
        if [ -L /etc/rc2.d/S97rhnsd ]; then
            /usr/bin/systemctl enable rhnsd.service >/dev/null 2>&1
        fi
        rm -f /etc/rc?.d/[SK]??rhnsd
    fi
fi
%endif

%preun
%if 0%{?suse_version}
%stop_on_removal rhnsd
exit 0
%else
if [ $1 = 0 ] ; then
    %if 0%{?fedora}
        %systemd_preun rhnsd.service
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
%restart_on_update rhnsd
%{insserv_cleanup}
%else
if [ "$1" -ge "1" ]; then
    %if 0%{?fedora}
    %systemd_postun_with_restart rhnsd.service
    %else
    service rhnsd condrestart >/dev/null 2>&1 || :
    %endif
fi
%endif

%clean
rm -fr $RPM_BUILD_ROOT


%files -f rhnsd.lang
%defattr(-,root,root)
%dir %{_sysconfdir}/sysconfig/rhn
%config(noreplace) %{_sysconfdir}/sysconfig/rhn/rhnsd
%{_sbindir}/rhnsd
%if 0%{?fedora} || 0%{?suse_version} >= 1210
%{_unitdir}/rhnsd.service
%else
%{_sbindir}/rcrhnsd
%{_initrddir}/rhnsd
%endif
%{_mandir}/man8/rhnsd.8*
%doc LICENSE

%changelog
* Mon Jun 17 2013 Michael Mraka <michael.mraka@redhat.com> 5.0.13-1
- removed old CVS/SVN version ids

* Mon Jun 17 2013 Tomas Kasparek <tkasparek@redhat.com> 5.0.12-1
- rebranding few more strings in client stuff

* Tue May 21 2013 Tomas Kasparek <tkasparek@redhat.com> 5.0.11-1
- branding clean-up of rhel client stuff

* Thu Apr 25 2013 Michael Mraka <michael.mraka@redhat.com> 5.0.10-1
- let rhnsd.service be enabled after installation
- Purging %%changelog entries preceding Spacewalk 1.0, in active packages.

* Fri Feb 15 2013 Milan Zazrivec <mzazrivec@redhat.com> 5.0.9-1
- Update .po and .pot files for rhnsd.
- New translations from Transifex for rhnsd.
- Download translations from Transifex for rhnsd.

* Fri Nov 30 2012 Jan Pazdziora 5.0.8-1
- Revert "876328 - updating rhel client tools translations"

* Mon Nov 19 2012 Jan Pazdziora 5.0.7-1
- Only run chkconfig if we are in the SysV world.
- rhnsd needs to be marked as forking.
- When talking to systemctl, we need to say .service.

* Fri Nov 16 2012 Jan Pazdziora 5.0.6-1
- 876328 - updating rhel client tools translations

* Sun Nov 11 2012 Michael Calmer <mc@suse.de> 5.0.5-1
- use systemd on openSUSE >= 12.1
- do not start rhnsd in runlevel 2 which has no network
- no use of /var/lock/subsys/ anymore

* Tue Oct 30 2012 Jan Pazdziora 5.0.4-1
- Update .po and .pot files for rhnsd.
- New translations from Transifex for rhnsd.
- Download translations from Transifex for rhnsd.

* Mon Jul 30 2012 Michael Mraka <michael.mraka@redhat.com> 5.0.3-1
- there's no elsif macro

* Wed Jul 25 2012 Michael Mraka <michael.mraka@redhat.com> 5.0.2-1
- make sure _unitdir is defined

* Wed Jul 25 2012 Michael Mraka <michael.mraka@redhat.com> 5.0.1-1
- implement rhnsd.service for systemd

* Tue Feb 28 2012 Jan Pazdziora 4.9.15-1
- Update .po and .pot files for rhnsd.
- Download translations from Transifex for rhnsd.

* Wed Dec 21 2011 Milan Zazrivec <mzazrivec@redhat.com> 4.9.14-1
- updated translations

* Fri Jul 29 2011 Tomas Lestach <tlestach@redhat.com> 4.9.13-1
- 679054 - fix random interval part (tlestach@redhat.com)

* Tue Jul 19 2011 Jan Pazdziora 4.9.12-1
- Merging Transifex changes for rhnsd.
- New translations from Transifex for rhnsd.
- Download translations from Transifex for rhnsd.

* Tue Jul 19 2011 Jan Pazdziora 4.9.11-1
- update .po and .pot files for rhnsd

* Fri Apr 15 2011 Jan Pazdziora 4.9.10-1
- changes to build rhnsd on SUSE (mc@suse.de)

* Fri Feb 18 2011 Jan Pazdziora 4.9.9-1
- l10n: Updates to Estonian (et) translation (mareklaane@fedoraproject.org)

* Thu Jan 20 2011 Tomas Lestach <tlestach@redhat.com> 4.9.8-1
- updating Copyright years for year 2011 (tlestach@redhat.com)
- update .po and .pot files for rhnsd (tlestach@redhat.com)

* Tue Nov 02 2010 Jan Pazdziora 4.9.7-1
- Update copyright years in the rest of the repo.
- update .po and .pot files for rhnsd

* Thu Aug 12 2010 Milan Zazrivec <mzazrivec@redhat.com> 4.9.6-1
- update .po and .pot files for rhnsd (msuchy@redhat.com)

* Thu Jul 01 2010 Miroslav Suchý <msuchy@redhat.com> 4.9.4-1
- l10n: Updates to Czech (cs) translation (msuchy@fedoraproject.org)
- cleanup - removing translation file, which does not match any language code
  (msuchy@redhat.com)
- update po files for rhnsd (msuchy@redhat.com)
- generate new pot file for rhnsd (msuchy@redhat.com)
- l10n: Updates to Polish (pl) translation (raven@fedoraproject.org)

