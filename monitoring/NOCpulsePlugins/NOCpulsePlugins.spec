Name:         NOCpulsePlugins
Version:      2.209.7.1
Release:      1%{?dist}
Summary:      NOCpulse authored Plug-ins
URL:          https://fedorahosted.org/spacewalk
Source0:      https://fedorahosted.org/releases/s/p/spacewalk/%{name}-%{version}.tar.gz
BuildArch:    noarch
Requires:     perl(:MODULE_COMPAT_%(eval "`%{__perl} -V:version`"; echo $version))
Requires:     nocpulse-common
Group:        Development/Libraries
License:      GPLv2
Buildroot:    %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

%description
NOCpulse provides application, network, systems and transaction monitoring,
coupled with a comprehensive reporting system including availability,
historical and trending reports in an easy-to-use browser interface.

This package contain NOCpulse authored plug-ins for probes.

%package Oracle
Summary:      NOCpulse plug-ins for Oracle
Group:        Development/Libraries
Requires:     %{name} = %{version}

%description Oracle
NOCpulse provides application, network, systems and transaction monitoring,
coupled with a comprehensive reporting system including availability,
historical and trending reports in an easy-to-use browser interface.

This package contain NOCpulse authored plug-ins for Oracle probes.

%prep
%setup -q

%build
# Nothing to build

%install
rm -rf $RPM_BUILD_ROOT

mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/nocpulse
mkdir -p $RPM_BUILD_ROOT%{_bindir}
mkdir -p $RPM_BUILD_ROOT%{_var}/lib/nocpulse/libexec
mkdir -p $RPM_BUILD_ROOT%{_var}/lib/nocpulse/ProbeState

install -p -m 644 *.ini   $RPM_BUILD_ROOT%{_sysconfdir}/nocpulse
install -p -m 644 *.pm    $RPM_BUILD_ROOT%{_var}/lib/nocpulse/libexec
install -p -m 755 rhn-probe-status $RPM_BUILD_ROOT%{_bindir}
install -p -m 755 rhn-catalog $RPM_BUILD_ROOT%{_bindir}
install -p -m 755 setTrending $RPM_BUILD_ROOT%{_bindir}

for pkg in Apache Apache/test General LogAgent MySQL NetworkService Oracle Oracle/test Satellite Unix Unix/test Weblogic 
do
  fulldir=$RPM_BUILD_ROOT%{_var}/lib/nocpulse/libexec/$pkg
  mkdir -p  $fulldir
  install -p -m 644 $pkg/*.pm $fulldir
done

# was only needed for the update case because of a moved home
%if ! 0%{?suse_version}
%post
if [ $1 -eq 2 ]; then
  ls /home/nocpulse/var/ProbeState/* 2>/dev/null | xargs -I file mv file %{_var}/lib/nocpulse/ProbeState
fi
%endif

%files
%dir %{_sysconfdir}/nocpulse
%dir %attr(-, nocpulse,nocpulse) %{_var}/lib/nocpulse
%attr(-,nocpulse,nocpulse) %dir %{_var}/lib/nocpulse/ProbeState
%dir %attr(-, nocpulse,nocpulse) %{_var}/lib/nocpulse/libexec
%{_var}/lib/nocpulse/libexec/Apache*
%{_var}/lib/nocpulse/libexec/General*
%{_var}/lib/nocpulse/libexec/LogAgent*
%{_var}/lib/nocpulse/libexec/MySQL*
%{_var}/lib/nocpulse/libexec/NetworkService*
%{_var}/lib/nocpulse/libexec/Satellite*
%{_var}/lib/nocpulse/libexec/Unix*
%{_var}/lib/nocpulse/libexec/Weblogic*
%{_var}/lib/nocpulse/libexec/*.pm
%config %{_sysconfdir}/nocpulse/*
%{_bindir}/*

%files Oracle
%{_var}/lib/nocpulse/libexec/Oracle*

%clean
rm -rf $RPM_BUILD_ROOT

%changelog
* Tue Sep 17 2013 Michael Mraka <michael.mraka@redhat.com> 2.209.7-1
- Grammar error occurred
- Purging %%changelog entries preceding Spacewalk 1.0, in active packages.

* Wed Aug 01 2012 Jan Pazdziora 2.209.6-1
- 844992 - force the array context so that Class::MethodMaker behaves the same
  in both versions 1 and 2.
- %%defattr is not needed since rpm 4.4

* Tue Apr 03 2012 Jan Pazdziora 2.209.5-1
- 518985 - fix ORA-00918: column ambiguously defined (mzazrivec@redhat.com)

* Tue Mar 13 2012 Michael Mraka <michael.mraka@redhat.com> 2.209.4-1
- fixed error: %%changelog entries must start with *

* Thu Aug 11 2011 Jan Pazdziora 2.209.3-1
- Add additional member name mappings in ProbeRecord to ensure that the probe
  will run when rhn-runprobe is called. (davidn@elrond.bioss.sari.ac.uk)

* Fri Feb 18 2011 Jan Pazdziora 2.209.2-1
- Localize the filehandle globs; also use three-parameter opens.

* Mon Jul 12 2010 Miroslav Suchý <msuchy@redhat.com> 2.209.1-1
- rename status to rhn-probe-status (msuchy@redhat.com)
- rename catalog to rhn-catalog (msuchy@redhat.com)
- fix spelling error (msuchy@redhat.com)
- fix rpmlint warning (msuchy@redhat.com)
- fix spelling error (msuchy@redhat.com)
- put status and catalog to /usr/bin rather then to /var/lib/nocpulse/libexec
  and setting up symlink (msuchy@redhat.com)
- preserve timestamp and set correct attributes for files (msuchy@redhat.com)
- split package NOCpulsePlugins to NOCpulsePlugins-Oracle, which contain Oracle
  only probes (msuchy@redhat.com)

