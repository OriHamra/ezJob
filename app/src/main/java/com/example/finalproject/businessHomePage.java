package com.example.finalproject;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class businessHomePage extends AppCompatActivity {

    private String businessNameString;

    private Intent intentPosition;

    private TextView businessNameView;
    private TextView title;
    private Button uploadPic;
    private ImageView businessPic;

    private ListView listViewPositions;
    private ArrayAdapter<String> positionsAdapter;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private int numOfBusinesses;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;

    private String username;
    private String password;
    private int businessNumber;

    private ArrayList<String> usernameList;
    private ArrayList<String> passwordList;
    private ArrayList<String> businessNameList;
    private ArrayList<String> positionNameList;

    public static final String SHARED_PREFS = "sharedPrefs"; //name of shared prefs
    public static final String KEY_REMEMBER = "rememberMe"; // shared prefs value to save - remember me


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_home_page);
        openScreen();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.business_menu, menu);
        return true;
    }

    /**
     * this function is responsible on the menu in this activity
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.candidate:
                clickOnCandidate();
                return true;

            case R.id.myStatus:
                return true;

            case R.id.openStatus:
                changeBusinessStatus("open");
                return true;

            case R.id.closeStatus:
                changeBusinessStatus("close");
                return true;

            case R.id.employed:
                clickOnEmployees();
                return true;

            case R.id.log_out_business:
                logOut();
                return true;

            default: return super.onOptionsItemSelected(item);

        }
    }

    /**
     * log out function- delete the remember key
     */
    public void logOut(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_REMEMBER, false);
        editor.apply();
        finishAffinity();
        // exit from ez job completely
    }

    /**
     * when the user clicked on my candidates in the menu, open candidate activity
     */
    public void clickOnCandidate(){
        Intent intent = new Intent(businessHomePage.this, CandidateForWorkScreen.class);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        startActivity(intent);
        Toast.makeText(this, "candidate selected", Toast.LENGTH_SHORT).show();
    }

    /**
     * change the status of business when the user does not want to be open to request
     * @param status
     */
    public void changeBusinessStatus(String status){
        databaseReference.child("business " + businessNumber).child("status").setValue(status);
    }

    /**
     * when the user click on my employees, open employees activity.
     */
    public void clickOnEmployees(){
        Intent intent = new Intent(businessHomePage.this, EmployeesOfBusiness.class);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        startActivity(intent);
        Toast.makeText(this, "employees selected", Toast.LENGTH_SHORT).show();
    }

    /**
     * this function just open business home page activity screen, for convenience
     */
    public void openScreen() {
        uploadPic = (Button) findViewById(R.id.uploadPic);
        uploadPic.setVisibility(View.INVISIBLE); // invisible the upload button
        title = (TextView) findViewById(R.id.title);
        businessPic = (ImageView) findViewById(R.id.businessPic);
        businessNameView = (TextView) findViewById(R.id.businessName);

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child("Businesses");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");

        usernameList = new ArrayList<>();
        passwordList = new ArrayList<>();
        businessNameList = new ArrayList<>();
        positionNameList = new ArrayList<>();

        intentPosition = new Intent(this, positionScreen.class);

        ReadFromDatabase();
    }

    /**
     * read businesses deatails from real time database
     */
    public void ReadFromDatabase(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numOfBusinesses = (int) dataSnapshot.getChildrenCount();
                for (int i = 1; i <= dataSnapshot.getChildrenCount(); i++){
                    String businessUsernameFromData = ((String) dataSnapshot.
                            child("business " + i).child("userName").getValue());
                    usernameList.add(businessUsernameFromData); // user names list
                    String businessPasswordFromData = ((String) dataSnapshot.
                            child("business " + i).child("password").getValue());
                    passwordList.add(businessPasswordFromData); // password list
                    String businessNameFromData = ((String) dataSnapshot.
                            child("business " + i).child("businessName").getValue());
                    businessNameList.add(businessNameFromData); // business name list
                }
                getBusinessDeatails();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // when there is an error (no internet, no signal)
                AlertDialog.Builder connectError = new AlertDialog.Builder(businessHomePage.this).
                        setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                connectError.show();
            }
        });
    }

    /**
     * get the specific business and the deatails of it - positions.
     */
    public void getBusinessDeatails(){
        for (int i =0; i<numOfBusinesses;i++){
            String userFromData = usernameList.get(i);
            String passwordFromData = passwordList.get(i);
            if (username.equals(userFromData) && password.equals(passwordFromData)){
                businessNameString = businessNameList.get(i);
                getBusinessName(businessNameString);
                businessNumber = i+1;
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int numOfPositions = (int) dataSnapshot.child("business " +businessNumber)
                                .child("positions").getChildrenCount();
                        for (int j =0 ; j < numOfPositions; j++){
                            String positionNameFromData = ((String)dataSnapshot.
                                    child("business " + businessNumber).child("positions")
                                    .child(String.valueOf(j))
                                    .child("positionName").getValue());
                            if (!positionNameList.contains(positionNameFromData)) {
                                positionNameList.add(positionNameFromData);
                            }
                        }
                        showListPositions(businessNumber);
                        downloadImageView();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // when there is an error (no internet, no signal)
                        AlertDialog.Builder connectError = new AlertDialog.Builder(businessHomePage.this)
                                .setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                        connectError.show();
                    }
                });
            }
        }
    }

    /**
     * get the business name
     * @param name
     */
    public void getBusinessName(String name){
        businessNameView.setText(name);
    }

    /**
     * download the image view of business, if not exist call to chooseImageFromGallery()
     */
    public void downloadImageView()
    {
        // get the byte array of the image from the storage
        StorageReference currentPhotoStorageReference = storageReference.
                child("imagesBusinesses/business" + businessNumber + ".jpg");

        ProgressDialog photoProgressDialog = new ProgressDialog(businessHomePage.this);
        photoProgressDialog.setMessage("Please Wait, Loading screen...");
        photoProgressDialog.show();

        final long TWENTY_MEGABYTE = 20 * 1024 * 1024;
        currentPhotoStorageReference.getBytes(TWENTY_MEGABYTE).
                addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // turn the byte array to bitmap and set the image view to this image
                Bitmap photoBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                photoProgressDialog.dismiss();
                businessPic.setImageBitmap(photoBitmap);
                // initialize the image view and text views and the photo and texts

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                // if there was an error so show toast and stop the dialog
                photoProgressDialog.dismiss(); // dismiss the progressDialog

                businessPic.setImageResource(R.drawable.ic_insert_photo_black_24dp); // change image resource
                uploadPic.setVisibility(View.VISIBLE);

                chooseImageFromGallery(); // if the business does not choose image already , he can choose now
            }
        });
    }


    /**
     * this function shows the positions list view of business
     * @param businessNumber
     */
    public void showListPositions(final int businessNumber){
        listViewPositions = (ListView) findViewById(R.id.listPositions);

        positionsAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, positionNameList);
        listViewPositions.setAdapter(positionsAdapter);
        listViewPositions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String finalPosition = String.valueOf(parent.getAdapter().getItem(position));
                intentPosition.putExtra("positionName", finalPosition);
                intentPosition.putExtra("username", username);
                intentPosition.putExtra("password", password);
                intentPosition.putExtra("businessNumber", businessNumber);
                startActivity(intentPosition);
            }
        });

    }

    /**
     * this function allows the user to get an image from his gallery to his profile
     */
    public void chooseImageFromGallery(){
        uploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uploadPic.getText().equals("choose image of your business")) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser
                            (intent, "Select Picture"), PICK_IMAGE_REQUEST);
                }
                else{
                    uploadToStorage();
                }

            }
        });
    }

    /**
     * when the user chose image, set the button to an upload to storage button
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                businessPic.setImageBitmap(bitmap);
                uploadPic.setText("save image");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * upload the image to storage in firebase
     */
    public void uploadToStorage(){
        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("imagesBusinesses/")
                    .child("business" + businessNumber + ".jpg");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(businessHomePage.this,
                                    "Uploaded", Toast.LENGTH_SHORT).show();
                            uploadPic.setVisibility(View.INVISIBLE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(businessHomePage.this,
                                    "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }
}
