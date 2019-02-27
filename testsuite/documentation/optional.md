## Optional components

### Testing with a proxy

Using a SUSE Manager proxy with the testsuite is not mandatory.

If you do not want a proxy, do not define `$PROXY` environment variable
before you run the testsuite. That's all.
If you want a proxy, make this variable point to the machine that will be
the proxy:
```bash
export PROXY=myproxy.example.com
```
and then run the testsuite.

Sumaform can prepare a proxy virtual machine and declare the `$PROXY`
variable on the controller (in `/root/.bashrc`).
To declare a proxy in your `main.tf` file, add a line to the controller
declaration that looks like:
```
proxy_configuration = "${module.proxy.configuration}"
```
The module defining the proxy is declared accordingly to the "Proxies"
chapter of the
[advanced instructions](https://github.com/moio/sumaform/blob/master/README_ADVANCED.md)
for sumaform.

Inside of the testsuite, the scenarios that are tagged with
```
@proxy
```
are executed only if the proxy is available.


### Testing with a SSH minion

Using a SSH minion with the testsuite is not mandatory.

If you do not want a SSH minion, do not define `SSHMINION` environment
variable before you run the testsuite. That's all.
If you want a SSH minion, make this variable point to the machine that
will be the SSH minion:
```bash
export SSHMINION=myssh.example.com
```
and then run the testsuite.

Sumaform can prepare a SSH minion virtual machine and declare the `$SSHMINION`
variable on the controller (in `/root/.bashrc`).
To declare a SSH minion in your `main.tf` file, add a line
to the controller declaration that looks like:
```
centos_configuration="${module.minsles12sp3ssh.configuration}"
```

Inside of the testsuite, the scenarios that are tagged with
```
@ssh_minion
```
are executed only if the SSH minion is available.


### Testing with a CentOS minion

Using a CentOS minion with the testsuite is not mandatory.

If you do not want a CentOS minion, do not define `CENTOSMINION` environment
variable before you run the testsuite. That's all.
If you want a CentOS minion, make this variable point to the machine that
will be the CentOS minion:
```bash
export CENTOSMINION=myceos.example.com
```
and then run the testsuite.

Sumaform can prepare a CentOS virtual machine and declare the `$CENTOSMINION`
variable on the controller (in `/root/.bashrc`).
To declare a CentOS minion in your `main.tf` file, add a line
to the controller declaration that looks like:
```
centos_configuration="${module.mincentos7.configuration}"
```
The module defining the CentOS minion will use a CentOS image:
```
image = "centos7"
```

Inside of the testsuite, the scenarios that are tagged with
```
@centos_minion
```
are executed only if the CentOS minion is available.


### Testing with a Ubuntu minion

Using an Ubuntu minion with the testsuite is not mandatory.

If you do not want an Ubuntu minion, do not define `UBUNTUMINION` environment
variable before you run the testsuite. That's all.
If you want an Ubuntu minion, make this variable point to the machine that
will be the Ubuntu minion:
```bash
export UBUNTUMINION=ubuntu.example.com
```
and then run the testsuite.

Sumaform can prepare an Ubuntu virtual machine and declare the `$UBUNTUMINION`
variable on the controller (in `/root/.bashrc`).
To declare an Ubuntu minion in your `main.tf` file, add a line
to the controller declaration that looks like:
```
ubuntu_configuration="${module.min-ubuntu.configuration}"
```
The module defining the Ubuntu minion will use a Ubuntu image:
```
image = "ubuntu1804"
```

Inside of the testsuite, the scenarios that are tagged with
```
@ubuntu_minion
```
are executed only if the Ubuntu minion is available.


### Testing with a SLE 15 system

The test suite will determine automatically whether your minion
is a SLE15 system or not.

Inside of the testsuite, the scenarios that are tagged with
```
@sle15_minion
```
are executed only if the minion is a SLE 15 system.

### Testing Uyuni

The test suite will determine automatically whether your server
is running Uyuni or SUSE Manager

Inside the testsuite, the scenarios that are tagged with

```
@susemanager
```
are executed only if the server has SUSE Manager installed and will
not run if Uyuni is detected.

### Testing with a mirror

Using a mirror with the testsuite is not mandatory.

If you do not want a mirror, do not define `MIRROR` environment
variable before you run the testsuite. That's all.
If you want a mirror, let this variable be equal to
`yes` or `true`:
```bash
export MIRROR=yes
```
and then run the testsuite.

Sumaform can prepare a mirror and declare the `$MIRROR`
variable on the controller (in `/root/.bashrc`).
To declare a mirror in your `main.tf` file, follow the
instructions in sumaform documentation.

Inside of the testsuite, the scenarios that are tagged with
```
@no_mirror
```
are executed only if you don't use a mirror.


### Testing with external Docker or Kiwi profiles

Normally, the profiles are stored within the testsuite itself, but
you can also use another git repository for that.

If you want to use external profiles, declare:
```bash
export GITPROFILES="https://github.com#mybranch:myprofiles"
```
and then run the testsuite.

This variable needs to be set even if you don't use external
profiles.

Sumaform declares the `$GITPROFILES`
variable on the controller (in `/root/.bashrc`).
To declare your own external profiles in your `main.tf` file, add a line
to the controller declaration that looks like:
```
git_profiles_repo="https://github.com#mybranch:myprofiles"
```


### Testing virtualization features

Using a virtualization host with the testsuite is not mandatory.

If you do not want a virtualization host minion, do not define `VIRTHOST_KVM_URL` and
`VIRTHOST_XEN_URL` environment variables before you run the testsuite. That's all.
If you want virtualization minions, make these variables point to the machines that
will be the virtualization KVM or Xen host minions and define the `VIRTHOST_KVM_PASSWORD`
and `VIRTHOST_XEN_PASSWORD` variables:

```bash
export VIRTHOST_KVM_URL=myvirthost.example.com
export VIRTHOST_KVM_PASSWORD=therootpwd
```

One of the virtualization servers can be disabled by not providing the corresponding
environment variables.

Make sure the image to use for the test virtual machines is located in
`/var/testsuite-data/disk-image-template.qcow2` on the virtual hosts.
A `/var/testsuite-data/disk-image-template-xenpv.qcow2` disk image to use for Xen ParaVirtualized
guests is needed on the machine pointed by `VIRTHOST_XEN_URL`.
In order for the virtual hosts to be able to report to the test server,
use a bridge virtual network for the test machines.

The `disk-image-template.qcow2` virtual disk image should have the root file system on the `/dev/sda1` partition,
have avahi daemon installed and running at first boot, and should be capable to be booted
as either a Xen HVM or KVM guest. The disk images used by sumaform are good candidates
for this.

Note that the virtualization host needs to be a physical machine that needs
to be accessible via SSH without a passphrase from the machine running the test suite. It
also requires the `qemu-img`, `virt-install` and `guestmount` tools to be installed and
the controller SSH public key needs to be added to the `authorized_keys` file.

Inside of the testsuite, the scenarios that are tagged with one of:
```
@virtualization_kvm
@virtualization_xen
```
are executed only if the corresponding virtualization host minion is available.

### Testing SUSE Manager for Retail

Testing SUSE Manager for Retail is optional. To test it, you need:
* a private network;
* a PXE boot minion.

The PXE boot minion will reside in the private network only.
The proxy will route between the private network and the
outer world.


#### Private network

If you do not want a private network, do not define `PRIVATENET`
environment variable before you run the testsuite. That's all.
If you want that optional network, make this variable contain
`yes` or `true`:
```bash
export PRIVATENET=yes
```
and then run the testsuite.

Sumaform declares the `$PRIVATENET`
variable on the controller (in `/root/.bashrc`).
To create the private network in your `main.tf` file, add a line
to the base declaration that looks like:
```
additional_network=true
```

Inside of the testsuite, the scenarios that are tagged with
```
@private_net
```
are executed only when there is a private network.


#### PXE boot minion

The PXE boot minion can be reached only from the proxy and
via the private network. The proxy reboots this minion
through SSH and then triggers a complete reinstallation
with the help of PXE.

If you do not want a PXE boot minion, do not define `PXEBOOTMAC`
environment variable before you run the testsuite. That's all.
If you want a PXE boot minion, make this variable contain
the MAC address of the PXE boot minion:
```bash
export PXEBOOTMAC=52:54:00:01:02:03
```
and then run the testsuite.

Sumaform declares the `$PXEBOOTMAC`
variable on the controller (in `/root/.bashrc`).
To create the PXE boot minion in your `main.tf` file, add a line
to the controller declaration that looks like:
```
pxeboot_configuration = "${module.pxeboot.configuration}
```
The module defining the PXE boot minion is declared accordingly to the
"PXE boot hosts" chapter of the
[advanced instructions](https://github.com/moio/sumaform/blob/master/README_ADVANCED.md)
for sumaform.

Inside of the testsuite, the scenarios that are tagged with
```
@pxeboot_minion
```
are executed only if the PXE boot minion is available.
