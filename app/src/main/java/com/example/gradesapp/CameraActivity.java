package com.example.gradesapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.text.Normalizer;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.support.design.widget.Snackbar.make;

public class CameraActivity extends AppCompatActivity implements
        CameraBridgeViewBase.CvCameraViewListener2,
        LoaderCallbackInterface {
    private static final int REQUEST_INSTALLATION_OPENCV_MANAGER = 0;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int PERMISSION_APLLICATION_STORAGE = 3;
    private static final int ACTION_LOAD_OPENCV = 4;
    private CameraBridgeViewBase cameraView;
    private View view;
    private Mat entry;
    private Mat entry_gray;
    private Mat exit;
    private Mat lastExit;
    CheckOMR processor;
    int state;
    boolean stablereading;
    Test test;
    Test lastValidTest;
    String title;
    String solution;
    double maximumScore;
    Scalar colorSuspense;
    Scalar colorApproved;
    FloatingActionButton fabCancel;
    FloatingActionButton fabRefresh;
    FloatingActionButton fabSaveImage;
    Snackbar snackbarSave;
    Snackbar snackbarToCorrect;
    Snackbar snackbarUpdate;
    Snackbar snackbarSaveImage;
    static final String TAG = "CameraActivity";
    String action;
    String key;
    String photoPathOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        cameraView = (CameraBridgeViewBase) findViewById(R.id.view_camera);
        cameraView.setCvCameraViewListener(this);
        cameraView.disableFpsMeter();
        view = findViewById(R.id.content_main);
        test = new Test();
        maximumScore = 10;
        colorSuspense = new Scalar(204, 0, 0);
        colorApproved = new Scalar(102, 153, 0);
        fabCancel = (FloatingActionButton) findViewById(R.id.fabCancel);
        fabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orderPermissionsStorage();
                finish();
            }
        });
        fabRefresh = (FloatingActionButton) findViewById(R.id.fabRefresh);
        fabRefresh.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                snackbarSave.dismiss();
                snackbarToCorrect.dismiss();
                snackbarUpdate.dismiss();
                snackbarSaveImage.dismiss();
               fabRefresh.setVisibility(View.INVISIBLE);
               fabSaveImage.setVisibility(View.INVISIBLE);
