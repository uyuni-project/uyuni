### Static run

#### Warning

If you want to run the testsuite without sumaform, you have to create and configure the machines.
You may do that manually, or with the help of Salt states.

Best solution is to use sumaform, so every time you have a clean and fresh testing environment.
With a static setup, if you break your testing machines, or if you have any trouble, you are on your own.


#### How to proceed

Set up the following environment variables:

* `SERVER` the Uyuni server you are testing against
* `PROXY` the Uyuni proxy (don't declare this variable if there is no proxy)
* `CLIENT` the traditional client
* `MINION` the Salt minion
* `BUILD_HOST` the Docker and Kiwi build host
* `SSH_MINION` the SSH-managed Salt minion
* `RH_MINION` the Redhat-like Salt minion
* `DEB_MINION` the Debian-like Salt minion

Once you have the machines configured, you can run the testsuite.

- To run all standard tests, from the controller:

```console
export SERVER="${PREFIX}srv.tf.local"
export CLIENT="${PREFIX}cli-sles15.tf.local"
export MINION="${PREFIX}min-sles15.tf.local"
export BUILD_HOST="${PREFIX}min-build.tf.local"
export SSH_MINION="${PREFIX}minssh-sles15.tf.local"
export RH_MINION="${PREFIX}min-centos7.tf.local"
export DEB_MINION="${PREFIX}min-ubuntu2004.tf.local"
run-testsuite
```

- To run the tests from your local machine you must do some extra steps:
```console
zypper addrepo https://download.opensuse.org/repositories/systemsmanagement:/sumaform:/tools/openSUSE_Leap_15.4/
zypper install *twopence*
```
```console
cd <your_repo>/testsuite
bundle install
```
```console
export SERVER="${PREFIX}srv.tf.local"
export CLIENT="${PREFIX}cli-sles15.tf.local"
export MINION="${PREFIX}min-sles15.tf.local"
...
```
Before you are able to run your tests in local, you must assure that your SSH connection to any of the nodes can be done without adding user/pass.
It is recommended to configure your `.ssh/config` to use a SSH private key.
In case you deployed the environment with sumaform, just use the SSH key `.ssh/id_rsa` stored in the controller.
Example:
```console
# cat .ssh/config
Host *.tf.local
    User root
    IdentityFile ~/.ssh/id_rsa_test_env
```
```console
rake -T # List all different possibilities i.e. rake cucumber:sanity_check
```

- To debug your tests, you might want to see the browser in your Desktop from where the Cucumber actions will happen.
  To enable it:
```console
export DEBUG=1
```
