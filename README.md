TravisCI [![Build Status](https://travis-ci.org/bnoi/MAARS.svg?branch=master)](https://travis-ci.org/bnoi/MAARS)

# Mitotic Analysis And Recording System

MAARS (Mitotic Analysis And Recording System) is a Micro-Manager plugin designed to automatically record and analyze fission yeast cells in mitosis on-the-fly. Fiji ops for quantitative post-analysis is also possible.

## Code structure
- maars_lib : core of MAARS, required by other sub-projects
- maars_bfSeg : segmentation with GUI
- maars_otf : on-the-fly analysis
- maars_post : quantitative post-analysis with Fiji ops
  - Fiji_deps : dependencies of MAARS required in Fiji
  - MM_deps : dependencies of MAARS required in Micro-Manager


## Installation (for basic users)

- copy either `jars/Fiji_deps` or `jars/MM_deps` to your ImageJ/Fiji plugin folder
- On-the-fly plugin in Micro-Manager:
  - Copy `jars/maars_lib_2.0-SNAPSHOT.jar` and `jars/maars_otf_2.0-SNAPSHOT.jar` to `$Micro Manager folder/mmplugins`.  
- Post-analysis Fiji ops:
  - Copy `jars/maars_lib_2.0-SNAPSHOT.jar` and `jars/maars_otf_2.0-SNAPSHOT.jar` to `$Fiji folder/jars`.  

[see more details for installation](doc/manual.md).

## For developers

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
