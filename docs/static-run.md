### Static run

#### Take care : 
 
if you want to run the testsuite without sumaform, you have to create/configure the machine manually, or in alternative
using the salt-states.

Best solution is to use sumaform, so every time you have a clean-fresh testing env.

For the static setup, if you break your testing-machines, you have any troubles, you are on your own, with the static setup.

Once you have the machines configured, you can run the rake and tests.

Setup the following environment variables.

* TESTHOST environment variable can be passed to change the default server you are testing against.
* CLIENT env variable test client
* MINION env variable test client/salt
* SSHMINION env variable test salt ssh-managed
* CENTOSMINION env variable test salt centos family os
* BROWSER (default `phantomjs` environment variable can be passed to change the default browser: `chrome`, `htmlunit`, `chrome`, `firefox`.

To run all standard tests call, from the control-node.

```console
export CENTOSMINION="${PREFIX}mincentos7.tf.local"
export MINION="${PREFIX}minsles12sp1.tf.local"
export TESTHOST=${PREFIX}suma3pg.tf.local
export CLIENT=${PREFIX}clisles12sp1.tf.local
export BROWSER=phantomjs
rake
```
