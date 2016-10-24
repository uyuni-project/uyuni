# How to run the spacewalk-testsuite with sumaform.

## WIP: for moment, the working version of sumaform+cucumber is there:

 - https://github.com/MalloZup/sumaform
 
Once the PR will merged upstream  https://github.com/moio/sumaform), you can use the upstream.

the libvirt-testing example, is in my repo
 (The file is for moment here, this not compatible already with the newest version of sumaform, due to refractoring
https://github.com/MalloZup/sumaform


#### Run it

1) Get sumaform ( https://github.com/moio/sumaform)



2) Use the libvirt-testing.-example and run it with terraform. 


3) Once you have all the machines, and terraform setup is finish ( 1 client, 1 minion(sle), 1 suma-server3.0, 1controlnode)
   
   log into control-node : ``` ssh root@control-node.tf.local```
   
 
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
   
 #### Analyze log:
 
 the log you get, are in output.html
