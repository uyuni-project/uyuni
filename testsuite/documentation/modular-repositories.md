# Setting up and using a modular repository

This document explains how AppStream / modular repositories work, how to build one with
the [Open Build Service (OBS)](https://build.opensuse.org/), and how to use it on a
Red Hat-like system. It is aimed at testing modular content with Uyuni and Multi-Linux
Manager.

Throughout the examples, replace `home:<obs_user>:Test-Packages:Pool` with your own OBS
project, and treat host names such as `rocky8-minion` as placeholders for your own test
machines.

## AppStream and modular repositories

AppStream is an open-source project that provides a unified framework for managing and
presenting software metadata in Linux distributions. It standardizes the way software
metadata is created, distributed, and consumed.

Modular repositories carry an extra piece of metadata (*modular metadata*) served in a
`.yaml` file. This file defines the modules, the relationships between them, and the
ownership of modular packages.

Red Hat-like distributions approach modularity slightly differently across versions:

- **RH8** defines every alternative version as a separate module stream, marking one of
  them as the default.
- **RH9** ships the default versions as regular, non-modular packages, but defines any
  other alternative version as a separate module stream.

### Features of a modular repository

- **Modularity:** software is divided into modules, each containing a set of related
  packages (specific versions of applications, libraries, or development tools).
- **Streams and profiles:** modules can have multiple streams (different versions) and
  profiles (configurations for specific use cases), so users can select the version and
  configuration that fits their needs.
- **Isolated environments:** each module can be enabled or disabled independently, so
  different versions of software can coexist without conflicts.

## Creating a modular repository with OBS

### Requirements and limitations

Modular repositories are mainly built for RH-like systems. OBS supports creating modules
but can only correctly parse and use a subset of the available options. In particular:

- The module metadata must reside in a file named `_modulemd.yaml`, inside a package
  named `modulemd` placed at the project's root. It is parsed at build time to produce a
  `modules.yaml` file inside the `repodata` directory of a repository.
- Only version 2 is supported for `modulemd` documents.
- Only version 1 is supported for `modulemd-defaults` documents.
- Only one `modulemd` document can be defined inside a `_modulemd.yaml` file.
- You must explicitly define a stream for the `modulemd` document entry.
- It does not appear possible to define multiple streams for a single module using one
  `modulemd` document.
- Most configuration entries are ignored (filters, artifacts, ...) or get their value
  replaced by one calculated during the build (references, version numbers, context ID).

### Project configuration

Set the following flags in the *Project Config* section of the OBS project. See the
[OBS documentation](https://openbuildservice.org/help/manuals/obs-user-guide/) for a
detailed explanation.

```
BuildFlags: modulemdplatform:80000:.module_el8.0.0:platform-el8:platform-el8.0.0
ExpandFlags: !module:common
```

TL;DR:

- The OBS `modulemd` parser expects a `modulemdplatform` build flag, which it uses to
  obtain version and distribution prefixes (by splitting on `:`). In the example, the
  setup targets a module that ends up in a CentOS 8 repository.
- `ExpandFlags` excludes the `common` module from the build. If it is present, it breaks
  the parser, which expects modules to follow a `NAME-STREAM` convention; otherwise it
  fails to calculate a modularity label (metadata identifying the module's content).

### Writing a `_modulemd.yaml`

As noted above, this file must live in a `modulemd` package at the project's root. OBS
reports that the `modulemd` package is excluded from builds, but it is still parsed to
produce a `modules.yaml` file if its contents are valid.

OBS does not support many of the possible `modulemd` options, so a typical
`_modulemd.yaml` is quite concise; you rely on other strategies for things like having
multiple streams of the same module.

Example `_modulemd.yaml` for a dummy package used in testing:

```yaml
document: modulemd
version: 2
data:
  name: andromeda
  stream: 2.0
  summary: andromeda module to test Uyuni and SUMA V 2.0
  description: andromeda module to test Uyuni and SUMA V 2.0
  license:
    module:
      - GPL-2.0
  dependencies:
    - requires:
        platform:
          - el8
  profiles:
    default:
      rpms:
        - andromeda-dummy
  api:
    rpms:
      - andromeda-dummy
  components:
    rpms:
      andromeda-dummy:
        rationale: This is version 2.0 of the andromeda-dummy package.
        buildorder: 10
        version: 2.0
```

The stream name/number and the RPM component version number are independent. The values
under `rpms` refer to the actual final built RPM, which is usually created by another
package that defines it in its `.spec` file.

Given that you have:

- a `_modulemd.yaml` file in your project, inside a `modulemd` package;
- other packages responsible for building the RPMs needed by the module (declared in the
  `_modulemd.yaml` `components` section);
- a final repository for a distribution supporting modular packages as build target;

the build results should include a `modules.yaml` alongside the RPMs. The produced
`modules.yaml` in the `repodata` directory will look like:

```yaml
document: modulemd
version: 2
data:
  name: andromeda
  stream: "2.0"
  version: 8000020240527114652
  context: d6fc407f
  arch: x86_64
  summary: andromeda module to test Uyuni and SUMA V 2.0
  description: >-
    andromeda module to test Uyuni and SUMA V 2.0
  license:
    module:
      - GPL-2.0
    content:
      - GPL-2.0
  xmd: {}
  dependencies:
    - requires:
        platform: [el8]
  profiles:
    default:
      rpms:
        - andromeda-dummy
  api:
    rpms:
      - andromeda-dummy
  components:
    rpms:
      andromeda-dummy:
        rationale: This is version 2.0 of the andromeda-dummy package.
        ref: obs://build.opensuse.org/home:<obs_user>:Test-Packages:Pool/CentOS_8/<hash>-andromeda-dummy-2.0
        buildorder: 10
        version: "2.0"
  artifacts:
    rpms:
      - andromeda-dummy-0:2.0-1.2.noarch
      - andromeda-dummy-0:2.0-1.2.src
```

### Setting up multiple streams for a module

The following instructions are mainly a way to work around OBS limitations. If you find a
straightforward way to do this without hacks, please update these docs.

As an example, assume you need a repository that satisfies these requirements:

- A single module that contains only one of the dummy packages and has a similar name to
  the package (e.g. module `andromeda`).
- The module must have 2 different streams (e.g. `2.0` and `2.1`) containing different
  versions of the package (`andromeda-dummy-2.0` and `andromeda-dummy-2.1`).
- The `andromeda-dummy` package must also be available as a regular package with an older
  version than the modular ones (e.g. `andromeda-dummy-1.0`). Otherwise unrelated features
  break, since modular packages are hidden from clients unless the module is enabled.

Normally you would define multiple streams or multiple documents in the `_modulemd.yaml`,
but OBS does not seem to support either approach. Attempting to build multiple streams at
once usually ends up with only the last version of a package considered, and all RPMs
included in the `artifacts` section — or it is just not possible.

What works relies on the fact that each build happens in its own *context*. Because of
that, you can change the entries in `_modulemd.yaml` multiple times to define a different
stream for the same package, having each build use a different RPM version. If this is
done in a build context where only the relevant RPM version is built, other versions are
unknown at build time, and the final `modules.yaml` gets a `modulemd` document defining
only that stream. (A single `modules.yaml` can contain multiple `modulemd` documents.)

1. Build your non-modular packages (can also be done at the end), e.g. build
   `andromeda-dummy-1.0` as a standard RPM.
2. Disable the build flag for the repositories you intend to build modules for.
3. Have one package for each version of the RPMs used by the modules, e.g.
   `andromeda-dummy` (1.0), `andromeda-dummy-2.0` and `andromeda-dummy-2.1`.
4. Set up `_modulemd.yaml` to define a single stream and use a specific package; change
   it accordingly when you want to build a new stream. In this example, build once with
   stream `2.0` and the `andromeda-dummy` 2.0 RPM, then change it to stream `2.1` and the
   2.1 RPM.
5. Re-enable the build flag for the `modulemd` package and the package(s) it uses.
6. A build should trigger — verify both the RPM version and a `modules.yaml` file are
   produced without errors.
7. Verify the `modules.yaml` in the repository is correctly updated with the new stream
   info.
8. Disable the build flag for the package version(s) and the `modulemd` package.
9. Repeat steps 4–8 as needed.

The resulting `modules.yaml` then contains one `modulemd` document per stream, for example
streams `2.0` and `2.1` of the `andromeda` module.

## Using a modular repository on a RH-like system

The examples below use a Rocky 8 minion. Add the repository and sync it:

```console
[root@rocky8-minion ~]# dnf config-manager --add-repo=https://download.opensuse.org/repositories/home:/<obs_user>:/Test-Packages:/Pool/CentOS_8/home:<obs_user>:Test-Packages:Pool.repo
[root@rocky8-minion ~]# dnf repolist
[root@rocky8-minion ~]# dnf makecache
```

Verify the available modules:

```console
[root@rocky8-minion ~]# dnf module list
Name       Stream  Profiles          Summary
andromeda  2.0 [x] default           andromeda module to test Uyuni and SUMA V 2.0
andromeda  2.1 [x] default           andromeda module to test Uyuni and SUMA V 2.1
Hint: [d]efault, [e]nabled, [x]disabled, [i]nstalled
```

With no module enabled, only the standard `andromeda-dummy-1.0` RPM is available; the
modular versions are filtered out:

```console
[root@rocky8-minion ~]# dnf install andromeda-dummy
Installing:
 andromeda-dummy   noarch   1.0-1.1   home_<obs_user>_Test-Packages_Pool   14 k

[root@rocky8-minion ~]# dnf install andromeda-dummy-2.0
Error: Unable to find a match: andromeda-dummy-2.0   # filtered out by modular filtering
```

Enable stream `2.0` and the resolved version changes:

```console
[root@rocky8-minion ~]# dnf module enable andromeda:2.0
Enabling module streams:
 andromeda   2.0

[root@rocky8-minion ~]# dnf install andromeda-dummy
Installing:
 andromeda-dummy   noarch   2.0-1.2   home_<obs_user>_Test-Packages_Pool   14 k

[root@rocky8-minion ~]# dnf install andromeda-dummy-1.0
Error: Unable to find a match: andromeda-dummy-1.0   # 1.0 now filtered out
```

Disable a module with:

```console
[root@rocky8-minion ~]# dnf module disable andromeda
```

You cannot have multiple streams of the same module active at the same time:

```console
[root@rocky8-minion ~]# dnf module enable andromeda:2.1
The operation would result in switching of module 'andromeda' stream '2.0' to stream '2.1'
Error: It is not possible to switch enabled streams of a module unless explicitly enabled
via configuration option module_stream_switch.
```

To switch, reset the module first (`dnf module reset andromeda`), then enable the other
stream.

## Setting up a local OBS dev environment

Running OBS locally is useful to test your project setup without waiting for OBS
scheduling, or to inspect its inner workings and debug build errors.

OBS is open source and the code is [available on GitHub](https://github.com/openSUSE/open-build-service).
A local setup uses Docker and Docker Compose to run the containers for the front end and
back end. Some parts are built in Ruby and generated with Rake, so you need:

- Git
- Docker Engine
- Docker Compose v2
- Rake

The OBS *Contributing* README has a detailed walk-through of how to set up a dev
environment.

## Resources

- [Introduction to modularity — Fedora docs](https://docs.fedoraproject.org/en-US/modularity/)
- [Red Hat 9 documentation about modularity](https://access.redhat.com/documentation/en-us/red_hat_enterprise_linux/9/)
- [Open Build Service documentation](https://openbuildservice.org/help/)
- [Open Build Service wiki](https://github.com/openSUSE/open-build-service/wiki)
