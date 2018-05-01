VERSION = 3.2.0
REGISTRY = registry.mgr.suse.de
ORATARGET = suma-3.2-ora
SHELL := /bin/bash
# --no-cache=true
BUILDOPTS=

all: suma-3.2-base suma-3.2-cobbler suma-3.2-gatherer suma-3.2-spacewalkkoan suma-3.2-pgsql suma-3.2-ora

suma-3.2-base::
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

suma-3.2-cobbler::
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

suma-3.2-gatherer::
	@echo "=================================="
	@echo "Building $@"
	@echo "=================================="
	pushd $@; \
	docker build $(BUILDOPTS) -t $@ . || exit 1; \
	docker tag $@ $@:$(VERSION):latest; \
	docker tag $@ $(REGISTRY)/$@; \
	docker tag $@:$(VERSION) $(REGISTRY)/$@:$(VERSION); \
	docker push $(REGISTRY)/$@:latest ; \
	docker push $(REGISTRY)/$@:$(VERSION); \
	popd

suma-3.2-spacewalkkoan::
	@echo "=================================="
	@echo "Building $@"
	@echo "=================================="
	pushd $@; \
	docker build $(BUILDOPTS) -t $@ . || exit 1; \
	docker tag $@ $@:$(VERSION):latest; \
	docker tag $@ $(REGISTRY)/$@; \
	docker tag $@:$(VERSION) $(REGISTRY)/$@:$(VERSION); \
	docker push $(REGISTRY)/$@:latest ; \
	docker push $(REGISTRY)/$@:$(VERSION); \
	popd

suma-3.2-pgsql::
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

suma-3.2-pgsql-4eclipse::
	@echo "=================================="
	@echo "Building $@"
	@echo "=================================="
	pushd $@; \
	docker build -t $@ . || exit 1; \
	docker tag $@ $@:$(VERSION); \
	docker tag $@ $(REGISTRY)/$@; \
	docker tag $@:$(VERSION) $(REGISTRY)/$@:$(VERSION); \
	docker push $(REGISTRY)/$@ ; \
	docker push $(REGISTRY)/$@:$(VERSION); \
	popd

suma-3.2-ora::
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

suma-3.2-ora-4eclipse-pre::
	@echo "=================================="
	@echo "Building $@"
	@echo "=================================="
	pushd $@; \
	docker build -t $@ . || exit 1; \
	popd
	@echo "========================================================================================================"
	@echo "MANUAL ACTION REQUIRED !!!!"
	@echo "The Oracle container requires some extra care:"
	@echo "1) Run the following command:"
	@echo "   'docker run --privileged -t -i suma-<branch>-ora-4eclipse-pre /bin/bash'"
	@echo "2) From inside of the container run the following command:"
	@echo "   '/root/db-prepare.sh'"
	@echo "3) Once the Oracle setup is done open a new terminal and run the following command:"
	@echo "   docker commit <id of the container> suma-<branch>-ora-4eclipse-pre"
	@echo "   The id of the container is the hostname of the running container."
	@echo "   Otherwise you can obtain it by doing: 'docker ps'"
	@echo "4) Finally call 'make suma-3.1-ora-4eclipse' to tag and push the container"
	@echo "These painful steps are going to disappear once docker's build system supports prileged containers."
	@echo "========================================================================================================"

suma-3.2-ora-4eclipse::
	@echo "=================================="
	@echo "Building $@"
	@echo "=================================="
	pushd $@; \
	docker build -t $@ . || exit 1; \
	docker tag $@ $@:$(VERSION); \
	docker tag $@ $(REGISTRY)/$@; \
	docker tag $@:$(VERSION) $(REGISTRY)/$@:$(VERSION); \
	docker push $(REGISTRY)/$@ ; \
	docker push $(REGISTRY)/$@:$(VERSION); \
	popd

