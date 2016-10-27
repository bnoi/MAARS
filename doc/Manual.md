#Installation
##Basic User
  -Copy built MAARS plugin (`jars/MAARS_-1.0-SNAPSHOT.jar`) inside `Micro Manager folder/mmplugins`.
  
  -Copy dependencies (`jars/maars_dependencies/`) inside `Micro Manager folder/plugins/maars_dependencies`.
  
##Developer
- `build-mm.sh` : use this script to build Micro-Manager under Linux.

- `mavenized-mm.sh` : use this script to update local Maven repository inside `repo/`. MM jars used are `MMJ_`, `MMCoreJ` and `MMAcqEngine`.

- `update-libs.sh` : use Maven to build MAARS and copy all dependencies to `jars/`. It allows "basic users" to copy the required .jar file to a standard Micro-Manager installation.

- `install.sh` : Use to install MAARS plugin and its dependencies to Micro-Manager installation.
###Eclipse/Netbean/IntelliJ
#Configuration
##Micro-Manager 2.0
#TroubleShooting
