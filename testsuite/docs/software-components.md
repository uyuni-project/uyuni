# Software components used by the testsuite

## Rubygems

The test suite is a Cucumber testsuite, and Cucumber is written in the Ruby programming language. Ruby gems (Ruby packages) are used to provide base functionality.

Below is the list of Ruby gems used by the testsuite. It may change over time as the test suite is developped or refactored.

### Testsuite control, syntax and structure

* ```yaml``` enables to use files in YAML format<br /> More specifically, to run lists of Cucumber features stored in YAML format
* ```rake``` enables to use Rakefiles, the Ruby equivalent of Makefiles<br /> Here, to run the cucumber testsuite with the right command line arguments
* ```minitest``` is a framework for unit testing
* ```simplecov``` allows to analyze code coverage of the testsuite
* ```pp``` offers pretty-print of Ruby objects
* ```timeout``` enables to interrupt long-running blocks of code

### Communication with test VMs

* ```twopence``` allows to run commands, import and extract files as one would do with ssh and scp, but with a test-oriented approach (timeouts, etc)
* ```lavanda``` offers Ruby convenience extensions to twopence

### Simulation of user interaction

* ```capybara``` simulates user interaction with a web interface<br /> It can rely on different drivers for different web browsers.<br />
 We use the ```chromedriver``` driver, which offers access to the ```google Chrome``` web browser, (which we run in headless mode)

### Standard Ruby Library

* ```date```, ```time``: date and time manipulation functions
* ```base64```, ```json```, ```nokogiri```: support for various encodings and data formats: base64, json, XML
* ```net```, ```openssl```, ```uri```, ```open-uri```, ```xmlrpc```: support for various network protocols
* ```securerandom```: UUIDs and other random generation
* ```socket```, ```stringio```, ```tempfile```, ```tmpdir```: file manipulation

### Various

* ```english```: English language processing
* ```jwt```: JSON Web Token (JWT) standard


## Test machines

Operating system images are built by kiwi, and stored [here](http://download.suse.de/ibs/Devel:/Galaxy:/Terraform:/Images/). Then sumaform uses them.

sumaform starts tests machines with terraform, then provisions them with salt.

sumaform can work with clouds, but most of the time we use it with libvirt/KVM/QEMU virtual machines. To do that, we rely on a libvirt extension to terraform we developed inhouse.

The testsuite runs on a special machine called the control node.
