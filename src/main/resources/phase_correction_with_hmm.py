from hmmlearn import hmm
import pandas as pd
import numpy as np

d = pd.DataFrame.from_csv("/media/tong/MAARSData/MAARSData/102/15-06-2/BF_1_MITOSIS/phases/32_anot_spots.csv", header=None, index_col=[0,1])
d.shape
d.xs(1, level=1)
d_unique = d.drop(1, level=1)
d_unique['dotNb'] = np.ones(len(d_unique))
d_unique.set_value(list(d.xs(1, level=1).index),'dotNb',2.0)
startprob = np.array([0.6, 0.3, 0.1])
transmat = np.array([[0.7, 0.2, 0.1], [0.3, 0.5, 0.2], [0.3, 0.3, 0.4]])
means = np.array([[0.0, 0.0], [3.0, -3.0], [5.0, 10.0]])
covars = np.tile(np.identity(2), (3, 1, 1))
hmm.GaussianHMM(n_components=3)
