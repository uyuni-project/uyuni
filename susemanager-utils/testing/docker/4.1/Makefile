VERSION = 4.1.2
REGISTRY = registry.mgr.suse.de
SHELL := /bin/bash
BUILDOPTS=
#--no-cache=true
PUSH_IT=false

public_containers: suma-4.1-root suma-4.1-base suma-4.1-cobbler suma-4.1-gatherer suma-4.1-spacewalkkoan suma-4.1-pgsql suma-4.1-pgsql-4eclipse suma-4.1-nodejs

all: public_containers


suma-4.1-root::
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

suma-4.1-base::
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

suma-4.1-cobbler::
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

suma-4.1-gatherer::
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

suma-4.1-spacewalkkoan::
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

suma-4.1-pgsql::
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

suma-4.1-pgsql-4eclipse::
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

suma-4.1-nodejs::
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
