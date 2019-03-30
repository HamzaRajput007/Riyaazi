package a58bhabscsgcs14.gabrial.gcu.edu.riyaazi;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ViewPager viewPager;
    private DrawerLayout drawer;
    private TabLayout tabLayout;
    private TabAdapter adapter;


    String datapath;
    File folder;
    File file;
    String langfile = "eng";

    public static MainActivity Instance;


    public void LoadResult(String res, Bitmap resultanBmp) {
        Fragment item = adapter.getItem(2);
        View a = item.getView();
        TextView viewById = a.findViewById(R.id.txtResult);
        ImageView img = a.findViewById(R.id.imgResult);
        LinearLayout cont = a.findViewById(R.id.resultContainer);
        img.setVisibility(View.VISIBLE);
        viewById.setVisibility(View.VISIBLE);
        cont.removeAllViews();
        img.setImageBitmap(resultanBmp);
        viewById.setText(res);
        viewPager.setCurrentItem(2);

    }


    public void LoadCardResult(String s, List<String> output) {
        viewPager.setCurrentItem(2);

        Fragment item = adapter.getItem(2);
        View a = item.getView();

        LinearLayout parent = a.findViewById(R.id.resultContainer);
        TextView viewById = a.findViewById(R.id.txtResult);
        ImageView img = a.findViewById(R.id.imgResult);
        img.setVisibility(View.GONE);
        viewById.setVisibility(View.GONE);
        parent.removeAllViews();
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(10, 10, 10, 10);

        for (String line : output) {
            CardView v = new CardView(this);
            v.setLayoutParams(p);
            v.setRadius(9);
            v.setContentPadding(15, 15, 15, 15);
            v.setCardBackgroundColor(Color.WHITE);
            v.setMaxCardElevation(15);
            v.setCardElevation(9);

            TextView t = new TextView(this);
            t.setTextSize(18);
            t.setText(line);
            t.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            v.addView(t);
            parent.addView(v);
        }
        {
            CardView v = new CardView(this);
            v.setLayoutParams(p);
            v.setRadius(9);
            v.setContentPadding(15, 15, 15, 15);
            v.setCardBackgroundColor(Color.WHITE);
            v.setMaxCardElevation(15);
            v.setCardElevation(9);
            TextView t = new TextView(this);
            t.setText(s);
            t.setTextSize(22);
            t.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            v.addView(t);
            v.setPadding(8, 8, 8, 8);
            parent.addView(v);
        }

    }

    private void ConfirmFileExists() {
        datapath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Tessarect";
        folder = new File(datapath + "/tessdata");
        file = new File(folder + "/" + langfile + ".traineddata");

        if (!folder.exists()) {
            folder.mkdirs();
            copyFiles();
        }
        if (folder.exists()) {
            if (!file.exists()) {
                copyFiles();
            }
        }
    }


    private void copyFiles() {
        try {
            AssetManager assetManager = getAssets();
            InputStream instream = assetManager.open(langfile + ".traineddata");
            OutputStream outstream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();
            File f = new File(file.getAbsolutePath());
            if (!f.exists()) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.d("OpenCV", "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new NewCalculatorFragment(), "Calculator");
        adapter.addFragment(new ScanFragment(), "Scan");
        adapter.addFragment(new ResultFragment(), "Result");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        Instance = this;
        AskPermissions();

        ConfirmFileExists();
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.menu);
        drawer = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navView = findViewById(R.id.nav_view);
        Menu menu = navView.getMenu();
        menu.findItem(R.id.help).setOnMenuItemClickListener(item1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.Instance);
            // Get the layout inflater
            LayoutInflater inflater = MainActivity.Instance.getLayoutInflater();

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.activity_helpand_feedback, null));
            builder.create().show();
            return false;
        });
        menu.findItem(R.id.about).setOnMenuItemClickListener(item1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.Instance);
            // Get the layout inflater
            LayoutInflater inflater = MainActivity.Instance.getLayoutInflater();

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.activity_about_us, null));
            builder.create().show();
            return false;
        });
        menu.findItem(R.id.how).setOnMenuItemClickListener(item1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.Instance);
            // Get the layout inflater
            LayoutInflater inflater = MainActivity.Instance.getLayoutInflater();

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.activity_supported_feature, null));

            builder.create().show();
            return false;
        });


    }

    private void AskPermissions() {

    }


    @Override
    protected void onResume() {
        super.onResume();

        AskPermissions();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.help) {
            viewPager.setCurrentItem(0);
        } else if (id == R.id.how) {
            viewPager.setCurrentItem(1);
        } else if (id == R.id.about) {
            viewPager.setCurrentItem(2);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


}



