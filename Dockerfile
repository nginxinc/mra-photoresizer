FROM openjdk:latest

ENV USE_NGINX_PLUS=true \
    NETWORK=fabric

COPY vault_env.sh /etc/letsencrypt/
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
	--no-install-recommends && rm -r /var/lib/apt/lists/*  && \
# Install vault client
    wget -q https://releases.hashicorp.com/vault/0.6.0/vault_0.6.0_linux_amd64.zip && \
    unzip -d /usr/local/bin vault_0.6.0_linux_amd64.zip && \
    mkdir -p /etc/ssl/nginx

# Install nginx
ADD install-nginx.sh /usr/local/bin/
COPY ./nginx /etc/nginx/
RUN /usr/local/bin/install-nginx.sh

# forward request and error logs to docker log collector
RUN ln -sf /dev/stdout /var/log/nginx/access.log && \
	ln -sf /dev/stderr /var/log/nginx/error.log && \
	chown -R nginx /var/log/nginx/

COPY start.sh PhotoResizer.yaml /app/
COPY . /build/
WORKDIR /build
RUN mvn clean install && \
    mvn package && \
    cp target/PhotoResizer-1.0.1-SNAPSHOT.jar /app && \
    rm -r target/apidocs target/classes target/dependency-maven-plugin-markers target/generated-sources target/generated-test-sources target/javadoc-bundle-options target/maven-archiver target/maven-status target/surefire-reports target/test-classes
COPY ./status.html /usr/share/nginx/html/status.html

WORKDIR /app
CMD ["./start.sh"]
EXPOSE 80 8000