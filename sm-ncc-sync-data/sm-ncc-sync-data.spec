Name:           sm-ncc-sync-data
Version:        1.7.3
Release:        1%{?dist}
Summary:        SUSE Manager specific scripts
Group:          Productivity/Other
License:        GPLv2
URL:            http://www.novell.com
Source0:        %{name}-%{version}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch:      noarch

%description 
This package contains data files with NCC information

%prep
%setup -q

%build

%install

mkdir -p %{buildroot}/usr/share/susemanager
mkdir -p %{buildroot}/srv/www/htdocs/pub/
install -m 0644 channel_families.xml %{buildroot}/usr/share/susemanager/channel_families.xml
install -m 0644 channels.xml         %{buildroot}/usr/share/susemanager/channels.xml
install -m 0644 res.key              %{buildroot}/srv/www/htdocs/pub/
install -m 0644 suse-307E3D54.key    %{buildroot}/srv/www/htdocs/pub/
install -m 0644 suse-9C800ACA.key    %{buildroot}/srv/www/htdocs/pub/

%clean
rm -rf %{buildroot}

%post
if [ -f /lvmboot/boot/grub/menu.lst ]; then
  if /bin/grep "/lvmboot/boot[[:space:]]\+/boot[[:space:]]\+.*bind" /etc/fstab >/dev/null; then
    /bin/umount /boot
    /bin/mv /lvmboot/boot /lvmboot/tmp
    /bin/mv /lvmboot/tmp/* /lvmboot
    /bin/rmdir /lvmboot/tmp
    /bin/umount /lvmboot
    /bin/rmdir /lvmboot
    /bin/cp /etc/fstab /etc/fstab.bak
    /usr/bin/sed -i 's|/lvmboot/boot.*bind.*||' /etc/fstab
    /usr/bin/sed -i 's|[[:space:]]/lvmboot[[:space:]]\+ext2| /boot ext2|' /etc/fstab
    /bin/mount /boot
    if [ -f /boot/grub/menu.lst ]; then
      /bin/cp /boot/grub/menu.lst /boot/grub/menu.lst.bak
      /usr/bin/sed -i 's|/boot/vmlinuz|/vmlinuz|' /boot/grub/menu.lst
      /usr/bin/sed -i 's|/boot/initrd|/initrd|' /boot/grub/menu.lst
      /usr/bin/sed -i 's|/boot/message|/message|' /boot/grub/menu.lst
    fi
  fi
fi
exit 0

%files
%defattr(-,root,root,-)
%dir /usr/share/susemanager
%dir /srv/www/htdocs/pub
/usr/share/susemanager/channel_families.xml
/usr/share/susemanager/channels.xml
/srv/www/htdocs/pub/*.key

%changelog

