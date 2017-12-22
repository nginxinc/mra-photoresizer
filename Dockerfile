FROM openjdk:8-jdk

ARG CONTAINER_ENGINE_ARG
ARG USE_NGINX_PLUS_ARG
ARG USE_VAULT_ARG

ENV USE_NGINX_PLUS=${USE_NGINX_PLUS_ARG:-true} \
    USE_VAULT=${USE_VAULT_ARG:-false} \
# CONTAINER_ENGINE specifies the container engine to which the
# containers will be deployed. Valid values are:
# - kubernetes
# - mesos (default)
# - local
    CONTAINER_ENGINE=${CONTAINER_ENGINE_ARG:-kubernetes}

COPY nginx/ssl /etc/ssl/nginx/
#Install Required packages for installing NGINX Plus
RUN apt-get update && apt-get install -y \
	jq \
	libffi-dev \
	libssl-dev \
	make \
	wget \
	vim \
	curl \
	apt-transport-https \
	ca-certificates \
	curl \
	librecode0 \
	libsqlite3-0 \
	libxml2 \
	lsb-release \
	unzip \
	maven \
	gnupg \
	--no-install-recommends && rm -r /var/lib/apt/lists/* && \
    mkdir -p /etc/ssl/nginx

# Install nginx
ADD install-nginx.sh /usr/local/bin/
COPY ./nginx /etc/nginx/
RUN /usr/local/bin/install-nginx.sh

# forward request and error logs to docker log collector
RUN ln -sf /dev/stdout /var/log/nginx/access_log && \
	ln -sf /dev/stderr /var/log/nginx/error_log && \
	chown -R nginx /var/log/nginx/

#COPY app/start.sh app/PhotoResizer.yaml /app/
COPY app /build/
WORKDIR /build
RUN mvn clean install && \
#    mvn package && \
#    cp target/PhotoResizer-1.0.1-SNAPSHOT.jar /app && \
    rm -r target/apidocs target/classes target/dependency-maven-plugin-markers \
    target/generated-sources target/generated-test-sources target/javadoc-bundle-options \
    target/maven-archiver target/maven-status target/surefire-reports target/test-classes
COPY ./status.html /usr/share/nginx/html/status.html

#WORKDIR /app

EXPOSE 80 8000 12005

CMD ["./start.sh"]
