import pandas as pd
import numpy as np
root = "/media/tong/screening/100x/Mitosis_analysis/795_wt/"
timeBoard = pd.read_csv(root + "mitosis_time_board.csv", header=None, index_col=0)
phaseDectetion = pd.read_csv(root + "colocalisation.csv", index_col=[0,1])
phaseDectetion.index.names=["cellNb", "phases"]
phaseTable = phaseDectetion.loc[phaseDectetion.index.get_level_values('phases') == "phase"]

for ind in timeBoard.index:
    xs, ys = np.where(phaseTable.loc[ind]==2)
    print(ind, ys)
    # xs, ys =np.where(phaseTable.loc[ind][timeBoard.loc[ind].astype(str)]==2)
    # if len(xs) > 0:
    #     print(ind , list(timeBoard.loc[ind]))
    #     print(ys[0])
