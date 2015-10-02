#!/usr/bin/env python

import argparse
import install
import configure
import container_management

def __add_install_parser(subparser):
    parser_installer = subparser.add_parser('install',
                                             formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser_installer.set_defaults(func=install.main)


def __add_configure_parser(subparser):
    parser_configure = subparser.add_parser('configure',
                                            formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser_configure.set_defaults(func=configure.main)
    parser_configure.add_argument('-b', '--bridge',
                                  required=True,
                                  help='Name of ovs bridge')
    parser_configure.add_argument('-i', '--interface',
                                  required=True,
                                  help='The main physical interface to which the ovs bridge is connected')


def __add_container_management_parser(subparser):
    parser_con_mgmt = subparser.add_parser('container',
                                           formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser_con_mgmt.set_defaults(func=container_management.main)
    parser_con_mgmt.add_argument('-i','--interface',
                                 required=True,
                                 help='The virtual interface that the container connects to')
    parser_con_mgmt.add_argument('-a','--action',
                                 required=True,
                                 help='Specify action to be performed on interface.',
                                 choices=['add','del'])

def main():
    parser = argparse.ArgumentParser(description="ovs agent",
                                     prog="ovs_agent",
                                     formatter_class=argparse.ArgumentDefaultsHelpFormatter)

    subparsers = parser.add_subparsers()
    __add_install_parser(subparsers)
    __add_configure_parser(subparsers)
    __add_container_management_parser(subparsers)
    

    args = parser.parse_args()
    if len(args.__dict__) == 0:
        print "Parser not yet implemented"
        sys.exit(0)

    args.func(args)


if __name__ == "__main__":
    main()
