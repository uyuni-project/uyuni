#
# spec file for package mgr-osad
#
# Copyright (c) 2021 SUSE LLC
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


# Old name and version+1 before switching to mgr-osad
%define oldname osad
%define oldversion 5.11.103
%global rhnroot /usr/share/rhn
%global rhnconf /etc/sysconfig/rhn
%global client_caps_dir /etc/sysconfig/rhn/clientCaps.d
%{!?fedora: %global sbinpath /sbin}%{?fedora: %global sbinpath %{_sbindir}}
%global __python /usr/bin/python2

%if 0%{?suse_version}
%global apache_group www
%global apache_user wwwrun
%else
%global apache_group apache
%global apache_user apache
%endif

%if 0%{?fedora} || 0%{?suse_version} > 1320 || 0%{?rhel} >= 8
%global build_py3   1
%global default_py3 1
%endif

%if ( 0%{?fedora} && 0%{?fedora} < 28 ) || ( 0%{?rhel} && 0%{?rhel} < 8 ) || (0%{?suse_version} && 0%{?sle_version} < 150000)
%global build_py2   1
%endif

%define pythonX %{?default_py3: python3}%{!?default_py3: python2}

Name:           mgr-osad
Summary:        Open Source Architecture Daemon
License:        GPL-2.0-only
Group:          System Environment/Daemons
Version:        4.3.6
Release:        1
Provides:       %{oldname} = %{oldversion}
Obsoletes:      %{oldname} < %{oldversion}
URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/uyuni-project/uyuni/archive/%{name}-%{version}-1.tar.gz
Source1:        https://raw.githubusercontent.com/uyuni-project/uyuni/%{name}-%{version}-1/client/tools/mgr-osad/%{name}-rpmlintrc
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
%if 0%{?fedora} || 0%{?rhel} || 0%{?suse_version} >= 1210
BuildArch:      noarch
%endif
%if 0%{?fedora} > 26
BuildRequires:  perl-interpreter
%else
BuildRequires:  perl
%endif
Requires:       %{pythonX}-%{name} = %{version}-%{release}
Conflicts:      mgr-osa-dispatcher < %{version}-%{release}
Conflicts:      mgr-osa-dispatcher > %{version}-%{release}
%if 0%{?suse_version} >= 1210
BuildRequires:  systemd
%{?systemd_requires}
%endif
%if 0%{?suse_version}
# provides chkconfig on SUSE
Requires(post): aaa_base
Requires(preun): aaa_base
# to make chkconfig test work during build
BuildRequires:  sysconfig
BuildRequires:  syslog
%if 0%{?suse_version} < 1210
Requires:       %fillup_prereq
Requires:       %insserv_prereq
%endif
%else
%if 0%{?fedora} || 0%{?rhel} >= 7
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
%endif
%endif

%description
OSAD agent receives commands over jabber protocol from Spacewalk Server and
commands are instantly executed.

This package effectively replaces the behavior of rhnsd/rhn_check that
only poll the Spacewalk Server from time to time.

%if 0%{?build_py2}
%package -n python2-%{name}
Summary:        Open Source Architecture Daemon
Group:          System Environment/Daemons
Provides:       python-%{name} = %{oldversion}
Obsoletes:      python-%{name} < %{oldversion}
Provides:       python2-%{oldname} = %{oldversion}
Obsoletes:      python2-%{oldname} < %{oldversion}
Requires:       %{name} = %{version}-%{release}
Requires:       python
Requires:       python-jabberpy
Requires:       python2-mgr-osa-common = %{version}
Requires:       python2-rhn-client-tools >= 2.8.4
Requires:       python2-uyuni-common-libs
Requires:       rhnlib >= 4.2.2
%if 0%{?rhel} && 0%{?rhel} <= 5
Requires:       python-hashlib
%endif
BuildRequires:  python-devel

%description -n python2-%{name}
Python 2 specific files for %{name}
%endif

%if 0%{?build_py3}
%package -n python3-%{name}
Summary:        Open Source Architecture Daemon
Group:          System Environment/Daemons
Provides:       python3-%{oldname} = %{oldversion}
Obsoletes:      python3-%{oldname} < %{oldversion}
Requires:       %{name} = %{version}-%{release}
Requires:       python3
Requires:       python3-jabberpy
Requires:       python3-mgr-osa-common = %{version}
Requires:       python3-rhn-client-tools >= 2.8.4
Requires:       python3-rhnlib >= 4.2.2
Requires:       python3-uyuni-common-libs
BuildRequires:  python3-devel
BuildRequires:  python3-rpm-macros

