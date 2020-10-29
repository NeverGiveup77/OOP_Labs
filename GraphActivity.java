package com.android.accelerometergraph;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GraphActivity extends Activity implements View.OnClickListener {
    double time;

    private int STORAGE_PERMISSION = 1;
    public volatile boolean RECORDING_STARTED = false;
    public volatile boolean IMAGE_IS_SAVING = false;
    public volatile boolean EVENT_SAVE = false;
    public volatile boolean MAKE_PREDICTION = true;
    public volatile boolean MAKE_PREDICTION_BTN = true;
    public volatile boolean EVENT_SAVE_DIR = false;

    float[][] output = new float[1][1];
    float[][][][] inputArray;
    float res;
    String text;
    Bitmap bitmap;
    RadioButton asphaltBtn;
    RadioButton cobblestoneBtn;
    Button eventSpeedHumpBtn;
    Button eventPotholeBtn;
    Button runInference;
    Button startRecording;
    TextView predictedClass;
    TextView predictionTime;

    public int EVENT = 0; // 0 - SPEED HUMP, 1 - POTHOLE
    public int ROAD_CLASS = 0; // 0 - ASPHALT, 1 - COBBLESTONE
    int NumOfSaves;
    public static myGLSurfaceView MainGraph;
//    public static myGLSurfaceView RecordingGraph;
    public static GLSurfaceView.Renderer mainRend;
//    public static GLSurfaceView.Renderer recordingRend;

    Interpreter interpreter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(GraphActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(GraphActivity.this, "You have alredy granted this permission", Toast.LENGTH_SHORT).show();
        } else {
            requestStoragePermission();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.inference_layout);
        cobblestoneBtn = (RadioButton) findViewById(R.id.btnCobblestone);
        asphaltBtn = (RadioButton) findViewById(R.id.btnAsphalt);
        eventSpeedHumpBtn = (Button) findViewById(R.id.btnSpeedHump);
        eventPotholeBtn = (Button) findViewById(R.id.btnPothole);
        runInference = (Button) findViewById(R.id.btnRunInference);
        startRecording = (Button) findViewById(R.id.btnStartStopRec);
        predictedClass = (TextView) findViewById(R.id.txtPredictionClass);
        predictionTime = (TextView) findViewById(R.id.txtPredictionTime);
        MainGraph = findViewById(R.id.MainGraph);
        MainGraph.setEGLContextClientVersion(2);
//        RecordingGraph = findViewById(R.id.RecordingGraph);
//        RecordingGraph.setEGLContextClientVersion(2);

        try {
            interpreter = new Interpreter(loadModelFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

//        recordingRend = new GLSurfaceView.Renderer() {
//            @Override
//            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//                AccelerometerGraphJNI.surfaceCreated();
//            }
//            @Override
//            public void onSurfaceChanged(GL10 gl, int width, int height) {
//                AccelerometerGraphJNI.surfaceChanged(width, height);
//            }
//            @Override
//            public void onDrawFrame(final GL10 gl) {
//                AccelerometerGraphJNI.updateData();
//                AccelerometerGraphJNI.drawFrame();
//            }
//        };
        mainRend = new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                AccelerometerGraphJNI.surfaceCreated();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                AccelerometerGraphJNI.surfaceChanged(width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                AccelerometerGraphJNI.updateData();
                AccelerometerGraphJNI.drawFrame_REC();
                if (MAKE_PREDICTION && MAKE_PREDICTION_BTN) {
                    MAKE_PREDICTION = false;
                    time = System.nanoTime();
                    bitmap = createBitmapFromGLSurface(0, 0, MainGraph.getWidth(), MainGraph.getWidth(), gl);
                    bitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, true);
                    RunInference task = new RunInference();
                    task.execute(bitmap);
                }
//                if ((RECORDING_STARTED && !IMAGE_IS_SAVING) || EVENT_SAVE) {
//                    EVENT_SAVE = false;
//                    IMAGE_IS_SAVING = true;
//                    Handler handler = new Handler(Looper.getMainLooper());
//                    bitmap = createBitmapFromGLSurface(0, 0, MainGraph.getWidth(), MainGraph.getWidth(), gl);
//                    bitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, true);
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            SavingImagesThread save = new SavingImagesThread(bitmap, getBaseContext());
//                            save.start();
//                        }
//                    });
//                }
            }
        };
//        RecordingGraph.setRenderer(recordingRend);
//        RecordingGraph.queueEvent(new Runnable() {
//            @Override
//            public void run() {
//                AccelerometerGraphJNI.init(getAssets());
//            }
//        });
        MainGraph.setRenderer(mainRend);
        MainGraph.queueEvent(new Runnable() {
            @Override
            public void run() {
                AccelerometerGraphJNI.init(getAssets());
            }
        });
    }
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("Storage permission is needed for recording graph")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(GraphActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
        }
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        String MODEL_ASSETS_PATH = "keras-Conv2D64x3_Dense_0_newDataset.tflite";
        AssetFileDescriptor assetFileDescriptor = this.getAssets().openFd(MODEL_ASSETS_PATH);
        FileInputStream fileInputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startoffset = assetFileDescriptor.getStartOffset();
        long declaredLength = assetFileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startoffset, declaredLength);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        RecordingGraph.onPause();
