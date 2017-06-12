from hmmlearn import hmm
import pandas as pd
import numpy as np
import os

#test set
root = "/media/tong/MAARSData/MAARSData/102/15-06-2/BF_1_MITOSIS/phases/"
allD = list()
for f in os.listdir(root):
    if not f.startswith("."):
        allD.append(pd.DataFrame.from_csv(root + f, index_col=[0]))
concated = pd.concat(allD)
g = concated.groupby(['phase', 'pole_dotNb', 'kt_dotNb'])
sliced = concated.loc[:,['phase', 'pole_dotNb', 'kt_dotNb']]
g.size()

states = ['I', 'M', 'A']
n_states = len(states)

# observations = sorted(list(g.groups.keys()))
observations = ["i","m","a"]
n_observations = len(observations)

start_probability = np.array([0.333, 0.333, 0.333])

transition_probability = np.array([
  [0.94, 0.05, 0.01],
  [0.01, 0.94, 0.05],
  [0.05, 0.01, 0.94]])

emission_probability = np.array([
  [0.97, 0.02, 0.01],
  [0.01, 0.97, 0.02],
  [0.02, 0.01, 0.97]])

model = hmm.MultinomialHMM(n_components=n_states)
model.startprob=start_probability
model.transmat=transition_probability
model.emissionprob=emission_probability
one_obs = np.array([list(allD[-1]['phase'])]).T.astype(np.int)
model.fit(one_obs)
logprob, hears = model.decode(one_obs, algorithm="viterbi")
print("obs:", ", ".join(map(lambda x: observations[x], allD[-1]['phase'].astype(int))))
print("Alice hears:", ", ".join(map(lambda x: states[x], hears)))
model.monitor_.history
model
model.transmat_
model.n_features
model.startprob_ = np.array([0.3, 0.3, 0.3])
model.transmat_ = np.array([[0.5, 0.3, 0.2], [0.5, 0.3, 0.2], [0.5, 0.3, 0.2]])
model.means = np.array([[0.0, 0.0], [3.0, -3.0], [5.0, 10.0]])
model.covars_= np.tile(np.identity(2), (3, 1, 1))
model
