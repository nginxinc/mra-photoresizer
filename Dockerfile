FROM java:latest

#NGINX Plus install
ADD nginx-repo.crt /etc/ssl/nginx/
ADD nginx-repo.key /etc/ssl/nginx/

# Get other files required for installation
RUN apt-get update && apt-get install -y \
    wget \
    apt-transport-https \
    libcurl3-gnutls \
    lsb-release \
    python

RUN wget -q -O /etc/ssl/nginx/CA.crt https://cs.nginx.com/static/files/CA.crt && \
    wget -q -O - http://nginx.org/keys/nginx_signing.key | apt-key add - && \
    wget -q -O /etc/apt/apt.conf.d/90nginx https://cs.nginx.com/static/files/90nginx && \
    printf "deb https://plus-pkgs.nginx.com/debian `lsb_release -cs` nginx-plus\n" >/etc/apt/sources.list.d/nginx-plus.list

# Install NGINX Plus
RUN apt-get update && apt-get install -y nginx-plus

# forward request logs to Docker log collector
RUN ln -sf /dev/stdout /var/log/nginx/access.log && \
    ln -sf /dev/stdout /var/log/nginx/error.log

COPY ./nginx-resizer.conf /etc/nginx/nginx-resizer.conf
RUN chown -R nginx /var/log/nginx/
COPY /resizer-start.sh /app/

# Amplify
COPY ./amplify_install.sh /app/amplify_install.sh
RUN API_KEY='0202c79a3d8411fcf82b35bc3d458f7e' HOSTNAME='resizer' sh /app/amplify_install.sh

#Java app
COPY target/PhotoResizer-1.0.1-SNAPSHOT.jar /app/
COPY PhotoResizer.yaml /app/

#Run app
WORKDIR /app
CMD ["./resizer-start.sh"]
EXPOSE 80 8080 