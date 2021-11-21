import socket

import numpy as np
import pandas as pd
from sklearn.neighbors import NearestNeighbors
import threading
import json


# Q = [[0, 0, 2, 0.0329, -78.6, 10000.0, 9.0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0,
#        0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0,
#        0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0,
#        0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0,
#        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
#        0.2222222222222222]]

class HTTPServer(object):
    def __init__(self):
        customer_array = np.load("customer_array.npy", allow_pickle=True)
        customer_rare_array = np.load("customer_rare_array.npy", allow_pickle=True)
        by_customer_rare = pd.read_csv('./by_customer_rare.csv')
        by_customer = pd.read_csv('./by_customer.csv')
        self.customer_rare = pd.read_csv('./customer_rare.csv')
        self.customer = pd.read_csv('./customer.csv')

        self.neigh = NearestNeighbors()
        self.neigh.fit(customer_array)

        self.neigh_rare = NearestNeighbors()
        self.neigh_rare.fit(customer_rare_array)

        self.by_customer = by_customer.set_index('customer_id')
        self.by_customer_rare = by_customer_rare.set_index('customer_id')

        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

    def start(self):
        self.server_socket.listen(128)
        while True:
            client_socket, client_address = self.server_socket.accept()
            handle_client_process = threading.Thread(target=self.handle_client, args=(client_socket,))
            handle_client_process.start()

    def handle_client(self, client_socket):
        """
        处理客户端请求
        """
        request_data = client_socket.recv(1024)
        print("request data:", request_data)
        request_lines = request_data.splitlines()

        request_start_line = request_lines[0]
        userVector = json.loads("[[" + request_start_line.decode("utf-8").split("=")[1].split(" H")[0] + "]]")
        res = self.generate_recommendation(userVector)

        response_start_line = "HTTP/1.1 200 OK\r\n"
        response_headers = "Server: My server\r\n"

        response = response_start_line + response_headers + "\r\n" + str(res)
        print("response data:", response)

        client_socket.send(bytes(response, "utf-8"))

        client_socket.close()

    def bind(self, port):
        self.server_socket.bind(("", port))

    def query(self, user_vecs, neigh, table, pool, k_value=3, return_num=1):
        dist, knn = neigh.kneighbors(user_vecs, k_value, return_distance=True)
        recommendation = []
        recommendation_weight = []
        for k, i in enumerate(list(knn)):
            user_id = table.iloc[i]['customer_id'].values
            recommended_restaurant = []
            for id in user_id:
                recommended_restaurant.append(pool.loc[id]['VENDOR'].split(' '))
            confidence = {}
            for j in range(len(recommended_restaurant)):
                for restaurant in recommended_restaurant[j]:
                    if restaurant not in confidence:
                        confidence[restaurant] = 0.
                    confidence[restaurant] += 1. / dist[0][j]
            p = np.asarray(list(confidence.values()))
            p /= np.sum(p)
            keys = list(confidence.keys())
            recommendation.append(np.random.choice(len(confidence.keys()), return_num, p=p, replace=False))
            ret = [keys[recommendation[-1][n]] for n in range(return_num)]
            return ret

    def generate_recommendation(self, user_vecs):
        l1 = self.query(user_vecs, self.neigh, self.customer, self.by_customer, k_value=3, return_num=2)
        l2 = self.query(user_vecs, self.neigh_rare, self.customer_rare, self.by_customer_rare, k_value=3, return_num=1)
        l1.extend(l2)
        return l1


def main():
    http_server = HTTPServer()
    http_server.bind(8005)
    http_server.start()


if __name__ == "__main__":
    main()
