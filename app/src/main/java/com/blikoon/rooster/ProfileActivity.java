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
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.drawingdemo.ImageEditingActivity;
import com.example.drawingdemo.Util;

import org.jivesoftware.smack.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG="ProfileActivity";
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private String userChoosenTask,filePath,userNameValue;

    private TextView nameText,emailIdText,phoneNoText;
    Typeface roboto_regular;
    private EditText userName,name,email,password;
    Button update;
    ImageView logoutBtn,imageViewLayout,editProfileInfoIcon;
    private CircleImageView profileImageView;
    private BroadcastReceiver mLoadProfileBroadcastReceiver;
    Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        roboto_regular= Utils.getCustomFont(this,FontType.ROBOTO_REGULAR);
        userNameValue=PreferenceManager.getDefaultSharedPreferences(this)
                .getString("xmpp_jid",null);

        nameText= (TextView) findViewById(R.id.userNickName);
        emailIdText= (TextView) findViewById(R.id.userEmailAddress);
        phoneNoText= (TextView) findViewById(R.id.userMobileNo);
        imageViewLayout= (ImageView) findViewById(R.id.profileImageLayoutView);
        editProfileInfoIcon= (ImageView) findViewById(R.id.editProfileInfoIcon);

        nameText.setTypeface(roboto_regular);
        emailIdText.setTypeface(roboto_regular);
        phoneNoText.setTypeface(roboto_regular);

        /*userName= (EditText) findViewById(R.id.userName);
        name= (EditText) findViewById(R.id.name);
        email= (EditText) findViewById(R.id.email_id);
        password= (EditText) findViewById(R.id.password_);*/
        profileImageView= (CircleImageView) findViewById(R.id.userProfileImageIcon);
       // update= (Button) findViewById(R.id.profileUpdate_button);
        logoutBtn= (ImageView) findViewById(R.id.Logout);
        editProfileInfoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ProfileActivity.this,UpdateProfile.class);
                startActivity(intent);
            }
        });
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoosterConnectionService.sConnectionState= RoosterConnection.ConnectionState.DISCONNECTED;
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ProfileActivity.this);
                prefs.edit().clear().commit();
                Intent intent=new Intent(ProfileActivity.this,LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        loadProfileData();
    }
    public void attempSignUp(){
        userName.setError(null);
        password.setError(null);

   //     String userNameValue=userName.getText().toString();
        String nameValue=name.getText().toString();
        String emailValue=email.getText().toString();
        String passwordValue=password.getText().toString();

        boolean cancel=false;
        View focusView=null;

        // Check for a valid userName.
      /*  if (TextUtils.isEmpty(userNameValue)) {
            userName.setError(getString(R.string.error_field_required));
            focusView = userName;
            cancel = true;
        }*/

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(passwordValue) && !isPasswordValid(passwordValue)) {
            password.setError(getString(R.string.error_invalid_password));
            focusView = password;
            cancel = true;
        }

        if(cancel){
            focusView.requestFocus();
        }else{
            Utils.showLoader(ProfileActivity.this);
            updateProfile();
        }
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }


    private void updateProfile()
    {
        Log.d(TAG,"saveCredentialsAndLogin() called.");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
               /* .putString("xmpp_jid", userName.getText().toString())*/
                /*.putString("xmpp_password", password.getText().toString())*/
                .putString("xmpp_name",name.getText().toString())
                .putString("xmpp_email",email.getText().toString())
                .putString("xmpp_profileImageFilePath",filePath)
                .commit();

        if (RoosterConnectionService.getState().equals(RoosterConnection.ConnectionState.CONNECTED)) {
            Log.d(TAG, "The client is connected to the server,Update User Info");
            //Send the message to the server

            Intent intent = new Intent(RoosterConnectionService.UPDATE_USER_PROFILE_DATA);
            intent.putExtra(RoosterConnectionService.VCARD_NAME,
                    name.getText().toString());
            intent.putExtra(RoosterConnectionService.VCARD_EMAIL,email.getText().toString());
            if(filePath!=null)
            intent.putExtra(RoosterConnectionService.VCARD_PROFILE_IMAGE,filePath);

            sendBroadcast(intent);

        }
    }

  public void uploadProfileImage(){
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
      prefs.edit()
              .putString("xmpp_profileImageFilePath",filePath)
              .commit();

      if (RoosterConnectionService.getState().equals(RoosterConnection.ConnectionState.CONNECTED)) {
          Log.d(TAG, "The client is connected to the server,Upload profile image");
          //Send the message to the server

          Intent intent = new Intent(RoosterConnectionService.UPLOAD_USER_PROFILE_IMAGE);

          if(filePath!=null)
              intent.putExtra(RoosterConnectionService.VCARD_PROFILE_IMAGE,filePath);

          sendBroadcast(intent);

      }
  }

