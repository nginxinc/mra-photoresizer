all:
	docker build -t ngra/photoresizer .

run:
	docker run -d -P --env-file=".env" ngra/photoresizer