%description -n python3-%{name}
Python 3 specific files for %{name}
%endif

%if 0%{?build_py2}
%package -n python2-mgr-osa-common
Summary:        OSA common files
Group:          System Environment/Daemons
Requires:       python-jabberpy
Conflicts:      %{name} < %{version}-%{release}
Conflicts:      %{name} > %{version}-%{release}
Obsoletes:      osa-common < %{oldversion}
Provides:       osa-common = %{oldversion}
Obsoletes:      python2-osa-common < %{oldversion}
Provides:       python2-osa-common = %{oldversion}

%description -n python2-mgr-osa-common
Python 2 common files needed by mgr-osad and mgr-osa-dispatcher
%endif

%if 0%{?build_py3}
%package -n python3-mgr-osa-common
Summary:        OSA common files
Group:          System Environment/Daemons
Requires:       python3-jabberpy
Conflicts:      %{name} < %{version}-%{release}
Conflicts:      %{name} > %{version}-%{release}
Obsoletes:      osa-common < %{oldversion}
Provides:       osa-common = %{oldversion}
Obsoletes:      python3-osa-common < %{oldversion}
Provides:       python3-osa-common = %{oldversion}

%description -n python3-mgr-osa-common
Python 3 common files needed by mgr-osad and mgr-osa-dispatcher
%endif

%package -n mgr-osa-dispatcher
Summary:        OSA dispatcher
Group:          System Environment/Daemons
Obsoletes:      osa-dispatcher < %{oldversion}
Provides:       osa-dispatcher = %{oldversion}
BuildRequires:  uyuni-base-common
Requires(pre):  uyuni-base-common
Requires:       %{pythonX}-mgr-osa-dispatcher = %{version}-%{release}
Requires:       lsof
Requires:       spacewalk-backend-server >= 1.2.32
Conflicts:      %{name} < %{version}-%{release}
Conflicts:      %{name} > %{version}-%{release}
%if 0%{?suse_version} >= 1210
%{?systemd_requires}
%endif
%if 0%{?suse_version}
# provides chkconfig on SUSE
Requires(post): aaa_base
Requires(preun): aaa_base
%else
Requires(post): chkconfig
Requires(preun): chkconfig
# This is for /sbin/service
Requires(preun): initscripts
%endif

%description -n mgr-osa-dispatcher
OSA dispatcher is supposed to run on the Spacewalk server. It gets information
from the Spacewalk server that some command needs to be execute on the client;
that message is transported via jabber protocol to OSAD agent on the clients.

%if 0%{?build_py2}
%package -n python2-mgr-osa-dispatcher
Summary:        OSA dispatcher
Group:          System Environment/Daemons
Obsoletes:      python2-osa-dispatcher < %{oldversion}
Provides:       python2-osa-dispatcher = %{oldversion}
%if 0%{?fedora} >= 28 || 0%{?rhel}
BuildRequires:  python2-devel
Requires:       python2
%else
BuildRequires:  python-devel
Requires:       python
%endif
Requires:       python-jabberpy
Requires:       python2-mgr-osa-common = %{version}-%{release}

%description -n python2-mgr-osa-dispatcher
Python 2 specific files for osa-dispatcher.
%endif

%if 0%{?build_py3}
%package -n python3-mgr-osa-dispatcher
Summary:        OSA dispatcher
Group:          System Environment/Daemons
Obsoletes:      python3-osa-dispatcher < %{oldversion}
Provides:       python3-osa-dispatcher = %{oldversion}
BuildRequires:  python3-devel
Requires:       python3
Requires:       python3-jabberpy
Requires:       python3-mgr-osa-common = %{version}-%{release}

%description -n python3-mgr-osa-dispatcher
Python 3 specific files for osa-dispatcher.
%endif

%prep
%setup -q
%if 0%{?suse_version}
cp prog.init.SUSE prog.init
%endif
%if 0%{?fedora} || 0%{?rhel} >= 8
sed -i 's@^#!/usr/bin/python$@#!/usr/bin/python3 -s@' invocation.py
%endif

%build
%if 0%{?build_py2}
make -f Makefile.osad all PYTHONPATH=%{python_sitelib}
%endif
%if 0%{?build_py3}
make -f Makefile.osad all PYTHONPATH=%{python3_sitelib}
%endif

%install
install -d $RPM_BUILD_ROOT%{rhnroot}
%if 0%{?build_py2}
make -f Makefile.osad install PREFIX=$RPM_BUILD_ROOT ROOT=%{rhnroot} INITDIR=%{_initrddir} \
        PYTHONPATH=%{python_sitelib} PYTHONVERSION=%{python_version}
