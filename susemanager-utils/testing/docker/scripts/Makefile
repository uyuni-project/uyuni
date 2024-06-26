# If you want to test with the head container, you can do it withç
# PRODUCT=SUSE-Manager make
ifeq ($(PRODUCT), SUSE-Manager)
DOCKER_CONTAINER = registry.suse.de/devel/galaxy/manager/head/docker/containers/suma-head-cobbler
else
DOCKER_CONTAINER = registry.opensuse.org/systemsmanagement/uyuni/master/docker/containers/uyuni-master-cobbler
endif

all :: cobbler_tests

pull_container ::
	@echo "Pulling container"
	docker pull $(DOCKER_CONTAINER)

cobbler_tests :: pull_container
	@echo "Running cobbler tests"
	docker run --rm --cap-add=NET_ADMIN -v $(CURDIR)/../../../../cobbler_reports:/reports -v $(CURDIR)/test_cobbler.sh:/test_cobbler.sh $(DOCKER_CONTAINER) /test_cobbler.sh

cobbler_tests_shell ::
	@echo "Spawning a shell inside of the cobbler tests container"
	@echo "See /test_cobbler.sh for Cobbler env setup"
	docker run --rm --cap-add=NET_ADMIN -ti -v $(CURDIR)/../../../../cobbler_reports:/reports -v $(CURDIR)/test_cobbler.sh:/test_cobbler.sh $(DOCKER_CONTAINER) /bin/bash
