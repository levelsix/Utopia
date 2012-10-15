#!/bin/bash

echo setting up .bashrc

cat >> /home/ec2-user/.bashrc <<EOF

alias taillog='sudo tail -n 100 -f /var/log/tomcat7/kingdom.log'
alias taillogerror='sudo tail -n 100 -f /var/log/tomcat7/kingdom_errors.log'
alias openfiles='sudo lsof | grep tomcat | wc -l'
alias restarttomcat='sudo /etc/init.d/tomcat7 restart'

EOF
