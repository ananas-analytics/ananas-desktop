# main

This package includes the code that works on backend (typically nodejs). It contains two subsystems:

- electron, exists in the electron folder
- nwjs, exists in the nwjs folder

Other files are common implementations across the two different subsystems

UI package access these functions from the interfaces, so that we can switch the backend implementation from one subsystems to another.


