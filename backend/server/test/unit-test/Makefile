# Docker tests variables
DOCKER_CONTAINER_BASE = manager_python_testing
DOCKER_TESTED_BRANCH  = head
DOCKER_REGISTRY       = suma-docker-registry.mgr.suse.de
DOCKER_RUN_EXPORT     = "PYTHONPATH=/manager/client/rhel/rhnlib/:/manager/client/rhel/rhn-client-tools/src"
DOCKER_VOLUMES        = -v "$(CURDIR)/../../../../:/manager"

DB_TESTS       = \
        test_capabilities.py \
        test_misc_functions.py \
        test_rhnActivationKey.py \
        test_rhnChannel.py \
        test_rhnServerGroup.py \
        test_server_registration.py


TESTS       = \
        test_rhnLib_timestamp.py

all:	$(addprefix test-,$(TESTS))

test-%:
	@echo Running $*
	PYTHONPATH=$(PYTHONPATH):../../../ python $*

docker_shell ::
	docker run --rm=true -t -i -e $(DOCKER_RUN_EXPORT) $(DOCKER_VOLUMES) $(DOCKER_REGISTRY)/$(DOCKER_CONTAINER_BASE)_oracle_$(DOCKER_TESTED_BRANCH) /bin/bash

