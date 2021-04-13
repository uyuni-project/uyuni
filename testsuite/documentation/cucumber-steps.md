## Writing your own Cucumber steps, and reusing Cucumber steps

### Table of Contents
1. [Basic notions](#a)
1. [Reusing old steps](#b)
    1. [Logging in and out](#b1)
    1. [Navigating through pages](#b2)
    1. [Texts](#b3)
    1. [Links](#b4)
    1. [Buttons](#b5)
    1. [Text input](#b6)
    1. [Operating system](#b7)
    1. [SUSE Manager utilities](#b8)
    1. [Registration and channels](#b9)
    1. [Events](#b10)
    1. [Salt](#b11)
    1. [XML-RPC](#b12)
    1. [Virtualization](b#13)
1. [Writing new tests](#c)
    1. [Running remote commands](#c1)
    1. [Getting the FQDN of a host](#c2)
    1. [Converting between host name and target](#c3)


<a name="a" />

### Basic notions

We use "targets" in the Ruby code to encapsulate the notion of testing hosts.
The corresponding notion in Cucumber steps is "step host names".

Possible values are currently:

| Test host | Ruby target |  Bash environment variable | Step host name | Sumaform module |
| --------- | ----------- | -------------------------- | -------------- | --------------- |
| SUSE Manager server | ```$server``` | ```$SERVER``` |  | ```"suse_manager"``` |
| SUSE Manager proxy | ```$proxy``` | ```$PROXY``` | ```"proxy"``` | ```"suse_manager_proxy"``` |
| SLES traditional client | ```$client``` | ```$CLIENT``` | ```"sle_client"``` or ```"sle_ssh_tunnel_client"```| ```"client"``` |
| SLES Salt minion | ```$minion``` | ```$MINION``` | ```"sle_minion"``` or ```"sle_migrated_minion"``` or ```"sle_ssh_tunnel_minion"``` or ```"sle_spack_migrated_minion"``` | ```"minion"``` |
| SLES Docker and Kiwi build host | ```$build_host``` | ```$BUILD_HOST``` | ```"build_host"``` | ```"minion"``` |
| SLES Salt SSH minion | ```$ssh_minion``` | ```$SSHMINION``` | ```"ssh_minion"``` or ```"ssh_spack_migrated_minion"``` | ```"minion"``` |
| CentOS Salt minion or traditional client | ```$ceos_minion``` | ```$CENTOSMINION``` | ```"ceos_minion"```, ```"ceos_traditional_client"```, or ```"ceos_ssh_minion"``` | ```"minion"``` |
| Ubuntu minion | ```$ubuntu_minion``` | ```$UBUNTUMINION``` | ```"ubuntu_minion"``` or ```"ubuntu_ssh_minion"``` | ```"minion"``` |
| PXE-Boot minion |  None | ```$PXEBOOT_MAC``` | ```"pxeboot_minion"``` | ```"pxeboot"``` |
| KVM virtual host minion | ```$kvm_server``` | ```$VIRTHOST_KVM_URL``` and ```$VIRTHOST_KVM_PASSWORD``` | ```"kvm_server"``` | ```"virthost"``` |
| Xen virtual host minion | ```$xen_server``` | ```$VIRTHOST_XEN_URL``` and ```$VIRTHOST_XEN_PASSWORD``` | ```"xen_server"``` | ```"virthost"``` |

These names are such for historical reasons and might be made better in the future.


<a name="b" />

### Reusing old steps

These are only the most important generic steps. For more specialized steps,
please grep in the source code.


<a name="b1" />

#### Logging in and out

* Go to the login page

```cucumber
  When I go to the home page
```

* Sign in as user "testing" with password "testing"

```cucumber
  Given I am authorized
```

* Sign in as a given user with given password

```cucumber
  Given I am authorized as "admin" with password "admin"
```

* Check we are signed in

```cucumber
  Then I should be logged in
```

To check for the initial log in, prefer ```Then I am logged in```.

* Sign out

```cucumber
  Given I sign out
```

* Check we are signed out

```cucumber
  Then I should not be authorized
```


<a name="b2" />

#### Navigating through pages

* Go to a given page through a the left menu tree with the complete menu path

```cucumber
  When I follow the left menu "Systems > System List > System Currency"
```

* Go to Admin => Setup Wizard => Products

```cucumber
  Given I am on the Products page
```

* Go to Admin => Organizations

```cucumber
  Given I am on the Organizations page
```

* Go to Patches => Patches => Relevant

```cucumber
  When I am on the patches page
```

* Go to Salt => Keys

```cucumber
  When I follow the left menu "Salt > Keys"
```

* Go to Systems => Overview

```cucumber
  When I am on the System Overview page
```

* Go to Systems => Autoinstallation => Overview

```cucumber
  When I am on Autoinstallation Overview page
```

* Go to Systems => Autoinstallation => Profiles => Upload Kickstart/Autoyast File

```cucumber
  When I am on the Create Autoinstallation Profile page
```

* Go to Systems => Bootstrapping

```cucumber
  When I follow the left menu "Systems > Bootstrapping"
```

* Go to Systems => Systems

```cucumber
  When I click Systems, under Systems node
```

* Go to Systems => System Groups

```cucumber
  When I am on the groups page
```

* Go to Systems => System Set Manager

```cucumber
  When I am on the System Manager System Overview page
```

* Go to Systems => System Set Manager => Overview

```cucumber
  When I am on System Set Manager Overview
```

* Go to Users => Users list => Active

```cucumber
  When I follow the left menu "Users > User List > Active"
```

* Go to details of a given client

```cucumber
  When I am on the Systems overview page of this "sle_client"
```

* Go to a tab page of a given client

```cucumber
  When I am on the "Virtualization" page of this "sle_client"
```

* Go to configuration of "SUSE Test" organization

```cucumber
  When I am on the System Manager System Overview page
```

* Test the last opened window

```cucumber
  And I switch to last opened windo
```

<a name="b3" />

#### Texts

* Check for a given text

```cucumber
  Then I should see a "System Overview" text
  Then I should not see a "[Management]" text
  Then I should see a "Keys" text in the content area
  Then I should see "sle_minion" hostname
  Then I should not see "sle_minion" hostname
  Then table row for "test-net1" should contain "running"
```

For a test with a regular expression, there is ```I should see a text like "..."```

* Same, but wait until text appears

```cucumber
  When I wait until I see "Successfully bootstrapped host!" text
  When I wait until I do not see "Loading..." text
  When I wait at most 360 seconds until I see "Product Description" text
  When I wait at most 600 seconds until the tree item "test-pool0" contains "inactive" text
  When I wait until table row for "test-net1" contains "running"
```

* Same, but re-issue HTTP requests to refresh the page

```cucumber
  When I wait until I see "Software Updates Available" text, refreshing the page
  When I wait until I do not see "Apply highstate scheduled by admin" text, refreshing the page
  When I wait until I see the name of "sle_minion", refreshing the page
  When I wait until I do not see the name of "sle_minion", refreshing the page
  When I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows
  When I refresh page until I see "sle_minion" hostname as text
```

(last one should probably be renamed - it looks in the contents area of the page)

* Wait until a tree item has no sub list

```cucumber
  When I wait at most 600 seconds until the tree item "test-pool0" has no sub-list
```

<a name="b4" />

#### Links

* Check for a link

```cucumber
  Then I should see a "Create Config Channel" link
  Then I should see a "Managed Systems" link in the left menu
  Then I should see a "Details" link in the content area
  Then I should not see a "norole" link
```

* Click on a given link

```cucumber
  When I follow "Dependancies"
  When I follow first "Schedule System Reboot"
  When I click on "Use in SSM" for "newgroup"
```


<a name="b5" />

#### Buttons

* Click on a given button

```cucumber
  When I click on "Schedule"
  When I click on "Refresh" in tree item "test-pool0"
```

The button can be identified by id, value, title, text content, or alt of image.
If several buttons have been found, this step picks the first one.

* Click on a given radio button

```cucumber
  When I check radio button "schedule-by-action-chain"
  When I choose ";"
```

The radio button can be identified by name, id or label text.

(will be unified)

* Make sure a radio button is checked

```cucumber
  Then radio button "radio-comma" is checked
```

* Click on a given check box

```cucumber
  When I check "manageWithSSH"
  When I uncheck "role_org_admin"
  When I check "container_build_host" if not checked
```

The check box can be identified by name, id or label text.

* Click on a check box in a table

```cucumber
  When I check the row with the "virgo-dummy-3456" link
  When I check the row with the "suse_docker_admin" text
  When I check the "sle_client" client
  When I check "New Test Channel" in the list
  When I uncheck "hoag-dummy-1.1-1.1" in the list
```

(will be unified)

* Make sure a checkbox is checked or not

```cucumber
  Then I should see "metadataSigned" as checked
  Then I should see "role_org_admin" as unchecked
```

* Select an item from a selection box

```cucumber
  When I select "Mr." from "prefix"
  When I select the hostname of "proxy" from "proxies"
```

* Wait for a selection box to contain an item

```cucumber
  When I wait until option "dir" appears in list "type"
```

* Make sure an item in a selection box is selected

```cucumber
  Then option "Mr." is selected as "prefix"
```

* Check for a button in a given row

```cucumber
  When I wait until table row for "test-vm" contains button "Resume"
  When I wait at most 300 seconds until table row for "test-vm" contains button "Resume"
  When I wait at most 600 seconds until the tree item "test-pool1" contains "test-pool1 is started automatically" button
```


<a name="b6" />

#### Text input

* Type text in given input field of a form

```cucumber
  When I enter "SUSE Test Key x86_64" as "description"
  When I enter "SUSE Test Key x86_64" as "description" text area
  When I enter "CVE-1999-12345" as "search_string" in the content area
  When I enter the hostname of "proxy" as "hostname"
```

Note that the text area variant handles the new lines characters while the others don't.

* Make sure a text is in a given input field of a form

```cucumber
  Then I should see "20" in field "usageLimit"
```

* Type text in the text editor

```cucumber
  When I enter "MGR_PROXY=yes" in the editor
```


<a name="b7" />

#### Operating system

* Run an arbitrary command and expect it to succeed

```cucumber
  When I run "rhn_check -vvv" on "sle_client"
  When I run "apt update" on "ubuntu_minion" with logging
```

* Run an arbitrary command and expect it to fail

```cucumber
  When I run "ls /srv/susemanager/salt/top.sls" on "server" without error control
  Then the command should fail
```

* Repositories

```cucumber
  When I enable repository "Test-Packages_Pool" on this "sle_minion"
  When I disable repository "Test-Packages_Pool" on this "sle_minion"
```

* Packages

```cucumber
  When I install package "virgo-dummy-1.0-1.1" on this "sle_minion"
  When I remove package "orion-dummy" from this "sle_minion"
  When I refresh packages list via spacecmd on "sle_minion"
  When I wait for "virgo-dummy-1.0" to be installed on "sle_minion"
  When I wait for "milkyway-dummy" to be uninstalled on "sle_minion"
  When I wait until refresh package list on "sle_minion" is finished
  When I wait until package "virgo-dummy" is installed on "sle_minion" via spacecmd
  Then "man" should be installed on "sle_client"
  Then "milkyway-dummy" should not be installed on "sle_minion"
  Then spacecmd should show packages "virgo-dummy-1.0 milkyway-dummy" installed on "sle_minion"
```

* Services

```cucumber
  When I shutdown the spacewalk service
  When I restart the spacewalk service
  When I wait until "salt-minion" service is up and running on "ceos_minion"
  Then service "bind" is enabled on "proxy"
  Then service "dhcpd" is running on "proxy"
```

* File removal
```cucumber
  When I remove "/root/foobar" from "sle_minion"
  When I destroy "/var/lib/pgsql/data/pg_xlog" directory on server
  When I destroy "/etc/s-mgr" directory on "sle_minion"
```

* File existence
```cucumber
  When I wait until file "/root/foobar" exists on "sle_minion"
  Then file "/etc/mgr-test-file.cnf" should exist on "sle_client"
  When I wait until file "/srv/tftpboot/pxelinux.cfg/default" exists on server
  Then file "/srv/susemanager/salt/manager_org_1/mixedchannel/init.sls" should exist on server
  Then file "/srv/susemanager/salt/manager_org_1/s-mgr/config/init.sls" should not exist on server
  Then file "/var/lib/libvirt/images/test-pool0" should not exist on "kvm_server"
  Then file "/var/lib/libvirt/images/test-pool1" should have 755 permissions on "kvm_server"
```

* File contents

```cucumber
  When I wait until file "/srv/tftpboot/pxelinux.cfg/default" contains "kernel_option=a_value" on server
  Then file "/etc/mgr-test-file.cnf" should contain "MGR_PROXY=yes" on "sle_client"

  When I get the contents of the remote file "/etc/salt/master.d/susemanager.conf"
  Then it should contain a "rest_cherrypy:" text
```

(last ones could be unified)

* Wait for reboot to finish

```cucumber
  When I wait and check that "sle_client" has rebooted
```


<a name="b8" />

#### SUSE Manager utilities

* Execute mgr-sync

```cucumber
  When I execute mgr-sync refresh
  When I wait for mgr-sync refresh is finished
  When I execute mgr-sync "list channels -e --no-optional"
  When I execute mgr-sync "list channels -e" with user "admin" and password "admin"
```

* Execute mgr-bootstrap

```cucumber
  When I execute mgr-bootstrap "--script=bootstrap-test.sh --traditional"
```

* Execute mgr-create-bootstrap-repo

```cucumber
  When I create the bootstrap repository for "sle_minion" on the server
```

* Execute spacewalk-channel

```cucumber
  When I execute spacewalk-channel and pass "--available-channels -u admin -p admin"
  Then spacewalk-channel fails with "--add -c test_child_channel -u admin -p admin"
```

* Execute spacewalk-repo-sync

```cucumber
  When I call spacewalk-repo-sync for channel "test_base_channel" with a custom url "http://localhost/pub/TestRepoRpmUpdates/"
```


<a name="b9" />

#### Registration and channels

* Register (with ```rhnreg_ks```)

```cucumber
  When I register "ceos_minion" as traditional client
```

* Test registration (with XML-RPC)

```cucumber
  Then "ssh_minion" should not be registered
  Then "ssh_minion" should be registered
```

* Check for base channel (with User Interface)

```cucumber
  Then the system should have a base channel set
```

* Download a package from a channel

```cucumber
  When I try to download "virgo-dummy-2.0-1.1.noarch.rpm" from channel "test-channel-x86_64"
  Then the download should get a 403 response
  Then the download should get no error
```

* HTTP file transfer

```cucumber
  When I fetch "pub/bootstrap/bootstrap-test.sh" to "sle_client"
```


<a name="b10" />

#### Events

* Wait for task completion

```cucumber
  When I wait until onboarding is completed for "ceos_minion"
  When I wait until event "Package Install/Upgrade scheduled by admin" is completed
```


<a name="b11" />

#### Salt

* Control Salt service

```cucumber
  When I stop salt-minion on "ceos_minion"
  When I start salt-minion on "ceos_minion"
  When I restart salt-minion on "ceos_minion"
```

* Control Salt processes

```cucumber
  Then salt-master should be listening on public port 4505
  Then salt-api should be listening on local port 9080
```

* Wait until current Salt activity has finished

```cucumber
  When I wait until no Salt job is running on "sle_minion"
```

* Test is Salt is working with ```test.ping```

```cucumber
  Then the Salt master can reach "sle_minion"
```

* Salt keys

```cucumber
  When I accept "sle_minion" key
  When I reject "sle_minion" from the Pending section
  When I delete "ceos_minion" key in the Salt master
  When I delete "sle_minion" from the Rejected section
  When I wait until Salt master sees "sle_minion" as "rejected"
  When I wait until the list of "all" keys contains the hostname of "sle_minion"
```

* Remote commands via Salt

```cucumber
  When I enter command "ls -lha /etc"
  When I click on preview
  When I click on run
  When I expand the results for "sle_minion"
  Then I should see "SuSE-release" in the command output for "sle_minion"
```

* Salt pillars

```cucumber
  When I refresh the pillar data
  Then the pillar data for "timezone:name" should be "Etc/GMT-5" on "sle_minion"
  Then the pillar data for "timezone" should be empty on "ssh_minion"
```

* Apply the Salt highstate

```cucumber
  When I apply highstate on "sle_minion"
```


<a name="b12" />

#### XML-RPC

* Log in and out some XML-RPC namespace on the server

```cucumber
  Given I am logged in via XML-RPC system as user "admin" and password "admin"
  When I logout from XML-RPC system namespace
  Given I am logged in via XML-RPC cve audit as user "admin" and password "admin"
  When I logout from XML-RPC cve audit namespace
  Given I am logged in via XML-RPC activationkey as user "admin" and password "admin"
  Given I am logged in via XML-RPC channel as user "admin" and password "admin"
  Given I am logged in via XML-RPC user as user "admin" and password "admin"
  When I logout from XML-RPC user namespace
  Given I am logged in via XML-RPC actionchain as user "admin" and password "admin"
```

* Calling various XML-RPC methods

For example:

```cucumber
  When I call actionchain.add_package_install()
```


<a name="b13" />

#### Virtualization

* Create a test virtual machine on a given host

The virtual machine is created without SUSE Manager, directly on the virtual host
using `qemu-img` and `virt-install`

```cucumber
  When I create "test-vm" virtual machine on "virt-server"
  When I create empty "/path/to/disk.qcow2" qcow2 disk file on "virt-server"
```

* Checking the state of a virtual machine

```cucumber
  Then I should see "test-vm" virtual machine shut off on "virt-server"
  Then I should see "test-vm" virtual machine running on "virt-server"
  Then I should see "test-vm" virtual machine paused on "virt-server"
  Then I should not see a "test-vm" virtual machine on "virt-server"
```

The previous steps are just checking the virtual machine state in libvirt.
To make sure the virtual machine is completely booted:

```cucumber
  When I wait until virtual machine "test-vm" on "virt-server" is started
```

* Check the definition of a virtual machine

```cucumber
Then "test-vm" virtual machine on "virt-server" should have 1024MB memory and 2 vcpus
Then "test-vm" virtual machine on "virt-server" should have spice graphics device
Then "test-vm" virtual machine on "virt-server" should have 2 NIC using "default" network
Then "test-vm" virtual machine on "virt-server" should have a NIC with 02:34:56:78:9a:bc MAC address
Then "test-vm" virtual machine on "virt-server" should have a "disk.qcow2" scsi disk
Then "test-vm" virtual machine on "virt-server" should have a virtio cdrom
Then "test-vm" virtual machine on "virt-server" should have no cdrom
Then "test-vm" virtual machine on "virt-server" should have a "myvolume" virtio disk from pool "test-pool"
Then "test-vm" virtual machine on "virt-server" should have "/path/to/image.iso" attached to a cdrom
Then "test-vm" virtual machine on "virt-server" should boot using autoyast
Then "test-vm" virtual machine on "virt-server" should boot on hard disk at next start
Then "test-vm" virtual machine on "virt-server" should stop on reboot
Then "test-vm" virtual machine on "virt-server" should not stop on reboot at next start
```

* Remove disk images from a storage pool

```cucumber
When I delete all "test-vm.*" volumes from "default" pool on "kvm_server" without error control
```

* Add or remove virtual network or storage pools

```cucumber
When I create test-net1 virtual network on "kvm_server"
When I create test-pool1 virtual storage pool on "kvm_server"
When I delete test-net1 virtual network on "kvm_server"
When I delete test-pool1 virtual storage pool on "kvm_server"
```

* Managing storage pools

```cucumber
When I refresh the "test-pool0" storage pool of this "kvm-server"
```

* Managing virtual networks

```cucumber
When I should not see a "test-net1" virtual network on "kvm-server"
When I should see a "test-net2" virtual network on "kvm_server"
When "test-net2" on "kvm_server" should have "192.168.128.1" IPv4 address with 24 prefix
```

<a name="c" />

### Writing new steps

Here we describe only the specificities of this testsuite. For a description
of the underlying libraries, have a look at
[Capybara documentation](http://www.rubydoc.info/github/jnicklas/capybara).

#### Running remote commands

When implementing a step, to run a command on a target, use:

```ruby
$server.run("uptime")
$client.run("uptime", false)
$minion.run("uptime", true)
$minion.run("uptime", true, 300)
$client.run("uptime", false, 500, "root")
```

Arguments taken by method ```run``` are:

1. command to execute on the target system.
2. true/false, by **default** is ```true```. If the return code of the command is nonzero, then we raise an error and make the test fail. Sometimes, we expect that a command fails, or sometimes, it is not relevant whether it succeeded, so we use ```false``` in such cases.
3. timeout : **default** is 200. You can increase/decrease the timeout. You may want to use a smaller timeout, but retry several times until ```DEFAULT_TIMEOUT```.
4. user : **default** is root. It's the user that executes the command.

#### Getting the FQDN of a host

When implementing a step, to get the FQDN of the host, use:

```ruby
  STDOUT.puts $client.full_hostname
```

#### Converting between host name and target

When implementing a step, to convert a step host name into a target, use:

```ruby
  node = get_target(target)
```

#### Using cookies to store login information

It is possible to work with cookies in testsuite and use them to store login information. There are no special dependencies except `Marshal` module for Ruby.

Following code is expected to be a part of function used as step definition for user authorization:

```ruby
  # Check if there is already stored cookie session. If that is true, use it to login.
  cookie_path = '/tmp/web-session'
  if File.file?(cookie_path)
    # Load of serialized cookie data from file in binary mode
    cookie_data = Marshal.load(File.open(cookie_path, 'rb'))
    cookie_data.each do |cookie|
      page.driver.browser.manage.add_cookie(cookie)
    end
    # Reload page for login to take an effect
    page.evaluate_script 'window.location.reload()'
    # End function call if user is logged in
    next if page.all(:xpath, "//header//span[text()='#{user}']").any?
  end
  # This code is executed only in case session is not stored yet.
  cookie = page.driver.browser.manage.all_cookies
  # Serialization and save of cookies to file in binary mode
  File.open(cookie_path, 'wb') { |cookie_file|
    Marshal.dump(cookie, cookie_file)
  }
```

NOTE: This solution was tested and worked properly, but there was no time gain in comparison with old solution using capybara steps.

