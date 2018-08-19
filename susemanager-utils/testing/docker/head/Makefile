VERSION = 3.2.5
REGISTRY = registry.mgr.suse.de
ORATARGET = suma-head-ora
SHELL := /bin/bash
# --no-cache=true
BUILDOPTS=

#
# no *-4eclipse container: we use the last released version for head
#

all: suma-head-base suma-head-cobbler suma-head-gatherer suma-head-spacewalkkoan suma-head-pgsql suma-head-ora

suma-head-base::
	@echo "=================================="
	@echo "Building $@"
	@echo "=================================="
	pushd $@; \
	docker build $(BUILDOPTS) -t $@ . || exit 1; \
	docker tag $@ $@:$(VERSION); \
	docker tag $@ $(REGISTRY)/$@:latest; \
	docker tag $@:$(VERSION) $(REGISTRY)/$@:$(VERSION); \
	docker push $(REGISTRY)/$@:latest ; \
	docker push $(REGISTRY)/$@:$(VERSION); \
	popd

suma-head-cobbler::
	@echo "=================================="
	@echo "Building $@"
	@echo "=================================="
	pushd $@; \
	docker build $(BUILDOPTS) -t $@ . || exit 1; \
	docker tag $@ $@:$(VERSION); \
	docker tag $@ $(REGISTRY)/$@:latest; \
	docker tag $@:$(VERSION) $(REGISTRY)/$@:$(VERSION); \
	docker push $(REGISTRY)/$@:latest ; \
	docker push $(REGISTRY)/$@:$(VERSION); \
	popd

suma-head-gatherer::
	@echo "=================================="
	@echo "Building $@"
	@echo "=================================="
	pushd $@; \
	docker build $(BUILDOPTS) -t $@ . || exit 1; \
	docker tag $@ $@:$(VERSION); \
	docker tag $@ $(REGISTRY)/$@:latest; \
	docker tag $@:$(VERSION) $(REGISTRY)/$@:$(VERSION); \
	docker push $(REGISTRY)/$@:latest ; \
	docker push $(REGISTRY)/$@:$(VERSION); \
	popd

suma-head-spacewalkkoan::
	@echo "=================================="
	@echo "Building $@"
	@echo "=================================="
	pushd $@; \
	docker build $(BUILDOPTS) -t $@ . || exit 1; \
	docker tag $@ $@:$(VERSION); \
	docker tag $@ $(REGISTRY)/$@:latest; \
	docker tag $@:$(VERSION) $(REGISTRY)/$@:$(VERSION); \
	docker push $(REGISTRY)/$@:latest ; \
	docker push $(REGISTRY)/$@:$(VERSION); \
	popd

suma-head-pgsql::
	@echo "=================================="
	@echo "Building $@"
	@echo "=================================="
	pushd $@; \
	docker build $(BUILDOPTS) -t $@ . || exit 1; \
	docker tag $@ $@:$(VERSION); \
	docker tag $@ $(REGISTRY)/$@:latest; \
	docker tag $@:$(VERSION) $(REGISTRY)/$@:$(VERSION); \
	docker push $(REGISTRY)/$@:latest ; \
	docker push $(REGISTRY)/$@:$(VERSION); \
	popd

suma-head-ora::
	@echo "=================================="
	@echo "Building $@"
	@echo "=================================="
	pushd $@; \
	docker build $(BUILDOPTS) -t $@ . || exit 1; \
	popd
	@echo "========================================================================================================"
	@echo "MANUAL ACTION REQUIRED !!!!"
	@echo "The Oracle container requires some extra care:"
	@echo "1) Run the following command:"
	@echo "   'docker run --privileged -t -i -v <dir containing git checkout>:/manager suma-<branch>-ora /bin/bash'"
	@echo "2) From inside of the container run the following command:"
	@echo "   '/manager/susemanager-utils/testing/docker/<branch>/suma-<branch>-ora/db-setup'"
	@echo "3) Once the Oracle setup is done open a new terminal and run the following command:"
	@echo "   docker commit <id of the container> suma-<branch>-ora"
	@echo "   The id of the container is the hostname of the running container."
	@echo "   Otherwise you can obtain it by doing: 'docker ps'"
	@echo "4) Finally call 'make ora-finish' to tag and push the container"
	@echo "These painful steps are going to disappear once docker's build system supports prileged containers."
	@echo "========================================================================================================"

ora-finish::
	docker tag $(ORATARGET) $(ORATARGET):$(VERSION); \
	docker tag $(ORATARGET) $(REGISTRY)/$(ORATARGET):latest; \
	docker tag $(ORATARGET):$(VERSION) $(REGISTRY)/$(ORATARGET):$(VERSION); \
	docker push $(REGISTRY)/$(ORATARGET):latest; \
	docker push $(REGISTRY)/$(ORATARGET):$(VERSION); \

