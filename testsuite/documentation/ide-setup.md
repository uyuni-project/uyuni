# IDE and editor setup

This document describes how to set up an IDE or editor to work on the test suite, plus
how to run the [RuboCop](https://github.com/rubocop/rubocop) linter locally the same way
the CI does.

Two options are covered:

- [RubyMine](#rubymine) — a full Ruby IDE (commercial)
- [Visual Studio Code / VSCodium](#visual-studio-code--vscodium) — a free editor

Use whichever you prefer; you do not need both.

## RubyMine

This is a short tutorial on how to set up development with RubyMine on openSUSE (tested
with Tumbleweed).

> **License:** RubyMine is a commercial JetBrains product and requires a license for
> commercial use. Obtain one through your organization's usual software-request process.
> If you prefer a free tool, use [VS Code / VSCodium](#visual-studio-code--vscodium)
> instead.

### Installation

The example below installs RubyMine on openSUSE Tumbleweed using Snap. See also the
[Snapcraft install page](https://snapcraft.io/install/rubymine/opensuse). Run these
commands as `root` (or prefix each with `sudo`), since they change repositories, install
packages and manage services.

```bash
# add the snappy repository
# replace openSUSE_Tumbleweed if you're using a different version of openSUSE
zypper addrepo --refresh https://download.opensuse.org/repositories/system:/snappy/openSUSE_Tumbleweed snappy
# import its GPG key
zypper --gpg-auto-import-keys refresh
# update zypper and install snapd
zypper dup --from snappy
zypper install snapd
# enable system services for snapd
systemctl enable snapd
systemctl start snapd
# only needed for Tumbleweed
systemctl enable snapd.apparmor
systemctl start snapd.apparmor
# install RubyMine
snap install rubymine --classic
# only needed for Tumbleweed if you receive a java error when you open it
zypper install libgthread-2_0-0
```

### Prerequisites

#### Installing Ruby

The test suite pins its Ruby version in [`testsuite/.ruby-version`](../.ruby-version)
(currently `3.3.4`) — always use that version so your setup does not drift from the repo.
Use your favourite version manager (`asdf`, `rvm`, `rbenv`, `chruby`) or your OS package
manager. For openSUSE Leap you can add the Ruby devel repositories from
<https://download.opensuse.org/repositories/devel:/languages:/ruby/>.

The example below uses `rvm` (replace `3.3.4` if `testsuite/.ruby-version` changes):

> **Note:** the `rvm` one-liner below pipes a remote script into your shell. Review the
> script first, or follow rvm's [GPG-verified install](https://rvm.io/rvm/install)
> (import the signing keys, then run the installer) if you prefer a verified path.

```bash
# fetch rvm (see the note above about reviewing/verifying the installer)
curl -sSL https://get.rvm.io | bash -s stable
# install the pinned Ruby version (see testsuite/.ruby-version)
rvm install 3.3.4
# set it as the default version
rvm alias create default ruby-3.3.4
# list Ruby versions
rvm list
# use Ruby 3.3.4
rvm use ruby-3.3.4
# check Ruby version
ruby --version
```

On recent macOS laptops the installation can be trickier; the following works with
`rbenv` and Homebrew:

```bash
brew install readline
brew install openssl
# Set this in ~/.zshrc
# Homebrew
export PATH=/opt/homebrew/bin:$PATH
export PATH="/opt/homebrew/sbin:$PATH"
# rbenv
export RBENV_ROOT=/opt/homebrew/opt/rbenv
export PATH=$RBENV_ROOT/bin:$PATH
eval "$(rbenv init -)"
# openssl
export PATH="/opt/homebrew/opt/openssl@1.1/bin:$PATH"
export LDFLAGS="-L/opt/homebrew/opt/openssl@1.1/lib"
export CPPFLAGS="-I/opt/homebrew/opt/openssl@1.1/include"
export PKG_CONFIG_PATH="/opt/homebrew/opt/openssl@1.1/lib/pkgconfig"
export RUBY_CONFIGURE_OPTS="--with-openssl-dir=/opt/homebrew/opt/openssl@1.1"
# then install the pinned Ruby version (see testsuite/.ruby-version)
RUBY_CFLAGS="-Wno-error=implicit-function-declaration" rbenv install 3.3.4
```

#### Importing the SSL certificate from the server

To let your IDE talk to a deployed server over HTTPS, import the server certificate. You
have to do this for every new deployment (and for every server if you use several).

```bash
# replace $SERVER with the FQDN of your server VM
wget http://$SERVER/pub/RHN-ORG-TRUSTED-SSL-CERT -O /etc/pki/trust/anchors/$SERVER.cert
update-ca-certificates
certutil -d sql:/root/.pki/nssdb -A -t TC -n "susemanager" -i /etc/pki/trust/anchors/$SERVER.cert
```

> **Note:** the certificate is fetched over plain HTTP, which is acceptable for local,
> throwaway sumaform test VMs. On any less trusted network, copy the certificate over an
> authenticated channel instead (e.g. `scp root@$SERVER:/srv/www/htdocs/pub/RHN-ORG-TRUSTED-SSL-CERT .`)
> or verify its fingerprint before trusting it.

### Setting up RubyMine

#### Ruby SDK

Inside RubyMine, go to **File → Settings → Languages and Frameworks → Ruby SDK and
gems** and select the Ruby version pinned in `testsuite/.ruby-version` (currently
`3.3.4`). The installed gems for that version are listed on the right.

#### Login shell

Inside RubyMine, go to **File → Settings → Tools → Terminal → Application settings** and
set the shell path to `/bin/zsh --login` (or `/bin/bash --login`).

#### Environment variables

The Cucumber run configuration needs the environment variables that point to your
deployed VMs. Inside RubyMine:

1. Go to **Run → Edit configurations**.
2. Click **Edit configuration templates...** (bottom left).
3. Select **Cucumber** on the left and paste your variables into the **Environment
   variables** field.

The values must match the machines you deployed with sumaform. Replace `<prefix>` with
the `name_prefix` you set in your sumaform `main.tf`; the `.tf.local` domain is the
sumaform default. You do not need all variables — only those matching what you deployed.

```bash
SERVER=<prefix>-srv.tf.local;CLIENT=<prefix>-cli-sles15.tf.local;MINION=<prefix>-min-sles15.tf.local;PROXY=<prefix>-pxy.tf.local;RHLIKE_MINION=<prefix>-min-centos7.tf.local;SSHMINION=<prefix>-minssh-sles15.tf.local;DEBLIKE_MINION=<prefix>-min-ubuntu2204.tf.local;BUILD_HOST=<prefix>-min-build.tf.local;VIRTHOST_KVM_URL=<prefix>-min-kvm.tf.local;VIRTHOST_KVM_PASSWORD=<password>;PXEBOOT_MAC=<mac-address>;DEBUG=1;GITPROFILES=https://github.com/uyuni-project/uyuni.git#:testsuite/features/profiles
```

The basic RubyMine setup is now complete.

## Visual Studio Code / VSCodium

### Installation

There are several ways to install VS Code; one is via the OBS Package Installer (`opi`).
See also the [openSUSE VS Code page](https://en.opensuse.org/Visual_Studio_Code).

```bash
# install the OBS Package Installer (opi)
sudo zypper in opi
# install VS Code via opi. A new zypper repository is added and should be kept for updates
opi vscode
# alternative: VSCodium (without MS branding/telemetry/licensing): https://github.com/VSCodium/vscodium
opi vscodium
```

### Extensions

The following extensions are useful when working on the test suite. Some of them should
be configured (e.g. to point at the right Ruby or RuboCop version).

Relevant to the test suite:

- [Ruby](https://github.com/rubyide/vscode-ruby)
- [Cucumber (Gherkin)](https://github.com/cucumber/vscode)
- [Better Jinja](https://github.com/samuelcolvin/jinjahtml-vscode) — Salt/Jinja
- [HashiCorp Terraform](https://github.com/hashicorp/vscode-terraform) — Terraform/sumaform

Generally useful:

- Mogami — checks for the latest version of each dependency
- AsciiDoc
- Code Spell Checker
- Error Lens
- GitHub Markdown Preview
- GitHub Pull Requests and Issues
- GitLens
- markdownlint
- Output Colorizer
- TODO Highlight
- vscode-icons
- YAML

### Viewing remote files

When browsing log files on a remote server to debug a test case, VS Code gives you syntax
highlighting and easy copy/paste. This needs two extensions (both closed-source):

- Remote - SSH
- Remote - SSH: Editing Configuration Files

> **Warning:** be careful on production systems — VS Code can copy proprietary software
> onto the remote host, which may not be allowed. For local (sumaform) VMs this is not a
> problem.

Install them by pressing <kbd>Ctrl</kbd>+<kbd>P</kbd> inside VS Code and pasting the
following, one after another (or search for them in the Extensions tab):

```bash
ext install ms-vscode-remote.remote-ssh
ext install ms-vscode-remote.remote-ssh-edit
```

Then open the Remote Explorer, add a new remote target, connect to it, and open the
folder you want to browse (for example `/var/log/rhn`).

## Running RuboCop locally

The CI lints the test suite Ruby code with RuboCop. To reproduce it exactly, run RuboCop
through Bundler so you get the version pinned in
[`testsuite/Gemfile`](../Gemfile) (currently `rubocop 1.82.1`) — this is what the
[`rubocop` GitHub workflow](../../.github/workflows/rubocop.yml) does:

```bash
# from a checkout of Uyuni (or spacewalk)
cd testsuite
gem install bundler
bundle install
# same command the CI runs
bundle exec rubocop features/*
```

You can also lint a single file while iterating:

```bash
cd testsuite
bundle exec rubocop features/step_definitions/common_steps.rb
```

### Disabling a check

You can temporarily disable a cop around a block of code:

```ruby
# rubocop:disable Metrics/BlockLength
Then(/^the uptime for "([^"]*)" should be correct$/) do |host|
  # ...
end
# rubocop:enable Metrics/BlockLength
```

### Configuration files

The RuboCop configuration is project-specific and lives inside the test suite folder:

```bash
cd testsuite
ls -l .rubocop.yml .rubocop_todo.yml
```

Edit these to define which checks should be enabled or ignored.
