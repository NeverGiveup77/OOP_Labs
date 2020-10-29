# TensorFlow and tf.keras
import tensorflow as tf
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.image as mpimg
import os
from cv2 import cv2
import random
import time
from scipy import misc, ndimage
import tensorflow.keras
from tensorflow.keras import backend as K
from tensorflow.keras.preprocessing.image import ImageDataGenerator

DATADIR = r"C:\Users\chern\androidRepository\NNData\RoadClassification"
CATEGORIES_MIXED = ["Cobblestone(mixed)"]
CATEGORIES_SEPARATE = ["Hiace - Asphalt", "Hiace - Cobblestone", "Leaf - Asphalt", "Leaf - Cobblestone",
 "Prius - Asphalt", "Prius - Cobblestone"]

IMG_WIDTH = 500
IMG_HEIGHT = 500

training_data = []

gen = ImageDataGenerator(horizontal_flip=True)


def rgb2gray(rgb):
    return np.dot(rgb[...,:3], [0.2989, 0.5870, 0.1140])
            

def create_training_data():
    counter = 0
    for category in CATEGORIES_MIXED:
        path = os.path.join(DATADIR, category)  # join path
        class_num = CATEGORIES_MIXED.index(category)
        for img in os.listdir(path):
            try:
                counter += 1
                new_array = mpimg.imread(os.path.join(path, img))
                new_array = rgb2gray(new_array)
                #new_array = cv2.imread(os.path.join(path, img), cv2.IMREAD_GRAYSCALE)
                # training_data.append([new_array, class_num])
                training_data.append([new_array, class_num])
                if(counter >= 100):
                    counter = 0
                    break
            except Exception as e:
                pass
            #plt.imshow(new_array)
            #plt.show()


create_training_data()

random.shuffle(training_data)

X = []
y = []

for features, label in training_data:
    X.append(features)
    y.append(label)

X = np.array(X)
X = np.reshape(X, (-1, 500, 500, 1))

aug_iter = gen.flow(
    X,
    y=None,
    batch_size=32,
    shuffle=True,
    sample_weight=None,
    seed=None,
    save_to_dir=r"C:\Users\chern\androidRepository\NNData\RoadClassification\Cobblestone(augmented)2",
    save_prefix="_AUG",
    save_format="jpg",
    subset=None,
)

aug_images = [next(aug_iter)[0].astype(np.uint8) for i in range(10)]

# import pickle

# NameX = "X_MIXED.pickle"
# NameY = "y_MIXED.pickle"

# pickle_out = open(NameX, "wb")
# pickle.dump(X, pickle_out)
# pickle_out.close()

# pickle_out = open(NameY, "wb")
# pickle.dump(y, pickle_out)
# pickle_out.close()