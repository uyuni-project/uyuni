# SLEnkins Spacewalk Suite HOWTO:

#### Official and complete documentation is here:
http://slenkins.suse.de/doc/
  


### HOWTO TOPICS:

*  1) Installation of SLEnkins in your machine.
* How to run the Spacewalk Suite on you local machine
* How to run it on Cloud6. 
* Developing a new/existing testsuite.

1) Installation of SLEnkins in your local machine/server.

**1-Install the packages*
```
openSUSE_Leap_42.1 modify it with your distro
zypper ar http://download.suse.de/ibs/Devel:/SLEnkins/openSUSE_Leap_42.1/Devel:SLEnkins.repo
zypper ref
zypper in slenkins-engine-vms slenkins
```


**1.Install the systemd-jail**

```
/usr/lib/slenkins/init-jail/init-jail.sh --local
```
   At the end, the script will suggest to add entries to */etc/fstab*,
   so that shared directories are mounted even after a reboot.
   These shared directories enable SLEnkins to share the test workspace
   and the locally built packages between your workstation and the jail.
   **Do as instructed.**

- Edit */etc/libvirt/libvirtd.conf* on your workstation
   so that it contains the following lines:
```
      listen_tcp = 1
      listen_addr = "127.0.0.1"
      auth_tcp = "none"

systemctl restart  libvirtd.service
```

**Run slenkins on your local machine!** 
```
slenkins-vms.sh -j -i sut=openSUSE_42.1-x86_64-default tests-helloworld
```
