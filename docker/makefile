CURDIR := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))

# Build a docker image.
image:
	cp ../release/kurorin.jar .
	cp ../app-config.edn .
	docker build --tag=kurorin .
	rm kurorin.jar app-config.edn

# Run container and jetty.
serve:
	mkdir -p books
	docker run --rm -it \
		-v $(CURDIR)books:/tmp/kurorin \
		--name kurorin \
		-p 3333:3333 \
		kurorin

# Work on shell.
shell:
	mkdir -p books
	docker run --rm -it \
		-v $(CURDIR)books:/tmp/kurorin \
		--name kurorin \
		kurorin /bin/sh

# Attach to the running server.
attach:
	docker exec -it kurorin /bin/sh

