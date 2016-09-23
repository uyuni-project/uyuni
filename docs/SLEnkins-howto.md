# SLEnkins Spacewalk Suite HOWTO

## Where is the code of the slenkins-cucumber ?
branch slenkins
https://github.com/SUSE/spacewalk-testsuite-base/tree/slenkins

## The difference between master suite and the SLEnkins suite.
[What has changed](changes.md)

## Official and full documentation about slenkins:
http://slenkins.suse.de/doc/

### Installation of SLEnkins in your local machine/server.

1. Add the repos and install required packages:
   
   For openSUSE_Leap_42.1 (modify it with your distro version):
   
   ```
   # zypper ar http://download.suse.de/ibs/Devel:/SLEnkins/openSUSE_Leap_42.1/Devel:SLEnkins.repo
   # zypper ref
   # zypper in slenkins-engine-vms slenkins
   ```

2. Install systemd-jail (NOTE: *NOT* from root):

   ```
   $ /usr/lib/slenkins/init-jail/init-jail.sh --local --with-susemanager
   ```

   A `jail` directory will be created inside the current directory (let's suppose `/home/mbologna/jail` from now on).
   
3. Edit `libvirt` config (`/etc/libvirt/libvirtd.conf`) so that it contains the following lines:

   ```
   listen_tcp = 1
   listen_addr = "127.0.0.1"
   auth_tcp = "none"
   ```
   
   and then restart `libvirt`:
   
   ```
   # systemctl restart libvirtd.service
   ```

4. Once the jail is done, test the `virsh` functionality:
 
   ```
      # systemd-nspawn -D $jail_path
      # su - slenkins
      $ virsh list
   ```
   
   At this point, you should see an empty list of virtual machines.

5. Modify `/etc/fstab` and add:
   
   ```
   /home/mbologna/jail/var/tmp/slenkins   /var/tmp/slenkins   none   bind,user       0 0
   /home/mbologna/jail/var/tmp/build-root   /var/tmp/build-root   none   bind,user       0 0
   ```
   
   These shared directories enable SLEnkins to share test workspace and locally built packages between your workstation and the jail. 

6. (Optional) Launch `hello world` tests:
   
   ```
   $ slenkins-vms.sh -j -i sut=openSUSE_42.1-x86_64-default tests-helloworld
   ```
   
   For explanation about this command and flags, refer to:
   http://slenkins.suse.de/doc/REFERENCE-slenkinsvms.txt
   
   basically -j say to run in the jail, -i = IMAGE for the vms created by virsh.

### Usage

Run the Spacewalk Suite on you local machine:

```
slenkins-vms.sh -j -i server=SLE_12_SP1-x86_64-default -i client=SLE_12_SP1-x86_64-default -i minion=SLE_12_SP1-x86_64-default tests-suse-manager
```

Basically, you can define your machine type, by chosing from avaible images:

the command give you the overview of images you can use:
``` slenkins-vms.sh -a ```

http://slenkins.suse.de/images/

The default is an image without graphic (gnome), minimal pattern.

You can combine the run the testsuite, with a different matrix of images (FAMILY like SP1 or ARCH like ppc64le)

**IF the testsuite fail, the vms created are conserved, so you can login into it. If testsuite success, the machines are automatically destroyed.**

for the spacewalk suite the pwd is the GALAXY standard.

## Current Results after the suite is running:

For getting the html file with results, have a look at ``/home/slenkins/$MY_JAIL/var/tmp/slenkins/workspace/cucumber/output.html`` 

At moment, i'm working to fix this scenarios :


cucumber features/test_config_channel.feature:99 # Scenario: Import the changed file
cucumber features/test_config_channel.feature:115 # Scenario: Import the changed file
cucumber features/test_config_channel.feature:131 # Scenario: Copy Sandbox file to Centrally-Managed
cucumber features/test_config_channel.feature:161 # Scenario: Change one local file and compare multiple (bsc#910243, bsc#910247)
cucumber features/action_chain.feature:61 # Scenario: I add a config file deployment to the action chain

cucumber features/action_chain.feature:120 # Scenario: I execute the action chain from the web ui
cucumber features/spacewalk-channel.feature:13 # Scenario: add an invalid child channel (bnc#875958)
cucumber features/lock_packages_on_client.feature:35 # Scenario: Unlock a package on the client
cucumber features/lock_packages_on_client.feature:78 # Scenario: Mix package locks and unlock events
cucumber features/systemspage2.feature:31 # Scenario: Completeness of the Virtual System table
cucumber features/nagios.feature:10 # Scenario: Check pending patches for host
cucumber features/nagios.feature:14 # Scenario: Check pending patches for non-existing host
cucumber features/bare-metal-test.feature:102 # Scenario: Disable Bare-metal discovery
cucumber features/ssh_push.feature:34 # Scenario: Register this client for SSH push via tunnel
cucumber features/ssh_push.feature:38 # Scenario: Check this client's contact method
cucumber features/xmlrpc_api.feature:6 # Scenario: Public API test
cucumber features/smdba.feature:41 # Scenario: Check database utilities
cucumber features/security.feature:37 # Scenario: Do not use jsession id
cucumber features/salt.feature:24 # Scenario: The minion appears in the master's incoming queue
cucumber features/salt_minions_page.feature:17 # Scenario: Minion is visible in the Pending section
cucumber features/salt_minions_page.feature:24 # Scenario: Reject and delete the pending minion key
cucumber features/salt_minions_page.feature:33 # Scenario: Accepted minion shows up as a registered system
cucumber features/salt_minions_page.feature:45 # Scenario: The minion communicates with the master

360 scenarios (23 failed, 337 passed)



### FAQ

http://slenkins.suse.de/doc/FAQ.txt

For any question, feel free to join the irc channel:
`#slenkins` on SUSE IRC server


