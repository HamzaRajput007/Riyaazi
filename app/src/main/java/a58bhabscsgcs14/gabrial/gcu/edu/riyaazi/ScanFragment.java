package a58bhabscsgcs14.gabrial.gcu.edu.riyaazi;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraUtils;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class ScanFragment extends Fragment {


    @Override
    public void onResume() {
        super.onResume();
        View view = getView();

        crop = view.findViewById(R.id.imageView2);
        cameraView = view.findViewById(R.id.camera);
        cameraView.setLifecycleOwner(this);
        cameraView.mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER); // Tap to focus!
        cameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] picture) {
                super.onPictureTaken(picture);
                // CameraUtils will read EXIF orientation for you, in a worker thread.
                CameraUtils.decodeBitmap(picture, e -> {
                    ProcessedImage = e;
                    ProcessImage(ProcessedImage);
                });
            }
        });
        cameraView.start();
        FloatingActionButton btn = view.findViewById(R.id.btnCapture);
        btn.setOnClickListener(v -> cameraView.capturePicture());
    }

    private static final String TAG = "MainActivity";
    public Bitmap ProcessedImage;
    public Bitmap DisplayImage;
    private CameraView cameraView;
    private ImageView crop;


    public ScanFragment() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }


    public void ProcessImage(Bitmap imageToProcess) {
        try {

            int height = cameraView.getHeight();
            int width = cameraView.getWidth();

            int imageHeight = imageToProcess.getHeight();
            int imageWidth = imageToProcess.getWidth();
            int cropheight = crop.getHeight();
            int cropWidth = crop.getWidth();
            int heightratio = imageHeight / cropheight;
            int widthratio = imageWidth / cropWidth;

            Mat raw = new Mat();
            Mat Gray = new Mat();
            Mat Gaus = new Mat();
            Mat thresh = new Mat();

            Bitmap tar = Bitmap.createBitmap(imageToProcess, imageWidth / 2 - (cropWidth), imageHeight / 2 - (cropheight), (imageWidth / widthratio) * 2, (imageHeight / heightratio) * 2);

            imageToProcess = tar;
            Utils.bitmapToMat(imageToProcess, raw);
            Imgproc.cvtColor(raw, Gray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(Gray, Gaus, new Size(3, 3), 0);
            Imgproc.adaptiveThreshold(Gaus, thresh, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 99, 4);
            Bitmap bmp = Bitmap.createBitmap(imageToProcess.getWidth(), imageToProcess.getHeight(), Bitmap.Config.ARGB_4444);
            Utils.matToBitmap(thresh, bmp);
            //    String str = detectText(bmp);

            CurrentBitmap = bmp;
            RunRecognition(bmp);

            // MainActivity.Instance.LoadResult(str , bmp);
            ProcessedImage = null;
        } catch (Exception ex) {
            arHelper.ShowToast("In ProcessImage() : " + ex.getMessage());
        }
    }

    Bitmap CurrentBitmap;

    void RunRecognition(Bitmap img) {


        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(img);
        FirebaseVisionTextRecognizer recognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        recognizer.processImage(image)
                .addOnSuccessListener(
                        texts -> {
                            processTextRecognitionResult(texts);
                        })
                .addOnFailureListener(
                        e -> {
                            // Task failed with an exception
                            e.printStackTrace();
                        });
        return;
    }

    private void processTextRecognitionResult(FirebaseVisionText texts) {
        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();

        if (blocks.size() == 0) {
            Log.d("MLKit", "Error ");
            return;
        }
        List<FirebaseVisionText.Line> lines = blocks.get(0).getLines();
        MainActivity.Instance.LoadResult(lines.get(0).getText() , CurrentBitmap);
        return;
    }


    public String detectText(Bitmap bitmap) {

        String text = "";
        try {
            TessBaseAPI tessBaseAPI = new TessBaseAPI();
            try {
                tessBaseAPI.init(MainActivity.Instance.datapath, MainActivity.Instance.langfile);
            } catch (Exception e) {
                Toast.makeText(MainActivity.Instance, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                return null;
            }

            //  tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "1234567890+-/x*");
            //  tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()=qwertyuiop[]}{POIU" + "YTREWQasdASDfghFGHjklJKLl;L:'|~`cvCVbnmBNM,./<>?");
            tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);

            tessBaseAPI.setImage(bitmap);
            text = tessBaseAPI.getUTF8Text();
            tessBaseAPI.end();

        } catch (Exception ex) {
            Toast.makeText(MainActivity.Instance, ex.getMessage(), Toast.LENGTH_LONG).show();
        }

        return text;
    }

    public String alignPicture(Bitmap filteredImageRecieved) {
        // FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(filteredImageRecieved);
        // FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
        //         .getOnDeviceTextRecognizer();
        return "";
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


}

class arHelper {
    public static void ShowToast(String text) {

        Toast.makeText(MainActivity.Instance, text, Toast.LENGTH_LONG).show();
    }
}

