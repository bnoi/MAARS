# Mitotic Analysing And Recording System

MAARS modules are Fiji and Micro-Manager libraries. Modules are :

- CellStateAnalysis_ : measure, save and load cell features
- CellBoundaries_ : detect and segment fission yeast cells in bright field.
- SigmaOptimization_ : optimize parameters in segmentation algorithm from CellBoundaries_.
- MaarsLib : core library for controlling microscop with Micro-Manager.

## Install (to complete)

- Copy the jars inside `jars/lib` inside `Micro Manager folder/plugins/jars`
- Add MAARS plugin (`jars/MAARS_-1.0-SNAPSHOT.jar`) inside `Micro Manager folder/plugins`

That's all !

## For developers

- `build-mm.sh` : use this script to build Micro-Manager under Linux.

- `mavenized-mm.sh` : use this script to update local Maven repository inside `repo/`. MM jars used are `MMJ_`, `MMCoreJ` and `MMAcqEngine`.

- `update-libs.sh` : use Maven to build MAARS and copy all dependencies to `jars/`. It allows "basic users" to copy the required .jar file to a standard Micro-Manager installation.

## License

BSD License.

## Authors

- Mainteners and contact
    - Hadrien Mary (hadrien.mary@gmail.com)
    - Tong Li (tongli.bioinfo@gmail.com )

- Original author
    - Marie Grosjean (marie.grosjean.31@gmail.com)
