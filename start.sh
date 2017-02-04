#!/bin/sh
NGINX_PID="/var/run/nginx.pid"    # /   (root directory)
APP="java -jar ./PhotoResizer-1.0.1-SNAPSHOT.jar server ./PhotoResizer.yaml"

NGINX_CONF="/etc/nginx/nginx.conf";
NGINX_FABRIC="/etc/nginx/nginx-fabric.conf";

if [ "$NETWORK" = "fabric" ]
then
    NGINX_CONF=$NGINX_FABRIC;
    echo This is the nginx conf = $NGINX_CONF;
    echo fabric configuration set;
fi

$APP &

nginx -c "$NGINX_CONF" -g "pid $NGINX_PID;"

service amplify-agent start

sleep 30
APP_PID=`ps aux | grep "$APP" | grep -v grep`

while [ -f "$NGINX_PID" ] &&  [ "$APP_PID" ];
do 
	sleep 5;
	APP_PID=`ps aux | grep "$APP" | grep -v grep`;
	#echo "The python process: $PID"
done