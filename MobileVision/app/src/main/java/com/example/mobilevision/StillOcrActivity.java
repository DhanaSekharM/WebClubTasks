package com.example.mobilevision;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilevision.database.Bills;
import com.example.mobilevision.database.DatabaseHelper;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Element;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * This activity displays the camera and displays the detected price using the MobileVison API
 */
public class StillOcrActivity extends AppCompatActivity {

    private String TAG = StillOcrActivity.class.getName();
    private CameraSource cameraSource;
    private SurfaceView cameraSurfaceView;
    private Button capture;
    private ProgressBar progressBar;
    private TextRecognizer textRecognizer;
    final static int REQUEST_CODE = 100;
    boolean isPermissionGranted = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_still_ocr);

        cameraSurfaceView = findViewById(R.id.still_ocr_image_sv);
        capture = findViewById(R.id.still_ocr_capture_btn);
        textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        progressBar = findViewById(R.id.still_ocr_pb);

        //check and request for camera permissions
        isPermissionGranted = requestCameraPermissions();

        if(isPermissionGranted) {
            if(textRecognizer.isOperational()) {
                showCameraSource();
            } else {
                Log.d(TAG, "ERROR");
            }
        } else {
            Toast.makeText(StillOcrActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImageAndDetectAmount();
            }
        });

    }

    private void captureImageAndDetectAmount() {
        progressBar.setVisibility(View.VISIBLE);

        /*Take picture when the capture button is pressed. The picture callback is fired once the
            picture is taken and the image is then processed.
         */
        cameraSource.takePicture(null, new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes) {
                cameraSource.stop();

                //create a frame from the bitmap for the textrecognizer to detect text
                Bitmap billBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Frame billFrame = new Frame.Builder().setBitmap(billBitmap).build();
                SparseArray<TextBlock> items = textRecognizer.detect(billFrame);
                String price;
                ArrayList<String> detected = new ArrayList<>();
                ArrayList<Double> detectedPrices = new ArrayList<>();   //A list to store all the prices detected on the bill

                //Capture the bill and detect all the numbers(which includes the bill amount) from the bill
                for(int i = 0; i < items.size(); i++) {
                    TextBlock item = items.valueAt(i);
                    List<Line>  lines = (List<Line>) item.getComponents();   // get lines from the textblock(equivalent to a paragraph)

                    for(Line line : lines) {
                        List<Element> elements = (List<Element>) line.getComponents(); //get elements(equivalent to words) from a line
                        for(Element element : elements) {
                            //To ignore the text in the bill
                            try {
                                detectedPrices.add(Double.parseDouble(element.getValue()));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    detected.add(item.getValue());
//                    string.append("\n");
                }
                if(detectedPrices.size() != 0) {
                    price = Collections.max(detectedPrices).toString();
                } else {
                    price = "NA";
                }
                progressBar.setVisibility(View.INVISIBLE);
                showPrice(price);
            }
        });
    }

    /**
     * Display a dialog box containing the detected price with an option to save it.
     * @param price Detected price from the bill
     */
    private void showPrice(final String price) {
        AlertDialog.Builder priceDialog = new AlertDialog.Builder(this);
        priceDialog.setMessage("Price: " + price)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(StillOcrActivity.this,"Saved", Toast.LENGTH_LONG).show();
                        finish();
                        Date currentDateAndTime = Calendar.getInstance().getTime();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm");
                        String date = dateFormat.format(currentDateAndTime);
                        Bills bill = new Bills();
                        bill.setDate(date);
                        bill.setPrice(price);

                        //store the info in database
                        DatabaseHelper.getInstance(StillOcrActivity.this)
                                .getDatabase()
                                .billsDao()
                                .insert(bill);

                    }
                })
                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        try {
                            //restart the camera
                            cameraSource.start(cameraSurfaceView.getHolder());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        AlertDialog alert = priceDialog.create();
        alert.setTitle("Detected price");
        alert.setCancelable(false);
        alert.show();

    }

    private void showCameraSource() {

        //Builds a camera source which manages the camera along with the detector
        cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                .setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(2.0f)
                .build();

        cameraSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @SuppressLint("MissingPermission")
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource.start(cameraSurfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

    }

    private boolean requestCameraPermissions() {
        if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(StillOcrActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE);  //request for camera access if not already granted
            return false;
        } else {
            return true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showCameraSource();
        }
    }
}
