FROM java:latest
COPY PhotoResizer* /app/
WORKDIR /app
CMD java -jar ./PhotoResizer-1.0-SNAPSHOT.jar server ./PhotoResizer.yaml
EXPOSE 8080

