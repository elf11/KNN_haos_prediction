#!/bin/bash

user=`whoami`

if [ $user != "root" ]
then
	echo "You must be root"
	exit 1
fi

cd WebKNN
ant war
cd ..

cp WebKNN/dist/knn-demo.war /var/lib/jetty/webapps
chmod 777 /var/lib/jetty/webapps/knn-demo.war
service jetty restart

exit 0
