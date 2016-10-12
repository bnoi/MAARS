# Mitotic Analysing And Recording System

MAARS (Mitotic Analysing And Recording System) is a Micro-Manager plugin designed to automatically record and analyze fission yeast cells in mitosis, either with an on-the-fly manner or an off-line manner.

The plugin is designed to be flexible and easy to use whatever the hardware and the type of cells you have.

## Kernel
The kernel of MAARS which is written in Java, use the API (or source code) of multiple open-source projects (i.e. Micro-manager, Trackmate) to acquire and analyze images. 

## Extension
An extension of MAARS which find anaphase B onset and chromosome lagging, is written in Python. We used functions of Anaconda in order to facilitate our analysis. (further may be change to Java version)

## Code structure
- jars : dependencies of MAARS
- repo : the folder that we put the builded plugin for Micro-Manager
- src : kernel code of MAARS

## Installation (for basic users)

- Copy MAARS plugin (`jars/MAARS_-1.0-SNAPSHOT.jar`) inside `Micro Manager folder/mmplugins`.

- Copy dependencies (`jars/maars_dependencies/`) inside `Micro Manager folder/plugins/maars_dependencies`.

That's all !

## For developers

- `build-mm.sh` : use this script to build Micro-Manager under Linux.

- `mavenized-mm.sh` : use this script to update local Maven repository inside `repo/`. MM jars used are `MMJ_`, `MMCoreJ` and `MMAcqEngine`.

- `update-libs.sh` : use Maven to build MAARS and copy all dependencies to `jars/`. It allows "basic users" to copy the required .jar file to a standard Micro-Manager installation.

- `install.sh` : Use to install MAARS plugin and its dependencies to Micro-Manager installation.

## License

[BSD License](LICENSE).

## Authors

- Mainteners and contact
    - Hadrien Mary (hadrien.mary@gmail.com)
    - Tong Li (tongli.bioinfo@gmail.com)

- Original author
    - Marie Grosjean (marie.grosjean.31@gmail.com)

# Funding
Centre national de la recherche scientifique (CNRS)

université paul sabatier

centre de biologie intégrative (CBI)

Laboratoire de biologie cellulaire et moléculaire du contrôle de la prolifération (LBCMCP)

Fondation pour la Recherche Médicale (FRM)

plan cancer 2009

Agence Nationale de la Recherche (ANR)
