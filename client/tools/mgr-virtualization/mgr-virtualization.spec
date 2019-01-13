#
# spec file for package mgr-virtualization
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
# Old name and version+1 before renaming to mgr-push
%define oldname          rhn-virtualization
%define oldversion       5.4.72.3
#
%define rhn_dir %{_datadir}/rhn
%define rhn_conf_dir %{_sysconfdir}/sysconfig/rhn
%define cron_dir %{_sysconfdir}/cron.d

%if 0%{?fedora} || 0%{?suse_version} > 1320 || 0%{?rhel} >= 8
%global build_py3   1
%global default_py3 1
%endif

%if ( 0%{?fedora} && 0%{?fedora} < 28 ) || ( 0%{?rhel} && 0%{?rhel} < 8 ) || 0%{?suse_version}
%global build_py2   1
%endif

%define pythonX %{?default_py3: python3}%{!?default_py3: python2}

Name:           mgr-virtualization
Summary:        Spacewalk action support for virualization
License:        GPL-2.0-only
Group:          System Environment/Base
Version:        4.0.2
Provides:       rhn-virtualization = %{oldversion}
Obsoletes:      rhn-virtualization < %{oldversion}
Release:        1%{?dist}

URL:            https://github.com/uyuni-project/uyuni
Source0:        https://github.com/spacewalkproject/spacewalk/archive/%{name}-%{version}.tar.gz
Source1:        %{name}-rpmlintrc

BuildRoot:      %{_tmppath}/%{name}-%{version}-build
%if 0%{?fedora} || 0%{?rhel} || 0%{?suse_version} >= 1210
BuildArch:      noarch
%endif
%if 0%{?suse_version}
# make chkconfig work in OBS
BuildRequires:  sysconfig
BuildRequires:  syslog
%endif

%description
rhn-virtualization provides various Spacewalk actions for manipulation
virtual machine guest images.

%if 0%{?build_py2}
%package -n python2-%{name}-common
Summary:        Files needed by rhn-virtualization-host
Group:          System Environment/Base
%{?python_provide:%python_provide python2-%{name}-common}
Provides:       python-%{name}-common = %{oldversion}
Obsoletes:      python-%{name}-common < %{oldversion}
Provides:       python-%{oldname}-common = %{oldversion}
Obsoletes:      python-%{oldname}-common < %{oldversion}
Provides:       %{name}-common = %{oldversion}
Obsoletes:      %{name}-common < %{oldversion}
Provides:       %{oldname}-common = %{oldversion}
Obsoletes:      %{oldname}-common < %{oldversion}
Requires:       python2-rhn-client-tools
Requires:       spacewalk-usix
BuildRequires:  python
%if 0%{?suse_version}
# aaa_base provide chkconfig
Requires:       aaa_base
# provide directories for filelist check in obs
BuildRequires:  rhn-check
BuildRequires:  rhn-client-tools
%else
Requires:       chkconfig
%endif
%description -n python2-%{name}-common
This package contains files that are needed by the rhn-virtualization-host
package.
%endif

%if 0%{?build_py3}
%package -n python3-%{name}-common
Summary:        Files needed by rhn-virtualization-host
Group:          System Environment/Base
Provides:       python3-%{oldname}-common = %{oldversion}
Obsoletes:      python3-%{oldname}-common < %{oldversion}
Obsoletes:      %{name}-common < %{oldversion}
Obsoletes:      %{oldname}-common < %{oldversion}
Requires:       python3-rhn-client-tools
Requires:       python3-spacewalk-usix
BuildRequires:  python3-devel

%description -n python3-%{name}-common
This package contains files that are needed by the rhn-virtualization-host
package.
%endif

%package host
Summary:        Spacewalk Virtualization support specific to the Host system
Group:          System Environment/Base
Provides:       %{oldname}-host = %{oldversion}
Obsoletes:      %{oldname}-host < %{oldversion}
Requires:       %{pythonX}-%{name}-host = %{version}-%{release}

