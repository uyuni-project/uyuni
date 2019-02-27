#
# spec file for package spacewalk-client-tools
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


%if 0%{?fedora} || 0%{?suse_version} > 1320 || 0%{?rhel} >= 8 || 0%{?mageia}
%global build_py3   1
%global default_py3 1
%endif

%global build_py2   1

%if %{_vendor} == "debbuild"
%{!?_presetdir:%global _presetdir /lib/systemd/system-preset}
# Bash constructs in scriptlets don't play nice with Debian's default shell, dash
%global _buildshell /bin/bash
%endif

%{!?__python2:%global __python2 /usr/bin/python2}
%{!?__python3:%global __python3 /usr/bin/python3}

%if %{undefined python2_version}
%global python2_version %(%{__python2} -Esc "import sys; sys.stdout.write('{0.major}.{0.minor}'.format(sys.version_info))")
%endif

%if %{undefined python3_version}
%global python3_version %(%{__python3} -Ic "import sys; sys.stdout.write(sys.version[:3])")
%endif

%if %{undefined python2_sitelib}
%global python2_sitelib %(%{__python2} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")
%endif

%if %{undefined python3_sitelib}
%global python3_sitelib %(%{__python3} -c "from distutils.sysconfig import get_python_lib; print(get_python_lib())")
%endif

%if %{_vendor} == "debbuild"
# For making sure we can set the right args for deb distros
%global is_deb 1
%endif

%define pythonX %{?default_py3: python3}%{!?default_py3: python2}

# package renaming fun :(
%define rhn_client_tools spacewalk-client-tools
%define rhn_setup	 spacewalk-client-setup
%define rhn_check	 spacewalk-check
%define rhnsd		 spacewalksd
#
%define without_rhn_register 1
%bcond_with    test

Name:           spacewalk-client-tools
Summary:        Support programs and libraries for Spacewalk
License:        GPL-2.0-only
%if %{_vendor} == "debbuild"
Group:      admin
Packager:   Uyuni Project <uyuni-devel@opensuse.org>
%else
Group:          System Environment/Base
%endif
Source0:        spacewalk-client-tools-%{version}.tar.gz
Source1:        %{name}-rpmlintrc
URL:            https://github.com/uyuni-project/uyuni
Version:        4.0.5
Release:        1%{?dist}
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
%if 0%{?fedora} || 0%{?rhel} || 0%{?suse_version} >= 1210 || 0%{?mageia} >= 6
BuildArch:      noarch
%endif
%if 0%{?suse_version}
BuildRequires:  update-desktop-files
%endif
Provides:       rhn-client-tools = %{version}-%{release}
Obsoletes:      rhn-client-tools < %{version}-%{release}
%if %{without_rhn_register}
Obsoletes:      rhn-setup-gnome
%endif
Requires:       %{pythonX}-%{name} = %{version}-%{release}
%if %{_vendor} != "debbuild"
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
%endif # 0%{?fedora}
%endif # 0%{?suse_version}
%endif # %{_vendor} != "debbuild"

%if %{_vendor} == "debbuild"
Requires: apt
%if 0%{?ubuntu} >= 1804
Requires: gpg
%else
Requires: gnupg
%endif
Requires: coreutils
%endif
BuildRequires: rpm

Conflicts:      up2date < 5.0.0
Conflicts:      yum-rhn-plugin < 1.6.4-1
Conflicts:      rhncfg < 5.9.23-1
Conflicts:      spacewalk-koan < 0.2.7-1
Conflicts:      rhn-kickstart < 5.4.3-1
Conflicts:      rhn-virtualization-host < 5.4.36-2

BuildRequires:  desktop-file-utils
BuildRequires:  gettext
BuildRequires:  intltool

%if 0%{?fedora}
BuildRequires:  dnf
BuildRequires:  fedora-logos
%endif

%if 0%{?mageia} >= 6
BuildRequires: dnf
%endif

%if 0%{?rhel}
BuildRequires:  redhat-logos
%if 0%{?rhel} >= 8
BuildRequires:  dnf
%else
BuildRequires:  yum
%endif
%endif

%description
Spacewalk Client Tools provides programs and libraries to allow your
system to receive software updates from Spacewalk.

%if 0%{?build_py2}
%package -n python2-%{name}
Summary:        Support programs and libraries for Spacewalk
%if %{_vendor} == "debbuild"
Group: python
%else
Group:          System Environment/Base
%endif
Provides:       python-%{name} = %{version}-%{release}
Obsoletes:      python-%{name} < %{version}-%{release}
Provides:       python2-rhn-client-tools = %{version}-%{release}
Obsoletes:      python2-rhn-client-tools < %{version}-%{release}
Requires:       %{name} = %{version}-%{release}
Requires:       rhnlib >= 2.5.78

%if %{_vendor} != "debbuild"
Requires:       rpm-python
Requires:       spacewalk-usix
%ifnarch s390 s390x
Requires:       python-dmidecode
%endif
Requires:       python-ethtool >= 0.4
BuildRequires:  python-devel
%if 0%{?fedora}
Requires:       libgudev
Requires:       pygobject2
Requires:       python-hwdata
%else
%if 0%{?suse_version} >= 1140
Requires:       python-hwdata
Requires:       python-pyudev
%else
%if 0%{?rhel} > 5
Requires:       python-gudev
Requires:       python-hwdata
%else
Requires:       hal >= 0.5.8.1-52
%endif # 0%{?rhel} > 5
%endif # 0%{?suse_version} >= 1140
%endif # 0%{?fedora}

%if 0%{?rhel} == 5
Requires:       newt
%endif

%if 0%{?rhel} > 5 || 0%{?fedora}
Requires:       newt-python
%endif

%if 0%{?suse_version}
Requires:       dbus-1-python
Requires:       python-newt
%else
Requires:       dbus-python
%endif # 0%{?suse_version}
Requires:       logrotate
Requires:       suseRegisterInfo

%if %{with test} && 0%{?rhel} != 6
# The following BuildRequires are for check only
BuildRequires:  python-coverage
BuildRequires:  rpm-python
%endif
%endif #%if %{_vendor} != "debbuild"

%if %{_vendor} == "debbuild"
Requires: python-rpm
Requires: python-dmidecode
Requires: python-ethtool >= 0.4
BuildRequires: python-dev
Requires: python2-hwdata
BuildRequires: python-rpm
BuildRequires: python-coverage
Requires: gir1.2-gudev-1.0
Requires: python-gi
Requires: python-pyudev
Requires: python-dbus
Requires: python-newt
Requires(preun): python-minimal
Requires(post): python-minimal
%endif

%description -n python2-%{name}
Python 2 specific files of %{name}.
%endif

%if 0%{?build_py3}
%package -n python3-%{name}
Summary:        Support programs and libraries for Spacewalk
%if %{_vendor} == "debbuild"
Group: python
%else
Group:          System Environment/Base
%endif
Provides:       python3-rhn-client-tools = %{version}-%{release}
Obsoletes:      python3-rhn-client-tools < %{version}-%{release}
Requires:       %{name} = %{version}-%{release}
%if %{_vendor} != "debbuild"
%if 0%{?suse_version}
%if 0%{?suse_version} >= 1500
Requires:       python3-dbus-python
%else
Requires:       dbus-1-python3
%endif
Requires:       libgudev-1_0-0
Requires:       python3-newt
Requires:       python3-pyudev
%else
Requires:       libgudev
Requires:       newt-python3
Requires:       python3-dbus
Requires:       python3-gobject-base
%endif
BuildRequires:  python3-devel
%endif

%ifnarch s390 s390x
Requires:       python3-dmidecode
%endif
Requires:       python3-hwdata
Requires:       python3-netifaces
Requires:       python3-rhnlib >= 2.5.78
Requires:       python3-rpm
Requires:       python3-spacewalk-usix

%if %{_vendor} == "debbuild"
BuildRequires: python3-dev
Requires: python3-dbus
Requires: python3-newt
Requires: gir1.2-gudev-1.0
Requires: python3-pyudev
Requires: python3-gi
Requires(preun): python3-minimal
Requires(post): python3-minimal
%endif

%if %{with test} && 0%{?rhel} != 6
# The following BuildRequires are for check only
BuildRequires:  python3-coverage
BuildRequires:  python3-rpm
%endif

%description -n python3-%{name}
Python 3 specific files of %{name}.
%endif

%package -n spacewalk-check
Summary:        Check for Spacewalk actions
Provides:       rhn-check = %{version}-%{release}
Obsoletes:      rhn-check < %{version}-%{release}
Requires:       %{name} = %{version}-%{release}
Requires:       %{pythonX}-spacewalk-check = %{version}-%{release}
%if %{_vendor} != "debbuild"
Group:          System Environment/Base
%if 0%{?suse_version}
Requires:       zypp-plugin-spacewalk >= 1.0.2
%else
%if 0%{?fedora} || 0%{?rhel} >= 8
Requires:       dnf-plugin-spacewalk >= 2.4.0
%else
Requires:       yum-rhn-plugin >= 2.8.2
%endif
%endif
%endif

%if %{_vendor} == "debbuild"
Requires: apt-transport-spacewalk
%endif

%description -n spacewalk-check
spacewalk-check polls a SUSE Manager or Spacewalk server to find and execute
scheduled actions.

%if 0%{?build_py2}
%package -n python2-spacewalk-check
Summary:        Check for RHN actions
Group:          System Environment/Base
Provides:       python-spacewalk-check = %{version}-%{release}
Obsoletes:      python-spacewalk-check < %{version}-%{release}
Provides:       python2-rhn-check = %{version}-%{release}
Obsoletes:      python2-rhn-check < %{version}-%{release}
Requires:       spacewalk-check = %{version}-%{release}

%if %{_vendor} == "debbuild"
Requires(preun): python-minimal
Requires(post): python-minimal
%endif

%description -n python2-spacewalk-check
Python 2 specific files for rhn-check.
%endif

%if 0%{?build_py3}
%package -n python3-spacewalk-check
Summary:        Support programs and libraries for Spacewalk
Group:          System Environment/Base
Provides:       python3-rhn-check = %{version}-%{release}
Obsoletes:      python3-rhn-check < %{version}-%{release}
Requires:       spacewalk-check = %{version}-%{release}

%if %{_vendor} == "debbuild"
Requires(preun): python3-minimal
Requires(post): python3-minimal
%endif

%description -n python3-spacewalk-check
Python 3 specific files for spacewalk-check.
%endif

%package -n spacewalk-client-setup
Summary:        Configure and register an Spacewalk client
Group:          System Environment/Base
Provides:       rhn-setup = %{version}-%{release}
Obsoletes:      rhn-setup < %{version}-%{release}
Requires:       %{pythonX}-spacewalk-client-setup
%if 0%{?fedora} || 0%{?rhel} || 0%{?debian} || 0%{?ubuntu}
Requires:       usermode >= 1.36
%endif
%if 0%{?mageia}
Requires: usermode-consoleonly >= 1.36
%endif
Requires:       %{name} = %{version}-%{release}
Requires:       %{rhnsd}
Requires:       suseRegisterInfo

%description -n spacewalk-client-setup
spacewalk-client-setup contains programs and utilities to configure a system to use
SUSE Manager or Spacewalk.

%if 0%{?build_py2}
%package -n python2-spacewalk-client-setup
Summary:        Configure and register an Spacewalk client
Group:          System Environment/Base
Provides:       python-spacewalk-client-setup = %{version}-%{release}
Obsoletes:      python-spacewalk-client-setup < %{version}-%{release}
Provides:       python2-rhn-setup = %{version}-%{release}
Obsoletes:      python2-rhn-setup < %{version}-%{release}
Requires:       spacewalk-client-setup = %{version}-%{release}
%if 0%{?rhel} == 5
Requires:       newt
%endif
%if 0%{?fedora} || 0%{?rhel} > 5
Requires:       newt-python
%endif
%if 0%{?suse_version} || 0%{?mageia} || 0%{?debian} || 0%{?ubuntu}
Requires:       python-newt
%endif

%if %{_vendor} == "debbuild"
Requires(preun): python-minimal
Requires(post): python-minimal
%endif


%description -n python2-spacewalk-client-setup
Python 2 specific files for spacewalk-client-setup.
%endif

%if 0%{?build_py3}
%package -n python3-spacewalk-client-setup
Summary:        Configure and register an Spacewalk client
Group:          System Environment/Base
Provides:       python3-rhn-setup = %{version}-%{release}
Obsoletes:      python3-rhn-setup < %{version}-%{release}
Requires:       spacewalk-client-setup = %{version}-%{release}
%if 0%{?suse_version} || 0%{?mageia} || 0%{?debian} || 0%{?ubuntu}
Requires:       python3-newt
%else
Requires:       newt-python3
%endif

%if %{_vendor} == "debbuild"
Requires(preun): python3-minimal
Requires(post): python3-minimal
%endif

%description -n python3-spacewalk-client-setup
Python 3 specific files for spacewalk-client-setup.
%endif

%if ! 0%{?without_rhn_register}
%package -n spacewalk-client-setup-gnome
Summary:        A GUI interface for RHN/Spacewalk Registration
Group:          System Environment/Base
Requires:       %{name} = %{version}-%{release}
Requires:       %{pythonX}-spacewalk-client-setup
Requires:       spacewalk-client-setup = %{version}-%{release}

%if %{_vendor} == "debbuild"
Requires: libpam0g
Requires: libpam-modules
Requires: libpam-runtime
Requires: libpam-gnome-keyring
%else
Requires:       pam >= 0.72
%endif

%description -n spacewalk-client-setup-gnome
rhn-setup-gnome contains a GTK+ graphical interface for configuring and
registering a system with a Red Hat Satellite or Spacewalk server.

%if 0%{?build_py2}
%package -n python2-spacewalk-client-setup-gnome
Summary:        Configure and register an RHN/Spacewalk client
Group:          System Environment/Base
Provides:       python-spacewalk-client-setup-gnome = %{version}-%{release}
Obsoletes:      python-spacewalk-client-setup-gnome < %{version}-%{release}
Requires:       spacewalk-client-setup-gnome = %{version}-%{release}
%if %{_vendor} != "debbuild"
%if 0%{?suse_version}
Requires:       gtk3
Requires:       python3-gobject
%else
Requires:       gtk3
Requires:       python3-gobject-base
# gtk-builder-convert
BuildRequires:  gtk2-devel
%endif
%if 0%{?fedora} || 0%{?rhel} > 5
Requires:       liberation-sans-fonts
%endif
%endif

%if %{_vendor} == "debbuild"
Requires: python-gnome2
Requires: python-gtk2
Requires: python-glade2
Requires: usermode
Requires: fonts-liberation
Requires(preun): python-minimal
Requires(post): python-minimal
%endif

%description -n python2-spacewalk-client-setup-gnome
Python 2 specific files for spacewalk-client-setup-gnome.
%endif

%if 0%{?build_py3}
%package -n python3-spacewalk-client-setup-gnome
Summary:        Configure and register an RHN/Spacewalk client
Group:          System Environment/Base
Requires:       spacewalk-client-setup-gnome = %{version}-%{release}
%if %{_vendor} != "debbuild"
%if 0%{?suse_version}
Requires:       python-gnome
Requires:       python-gtk
%else
Requires:       pygtk2
Requires:       pygtk2-libglade
Requires:       usermode-gtk
%endif
%if 0%{?fedora} || 0%{?rhel} > 5
Requires:       liberation-sans-fonts
%endif
%endif

%if %{_vendor} == "debbuild"
BuildRequires: libgtk2.0-dev
Requires: libgtk-3-bin
Requires: gir1.2-gtk-3.0

Requires: python3-gi
Requires: fonts-liberation
Requires(preun): python3-minimal
Requires(post): python3-minimal
%endif

%description -n python3-spacewalk-client-setup-gnome
Python 3 specific files for spacewalk-client-setup-gnome.
%endif
%endif

%prep
%setup -q

%build
make -f Makefile.rhn-client-tools %{?is_deb:PLATFORM=deb}

%install
%if 0%{?build_py2}
make -f Makefile.rhn-client-tools install VERSION=%{version}-%{release} \
        PYTHONPATH=%{python_sitelib} PYTHONVERSION=%{python_version} \
        PREFIX=$RPM_BUILD_ROOT MANPATH=%{_mandir} %{?is_deb:PLATFORM=deb}
%endif
%if 0%{?build_py3}
sed -i 's|#!/usr/bin/python|#!/usr/bin/python3|' src/actions/*.py src/bin/*.py test/*.py
make -f Makefile.rhn-client-tools %{?is_deb:PLATFORM=deb}
%if ! 0%{?without_rhn_register}
for g in data/*.glade ; do
        mv $g $g.old
        gtk-builder-convert $g.old $g
done
sed -i 's/GTK_PROGRESS_LEFT_TO_RIGHT/horizontal/' data/progress.glade
sed -i 's/GtkComboBox/GtkComboBoxText/; /property name="has_separator"/ d;' data/rh_register.glade
sed -i '/class="GtkVBox"/ {
                s/GtkVBox/GtkBox/;
                a \ \ \ \ \ \ \ \ <property name="orientation">vertical</property\>
                }' data/gui.glade
%endif
make -f Makefile.rhn-client-tools install VERSION=%{version}-%{release} \
        PYTHONPATH=%{python3_sitelib} PYTHONVERSION=%{python3_version} \
        PREFIX=$RPM_BUILD_ROOT MANPATH=%{_mandir} %{?is_deb:PLATFORM=deb}
%endif

ln -s spacewalk-channel $RPM_BUILD_ROOT%{_sbindir}/rhn-channel

mkdir -p $RPM_BUILD_ROOT/var/lib/up2date
mkdir -pm700 $RPM_BUILD_ROOT%{_localstatedir}/spool/up2date
touch $RPM_BUILD_ROOT%{_localstatedir}/spool/up2date/loginAuth.pkl
%if 0%{?fedora} || 0%{?mageia} || 0%{?debian} >= 8 || 0%{?ubuntu} >= 1504
mkdir -p $RPM_BUILD_ROOT/%{_presetdir}
install 50-spacewalk-client.preset $RPM_BUILD_ROOT/%{_presetdir}
%endif

%if 0%{?suse_version}
# zypp-plugin-spacewalk has its own action/errata.py
rm -f $RPM_BUILD_ROOT%{_datadir}/rhn/actions/errata.py*
%endif

%if 0%{?build_py2}
%if 0%{?fedora} || 0%{?rhel} > 5 || 0%{?suse_version} >= 1140 || 0%{?mageia} || 0%{?debian} || 0%{?ubuntu}
rm $RPM_BUILD_ROOT%{python_sitelib}/up2date_client/hardware_hal.*
%else
rm $RPM_BUILD_ROOT%{python_sitelib}/up2date_client/hardware_gudev.*
rm $RPM_BUILD_ROOT%{python_sitelib}/up2date_client/hardware_udev.*
%endif
%endif

%if 0%{?rhel} == 5
%if 0%{?build_py2}
rm -rf $RPM_BUILD_ROOT%{python_sitelib}/up2date_client/firstboot
%endif
rm -f $RPM_BUILD_ROOT%{_datadir}/firstboot/modules/rhn_register.*
%endif
%if 0%{?rhel} == 6
rm -rf $RPM_BUILD_ROOT%{_datadir}/firstboot/modules/rhn_*_*.*
%endif
%if ! 0%{?rhel} || 0%{?rhel} > 6
%if 0%{?build_py2}
rm -rf $RPM_BUILD_ROOT%{python_sitelib}/up2date_client/firstboot
%endif
rm -rf $RPM_BUILD_ROOT%{_datadir}/firstboot/
%endif
%if 0%{?build_py3}
rm -rf $RPM_BUILD_ROOT%{python3_sitelib}/up2date_client/firstboot
%endif

%if ! 0%{?without_rhn_register}
desktop-file-install --dir=${RPM_BUILD_ROOT}%{_datadir}/applications --vendor=rhn rhn_register.desktop
%if 0%{?suse_version}
%suse_update_desktop_file -r rhn_register "Settings;System;SystemSetup;"
# no usermod on SUSE
rm -f $RPM_BUILD_ROOT%{_bindir}/rhn_register
%endif
%endif

# create mgr_check symlink
ln -sf rhn_check $RPM_BUILD_ROOT/%{_sbindir}/mgr_check
ln -sf spacewalk-update-status $RPM_BUILD_ROOT/%{_sbindir}/mgr-update-status

# remove all unsupported translations
cd $RPM_BUILD_ROOT
for d in usr/share/locale/*; do
  if [ ! -d "/$d" ]; then
    rm -rfv "./$d"
  fi
done
cd -

%if %{_vendor} != "debbuild"
%find_lang rhn-client-tools
%endif

# create links to default script version
%define default_suffix %{?default_py3:-%{python3_version}}%{!?default_py3:-%{python_version}}
for i in \
    /usr/sbin/rhn-profile-sync \
    /usr/sbin/rhn_check \
    /usr/sbin/rhn_register \
    /usr/sbin/rhnreg_ks \
    /usr/sbin/spacewalk-channel \
; do
    ln -s $(basename "$i")%{default_suffix} "$RPM_BUILD_ROOT$i"
done

%if 0%{?without_rhn_register}
rm -rf $RPM_BUILD_ROOT/etc/pam.d
rm -rf $RPM_BUILD_ROOT/etc/security/console.apps
rm -rf $RPM_BUILD_ROOT/usr/share/setuptool
rm -f $RPM_BUILD_ROOT/usr/bin/rhn_register
rm -f $RPM_BUILD_ROOT/usr/sbin/rhn_register
rm -f $RPM_BUILD_ROOT/usr/share/man/man8/rhn_register.8.gz
#spacewalk-client-setup-gnome
rm -rf $RPM_BUILD_ROOT/%{_datadir}/firstboot
rm -rf $RPM_BUILD_ROOT/%{_datadir}/pixmaps
rm -rf $RPM_BUILD_ROOT/%{_datadir}/icons
rm -rf $RPM_BUILD_ROOT/%{python_sitelib}/up2date_client/firstboot

rm -f $RPM_BUILD_ROOT/%{python_sitelib}/up2date_client/messageWindow.*
rm -f $RPM_BUILD_ROOT/%{python_sitelib}/up2date_client/rhnregGui.*
rm -f $RPM_BUILD_ROOT/%{python_sitelib}/up2date_client/gtk_compat.*
rm -f $RPM_BUILD_ROOT/%{python_sitelib}/up2date_client/progress.*
rm -f $RPM_BUILD_ROOT/%{python_sitelib}/up2date_client/gui.*
rm -f $RPM_BUILD_ROOT/%{_datadir}/rhn/up2date_client/rh_register.glade
rm -f $RPM_BUILD_ROOT/%{_datadir}/rhn/up2date_client/gui.glade
rm -f $RPM_BUILD_ROOT/%{_datadir}/rhn/up2date_client/progress.glade
rm -f $RPM_BUILD_ROOT/%{_datadir}/man/man8/rhn_register.*
%if 0%{?build_py3}
rm -f $RPM_BUILD_ROOT/%{python3_sitelib}/up2date_client/messageWindow.*
rm -f $RPM_BUILD_ROOT/%{python3_sitelib}/up2date_client/rhnregGui.*
rm -f $RPM_BUILD_ROOT/%{python3_sitelib}/up2date_client/gtk_compat.*
rm -f $RPM_BUILD_ROOT/%{python3_sitelib}/up2date_client/progress.*
rm -f $RPM_BUILD_ROOT/%{python3_sitelib}/up2date_client/gui.*
%endif
%endif

%if 0%{?suse_version}
%py_compile -O %{buildroot}/%{python_sitelib}
%if 0%{?build_py3}
%py3_compile -O %{buildroot}/%{python3_sitelib}
%endif
%endif

%post
rm -f %{_localstatedir}/spool/up2date/loginAuth.pkl

%if ! 0%{?without_rhn_register}
%post -n spacewalk-client-setup-gnome
touch --no-create %{_datadir}/icons/hicolor &>/dev/null || :
# See posttrans section below
%if %{_vendor} == "debbuild"
gtk-update-icon-cache %{_datadir}/icons/hicolor &>/dev/null || :
%endif

%postun -n spacewalk-client-setup-gnome
%if %{_vendor} != "debbuild"
if [ $1 -eq 0 ] ; then
%endif
%if %{_vendor} == "debbuild"
if [[ "$1" == "purge" || "$1" == "remove" ]]; then
%endif
    touch --no-create %{_datadir}/icons/hicolor &>/dev/null
    gtk-update-icon-cache %{_datadir}/icons/hicolor &>/dev/null || :
fi

# This macro doesn't exist for debbuild. I'm shoving this into post instead.
%if %{_vendor} != "debbuild"
%posttrans -n spacewalk-client-setup-gnome
gtk-update-icon-cache %{_datadir}/icons/hicolor &>/dev/null || :
%endif
%endif

%if %{with test} && 0%{?fedora}
%check

make -f Makefile.rhn-client-tools test
%endif

%if %{_vendor} == "debbuild"
%files
# No find_lang on Debian systems
%{_datadir}/locale/
/var/lib/up2date/
%else
%files -f rhn-client-tools.lang
%endif
%defattr(-,root,root,-)
# some info about mirrors
%doc doc/mirrors.txt
%doc doc/AUTHORS
%doc doc/LICENSE
%{_mandir}/man8/rhn-profile-sync.8*
%{_mandir}/man5/up2date.5*

%dir %{_sysconfdir}/sysconfig/rhn
%dir %{_sysconfdir}/sysconfig/rhn/clientCaps.d
%dir %{_sysconfdir}/sysconfig/rhn/allowed-actions
%dir %{_sysconfdir}/sysconfig/rhn/allowed-actions/configfiles
%dir %{_sysconfdir}/sysconfig/rhn/allowed-actions/script
%verify(not md5 mtime size) %config(noreplace) %{_sysconfdir}/sysconfig/rhn/up2date
%config(noreplace) %{_sysconfdir}/logrotate.d/up2date

# dirs
%dir %{_datadir}/rhn
%dir %{_localstatedir}/spool/up2date

%{_sbindir}/rhn-profile-sync

%ghost %attr(600,root,root) %verify(not md5 size mtime) %{_localstatedir}/spool/up2date/loginAuth.pkl

#public keys and certificates
%{_datadir}/rhn/RHNS-CA-CERT

%if 0%{?fedora} || 0%{?mageia} || 0%{?debian} >= 8 || 0%{?ubuntu} >= 1504
%{_presetdir}/50-spacewalk-client.preset
%endif

%if 0%{?build_py2}
%files -n python2-%{name}
%defattr(-,root,root,-)
%{_sbindir}/rhn-profile-sync-%{python_version}
%dir %{python_sitelib}/up2date_client/
%{python_sitelib}/up2date_client/__init__.*
%{python_sitelib}/up2date_client/config.*
%{python_sitelib}/up2date_client/haltree.*
%{python_sitelib}/up2date_client/hardware*
%{python_sitelib}/up2date_client/up2dateUtils.*
%{python_sitelib}/up2date_client/up2dateLog.*
%{python_sitelib}/up2date_client/up2dateErrors.*
%{python_sitelib}/up2date_client/up2dateAuth.*
%{python_sitelib}/up2date_client/rpcServer.*
%{python_sitelib}/up2date_client/rhnserver.*
%{python_sitelib}/up2date_client/pkgUtils.*
%{python_sitelib}/up2date_client/rpmUtils.*
%{python_sitelib}/up2date_client/debUtils.*
%{python_sitelib}/up2date_client/rhnPackageInfo.*
%{python_sitelib}/up2date_client/rhnChannel.*
%{python_sitelib}/up2date_client/rhnHardware.*
%{python_sitelib}/up2date_client/transaction.*
%{python_sitelib}/up2date_client/clientCaps.*
%{python_sitelib}/up2date_client/capabilities.*
%{python_sitelib}/up2date_client/rhncli.*
%{python_sitelib}/up2date_client/pkgplatform.*
%endif

%if 0%{?build_py3}
%files -n python3-%{name}
%defattr(-,root,root,-)
%{_sbindir}/rhn-profile-sync-%{python3_version}
%dir %{python3_sitelib}/up2date_client/
%{python3_sitelib}/up2date_client/__init__.*
%{python3_sitelib}/up2date_client/config.*
%{python3_sitelib}/up2date_client/haltree.*
%{python3_sitelib}/up2date_client/hardware*
%{python3_sitelib}/up2date_client/up2dateUtils.*
%{python3_sitelib}/up2date_client/up2dateLog.*
%{python3_sitelib}/up2date_client/up2dateErrors.*
%{python3_sitelib}/up2date_client/up2dateAuth.*
%{python3_sitelib}/up2date_client/rpcServer.*
%{python3_sitelib}/up2date_client/rhnserver.*
%{python3_sitelib}/up2date_client/pkgUtils.*
%{python3_sitelib}/up2date_client/rpmUtils.*
%{python3_sitelib}/up2date_client/debUtils.*
%{python3_sitelib}/up2date_client/rhnPackageInfo.*
%{python3_sitelib}/up2date_client/rhnChannel.*
%{python3_sitelib}/up2date_client/rhnHardware.*
%{python3_sitelib}/up2date_client/transaction.*
%{python3_sitelib}/up2date_client/clientCaps.*
%{python3_sitelib}/up2date_client/capabilities.*
%{python3_sitelib}/up2date_client/rhncli.*
%{python3_sitelib}/up2date_client/pkgplatform.*

%if %{_vendor} != "debbuild"
%dir %{python3_sitelib}/up2date_client/__pycache__/
%{python3_sitelib}/up2date_client/__pycache__/__init__.*
%{python3_sitelib}/up2date_client/__pycache__/config.*
%{python3_sitelib}/up2date_client/__pycache__/haltree.*
%{python3_sitelib}/up2date_client/__pycache__/hardware*
%{python3_sitelib}/up2date_client/__pycache__/up2dateUtils.*
%{python3_sitelib}/up2date_client/__pycache__/up2dateLog.*
%{python3_sitelib}/up2date_client/__pycache__/up2dateErrors.*
%{python3_sitelib}/up2date_client/__pycache__/up2dateAuth.*
%{python3_sitelib}/up2date_client/__pycache__/rpcServer.*
%{python3_sitelib}/up2date_client/__pycache__/rhnserver.*
%{python3_sitelib}/up2date_client/__pycache__/pkgUtils.*
%{python3_sitelib}/up2date_client/__pycache__/rpmUtils.*
%{python3_sitelib}/up2date_client/__pycache__/debUtils.*
%{python3_sitelib}/up2date_client/__pycache__/rhnPackageInfo.*
%{python3_sitelib}/up2date_client/__pycache__/rhnChannel.*
%{python3_sitelib}/up2date_client/__pycache__/rhnHardware.*
%{python3_sitelib}/up2date_client/__pycache__/transaction.*
%{python3_sitelib}/up2date_client/__pycache__/clientCaps.*
%{python3_sitelib}/up2date_client/__pycache__/capabilities.*
%{python3_sitelib}/up2date_client/__pycache__/rhncli.*
%{python3_sitelib}/up2date_client/__pycache__/pkgplatform.*
%endif
%endif

%files -n spacewalk-check
%defattr(-,root,root,-)
%{_mandir}/man8/rhn_check.8*
%{_sbindir}/rhn_check
%{_sbindir}/mgr_check
%{_sbindir}/spacewalk-update-status
%{_sbindir}/mgr-update-status

%if 0%{?build_py2}
%files -n python2-spacewalk-check
%defattr(-,root,root,-)
%{_sbindir}/rhn_check-%{python_version}
%dir %{python_sitelib}/rhn
%dir %{python_sitelib}/rhn/actions
%{python_sitelib}/up2date_client/getMethod.*
# actions for rhn_check to run
%{python_sitelib}/rhn/actions/__init__.*
%{python_sitelib}/rhn/actions/hardware.*
%{python_sitelib}/rhn/actions/systemid.*
%{python_sitelib}/rhn/actions/reboot.*
%{python_sitelib}/rhn/actions/rhnsd.*
%{python_sitelib}/rhn/actions/up2date_config.*
%endif

%if 0%{?build_py3}
%files -n python3-spacewalk-check
%defattr(-,root,root,-)
%{_sbindir}/rhn_check-%{python3_version}
%dir %{python3_sitelib}/rhn
%dir %{python3_sitelib}/rhn/actions
%{python3_sitelib}/up2date_client/getMethod.*
%{python3_sitelib}/rhn/actions/__init__.*
%{python3_sitelib}/rhn/actions/hardware.*
%{python3_sitelib}/rhn/actions/systemid.*
%{python3_sitelib}/rhn/actions/reboot.*
%{python3_sitelib}/rhn/actions/rhnsd.*
%{python3_sitelib}/rhn/actions/up2date_config.*

%if %{_vendor} != "debbuild"
%dir %{python3_sitelib}/rhn/actions/__pycache__/
%{python3_sitelib}/up2date_client/__pycache__/getMethod.*
%{python3_sitelib}/rhn/actions/__pycache__/__init__.*
%{python3_sitelib}/rhn/actions/__pycache__/hardware.*
%{python3_sitelib}/rhn/actions/__pycache__/systemid.*
%{python3_sitelib}/rhn/actions/__pycache__/reboot.*
%{python3_sitelib}/rhn/actions/__pycache__/rhnsd.*
%{python3_sitelib}/rhn/actions/__pycache__/up2date_config.*
%endif
%endif

%files -n spacewalk-client-setup
%defattr(-,root,root,-)
%{_mandir}/man8/rhnreg_ks.8*
%{_mandir}/man8/spacewalk-channel.8*
%{_mandir}/man8/rhn-channel.8*

%{_sbindir}/rhnreg_ks
%{_sbindir}/spacewalk-channel
%{_sbindir}/rhn-channel

%if ! 0%{?without_rhn_register}
%{_mandir}/man8/rhn_register.8*
%config(noreplace) %{_sysconfdir}/security/console.apps/rhn_register
%config(noreplace) %{_sysconfdir}/pam.d/rhn_register
%if 0%{?fedora} || 0%{?rhel}
%{_bindir}/rhn_register
%endif
%{_sbindir}/rhn_register
%{_datadir}/setuptool/setuptool.d/99rhn_register

%if 0%{?suse_version}
# on SUSE directories not owned by any package
%dir %{_sysconfdir}/security/console.apps
%dir %{_datadir}/setuptool
%dir %{_datadir}/setuptool/setuptool.d
%endif
%endif

%if 0%{?build_py2}
%files -n python2-spacewalk-client-setup
%defattr(-,root,root,-)
%{_sbindir}/rhn_register-%{python_version}
%{_sbindir}/rhnreg_ks-%{python_version}
%{_sbindir}/spacewalk-channel-%{python_version}
%{python2_sitelib}/up2date_client/rhnreg.*
%{python2_sitelib}/up2date_client/pmPlugin.*
%{python2_sitelib}/up2date_client/tui.*
%{python2_sitelib}/up2date_client/rhnreg_constants.*
%endif

%if 0%{?build_py3}
%files -n python3-spacewalk-client-setup
%defattr(-,root,root,-)
%{_sbindir}/rhn_register-%{python3_version}
%{_sbindir}/rhnreg_ks-%{python3_version}
%{_sbindir}/spacewalk-channel-%{python3_version}
%{python3_sitelib}/up2date_client/rhnreg.*
%{python3_sitelib}/up2date_client/pmPlugin.*
%{python3_sitelib}/up2date_client/tui.*
%{python3_sitelib}/up2date_client/rhnreg_constants.*

%if %{_vendor} != "debbuild"
%{python3_sitelib}/up2date_client/__pycache__/rhnreg.*
%{python3_sitelib}/up2date_client/__pycache__/pmPlugin.*
%{python3_sitelib}/up2date_client/__pycache__/tui.*
%{python3_sitelib}/up2date_client/__pycache__/rhnreg_constants.*
%endif
%endif

%if ! 0%{?without_rhn_register}
%files -n spacewalk-client-setup-gnome
%defattr(-,root,root,-)
%{_datadir}/pixmaps/*png
%{_datadir}/icons/hicolor/16x16/apps/up2date.png
%{_datadir}/icons/hicolor/24x24/apps/up2date.png
%{_datadir}/icons/hicolor/32x32/apps/up2date.png
%{_datadir}/icons/hicolor/48x48/apps/up2date.png
%if 0%{?rhel} > 6 || 0%{?fedora}
%{_datadir}/icons/hicolor/22x22/apps/up2date.png
%{_datadir}/icons/hicolor/256x256/apps/up2date.png
%endif
%{_datadir}/applications/rhn_register.desktop
%{_datadir}/rhn/up2date_client/gui.glade
%{_datadir}/rhn/up2date_client/progress.glade
%{_datadir}/rhn/up2date_client/rh_register.glade

%if 0%{?suse_version}
# on SUSE these directories are part of packages not installed
# at buildtime. OBS failed with not owned by any package
%dir %{_datadir}/icons/hicolor
%dir %{_datadir}/icons/hicolor/16x16
%dir %{_datadir}/icons/hicolor/16x16/apps
%dir %{_datadir}/icons/hicolor/24x24
%dir %{_datadir}/icons/hicolor/24x24/apps
%dir %{_datadir}/icons/hicolor/32x32
%dir %{_datadir}/icons/hicolor/32x32/apps
%dir %{_datadir}/icons/hicolor/48x48
%dir %{_datadir}/icons/hicolor/48x48/apps
%dir %{_datadir}/firstboot
%dir %{_datadir}/firstboot/modules
%endif

%if 0%{?build_py2}
%files -n python2-spacewalk-client-setup-gnome
%defattr(-,root,root,-)
%{python_sitelib}/up2date_client/messageWindow.*
%{python_sitelib}/up2date_client/rhnregGui.*
%{python_sitelib}/up2date_client/gtk_compat.*
%{python_sitelib}/up2date_client/gui.*
%{python_sitelib}/up2date_client/progress.*
%if 0%{?rhel} == 5
%{_datadir}/firstboot/modules/rhn_login_gui.*
%{_datadir}/firstboot/modules/rhn_choose_channel.*
%{_datadir}/firstboot/modules/rhn_register_firstboot_gui_window.*
%{_datadir}/firstboot/modules/rhn_start_gui.*
%{_datadir}/firstboot/modules/rhn_choose_server_gui.*
%{_datadir}/firstboot/modules/rhn_provide_certificate_gui.*
%{_datadir}/firstboot/modules/rhn_create_profile_gui.*
%{_datadir}/firstboot/modules/rhn_review_gui.*
%{_datadir}/firstboot/modules/rhn_finish_gui.*
%else
%if 0%{?rhel} == 6
%{_datadir}/firstboot/modules/rhn_register.*
%{python_sitelib}/up2date_client/firstboot/rhn_login_gui.*
%{python_sitelib}/up2date_client/firstboot/rhn_start_gui.*
%{python_sitelib}/up2date_client/firstboot/rhn_choose_server_gui.*
%{python_sitelib}/up2date_client/firstboot/rhn_choose_channel.*
%{python_sitelib}/up2date_client/firstboot/rhn_provide_certificate_gui.*
%{python_sitelib}/up2date_client/firstboot/rhn_create_profile_gui.*
%{python_sitelib}/up2date_client/firstboot/rhn_review_gui.*
%{python_sitelib}/up2date_client/firstboot/rhn_finish_gui.*
%endif # 0%{?rhel} == 6
%endif # 0%{?rhel} == 5
%endif # 0%{?build_py2}

%if 0%{?build_py3}
%files -n python3-spacewalk-client-setup-gnome
%defattr(-,root,root,-)
%{python3_sitelib}/up2date_client/messageWindow.*
%{python3_sitelib}/up2date_client/rhnregGui.*
%{python3_sitelib}/up2date_client/gtk_compat.*
%{python3_sitelib}/up2date_client/gui.*
%{python3_sitelib}/up2date_client/progress.*

%if %{_vendor} != "debbuild"
%{python3_sitelib}/up2date_client/__pycache__/messageWindow.*
%{python3_sitelib}/up2date_client/__pycache__/rhnregGui.*
%{python3_sitelib}/up2date_client/__pycache__/gtk_compat.*
%{python3_sitelib}/up2date_client/__pycache__/gui.*
%{python3_sitelib}/up2date_client/__pycache__/progress.*
%endif # %{_vendor} != "debbuild"
%endif # 0%{?build_py3}
%endif # ! 0%{?without_rhn_register}

%if %{_vendor} == "debbuild"

%if 0%{?build_py2}
%post -n python2-%{name}
# Do late-stage bytecompilation, per debian policy
pycompile -p python2-%{name} -V -3.0

%preun -n python2-%{name}
# Ensure all *.py[co] files are deleted, per debian policy
pyclean -p python2-%{name}

%post -n python2-rhn-check
# Do late-stage bytecompilation, per debian policy
pycompile -p python2-rhn-check -V -3.0

%preun -n python2-rhn-check
# Ensure all *.py[co] files are deleted, per debian policy
pyclean -p python2-rhn-check

%post -n python2-rhn-setup
# Do late-stage bytecompilation, per debian policy
pycompile -p python2-rhn-setup -V -3.0

%preun -n python2-rhn-setup
# Ensure all *.py[co] files are deleted, per debian policy
pyclean -p python2-rhn-setup

%post -n python2-rhn-setup-gnome
# Do late-stage bytecompilation, per debian policy
pycompile -p python2-rhn-setup-gnome -V -3.0

%preun -n python2-rhn-setup-gnome
# Ensure all *.py[co] files are deleted, per debian policy
pyclean -p python2-rhn-setup-gnome
%endif

%if 0%{?build_py3}
%post -n python3-%{name}
# Do late-stage bytecompilation, per debian policy
py3compile -p python3-%{name} -V -4.0

%preun -n python3-%{name}
# Ensure all *.py[co] files are deleted, per debian policy
py3clean -p python3-%{name}

%post -n python3-rhn-check
# Do late-stage bytecompilation, per debian policy
py3compile -p python3-rhn-check -V -4.0

%preun -n python3-rhn-check
# Ensure all *.py[co] files are deleted, per debian policy
py3clean -p python3-rhn-check

%post -n python3-rhn-setup
# Do late-stage bytecompilation, per debian policy
py3compile -p python3-rhn-setup -V -4.0

%preun -n python3-rhn-setup
# Ensure all *.py[co] files are deleted, per debian policy
py3clean -p python3-rhn-setup

%post -n python3-rhn-setup-gnome
# Do late-stage bytecompilation, per debian policy
py3compile -p python3-rhn-setup-gnome -V -4.0

%preun -n python3-rhn-setup-gnome
# Ensure all *.py[co] files are deleted, per debian policy
py3clean -p python3-rhn-setup-gnome
%endif
%endif


%changelog
