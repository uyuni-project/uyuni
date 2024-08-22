# Static run

## Warning

If you want to run the test suite without sumaform, you have to create and configure the machines.
You may do that manually, or with the help of Salt states.

The best solution is to use sumaform, so every time you have a clean and fresh testing environment.
With a static setup, if you break your testing machines, or if you have any trouble, you are on your own.

## How to proceed

Set up the following environment variables:

* `SERVER` the Uyuni server you are testing against
* `PROXY` the Uyuni proxy (don't declare this variable if there is no proxy)
* `MINION` the Salt minion
* `BUILD_HOST` the Docker and Kiwi build host
* `SSH_MINION` the SSH-managed Salt minion
* `RHLIKE_MINION` the Red Hat-like Salt minion
* `DEBLIKE_MINION` the Debian-like Salt minion

Once you have the machines configured, you can run the test suite.

* To run all standard tests, from the controller:

```bash
export SERVER="${PREFIX}srv.tf.local"
export CLIENT="${PREFIX}cli-sles15.tf.local"
export MINION="${PREFIX}min-sles15.tf.local"
export BUILD_HOST="${PREFIX}min-build.tf.local"
export SSH_MINION="${PREFIX}minssh-sles15.tf.local"
export RHLIKE_MINION="${PREFIX}min-rocky8.tf.local"
export DEBLIKE_MINION="${PREFIX}min-ubuntu2204.tf.local"
run-testsuite
```

* To run the tests from your local machine you must do some extra steps:

```bash
# add sumaform tools repository
zypper addrepo https://download.opensuse.org/repositories/systemsmanagement:/sumaform:/tools/openSUSE_Tumbleweed/

# install required Ruby gems
cd <your_repo>/testsuite
bundle install

export SERVER="${PREFIX}srv.tf.local"
export CLIENT="${PREFIX}cli-sles15.tf.local"
export MINION="${PREFIX}min-sles15.tf.local"
...
```

Before you are able to run your tests locally, you must assure that your SSH connection to any of the nodes can be
established without a username/password.
It is recommended to configure your `.ssh/config` to use a SSH private key.
In case you deployed the environment with sumaform, just use the SSH key `.ssh/id_rsa` stored in the controller.
Example:

```bash
$ cat .ssh/config
Host *.tf.local
    User root
    IdentityFile ~/.ssh/id_rsa_test_env

# ssh to the controller
ssh root@head-ctl.tf.local

# list all different Rake tasks
rake -T

# execute one of the tasks
rake cucumber:sanity_check
```

* To debug your tests, you might want to see the browser on your desktop from where the Cucumber actions will happen.
To enable it:

```bash
export DEBUG=1
```
