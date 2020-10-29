# TensorFlow and tf.keras
import tensorflow as tf
from tensorflow import keras
import numpy as np
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, Dropout, Activation, Conv2D, Flatten, MaxPooling2D
from tensorflow.keras.callbacks import TensorBoard
import pickle

import matplotlib.pyplot as plt
import matplotlib.image as mpimg
import os
from cv2 import cv2
import random

import pydot
import graphviz

import time

NameX = "X_MIXED.pickle"
NameY = "y_MIXED.pickle"

X = pickle.load(open(NameX, "rb"))
y = pickle.load(open(NameY, "rb"))

X = np.array(X) / 255.0
y = np.array(y)

dense_layers = [0]
layer_sizes = [64]
conv_layers = [3]

for dense_layer in dense_layers:
    for layer_size in layer_sizes:
        for conv_layer in conv_layers:
            NAME = "{}-Conv2D-{}-Nodes-{}-Dense".format(conv_layer, layer_size, dense_layer)
            tensorboard = TensorBoard(log_dir='FF826/logs/{}'.format(NAME))
            print(NAME)

            model = Sequential()
            model.add(  Conv2D(layer_size, (3,3), input_shape = (500, 500, 1))  )
            model.add(Activation("relu"))
            model.add(MaxPooling2D(pool_size=(2,2)))

            for l in range(conv_layer-1):
                model.add(  Conv2D(layer_size, (3,3))  )
                model.add(Activation("relu"))
                model.add(MaxPooling2D(pool_size=(2,2)))

            model.add(Flatten())
            for l in range(dense_layer):
                model.add(Dense(layer_size))
                model.add(Activation('relu'))

            model.add(Dense(1))
            model.add(Activation('sigmoid'))

            model.compile(loss="binary_crossentropy",
            optimizer="adam", metrics=['accuracy'])

            model.summary()

            model.fit(X, y, batch_size=128, epochs=10, validation_split=0.4, callbacks=[tensorboard])
            model.save("keras-Conv2D64x3_Dense_0_FF825")

converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()
open(r"C:\Users\chern\androidRepository\NNModels\FF825", "xb").write(tflite_model)

