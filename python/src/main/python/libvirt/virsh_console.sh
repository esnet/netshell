#!/bin/bash

#temporary solution to send service network restart to VM
#TODO need to be remove clear text password

#obtained from https://gist.github.com/shalk/7003628
#modified by amercian on 08/13/2015

expect -c "
    set timeout 10
    spawn virsh -c lxc:/// console $1
    expect {
	\"Escape character\" {send \"\r\r\" ; exp_continue} 
	\"Escape character\" {send \"\r\r\" ; exp_continue} 
	\"login:\" {send \"root\r\"; exp_continue}
	\"Password:\" {send \"NEWROOTPW\r\";} 
	} 
	expect \"~ #\"
	send \"echo  123\r\" 
	expect \"~ #\"
	send \"date\r\"
	send \"service network restart\r\"
	send \"exit\r\"
	expect \"login:\"
	send \"\"
	expect eof
"
