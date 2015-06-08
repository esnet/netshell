import os
import vmXMLDesc
import vmCreation
import vmDeletion

# Authored by amercian
# on 06/08/2015

def main():
    #Single file which manager different components of VM Actions

    vmName = getVMname()
    vmCreation.lxcCreation(vmName)
    conn = vmCreation.lxcConnect()

    vmDeletion.connectionClose(conn)

if __name__ == "__main__":
     main()
