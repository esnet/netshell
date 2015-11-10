#!/usr/bin/python

import argparse
import sys


#Note: If argparse throws error related to sys.path or sys.prefix, please ensure both are set to the
# correct Jython path in your environment.

def main():
	parser = argparse.ArgumentParser(description='Remote command execution',
									formatter_class=argparse.ArgumentDefaultsHelpFormatter)
	parser.add_argument('-s','--src',  
						required=True,
						help='source host')
	parser.add_argument('-c','--client',
						required=True,
						help='client host')
	parser.add_argument('-t','--testduration',
						required=True,
						type=int,
						help='duration of test in seconds')
	parser.add_argument('-i','--interval',
						required=True,
						type=int,
						help='interval between each test (in seconds)')
	parser.add_argument('-T','--TTL',
						required=True,
						type=int,
						help='how long should the test last(in seconds)')
	parser.add_argument('-u','--username',
						required=True,
						help='user name to write test results to the database')
	parser.add_argument('-k','--key',
						required=True,
						help='User key to write test results to the database')
	parser.add_argument('-u','--uri',
						required=True,
						help='Database uri')


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
