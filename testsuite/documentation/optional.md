# Optional components

The test suite can be parametrized to accommodate various test situations.

The parameters are stored in `/root/.bashrc`.

sumaform can prepare those parameters in `.bashrc` for you. For information
about the syntax in your sumaform's `main.tf` file that allows to do this,
please refer to the sumaform documentation:

* [basic instructions](https://github.com/uyuni-project/sumaform/blob/master/README.md)
* [test suite instructions](https://github.com/uyuni-project/sumaform/blob/master/README_TESTING.md)
* [advanced instructions](https://github.com/uyuni-project/sumaform/blob/master/README_ADVANCED.md)

This document here focuses on the test suite side.

## Testing with a proxy

Using an Uyuni proxy with the test suite is not mandatory.

If you do not want a proxy, do not define `$PROXY` environment variable
before you run the test suite. That's all.

If you want a proxy, make this variable point to the machine that will be
the proxy:

```bash
export PROXY=myproxy.example.com
```

and then run the test suite.

Inside of the test suite, the scenarios that are tagged with

```
@proxy
```

are executed only if the proxy is available.

## Testing with a SLE minion

Using a minion with the test suite is not mandatory.

If you do not want a SLE minion, do not define `MINION` environment
variable before you run the test suite. That's all.

If you want a SLE minion, make this variable point to the machine that
will be the minion:

```bash
export MINION=myminion.example.com
```

and then run the test suite.

Inside of the test suite, the scenarios that are tagged with

```
@sle_minion
```

are executed only if the minion is available.

## Testing with a Docker and Kiwi build host

Using a Docker and Kiwi build host with the test suite is not mandatory.

If you do not want such a machine, do not define `BUILD_HOST` environment
variable before you run the test suite. That's all.

If you want a Docker and Kiwi build host, make this variable point to the machine
that will be the build host:

```bash
export BUILD_HOST=my_build_host.example.com
```

and then run the test suite.

Inside of the test suite, the scenarios that are tagged with

```
@buildhost
```

are executed only if the Docker and Kiwi build host is available.

## Testing with a SSH minion

Using a SSH minion with the test suite is not mandatory.

If you do not want a SSH minion, do not define `SSH_MINION` environment
variable before you run the test suite. That's all.

If you want a SSH minion, make this variable point to the machine that
will be the SSH minion:

```bash
export SSH_MINION=myssh.example.com
```

and then run the test suite.

Inside of the test suite, the scenarios that are tagged with

```
@ssh_minion
```

### Testing with a Red Hat-like minion

Using a Red Hat-like minion (CentOS, Alma, Rocky, ...) with the test suite
is not mandatory.

If you do not want a Red Hat-like minion, do not define `RHLIKE_MINION`
environment variable before you run the test suite. That's all.

If you want a Red Hat-like minion, make this variable point to the machine
that will be the Red Hat-like minion:

```bash
export RHLIKE_MINION=rocky8.example.com
```

and then run the test suite.

Inside of the test suite, the scenarios that are tagged with

```
@rhlike_minion
```

are executed only if the Red Hat-like minion is available.

## Testing with a Debian-like minion

Using a Debian-like minion (Debian, Ubuntu, ...) with the test suite
is not mandatory.

If you do not want a Debian-like minion, do not define `DEBLIKE_MINION`
environment variable before you run the test suite. That's all.

If you want a Debian-like minion, make this variable point to the machine
that will be the Debian-like minion:

```bash
export DEBLIKE_MINION=ubuntu2204.example.com
```

and then run the test suite.

Inside of the test suite, the scenarios that are tagged with

```
@deblike_minion
```

are executed only if the Debian-like minion is available.

## Testing Uyuni

The test suite will determine automatically whether your server
is running Uyuni or SUSE Manager.

Inside the test suite, the scenarios that are tagged with

```
@susemanager
```

are executed only if the server has SUSE Manager installed and will
not run if Uyuni is detected.

Inside the test suite, the scenarios that are tagged with

```
@uyuni
```

are executed only if the server has Uyuni installed and will
not run if SUSE Manager is detected.

## Testing with a mirror

Using a mirror with the test suite is not mandatory.

If you do not want a mirror, do not define `MIRROR` environment
variable before you run the test suite. That's all.

If you want a mirror, let this variable be equal to
`yes` or `true`:

```bash
export MIRROR=yes
```

and then run the test suite.

Inside of the test suite, the scenarios that are tagged with

```
@no_mirror
```

are executed only if you don't use a mirror.

## Testing with SCC credentials

Using the SCC credentials with the test suite is not mandatory.

If you do not want to use SCC, do not define `SCC_CREDENTIALS` environment
variable before you run the test suite. That's all.

If you want to use SCC, let this variable be equal to
`"username|password"`:

```bash
export SCC_CREDENTIALS="username|password"
```

and then run the test suite.

## Testing with external Docker or Kiwi profiles

Normally, the profiles are stored within the test suite itself (on the uyuni branch only),
but you can also use another git repository for that.

If you want to use external profiles, declare:

```bash
export GITPROFILES="https://github.com#mybranch:myprofiles"
```

and then run the test suite.

This variable needs to be set even if you don't use external profiles (to the normal
place `https://github.com/uyuni-project/uyuni/tree/master/testsuite/features/profiles`).

## Testing virtualization features

Using a virtualization host with the test suite is not mandatory.

If you do not want a virtualization host minion, do not define `VIRTHOST_KVM_URL` environment 
variable before you run the test suite. That's all.

If you want virtualization minions, make these variables point to the machines that
will be the virtualization KVM host minions and define the `VIRTHOST_KVM_PASSWORD` variable:

```bash
export VIRTHOST_KVM_URL=myvirthost.example.com
export VIRTHOST_KVM_PASSWORD=therootpwd
```

Make sure the image to use for the test virtual machines is located in
`/var/testsuite-data/` on the virtual hosts.

In order for the virtual hosts to be able to report to the test server,
use a bridge virtual network for the test machines.

The `leap-disk-image-template.qcow2` virtual disk image should
have avahi daemon installed and running at first boot, and should be capable to be booted
as a KVM guest. The disk images used by sumaform are good candidates
for this.

Note that the virtualization host needs to be a physical machine that needs
to be accessible via SSH without a passphrase from the machine running the test suite. It
also requires the `qemu-img`, `virt-install` and `virt-customize` tools to be installed and
the controller SSH public key needs to be added to the `authorized_keys` file.

Inside of the test suite, the scenarios that are tagged with:

```
@virtualization_kvm
```

are executed only if the corresponding virtualization host minion is available.

## Testing Uyuni for Retail

Testing Uyuni for Retail is optional. To test it, you need:

* a private network
* a PXE boot minion.
* a DHCP and DNS server

The PXE boot minion and the DHCP and DNS server will reside in the private network only.
The proxy will route between the private network and the outer world.

### Private network

If you do not want a private network, do not define `PRIVATENET`
environment variable before you run the test suite. That's all.

If you want that optional network, make this variable contain
`yes` or `true`:

```bash
export PRIVATENET=yes
```

and then run the test suite.

Inside of the test suite, the scenarios that are tagged with

```
@private_net
```

are executed only when there is a private network.

### PXE boot minion

The PXE boot minion can be reached only from the proxy and
via the private network. The proxy reboots this minion
through SSH and then triggers a complete reinstallation
with the help of PXE.

If you do not want a PXE boot minion, do not define `PXEBOOT_MAC` nor
`PXEBOOT_IMAGE` environment variables before you run the test suite.
That's all.

If you want a PXE boot minion, make these variables contain
the MAC address of the PXE boot minion and the name of
the desired image you want it reformatted with:

```bash
export PXEBOOT_MAC=52:54:00:01:02:03
export PXEBOOT_IMAGE=sles12sp5
```

and then run the test suite.

`52:54:00:` is the prefix assigned to qemu.
Currently supported images are `sles12sp5` and `sles15sp4`.

Inside of the test suite, the scenarios that are tagged with

```
@pxeboot_minion
```

are executed only if the PXE boot minion is available.

## HTTP Proxy for the Uyuni server

Using an HTTP proxy for the Uyuni server when testing is not mandatory.

If you do not want an HTTP proxy, do not define `SERVER_HTTP_PROXY`
environment variable before you run the test suite. That's all.

If you want to specify an HTTP proxy on Uyuni's "Setup Wizard" page,
make this variable contain the hostname and port of the proxy:

```bash
export SERVER_HTTP_PROXY = "hostname:port"
```

Inside the test suite, the scenarios that are tagged with

```
@server_http_proxy
```

are executed only if the HTTP proxy is available.

## Custom download endpoint for packages

Using an custom download endpoint for the packages when testing is not mandatory.

If you do not want a custom download endpoint, do not define `CUSTOM_DOWNLOAD_ENDPOINT`
environment variable before you run the test suite. That's all.

If you want to specify a custom download endpoint for downloading the packages,
make this variable contain the URL of the custom download endpoint:

```bash
export CUSTOM_DOWNLOAD_ENDPOINT = "protocol://hostname:port"
```

Inside the test suite, the scenarios that are tagged with

```
@custom_download_endpoint
```

are executed only if the custom download endpoint is available.

## Docker registry server

Using a Docker authenticated registry server when testing is not mandatory.

If you do not want an authenticated registry server, do not define `AUTH_REGISTRY` nor `AUTH_REGISTRY_CREDENTIALS`
environment variables before you run the test suite. That's all.

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

You can also set this option from sumaform, see [Alternative Authenticated Docker Registry](https://github.com/uyuni-project/sumaform/blob/master/README_TESTING.md#alternative-authenticated-docker-registry).
