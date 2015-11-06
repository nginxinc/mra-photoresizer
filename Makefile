all:
	docker build -t ngra/photoresizer .

run:
	docker run -d -P --env-file=".env_dev" ngra/photoresizer
	
debug:
	docker run -P --env-file=".env_dev" ngra/photoresizer
	
		