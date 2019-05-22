DOCKER_CONTAINER = registry.mgr.suse.de/suma-4.0-cobbler

all :: cobbler_tests

pull_container ::
	@echo "Pulling container"
	docker pull $(DOCKER_CONTAINER)

cobbler_tests :: pull_container
	@echo "Running cobbler tests"
	docker run --rm -v $(CURDIR)/../../../../cobbler_reports:/reports -v $(CURDIR)/test_cobbler.sh:/test_cobbler.sh $(DOCKER_CONTAINER) /test_cobbler.sh

cobbler_tests_shell ::
	@echo "Spawning a shell inside of the cobbler tests container"
	docker run --rm -ti -v $(CURDIR)/../../../../cobbler_reports:/reports -v $(CURDIR)/test_cobbler.sh:/test_cobbler.sh $(DOCKER_CONTAINER) /bin/bash
