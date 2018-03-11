FROM openjdk:8-jdk

ARG CONTAINER_ENGINE_ARG
ARG USE_NGINX_PLUS_ARG
ARG USE_VAULT_ARG

# CONTAINER_ENGINE specifies the container engine to which the
# containers will be deployed. Valid values are:
# - kubernetes (default)
# - mesos
# - local
ENV USE_NGINX_PLUS=${USE_NGINX_PLUS_ARG:-true} \
    USE_VAULT=${USE_VAULT_ARG:-false} \
    CONTAINER_ENGINE=${CONTAINER_ENGINE_ARG:-kubernetes}

COPY nginx/ssl /etc/ssl/nginx/

# Install Required packages for installing NGINX Plus
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

# Install nginx and forward request and error logs to docker log collector
ADD install-nginx.sh /usr/local/bin/
COPY ./nginx /etc/nginx/
RUN /usr/local/bin/install-nginx.sh && \
    ln -sf /dev/stdout /var/log/nginx/access_log && \
	ln -sf /dev/stderr /var/log/nginx/error_log && \
	chown -R nginx /var/log/nginx/

# Copy and build application
COPY app /build/
WORKDIR /build
RUN mvn clean install && \
    rm -r target/apidocs target/classes target/dependency-maven-plugin-markers \
    target/generated-sources target/generated-test-sources target/javadoc-bundle-options \
    target/maven-archiver target/maven-status target/surefire-reports target/test-classes

EXPOSE 80 8000 12005

CMD ["./start.sh"]
