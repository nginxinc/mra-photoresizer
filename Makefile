tag = ngrefarch/ngra-photoresizer
name = ngra-photoresizer
volumes = -v $(CURDIR)/nginx-resizer.conf:/etc/nginx/nginx-resizer.conf

build:
	docker build -t $(tag) .

clean:
	docker build --no-cache -t $(tag) .

run:
	docker run --env-file .env_dev --name $(name) -it -p 80:80 -p 8000:8000 $(tag)

run-v:
	docker run --env-file .env_dev --name $(name) -it -p 80:80 -p 8000:8000 $(volumes) $(tag)

shell:
	docker run --env-file .env_dev --name $(name) -it -p 80:80 -p 8000:8000 $(volumes) $(tag) bash

push:
	docker push $(tag)