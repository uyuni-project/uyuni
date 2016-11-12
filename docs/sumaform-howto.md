# How to run the spacewalk-testsuite with sumaform.

#### Run it

1) Get sumaform ( https://github.com/moio/sumaform) and follow the installation doc.

Follow the documentation here : 

https://github.com/moio/sumaform/blob/master/README_ADVANCED.md#cucumber-testsuite
   
   
### Analyze log:
 
 ``` scp root@control-node.tf.local:spacewalk-testsuite-base/output.html . ```
 
 ``` firefox output.html```
  to use the testing.tf
 
 
### Where live the source code that sumaform is using for cucumber (this will be merged on master soon)
 
 https://github.com/SUSE/spacewalk-testsuite-base/tree/slenkins
 
 
 
