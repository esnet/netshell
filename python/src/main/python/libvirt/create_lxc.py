#!/usr/bin/python

import libvirt 
import os 
import argparse 
import random 
 
 
class Paths(object): 
    CONTAINER_PATH = '/containers/{0}' 
    CONTAINER_ETC = CONTAINER_PATH + '/etc' 
    CONTAINER_INIT_D = CONTAINER_ETC + '/init.d' 
    CONTAINER_NETWORK = CONTAINER_ETC + '/network' 
    CONTAINER_HAPROXY = CONTAINER_ETC + '/haproxy' 
    BASE_CONTAINER = '~/containers_base' 
 
 
def create_domain(name): 
    container_path = Paths.CONTAINER_PATH.format(name) 
    container_etc = Paths.CONTAINER_ETC.format(name) 
    os.system('mkdir -p {0}'.format(container_path)) 
    os.system('cp -r {0}/* {1}'.format(Paths.BASE_CONTAINER, container_etc)) 
    domain_xml = ''' 
<domain type="lxc"> 
  <name>{0}</name> 
  <memory>102400</memory> 
  <currentmemory>102400</currentmemory> 
  <vcpu>1</vcpu> 
  <os> 
    <type arch="x86_64">exe</type> 
    <init>/bin/startup.sh</init> 
  </os> 
  <clock offset="utc"> 
  <on_poweroff>destroy</on_poweroff> 
  <on_reboot>restart</on_reboot> 
  <on_crash>destroy</on_crash> 
  <devices> 
    <emulator>/usr/lib/libvirt/libvirt_lxc</emulator> 
    <filesystem accessmode="passthrough" type="mount"> 
      <source dir="{1}/etc/network"></source> 
      <target dir="/etc/network"> 
    </target></filesystem> 
    <filesystem accessmode="passthrough" type="mount"> 
      <source dir="{1}/etc/init.d"></source> 
      <target dir="/etc/init.d"> 
    </target></filesystem> 
    <filesystem accessmode="passthrough" type="mount"> 
      <source dir="{1}/etc/haproxy"></source> 
      <target dir="/etc/haproxy"> 
    </target></filesystem> 
    <interface type="network"> 
      <source network="default"></source> 
    </interface> 
    <console type="pty"> 
      <target port="0" type="lxc"> 
    </target></console> 
  </devices> 
</clock></domain> 
    ''' 
    domain_xml = domain_xml.format(name, container_path) 
    lxc_conn = libvirt.open('lxc:///') 
    domain = lxc_conn.defineXML(domain_xml) 
    if domain.create() == 0: 
        print '{0} domain created successfully.'.format(name) 
        return domain 
    else: 
        print '{0} did NOT create successfully.'.format(name) 
 
 
def delete_domain(domain): 
    try: 
        domain.destroy() 
        domain.undefine() 
    except Exception as destroy_exception: 
        try: 
            domain.undefine() 
        except Exception as undefine_exception: 
            print undefine_exception 
            print 'Could not clean up domain.' 
 
 
def delete_domain_by_name(name): 
    lxc_conn = libvirt.open('lxc:///') 
    try: 
        domain = lxc_conn.lookupByName(name) 
    except: 
        print 'Failed to find the domain {0}'.format(name) 
        return 
    delete_domain(domain) 
 
 
if __name__ == '__main__': 
    parser = argparse.ArgumentParser() 
    parser.add_argument('container_name', metavar='container_name', type=str, 
                        help='Name of container to create.') 
    parser.add_argument('-d', '--delete', action='store_true', 
                        help='Delete container name specified.') 
    args = parser.parse_args() 
    if args.delete: 
        delete_domain_by_name(args.container_name) 
    else: 
        create_domain(args.container_name) 
