define create_dockerfile
	sed -e"s/PARENT_CONTAINER/$1/g" Dockerfile.template > Dockerfile
endef

define remove_dockerfile
	@rm Dockerfile
endef