%endif

%if 0%{?build_py3}
make -f Makefile.osad install PREFIX=$RPM_BUILD_ROOT ROOT=%{rhnroot} INITDIR=%{_initrddir} \
        PYTHONPATH=%{python3_sitelib} PYTHONVERSION=%{python3_version}
sed -i 's|#!/usr/bin/python\b|#!/usr/bin/python3|' $RPM_BUILD_ROOT/usr/sbin/osad-%{python3_version}
sed -i 's|#!/usr/bin/python\b|#!/usr/bin/python3|' $RPM_BUILD_ROOT/usr/sbin/osa-dispatcher-%{python3_version}
%endif

%define default_suffix %{?default_py3:-%{python3_version}}%{!?default_py3:-%{python_version}}
ln -s osad%{default_suffix} $RPM_BUILD_ROOT/usr/sbin/osad
# osa-dispatcher is python2 even on Fedora
ln -s osa-dispatcher%{default_suffix} $RPM_BUILD_ROOT/usr/sbin/osa-dispatcher

mkdir -p %{buildroot}%{_var}/log/rhn
touch %{buildroot}%{_var}/log/osad
touch %{buildroot}%{_var}/log/rhn/osa-dispatcher.log

%if 0%{?fedora} || 0%{?rhel} > 6
sed -i 's/#LOGROTATE-3.8#//' $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d/osa-dispatcher
%endif

%if 0%{?fedora} || 0%{?suse_version} >= 1210 || 0%{?rhel} >= 7
rm $RPM_BUILD_ROOT/%{_initrddir}/osad
rm $RPM_BUILD_ROOT/%{_initrddir}/osa-dispatcher
mkdir -p $RPM_BUILD_ROOT/%{_unitdir}
install -m 0644 osad.service $RPM_BUILD_ROOT/%{_unitdir}/
install -m 0644 osa-dispatcher.service $RPM_BUILD_ROOT/%{_unitdir}/
%endif

mkdir -p %{buildroot}%{_var}/log/rhn
touch %{buildroot}%{_var}/log/osad
touch %{buildroot}%{_var}/log/rhn/osa-dispatcher.log

%if 0%{?suse_version}
%if 0%{?build_py2}
%py_compile -O %{buildroot}/%{python_sitelib}
%endif
%if 0%{?build_py3}
%py3_compile -O %{buildroot}/%{python3_sitelib}
%endif
%endif

%if 0%{?suse_version}
# add rclinks
%if 0%{?suse_version} < 1210
ln -sf ../../etc/init.d/osad %{buildroot}%{_sbindir}/rcosad
ln -sf ../../etc/init.d/osa-dispatcher %{buildroot}%{_sbindir}/rcosa-dispatcher
%else
ln -s %{_sbindir}/service %{buildroot}%{_sbindir}/rcosad
ln -s %{_sbindir}/service %{buildroot}%{_sbindir}/rcosa-dispatcher
%endif
%endif

%{!?systemd_post: %global systemd_post() if [ $1 -eq 1 ] ; then /usr/bin/systemctl enable %%{?*} >/dev/null 2>&1 || : ; fi; }
%{!?systemd_preun: %global systemd_preun() if [ $1 -eq 0 ] ; then /usr/bin/systemctl --no-reload disable %%{?*} > /dev/null 2>&1 || : ; /usr/bin/systemctl stop %%{?*} >/dev/null 2>&1 || : ; fi; }
%{!?systemd_postun_with_restart: %global systemd_postun_with_restart() /usr/bin/systemctl daemon-reload >/dev/null 2>&1 || : ; if [ $1 -ge 1 ] ; then /usr/bin/systemctl try-restart %%{?*} >/dev/null 2>&1 || : ; fi; }

%post
ARG=$1
%if 0%{?suse_version} >= 1210
%service_add_post osad.service
if [ $ARG -eq 1 ] ; then
    # executed only in case of install
    /usr/bin/systemctl enable osad.service >/dev/null 2>&1
    /usr/bin/systemctl start osad.service ||:
fi
%else
if [ -f %{_sysconfdir}/init.d/osad ]; then
    /sbin/chkconfig --add osad ||:
fi
if [ -f %{_unitdir}/osad.service ]; then
    %systemd_post osad.service
    if [ "$1" = "2" ]; then
        # upgrade from old init.d
        if [ -L /etc/rc2.d/S97osad ]; then
            /usr/bin/systemctl enable osad.service >/dev/null 2>&1
        fi
        rm -f /etc/rc?.d/[SK]??osad
    fi
