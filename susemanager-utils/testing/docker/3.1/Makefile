VERSION = 3.1.0
REGISTRY = registry.mgr.suse.de
SHELL := /bin/bash

all: suma-3.1-base suma-3.1-cobbler suma-3.1-gatherer suma-3.1-spacewalkkoan suma-3.1-pgsql

suma-3.1-base::
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

suma-3.1-cobbler::
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

suma-3.1-gatherer::
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

suma-3.1-spacewalkkoan::
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

suma-3.1-pgsql::
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

suma-3.1-pgsql-4eclipse::
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
