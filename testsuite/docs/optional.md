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


### Testing with a SLE 15 system

The test suite will determine automatically whether your minion
is a SLE15 system or not.

Inside of the testsuite, the scenarios that are tagged with
```
@sle15_minion
```
are executed only if the minion is a SLE 15 system.


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


### Testing SUSE Manager for Retail

Testing SUSE Manager for Retail is optional. To test it, you need:
* a private network;
* a JeOS minion.

The JeOS minion will reside in the private network only.
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

If you do not want a JeOS minion, do not define `JEOSMAC`
environment variable before you run the testsuite. That's all.
If you want a JeOS minion, make this variable contain
the MAC address of the JeOS minion:
```bash
export JEOSMAC=52:54:00:01:02:03
```
and then run the testsuite.

#### JeOS minion

The JeOS minion can be reached only from the proxy and
via the private network. It has a fixed IP address and
domain name:
`192.168.5.4` and `terminal.example.org`.

This is needed to be able to reboot it and then
to trigger a complete reinstallation with help of PXE.

Summaform cannot prepare yet the JeOS virtual machine.
This is work in progress (github issue #5952).

Inside of the testsuite, the scenarios that are tagged with
```
@jeos_minion
```
are executed only if the JeOS minion is available.
