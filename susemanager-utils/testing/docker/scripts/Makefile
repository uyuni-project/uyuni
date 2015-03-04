all :: cobbler_tests

cobbler_tests ::
	@echo "Running cobbler tests"
	docker run --rm -v $(CURDIR)/../../../../cobbler_reports:/reports -v $(CURDIR)/test_cobbler.sh:/test_cobbler.sh suma-docker-registry.suse.de/suma-head-cobbler /test_cobbler.sh

cobbler_tests_shell ::
	@echo "Spawning a shell inside of the cobbler tests container"
	docker run --rm -ti -v $(CURDIR)/../../../../cobbler_reports:/reports -v $(CURDIR)/test_cobbler.sh:/test_cobbler.sh suma-docker-registry.suse.de/suma-head-cobbler /bin/bash
