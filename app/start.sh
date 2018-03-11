#!/bin/sh
APP="java -jar target/PhotoResizer-1.0.1-SNAPSHOT.jar server PhotoResizer.yaml"

if [ "$NETWORK" = "fabric" ]
then
    echo fabric configuration set;
    NGINX_PID="/var/run/nginx.pid"    # /   (root directory)
    NGINX_CONF="/etc/nginx/nginx.conf";
    nginx -c "$NGINX_CONF" -g "pid $NGINX_PID;"
fi

$APP &

sleep 30
APP_PID=`ps aux | grep "$APP" | grep -v grep`

if [ "$NETWORK" = "fabric" ]
then
    while [ -f "$NGINX_PID" ] &&  [ "$APP_PID" ];
    do
	    sleep 5;
	    APP_PID=`ps aux | grep "$APP" | grep -v grep`;
    done
else
    while [ "$APP_PID" ];
    do
	    sleep 5;
	    APP_PID=`ps aux | grep "$APP" | grep -v grep`;
    done
fi

