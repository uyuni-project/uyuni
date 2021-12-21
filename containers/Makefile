BUILDER := podman
REGISTRY := registry.tf.local
REGISTRY_TLS_VERIFY := false
IMAGES := $(wildcard *-image)


# building
build_targets := $(patsubst %-image,%,$(IMAGES))

build: $(build_targets)

$(build_targets):
	@${BUILDER} build -f $@-image/Dockerfile --build-arg BASE_IMAGE_PREFIX=registry.suse.com/ -t $@ ..

# pushing to registry
push_targets := $(patsubst %-image,push-%,$(IMAGES))

push: $(push_targets)

$(push_targets): push-%-image: %
	@${BUILDER} tag $< ${REGISTRY}/$<
	@${BUILDER} image push --tls-verify=$(REGISTRY_TLS_VERIFY) ${REGISTRY}/$<
