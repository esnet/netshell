#!/bin/python
#
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
#

from pexpect import pxssh

hostname="localhost"
username="karaf"
password="karaf"
port=8101
cmds = [
("bundle:refresh -f org.apache.sshd.core",5),
("feature:repo-add mvn:net.es/netshell-kernel/1.0-SNAPSHOT/xml/features",1),
("feature:install odl-dlux-core odl-openflowplugin-all",3),
("feature:install odl-dlux-all",2),
("feature:install odl-openflowplugin-adsal-compatibility odl-nsf-managers",20),
("feature:repo-add mvn:com.corsa.pipeline.sdx3/sdx3-features/0.1.1/xml/features",1),
("feature:install corsa-sdx3-all",2),
("feature:install netshell-kernel netshell-python",8),
("feature:install netshell-odl-corsa",2),
("feature:install netshell-odl-mdsal",2),
("feature:install netshell-controller",2),
{"feature:repo-add mvn:net.es/enos-esnet/1.0-SNAPSHOT/xml/features",2),
{"feature:install enos-esnet",5)
]

prompt=['opendaylight-user@root>']

options = ("-o 'RSAAuthentication=no' -o 'PubkeyAuthentication=no' -o 'StrictHostKeyChecking=no' -o 'UserKnownHostsFile=/dev/null'")

try:
	s = pxssh.pxssh() 
	s.SSH_OPTS = options
	s.force_password=True
	s.login(hostname, username, password,port=port,auto_prompt_reset=False)
	s.synch_original_prompt()
	r = s.prompt(timeout=20)
	for (cmd,timeout) in cmds:
		s.sendline(cmd)
		r = s.prompt (timeout = timeout)
	s.logout()
except pxssh.ExceptionPxssh as e:
	print("pxssh failed on login.")
	print(e)

