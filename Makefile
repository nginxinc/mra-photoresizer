tag = ng-ref-arch/ngra-photoresizer
name = ngra-photoresizer
volumes = -v $(CURDIR)/nginx-resizer.conf:/etc/nginx/nginx-resizer.conf

build:
	docker build  --build-arg VAULT_TOKEN=$(VAULT_TOKEN) -t $(tag) .

run:
	docker run --env-file .env_dev --name $(name) -it -p 80:80 -p 8080:8080 $(tag)

run-v:
	docker run --env-file .env_dev --name $(name) -it -p 80:80 -p 8080:8080 $(volumes) $(tag)

shell:
	docker run --env-file .env_dev --name $(name) -it -p 80:80 -p 8080:8080 $(volumes) $(tag) bash

push:
	docker push $(name)