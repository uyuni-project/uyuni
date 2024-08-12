# Software components used by the test suite

## Rubygems

The test suite is a Cucumber test suite, and Cucumber is interpreted via the Ruby programming language. Ruby gems
(Ruby packages) are used to provide base functionalities.

Below is the list of Ruby gems used by the test suite. It may change over time as the test suite is developed or
refactored.

### Test suite control, syntax and structure

* [`cucumber`](https://github.com/cucumber/cucumber-ruby) is what we use for behavior driven development
* [`cucumber-html-formatter`](https://github.com/cucumber/html-formatter) produces a pretty HTML report for Cucumber
runs
* [`mime-types`](https://github.com/mime-types/ruby-mime-types/) provides a registry for information about MIME media
type definitions
* [`minitest`](https://github.com/minitest/minitest) is a framework for unit testing
* [`parallel_tests`](https://github.com/grosser/parallel_tests) allows running tests in parallel
* [`pp`](https://github.com/ruby/pp) offers pretty-print of Ruby objects
* [`rake`](https://github.com/ruby/rake) enables to use Rakefiles, the Ruby equivalent of Makefiles. Here, to run the
Cucumber test suite with the right command line arguments
* [`rubocop`](https://github.com/rubocop/rubocop) is the code style checker for Ruby
* [`simplecov`](https://github.com/simplecov-ruby/simplecov) allows to analyze code coverage of the test suite
(**Disabled by default**)
* [`syntax`](https://github.com/dblock/syntax) is a syntax highlighting a library
* [`timeout`](https://github.com/ruby/timeout) enables to interrupt long-running blocks of code
* [`yaml`](https://github.com/ruby/yaml) enables to use files in YAML format. More specifically,
to run lists of Cucumber features stored in YAML format

### Communication with test VMs

* [`RemoteNode`] is a custom Ruby class that allows to communicate with remote test machines via SSH and SCP
* [`net-ssh`] is a Ruby library that allows to communicate with remote machines via SSH
* [`net-scp`] is a Ruby library that allows to copy files to and from remote machines via SCP

### Simulation of user interaction

* [`capybara`](https://github.com/teamcapybara/capybara) simulates user interaction with a web interface.
It can rely on different drivers for different web browsers.
* [`selenium-webdriver`](https://github.com/SeleniumHQ/selenium) is for automating web applications for testing purposes.
We use `chromedriver` as driver, which offers access to the `Google Chrome` web browser run in headless mode.

### Standard Ruby Library

* `date`, `time`: date and time manipulation functions
* `base64`, `json`, `nokogiri`: support for various encodings and data formats: base64, json, XML
* `net`, `openssl`, `uri`: support for network access
* `securerandom`: UUIDs and other random generation
* `socket`, `stringio`, `tempfile`, `tmpdir`: file manipulation

### Various

* [`english`](https://github.com/ruby/English): English language processing
* [`jwt`](https://github.com/jwt/ruby-jwt): JSON Web Token (JWT) standard
* [`open-uri`](https://github.com/ruby/open-uri), [`xmlrpc`](https://github.com/ruby/xmlrpc), [`faraday`](https://github.com/lostisland/faraday): network and RPC helpers
* [`pg`](https://github.com/ged/ruby-pg) interface to the PostgreSQL RDBMS
* [`rack`](https://github.com/rack/rack), [`rack-test`](https://github.com/rack/rack-test) is a modular Ruby web server interface
* [`websocket`](https://github.com/imanel/websocket-ruby), [`websocket-driver`](https://github.com/faye/websocket-driver-ruby) is a universal Ruby library to handle WebSocket protocols

## Test machines

Operating system images are built by Kiwi and stored [here](http://download.suse.de/ibs/Devel:/Galaxy:/Terraform:/Images/).
Then sumaform uses them, starts tests machines with terraform and then provisions them with Salt.

sumaform can work with clouds, but most of the time we use it with libvirt/KVM/QEMU virtual machines. To do that, we
rely on a libvirt extension to terraform called [terraform-provider-libvirt](https://github.com/dmacvicar/terraform-provider-libvirt)
that we developed inhouse.

The test suite runs on a special machine called the control node.
