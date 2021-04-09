package com.chisomanuforom.mac.seamfix;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.predictions.models.Gender;
import com.amplifyframework.storage.s3.AWSS3StoragePlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;

public class EnrollActivity extends AppCompatActivity {


    //declaring views
    EditText editTextFirstName;
    EditText editTextSurname;
    Button buttonFemale;
    Button buttonMale;
    EditText editTextPhoneNumber;
    EditText editTextEmailAddress;
    ImageView imageViewPicture;
    Button buttonSave;

    String EMPTY_STRING = "";


    //declaring variables
    String firstName, surname, gender, phoneNumber, emailAddress, deviceId;
    byte[] picture ;
    int batteryLevel, deviceOsVersion;
    float cameraMegaPixels;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll);


        //initializing Amplify Auth and Storage
        try {
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.addPlugin(new AWSS3StoragePlugin());
            Amplify.configure(getApplicationContext());

            Log.i("MyAmplifyApp", "Initialized Amplify");
        } catch (AmplifyException error) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error);
        }

        //to show back button on action bar
        showBackButton();

        //showing activity name on action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Let's Get You Onboarded");
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        //setting the background color of the action bar
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#6600cc")));


        //loading the views
        editTextFirstName = (EditText)findViewById(R.id.editTextFirstName);
        editTextSurname = (EditText) findViewById(R.id.editTextSurname);
        buttonFemale =  (Button) findViewById(R.id.buttonFemale);
        buttonMale = (Button) findViewById(R.id.buttonMale);

        buttonFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                buttonFemale.setBackgroundColor(getResources().getColor(R.color.DarkSlateBlue));
                buttonFemale.setTextColor(getResources().getColor(R.color.White));
                buttonFemale.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.femeninewhite), null, null, null);
                buttonMale.setBackgroundColor(getResources().getColor(R.color.LightGrey));
                buttonMale.setTextColor(getResources().getColor(R.color.Black));
                buttonMale.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.malered), null, null, null);

            }
        });

        buttonMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                buttonMale.setBackgroundColor(getResources().getColor(R.color.DarkSlateBlue));
                buttonMale.setTextColor(getResources().getColor(R.color.White));
                buttonMale.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.masculinewhite), null, null, null);
                buttonFemale.setBackgroundColor(getResources().getColor(R.color.LightGrey));
                buttonFemale.setTextColor(getResources().getColor(R.color.Black));
                buttonFemale.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.malered), null, null, null);

            }
        });

        editTextPhoneNumber = (EditText) findViewById(R.id.editTextPhoneNumber);
        editTextEmailAddress = (EditText) findViewById(R.id.editTextEmailAddress);
        imageViewPicture = (ImageView) findViewById(R.id.imageViewPicture);
        imageViewPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(EnrollActivity.this);
            }
        });


        buttonSave = (Button) findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });




    }



    private void selectImage(Context context) {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);

                } else if (options[item].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , 1);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        imageViewPicture.setImageBitmap(selectedImage);
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                imageViewPicture.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                imageViewPicture.setScaleType(ImageView.ScaleType.FIT_XY);
                                imageViewPicture.setAdjustViewBounds(true);
                                cursor.close();
                            }
                        }

                    }
                    break;
            }
        }
    }


    public static int getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (int) (batteryPct * 100);
    }

    public static  String getDeviceId(Context context){

        String id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        return  id;
    }

    public static  int getDeviceOsVersion(){

        int deviceOs = Build.VERSION.SDK_INT;

        return  deviceOs;

    }


    public float getBackCameraResolutionInMp()
    {
        int noOfCameras = Camera.getNumberOfCameras();
        float maxResolution = -1;
        long pixelCount = -1;
        for (int i = 0;i < noOfCameras;i++)
        {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
            {
                Camera camera = Camera.open(i);;
                Camera.Parameters cameraParams = camera.getParameters();
                for (int j = 0;j < cameraParams.getSupportedPictureSizes().size();j++)
                {
                    long pixelCountTemp = cameraParams.getSupportedPictureSizes().get(j).width * cameraParams.getSupportedPictureSizes().get(j).height; // Just changed i to j in this loop
                    if (pixelCountTemp > pixelCount)
                    {
                        pixelCount = pixelCountTemp;
                        maxResolution = ((float)pixelCountTemp) / (1024000.0f);
                    }
                }

                camera.release();
            }
        }

        return maxResolution;
    }




    private void uploadFile() {
        File exampleFile = new File(getApplicationContext().getFilesDir(), "Payload");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(exampleFile));



            //using json to capture enrollment data
            JSONObject obj = new JSONObject();
            try {
                obj.put("FirstName", firstName);
                obj.put("Surname", surname);
                obj.put("Gender", gender);
                obj.put("PhoneNumber", phoneNumber);
                obj.put("EmailAddress", emailAddress);
                obj.put("picture", picture);
                obj.put("DeviceId", deviceId );
                obj.put("BatteryLevel", batteryLevel );
                obj.put("DeviceOsVersion", deviceOsVersion );
                obj.put("CameraMegaPixels", cameraMegaPixels );

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            writer.append(obj.toString());
            writer.close();
        } catch (Exception exception) {
            Log.e("MyAmplifyApp", "Upload failed", exception);
        }


        //The format for the Payload is json and saved as a .txt doc
        Amplify.Storage.uploadFile(
                "chisom_anuforom/Payload",
                exampleFile,
                result -> Log.i("MyAmplifyApp", "Successfully uploaded: " + result.getKey()),
                storageFailure -> Log.e("MyAmplifyApp", "Upload failed", storageFailure)


        );


    }




    protected void save(){


        //***********************************
        //validating entries
        //***********************************

        //validating first name
        if(!editTextFirstName.getText().equals(EMPTY_STRING)){
            firstName = editTextFirstName.getText().toString();
        } else{
            Toast.makeText(EnrollActivity.this, "First Name is required.", Toast.LENGTH_LONG).show();
            return;
        }
        //Validating Surname
        if(!editTextSurname.getText().equals(EMPTY_STRING)){
            surname = editTextSurname.getText().toString();
        } else{
            Toast.makeText(EnrollActivity.this, "Surname is required.", Toast.LENGTH_LONG).show();
            return;
        }

        //Validating Gender
        //if gender is female
//        ColorDrawable buttonFemaleColor = (ColorDrawable) buttonFemale.getBackground();
//        int buttonFemaleColorInt = buttonFemaleColor.getColor();
        if(((ColorDrawable) buttonFemale.getBackground()).getColor() == getResources().getColor(R.color.DarkSlateBlue)){

            gender = buttonFemale.getText().toString();
        }

        //if gender is male
//        ColorDrawable buttonMaleColor = (ColorDrawable) buttonMale.getBackground();
//        int buttonMaleColorInt = buttonMaleColor.getColor();
        if(((ColorDrawable) buttonMale.getBackground()).getColor() == getResources().getColor(R.color.DarkSlateBlue)){

            gender = buttonMale.getText().toString();
        }


        //validating phone number
        if(!editTextPhoneNumber.getText().equals(EMPTY_STRING)){
            phoneNumber = editTextPhoneNumber.getText().toString();
        } else{
            Toast.makeText(EnrollActivity.this, "Phone Number is required.", Toast.LENGTH_LONG).show();
            return;
        }


        //validating email address
        if(!editTextEmailAddress.getText().toString().isEmpty()){
            emailAddress = editTextEmailAddress.getText().toString();
        } else{
            Toast.makeText(EnrollActivity.this, "Email Address is required.", Toast.LENGTH_LONG).show();
            return;
        }

        //validate picture
        if(imageViewPicture != null){

           picture = getPictureArray(imageViewPicture);

        }



        batteryLevel  = getBatteryPercentage(EnrollActivity.this);

        deviceId = getDeviceId(EnrollActivity.this);

        deviceOsVersion = getDeviceOsVersion();

        cameraMegaPixels = getBackCameraResolutionInMp();



        uploadFile();//uploading to amazon s3 bucket



        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(EnrollActivity.this);
        builder.setTitle("Seamfix Test");
        builder.setMessage("Uploaded to S3 Bucket");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        builder.show();
        return;

    }

    private byte[] getPictureArray(ImageView picture){

        Bitmap bitmap = ((BitmapDrawable) picture.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageInByte = baos.toByteArray();

        return  imageInByte;

    }





    public void showBackButton() {

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }



}