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
    1. [Salt](#b10)
    1. [XML-RPC](#b11)
1. [Writing new tests](#c)
    1. [Running remote commands](#c1)
    1. [Getting the FQDN of a host](#c2)
    1. [Converting between host name and target](#c3)


<a name="a" />

### Basic notions

We use "targets" in the Ruby code to encapsulate the notion of testing hosts.
The corresponding notion in cucumber steps is "step host names".

Possible values are currently:

| Test host | Ruby target |  Bash environment variable | Step host name | Sumaform module |
| --------- | ----------- | -------------------------- | -------------- | --------------- |
| SUSE Manager server | ```$server``` | ```$TESTHOST``` |  | ```"suse_manager"``` |
| SUSE Manager proxy | ```$proxy``` | ```$PROXY``` | ```"proxy"``` | ```"suse_manager_proxy"``` |
| SLES traditional client | ```$client``` | ```$CLIENT``` | ```"sle-client"``` | ```"client"``` |
| SLES Salt minion | ```$minion``` | ```$MINION``` | ```"sle-minion"``` or ```"sle-migrated-minion"``` | ```"minion"``` |
| SLES Salt SSH minion | ```$ssh_minion``` | ```$SSHMINION``` | ```"ssh-minion"``` | ```"minion"``` |
| Cent OS Salt minion or traditional client | ```$ceos_minion``` | ```$CENTOSMINION``` | ```"ceos-minion"``` or ```"ceos-traditional-client"``` | ```"minion"``` |

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

* Go to a given page through a link

```cucumber
  When I follow "Salt"
```

* Go to Admin => Setup Wizard

```cucumber
  When I am on the Admin page
```

* Go to Admin => Organizations

```cucumber
  When I am on the Organizations page
```

* Go to Home => User Account => Credentials

```cucumber
  When I am on the Credentials page
```

* Go to Patches => Patches => Relevant

```cucumber
  When I am on the patches page
```

* Go to Salt => Keys

```cucumber
  When I go to the minion onboarding page
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
  When I go to the bootstrapping page
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
  When I am on the Active Users page
```

* Go to details of a given client

```cucumber
  When I am on the Systems overview page of this "sle-client"
```

* Go to configuration of "SUSE Test" organization

```cucumber
  When I am on the System Manager System Overview page
```

* Reload current page

```cucumber
  When I reload the page
```

<a name="b3" />

#### Texts

* Check for a given text

```cucumber
  Then I should see a "System Overview" text
  Then I should not see a "[Management]" text
  Then I should see a "Keys" text in the content area
  Then I should see "sle-minion" hostname
  Then I should not see "sle-minion" hostname
```

For a test with a regular expression, there is ```I should see a text like "..."```

* Same, but wait until text appears

```cucumber
  Then I wait until I see "Successfully bootstrapped host! " text
```

* Same, but re-issue HTTP requests to refresh the page

```cucumber
  When I wait until I see "Software Updates Available" text, refreshing the page
  When I try to reload page until contains "OpenSCAP xccdf scanning" text
  When I refresh page until see "sle-minion" hostname as text
```

(first two will be unified)


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
  When I check the row with the "sle-client" hostname
  When I check the row with the "suse_docker_admin" text
  When I check "New Test Channel" in the list
  When I uncheck "hoag-dummy-1.1-2.1" in the list
```

(will be unified)

* Make sure a checkbox is checked or not

```cucumber
  Then I should see "metadataSigned" as checked
  Then I should see "role_org_admin" as unchecked
```

* Select an item from a selection box

```cucumber
  Then I select "Mr." from "prefix"
```

* Make sure an item in a selection box is selected

```cucumber
  Then option "Mr." is selected as "prefix"
```


<a name="b6" />

#### Text input

* Type text in given input field of a form

```cucumber
  When I enter "SUSE Test Key x86_64" as "description"
  When I enter "CVE-1999-12345" as "search_string" in the content area
```

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
  When I run "rhn_check -vvv" on "sle-client"
```

* Run an arbitrary command and expect it to fail

```cucumber
  When I run "ls /srv/susemanager/salt/top.sls" on "server" without error control
  Then the command should fail
```

* Packages

```cucumber
  When I enable repository "Devel_Galaxy_BuildRepo" on this "sle-minion"
  When I disable repository "Devel_Galaxy_BuildRepo" on this "sle-minion"
  When I install pkg "virgo-dummy-1.0-1.1" on this "sle-minion"
  When I manually install the "milkyway-dummy" package in the minion
  When I remove pkg "orion-dummy" on this "sle-minion"
  When I manually remove the "milkyway-dummy" package in the minion
  When I wait for "virgo-dummy-1.0" to be installed on this "sle-minion"
  Then "man" is installed on "sle-client"
  Then "milkyway-dummy" is not installed
```

(some of these will get unified)

* Services

```cucumber
  When I shutdown the spacewalk service
  When I restart the spacewalk service
  When I wait until "salt-minion" service is up and running on "ceos-minion"
```

* Files

```cucumber
  When I fetch "pub/bootstrap/bootstrap-test.sh" to "sle-client"
  When I remove "/root/foobar" from "sle-minion"
  Then file "/root/foobar" exists on "sle-minion"
  Then file "/srv/tftpboot/pxelinux.cfg/default" exists on server
  Then file "/srv/tftpboot/pxelinux.cfg/default" contains "kernel_option=a_value"
  When I get the contents of the remote file "/etc/salt/master.d/susemanager.conf"
  Then it should contain a "rest_cherrypy:" text
```

(last ones could be unified)

* Wait for reboot to finish

```cucumber
  When I wait and check that "sle-client" has rebooted
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
  When I create the "x86_64" bootstrap-repo for "sle-minion" on the server
```

* Execute spacewalk-channel

```cucumber
  When I execute spacewalk-channel and pass "--available-channels -u admin -p admin"
  Then spacewalk-channel fails with "--add -c test_child_channel -u admin -p admin"
```

* Execute spacewalk-repo-sync

```cucumber
  When I call spacewalk-repo-sync for channel "test_base_channel" with a custom url "http://localhost/pub/TestRepo/"
```


<a name="b9" />

#### Registration and channels

* Register (with ```rhnreg_ks```)

```cucumber
  When I register "ceos-minion" as traditional client
```

* Test registration (with XML-RPC)

```cucumber
  Then "ssh-minion" is not registered in Spacewalk
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


<a name="b10" />

#### Salt

* Control Salt service

```cucumber
  When I stop salt-master
  When I start salt-master
  When I stop salt-minion on "ceos-minion"
  When I start salt-minion on "ceos-minion"
  When I restart salt-minion on "ceos-minion"
  Then salt-minion should be running on "sle-minion"
```

* Control Salt processes

```cucumber
  Then salt-master should be listening on public port 4505
  Then salt-api should be listening on local port 9080
```

* Wait until current Salt activity has finished

```cucumber
  When I wait until no Salt job is running on "sle-minion"
```

* Test is Salt is working with ```test.ping```

```cucumber
  Then the Salt master can reach "sle-minion"
```

* Salt keys

```cucumber
  When I accept "sle-minion" key
  When I reject "sle-minion" from the Pending section
  When I delete "sle-minion" from the Rejected section
  When I wait until Salt master sees "sle-minion" as "rejected"
  When I wait until onboarding is completed for "ceos-minion"
  When I wait until salt-key "mincentos" is deleted
  Then the list of the "all" keys should contain "sle-minion" hostname
  Then "sle-minion" key is "unaccepted"
```

* Remote commands via Salt

```cucumber
  When I enter command "ls -lha /etc"
  When I click on preview
  When I click on run
  When I expand the results for "sle-minion"
  Then I should see "SuSE-release" in the command output for "sle-minion"
```

* Salt states

```cucumber
  When I select the state "teststate"
```

* Salt pillars

```cucumber
  When I refresh the pillar data
  Then the pillar data for "timezone:name" should be "Etc/GMT-5" on "sle-minion"
  Then the pillar data for "timezone" should be empty on "ssh-minion"
```

* Apply the Salt highstate

```cucumber
  When I apply highstate on "sle-minion"
```

<a name="b11" />

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
  STDOUT puts $client.full_hostname
```

#### Converting between host name and target

When implementing a step, to convert a step host name into a target, use:

```ruby
  node = get_target(target)
```


