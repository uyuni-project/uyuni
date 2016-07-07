# What is different between the master suite and the slenkins suite

Basically the slenkins-suite use twopence, this allow to define targets for remote command execution.

In the SLEnkins suite, is added in Gemfile the requirement Twopence, and in the support directory,
twopence.init.rb
https://github.com/SUSE/spacewalk-testsuite-base/blob/slenkins/features/support/twopence_init.rb#L4

```
$server_ip = ENV['TESTHOST']
$client_ip = ENV['CLIENT']

$client = Twopence::init("ssh:#{$client_ip}")
$server = Twopence::init("ssh:#{$server_ip}")
```

In this snippet, we define 2 target, that use ssh as protocol for communication. (virtio is another protocl, but for cloud is not already supported).

The machines(vms), network, disk-space, ram ares setted up dinamically by slenkins-engine, so nothing is hardcoded. (and this parameters can be setted on the node file)
https://github.com/SUSE/spacewalk-testsuite-base/blob/slenkins/slenkins/tests-control/nodes


Some changes in testcases of cucumber were added, because in SLEnkins suite the cucumber features and step_definitions resides all in the control_node.
The control-node is a systemd-container. SLE-12-sp1 that execute the suite on system under tests SUT. (these are twopence targets)

At moment, we have 3 machines: server, client (is without salt), minion (salt client).

For a quick design explanation, look here:
https://github.com/okirch/susetest/blob/master/doc/susetest_design.jpg


An example for a change is here: **features/step_definitions/register_client_steps.rb**

Master
```
Given(/^I am root$/) do
  uid = `id -u`
  if ! $?.success? || uid.to_i != 0
    raise "You are not root!"
  end
  if $myhostname == "linux"
    raise "Invalid hostname"
  end
end
```
SLEnkins
```
Given(/^I am root$/) do
  user, local, remote, code = $client.test_and_store_results_together("whoami", "root", 500)
  if  user.strip != "root"
    puts  "user on client was #{user}" 
    raise "You are not root!"
  end
end
```

So the main difference, that is overall on the features/step_definitions, is :
1) Twopence commands, that give a target -->
This imply No more commands without Targets specification. (it make also the code more readeable, and ordered, since we know where is executed (on server, client, minion)


1) ```user, local, remote, code = $client.test_and_store_results_together("whoami", "root", 500)``

user is th output
code is the retcode from command.
the other 2 vars, are for debugging twopence and for testing we can avoid to use them.

You can specify easyly target ( this are defined by twopence init in support)
```user, local, remote, code = $server.test_and_store_results_together("whoami", "root", 500)``

Root is the user that will execute the cmd. You can specify others.
500 is the timeout

In other tests, we have like this:

```
out, local, remote, code = $client.test_and_store_results_together("yum clean all", "root", "600")
fail if code != 0
```






