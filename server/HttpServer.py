import numpy as np
import pandas as pd
import os
import matplotlib.pyplot as plt
import seaborn as sns
#import geopandas as gpd
import re
import sklearn
import numpy as np
from sklearn.neighbors import NearestNeighbors
import socket
import sys
import threading
import json
import numpy as np

# In[2]:
import multiprocessing

#sample data format
Q = [[0, 0, 2, 0.0329, -78.6, 10000.0, 9.0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0,
       0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0,
       0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0,
       0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
       0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
       0.2222222222222222]]

to_send = "#AFSDG,"+" ".join([str(i) for i in Q[0]])+"@"
to_send_encoded = to_send.encode("utf-8")
print(to_send_encoded)


def sender():
    # read user_vec from client, put into cosumer queue
    # que obj support internal blocking mechanism, maybe don't need a lock
    print("setting client sender")
    host = socket.gethostname()
    port2 = 12348

    
    #client side
    serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    serversocket.connect((host, port2))
    myaddr = serversocket.getsockname()
    print("server addr:%s" % str(myaddr))
    while True:
        serversocket.send(to_send_encoded)    #client send test instance to server
        serversocket.send(to_send_encoded)    #client send test instance to server
        serversocket.send(to_send_encoded)    #client send test instance to server
        serversocket.send(to_send_encoded)    #client send test instance to server
        print("send sucess")

if __name__ == '__main__':
    sender()



