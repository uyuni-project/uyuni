%define config_dir     %{_var}/lib/nocpulse/trapReceiver
Name:         SatConfig-installer
Summary:      Satellite Configuration System - command line installer
URL:          https://fedorahosted.org/spacewalk
Source0:      https://fedorahosted.org/releases/s/p/spacewalk/%{name}-%{version}.tar.gz
Version:      3.24.7.1
Release:      1%{?dist}
BuildArch:    noarch
Requires:     perl(:MODULE_COMPAT_%(eval "`%{__perl} -V:version`"; echo $version))
Group:        Applications/System
License:      GPLv2
Buildroot:    %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
Requires(pre): nocpulse-common
%if 0%{?suse_version}
Requires:      perl(MIME::Parser)
%endif

%description
SatConfig-installer is the command line mechanism by which Netsaint
configuration files are installed and used by Netsaint on satellite boxes.
The program must be run on the Spacewalk.  

%prep
%setup -q

%build
#Nothing to build

%install
rm -rf $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT%{_bindir}
mkdir -p $RPM_BUILD_ROOT%config_dir

install -m 755 scheduleEvents $RPM_BUILD_ROOT%{_bindir}
install -m 755 validateCurrentStateFiles.pl $RPM_BUILD_ROOT%{_bindir}

# only for update because of a moved home
%if ! 0%{?suse_version}
%post
if [ $1 -eq 2 ]; then
  ls /home/nocpulse/var/trapReceiver/* 2>/dev/null | xargs -I file mv file %config_dir
fi
%endif

%files
%attr(755,nocpulse,nocpulse) %dir %config_dir
%{_bindir}/scheduleEvents
%{_bindir}/validateCurrentStateFiles.pl

%clean
rm -rf $RPM_BUILD_ROOT

%changelog
* Mon May 11 2009 Milan Zazriec <mzazrivec@redhat.com> 3.24.6-1
- 498257 - migrate existing files into new nocpulse homedir

* Thu Apr 23 2009 jesus m. rodriguez <jesusr@redhat.com> 3.24.5-1
- change Source0 to point to fedorahosted.org (msuchy@redhat.com)

* Tue Nov 25 2008 Miroslav Suchý <msuchy@redhat.com> 3.24.4-1
- fix missing semicolon 

* Tue Oct 21 2008 Miroslav Suchý <msuchy@redhat.com> 3.24.3-1
- 467441 - fix requires

* Mon Oct 20 2008 Miroslav Suchý <msuchy@redhat.com> 3.24.2-1
- 467441 - fix namespace

* Wed Sep 24 2008 Miroslav Suchý <msuchy@redhat.com> 3.24.1-1
- spec cleanup for Fedora

* Thu Jun 19 2008 Miroslav Suchy <msuchy@redhat.com>
- migrating nocpulse home dir (BZ 202614)
