# Test Architecture

## General

We have three kind of test suites on this project, each of them has its own purpose and workflow:

- **CI Test Suite**: This test suite is executed periodically on the Jenkins pipelines, we run it for each of our released versions, plus our main branch. It's composed by a set of tests that covers all the server features and three kind of operating systems as clients. The main goal is to ensure that the server is working as expected and these clients can be managed by the server.
- **Build Validation Test Suite**: This test suite is executed on the Jenkins pipelines, but for now it's triggered only manually. Its purpose is to validate new Maintenance Updates or new versions of our product. It's composed by a set of tests that covers a very minimal setup of the server and proxy/RBS, plus all the supported clients including basic operations on them.
- **GitHub Validation Test Suite**: This test suite is executed on the GitHub Actions, it's triggered for each PR, and it's composed by a sub-set of the tests from the CI Test Suite, those that can be executed in a GitHub runner. The main goal is to give fast feedback to the developers about the changes they are introducing in a Pull Request.

## Test Suites

### CI Test Suite

The CI Test Suite is composed by the following stages:
- **Core**: This stage setups the server, so it can be used by the following tests.
- **RepoSync**: The goal of this stage is to synchronize the products that correspond to the clients that will be used in the tests, but it also takes care of creating the custom channels and Activation keys that we need to bootstrap and manage the clients.
- **Init Clients**: This stage bootstrap and onboard a proxy,a build host, a virtualization host, plus a sub-set of the supported clients.
- **Secondary**: This stage has all the different server features that we want to cover. We have this stage split in two, because there are some tests that in theory can run in parallel, but in practice we still have issues that way, so we have to run them sequentially.
- **Finishing**: This stage collects some logs and metrics from the system, so we can debug and analyze the results of the tests. It also includes a test that must run at the end, to validate the database backup and restore feature.

#### How we include the code changes that we want to test?

We rely on Sumaform to deploy our VM instances for each of our components. Sumaform not only deploy the VM, but also configures all what we need inside each system, it make use of Salt for that purpose.
As part of the Salt states configuring the systems, we have a state that injects repositories into the system, that points to the IBS/OBS repositories containing our packages in development for each release version, including main branch.

Those repositories are not directly handled by our server, so during the reposync stage we create custom channels with new custom repositories that will parse and include the repositories injected by Sumaform.
These custom channels will be part of the Activation Keys that we use to bootstrap the clients.

### Build Validation Test Suite

The Build Validation Test Suite is composed by the following stages:
- **Sanity Check**: This stage checks that all the nodes are up and we can interact with them.
- **Core**: This stage setups the server, so it can be used by the following tests.
- **RepoSync**: The goal of this stage is to synchronize the products that correspond to the clients that will be used in the tests.
- **Add MU Repositories**: This stage setups the custom repositories that are part of the Maintenance Updates.
- **Add Non-MU Repositories**: This stage setups the custom repositories that are not part of the Maintenance Updates, like the repositories containing the ISOs.
- **Create bootstrap repositories**: This stage creates the bootstrap repositories that are used to bootstrap the clients.
- **Init Clients**: This stage bootstrap and onboard a proxy plus all the supported clients.
- **Init Monitoring**: This stage setups the monitoring server.
- **Retail**: This stage setups the retail environment, and run the retail tests.
- **Migration**: This stage tests the migration from different product versions, and between Salt implementations.
- **Finishing**: This stage collects some logs and metrics from the system, so we can debug and analyze the results of the tests. It also includes a test that must run at the end, to validate the database backup and restore feature.

#### How we include the maintenance updates that we want to test?

We rely on Sumaform to deploy our VM instances for each of our components. Sumaform not only deploy the VM, but also configures all what we need inside each system, it make use of Salt for that purpose.
In such setup, we don't rely on repositories injected by Sumaform, because we are not testing the development repositories, but the Maintenance Updates repositories.

We create custom channels and repositories that will contain the Maintenance Updates repositories, and we use them to bootstrap the clients.

### GitHub Validation Test Suite

The GitHub Validation Test Suite is composed by the following stages:
- **Core**: This stage setups the server, creates fake custom channels, and activation keys.
- **Init Clients**: This stage don't really bootstrap the minions, but just do a minimal check and subscribe to the needed fake channels.
- **Secondary**: This stage is split on five different GH jobs, each one covering a different server features. Note that we don't have the same coverage as the CI Test Suite, because we are limited by the GitHub runner support on some features.

#### How we include the code changes that we want to test?

On the GitHub setup, we don't have a deployment with Sumaform, instead we run each component in a new container.
Regarding the server container, we have a script to build from the source code, so it includes the changes from the PR.