//                fabRefresh.hide();
//                fabSaveImage.hide();
                state = 0;
                cameraView.enableView();
            }
        });
        fabSaveImage = (FloatingActionButton) findViewById(R.id.fabSaveImage);
        fabSaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orderPermissionsStorage();
                //System.out.println("FabSaveImage is clicked!!!!");
            }
        });
        snackbarSave = Snackbar.make(fabRefresh, "Test scanning", Snackbar.LENGTH_INDEFINITE)
                .setAction("save", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveSolution();
                    }
                })
                .setActionTextColor(getResources().getColor(R.color.colorAccent));
        snackbarSave.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        snackbarToCorrect = Snackbar.make(fabRefresh, "Test scanning", Snackbar.LENGTH_INDEFINITE)
                .setAction("correct", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Corrector();

                    }
                }).setActionTextColor(getResources().getColor(R.color.colorAccent));
        snackbarToCorrect.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        snackbarUpdate = Snackbar.make(fabRefresh, "Test scanning", Snackbar.LENGTH_INDEFINITE)
                .setAction("update", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        update();
                    }
                }).setActionTextColor(getResources().getColor(R.color.colorAccent));
        snackbarUpdate.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        snackbarSaveImage = Snackbar.make(fabRefresh, "Image is saved", Snackbar.LENGTH_INDEFINITE)
                .setAction("Ver", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(new File(photoPathOut)), "image/png");
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);
                    }
                }).setActionTextColor(getResources().getColor(R.color.colorAccent));
        snackbarSaveImage.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        manageAction();
    }

    private void manageAction() {
        action = getIntent().getExtras().getString("action");
        switch (action) {
            case "correct":
                actionToCorrect();
                break;
            case "update":
                actionUpdate();
                break;
            default:
                break;
        }
    }

    private void actionToCorrect() {
        // https://www.drillio.com/en/2011/java-remove-accent-diacritic/
        title = getIntent().getExtras().getString("title");
        title = Normalizer.normalize(title, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        ;
        solution = getIntent().getExtras().getString("solution");
    }

    private void actionUpdate() {
        key = getIntent().getExtras().getString("key");
    }

    // https://stackoverflow.com/questions/11392183/how-to-check-programmatically-if-an-application-is-installed-or-not-in-android
    public static boolean isPackageInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);
        if (intent == null) {
            return false;
        }
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return !list.isEmpty();
    }

    // https://stackoverflow.com/questions/11753000/how-to-open-the-google-play-store-directly-from-my-android-application
    public void installerApp(String appPackageName) {
        try {
            startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)),
                    REQUEST_INSTALLATION_OPENCV_MANAGER);
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)),
                    REQUEST_INSTALLATION_OPENCV_MANAGER);
        }
    }

    public void checkOpenCVManagerInstalled () {
        if (isPackageInstalled(this, "org.opencv.engine")) {
            ((MyApplication) getApplication()).setOpencvManagerInstalled(true);
            loadOpenCV();
        } else {
            ((MyApplication) getApplication()).setOpencvManagerInstalled(false);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You need to install the OpenCV Manager application.")
                    .setPositiveButton("Agreed!", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            installerApp("org.opencv.engine");
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
            builder.create().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_INSTALLATION_OPENCV_MANAGER:
                if (isPackageInstalled(this, "org.opencv.engine")) {
                    ((MyApplication) getApplication()).setOpencvManagerInstalled(true);
                    loadOpenCV();
                } else {
                    ((MyApplication) getApplication()).setOpencvManagerInstalled(false);
                    finish();
                }
                break;
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onStart() {
        super.onStart();
        fabRefresh.setVisibility(View.INVISIBLE);
        fabSaveImage.setVisibility(View.INVISIBLE);
//        fabRefresh.hide();
//        fabSaveImage.hide();
        checkPermissions(ACTION_LOAD_OPENCV);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    void checkPermissions(int action) {
        switch (action) {
            case ACTION_LOAD_OPENCV:
                checkOpenCVManagerInstalled ();
                askCameraPermission();
                break;
        }
    }

    void loadOpenCV() {
        if (((MyApplication) getApplication()).canLoadOpenCV())
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, this);
    }

    @Override
    public void onPause() {
        if (cameraView != null)
            cameraView.disableView();
        if (getIntent().hasExtra("solution"))
            getIntent().getExtras().remove("solution");
        snackbarSave.dismiss();
        snackbarToCorrect.dismiss();
        snackbarUpdate.dismiss();
        snackbarSaveImage.dismiss();
        super.onPause();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraView != null)
            cameraView.disableView();
    }

    public void askCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ((MyApplication) getApplication()).setPermissionCamera(false);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Snackbar snackbar = make(view, "Permission is needed to use the camera.", Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }
                });
                snackbar.show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        } else {
            ((MyApplication) getApplication()).setPermissionCamera(true);
            loadOpenCV();
        }
    }


    @Override
    public void onManagerConnected(int status) {
        switch (status) {
            case LoaderCallbackInterface.SUCCESS:
                state = 0;
                stablereading = false;
                cameraView.enableView();
                entry_gray = new Mat();
                lastExit = new Mat();
                break;
            default:
                Toast.makeText(CameraActivity.this, "OpenCV did not load correctly. In some cases, this issue has been resolved by uninstalling the OpenCV Manager application and reinstalling it from the Play Store." , Toast.LENGTH_LONG).show();
                finish();
                break;
        }
    }

    @Override
    public void onPackageInstall(int operation, InstallCallbackInterface callback) {

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        exit = new Mat();
        processor = new CheckOMR();
    }

    @Override
    public void onCameraViewStopped() {}

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        switch (state) {
            case 0: // Lectura de la cámara
                entry = inputFrame.rgba();
                entry_gray = inputFrame.gray();
                test = new Test();
                stablereading = processor.proccess(entry_gray, exit, test);
                entry.copyTo(exit);
                if (stablereading) {
                    exit.copyTo(lastExit);
                    lastValidTest = test;
                    processor.drawBoxes(exit, test);
                    state = 1;
                    launchMessage();
                }
                break;
            case 1: // Sample scanned test
                lastExit.copyTo(exit);
                processor.drawBoxes(exit, lastValidTest);
                break;
            case 3: // Show the result of the correction
                if (lastValidTest.sameStructure(solution)) {
                    String Correction = lastValidTest.getCorrection(solution);
                    processor.drawCorrection(lastExit, lastValidTest, Correction);
                    lastExit.copyTo(exit);
                    double punctuation = lastValidTest.getScore(maximumScore, Correction, false);
                    String user = ((MyApplication) getApplication()).getUsername();
                    String date= getDate(System.currentTimeMillis());
                    processor.showTitle(lastExit, title, user, date);
                    processor.showScore(lastExit, punctuation, maximumScore);
                    lastExit.copyTo(exit);
                    state = 4;
                }
                break;
            case 4: // Una vez showa la corrección, dejamos de update la pantalla.
                lastExit.copyTo(exit);
                deactiveCameraView();
                break;
        }
        return exit;
    }

    void deactiveCameraView() {
        CameraActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cameraView.disableView();
            }
        });
    }

    private boolean takePhoto(final Mat output) {
        final long currentTimeMillis = System.currentTimeMillis();
        final String appName = getString(R.string.app_name);
        final String galleryPath = Environment
                .getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).toString();
        final String albumPath = galleryPath + "/" + appName;
        photoPathOut = albumPath + "/Out_" + currentTimeMillis + ".png";
        boolean result = true;
        File album = new File(albumPath);
        if (!album.isDirectory() && !album.mkdirs()) {
            Log.e(TAG, "Error creating the directory" + albumPath);
            return false;
        }
        Mat mBgr = new Mat();
        if (output.channels() == 1)
            Imgproc.cvtColor(output, mBgr, Imgproc.COLOR_GRAY2BGR, 3);
        else
            Imgproc.cvtColor(output, mBgr, Imgproc.COLOR_RGBA2BGR, 3);
        if (!Imgcodecs.imwrite(photoPathOut, mBgr)) {
            Log.e(TAG, "Fail to save " + photoPathOut);
            result = false;
        }
        mBgr.release();
        return result;
    }

    public void orderPermissionsStorage() {
        view = findViewById(R.id.content_main);
        System.out.println("I am in orderPerStore!!!!");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("I am in orderPerStore!!!! 1st if");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                System.out.println("I am in orderPerStore!!!! 2nd if");
                make(view, "Permission is needed to use the storage", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_APLLICATION_STORAGE);
                            }
                        }).show();
            } else {
                System.out.println("I am in orderPerStore!!!! 2nd else");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_APLLICATION_STORAGE);
            }
        } else {
            System.out.println("I am in orderPerStore!!!! 1st else");
            saveImage();
        }
    }

    void saveImage() {
        System.out.println("I am in saveImage");
        if (takePhoto(exit)) {
            System.out.println("I am in save`image!!!! 1st if");
            snackbarSaveImage.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((MyApplication) getApplication()).setPermissionCamera(true);
                    loadOpenCV();
                } else {
                    Snackbar snackbar = make(view, "Not having permission to use the camera is disabled this functionality", Snackbar.LENGTH_INDEFINITE)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    finish();
                                }
                            }).setActionTextColor(getResources().getColor(R.color.colorAccent));
                    snackbar.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    snackbar.show();
                }
                break;
            }
            case PERMISSION_APLLICATION_STORAGE: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto(exit);
                    showSnackbar("Image is saved");
                } else {
                    Snackbar snackbar = make(view, "If you do not have storage permission, you can not save the image.", Snackbar.LENGTH_INDEFINITE)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                }
                            }).setActionTextColor(getResources().getColor(R.color.colorAccent));
                    snackbar.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    snackbar.show();
                }
                break;
            }
        }
    }

    public void saveSolution() {
        Intent intent = new Intent(CameraActivity.this, ExamDetailActivity.class);
        intent.putExtra("action", "save");
        intent.putExtra("solution", lastValidTest.getCodification());
        intent.putExtra("numberOfQuestions", lastValidTest.getNumberOfQuestions());
        intent.putExtra("creationDate", System.currentTimeMillis());
        startActivity(intent);
    }

    @SuppressLint("RestrictedApi")
    public void Corrector() {
        state = 3;
        cameraView.enableView();
     fabSaveImage.setVisibility(View.VISIBLE);
        fabSaveImage.show();
    }

    public void update() {
        Intent intent = new Intent(CameraActivity.this, ExamDetailActivity.class);
        intent.putExtra("action", "update");
        intent.putExtra("key", key);
        intent.putExtra("solution", lastValidTest.getCodification());
        intent.putExtra("numberOfQuestions", lastValidTest.getNumberOfQuestions());
        startActivity(intent);
    }

    public void launchMessage() {
        CameraActivity.this.runOnUiThread(new Runnable() {
            @SuppressLint("RestrictedApi")
            @Override
            public void run() {
                switch (action) {
                    case "create":
                        cameraView.disableView();
                        fabRefresh.setVisibility(View.VISIBLE);

//                        fabRefresh.show();
                        snackbarSave.show();
                        break;
                    case "correct":
                        if (lastValidTest.sameStructure(solution)) {
                            cameraView.disableView();
                            fabRefresh.setVisibility(View.VISIBLE);
//                            fabRefresh.show();
                            snackbarToCorrect.show();
                        } else {
                            state = 0;
                        }
                        break;
                    case "update":
                        cameraView.disableView();
                        fabRefresh.setVisibility(View.VISIBLE);
//                        fabRefresh.show();
                        snackbarUpdate.show();
                        break;
                }
            }
        });
    }

    void showSnackbar(String message) {
        Snackbar snackbar = make(view, message, Snackbar.LENGTH_LONG)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                }).setActionTextColor(getResources().getColor(R.color.colorAccent));
        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        snackbar.show();
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }
}
