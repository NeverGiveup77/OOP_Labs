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

NameX = "X_MIXED.pickle"
NameY = "y_MIXED.pickle"

X = pickle.load(open(NameX, "rb"))
y = pickle.load(open(NameY, "rb"))

X = np.array(X) / 255.0
y = np.array(y)

DATADIR = r"C:\Users\chern\androidRepository\NNData\RoadClassification"
CATEGORIES = ["Asphalt", "Cobblestone"]

IMG_WIDTH = 500
IMG_HEIGHT = 500


model = Sequential()
model.add(  Conv2D(64, (3,3), input_shape = (500, 500, 1))  )
model.add(Activation("relu"))
model.add(MaxPooling2D(pool_size=(2,2)))

model.add(  Conv2D(64, (3,3))  )
model.add(Activation("relu"))
model.add(MaxPooling2D(pool_size=(2,2)))

model.add(  Conv2D(64, (3,3))  )
model.add(Activation("relu"))
model.add(MaxPooling2D(pool_size=(2,2)))
 
model.add(Flatten())
model.add(Dense(64))

model.add(Dense(1))
model.add(Activation('sigmoid'))

model.compile(loss="binary_crossentropy",
optimizer="adam", metrics=['accuracy'])

model.fit(X, y, batch_size=32, epochs=10)

#model = keras.models.load_model("kerasModel_1_0_MIXED")

# for category in CATEGORIES:
#     path = os.path.join(DATADIR, category)  # join path
#     class_num = CATEGORIES.index(category)
#     for img in os.listdir(path):
#         #new_array = mpimg.imread(os.path.join(path, img))
#         new_array = cv2.imread(os.path.join(path, img))
#         test = np.array(new_array).reshape(-1, IMG_HEIGHT, IMG_WIDTH, 3)
#         prediction = model.predict(test)
#         if round(prediction[0][1]) == True:
#             print("Cobblestone")
#         else:
#             print("Asphalt")
            
#         #np.argmax(prediction[0])
#         #prediction = (CATEGORIES[int(prediction[0][0])])
#         #print(prediction)
#         plt.imshow(new_array)
#         plt.show()

model.save("kerasModel_1_0_MIXED")
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()
open(r"C:\Users\chern\androidRepository\NNModels\kerasModel_1_0_MIXED.tflite", "xb").write(tflite_model)