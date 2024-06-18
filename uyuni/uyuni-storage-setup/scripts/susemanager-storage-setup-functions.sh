#!/bin/bash

# Copyright (c) 2019--2024 SUSE Linux GmbH
# Copyright (c) 2024 SUSE LLC
#
# This file is part of uyuni-storage-setup.
#
# uyuni-storage-setup is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; version 3 of the License
#
# uyuni-storage-setup is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program

info() {
    echo "--> $@"
}

die() {
    echo "Error: $@" >&2
    exit 1
}

linux_device() {
    test -z "$1" && die "linux_device called without argument"
    local device=$(readlink $1 2>/dev/null)
    if [ -z "$device" ];then
        echo $1
        return
    fi
    device=$(basename $device)
    device=/dev/$device
    echo $device
}

check_content_signature() {
    test -z "$1" && die "check_content_signature called without argument"
    local device=$1
    local signature=$(blkid $device -s TYPE -o value 2>/dev/null)
    if [ ! -z "$signature" ];then
        die "Found filesystem signature $signature on $device"
    fi
}

check_device_empty() {
    test -z "$1" && die "check_device_empty called without argument"
    local device=$(linux_device $1)
    local part=$(get_first_partition_device $1)
    if [ -b "$part" ]; then
        check_content_signature $part
    fi
}

is_btrfs_subvolume() {
    test -z "$1" && die "is_btrfs_subvolume called without argument"
    local mountpoint=${1%/}
    if grep -qi "subvol=@$mountpoint" /etc/fstab; then
        return 0
    fi
    return 1
}



check_mountpoint() {
    test -z "$1" && die "check_mountpoint called without argument"
    local mount_point=${1%/}
    [[ $mount_point == /* ]] || die "Mountpoint $mount_point is not an absolute path"
    if grep -qi "$mount_point" /etc/fstab; then
        die "$mount_point already in /etc/fstab"
    fi
    # check if mount_point exists
    if [ ! -d $mount_point ]; then
        mkdir $mount_point || die "Unable to create $mount_point"
	chmod 0700 $mount_point
	if /usr/sbin/selinuxenabled; then
            chcon --reference=$mount_point/.. $mount_point || die "Unable to set selinux context to $mount_point"
	fi
    fi
}

get_first_partition_device() {
    test -z "$1" && die "get_first_partition_device called without argument"
    local device=$1
    # In EC2 on newer instance types attached devices are NVMe based and we
    # get a partition entry
    local partition=${device}p1
    if [ -e $partition ]; then
        echo $partition
        return
    fi
    # If we are not on NVMe it just shows up as a scsi device
    echo ${device}1
}

create_partition() {
    test -z "$1" && die "create_parition called without argument"
    local disk=$1
    local result=$(parted -s $disk mklabel GPT 2>&1)
    if [ $? != 0 ]; then
        die "Creating new GPT label failed: $result"
    fi
    local result=$(parted -s $disk mkpart primary 2048s 100% 2>&1)
    if [ $? != 0 ]; then
        die "Partition setup failed: $result"
    fi
    rm -f $cmd_sequence
}

create_filesystem() {
    test -z "$2" && die "create_filesystem called without arguments"
    local part=$(get_first_partition_device $1)
    local fs=$2
    local tool=mkfs.$fs
    result=$($tool -f $part 2>&1)
    if [ $? != 0 ]; then
        die "$fs filesystem setup failed: $result"
    fi
}

update_fstab() {
    test -z "$2" && die "update_fstab called without arguments"
    local part=$(get_first_partition_device $1)
    local mount_point=$2
    local uuid=$(blkid -s UUID -o value $part)
    if [ -z "$uuid" ]; then
        die "could not determine UUID of $part"
    fi
    if grep -qi "$uuid" /etc/fstab; then
        die "$uuid already added to fstab"
    fi
    local fs=$(blkid -s TYPE $part -o value)
    echo "UUID=$uuid $mount_point $fs defaults,nofail 1 2" >> /etc/fstab
}

mount_storage() {
    test -z "$2" && die "mount_storage called without arguments"
    local part=$(get_first_partition_device $1)
    local mount_point=$2
    mkdir -p $mount_point
    chmod 0700 $mount_point
    if /usr/sbin/selinuxenabled; then
        chcon --reference=$mount_point/.. $mount_point || die "Unable to set selinux context to $mount_point"
    fi
    local result=$(mount $part $mount_point 2>&1)
    if [ $? != 0 ]; then
        die "Mounting $part failed with $result"
    fi
}

umount_subvolume() {
    test -z "$1" && die "umount_subvolume called without arguments"
    local mount_point=$1
    if ! is_btrfs_subvolume $mount_point ; then
        die "$mount_point is not a btrfs subvolume"
    fi
    local result=$(umount $mount_point 2>&1)
    if [ $? != 0 ]; then
        die "Unmounting $mount_point failed with $result"
    fi
    cp /etc/fstab /etc/fstab.$(date +"%Y%m%d%H%M%S")
    sed -i 's|.*subvol=@/var/lib/containers/storage/volumes/\?[[:space:],]\+.*||' /etc/fstab
}

remount_storage() {
    test -z "$3" && die "remount_storage called without arguments"
    local device=$1
    local tmp_mount_point=$2
    local mount_point=$3
    local result=$(umount $tmp_mount_point)
    if [ $? != 0 ]; then
        die "Umount $tmp_mount_point failed"
    fi
    if [ ! -d $mount_point ]; then
        mkdir -p $mount_point
        chmod 0700 $mount_point
        if /usr/sbin/selinuxenabled; then
            chcon --reference=$mount_point/.. $mount_point || die "Unable to set selinux context to $mount_point"
        fi
    fi
    mount_storage $device $mount_point
    rmdir $tmp_mount_point
}

move_storage() {
    test -z "$2" && die "move_storage called without arguments"
    source="$1"
    destination="$2"
    result=$(rsync -aXA $source $destination 2>&1)
    if [ $? != 0 ]; then
        die "Syncing $source failed with: $result"
    fi
    dname=$(basename $source)
    rm -rf $source/*
}
