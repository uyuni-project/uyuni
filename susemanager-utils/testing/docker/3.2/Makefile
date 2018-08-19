VERSION = 3.2.1
REGISTRY = registry.mgr.suse.de
SHELL := /bin/bash
# --no-cache=true
BUILDOPTS=

all: suma-3.2-base suma-3.2-cobbler suma-3.2-gatherer suma-3.2-spacewalkkoan suma-3.2-pgsql

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
	docker tag $@ $@:$(VERSION); \
	docker tag $@ $(REGISTRY)/$@:latest; \
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
	docker tag $@ $@:$(VERSION); \
	docker tag $@ $(REGISTRY)/$@:latest; \
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
