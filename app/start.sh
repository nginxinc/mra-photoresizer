#!/bin/sh
NGINX_PID="/var/run/nginx.pid"    # /   (root directory)
APP="java -jar target/PhotoResizer-1.0.1-SNAPSHOT.jar server PhotoResizer.yaml"

NGINX_CONF="/etc/nginx/nginx.conf";

nginx -c "$NGINX_CONF" -g "pid $NGINX_PID;" &

su resizer -c 'java -jar target/PhotoResizer-1.0.1-SNAPSHOT.jar server PhotoResizer.yaml' &

sleep 30
APP_PID=`ps aux | grep "$APP" | grep -v grep`

while [ -f "$NGINX_PID" ] &&  [ "$APP_PID" ];
do 
	sleep 5;
	APP_PID=`ps aux | grep "$APP" | grep -v grep`;
done
