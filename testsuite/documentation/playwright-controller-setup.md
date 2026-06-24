# Playwright controller setup

The UI part of the test suite is driven by [Playwright](https://playwright.dev/) through the
[`capybara-playwright-driver`](https://github.com/YusukeIwaki/capybara-playwright-driver) gem. This replaces the old
Selenium / `chromedriver` stack. This document lists what the **controller** (the machine that runs `cucumber` /
`parallel_cucumber`) needs, and the openSUSE-specific gotchas.

## What changed vs. Selenium

| Before (Selenium) | After (Playwright) |
| --- | --- |
| `selenium-webdriver` gem | `capybara-playwright-driver` gem (pulls `playwright-ruby-client`) |
| distro `chromedriver` package | **none** — Playwright talks CDP directly |
| distro `chromium` used by chromedriver | distro `chromium` kept **only for its shared libraries**; the browser actually launched is the Playwright-managed Chromium |
| nothing from npm | Node.js `playwright` package (the CLI + Node driver) |

The Cucumber features, step definitions and Capybara DSL are unchanged. Each `parallel_cucumber` worker auto-spawns its
own headless Chromium — same per-worker model as chromedriver before, so no separate browser server is needed.

## Pinned versions

These three must stay aligned:

| Component | Version | Why |
| --- | --- | --- |
| `capybara-playwright-driver` (gem) | `0.5.9` | declared in `testsuite/Gemfile` |
| `playwright-ruby-client` (gem) | `1.60.0` | **explicitly pinned** in `testsuite/Gemfile`. The repo commits no `Gemfile.lock`, so an explicit pin is required to stop Bundler resolving a newer client than the installed Node driver |
| `playwright` (npm) | `1.60.0` | **must equal** the `playwright-ruby-client` version — the Ruby client shells out to this exact Node driver |

If you bump the gem, bump the npm package to the matching version.

## Dependencies to install on the controller

1. **Ruby 3.3 + build deps** — already provisioned for the suite (`ruby3.3`, `ruby3.3-devel`, `gcc`, `make`, …).
2. **The gems** — `bundle install` against `testsuite/Gemfile` installs `capybara-playwright-driver` (and drops
   `selenium-webdriver`). No change to how gems are installed.
3. **Node.js + npm** — package `npm-default` (already used by the suite for the HTML reporter).
4. **Playwright CLI (Node)** — `npm install -g playwright@1.60.0`. Installs the CLI to the global npm prefix
   (`/usr/local/bin/playwright` on openSUSE Leap).
5. **A Playwright-managed Chromium** — `playwright install chromium`. Downloads a CDP-matched Chromium build into
   `~/.cache/ms-playwright` (i.e. `/root/.cache/ms-playwright` when the suite runs as `root`).
6. **Chromium shared libraries** — keep the distro `chromium` package installed. It is *not* the browser Playwright
   launches, but it pulls the shared libraries (libgbm, NSS, fonts, …) that the Playwright Chromium needs. Combined
   with the suite's existing `cantarell-fonts`, `mozilla-nss-tools` and `ca-certificates-mozilla`, this satisfies the
   runtime deps.

### ⚠️ openSUSE gotcha — do NOT use `--with-deps`

Playwright's `playwright install --with-deps` only knows how to install OS libraries on **apt** (Debian/Ubuntu) and
**dnf/yum** (RHEL family). On **openSUSE / zypper it is unsupported** and will fail or do nothing useful. Therefore on
the controller:

```bash
playwright install chromium        # ✅ download the browser only
# playwright install --with-deps chromium   # ❌ do NOT — no zypper support
```

The OS libraries come from the distro `chromium` package (kept) instead.

## Environment variables

| Variable | Value | Purpose |
| --- | --- | --- |
| `PLAYWRIGHT_CLI_EXECUTABLE_PATH` | `/usr/local/bin/playwright` | tells `playwright-ruby-client` where the Node CLI is. Set in three places for robustness: the `controller/bashrc` template (the shells the suite runs in), `/etc/profile.d/playwright.sh` (login shells, via the SLS), and a hard-coded default in `features/support/env.rb`. |
| `PLAYWRIGHT_BROWSERS_PATH` | *(unset — default)* | only needed if the suite runs as a non-`root` user and the browser cache lives elsewhere. Default `~/.cache/ms-playwright` is fine for `root`. |

## Quick verification on the controller

```bash
playwright --version                       # -> Version 1.60.0
ls ~/.cache/ms-playwright                   # -> chromium-XXXX directory present
ruby -e 'require "capybara/playwright"; puts "gem ok"'   # from the testsuite dir, after bundle install
```

## How it is provisioned

The real controllers are provisioned with Salt, in the SUSE CI / sumaform Salt tree (outside this
repository). The controller state installs the dependencies listed above, in short:

1. keep the distro `chromium` package; remove `chromedriver` (Selenium-only);
2. install the Node driver globally: `npm install -g playwright@1.60.0`;
3. download the browser: `playwright install chromium` (never `--with-deps` on openSUSE — see above);
4. export `PLAYWRIGHT_CLI_EXECUTABLE_PATH=/usr/local/bin/playwright`.

The `capybara-playwright-driver` and `playwright-ruby-client` gems are pulled in automatically by the
existing `bundle install` step against `testsuite/Gemfile`.
