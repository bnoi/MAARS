from hmmlearn import hmm
import pandas as pd
import numpy as np
import os

states = ['interphase', 'metaphase', 'anaphase']
n_states = len(states)


#test set
root = "/Volumes/Macintosh/curioData/MAARSdata/102/15-06-2/BF_1_MITOSIS/phases/"
allD = list()
for f in os.listdir(root):
    if not f.startswith("."):
        allD.append(pd.DataFrame.from_csv(root + f, index_col=[0]))
concated = pd.concat(allD)

concated.groupby(['phase', 'pole_dotNb', 'kt_dotNb']).size()
model = hmm.MultinomialHMM(3)
model.fit(X)
model.monitor_.history
model
model.transmat_
model.n_features
model.startprob_ = np.array([0.3, 0.3, 0.3])
model.transmat_ = np.array([[0.5, 0.3, 0.2], [0.5, 0.3, 0.2], [0.5, 0.3, 0.2]])
model.means = np.array([[0.0, 0.0], [3.0, -3.0], [5.0, 10.0]])
model.covars_= np.tile(np.identity(2), (3, 1, 1))
model



