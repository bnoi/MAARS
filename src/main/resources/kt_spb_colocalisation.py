
# coding: utf-8

from os import path
import matplotlib.pyplot as plt
import tmXmlToDataFrame as tm
import numpy as np
import pandas as pd
import argparse
import multiprocessing as mp

def dist(x1,y1,x2,y2):
    return np.sqrt((x1-x2)**2 + (y1-y2)**2)

def get_outside_spots(x_base, y_base, xs, ys, radius):
    spots_outside_xs = list()
    spots_outside_ys = list()
    for i in range(0,len(xs)):
        if dist(x_base, y_base,xs[i],ys[i]) > radius:
            spots_outside_xs.append(xs[i])
            spots_outside_ys.append(ys[i])
    return spots_outside_xs,spots_outside_ys

def change_label(df, index, col, value):
    for j in df.loc[index]:
        df.set_value(index,col, value)

def prepare_data(root, cellNb, channel):
    spotsPath = root +cellNb+"_"+channel+".xml"
    chSpots = tm.getAllSpots(spotsPath)
    chSpots["phase"] = ["interphase"]*len(chSpots)
    chSpots["dotNb"] = ["1"] * len(chSpots)
    return chSpots

def generate_circle_coords(radius):
    an = np.linspace(0, 2*np.pi, 100)
    xs = radius *np.cos(an)
    ys = radius *np.sin(an)
    return (xs, ys)

def process_one_cell(root, cellNb, poleCh, ktCh, savingRoot,radius = 0.25):
    print("processing cell %s" %cellNb)
    poleSpots = prepare_data(root,cellNb,poleCh)
    ktSpots  = prepare_data(root,cellNb,ktCh)
    pos_x = "POSITION_X"
    pos_y = "POSITION_Y"
    for j in [pos_x, pos_y]:
        poleSpots[j] = poleSpots[j].astype(np.float)
        ktSpots[j] = ktSpots[j].astype(np.float)

    meta_color = "red"
    ana_color = "blue"
    circle_xs, circle_ys = generate_circle_coords(radius)

    alpha = 0.8
    merged_frameNb = sorted(list(poleSpots.index.levels[0]) + list(set(list(ktSpots.index.levels[0])) - set(list(poleSpots.index.levels[0]))))
    for x in merged_frameNb:
        if x not in poleSpots.index or x not in ktSpots.index:
            continue
        current_frame_poles = poleSpots.loc[x]
        current_frame_kts = ktSpots.loc[x]
        change_label(poleSpots,x,"dotNb",len(current_frame_poles))
        change_label(ktSpots,x,"dotNb",len(current_frame_kts))
        if len(current_frame_poles)<2:
            continue
        spots_outside = (list(current_frame_kts[pos_x]), list(current_frame_kts[pos_y]))
        for i in range(0,len(current_frame_poles[pos_x])):
            spots_outside = get_outside_spots(current_frame_poles[pos_x].iloc[i], current_frame_poles[pos_y].iloc[i],
                                              spots_outside[0],spots_outside[1],radius)
        if len(spots_outside[0])==0:
            change_label(poleSpots,x,"phase","anaphase")
            change_label(ktSpots,x,"phase","anaphase")
        else:
            change_label(poleSpots,x,"phase","metaphase")
            change_label(ktSpots,x,"phase","anaphase")
    # print(cfpSpots['phase'][cfpSpots['phase']=='metaphase'])
    # indexsOfMetaphase = list(cfpSpots.index.levels[0][cfpSpots['phase'][cfpSpots['phase']=='metaphase'].index.labels[0]])
    phase_d = pd.DataFrame(index=merged_frameNb)
    phase_d['phase'] = poleSpots['phase'].xs(0,level=1)
    phase_d['pole_dotNb'] = poleSpots['dotNb'].xs(0,level=1)
    phase_d['kt_dotNb'] = ktSpots['dotNb'].xs(0,level=1)
    phase_d.to_csv(savingRoot +cellNb+"_anot_spots.csv")

parser = argparse.ArgumentParser(description='Find colocalisation state of each frame')
parser.add_argument('root', metavar='root Path', type=str,
                    help='path to root dir containing spot files (.xml)')
parser.add_argument('poleChannel', type=str,help='channel of pole')
parser.add_argument('ktChannel', type=str,help='channel of kinetochore')
parser.add_argument('savingRoot', type=str,help='folder to save results')
parser.add_argument('cellNbs', type=str, nargs='+',help='cell number(s)')
args = parser.parse_args()
pool = mp.Pool(mp.cpu_count())
tasks = []
for cellNb in args.cellNbs:
    tasks.append((args.root, cellNb, args.poleChannel, args.ktChannel, args.savingRoot))
results = [pool.apply_async( process_one_cell, t ) for t in tasks]
for result in results:
    result.get()
pool.close()


    # fig,ax = plt.subplots(figsize=(5,5))
    # ax.axis('equal')
    # for k in [0,1]:
    #     ax.plot(circle_xs + current_frame_poles[pos_x].iloc[k], circle_ys + current_frame_poles[pos_y].iloc[k], "y--",alpha = alpha)
    # ax.scatter(current_frame_poles[pos_x],current_frame_poles[pos_y],alpha = alpha, c= dot_color)
    # ax.scatter(current_frame_kts[pos_x],current_frame_kts[pos_y],alpha = alpha, c= "green")
    # # alpha += 0.9/len(gfpSpots.index.levels[0])
    # ax.set_title("Frame" + str(x))
    # plt.show()
