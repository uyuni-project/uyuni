### Static run

#### Running the testsuite without sumaform
 
If you want to run the testsuite without sumaform, you have to create and configure the machine manually, or with the help of Salt states.

Best solution is to use sumaform, so every time you have a clean and fresh testing environment.
With a static setup, if you break your testing machines, or if you have any trouble, you are on your own.

Set up the following environment variables:

* `TESTHOST` the SUSE Manager server you are testing against
* `PROXY` the SUSE Manager proxy (don't declare this variable if there is no proxy)
* `CLIENT` the traditional client
* `MINION` the Salt minion
* `SSHMINION` the SSH-managed Salt minion
* `CENTOSMINION` the CentOS Salt minion

Once you have the machines configured, you can run the testsuite.
To run all standard tests, from the controller:

```console
export TESTHOST="${PREFIX}suma3pg.tf.local"
export CLIENT="${PREFIX}clisles12sp3.tf.local"
export MINION="${PREFIX}minsles12sp3.tf.local"
export SSHMINION="${PREFIX}minsles12sp3ssh.tf.local"
export CENTOSMINION="${PREFIX}mincentos7.tf.local"
run-testsuite
```