//        RecordingGraph.queueEvent(new Runnable() {
//            @Override
//            public void run() {
//                AccelerometerGraphJNI.pause();
//            }
//        });
        MainGraph.onPause();
        MainGraph.queueEvent(new Runnable() {
            @Override
            public void run() {
                AccelerometerGraphJNI.pause();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        RecordingGraph.onResume();
//        RecordingGraph.queueEvent(new Runnable() {
//            @Override
//            public void run() {
//                AccelerometerGraphJNI.resume();
//            }
//        });
        MainGraph.onResume();
        MainGraph.queueEvent(new Runnable() {
            @Override
            public void run() {
                AccelerometerGraphJNI.resume();
            }
        });
    }

    private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10 gl)
            throws OutOfMemoryError {
        int[] bitmapBuffer = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);
        try {
            gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
        } catch (GLException e) {
            return null;
        }
        return Bitmap.createBitmap(bitmapBuffer, w, h, Bitmap.Config.ARGB_8888);
    }

    private float[][][][] bitmapToInputArray(Bitmap bitmap) {
        int batchNum = 0;
        float[][][][] input = new float[1][500][500][1];
        for (int x = 0; x < 500; x++) {
            for (int y = 0; y < 500; y++) {
                int pixel = bitmap.getPixel(x, y);
                int r = (pixel >> 16) & 0x000000FF;
                input[batchNum][y][x][0] = r / 255.0f;
            }
        }
        return input;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnAsphalt:
                ROAD_CLASS = 0;
                asphaltBtn.setChecked(true);
                break;
            case R.id.btnCobblestone:
                ROAD_CLASS = 1;
                cobblestoneBtn.setChecked(true);
                break;
            case R.id.btnPothole:
                EVENT = 1;
                EVENT_SAVE = true;
                EVENT_SAVE_DIR = true;
                break;
            case R.id.btnSpeedHump:
                EVENT = 0;
                EVENT_SAVE = true;
                EVENT_SAVE_DIR = true;
                break;
            case R.id.btnRunInference:
                MAKE_PREDICTION_BTN = !MAKE_PREDICTION_BTN;
                if (MAKE_PREDICTION_BTN) {
                    runInference.setText("STOP INFERENCE");
                } else runInference.setText("RUN INFERENCE");
                break;
            case R.id.btnStartStopRec:
                if (RECORDING_STARTED) {
                    startRecording.setText("START");
                } else startRecording.setText("STOP");
                RECORDING_STARTED = !RECORDING_STARTED;
                break;
        }
    }

    public void saveToInternalStorage(Bitmap bitmapImage)
    {
        File path = Environment.getExternalStorageDirectory();
        File dir = null;
        if (!EVENT_SAVE_DIR && ROAD_CLASS == 1) {
            dir = new File(path.getAbsolutePath()+"/AccelerometerGraphPics/Cobblestone/");
        } else if (!EVENT_SAVE_DIR && ROAD_CLASS == 0) {
            dir = new File(path.getAbsolutePath()+"/AccelerometerGraphPics/Asphalt/");
        } else if (EVENT_SAVE_DIR && EVENT == 0 && ROAD_CLASS == 1) {
            dir = new File(path.getAbsolutePath() + "/AccelerometerGraphPics/Cobblestone/SpeedHump/");
        } else if (EVENT_SAVE_DIR && EVENT == 0 && ROAD_CLASS == 0) {
            dir = new File(path.getAbsolutePath()+"/AccelerometerGraphPics/Asphalt/SpeedHump/");
        } else if (EVENT_SAVE_DIR && EVENT == 1 && ROAD_CLASS == 1) {
            dir = new File(path.getAbsolutePath()+"/AccelerometerGraphPics/Cobblestone/Pothole/");
        } else if (EVENT_SAVE_DIR && EVENT == 1 && ROAD_CLASS == 0) {
            dir = new File(path.getAbsolutePath()+"/AccelerometerGraphPics/Asphalt/Pothole/");
        }
        dir.mkdirs();
        String imgName = "CODE_E" + EVENT_SAVE + "_E" + EVENT + "_R" + ROAD_CLASS + "_№" + NumOfSaves;
        File file = null;
        try {
            file = File.createTempFile(imgName, ".jpg", dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        IMAGE_IS_SAVING = false;
        EVENT_SAVE = false;
        EVENT_SAVE_DIR = false;
    }

    public class SavingImagesThread extends Thread {

        Bitmap bitmap;
        Context context;
        GraphActivity graphActivity = new GraphActivity();

        SavingImagesThread(Bitmap bitmap, Context context){
            this.bitmap = bitmap;
            this.context = context;
        };

        @Override
        public void run()
        {
            if (EVENT_SAVE_DIR) {
                graphActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        saveToInternalStorage(bitmap);
                        NumOfSaves++;
                        Toast.makeText(context, "EVENT SAVED", Toast.LENGTH_SHORT).show();
                    }
                });
            }  else {
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                graphActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        saveToInternalStorage(bitmap);
                        NumOfSaves++;
                        Toast.makeText(context, "CLASS SAVED", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    public class RunInference extends AsyncTask<Bitmap, Integer, Float> {
        @Override
        protected Float doInBackground(Bitmap... bitmaps) {
            inputArray = bitmapToInputArray(bitmap);
            interpreter.run(inputArray, output);
            res = output[0][0];
            return res;
        }

        @Override
        protected void onPostExecute(Float aFloat) {
            super.onPostExecute(aFloat);
            if (Math.round(res) == 1) text = ("COBBLESTONE >> " + Math.round(res * 100) + "%");
            else {
                res = 1 - res;
                text = ("ASPHALT >> " + Math.round(res * 100) + "%");
            }
            predictionTime.setText(fmt((System.nanoTime() - time)/1000000)+"ms");
            predictedClass.setText(text);
            MAKE_PREDICTION = true;
        }
    }

    public static String fmt(double d)
    {
//        if(d == (long) d)
            return String.format("%d",(long)d);
//        else
//            return String.format("%s",d);
    }
}