%if 0%{?default_py3}
BuildRequires:  systemd
Requires(pre): systemd
Requires(post): systemd
Requires(preun): systemd
Requires(postun): systemd
%else
%if 0%{?suse_version}
Requires:       cron
%else
Requires:       /usr/sbin/crond
%endif
%endif

%description host
This package contains code for Spacewalk's Virtualization support
that is specific to the Host system (a.k.a. Dom0).

%if 0%{?build_py2}
%package -n python2-%{name}-host
Summary:        RHN/Spacewalk Virtualization support specific to the Host system
Group:          System Environment/Base
Provides:       python2-%{oldname}-host = %{oldversion}
Obsoletes:      python2-%{oldname}-host < %{oldversion}
Requires:       %{name}-host = %{version}-%{release}
Requires:       libvirt-python
Requires:       python2-%{name}-common = %{version}-%{release}
%if 0%{?suse_version}
Requires:       python-curl
%else
Requires:       python-pycurl
%endif
%description -n python2-%{name}-host
Python 2 files for %{name}-host.
%endif

%if 0%{?build_py3}
%package -n python3-%{name}-host
Summary:        RHN/Spacewalk Virtualization support specific to the Host system
Group:          System Environment/Base
Provides:       python3-%{oldname}-host = %{oldversion}
Obsoletes:      python3-%{oldname}-host < %{oldversion}
Requires:       %{name}-host = %{version}-%{release}
%if 0%{?suse_version}
Requires:       python3-libvirt-python
%else
Requires:       libvirt-python3
%endif
Requires:       python3-%{name}-common = %{version}-%{release}
Requires:       python3-pycurl

%description -n python3-%{name}-host
Python 3 files for %{name}-host.
%endif

%prep
%setup -q

%build
make -f Makefile.rhn-virtualization

%install
%if 0%{?build_py2}
make -f Makefile.rhn-virtualization DESTDIR=$RPM_BUILD_ROOT PKGDIR0=%{_initrddir} \
        PYTHONPATH=%{python_sitelib} install
sed -i 's,@PYTHON@,python,; s,@PYTHONPATH@,%{python_sitelib},;' \
        $RPM_BUILD_ROOT/%{_initrddir}/rhn-virtualization-host
%endif

%if 0%{?build_py3}
make -f Makefile.rhn-virtualization DESTDIR=$RPM_BUILD_ROOT PKGDIR0=%{_initrddir} \
        PYTHONPATH=%{python3_sitelib} install
        sed -i 's,@PYTHON@,python3,; s,@PYTHONPATH@,%{python3_sitelib},;' \
                $RPM_BUILD_ROOT/%{_initrddir}/rhn-virtualization-host
%endif

%if 0%{?default_py3}
install -d %{buildroot}%{_unitdir}
install -D -m 0644 scripts/mgr-virtualization.timer %{buildroot}%{_unitdir}/mgr-virtualization.timer
install -D -m 0644 scripts/mgr-virtualization.service %{buildroot}%{_unitdir}/mgr-virtualization.service
sed -i 's,@PYTHON@,python3,; s,@PYTHONPATH@,%{python3_sitelib},;' \
       %{buildroot}%{_unitdir}/mgr-virtualization.service

%else
install -d $RPM_BUILD_ROOT%{cron_dir}
install -D -m 0644 scripts/rhn-virtualization.cron %{cron_dir}/rhn-virtualization.cron
sed -i 's,@PYTHON@,python,; s,@PYTHONPATH@,%{python_sitelib},;' \
        $RPM_BUILD_ROOT/%{cron_dir}/rhn-virtualization.cron
%endif

%if 0%{?suse_version}
rm -f $RPM_BUILD_ROOT/%{_initrddir}/rhn-virtualization-host
%endif

%if 0%{?suse_version}
%py_compile -O %{buildroot}/%{python_sitelib}
%if 0%{?build_py3}
%py3_compile -O %{buildroot}/%{python3_sitelib}
%endif
%endif

%if 0%{?suse_version}
rm -f $RPM_BUILD_ROOT/%{_initrddir}/rhn-virtualization-host
%py_compile -O %{buildroot}/%{python_sitelib}
%if 0%{?build_py3}
%py3_compile -O %{buildroot}/%{python3_sitelib}
%endif
%endif

