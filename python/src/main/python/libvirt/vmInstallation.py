import sys, getopt
import operator
import subprocess
import select
import fileinput
from os.path import expanduser

# Authored by amercian
# on 07/06/2015

#########################################################
# 	     VM ENOS/Netshell Installation              #
#########################################################

#TODO: test automation

name = "" #VM name 
command = "" #path to chroot
root = ""  #path to VM root dir

#name = input('Enter the name of the VM: ')

def main(argv):
    global name

    try:
	opts, args = getopt.getopt(argv,"hn:",["help", "name="])
    except getopt.GetoptError:
	print "Incorrect input options. Use vmInstallation -h"
	sys.exit(2)

    for opt, arg in opts:
	if opt in ("-h", "--help"):
	    print 'vmInstallation -n <username>'
	    sys.exit()
	elif opt in ("-n", "--name"):
	    name = arg

def subprocess_cmd(command):
   process = subprocess.Popen(command,stdout=subprocess.PIPE, shell=True)
   proc_stdout = process.communicate()[0].strip()
   print proc_stdout



if __name__ == "__main__":
    global name
    main(sys.argv[1:])

    command = "/var/lib/libvirt/lxc/{}/".format(name)
    root = command + "root/"

    #scp java jdk to lxc root home
    subprocess.call(["sudo","cp","{}/jdk-7u79-linux-x64.gz".format(expanduser("~")),"{}".format(root)])
    #untar
    subprocess.call(["chroot", command, "/bin/bash", "-c", "tar xfz jdk-7u79-linux-x64.gz"])

    #append the following lines to bash_profile
    with open("{}/.bash_profile".format(root), "a") as myfile:
	myfile.write("export PATH=/root/jdk1.7.0_79/bin:$PATH \n \
		 export JAVA_HOME=/root/jdk1.7.0_79")

    #restart bash
    subprocess.call(["chroot", command, "/bin/bash", "-c", "source {}/.bash_profile".format(expanduser("~"))])

    #install dependencies
    subprocess.call(["chroot", command, "/bin/bash", "-c", "yum install -y wget git maven"])
    subprocess.call(["chroot", command, "/bin/bash", "-c", "yum -y update"])

    #install jython
    subprocess.call(["chroot", command, "/bin/bash", "-c", "wget http://search.maven.org/remotecontent?filepath=org/python/jython-installer/2.7.0/jython-installer-2.7.0.jar -O root/jython_installer-2.7.0.jar"])

    #install jython with options - How?
    subprocess.call(["chroot", command, "/bin/bash", "-c", "java -jar root/jython_installer-2.7.0.jar --console"])

    #install Netshell and ENOS - credential required how?
    subprocess.call(["chroot", command, "/bin/bash", "-c", "git clone https://github.com/esnet/netshell.git"])

    subprocess.call(["chroot", command, "/bin/bash", "-c", "git clone https://github.com/esnet/enos.git"])

    #mvn install netshell
    netshell_kernel = root + "/netshell/kernel"
    subprocess.call(["chroot", netshell_kernel, "/bin/bash", "-c", "mvn install"])

    netshell_python = root + "/netshell/python"
    subprocess.call(["chroot", netshell_python, "/bin/bash", "-c", "mvn install"])

    netshell_controller = root + "/netshell/controller"
    subprocess.call(["chroot", netshell_controller, "/bin/bash", "-c", "mvn install"])

    netshell_odl = root + "/netshell/odl"
    subprocess.call(["chroot", netshell_odl, "/bin/bash", "-c", "mvn install"])

    subprocess.call(["chroot", command, "/bin/bash", "-c", "mkdir /var/netshell"])
    subprocess.call(["chroot", command, "/bin/bash", "-c", "chown -R root /var/netshell"])

    #Karaf
    subprocess.call(["chroot", command, "/bin/bash", "-c", "wget https://nexus.opendaylight.org/content/groups/public/org/opendaylight/integration/distribution-karaf/0.2.3-Helium-SR3/distribution-karaf-0.2.3-Helium-SR3.tar.gz"])

    #untar karaf
    subprocess.call(["chroot", command, "/bin/bash", "-c", "tar xzf distribution-karaf-0.2.3-Helium-SR3.tar.gz"])

    #Configuring Netshell

    #create file /root/netshell.conf
    with open("{}/netshell.conf".format(root), "w") as myfile:
	myfile.write("""{ \n \
			"global": { \n \
				"defaultLogLevel":		"info", \n \
				"rootDirectory":		"/root/netshell-root", \n \
				"securityManagerDisabled":	1, \n \
				"sshDisabled":			0, \n \
				"sshIdleTimeout":		3600000, \n \
				"sshPort":          8000 \n \
    			} \n \
		     }""")

    #append to bash_profile
    with open("{}/.bash_profile".format(root), "a") as myfile:
	myfile.write("""export JAVA_OPTS="-Xmx8192m -XX:MaxPermSize=2048m" \n \
         export JAVA_OPTS=$JAVA_OPTS" -Dnetshell.configuration=/root/netshell.conf" """)

    subprocess.call(["chroot", command, "/bin/bash", "-c", "source .bash_profile"])

    subprocess.call(["chroot", command, "/bin/bash", "-c", "mkdir netshell-root/etc"])
    subprocess.call(["chroot", command, "/bin/bash", "-c", "touch netshell-root/etc/init.py"])

    with open("{}/netshell-root/etc/profile.py".format(root), "w") as myfile:
	myfile.write(""" import sys \n \
		  from net.es.netshell.boot import BootStrap \n \
		  from net.es.netshell.kernel.exec import KernelThread \n \
		  from java.nio.file import Paths \n \
		  \n \
		  def realPathName(path): \n \
		  \t return Paths.get(sys.netshell_root.toString(), path) \n \
		  \n \
		  def pwd(): \n \
		  \t return sys.netshell_root \n \
		  \n
		  def cd(path): \n
		  \t sys.netshell_root = realPathName(path) \n \
		  \n \
		  sys.netshell_root = BootStrap.rootPath \n \
		  \n \
		  if not sys.netshell_root.toString() + "/lib" in sys.path: \n \
		  \t sys.path.append (sys.netshell_root.toString() + "/lib") \n \
		  \t sys.path.append ('/root/jython/Lib') \n \
	  	  \t sys.path.append('/root/jython/Lib/site-packages/') \n \
	  	  \t sys.path.append (pwd().toString()) """)

# TODO feature install bundles in karaf - How?
