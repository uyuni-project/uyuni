#!/bin/bash

# called by dracut
check() {
    return 0
}

# called by dracut
depends() {
    echo network
    return 0
}

get_python_pkg_deps() {
    rpm -q --requires "$@" | while read req ver; do
        if [[ $req == rpmlib* ]]; then
          continue
        fi
        rpm -q --whatprovides "$req"
    done | grep -v 'no package provides'| sort -u
}

get_python_pkg_deps_recursive() {
    # get rpm with version
    for n in "$@"; do
      v=$(rpm -q "$n")
      deps=$(echo -e "$deps\n$v")
    done
    input="$deps"
    res=""
    while [ -n "$deps" ] ; do
       freeze="$deps"
       deps=""
       for d in $freeze; do
          if [[ $res == *$d* ]]; then
            # we got already the dependencies of this
            continue
          fi
          d2=$(get_python_pkg_deps $d)
          # we have dependencies of $d, store it in $res
          res=$(echo -e "$d\n$res" |sort -u)
          # add dependencies of $d2 to next round of deps to test
          deps=$(echo -e "$deps\n$d2" | sort -u)
       done
    done
    # be sure that original input is part of the list
    res=$(echo -e "$res\n$input" |sort -u)
    echo $res
}

# called by dracut
install() {
    RELPKG=$(rpm -q --whatprovides --qf "%{name}\n" distribution-release)

    inst_multiple -o $(rpm -ql $(get_python_pkg_deps_recursive spacewalk-client-setup spacewalk-client-tools python3-spacewalk-client-tools python3-spacewalk-client-setup python3-zypp-plugin-spacewalk wget rpm $RELPKG) | \
                  grep -v '\.pyc$\|/etc/salt/minion_id\|/usr/share/locale/\|/usr/share/doc/\|/usr/share/man' )
    inst_multiple -o /usr/lib64/libffi.so.7 # dracut dependency solver does not see this
    inst_multiple -o grep dig ldconfig date dbus-uuidgen systemd-machine-id-setup dmidecode seq parted \
                     lsblk partprobe mdadm dcounter mkswap curl head md5sum resize2fs mkfs mkfs.btrfs \
                     mkfs.ext2 mkfs.ext3 mkfs.ext4 mkfs.fat mkfs.vfat mkfs.xfs sync cryptsetup busybox \
                     swapon rpm
    inst_multiple -o $(find /var/lib/rpm/ -type f)

    inst "/etc/rc.status"

    inst_hook cmdline 91 "$moddir/mgrbootstrap-root.sh"
    inst_hook pre-mount 99 "$moddir/mgrbootstrap.sh"

    echo "rd.neednet=1" > "${initdir}/etc/cmdline.d/50mgrbootstrap.conf"
}

