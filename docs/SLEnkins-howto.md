# SLEnkins Spacewalk Suite HOWTO:

#### Official and complete documentation is here:
http://slenkins.suse.de/doc/

### The difference between master suite and the SLEnkins suite.
[What has changed](changes.md)


### HOWTO TOPICS:

*  1) Installation of SLEnkins in your machine.
*  2) How to run the Spacewalk Suite on you local machine *DINAMIC-SETUP*
*  3) How to run  the Spacewalk Suite on bare-machines,  *STATIC-SETUP* ( like from orthos, etc. )
*  4) How to run it on Cloud6 with Jenkins(2.0) (slenkins.suse.de) 
*  5) Developing a new/existing testsuite.
*  6) Extras infos (hacking)


### 1) Installation of SLEnkins in your local machine/server.



**Install the packages**
```
openSUSE_Leap_42.1 modify it with your distro
zypper ar http://download.suse.de/ibs/Devel:/SLEnkins/openSUSE_Leap_42.1/Devel:SLEnkins.repo
zypper ref
zypper in slenkins-engine-vms slenkins
```


**Install the systemd-jail**

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

For explanation about this command and flags , read this:
http://slenkins.suse.de/doc/REFERENCE-slenkinsvms.txt

basically -j say to run in the jail, -i = IMAGE for the vms created by virsh.

### 2 How to run the Spacewalk Suite on you local machine

```
slenkins-vms.sh -j -i server=SLE_12_SP1-x86_64-default -i client=SLE_12_SP1-x86_64-default -i minion=SLE_12_SP1-x86_64-default tests-suse-manager
```

Basically, you can define your machine type, by chosing from avaible images:

the command give you the overview of images you can use:
``` slenkins-vms.sh -a ```

http://slenkins.suse.de/images/

The default is an image without graphic (gnome), minimal pattern.

You can combine the run the testsuite, with a different matrix of images (FAMILY like SP1 or ARCH like ppc64le)

```
slenkins-vms.sh -j -i server=SLE_12_SP1-x86_64-gnome -i client=SLE_12_SP1-ppc64le-default -i 
minion=SLE_12_SP1-x86_64-fips tests-suse-manager
```

**IF the testsuite fail, the vms created are conserved, so you can login into it. If testsuite success, the machines are automatically destroyed.**

for the spacewalk suite the pwd is the GALAXY standard.


### 3) How to run  the Spacewalk Suite on bare-machines,  *STATIC-SETUP* ( like from orthos, etc. )


