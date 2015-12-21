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

import subprocess
import sys, getopt
import operator

# Authored by amercian
# dated 07/08/2015

###############################
#  Host User Configuration    #
###############################

hostName = ""

#name = input("Enter the name of the user: ")

def main(argv):
    global hostName

    try:
	opts, args = getopt.getopt(argv,"hn:",["help", "name="])
    except getopt.GetoptError:
	print "Incorrect input options. Use hostConfig.py -h"
	sys.exit(2)

    for opt, arg in opts:
	if opt in ("-h", "--help"):
	    print 'hostConfig -n <username>'
	    sys.exit()
	elif opt in ("-n", "--name"):
	    hostName = arg

if __name__ == "__main__":
    global hostName
    main(sys.argv[1:])

    #by default need to run as ROOT
    subprocess.call(["sudo","adduser", "{}".format(hostName)])

    subprocess.call(["passwd", "{}".format(hostName)])
