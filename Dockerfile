FROM java:latest

# Get other files required for installation
RUN apt-get update && apt-get install -y \
    wget \
    jq \
    apt-transport-https \
    libcurl3-gnutls \
    lsb-release \
    apt-utils \
    python
#
## Download certificate and key from the the vault and copy to the build context
#ARG VAULT_TOKEN
#RUN mkdir -p /etc/ssl/nginx
#RUN wget -q -O - --header="X-Vault-Token: $VAULT_TOKEN" \
#    http://vault.ngra.ps.nginxlab.com:8200/v1/secret/nginx-repo.crt \
#    | jq -r .data.value > /etc/ssl/nginx/nginx-repo.crt
#RUN wget -q -O - --header="X-Vault-Token: $VAULT_TOKEN" \
#    http://vault.ngra.ps.nginxlab.com:8200/v1/secret/nginx-repo.key \
#    | jq -r .data.value > /etc/ssl/nginx/nginx-repo.key
#
## Download NGINX Plus
#RUN wget -q -O /etc/ssl/nginx/CA.crt https://cs.nginx.com/static/files/CA.crt && \
#    wget -q -O - http://nginx.org/keys/nginx_signing.key | apt-key add - && \
#    wget -q -O /etc/apt/apt.conf.d/90nginx https://cs.nginx.com/static/files/90nginx && \
#    printf "deb https://plus-pkgs.nginx.com/debian `lsb_release -cs` nginx-plus\n" >/etc/apt/sources.list.d/nginx-plus.list
#
#RUN apt-get update && apt-get install -y nginx-plus
#
# Download NGINX
ENV NGINX_VERSION 1.9.10-1~jessie

RUN apt-key adv --keyserver hkp://pgp.mit.edu:80 --recv-keys 573BFD6B3D8FBC641079A6ABABF5BD827BD9BF62 \
	&& echo "deb http://nginx.org/packages/mainline/debian/ jessie nginx" >> /etc/apt/sources.list \
	&& apt-get update \
	&& apt-get install -y ca-certificates nginx=${NGINX_VERSION} gettext-base \
	&& rm -rf /var/lib/apt/lists/*
	
# forward request logs to Docker log collector
RUN ln -sf /dev/stdout /var/log/nginx/access.log \
    && ln -sf /dev/stdout /var/log/nginx/error.log

COPY ./nginx-resizer.conf /etc/nginx/nginx-resizer.conf
RUN chown -R nginx /var/log/nginx/
COPY /resizer-start.sh /app/

COPY ../../nginx/nginx /usr/sbin/
COPY ../../nginx/status.html /usr/share/nginx/html/


# Amplify
COPY ./amplify_install.sh /app/amplify_install.sh
RUN API_KEY='0202c79a3d8411fcf82b35bc3d458f7e' HOSTNAME='resizer' sh /app/amplify_install.sh

#Java app
COPY target/PhotoResizer-1.0.1-SNAPSHOT.jar /app/
COPY PhotoResizer.yaml /app/

#Run app
WORKDIR /app
CMD ["./resizer-start.sh"]
#CMD java -jar ./PhotoResizer-1.0.1-SNAPSHOT.jar server ./PhotoResizer.yaml
EXPOSE 80 8000