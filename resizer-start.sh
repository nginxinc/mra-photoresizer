#!/bin/sh
conf="/var/run/nginx.pid"    # /   (root directory)

java -jar ./PhotoResizer-1.0.1-SNAPSHOT.jar server ./PhotoResizer.yaml &

nginx -c /etc/nginx/nginx-resizer.conf &

service amplify-agent start

sleep 30

while [ -f "$conf" ]
do 
	sleep 5;
done