# Run the spacewalk-testsuite with sumaform

1. Install [sumaform](https://github.com/moio/sumaform)
2. Rename `main.tf.libvirt-testsuite.example` to `main.tf` in order to use [already provisioned machines](https://github.com/moio/sumaform/blob/master/main.tf.libvirt-testsuite.example) for running the testsuite.
3. Once you get the machines up and running, log in the control node:
```
$ ssh -t root@${YOURPREFIX}control-node.tf.local
```
4. Run the tests with the command:
```
# run-testsuite
```
5. Copy testsuite output to your local machine:
```
$ scp root@control-node.tf.local:spacewalk-testsuite-base/output.html .
```
6. Open it with your preferred browser:
```
$ firefox output.html
```

## Run a custom testsuite branch

Substitute the `spacewalk-testsuite-base` repo with your custom repo:
```
$ ssh -t root@control-node.tf.local
# cd /root
# rm -rF spacewalk-testsuite-base
# git clone $YOUR_SPACEWALK_TESTSUITE_CUSTOM_REPO
# run-testsuite
```
