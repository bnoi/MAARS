# Mitotic Analysing And Recording System

MAARS (Mitotic Analysing And Recording System) is a Micro-Manager plugin designed to automatically record cells in mitosis.

The plugin is designed to be flexible and easy to use whatever the hardware and the type of cells you have.

## Installation

- Copy MAARS plugin (`jars/MAARS_-1.0-SNAPSHOT.jar`) inside `Micro Manager folder/mmplugins`.

- Copy dependencies (`jars/maars_dependencies/`) inside `Micro Manager folder/plugins/maars_dependencies`.

That's all !

## For developers

- `build-mm.sh` : use this script to build Micro-Manager under Linux.

- `mavenized-mm.sh` : use this script to update local Maven repository inside `repo/`. MM jars used are `MMJ_`, `MMCoreJ` and `MMAcqEngine`.

- `update-libs.sh` : use Maven to build MAARS and copy all dependencies to `jars/`. It allows "basic users" to copy the required .jar file to a standard Micro-Manager installation.

## License

[BSD License](LICENSE).

## Authors

- Mainteners and contact
    - Hadrien Mary (hadrien.mary@gmail.com)
    - Tong Li (tongli.bioinfo@gmail.com)

- Original author
    - Marie Grosjean (marie.grosjean.31@gmail.com)
