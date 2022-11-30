import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from os import listdir

PATH = 'exp'
DEST = 'graph'
files = [file for file in listdir(PATH) if '.csv' in file]

def trend(file_id, df):
    plt.cla()
    plt.plot(df.time, df.success, label="success")
    plt.plot(df.time, df.flying, label="flying")
    plt.plot(df.time, df.dropped, label="dropped")
    plt.plot(df.time, df.failed, label="rejected")
    plt.legend(loc="upper left")
    plt.savefig(f"{DEST}/{file_id}trend.png", dpi=500)
    
def stack(file_id, df):
    plt.cla()
    # stack
    plt.stackplot(df.time, df.success, df.flying, df.dropped, df.failed, labels=['success', 'flying', 'dropped', 'rejected'])
    plt.legend(loc="upper left")
    plt.savefig(f'{DEST}/{file_id}stack.png', dpi=500)

for file in files:
    df = pd.read_csv(f'{PATH}/{file}')
    file_id = file[:-4]
    trend(file_id, df)
    stack(file_id, df)
