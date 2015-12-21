#!/usr/bin/python
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

import net.es.netshell.api.RemoteExecution

import argparse
import sys


#Note: If argparse throws error related to sys.path or sys.prefix, please ensure both are set to the
# correct Jython path in your environment.

def main():
	parser = argparse.ArgumentParser(description='Remote command execution',
									formatter_class=argparse.ArgumentDefaultsHelpFormatter)
	parser.add_argument('-H','--host',  
						required=True,
						help='Remote host')
	parser.add_argument('-p','--port',
						required=True,
						type=int,
						help='Remote port')
	parser.add_argument('-l','--login',
						required=True,
						help='Remote login id')
	parser.add_argument('-P','--password',
						required=True,
						help='Password on remote host')
	parser.add_argument('-c','--command',
						required=True,
						help='Command to be executed in the remote host. NOTE: -c <command> must always the last \
						option',
						nargs=argparse.REMAINDER)

	args = parser.parse_args()
	print args

	if(args.command):
		command = " ".join(args.command)
	else:
		print "Please specify command to be executed in remote host"

	print "Executing..."+command

	net.es.netshell.api.RemoteExecution.sshExec(args.host,args.port,args.login,args.password,command)

	print "Done"

if __name__ == '__main__':
	main()
