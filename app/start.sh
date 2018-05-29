#!/bin/sh
NGINX_PID="/var/run/nginx.pid"    # /   (root directory)
APP="java -jar target/PhotoResizer-1.0.1-SNAPSHOT.jar server PhotoResizer.yaml"


su resizer -c ${APP} &

sleep 30
APP_PID=`ps aux | grep "$APP" | grep -v grep`


case "$NETWORK" in
    fabric)
        NGINX_CONF="/etc/nginx/fabric_nginx_$CONTAINER_ENGINE.conf"
        echo 'Fabric configuration set'
        nginx -c "$NGINX_CONF" -g "pid $NGINX_PID;" &

        sleep 10

        while [ -f "$NGINX_PID" ] &&  [ "$APP_PID" ];
        do
	        sleep 5;
	        APP_PID=`ps aux | grep "$APP" | grep -v grep`;
        done
        ;;
    router-mesh)
        while [ "$APP_PID" ];
        do
	        sleep 5;
	        APP_PID=`ps aux | grep "$APP" | grep -v grep`;
        done
        ;;
    proxy)
        while [ "$APP_PID" ];
        do
	        sleep 5;
	        APP_PID=`ps aux | grep "$APP" | grep -v grep`;
        done
        ;;
    *)
        echo 'Network not supported'
esac
