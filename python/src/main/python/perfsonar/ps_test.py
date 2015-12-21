#!/usr/bin/python

import argparse
import sys
import net.es.netshell.kernel.perfsonar.Bwctl
import java.io.OutputStream
import sys


#Note: If argparse throws error related to sys.path or sys.prefix, please ensure both are set to the
# correct Jython path in your environment.

def main():
	parser = argparse.ArgumentParser(description='Run perfsonar test',
									formatter_class=argparse.ArgumentDefaultsHelpFormatter)
	parser.add_argument('-s','--source',  
						required=True,
						help='source host')
	parser.add_argument('-c','--client',
						required=True,
						help='client host')
	parser.add_argument('-u','--username',
						help='user name to write test results to the database')
	parser.add_argument('-k','--key',
						help='User key to write test results to the database')
	parser.add_argument('-U','--uri',
						help='Database uri')



	args = parser.parse_args()

	bwctl = net.es.netshell.kernel.perfsonar.Bwctl.getInstance()


	result=None

	if(args.source and args.client and args.username and args.key and args.uri):
		result = bwctl.runPersistentBwctlTest(args.source,args.client, args.username, args.key, args.uri)
	elif (args.source and args.client):
		result = bwctl.runBwctlTest(args.source,args.client)
	else:
		print "Insufficient arguments \n"


	

	if(result):
	    print "Ran bwctl test."
	else:
		print "Error running test."

if __name__ == '__main__':
	main()
