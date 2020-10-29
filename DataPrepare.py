# TensorFlow and tf.keras
import tensorflow as tf
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.image as mpimg
import os
from cv2 import cv2
import random

DATADIR = r"C:\Users\chern\androidRepository\NNData\RoadClassification"
CATEGORIES_MIXED = ["Asphalt(mixed)", "Cobblestone(mixed)"]
CATEGORIES_SEPARATE = ["Hiace - Asphalt", "Hiace - Cobblestone", "Leaf - Asphalt", "Leaf - Cobblestone",
 "Prius - Asphalt", "Prius - Cobblestone"]

IMG_WIDTH = 500
IMG_HEIGHT = 500

training_data = []

def rgb2gray(rgb):
    return np.dot(rgb[...,:3], [0.2989, 0.5870, 0.1140])

def create_training_data():
    amountOfData = 0
    for category in CATEGORIES_MIXED:
        path = os.path.join(DATADIR, category)  # join path
        class_num = CATEGORIES_MIXED.index(category)
        for img in os.listdir(path):
            amountOfData += 1
            new_array = mpimg.imread(os.path.join(path, img))
            new_array = rgb2gray(new_array)
            #new_array = cv2.imread(os.path.join(path, img), cv2.IMREAD_GRAYSCALE)
            #plt.imshow(np.array(new_array))
            #plt.show()
            training_data.append([new_array, class_num])
            if(amountOfData > 825):
                amountOfData = 0
                break
            

create_training_data()

random.shuffle(training_data)

for sample in training_data[:10]:
    print(sample[1])

X = []
y = []

for features, label in training_data:
    X.append(features)
    y.append(label)

X = np.array(X).reshape(-1, IMG_HEIGHT, IMG_WIDTH, 1)

import pickle

NameX = "X_MIXED.pickle"
NameY = "y_MIXED.pickle"

pickle_out = open(NameX, "wb")
pickle.dump(X, pickle_out)
pickle_out.close()

pickle_out = open(NameY, "wb")
pickle.dump(y, pickle_out)
pickle_out.close()