%if 0%{?suse_version}
%post host
if [ -d /proc/xen ]; then
    # xen kernel is running
    # change the default template to the xen version
    sed -i 's@^IMAGE_CFG_TEMPLATE=/etc/sysconfig/rhn/kvm-template.xml@IMAGE_CFG_TEMPLATE=/etc/sysconfig/rhn/xen-template.xml@' /etc/sysconfig/rhn/image.cfg
fi
%if 0%{?default_py3}
%service_add_post mgr-virtualization.timer

%pre
%service_add_pre mgr-virtualization.timer

%preun
%service_del_preun mgr-virtualization.timer

%postun
%service_del_postun mgr-virtualization.timer
%endif

%else

%if 0%{?default_py3}
%{!?systemd_post: %global systemd_post() if [ $1 -eq 1 ] ; then /usr/bin/systemctl enable %%{?*} >/dev/null 2>&1 || : ; fi; }
%{!?systemd_preun: %global systemd_preun() if [ $1 -eq 0 ] ; then /usr/bin/systemctl --no-reload disable %%{?*} > /dev/null 2>&1 || : ; /usr/bin/systemctl stop %%{?*} > /dev/null 2>&1 || : ; fi; }
%{!?systemd_postun_with_restart: %global systemd_postun_with_restart() /usr/bin/systemctl daemon-reload >/dev/null 2>&1 || : ; if [ $1 -ge 1 ] ; then /usr/bin/systemctl try-restart %%{?*} >/dev/null 2>&1 || : ; fi; }
%endif

%post host
%if 0%{?default_py3}
%systemd_post mgr-virtualization.timer
%else
/sbin/chkconfig --add rhn-virtualization-host
/sbin/service crond condrestart
%endif
if [ -d /proc/xen ]; then
    # xen kernel is running
    # change the default template to the xen version
    sed -i 's@^IMAGE_CFG_TEMPLATE=/etc/sysconfig/rhn/kvm-template.xml@IMAGE_CFG_TEMPLATE=/etc/sysconfig/rhn/xen-template.xml@' /etc/sysconfig/rhn/image.cfg
fi

%preun host
%if 0%{?default_py3}
%systemd_preun mgr-virtualization.timer
%else
if [ $1 = 0 ]; then
  /sbin/chkconfig --del rhn-virtualization-host
fi
%endif

%postun host
%if 0%{?default_py3}
%systemd_postun_with_restart mgr-virtualization.timer
%else
/sbin/service crond condrestart
%endif
%endif

%if 0%{?build_py2}
%files -n python2-%{name}-common
%defattr(-,root,root,-)
%dir %{python_sitelib}/virtualization
%{python_sitelib}/virtualization/__init__.py*
%{python_sitelib}/virtualization/batching_log_notifier.py*
%{python_sitelib}/virtualization/constants.py*
%{python_sitelib}/virtualization/errors.py*
%{python_sitelib}/virtualization/notification.py*
%{python_sitelib}/virtualization/util.py*
%doc LICENSE
%if 0%{?suse_version}
%dir %{python_sitelib}/virtualization
%endif
%endif

%if 0%{?build_py3}
%files -n python3-%{name}-common
%defattr(-,root,root,-)
%dir %{python3_sitelib}/virtualization
%{python3_sitelib}/virtualization/__init__.py*
%{python3_sitelib}/virtualization/batching_log_notifier.py*
%{python3_sitelib}/virtualization/constants.py*
%{python3_sitelib}/virtualization/errors.py*
%{python3_sitelib}/virtualization/notification.py*
%{python3_sitelib}/virtualization/util.py*
%doc LICENSE
%dir %{python3_sitelib}/virtualization/__pycache__
%{python3_sitelib}/virtualization/__pycache__/__init__.*
%{python3_sitelib}/virtualization/__pycache__/batching_log_notifier.*
%{python3_sitelib}/virtualization/__pycache__/constants.*
%{python3_sitelib}/virtualization/__pycache__/errors.*
%{python3_sitelib}/virtualization/__pycache__/notification.*
%{python3_sitelib}/virtualization/__pycache__/util.*
%if 0%{?suse_version}
%dir %{python3_sitelib}/virtualization
%endif
%endif

