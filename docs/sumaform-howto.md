# How to run the spacewalk-testsuite with sumaform.

## WIP: for moment, the only working version of sumaform + testing(cucumber) is there:

 - https://github.com/MalloZup/sumaform
 
Once the PR will merged upstream  https://github.com/moio/sumaform), you can use the upstream.

the libvirt-testing example, is in my repo https://github.com/MalloZup/sumaform/blob/master/main.tf.libvirt-testing.example

 (The file is for moment here, because the upstream was refractored, and my version is not compatible already with the newest version of sumaform, due to refractoring-variables issues).
 


#### Run it

1) Get sumaform ( https://github.com/moio/sumaform)



2) Use the libvirt-testing.-example and run it with terraform. 


3) Once you have all the machines, and terraform setup is finish ( 1 client, 1 minion(sle), 1 suma-server, 1 control-node)
   
   log into control-node : ``` ssh root@control-node.tf.local ```
   
 
4) before executing the rake comand(also running the cucumber-suite) export the ip-adress of machines.

   If you use the libvirt-testing.example, take this script then :
   https://github.com/SUSE/spacewalk-testsuite-base/blob/slenkins/run.sh
   
   ```console
   cd root/spacewalk-testsuite-base
   bash run.sh
   ```
   run.sh is equivalent too:
   ```
   export MINION=minion12sp1.tf.local
   export TESTHOST=suma3pg.tf.local
   export CLIENT=clisles12sp1.tf.local
   export BROWSER=phantomjs
   rake
   ```
   
   
### Analyze log:
 
 ``` scp root@control-node.tf.local:spacewalk-testsuite-base/output.html . ```
 
 ``` firefox output.html```
 
 
 
 
### Considerations:

 
 Sumaform is a great tool for deploying suse-manager server.
 
 We have to differenciate between deploy and testing environments :
 
**Deploy**
 
 - if you want to deploy, 4 client against a server, and this client should be registered to the client, then you want to make something else, that's fine.
 
**Testing env.**
 
 - in the testing environments, we don't register clients/minion against a server( this is done by the testsuite cucumber as part of a test)
 - addionaly , testing machines, has some extra packages that we use in the cucumber suite.
 
 **Resuming**
 So if you want to do deployment, and check/reproduce something, than use the deployment file.
 
 If you want to improve the tests-suite, or have the machines after a regression founded, then you have to use the testing.tf
 
 
### Where live the source code that sumaform is using for cucumber : 
 
 https://github.com/SUSE/spacewalk-testsuite-base/tree/slenkins
 
 
 
