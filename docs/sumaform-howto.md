# How to run the spacewalk-testsuite with sumaform.

#### Run it

1) Get sumaform ( https://github.com/moio/sumaform) and follow the installation doc.

2) Use the libvirt-testing.-example and run it with terraform. https://github.com/moio/sumaform/blob/master/main.tf.libvirt-testsuite.example

2a) use a prefix for your machine, edit this in the main.tf

3) Once you have all the machines, and terraform setup is finish ( 1 client, 1 minion(sle), 1 suma-server, 1 control-node)
   
   log into control-node : ``` ssh root@control-node.tf.local ```
   
4) use the command run-testsuite for running the whole testsuite.

 If you want to run specific feature, just use the run.sh script, and use cucumber features/myfeature at the end.
   
   
### Analyze log:
 
 ``` scp root@control-node.tf.local:spacewalk-testsuite-base/output.html . ```
 
 ``` firefox output.html```
  to use the testing.tf
 
 
### Where live the source code that sumaform is using for cucumber (this will be merged on master soon)
 
 https://github.com/SUSE/spacewalk-testsuite-base/tree/slenkins
 
 
 
