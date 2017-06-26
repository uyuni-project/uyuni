# How to run the spacewalk-testsuite with sumaform

## Run the testsuite (master)

1. Get sumaform ( https://github.com/moio/sumaform) and follow the installation doc.
2. Use testsuite `main.tf` for the machines: ```https://github.com/moio/sumaform/blob/master/main.tf.libvirt-testsuite.example```
3. Once you get the machines up and running, log in on the control-node:

    ssh -t root@$YOURPREFIXcontrol-node.tf.local

4. Run the tests with the command ```run-testsuite ``` 
5. The results are stored in html format ```spacewalk-testsuite-base/output.html ``` 

## Run the testsuite with custom testsuite branch

1.  Build the infrastructure needed  ```https://github.com/moio/sumaform/blob/master/main.tf.libvirt-testsuite.example```
2.   
    ssh -t root@control-node.tf.local
    rm -rF /root/spacewalk-testsuite-base
    cd /root/
    git clone YOUR_SPACEWALK_TESTSUITE_CUSTOM REPO
    run-testsuite

## View testsuite logs

* Copy testsuite output to your local machine:
``` scp root@control-node.tf.local:spacewalk-testsuite-base/output.html . ```
* Open it with your preferred browser
``` firefox output.html```
