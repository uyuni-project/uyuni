%define startup_root   %{_sysconfdir}/rc.d
%define queue_dir      %{_var}/lib/nocpulse/queue
%define notif_qdir     %queue_dir/notif
%define states_qdir    %queue_dir/sc_db
%define trends_qdir    %queue_dir/ts_db
%define commands_qdir  %queue_dir/commands
%define snmp_qdir      %queue_dir/snmp

Name:         MessageQueue
Version:      3.26.9.1
Release:      1%{?dist}
Summary:      Message buffer/relay system
URL:          https://fedorahosted.org/spacewalk
Source0:      https://fedorahosted.org/releases/s/p/spacewalk/%{name}-%{version}.tar.gz
BuildArch:    noarch
%if 0%{?suse_version}
BuildRequires: nocpulse-common
%else
Requires:     perl(:MODULE_COMPAT_%(eval "`%{__perl} -V:version`"; echo $version))
%endif
Requires:     ProgAGoGo nocpulse-common
Group:        Applications/Communications
License:      GPLv2
Buildroot:    %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

%description
MessageQueue is a mechanism by which Spacewalk plugins and event handlers
can safely and quickly buffer outbound messages. The system provides
a dequeue daemon that reliably dequeues messages to internal systems.

%prep
%setup -q

%build
#Nothing to build

%install
rm -rf $RPM_BUILD_ROOT

mkdir -p $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse
mkdir -p $RPM_BUILD_ROOT%{_bindir}
mkdir -p $RPM_BUILD_ROOT%notif_qdir
mkdir -p $RPM_BUILD_ROOT%states_qdir
mkdir -p $RPM_BUILD_ROOT%trends_qdir
mkdir -p $RPM_BUILD_ROOT%commands_qdir
mkdir -p $RPM_BUILD_ROOT%snmp_qdir

# Install libraries
install	-m 644 *.pm $RPM_BUILD_ROOT%{perl_vendorlib}/NOCpulse/

# Install binaries
install -m 755 dequeue $RPM_BUILD_ROOT%{_bindir}

# stuff needing special ownership doesn't go in filelist
install -m 755 queuetool $RPM_BUILD_ROOT%{_bindir}

%post
if [ $1 -eq 2 ]; then
  ls /home/nocpulse/var/queue/commands/* 2>/dev/null | xargs -I file mv file %commands_qdir
  ls /home/nocpulse/var/queue/notif/* 2>/dev/null | xargs -I file mv file %notif_qdir
  ls /home/nocpulse/var/queue/sc_db/* 2>/dev/null | xargs -I file mv file %states_qdir
  ls /home/nocpulse/var/queue/snmp/* 2>/dev/null | xargs -I file mv file %snmp_qdir
  ls /home/nocpulse/var/queue/ts_db/* 2>/dev/null | xargs -I file mv file %trends_qdir
fi

%files
%defattr(-,root,root)
%attr(755,nocpulse,nocpulse) %dir %queue_dir
%attr(755,nocpulse,nocpulse) %dir %states_qdir
%attr(755,nocpulse,nocpulse) %dir %notif_qdir
%attr(755,nocpulse,nocpulse) %dir %trends_qdir
%attr(755,nocpulse,nocpulse) %dir %commands_qdir
%attr(755,nocpulse,nocpulse) %dir %snmp_qdir
%{_bindir}/queuetool
%{_bindir}/dequeue
%{perl_vendorlib}/NOCpulse/*

%clean
rm -rf $RPM_BUILD_ROOT

%changelog
* Wed Jun 12 2013 Tomas Kasparek <tkasparek@redhat.com> 3.26.9-1
- rebrading RHN Satellite to Red Hat Satellite

* Mon Apr 22 2013 Jan Pazdziora 3.26.8-1
- If the host lookup fails, do not hide the error.

* Fri Feb 03 2012 Miroslav Suchý 3.26.7-1
- If $url is not set, fail with sane message. (msuchy@redhat.com)

* Mon Dec 19 2011 Michael Mraka <michael.mraka@redhat.com> 3.26.6-1
- 768188 - return mac address of the first available interface

* Fri Feb 18 2011 Jan Pazdziora 3.26.5-1
- Localize the filehandle globs; also use three-parameter opens.

