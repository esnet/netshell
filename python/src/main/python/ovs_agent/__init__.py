#!/usr/bin/env python

import argparse

def __add_install_parser(parser):
    print "Not yet implemented"

def __add_configure_parser(parser):
    print "Not yet implemented"

def main():
    parser = argparse.ArgumentParser(description="ovs agent",
                                     prog="ovs_agent",
                                     formatter_class=argparse.ArgumentDefaultsHelpFormatter)

    subparsers = parser.add_subparsers()
    __add_install_parser(subparsers)
    __add_configure_parser(subparsers)
    

    args = parser.parse_args()
    if len(args.__dict__) == 0:
        print "Parser not yet implemented"
        sys.exit(0)


    args.func(args)


if __name__ == "__main__":
    main()
