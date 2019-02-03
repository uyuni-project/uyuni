VERSION = 4.0.0
REGISTRY = registry.mgr.suse.de
SHELL := /bin/bash
# --no-cache=true
BUILDOPTS=

#
# no *-4eclipse container: we use the last released version for head
#

all: suma-head-root suma-head-base suma-head-cobbler suma-head-gatherer suma-head-spacewalkkoan suma-head-pgsql

suma-head-root::
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
	popd

