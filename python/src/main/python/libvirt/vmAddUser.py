import subprocess


# Authored by amercian
# dated 08/07/2015

###############################
#  VM User Configuration      #
###############################

vmName = ""
userName = ""

#name = input("Enter the name of the user: ")

def main(argv):
    global vmName, userName

    try:
	opts, args = getopt.getopt(argv,"hn:u:",["help", "name=","username="])
    except getopt.GetoptError:
	print "Incorrect input options. Use vmAddUser.py -h"
	sys.exit(2)

    for opt, arg in opts:
	if opt in ("-h", "--help"):
	    print 'run as root \n Usage: \n vmAddUser -n <vmName> -u <newUserName>'
	    sys.exit()
	elif opt in ("-n", "--vmname"):
	    vmName = arg
	elif opt in ("-u", "--username"):
	    userName = arg

if __name__ == "__main__":
    global vmName, userName
    main(sys.argv[1:])

    #by default need to run as ROOT
    command = "/var/lib/libvirt/lxc/{}/".format(vmName)
    subprocess.call(["chroot", command, "/bin/bash", "-c", "useradd {}".format(userName)])

    subprocess.call(["chroot", command, "/bin/bash", "-c", "passwd {}".format(userName)])
