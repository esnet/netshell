#!/bin/bash

# ESnet Network Operating System (ENOS) Copyright (c) 2015, The Regents
# of the University of California, through Lawrence Berkeley National
# Laboratory (subject to receipt of any required approvals from the
# U.S. Dept. of Energy).  All rights reserved.
#
# If you have questions about your rights to use or distribute this
# software, please contact Berkeley Lab's Innovation & Partnerships
# Office at IPO@lbl.gov.
#
# NOTICE.  This Software was developed under funding from the
# U.S. Department of Energy and the U.S. Government consequently retains
# certain rights. As such, the U.S. Government has been granted for
# itself and others acting on its behalf a paid-up, nonexclusive,
# irrevocable, worldwide license in the Software to reproduce,
# distribute copies to the public, prepare derivative works, and perform
# publicly and display publicly, and to permit other to do so.

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
