VERSION = 4.0.0
REGISTRY = registry.mgr.suse.de
SHELL := /bin/bash
BUILDOPTS=
#--no-cache=true
PUSH_IT=false

all: uyuni-master-root uyuni-master-base uyuni-master-cobbler uyuni-master-gatherer uyuni-master-spacewalkkoan uyuni-master-pgsql uyuni-master-pgsql-4eclipse uyuni-master-nodejs uyuni-push-to-obs

uyuni-master-root::
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

uyuni-master-base::
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

uyuni-master-cobbler::
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

uyuni-master-gatherer::
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

uyuni-master-spacewalkkoan::
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

uyuni-master-pgsql::
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

uyuni-master-pgsql-4eclipse::
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

uyuni-master-nodejs::
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

uyuni-push-to-obs::
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