fi

# Fix the /var/log/osad permission BZ 836984
if [ -f %{_var}/log/osad ]; then
    /bin/chmod 600 %{_var}/log/osad
fi
if [ $ARG -eq 1 ] ; then
  # executed only in case of install
  /sbin/service osad start ||:
fi
%endif

%preun
%if 0%{?suse_version} >= 1210
%service_del_preun osad.service
%else
if [ $1 = 0 ]; then
    %if 0%{?fedora} || 0%{?rhel} >= 7
    %systemd_preun osad.service
    %else
    /sbin/service osad stop > /dev/null 2>&1
    /sbin/chkconfig --del osad
    %endif
fi
%endif

%postun
%if 0%{?fedora} || 0%{?rhel} >= 7
%systemd_postun osad.service
%else
%if 0%{?suse_version} >= 1210
%service_del_postun -n osad.service
%endif
%endif

%posttrans
if [ -x /usr/bin/systemctl ]; then
    (
        test "$YAST_IS_RUNNING" = instsys && exit 0
        test -e /var/lib/systemd/migrated/enable-osad && /usr/bin/systemctl enable osad.service >/dev/null 2>&1
        rm -f /var/lib/systemd/migrated/enable-osad 2> /dev/null
        test -e /var/lib/systemd/migrated/activate-osad && /usr/bin/systemctl start osad.service >/dev/null 2>&1
        rm -f /var/lib/systemd/migrated/activate-osad 2> /dev/null
        test -f /etc/sysconfig/services -a \
             -z "$DISABLE_RESTART_ON_UPDATE" && . /etc/sysconfig/services
        test "$DISABLE_RESTART_ON_UPDATE" = yes -o \
             "$DISABLE_RESTART_ON_UPDATE" = 1 && exit 0
        /usr/bin/systemctl try-restart osad.service
    ) || :
fi

%pre
%if 0%{?suse_version} >= 1210
%service_add_pre osad.service
%endif
if [ -x /usr/bin/systemctl ]; then
  (
    [ -d /var/lib/systemd/migrated ] || mkdir -p /var/lib/systemd/migrated || :
    /usr/bin/systemctl is-enabled osad.service >/dev/null 2>&1 && touch /var/lib/systemd/migrated/enable-osad
    /usr/bin/systemctl is-active osad.service >/dev/null 2>&1 && touch /var/lib/systemd/migrated/activate-osad
  ) ||:
fi

%if 0%{?suse_version} >= 1210
%pre -n mgr-osa-dispatcher
%service_add_pre osa-dispatcher.service

%postun -n mgr-osa-dispatcher
%service_del_postun osa-dispatcher.service

%endif

%post -n mgr-osa-dispatcher
%if 0%{?suse_version} >= 1210
%service_add_post osa-dispatcher.service
%else
if [ -f %{_sysconfdir}/init.d/osa-dispatcher ]; then
    /sbin/chkconfig --add osa-dispatcher ||:
fi
if [ -f %{_unitdir}/osa-dispatcher.service ]; then
    %systemd_post osa-dispatcher.service
    if [ "$1" = "2" ]; then
        # upgrade from old init.d
        if [ -L /etc/rc2.d/S86osa-dispatcher ]; then
            /usr/bin/systemctl enable osa-dispatcher.service >/dev/null 2>&1
        fi
        rm -f /etc/rc?.d/[SK]??osa-dispatcher
    fi
fi
%endif

%preun -n mgr-osa-dispatcher
%if 0%{?suse_version} >= 1210
%service_del_preun osa-dispatcher.service
%else
if [ $1 = 0 ]; then
    %if 0%{?fedora} || 0%{?rhel} >= 7
    %systemd_preun osa-dispatcher.service
    %else
    /sbin/service osa-dispatcher stop > /dev/null 2>&1
    /sbin/chkconfig --del osa-dispatcher
    %endif
fi
%endif

