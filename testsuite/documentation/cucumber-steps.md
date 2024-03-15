# Writing your own Cucumber steps, and reusing Cucumber steps

## Table of Contents

- [Basic notions](#basic-notions)
- [Reusing old steps](#reusing-old-steps)
  - [Logging in and out](#logging-in-and-out)
  - [Navigating through pages](#navigating-through-pages)
  - [Texts](#texts)
  - [Links](#links)
  - [Buttons](#buttons)
  - [Text input](#text-input)
  - [Operating system](#operating-system)
  - [Uyuni utilities](#uyuni-utilities)
  - [Registration and channels](#registration-and-channels)
  - [Events](#events)
  - [Salt](#salt)
  - [XML-RPC or HTTP API](#xml-rpc-or-http-api)
  - [Virtualization](#virtualization)
- [Writing new steps](#writing-new-steps)
  - [Running remote commands](#running-remote-commands)
  - [Getting the FQDN of a host](#getting-the-fqdn-of-a-host)
  - [Converting between host name and target](#converting-between-host-name-and-target)
  - [Using cookies to store login information](#using-cookies-to-store-login-information)

## Basic notions

We use "targets" in the Ruby code to encapsulate the notion of testing hosts.
The corresponding notion in Cucumber steps is "step host names".

Possible values are currently:

| Test host                       | Ruby target              | Bash environment variable                                | Step host name           | sumaform module            |
|---------------------------------|--------------------------|----------------------------------------------------------|--------------------------|----------------------------|
| Uyuni server                    | ```$server```            | ```$SERVER```                                            |                          | ```"suse_manager"```       |
| Uyuni proxy                     | ```$proxy```             | ```$PROXY```                                             | ```"proxy"```            | ```"suse_manager_proxy"``` |
| SLES Salt minion                | ```$minion```            | ```$MINION```                                            | ```"sle_minion"```       | ```"minion"```             |
| SLES Docker and Kiwi build host | ```$build_host```        | ```$BUILD_HOST```                                        | ```"build_host"```       | ```"build_host"```         |
| Monitoring Server               | ```$monitoring_server``` | ```$MONITORING_SERVER```                                 | ```"monitoring_server``` | ```"minion"```             |
| SLES Salt SSH minion            | ```$ssh_minion```        | ```$SSH_MINION```                                        | ```"ssh_minion"```       | ```"minion"```             |
| Red Hat-like Salt minion        | ```$rhlike_minion```     | ```$RHLIKE_MINION```                                     | ```"rhlike_minion"```    | ```"minion"```             |
| Debian-like Salt minion         | ```$deblike_minion```    | ```$DEBLIKE_MINION```                                    | ```"deblike_minion"```   | ```"minion"```             |
| PXE-boot minion                 | None                     | ```$PXEBOOT_MAC```                                       | ```"pxeboot_minion"```   | ```"pxeboot"```            |
| KVM virtual host minion         | ```$kvm_server```        | ```$VIRTHOST_KVM_URL``` and ```$VIRTHOST_KVM_PASSWORD``` | ```"kvm_server"```       | ```"virthost"```           |
| Salt bundle migration minion (nested VM)  | ```$salt_migration_minion```      | ```$MIN_NESTED```                                        | ```"salt_migration_minion"```       |      ```"virthost"```       |

These names are such for historical reasons and might be made better in the future.

## Reusing old steps

These are only the most important generic steps. For more specialized steps,
please grep in the source code.

### Logging in and out

* Go to the login page

```gherkin
  When I go to the home page
```

* Sign in as user "testing" with password "testing"

```gherkin
  Given I am authorized
```

* Sign in as a given user with given password

```gherkin
  Given I am authorized as "admin" with password "admin"
```

* Check we are signed in

```gherkin
  Then I should be logged in
```

To check for the initial log in, prefer ```Then I am logged in```.

* Sign out

```gherkin
  Given I sign out
```

* Check we are signed out

```gherkin
  Then I should not be authorized
```

### Navigating through pages

* Go to a given page through a the left menu tree with the complete menu path

```gherkin
  When I follow the left menu "Systems > System List > System Currency"
```

* Go to Admin => Setup Wizard => Products

```gherkin
  Given I am on the Products page
```

* Go to Admin => Organizations

```gherkin
  Given I am on the Organizations page
```

* Go to Patches => Patches => Relevant

```gherkin
  When I am on the patches page
```

* Go to Salt => Keys

```gherkin
  When I follow the left menu "Salt > Keys"
```

* Go to Systems => Autoinstallation => Overview

```gherkin
  When I am on Autoinstallation Overview page
```

* Go to Systems => Autoinstallation => Profiles => Upload Kickstart/AutoYaST File

```gherkin
  When I am on the Create Autoinstallation Profile page
```

* Go to Systems => Bootstrapping

```gherkin
  When I follow the left menu "Systems > Bootstrapping"
```

* Go to Systems => Systems

```gherkin
  When I click Systems, under Systems node
```

* Go to Systems => System Groups

```gherkin
  When I am on the groups page
```

* Go to Systems => System Set Manager

```gherkin
  When I am on the System Manager System Overview page
```

* Go to Systems => System Set Manager => Overview

```gherkin
  When I follow the left menu "Systems > System Set Manager > Overview"
```

* Go to Users => Users list => Active

```gherkin
  When I follow the left menu "Users > User List > Active"
```

* Go to details of a given client

```gherkin
  When I am on the Systems overview page of this "sle_minion"
```

* Go to a tab page of a given client

```gherkin
  When I am on the "Virtualization" page of this "sle_minion"
```

* Go to configuration of "SUSE Test" organization

```gherkin
  When I am on the System Manager System Overview page
```

* Test the last opened window

```gherkin
  And I switch to last opened windo
```

### Texts

* Check for a given text

```gherkin
  Then I should see a "System Overview" text
  Then I should not see a "[Management]" text
  Then I should see a "Keys" text in the content area
  Then I should see "sle_minion" hostname
  Then I should not see "sle_minion" hostname
  Then table row for "test-net1" should contain "running"
```

For a test with a regular expression, there is ```I should see a text like "..."```

* Same, but wait until text appears

```gherkin
  When I wait until I see "Successfully bootstrapped host!" text
  When I wait until I do not see "Loading..." text
  When I wait at most 360 seconds until I see "Product Description" text
  When I wait at most 600 seconds until the tree item "test-pool0" contains "inactive" text
  When I wait until table row for "test-net1" contains "running"
```

* Same, but re-issue HTTP requests to refresh the page

```gherkin
  When I wait until I see "Software Updates Available" text, refreshing the page
  When I wait until I do not see "Apply highstate scheduled by admin" text, refreshing the page
  When I wait until I see the name of "sle_minion", refreshing the page
  When I wait until I do not see the name of "sle_minion", refreshing the page
  When I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows
  When I refresh page until I see "sle_minion" hostname as text
```

(last one should probably be renamed - it looks in the contents area of the page)

* Wait until a tree item has no sub list

```gherkin
  When I wait at most 600 seconds until the tree item "test-pool0" has no sub-list
```

### Links

* Check for a link

```gherkin
  Then I should see a "Create Config Channel" link
  Then I should see a "Managed Systems" link in the left menu
  Then I should see a "Details" link in the content area
  Then I should not see a "norole" link
```

* Click on a given link

```gherkin
  When I follow "Dependancies"
  When I follow first "Schedule System Reboot"
  When I click on "Use in SSM" for "newgroup"
```

### Buttons

* Click on a given button

```gherkin
  When I click on "Schedule"
  When I click on "Refresh" in tree item "test-pool0"
```

The button can be identified by id, value, title, text content, or alt of image.
If several buttons have been found, this step picks the first one.

* Click on a given radio button

```gherkin
  When I check radio button "schedule-by-action-chain"
  When I choose ";"
```

The radio button can be identified by name, id or label text.

(will be unified)

* Make sure a radio button is checked

```gherkin
  Then radio button "radio-comma" should be checked
```

* Click on a given check box

```gherkin
  When I check "manageWithSSH"
  When I uncheck "role_org_admin"
  When I check "Container Build Host" if not checked
```

The check box can be identified by name, id or label text.

* Click on a check box in a table

```gherkin
  When I check the row with the "virgo-dummy-3456" link
  When I check the row with the "suse_docker_admin" text
  When I check the "sle_minion" client
  When I check "New Test Channel" in the list
  When I uncheck "hoag-dummy-1.1-1.1" in the list
```

(will be unified)

* Make sure a checkbox is checked or not

```gherkin
  Then I should see "metadataSigned" as checked
  Then I should see "role_org_admin" as unchecked
```

* Select an item from a selection box

```gherkin
  When I select "Mr." from "prefix"
  When I select the hostname of "proxy" from "proxies"
```

* Wait for a selection box to contain an item

```gherkin
  When I wait until option "dir" appears in list "type"
```

* Make sure an item in a selection box is selected

```gherkin
  Then option "Mr." is selected as "prefix"
```

* Check for a button in a given row

```gherkin
  When I wait until table row for "test-vm" contains button "Resume"
  When I wait at most 300 seconds until table row for "test-vm" contains button "Resume"
  When I wait at most 600 seconds until the tree item "test-pool1" contains "test-pool1 is started automatically" button
```

### Text input

* Type text in given input field of a form

```gherkin
  When I enter "SUSE Test Key x86_64" as "description"
  When I enter "SUSE Test Key x86_64" as "description" text area
  When I enter "CVE-1999-12345" as "search_string" in the content area
  When I enter the hostname of "proxy" as "hostname"
```

Note that the text area variant handles the new lines characters while the others don't.

* Make sure a text is in a given input field of a form. The identifier can be the name or ID of the element, but it's always the HTML label, not the shown text.

```gherkin
  Then I should see "20" in field identified by "usageLimit"
```

* Type text in the text editor

```gherkin
  When I enter "MGR_PROXY=yes" in the editor
```

### Operating system

* Run an arbitrary command and expect it to succeed

```gherkin
  When I run "zypper up" on "sle_minion"
  When I run "apt update" on "deblike_minion" with logging
```

* Run an arbitrary command and expect it to fail

```gherkin
  When I run "ls /srv/susemanager/salt/top.sls" on "server" without error control
  Then the command should fail
```

* Repositories

```gherkin
  When I enable repository "Test-Packages_Pool" on this "sle_minion"
  When I disable repository "Test-Packages_Pool" on this "sle_minion"
```

* Packages

```gherkin
  When I install package "virgo-dummy-1.0-1.1" on this "sle_minion"
  When I remove package "orion-dummy" from this "sle_minion"
  When I refresh packages list via spacecmd on "sle_minion"
  When I wait for "virgo-dummy-1.0" to be installed on "sle_minion"
  When I wait for "milkyway-dummy" to be uninstalled on "sle_minion"
  When I wait until refresh package list on "sle_minion" is finished
  When I wait until package "virgo-dummy" is installed on "sle_minion" via spacecmd
  Then "man" should be installed on "sle_minion"
  Then "milkyway-dummy" should not be installed on "sle_minion"
  Then spacecmd should show packages "virgo-dummy-1.0 milkyway-dummy" installed on "sle_minion"
```

* Services

```gherkin
  When I shutdown the spacewalk service
  When I restart the spacewalk service
  When I wait until "salt-minion" service is up and running on "rhlike_minion"
  Then service "bind" is enabled on "proxy"
  And service "dhcpd" is running on "proxy"
  When I restart the "bind" service on "sle_minion"
  And I start the "apache2" service on "proxy"
  And I stop the "apache2" service on "proxy"
  And I reload the "apache2" service on "proxy"
  And I enable the "apache2" service on "proxy"
  And I disable the "apache2" service on "proxy"
```

* File removal

```gherkin
  When I remove "/root/foobar" from "sle_minion"
  When I destroy "/var/lib/pgsql/data/pg_xlog" directory on server
  When I destroy "/etc/s-mgr" directory on "sle_minion"
```

* File existence

```gherkin
  When I wait until file "/root/foobar" exists on "sle_minion"
  Then file "/etc/mgr-test-file.cnf" should exist on "sle_minion"
  When I wait until file "/srv/tftpboot/pxelinux.cfg/default" exists on server
  Then file "/srv/susemanager/salt/manager_org_1/mixedchannel/init.sls" should exist on server
  Then file "/srv/susemanager/salt/manager_org_1/s-mgr/config/init.sls" should not exist on server
  Then file "/var/lib/libvirt/images/test-pool0" should not exist on "kvm_server"
  Then file "/var/lib/libvirt/images/test-pool1" should have 755 permissions on "kvm_server"
```

* File contents

```gherkin
  When I wait until file "/srv/tftpboot/pxelinux.cfg/default" contains "kernel_option=a_value" on server
  Then file "/etc/mgr-test-file.cnf" should contain "MGR_PROXY=yes" on "sle_minion"

  When I get the contents of the remote file "/etc/salt/master.d/susemanager.conf"
  Then it should contain a "rest_cherrypy:" text
```

(last ones could be unified)

* Wait for reboot to finish

```gherkin
  When I wait and check that "sle_minion" has rebooted
```

### Uyuni utilities

* Execute mgr-sync

```gherkin
  When I execute mgr-sync refresh
  When I wait for mgr-sync refresh is finished
  When I execute mgr-sync "list channels -e --no-optional"
  When I execute mgr-sync "list channels -e" with user "admin" and password "admin"
```

* Execute mgr-bootstrap to create a bootstrap script

```gherkin
  When I execute mgr-bootstrap "--activation-keys=1-AK-KEY-NAME --script=bootstrap-test.sh"
```

* Execute mgr-create-bootstrap-repo

```gherkin
  When I create the bootstrap repository for "sle_minion" on the server
```

* Execute spacewalk-repo-sync

```gherkin
  When I call spacewalk-repo-sync for channel "test_base_channel" with a custom url "http://localhost/pub/TestRepoRpmUpdates/"
```

### Registration and channels

* Test registration (with API)

```gherkin
  Then "ssh_minion" should not be registered
  Then "ssh_minion" should be registered
```

* Check for base channel (with User Interface)

```gherkin
  Then the system should have a base channel set
```

* Download a package from a channel

```gherkin
  When I try to download "virgo-dummy-2.0-1.1.noarch.rpm" from channel "test-base-channel-x86_64"
  Then the download should get a 403 response
  Then the download should get no error
```

* HTTP file transfer

```gherkin
  When I fetch "pub/bootstrap/bootstrap-test.sh" to "sle_minion"
```

### Events

* Wait for task completion

```gherkin
  When I wait until onboarding is completed for "rhlike_minion"
  When I wait until event "Package Install/Upgrade scheduled by admin" is completed
```

### Salt

* Control Salt service

```gherkin
  When I stop salt-minion on "rhlike_minion"
  When I start salt-minion on "rhlike_minion"
  When I restart salt-minion on "rhlike_minion"
```

* Control Salt processes

```gherkin
  Then salt-master should be listening on public port 4505
  Then salt-api should be listening on local port 9080
```

* Wait until current Salt activity has finished

```gherkin
  When I wait until no Salt job is running on "sle_minion"
```

* Test is Salt is working with ```test.ping```

```gherkin
  Then the Salt master can reach "sle_minion"
```

* Salt keys

```gherkin
  When I accept "sle_minion" key
  When I reject "sle_minion" from the Pending section
  When I delete "rhlike_minion" key in the Salt master
  When I delete "sle_minion" from the Rejected section
  When I wait until Salt master sees "sle_minion" as "rejected"
  When I wait until the list of "all" keys contains the hostname of "sle_minion"
```

* Remote commands via Salt

```gherkin
  When I enter command "ls -lha /etc"
  When I click on preview
  When I click on run
  When I expand the results for "sle_minion"
  Then I should see "SuSE-release" in the command output for "sle_minion"
```

* Salt pillars

```gherkin
  When I refresh the pillar data
  Then the pillar data for "timezone:name" should be "Etc/GMT-5" on "sle_minion"
  Then the pillar data for "timezone" should be empty on "ssh_minion"
```

* Apply the Salt highstate

```gherkin
  When I apply highstate on "sle_minion"
```

### XML-RPC or HTTP API

* Calling various API methods

For example:

```gherkin
  When I call actionchain.add_package_install()
```

### Virtualization

* Create a test virtual machine on a given host

The virtual machine is created without Uyuni, directly on the virtual host
using `qemu-img` and `virt-install`

```gherkin
  When I create a leap virtual machine named "test-vm" without cloudinit on "virt-server"
  When I create a sles virtual machine named "test-vm" with cloudinit on "virt-server"
  When I create empty "/path/to/disk.qcow2" qcow2 disk file on "virt-server"
```

* Checking the state of a virtual machine

```gherkin
  Then I should see "test-vm" virtual machine shut off on "virt-server"
  Then I should see "test-vm" virtual machine running on "virt-server"
  Then I should see "test-vm" virtual machine paused on "virt-server"
  Then I should not see a "test-vm" virtual machine on "virt-server"
```

The previous steps are just checking the virtual machine state in libvirt.
To make sure the virtual machine is completely booted:

```gherkin
  When I wait until virtual machine "test-vm" on "virt-server" is started
```

* Check the definition of a virtual machine

```gherkin
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

* Stop a virtual machine

```gherkin
  When I stop the virtual machine named "test-vm" on "kvm_server"
```

* Delete a virtual machine

```gherkin
  When I delete the virtual machine named "test-vm" on "kvm_server"
```

* Remove disk images from a storage pool

```gherkin
When I delete all "test-vm.*" volumes from "default" pool on "kvm_server" without error control
```

* Add or remove virtual network or storage pools

```gherkin
When I create test-net1 virtual network on "kvm_server"
When I create test-pool1 virtual storage pool on "kvm_server"
When I delete test-net1 virtual network on "kvm_server"
When I delete test-pool1 virtual storage pool on "kvm_server"
```

* Managing storage pools

```gherkin
When I refresh the "test-pool0" storage pool of this "kvm-server"
```

* Managing virtual networks

```gherkin
When I should not see a "test-net1" virtual network on "kvm-server"
When I should see a "test-net2" virtual network on "kvm_server"
When "test-net2" on "kvm_server" should have "192.168.128.1" IPv4 address with 24 prefix
```

## Writing new steps

Here we describe only the specifics of this test suite. For a description
of the underlying libraries, have a look at
[Capybara documentation](http://www.rubydoc.info/github/jnicklas/capybara).

### Running remote commands

When implementing a step, to run a command on a target, use:

```ruby
get_target('server').run("uptime")
get_target('sle_minion').run("uptime", check_errors: false)
get_target('sle_minion').run("uptime", check_errors: true)
get_target('sle_minion').run("uptime", check_errors: true, timeout: 300)
get_target('sle_minion').run("uptime", check_errors: false, timeout: 500, user: 'root')
```

Arguments taken by method ```run``` are:

1. command to execute on the target system.
2. true/false, by **default** is ```true```. If the return code of the command is nonzero, then we raise an error and
make the test fail. Sometimes, we expect that a command fails, or sometimes, it is not relevant whether it succeeded,
so we use ```false``` in such cases.
3. timeout : **default** is 200. You can increase/decrease the timeout. You may want to use a smaller timeout, but
retry several times until ```DEFAULT_TIMEOUT```.
4. user : **default** is root. It's the user that executes the command.

### Getting the FQDN of a host

When implementing a step, to get the FQDN of the host, use:

```ruby
  STDOUT.puts get_target('sle_minion').full_hostname
```

### Converting between host name and target

When implementing a step, to convert a step host name into a target, use:

```ruby
  node = get_target(target)
```

### Using cookies to store login information

It is possible to work with cookies in test suite and use them to store login information. There are no special
dependencies except `Marshal` module for Ruby.

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

TIP: We still can play with timeout value in our test environment, if necessary. See default value [here](https://github.com/uyuni-project/uyuni/blob/master/web/conf/rhn_web.conf#L29-L31)