public void loadProfileData(){
    if (RoosterConnectionService.getState().equals(RoosterConnection.ConnectionState.CONNECTED)) {
        Log.d(TAG, "The client is connected to the server,Update User Info");
        //Send the message to the server

        Intent intent = new Intent(RoosterConnectionService.LOAD_VCARD);
        sendBroadcast(intent);

    }
}

public void performFullImageDisplay(View view){
    if(bitmap!=null)
      Utils.showFullImageDialog(this,bitmap);
    else
        Toast.makeText(this,"Image Not Available !!!",Toast.LENGTH_LONG).show();
}

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle=intent.getExtras();
        if(bundle!=null){
         String action=bundle.getString("action");
         if(action!=null && action.equalsIgnoreCase("refresh"))
           loadProfileData();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLoadProfileBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                switch (action)
                {
                    case RoosterConnectionService.GET_USER_PROFILE_DATA:
                        Log.d(TAG,"Got a broadcast to show the main app window");
                        //Show the main app window
                    String nickName=intent.getStringExtra(RoosterConnectionService.VCARD_NAME);
                    String emailAddress= intent.getStringExtra(RoosterConnectionService.VCARD_EMAIL);
                        String mobileNo= intent.getStringExtra(RoosterConnectionService.VCARD_MOBILE_NO);
                    byte [] imgArray=intent.getByteArrayExtra(RoosterConnectionService.VCARD_PROFILE_IMAGE);
                        if(imgArray!=null) {
                            bitmap = BitmapFactory.decodeByteArray(imgArray, 0, imgArray.length);
                            profileImageView.setImageBitmap(null);
                            profileImageView.setImageBitmap(bitmap);
                           /* imageViewLayout.setImageBitmap(null);
                            imageViewLayout.setImageBitmap(bitmap);*/
                            String saveThis = Base64.encodeToString(imgArray, Base64.DEFAULT);
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ProfileActivity.this);
                            prefs.edit()
                                    .putString("profileImage", saveThis)
                                    .commit();
                        }
                        if(nickName!=null)
                            nameText.setText(Utils.capitalize(nickName));
                        if(emailAddress!=null)
                            emailIdText.setText(emailAddress);
                        if(mobileNo!=null)
                        phoneNoText.setText(mobileNo);
Utils.dismissLoader();

                        /*Log.d("userName",intent.getStringExtra(RoosterConnectionService.VCARD_USERNAME));
                        Log.d("NickName",intent.getStringExtra(RoosterConnectionService.VCARD_NAME));
                        Log.d("Email",intent.getStringExtra(RoosterConnectionService.VCARD_EMAIL));*/
                }

            }
        };
        IntentFilter filter = new IntentFilter(RoosterConnectionService.GET_USER_PROFILE_DATA);
        this.registerReceiver(mLoadProfileBroadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mLoadProfileBroadcastReceiver);
    }

    /*PROFILE IMAGE SELECTION*/
    public void performSelectImage(View view){
        selectImage();
    }
    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Gallery",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result= Utils.checkPermission(ProfileActivity.this);

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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();  return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i=new Intent(ProfileActivity.this,ContactListActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        i.putExtra("action","refresh");
        startActivity(i);
        finish();
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

   /* private void onCaptureImageResult(Intent data) {

        try{
            Bitmap bm=Utils.loadSampledImage(filePath);
            profileImageView.setImageBitmap(bm);
            uploadProfileImage();
        }  catch (Exception e) {
            e.printStackTrace();
        }


    }*/

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm=null;
        Uri selectedImageUri=null;
        if (data != null) {
            try {
                selectedImageUri=data.getData();
                filePath=getRealPathFromURI(selectedImageUri);
                bm=Utils.loadSampledImage(filePath);
         //       profileImageView.setImageBitmap(bm);
                uploadProfileImage();

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


    private void onCaptureImageResult(Intent data) {

        try {
            System.gc();
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inSampleSize = 2;

            Bitmap newBitmap = BitmapFactory.decodeFile(
                    filePath, bounds);
            ExifInterface exif = new ExifInterface(
                    filePath);
            String orientString = exif
                    .getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = orientString != null ? Integer
                    .parseInt(orientString)
                    : ExifInterface.ORIENTATION_NORMAL;

            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
                rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
                rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
                rotationAngle = 270;

            Matrix mat = new Matrix();
            mat.postRotate(rotationAngle);

            newBitmap = Bitmap.createBitmap(newBitmap, 0, 0,
                    newBitmap.getWidth(), newBitmap.getHeight(),
                    mat, true);

            FileOutputStream out = null;
            out = new FileOutputStream(filePath);

            // write the compressed bitmap at the destination specified by
            // filename.
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            uploadProfileImage();

            profileImageView.setImageBitmap(newBitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putString("file_uri", filePath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        filePath = savedInstanceState.getString("file_uri");
    }

}
