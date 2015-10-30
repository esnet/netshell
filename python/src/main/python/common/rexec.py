#!/usr/bin/python

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
						help='Command to be executed in the remote host. Place the command within double quotes\
						For example: -c "ls -l"',
						nargs='+')

	args = parser.parse_args()

	if(args.command):
		command = " ".join(args.command)
	else:
		print "Please specify command to be executed in remote host"

	print "Executing..."+command

	net.es.netshell.api.RemoteExecution.sshExec(args.host,args.port,args.login,args.password,command)

	print "Executed successfully: "+command

if __name__ == '__main__':
	main()