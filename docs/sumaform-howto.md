# How to run the spacewalk-testsuite with sumaform.

######  Branches of spacewalt-testsuite-base:

master = Manager-Devel Head

manager30 = Manager 30

___
#### Run the normal testsuite (master)

1) Get sumaform ( https://github.com/moio/sumaform) and follow the installation doc.
2) Use this main.tf for the machines: ```https://github.com/moio/sumaform/blob/master/main.tf.libvirt-testsuite.example```
3) Once you get the machines up and running, log in on the control-node:

```ssh -t root@$YOURPREFIXcontrol-node.tf.local```

4) Run the tests with the command ```run-testsuite ``` 

5) The results are stored also in html format ```spacewalk-testsuite-base/output.html ``` 

### Run the testsuite with custom spacewalk-branch server

1) Build the infrastructure needed  ```https://github.com/moio/sumaform/blob/master/main.tf.libvirt-testsuite.example```
2) Deploy your custom  server to  the sumaform created server( see spacewalk wiki)
3) Now, just log-in on the control-node and run the testsuite:
     ```console
     ssh -t root@control-node.tf.local
     run-testsuite
     ```

### Run the testsuite with custom testsuite branch

1)  Build the infrastructure needed  ```https://github.com/moio/sumaform/blob/master/main.tf.libvirt-testsuite.example```
2)   ```console
     ssh -t root@control-node.tf.local
     rm -rF /root/spacewalk-testsuite-base
     cd /root/
     git clone YOUR_SPACEWALK_TESTSUITE_CUSTUM REPO
     run-testsuite
     ```

### Analyze log
 
 ``` scp root@control-node.tf.local:spacewalk-testsuite-base/output.html . ```
 
 ``` firefox output.html```
  to use the testing.tf
 

 
 
