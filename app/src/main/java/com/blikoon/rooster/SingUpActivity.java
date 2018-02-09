package com.blikoon.rooster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

public class SingUpActivity extends AppCompatActivity {
    private static final String TAG="SignUpActivity";
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private String userChoosenTask,filePath;

    private EditText userName,name,email,password;
    Button signUp;
    private CircleImageView profileImageView;
    private BroadcastReceiver mBroadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        userName= (EditText) findViewById(R.id.userName);
        name= (EditText) findViewById(R.id.name);
        email= (EditText) findViewById(R.id.email_id);
        password= (EditText) findViewById(R.id.password_);
        profileImageView= (CircleImageView) findViewById(R.id.userProfileImage);
        signUp= (Button) findViewById(R.id.sign_up_button);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attempSignUp();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                switch (action)
                {
                    case RoosterConnectionService.UI_AUTHENTICATED:
                        Log.d(TAG,"Got a broadcast to show the main app window");
                        //Show the main app window
                        Utils.dismissLoader();
                        Intent i2 = new Intent(SingUpActivity.this,ContactListActivity.class);
                        startActivity(i2);
                        finish();
                        break;
                }

            }
        };
        IntentFilter filter = new IntentFilter(RoosterConnectionService.UI_AUTHENTICATED);
        this.registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mBroadcastReceiver);
    }

    public void attempSignUp(){
        userName.setError(null);
        password.setError(null);

        String userNameValue=userName.getText().toString();
        String nameValue=name.getText().toString();
        String emailValue=email.getText().toString();
        String passwordValue=password.getText().toString();

        boolean cancel=false;
        View focusView=null;

        // Check for a valid userName.
        if (TextUtils.isEmpty(userNameValue)) {
            userName.setError(getString(R.string.error_field_required));
            focusView = userName;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(passwordValue) && !isPasswordValid(passwordValue)) {
            password.setError(getString(R.string.error_invalid_password));
            focusView = password;
            cancel = true;
        }

        if(cancel){
            focusView.requestFocus();
        }else{
       //     Utils.showLoader(AppController.mInstance);
            saveCredentialsAndSignUp();
        }
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }


    private void saveCredentialsAndSignUp()
    {
        Log.d(TAG,"saveCredentialsAndLogin() called.");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .putString("xmpp_jid", userName.getText().toString())
                .putString("xmpp_password", password.getText().toString())
                .putString("xmpp_name",name.getText().toString())
                .putString("xmpp_email",email.getText().toString())
                .putString("xmpp_profileImageFilePath",filePath)
                .commit();

        //Start the service
        Intent i1 = new Intent(this,RoosterConnectionService.class);
        startService(i1);

    }


 /*PROFILE IMAGE SELECTION*/
 public void performSelectImage(View view){
     selectImage();
 }
    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Gallery",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(SingUpActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result= Utils.checkPermission(SingUpActivity.this);

                if (items[item].equals("Take Photo")) {
                    userChoosenTask ="Take Photo";
                    if(result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Gallery")) {
                    userChoosenTask ="Choose from Gallery";
                    if(result)
                        galleryIntent();

                }
                else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        filePath = destination.getAbsolutePath();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(destination));
        intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {

        try{
            Bitmap bm=Utils.loadSampledImage(filePath);
            profileImageView.setImageBitmap(bm);
        }  catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm=null;
        Uri selectedImageUri=null;
        if (data != null) {
            try {
                selectedImageUri=data.getData();
                filePath=getRealPathFromURI(selectedImageUri);
                bm=Utils.loadSampledImage(filePath);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        profileImageView.setImageBitmap(bm);
    }

    private String getRealPathFromURI(Uri contentURI) {
        String picturePath="";
        try {
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(contentURI,
                    filePath, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePath[0]);
            picturePath = c.getString(columnIndex);
            c.close();
        }catch (Exception e){

        }
        return picturePath;
    }
}
