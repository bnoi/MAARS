TravisCI [![Build Status](https://travis-ci.org/bnoi/MAARS.svg?branch=master)](https://travis-ci.org/bnoi/MAARS)

# Mitotic Analysis And Recording System

MAARS (Mitotic Analysis And Recording System) is a Micro-Manager plugin designed to automatically record and analyze fission yeast cells in mitosis on-the-fly. ***Fiji ops*** for quantitative post-analysis is also possible.

## Code structure
- maars_lib : core of MAARS, required by other sub-projects
- maars_bfSeg : segmentation with GUI
- maars_otf : on-the-fly analysis in Micro-Manager 2.0
- maars_post : quantitative post-analysis with Fiji ops
Dependencies:
  - folder jars/Fiji_deps : dependencies of MAARS required in Fiji
  - folder jars/MM_deps : dependencies of MAARS required in Micro-Manager

## [I just want to segment my cells](doc/BfSeg_manual.md)

## Installation (for basic users)

1. ___(You can skip this step if you only need the segmentation but not fluo analysis)___ Copy MAARS dependencies: copy either `jars/Fiji_deps` or `jars/MM_deps` into your Fiji `jars` folder or ImageJ `plugins/jars` folder correspondingly.
2. Copy the the MAARS modules:
    1. Copy the `jars/maars_lib_2.0.0-SNAPSHOT.jar` into your `path_to_Fiji/MM_folder/plugins` folder (This is the core module required by all the other modules of MAARS)
    2. If you want to launch MAARS
        - ___on-the-fly with Micro-Manager___ (***Currently unstable*** due to updates in version 2.0): copy `jars/maars_otf_2.0.0-SNAPSHOT.jar` to `path_to_Micro_Manager_folder/mmplugins`.  
        - or with ___Fiji___ : Copy `jars/maars_post_2.0.0-SNAPSHOT.jar` and `jars/maars_bfSeg_2.0.0-SNAPSHOT.jar` into `path_to_Fiji_folder/plugins`.  



## For developers (to update)

- `build-mm.sh` : use this script to build Micro-Manager under Linux.

- `mavenized-mm.sh` : use this script to update local Maven repository inside `repo/`. MM jars used are `MMJ_`, `MMCoreJ` and `MMAcqEngine`.

- `update-libs.sh` : use Maven to build MAARS and copy all dependencies to `jars/`. It allows "basic users" to copy the required .jar file to a standard Micro-Manager installation.

- `install.sh` : Use to install MAARS plugin and its dependencies to Micro-Manager installation.

- `copyFijiDeps.sh` : copy dependencies for running MAARS in Fiji to `Fiji_deps` folder

- `copyMMDeps.sh` : copy dependencies for running MAARS in Micro-Manager to `MM_deps` folder

## License

[BSD compatible CeCILL-B License](LICENSE).

## Authors

- Mainteners and contact
    - Tong Li (tong.li@univ-tlse3.fr)

- Co-author
    - Hadrien Mary (hadrien.mary@gmail.com)
    - Simon Cabello (simon.cabelloaguilar@gmail.com)
    - Marie Grosjean (marie.grosjean.31@gmail.com)
    - Jonathan Fouchard (j.fouchard@ucl.ac.uk)
    - Céline Reyes (celine.reyes-villain@univ-tlse3.fr)
    - Sylvie Tournier (sylvie.tournier-gachet@univ-tlse3.fr)
    - Yannick Gachet (yannick.gachet@univ-tlse3.fr)

# Funding
Tong LI was supported by Ministère de l'enseigenment supérieur et de la recherche

Hadien Mary was supported by the Fondation pour la Recherche Medicale.

Jonathan Fouchard and Simon Cabello were supported by the plan Cancer 2009-2013 ‘Systems Biology’.

This work was funded by the ANR-blanc120601 ‘Chromocatch’ and the plan Cancer 2009–2013 ‘Systems
Biology’.

Fondation pour la Recherche Médicale (FRM)

plan cancer 2009 - 2013

Agence Nationale de la Recherche (ANR)

# Where is our lab

LBCMCP, Centre de Biologie Intégrative (CBI), Université de Toulouse, CNRS, UPS, France

http://cbi-toulouse.fr/eng/
