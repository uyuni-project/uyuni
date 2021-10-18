## Optional components

The test suite can be parametrized to accomodate various test situations.

The parameters are stored in `/root/.bashrc`.

sumaform can prepare those parameters in `.bashrc` for you. For information
about the syntax in your sumaform's `main.tf` file that allows to do this,
please refer to sumaform's documentation:
* [basic instructions](https://github.com/uyuni-project/sumaform/blob/master/README.md)
* [test suite instructions](https://github.com/uyuni-project/sumaform/blob/master/README_TESTING.md)
* [advanced instructions](https://github.com/uyuni-project/sumaform/blob/master/README_ADVANCED.md)

This document here focuses on he test suite side.


### Testing with a proxy

Using a Uyuni proxy with the testsuite is not mandatory.

If you do not want a proxy, do not define `$PROXY` environment variable
before you run the testsuite. That's all.

If you want a proxy, make this variable point to the machine that will be
the proxy:
```bash
export PROXY=myproxy.example.com
```
and then run the testsuite.

Inside of the testsuite, the scenarios that are tagged with
```
@proxy
```
are executed only if the proxy is available.


### Testing with a SLE minion

Using a minion with the testsuite is not mandatory.

If you do not want a SLE minion, do not define `MINION` environment
variable before you run the testsuite. That's all.

If you want a SLE minion, make this variable point to the machine that
will be the minion:
```bash
export MINION=myminion.example.com
```
and then run the testsuite.

Inside of the testsuite, the scenarios that are tagged with
```
@sle_minion
```
are executed only if the minion is available.


### Testing with a Docker and Kiwi build host

Using a Docker and Kiwi build host with the testsuite is not mandatory.

If you do not want such a machine, do not define `BUILD_HOST` environment
variable before you run the testsuite. That's all.

If you want a Docker and Kiwi build host, make this variable point to the machine
that will be the build host:
```bash
export BUILD_HOST=my_build_host.example.com
```
and then run the testsuite.

Inside of the testsuite, the scenarios that are tagged with
```
@buildhost
```
are executed only if the Docker and Kiwi build host is available.


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

Inside of the testsuite, the scenarios that are tagged with
```
@ssh_minion
```
are executed only if the SSH minion is available.


### Testing with a traditional client

Using a traditional client with the testsuite is not mandatory.

If you do not want a traditional client, do not define `CLIENT` environment
variable before you run the testsuite. That's all.

If you want a traditional client, make this variable point to the machine that
will be the traditional client:
```bash
export CLIENT=mytraditionalclient.example.com
```
and then run the testsuite.

Inside of the testsuite, the scenarios that are tagged with
```
@sle_client
```
are executed only if the traditional client is available.


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

Inside of the testsuite, the scenarios that are tagged with
```
@centos_minion
```
are executed only if the CentOS minion is available.


### Testing with an Ubuntu minion

Using an Ubuntu minion with the testsuite is not mandatory.

If you do not want an Ubuntu minion, do not define `UBUNTUMINION` environment
variable before you run the testsuite. That's all.

If you want an Ubuntu minion, make this variable point to the machine that
will be the Ubuntu minion:
```bash
export UBUNTUMINION=ubuntu.example.com
```
and then run the testsuite.

Inside of the testsuite, the scenarios that are tagged with
```
@ubuntu_minion
```
are executed only if the Ubuntu minion is available.


### Testing Uyuni

The test suite will determine automatically whether your server
is running Uyuni or SUSE Manager

Inside the testsuite, the scenarios that are tagged with

```
@susemanager
```
are executed only if the server has SUSE Manager installed and will
not run if Uyuni is detected.

Inside the testsuite, the scenarios that are tagged with

```
@uyuni
```
are executed only if the server has Uyuni installed and will
not run if SUSE Manager is detected.


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

Inside of the testsuite, the scenarios that are tagged with
```
@no_mirror
```
are executed only if you don't use a mirror.


### Testing with SCC credentials

Using the SCC credentials with the testsuite is not mandatory.

If you do not want to use SCC, do not define `SCC_CREDENTIALS` environment
variable before you run the testsuite. That's all.

If you want to use SCC, let this variable be equal to
`"username|password"`:
```bash
export SCC_CREDENTIALS="username|password"
```
and then run the testsuite.


### Testing with external Docker or Kiwi profiles

Normally, the profiles are stored within the testsuite itself (on the uyuni branch only),
but you can also use another git repository for that.

If you want to use external profiles, declare:
```bash
export GITPROFILES="https://github.com#mybranch:myprofiles"
```
and then run the testsuite.

This variable needs to be set even if you don't use external profiles (to the normal
place `https://github.com/uyuni-project/uyuni/tree/master/testsuite/features/profiles`).


### Testing virtualization features

Using a virtualization host with the testsuite is not mandatory.

If you do not want a virtualization host minion, do not define `VIRTHOST_KVM_URL` or
`VIRTHOST_XEN_URL` environment variables before you run the testsuite. That's all.

If you want virtualization minions, make these variables point to the machines that
will be the virtualization KVM or Xen host minions and define the `VIRTHOST_KVM_PASSWORD`
and `VIRTHOST_XEN_PASSWORD` variables:

```bash
export VIRTHOST_KVM_URL=myvirthost.example.com
export VIRTHOST_KVM_PASSWORD=therootpwd
```

Make sure the image to use for the test virtual machines is located in
`/var/testsuite-data/disk-image-template.qcow2` on the virtual hosts.
A `/var/testsuite-data/disk-image-template-xenpv.qcow2` disk image to use for Xen ParaVirtualized
guests is needed on the machine pointed by `VIRTHOST_XEN_URL`.

In order for the virtual hosts to be able to report to the test server,
use a bridge virtual network for the test machines.

The `disk-image-template.qcow2` virtual disk image should
have avahi daemon installed and running at first boot, and should be capable to be booted
as either a Xen HVM or KVM guest. The disk images used by sumaform are good candidates
for this.

Note that the virtualization host needs to be a physical machine that needs
to be accessible via SSH without a passphrase from the machine running the test suite. It
also requires the `qemu-img`, `virt-install` and `virt-customize` tools to be installed and
the controller SSH public key needs to be added to the `authorized_keys` file.

Inside of the testsuite, the scenarios that are tagged with one of:
```
@virtualization_kvm
@virtualization_xen
```
are executed only if the corresponding virtualization host minion is available.


### Testing Uyuni for Retail

Testing Uyuni for Retail is optional. To test it, you need:
* a private network;
* a PXE boot minion.

The PXE boot minion will reside in the private network only.
The proxy will route between the private network and the outer world.


#### Private network

If you do not want a private network, do not define `PRIVATENET`
environment variable before you run the testsuite. That's all.

If you want that optional network, make this variable contain
`yes` or `true`:
```bash
export PRIVATENET=yes
```
and then run the testsuite.

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

If you do not want a PXE boot minion, do not define `PXEBOOT_MAC` nor
`PXEBOOT_IMAGE` environment variables before you run the testsuite.
That's all.

If you want a PXE boot minion, make these variables contain
the MAC address of the PXE boot minion and the name of
the desired image you want it reformatted with:
```bash
export PXEBOOT_MAC=52:54:00:01:02:03
export PXEBOOT_IMAGE=sles12sp3
```
and then run the testsuite.

`52:54:00:` is the prefix assigned to qemu.
Currently supported images are `sles12sp3` and `sles15sp1`.

Inside of the testsuite, the scenarios that are tagged with
```
@pxeboot_minion
```
are executed only if the PXE boot minion is available.


## HTTP Proxy for the Uyuni server

Using an HTTP proxy for the Uyuni server when testing is not mandatory.

If you do not want an HTTP proxy, do not define `SERVER_HTTP_PROXY`
environment variable before you run the testsuite. That's all.

If you want to specify an HTTP proxy on Uyuni's "Setup Wizard" page,
make this variable contain the hostname and port of the proxy:
```bash
export SERVER_HTTP_PROXY = "hostname:port"
```

Inside the testsuite, the scenarios that are tagged with
```
@server_http_proxy
```
are executed only if the HTTP proxy is available.


## Docker Registry server

Using a Docker Authenticated Registry server when testing is not mandatory.

If you do not want an authenticated registry server, do not define `AUTH_REGISTRY` nor `AUTH_REGISTRY_CREDENTIALS`
environment variables before you run the testsuite. That's all.

If you want to specify an authenticated registry server to be used when testing Docker, make the
`AUTH_REGISTRY` variable contain the URI of the registry server,
and place the credentials on that server in the `AUTH_REGISTRY_CREDENTIALS` separated
by a vertical bar:
```bash
export AUTH_REGISTRY = "hostname:port/path"
export AUTH_REGISTRY_CREDENTIALS = "user|password"
```

In case you want to use a non-authenticated registry, you need to use:
```bash
export NO_AUTH_REGISTRY = "hostname:port/path"
```

You can also set this option from sumaform:
https://github.com/uyuni-project/sumaform/blob/master/README_TESTING.md#alternative-authenticated-docker-registry
