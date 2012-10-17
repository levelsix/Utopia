#!/bin/bash

OPENFILES=`/usr/sbin/lsof | grep tomcat | wc -l`
WHENTORESTART=$RANDOM
RANGE=500
let "WHENTORESTART %= $RANGE"
WHENTORESTART=$(($WHENTORESTART + 3000))
if [ "$OPENFILES" -gt "$WHENTORESTART" ]
then
	echo Tomcats open files $OPENFILES is greater than $WHENTORESTART so restarting
	/etc/init.d/tomcat7 restart
else
		echo Tomcats open files $OPENFILES is not greater than $WHENTORESTART so not restarting
fi
