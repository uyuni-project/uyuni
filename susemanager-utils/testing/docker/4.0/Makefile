VERSION = 4.0.0
REGISTRY = registry.mgr.suse.de
SHELL := /bin/bash
# --no-cache=true
BUILDOPTS=
PUSH_IT=false

all: suma-4.0-root suma-4.0-base suma-4.0-cobbler suma-4.0-gatherer suma-4.0-spacewalkkoan suma-4.0-pgsql suma-4.0-pgsql-4eclipse suma-4.0-nodejs

suma-4.0-root::
	@echo "=================================="
	@echo "Building $@"
	@echo "=================================="
	pushd $@; \
	docker build $(BUILDOPTS) -t $@ . || exit 1; \
	docker tag $@ $@:$(VERSION); \
	docker tag $@ $(REGISTRY)/$@:latest; \
	docker tag $@:$(VERSION) $(REGISTRY)/$@:$(VERSION); \
	$(PUSH_IT) && docker push $(REGISTRY)/$@:latest ; \
	$(PUSH_IT) && docker push $(REGISTRY)/$@:$(VERSION); \
	popd

suma-4.0-base::
	@echo "=================================="
	@echo "Building $@"
	@echo "=================================="
	pushd $@; \
	docker build $(BUILDOPTS) -t $@ . || exit 1; \
	docker tag $@ $@:$(VERSION); \
	docker tag $@ $(REGISTRY)/$@:latest; \
	docker tag $@:$(VERSION) $(REGISTRY)/$@:$(VERSION); \
	$(PUSH_IT) && docker push $(REGISTRY)/$@:latest ; \
	$(PUSH_IT) && docker push $(REGISTRY)/$@:$(VERSION); \
	popd

suma-4.0-cobbler::
	@echo "=================================="
	@echo "Building $@"
	@echo "=================================="
	pushd $@; \
	docker build $(BUILDOPTS) -t $@ . || exit 1; \
	docker tag $@ $@:$(VERSION); \
	docker tag $@ $(REGISTRY)/$@:latest; \
	docker tag $@:$(VERSION) $(REGISTRY)/$@:$(VERSION); \
	$(PUSH_IT) && docker push $(REGISTRY)/$@:latest ; \
	$(PUSH_IT) && docker push $(REGISTRY)/$@:$(VERSION); \
	popd

suma-4.0-gatherer::
	@echo "=================================="
	@echo "Building $@"
	@echo "=================================="
	pushd $@; \
	docker build $(BUILDOPTS) -t $@ . || exit 1; \
	docker tag $@ $@:$(VERSION); \
	docker tag $@ $(REGISTRY)/$@:latest; \
	docker tag $@:$(VERSION) $(REGISTRY)/$@:$(VERSION); \
	$(PUSH_IT) && docker push $(REGISTRY)/$@:latest ; \
	$(PUSH_IT) && docker push $(REGISTRY)/$@:$(VERSION); \
	popd

suma-4.0-spacewalkkoan::
	@echo "=================================="
	@echo "Building $@"
	@echo "=================================="
	pushd $@; \
	docker build $(BUILDOPTS) -t $@ . || exit 1; \
	docker tag $@ $@:$(VERSION); \
	docker tag $@ $(REGISTRY)/$@:latest; \
	docker tag $@:$(VERSION) $(REGISTRY)/$@:$(VERSION); \
	$(PUSH_IT) && docker push $(REGISTRY)/$@:latest ; \
	$(PUSH_IT) && docker push $(REGISTRY)/$@:$(VERSION); \
	popd

suma-4.0-pgsql::
	@echo "=================================="
	@echo "Building $@"
	@echo "=================================="
	pushd $@; \
	docker build $(BUILDOPTS) -t $@ . || exit 1; \
	docker tag $@ $@:$(VERSION); \
	docker tag $@ $(REGISTRY)/$@:latest; \
	docker tag $@:$(VERSION) $(REGISTRY)/$@:$(VERSION); \
	$(PUSH_IT) && docker push $(REGISTRY)/$@:latest ; \
	$(PUSH_IT) && docker push $(REGISTRY)/$@:$(VERSION); \
	popd

suma-4.0-pgsql-4eclipse::
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

suma-4.0-nodejs::
	@echo "=================================="
	@echo "Building $@"
	@echo "=================================="
	pushd $@; \
	docker build $(BUILDOPTS) -t $@ . || exit 1; \
	docker tag $@ $@:$(VERSION); \
	docker tag $@ $(REGISTRY)/$@:latest; \
	docker tag $@:$(VERSION) $(REGISTRY)/$@:$(VERSION); \
	$(PUSH_IT) && docker push $(REGISTRY)/$@:latest ; \
	$(PUSH_IT) && docker push $(REGISTRY)/$@:$(VERSION); \
	popd