%files
%defattr(-,root,root)
%{_sbindir}/osad
%config(noreplace) %{_sysconfdir}/sysconfig/rhn/osad.conf
%config(noreplace) %attr(600,root,root) %{_sysconfdir}/sysconfig/rhn/osad-auth.conf
%config(noreplace) %{client_caps_dir}/*
%if 0%{?fedora} || 0%{?suse_version} >= 1210 || 0%{?rhel} >= 7
%{_unitdir}/osad.service
%else
%attr(755,root,root) %{_initrddir}/osad
%endif
%{!?_licensedir:%global license %doc}
%license LICENSE
%config(noreplace) %attr(644,root,root) %{_sysconfdir}/logrotate.d/osad
%ghost %attr(600,root,root) %{_var}/log/osad
%if 0%{?suse_version}
%{_sbindir}/rcosad
# provide directories not owned by any package during build
%dir %{_sysconfdir}/sysconfig/rhn
%dir %{_sysconfdir}/sysconfig/rhn/clientCaps.d
%endif

%if 0%{?build_py2}
%files -n python2-%{name}
%defattr(-,root,root)
%attr(755,root,root) %{_sbindir}/osad-%{python_version}
%dir %{python_sitelib}/osad
%{python_sitelib}/osad/osad.py*
%{python_sitelib}/osad/osad_client.py*
%{python_sitelib}/osad/osad_config.py*
%endif

%if 0%{?build_py3}
%files -n python3-%{name}
%defattr(-,root,root)
%attr(755,root,root) %{_sbindir}/osad-%{python3_version}
%dir %{python3_sitelib}/osad
%{python3_sitelib}/osad/osad.py*
%{python3_sitelib}/osad/osad_client.py*
%{python3_sitelib}/osad/osad_config.py*
%dir %{python3_sitelib}/osad/__pycache__
%{python3_sitelib}/osad/__pycache__/osad.*
%{python3_sitelib}/osad/__pycache__/osad_client.*
%{python3_sitelib}/osad/__pycache__/osad_config.*
%endif

%files -n mgr-osa-dispatcher
%defattr(0644,root,root,0755)
%{_sbindir}/osa-dispatcher
%config(noreplace) %{_sysconfdir}/sysconfig/osa-dispatcher
%config(noreplace) %{_sysconfdir}/logrotate.d/osa-dispatcher
%{rhnroot}/config-defaults/rhn_osa-dispatcher.conf
%dir %{_sysconfdir}/rhn/tns_admin
%dir %{_sysconfdir}/rhn/tns_admin/osa-dispatcher
%config(noreplace) %{_sysconfdir}/rhn/tns_admin/osa-dispatcher/sqlnet.ora
%if 0%{?fedora} || 0%{?suse_version} >= 1210 || 0%{?rhel} >= 7
%{_unitdir}/osa-dispatcher.service
%else
%attr(755,root,root) %{_initrddir}/osa-dispatcher
%endif
%attr(770,root,%{apache_group}) %dir %{_var}/log/rhn/oracle
%attr(770,root,root) %dir %{_var}/log/rhn/oracle/osa-dispatcher
%{!?_licensedir:%global license %doc}
%license LICENSE
%ghost %attr(640,%{apache_user},%{apache_group}) %{_var}/log/rhn/osa-dispatcher.log
%if 0%{?suse_version}
%{_sbindir}/rcosa-dispatcher
%dir %{rhnroot}
%dir %{_sysconfdir}/rhn/tns_admin
%attr(770,root,%{apache_group}) %dir %{_var}/log/rhn
%endif

%if 0%{?build_py2}
%files -n python2-mgr-osa-dispatcher
%defattr(-,root,root)
%attr(755,root,root) %{_sbindir}/osa-dispatcher-%{python_version}
%dir %{python_sitelib}/osad
%{python_sitelib}/osad/osa_dispatcher.py*
%{python_sitelib}/osad/dispatcher_client.py*
%endif

%if 0%{?build_py3}
%files -n python3-mgr-osa-dispatcher
%defattr(-,root,root)
%attr(755,root,root) %{_sbindir}/osa-dispatcher-%{python3_version}
%dir %{python3_sitelib}/osad
%{python3_sitelib}/osad/osa_dispatcher.py*
%{python3_sitelib}/osad/dispatcher_client.py*
%{python3_sitelib}/osad/__pycache__/osa_dispatcher.*
%{python3_sitelib}/osad/__pycache__/dispatcher_client.*
%endif

%if 0%{?build_py2}
%files -n python2-mgr-osa-common
%defattr(-,root,root)
%{python_sitelib}/osad/__init__.py*
%{python_sitelib}/osad/jabber_lib.py*
%{python_sitelib}/osad/rhn_log.py*
%endif

%if 0%{?build_py3}
%files -n python3-mgr-osa-common
%defattr(-,root,root)
%{python3_sitelib}/osad/__init__.py*
%{python3_sitelib}/osad/jabber_lib.py*
%{python3_sitelib}/osad/rhn_log.py*
%{python3_sitelib}/osad/__pycache__/__init__.*
%{python3_sitelib}/osad/__pycache__/jabber_lib.*
%{python3_sitelib}/osad/__pycache__/rhn_log.*
%endif

%changelog
