# TensorFlow and tf.keras
import tensorflow as tf
from tensorflow import keras
import numpy as np
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, Dropout, Activation, Conv2D, Flatten, MaxPooling2D
import pickle

import matplotlib.pyplot as plt
import matplotlib.image as mpimg
import os
from cv2 import cv2
import random

def rgb2gray(rgb):
    return np.dot(rgb[...,:3], [0.2989, 0.5870, 0.1140])

#TESTER#TESTER#TESTER#TESTER#TESTER#TESTER#TESTER#TESTER#TESTER#TESTER#TESTER#TESTER#TESTER#TESTER#TESTER#TESTER#TESTER
DATADIR = r"C:\Users\chern\androidRepository\NNData\RoadClassification"
CATEGORIES = ["Test(mixed)"]

CATEGORIES_2 = ["Asphalt", "Cobblestone"]

IMG_WIDTH = 500
IMG_HEIGHT = 500

TESTING_MODEL = "keras-Conv2D64x3_Dense_0_newDataset"

model = keras.models.load_model(TESTING_MODEL)

for category in CATEGORIES:
    path = os.path.join(DATADIR, category)  # join path
    class_num = CATEGORIES.index(category)
    for img in os.listdir(path):
        new_array = mpimg.imread(os.path.join(path, img))
        new_array = rgb2gray(new_array) / 255.0
        test = np.array(new_array).reshape(-1, IMG_HEIGHT, IMG_WIDTH, 1)
        prediction = model.predict(test)
        #if round(prediction[0][0]) == True:
            #print("Cobblestone")
        #else:
            #print("Asphalt")
            
        #np.argmax(prediction[0])
        prediction = (CATEGORIES_2[int(prediction[0][0])])
        print(prediction)
        plt.imshow(new_array)
        plt.show()


model.save(TESTING_MODEL)
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()
open(r"C:\Users\chern\androidRepository\NNModels\keras-Conv2D64x3_Dense_0_newDataset.tflite", "xb").write(tflite_model)