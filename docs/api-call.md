## Testing-Api cucumber/spacewalk

This documentation, will explain you some usefull api-call.

### Running commands on a system : 

At moment, we have 3 official tergets: $server, $minion, $client. ( this are sles systems).


```console
$server.run("uptime")
$client.run("uptime", false)
$minion.run("uptime", true)
$minion.run("uptime", true, 300)
$client.run("uptime", false, 500, "root")
```
Arguments taken by method *run*

1: command to execute on the target system

2: true/false, by **default** is true. If the returncode of the command is !=0 then we raise an error and make failing the test. 
   Sometimes we expect that a command fail, or is not relevant that is succedded, so we use *false* in this cae

3: timeout : **default** is 200 . you can increase/decrease the timeout.

4: user : **default** root. user that execute cmd

### Accessing/Navigation through the Suse-manager-Server.


* if you want to go to a specif page, just add this in your feature :

```
   Given I am authorized
   When I follow "Salt"
```

* Check that a specific word exist on the webpage:

```
 Then I should see a "System Overview" text
 Then I should see a "INSERT YOUR TEXT HERE" text
```

* Click a specific link:

```
 And I click on "Finish"
```
