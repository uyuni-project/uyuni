# Spacewalk Testsuite for Suse-Manager 3.0

## Introduction

Testsuite to automatically test Spacewalk/Suse-Manager.

### Usefull tutorial infos:

[Testing-api tutorial](docs/api-call.md)
[Branches that we use](docs/branches.md)
[Debug](Debug.md)
[Pitfalls-test.md](Pitfalls)

## Running

You can run the Spacewalk Testsuite basically with two options:

* with sumaform (Official way)
* static setup (means you have the machine already properly configured/provisioned)

### Run Spacewalk Testsuite with Sumaform 

[Sumaform Cucumber testing howto](docs/sumaform-howto.md)

### Static setup
* The SUSE Manager official testsuite applicance has all the gems pre-installed as rpms. Alternatively you can use [rbenv](http://rbenv.org/) (packages available [here](https://software.opensuse.org/download/package?project=devel:languages:ruby:extensions&package=rbenv))

```console
rbenv local $version
gem install bundler --pre
rbenv rehash
bundle config build.nokogiri --use-system-libraries
bundle install
```

Setup the following environment variables.

* SERVER environment variable can be passed to change the default server you are testing against.
* CLIENT env variable test client
* MINION env variable test client/salt
* BROWSER (default `phantomjs` environment variable can be passed to change the default browser: `chrome`, `htmlunit`, `chrome`, `firefox`.

To run all standard tests call:

```console
rake
```

Or look at `rake -T` for available tasks.

## Custom feature run sets

Add a file into `run_sets/$name.yml` and then execute `rake cucumber:$name`.

## Conventions when adding more tests

* Add required gems to `Gemfile`.
* Cucumber features under features.
* Helpers shared scross tests/features should go into the `lib/spacewalk_testsuite_base library`.

## License

* The testsuite is licensed under the MIT license. See the `MIT-LICENSE.txt` file included in the distribution.

# Dummy packages used by the Testsuite
Some of the scenarios that are tested on this testsuite make use of some external testing repositories which contain dummy packages. These packages are used to test package and patch installation and upgrade.

The repositories are:
- https://build.suse.de/project/show/Devel:Galaxy:BuildRepo - For packages which must be installed on the client systems already.
- https://build.suse.de/project/show/Devel:Galaxy:TestsuiteRepo - Provides packages for "Test-Channel-x86_64" channel.

## Type of provided packages
- Normal dummy package: `andromeda-dummy`, `hoag-dummy`, `orion-dummy`, `milkyway-dummy`, etc.
- Wrong encoding of RPM attributes package: `blackhole-dummy`
