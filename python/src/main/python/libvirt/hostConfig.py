import subprocess


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
	opts,, args = getopt.getopt(argv,"hn:",["help", "name="])
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
