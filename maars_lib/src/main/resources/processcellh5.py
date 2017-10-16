import cellh5
path = "/Volumes/Macintosh/curioData/MAARSdata/102/12-06-1/Mitosis/Pos1/test.ch5"
fh = cellh5.CH5File(path,"r+")

for x in fh.image_definition:
    print(x)
for y in fh.object_definition:
    print(y)
fh.wells
fh.positions
fh.set_current_pos("102")
for z in fh.feature_definition:
    print(z)
for x in fh.get_definition_root():
    print(x)
