# Mitotic Analysing And Recording System

MAARS modules are Fiji and Micro-Manager libraries. Modules are:

- CellStateAnalysis_ : measure, save and load cell features
- CellBoundaries_ : detect and segment fission yeast cells in bright field.
- SigmaOptimization_ : optimize parameters in segmentation algorithm from CellBoundaries_.
- MaarsLib : core library for controlling microscop with Micro-Manager.

## Install (to complete)

Following jars are needed for runtime (copy them inside Micro Manager folder/plugins)

- imagej-common-0.12.2.jar
- imglib2-2.2.0.jar
- imglib2-algorithm-0.2.0.jar
- imglib2-algorithm-fft-0.1.1.jar
- imglib2-algorithm-gpl-0.1.2.jar
- imglib2-ij-2.0.0-beta-29.jar
- jama-1.0.3.jar
- jgrapht-0.8.3.jar
- mines-jtk-20100113.jar
- scijava-common-2.37.0.jar
- TrackMate_-2.7.1.jar

In theory version specified here are not mandatory. So you can copy the last version (or at least a recent version) of these jars.

## License

BSD License.

## Authors

- Mainteners and contact
    - Hadrien Mary (hadrien.mary@gmail.com)
    - Jonathan Fouchard (jonathan.fouchard@univ-tlse3.fr)

- Original author
    - Marie Grosjean (marie.grosjean.31@gmail.com)
