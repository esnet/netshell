#
# ENOS, Copyright (c) 2015, The Regents of the University of California,
# through Lawrence Berkeley National Laboratory (subject to receipt of any
# required approvals from the U.S. Dept. of Energy).  All rights reserved.
#
# If you have questions about your rights to use or distribute this software,
# please contact Berkeley Lab's Technology Transfer Department at TTD@lbl.gov.
#
# NOTICE.  This software is owned by the U.S. Department of Energy.  As such,
# the U.S. Government has been granted for itself and others acting on its
# behalf a paid-up, nonexclusive, irrevocable, worldwide license in the Software
# to reproduce, prepare derivative works, and perform publicly and display
# publicly.  Beginning five (5) years after the date permission to assert
# copyright is obtained from the U.S. Department of Energy, and subject to
# any subsequent five (5) year renewals, the U.S. Government is granted for
# itself and others acting on its behalf a paid-up, nonexclusive, irrevocable,
# worldwide license in the Software to reproduce, prepare derivative works,
# distribute copies to the public, perform publicly and display publicly, and
# to permit others to do so.
#

from java.lang import Thread, ThreadGroup

import jarray

rootThreadGroup = None

def getRootThreadGroup():
    global rootThreadGroup

    if rootThreadGroup != None:
        return rootThreadGroup
    tg = Thread.currentThread().getThreadGroup()
    ptg = tg.getParent()
    while  ptg != None:
        tg = ptg
        ptg = tg.getParent()
    return tg

def getThreadGroup(name):
    groups = getAllThreadGroups()
    for group in groups:
        if group.getName().equals(name):
            return group;
    return None;

def getAllThreadGroups():
    root = getRootThreadGroup()
    nbGroups = root.activeGroupCount()
    groups = None
    while True:
        groups = jarray.zeros(nbGroups, ThreadGroup)
        n = root.enumerate(groups, True)
        if n == nbGroups:
            nbGroups *= 2
        else:
            nbGroups = n
            break
    return groups[0:(len(groups) - nbGroups) * -1]

def getAllThreads(match=None):
    root = getRootThreadGroup()
    nbThreads = root.activeGroupCount()
    threads = None
    while True:
        threads = jarray.zeros(nbThreads, Thread)
        n = root.enumerate(threads, True)
        if n == nbThreads:
            nbThreads *= 2
        else:
            nbThreads = n
            break
    threads =  threads[0:(len(threads) - nbThreads) * -1]

    if match != None:
        filtered = []
        for thread in threads:
            if match in thread.getName():
                filtered.append(thread)
        threads = filtered
    return threads

def getThread(id):
    threads = getAllThreads()
    for thread in threads:
        if thread.getId() == id:
            return thread
    return None

def displayThread(thread, stack=True):
    name = thread.getName()
    print "Thread id=",thread.getId(),name
    print "  Stack Trace:\n"
    if stack:
        st = thread.getStackTrace()
        index = len(st)
        for trace in st:
            print " ",index, trace


def print_syntax():
    print
    print "threadctl <cmd> <cmds options>"
    print "Java Threads tool"
    print " Commands are:"
    print "\thelp"
    print "\tPrints this help."
    print "\tshow-thread <tid | all> [grep <string>] Displays a thread by its id or all threads"
    print "\t\tAn optional string to match can be provided."
    print "\tshow-threadgroup all [grep <string>] Displays all thread groups."
    print "\t\tAn optional string to match can be provided."


if __name__ == '__main__':
    argv = sys.argv

    if len(argv) == 1:
        print_syntax()
        sys.exit()
    cmd = argv[1]
    if cmd == "help":
        print_syntax()
    elif cmd == "show-thread":
        gri = argv[2]
        if gri == 'all':
            match = None
            if 'grep' in argv:
                match = argv[4]
            threads = getAllThreads(match=match)
            if threads != None:
                for thread in threads:
                    displayThread(thread=thread)
                    print
        else:
            thread = getThread(long(argv[2]))
            if (thread == None):
                print "unknown",argv[2]
                sys.exit()
            displayThread(thread)