%files host
%defattr(-,root,root,-)
%if 0%{?suse_version}
%dir %{rhn_conf_dir}
%else
%{_initrddir}/rhn-virtualization-host
%endif
%dir %{rhn_conf_dir}/virt
%dir %{rhn_conf_dir}/virt/auto
%if 0%{?default_py3}
%{_unitdir}/mgr-virtualization.service
%{_unitdir}/mgr-virtualization.timer
%else
%config(noreplace) %attr(644,root,root) %{cron_dir}/rhn-virtualization.cron
%endif
%{rhn_conf_dir}/*-template.xml
%config(noreplace) %{rhn_conf_dir}/image.cfg
%doc LICENSE

%if 0%{?build_py2}
%files -n python2-%{name}-host
%defattr(-,root,root,-)
%dir %{python_sitelib}/rhn
%dir %{python_sitelib}/rhn/actions
%{python_sitelib}/virtualization/domain_config.py*
%{python_sitelib}/virtualization/domain_control.py*
%{python_sitelib}/virtualization/domain_directory.py*
%{python_sitelib}/virtualization/get_config_value.py*
%{python_sitelib}/virtualization/init_action.py*
%{python_sitelib}/virtualization/poller.py*
%{python_sitelib}/virtualization/schedule_poller.py*
%{python_sitelib}/virtualization/poller_state_cache.py*
%{python_sitelib}/virtualization/start_domain.py*
%{python_sitelib}/virtualization/state.py*
%{python_sitelib}/virtualization/support.py*
%{python_sitelib}/rhn/actions/virt.py*
%{python_sitelib}/rhn/actions/image.py*
%if 0%{?suse_version}
%dir %{python_sitelib}/rhn
%dir %{python_sitelib}/rhn/actions
%endif
%endif

%if 0%{?build_py3}
%files -n python3-%{name}-host
%defattr(-,root,root,-)
%dir %{python3_sitelib}/rhn
%dir %{python3_sitelib}/rhn/actions
%dir %{python3_sitelib}/rhn/actions/__pycache__
%{python3_sitelib}/virtualization/domain_config.py*
%{python3_sitelib}/virtualization/domain_control.py*
%{python3_sitelib}/virtualization/domain_directory.py*
%{python3_sitelib}/virtualization/get_config_value.py*
%{python3_sitelib}/virtualization/init_action.py*
%{python3_sitelib}/virtualization/poller.py*
%{python3_sitelib}/virtualization/schedule_poller.py*
%{python3_sitelib}/virtualization/poller_state_cache.py*
%{python3_sitelib}/virtualization/start_domain.py*
%{python3_sitelib}/virtualization/state.py*
%{python3_sitelib}/virtualization/support.py*
%{python3_sitelib}/rhn/actions/virt.py*
%{python3_sitelib}/rhn/actions/image.py*
%{python3_sitelib}/virtualization/__pycache__/domain_config.*
%{python3_sitelib}/virtualization/__pycache__/domain_control.*
%{python3_sitelib}/virtualization/__pycache__/domain_directory.*
%{python3_sitelib}/virtualization/__pycache__/get_config_value.*
%{python3_sitelib}/virtualization/__pycache__/init_action.*
%{python3_sitelib}/virtualization/__pycache__/poller.*
%{python3_sitelib}/virtualization/__pycache__/schedule_poller.*
%{python3_sitelib}/virtualization/__pycache__/poller_state_cache.*
%{python3_sitelib}/virtualization/__pycache__/start_domain.*
%{python3_sitelib}/virtualization/__pycache__/state.*
%{python3_sitelib}/virtualization/__pycache__/support.*
%{python3_sitelib}/rhn/actions/__pycache__/virt.*
%{python3_sitelib}/rhn/actions/__pycache__/image.*
%if 0%{?suse_version}
%dir %{python3_sitelib}/rhn
%dir %{python3_sitelib}/rhn/actions
%dir %{python3_sitelib}/rhn/actions/__pycache__
%endif
%endif

%changelog
