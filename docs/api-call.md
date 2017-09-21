## Testing API for SUSE Manager

This documentation will explain you some useful API calls. For a more complete description of the underlying libraries, have a look at [Capybara documentation](http://www.rubydoc.info/github/jnicklas/capybara).


### Running commands on a system

We use "targets". These look like ```$server```, ```$minion```, or ```$client```.
The fully qualified names of the associated systems are defined in
environment variables like ```TESTHOST```, ```MINION```, and ```CLIENT```.
For a complete list of the current targets, look at
[twopence_init.rb](https://github.com/SUSE/spacewalk-testsuite-base/blob/master/features/support/twopence_init.rb#L17).

```console
$server.run("uptime")
$client.run("uptime", false)
$minion.run("uptime", true)
$minion.run("uptime", true, 300)
$client.run("uptime", false, 500, "root")
```
Arguments taken by method ```run```:

1. command to execute on the target system.
2. true/false, by **default** is ```true```. If the return code of the command is nonzero, then we raise an error and make the test fail. Sometimes, we expect that a command fails, or sometimes, it is not relevant whether it succeeded, so we use ```false``` in such cases.
3. timeout : **default** is 200. You can increase/decrease the timeout. You may want to use a smaller timeout, but retry several times until ```DEFAULT_TIMEOUT```.
4. user : **default** is root. It's the user that executes the command.


### Navigating through the SUSE manager Web interface

* if you want to go to a given page, just add this to your feature:

```
   Given I am authorized
   When I follow "Salt"
```

* Check that a given text exists in the Web page:

```
 Then I should see a "System Overview" text
 Then I should see a "INSERT YOUR TEXT HERE" text
```

* Click on a given link:

```
 And I click on "Finish"
```

* Type text in given field

```
 When I enter "SUSE Test Key x86_64" as "description"
```
