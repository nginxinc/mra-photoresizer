#!/bin/sh
conf="/var/run/nginx.pid"    # /   (root directory)

java -jar ./PhotoResizer-1.0.1-SNAPSHOT.jar server ./PhotoResizer.yaml &

nginx -c /etc/nginx/nginx-resizer.conf -g "pid $conf;" &

service amplify-agent start

sleep 500

while [ -f "$conf" ]
do 
	sleep 500;
done