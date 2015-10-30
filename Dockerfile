FROM java:latest
COPY target/PhotoResizer-1.0-SNAPSHOT.jar /app/
COPY PhotoResizer.yaml /app/
WORKDIR /app
EXPOSE 8080
CMD java -jar ./PhotoResizer-1.0-SNAPSHOT.jar server ./PhotoResizer.yaml

