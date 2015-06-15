import java.io
import com.jcraft
import java.util

#
# created by amercian on 06/15/2015
# test Python VM SSH module

msg = ""

jsch = com.jcraft.jsch.JSch()
session = jsch.getSession("root","192.168.121.20", 22)
session.setPassword("MYROOTPASS")
config = java.util.Properties()
config.put("StrictHostKeyChecking","no")
session.setConfig(config)
session.connect()
channel = session.openChannel("exec")
inputstream = channel.getInputStream()
instreamReader = java.io.InputStreamReader(inputstream)
reader = java.io.BufferedReader(instreamReader)

channel.setCommand("pwd;")
channel.connect()

while(msg != None):
    msg = reader.readLine()
    print msg

channel.disconnect()              
session.disconnect()

