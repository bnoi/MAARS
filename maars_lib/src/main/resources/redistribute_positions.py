import os
import re
import shutil
bfImgsDir = "/home/tong/Desktop/20170621/34C_1"
savingRoot = "/home/tong/Desktop/20170621/test"
bfDir = "BF_1"
fluoDir = "FLUO_1"
fluoImgsDir = "/home/tong/Desktop/20170621/34C_2"


def getPositions(bfImgsDir):
    posSuffix = list()
    pattern = "MMStack_(.*?).ome.tif"
    l = os.listdir(bfImgsDir)
    for f in l:
        m = re.findall(pattern,f)
        if len(m) >0:
            posSuffix.append(m[0])
    return posSuffix

def mkDirs(savingRoot, posSuffixs, bfDir, fluoDir):
    for pos in posSuffixs:
        posDir = savingRoot + os.sep + pos
        os.mkdir(posDir)
        os.mkdir(posDir + os.sep + bfDir)
        os.mkdir(posDir + os.sep + fluoDir)


def copyBfImgs(bfImgsDir, savingRoot, bfDir, posSuffixs):
    l = os.listdir(bfImgsDir)
    for pos in posSuffixs:
        pattern = ".*MMStack_%s.ome.tif" %(pos)
        print("bf %s : " %(pos))
        for f in l:
            if re.match(pattern, f):
                print("\tcopying " + f)
                shutil.copy(bfImgsDir + os.sep + f,
                savingRoot + os.sep + pos + os.sep + bfDir + os.sep + f)

def copyFluoImgs(fluoImgsDir, savingRoot, fluoDir,posSuffixs):
    l = os.listdir(fluoImgsDir)
    for pos in posSuffixs:
        pattern = ".*MMStack_%s.*.ome.tif" %(pos)
        print("fluo %s : " %(pos))
        for f in l:
            if re.match(pattern, f):
                print("\tcopying " + f)
                shutil.copy(fluoImgsDir + os.sep + f,
                savingRoot + os.sep + pos + os.sep + fluoDir + os.sep + f)

posSuffixs = getPositions(bfImgsDir)
mkDirs(savingRoot, posSuffixs, bfDir , fluoDir)
copyBfImgs(bfImgsDir, savingRoot, bfDir, posSuffixs)
copyFluoImgs(fluoImgsDir, savingRoot, fluoDir, posSuffixs)
print("Done")
