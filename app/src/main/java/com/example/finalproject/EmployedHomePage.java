package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.Style;
//import com.mapbox.mapboxsdk.geometry.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.ColorUtils;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.sin;

public class EmployedHomePage extends AppCompatActivity {
    private TextView circleRadiusTextView;
    private SeekBar circleRadiusSeekBar;
    private Button submitButton;

    private SearchView searchAddress;
    Geocoder geocoder;
    List<Address> addresses;
    String customerAddress;

    private MapboxMap map;
    private MapView mapView;
    private Style styleStreet; // style of the map
    private String accessToken = "pk.eyJ1Ijoib3JpaGFtcmEiLCJhIjoiY2s5MDBiaTZ3MDB6cj" +
            "NycnJjYnhyMWpudiJ9.bNhRCx_4z-15jwpvLW-bZw";

    private FirebaseDatabase database;
    private DatabaseReference businessRef;
    private DatabaseReference employedRef;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private int numOfEmployeds = 0;
    private int numOfBusinesses = 0;
    private int numOfPositions = 0;
    private int numOfCandidate = 0;
    private int employedNumber;

    private double latEmployed;
    private double longEmployed;

    private String employedAddress;
    private String username;
    private String password;
    private String employedAge;
    private String employedEmail;
    private String employedGender;
    private String employedName;
    private String employedPhoneNumber;

    private String finalPosition;
    private String businessUserName;

    private ArrayList<String> businessAddressesList;
    private ArrayList<String> businessNameList;
    private ArrayList<String> businessUserNameList;
    private ArrayList<String> businessStatusList;

    private ArrayList<Double> businessLatitudesList;
    private ArrayList<Double> businessLongitudesList;

    private ArrayList<String> positionNameList;
    private ArrayAdapter<String> positionsAdapter;

    private ArrayList<String> usernameList;
    private ArrayList<String> passwordList;
    private ArrayList<Employed> employedListFromData;

    private ArrayList<String> positionsGeneralList; // position list to the searchView

    private int circleRadius;
    private Icon iconBlue;

    private int numOfEmployedsToRate; // number of employees to rate
    private ArrayList<Double> ratingList; // list of rating
    private double average; // final rate bu calculate average
    private RatingBar stars; // the rating bar in business profile dialog

    public static final String SHARED_PREFS = "sharedPrefs"; //name of shared prefs
    public static final String KEY_REMEMBER = "rememberMe"; // shared prefs value to save - remember me

    ProgressDialog mapDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, accessToken);

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_employed_home_page);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");

        database = FirebaseDatabase.getInstance();
        businessRef = database.getReference().child("Businesses");
        employedRef = database.getReference().child("Employeds");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        businessAddressesList = new ArrayList<>();
        businessNameList = new ArrayList<>();
        businessUserNameList = new ArrayList<>();
        businessStatusList = new ArrayList<>();

        businessLatitudesList = new ArrayList<>();
        businessLongitudesList = new ArrayList<>();

        usernameList = new ArrayList<>();
        passwordList = new ArrayList<>();
        employedListFromData = new ArrayList<>();

        positionsGeneralList = new ArrayList<>();

        ratingList = new ArrayList<>();


        createMapBox();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.employed_menu, menu);
        return true;
    }

    /**
     * this function responsible the menu in this activity
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.businesses_menu:
                clickOnBusinesses();
                return true;

            case R.id.log_out_employed:
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
     * open confirmed businesses activity
     */
    public void clickOnBusinesses(){
        Intent intent = new Intent(EmployedHomePage.this, confirmedBusinessesOfEmployed.class);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        startActivity(intent);
    }


    /**
     * create map box mapview and set style
     */
    public void createMapBox(){
        mapDialog = new ProgressDialog(EmployedHomePage.this);
        mapDialog.setMessage("Please Wait, Loading screen...");
        mapDialog.show();
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                map = mapboxMap;
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        styleStreet = style;
                        ReadEmployedAddress();
                        ReadFromDatabaseAddresses();
                        getCustomerLatLng(); // get customer searchView address
                    }
                });
            }
        });
    }

    /**
     * this function gets address and do geo coding - address to LatLng
     * @param strAddress
     * @return
     */
    public double[] getLocationFromAddress(String strAddress){
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        double latitude = 0.0;
        double longitude = 0.0;

        try {
            address = coder.getFromLocationName(strAddress,1);
            if (address.size() ==0) {
                return null;
            }
            Address location=address.get(0);
            latitude = location.getLatitude();
            longitude = location.getLongitude();

        } catch (Exception e){
            e.printStackTrace();
        }

        double[] latlangArr = {latitude, longitude};
        return latlangArr;
    }


    /**
     * read from real time data base employees list in order to recognize the user in database
     */
    public void ReadEmployedAddress(){
        employedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numOfEmployeds = (int) dataSnapshot.getChildrenCount();
                for (int i =1; i<= numOfEmployeds; i++){
                    String usernameFromData = ((String) dataSnapshot.child("employed " + i).
                            child("userName").getValue());
                    usernameList.add(usernameFromData);
                    String passwordFromData = ((String) dataSnapshot.child("employed " + i).
                            child("password").getValue());
                    passwordList.add(passwordFromData);
                }
                recognizeEmployed();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // when there is an error (no internet, no signal)
                AlertDialog.Builder connectError = new AlertDialog.Builder(EmployedHomePage.this).
                        setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                connectError.show();
            }
        });
    }

    /**
     * recognize the user  - employedNumber
     */
    public void recognizeEmployed(){
        System.out.println(numOfEmployeds);
        for (int i=0; i<numOfEmployeds;i++){
            String usernameData = usernameList.get(i);
            String passwordData = passwordList.get(i);
            if (username.equals(usernameData) && password.equals(passwordData)){
                employedNumber = i+1;
                useEmployedDeatails();
            }
        }
    }

    /**
     * get employed values from data base and add his marker
     */
    public void useEmployedDeatails(){
        employedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                employedAddress = ((String) dataSnapshot.child("employed " + employedNumber).
                        child("address").getValue());

                employedAge = String.valueOf((long) dataSnapshot.child("employed " + employedNumber).
                        child("age").getValue());

                employedEmail = ((String) dataSnapshot.child("employed " + employedNumber).
                        child("email").getValue());

                employedGender = ((String) dataSnapshot.child("employed " + employedNumber).
                        child("gender").getValue());

                employedName = ((String) dataSnapshot.child("employed " + employedNumber).
                        child("name").getValue());

                employedPhoneNumber = ((String) dataSnapshot.child("employed " + employedNumber).
                        child("phoneNumber").getValue());

                addEmployedMarker();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // when there is an error (no internet, no signal)
                AlertDialog.Builder connectError = new AlertDialog.Builder(EmployedHomePage.this).
                        setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                connectError.show();
            }
        });
    }

    /**
     * add employed marker to mapview with geocoding and blue icon
     */
    public void addEmployedMarker(){
        MarkerOptions options = new MarkerOptions();

        latEmployed = getLocationFromAddress(employedAddress)[0];
        longEmployed = getLocationFromAddress(employedAddress)[1];

        LatLng employedLatLng = new LatLng(latEmployed, longEmployed);
        options.position(employedLatLng);

        IconFactory iconFactory = IconFactory.getInstance(EmployedHomePage.this);
        iconBlue = iconFactory.fromResource(R.drawable.blue_marker);
        options.setIcon(iconBlue);
        options.setTitle("my address");
        map.addMarker(options);
        animateCameraToPlace(employedLatLng);

        defineSeekBar();
        mapDialog.dismiss(); // map dialog dismiss
    }

    /**
     * read businesses from data base to the the screen with the values and add markers
     * of businesses to mapview
     */
    public void ReadFromDatabaseAddresses(){
        businessRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numOfBusinesses = (int) dataSnapshot.getChildrenCount();
                for (int i = 1; i <= numOfBusinesses; i++) {
                    String businessPlaceFromData = ((String) dataSnapshot.child("business " + i).
                            child("businessPlace").getValue());
                    businessAddressesList.add(businessPlaceFromData);

                    String businessNameFromData = ((String) dataSnapshot.child("business " + i).
                            child("businessName").getValue());
                    businessNameList.add(businessNameFromData);

                    String businessUserNameFromData = ((String) dataSnapshot.child("business " + i).
                            child("userName").getValue());
                    businessUserNameList.add(businessUserNameFromData);

                    String businessStatusFromData = ((String) dataSnapshot.child("business " + i).
                            child("status").getValue());
                    businessStatusList.add(businessStatusFromData);

                    double latitudeFromData = ((double) dataSnapshot.child("business " + i).
                            child("location").child("latitude").getValue());
                    businessLatitudesList.add(latitudeFromData);

                    double longitudeFromData = ((double) dataSnapshot.child("business " + i).
                            child("location").child("longitude").getValue());
                    businessLongitudesList.add(longitudeFromData);

                    // add to positions list witch subject is exist
                    int numOfPositions = (int) dataSnapshot.child("business " +i).
                            child("positions").getChildrenCount();
                    for (int j =0; j<numOfPositions;j++){
                        String positionNameGeneral = ((String) dataSnapshot.
                                child("business " + i).child("positions").child(String.valueOf(j)).
                                child("positionName").getValue());
                        positionsGeneralList.add(positionNameGeneral);
                    }

                }
                addMarkers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // when there is an error (no internet, no signal)
                AlertDialog.Builder connectError = new AlertDialog.Builder(EmployedHomePage.this).
                        setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                connectError.show();
            }
        });
    }


    /**
     * add markers to the mapview if the status is open to request
     */
    public void addMarkers(){
        for (double latitude:businessLatitudesList){
            double longitude = businessLongitudesList.get(businessLatitudesList.indexOf(latitude));
            String status = businessStatusList.get(businessLatitudesList.indexOf(latitude));

            int number = businessLatitudesList.indexOf(latitude) + 1;
            System.out.println("business " + number);
            System.out.println("business status- " + status);

            if (status.equals("open")){
                MarkerOptions options = new MarkerOptions();
                options.position(new LatLng(latitude, longitude));
                map.addMarker(options);
            }
        }
        onClickMarker();
    }

    /**
     * click on marker - build alert dialog with business profile.
     */
    public void onClickMarker(){
        map.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                positionNameList = new ArrayList<>();
                LatLng latLng = new LatLng(latEmployed, longEmployed);
                if (!marker.getPosition().equals(latLng)) {
                    buildLayoutAlertBusiness(marker);
                }
                return false;
            }
        });
    }

    /**
     * add business deatails to his profile - with the image and business positions
     * @param marker
     */
    public void buildLayoutAlertBusiness(Marker marker){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(EmployedHomePage.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_position_description, null);
        TextView businessNameTextView = view.findViewById(R.id.textViewBusinessName);
        TextView businessPlaceTextView = view.findViewById(R.id.textViewBusinessPlace);
        ImageView businessImageView = view.findViewById(R.id.businessImage);
        ListView positionListView = view.findViewById(R.id.positionListView);
        stars = view.findViewById(R.id.starsBusiness);
        TextView distanceTextView = view.findViewById(R.id.distanceTextView);

        showDistanceToMarker(marker, distanceTextView); // show distance in distance text view

        for (int i=0; i< numOfBusinesses; i++){
            String address = businessAddressesList.get(i);
            String businessName = businessNameList.get(i);
            LatLng latLng = new LatLng(businessLatitudesList.get(i), businessLongitudesList.get(i));
            if (marker.getPosition().equals(latLng)){

                businessPlaceTextView.setText(businessPlaceTextView.getText() + address);
                businessNameTextView.setText(businessNameTextView.getText() + businessName);

                businessUserName = businessUserNameList.get(i); // for the service

                int businessNumber = i+1;

                Toast loadingImageToast = Toast.makeText(EmployedHomePage.this,
                        "business image is loading",Toast.LENGTH_SHORT);
                loadingImageToast.setGravity(Gravity.CENTER, 10, 10);
                loadingImageToast.show();

                downloadImageView(businessImageView, businessNumber); // set image view of business

                ReadClickPositionName(businessNumber);

                positionsAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1, positionNameList);
                // set list of image view
                positionListView.setAdapter(positionsAdapter);
                clickOnPosition(positionListView, businessNumber);
            }
        }

        alertBuilder.setView(view);
        AlertDialog dialog = alertBuilder.create();
        dialog.show();

    }

    /**
     * get all of the rate attributes of the clicked business
     * employees to calculate the average.
     * @param businessNumber
     */
    public void getStarsOfBusiness(int businessNumber){
        businessRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                System.out.println("numOfPosition " + numOfPositions);
                for (int i=0; i<numOfPositions;i++){
                    numOfEmployedsToRate = (int) dataSnapshot.child("business " +businessNumber).
                            child("positions").child(String.valueOf(i)).child("employedList").getChildrenCount();

                    System.out.println("numOfEmployeesToRate " + numOfEmployedsToRate);

                    for (int j=0; j<numOfEmployedsToRate;j++){

                        if (dataSnapshot.child("business " +businessNumber).
                                child("positions").child(String.valueOf(i)).child("employedList").
                                child(String.valueOf(j)).child("rate").getValue() != null){

                            double rateOfEmployed = Double.parseDouble(String.valueOf(dataSnapshot.
                                    child("business " +businessNumber).
                                    child("positions").child(String.valueOf(i)).child("employedList").
                                    child(String.valueOf(j)).child("rate").getValue()));

                            if (rateOfEmployed != 0) {
                                // do not add default rate that equals to 0 to the rating
                                System.out.println("rateOfEmployed " + rateOfEmployed);
                                ratingList.add((double) rateOfEmployed);
                            }
                        }
                    }
                }
                calculateAverageOfStars();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // when there is an error (no internet, no signal)
                AlertDialog.Builder connectError = new AlertDialog.Builder(EmployedHomePage.this).
                        setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                connectError.show();
            }
        });
    }

    /**
     * calculate the average of rating employees to show it in
     * rating bar
     */
    public void calculateAverageOfStars(){
        double sum = 0;
        for (double rate:ratingList){
            sum += rate;
        }
        average = sum / ratingList.size();
        stars.setRating((float) average);
        System.out.println("average " + average);
    }


    /**
     * get position name list of business to set the list view
     * @param businessNumber
     */
    public void ReadClickPositionName(int businessNumber){
        businessRef.addValueEventListener(new ValueEventListener() {
            // to read positions names of business to the list view
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numOfPositions = (int) dataSnapshot.child("business " +businessNumber).
                        child("positions").getChildrenCount();
                for (int j =0 ; j < numOfPositions; j++){
                    // read position features
                    String positionNameFromData = ((String)dataSnapshot.
                            child("business " + businessNumber).child("positions").
                            child(String.valueOf(j)).child("positionName").getValue());
                    positionNameList.add(positionNameFromData);
                    // save all positions that exist to the searchView
                }
                ratingList = new ArrayList<>(); // reset the list
                getStarsOfBusiness(businessNumber);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // when there is an error (no internet, no signal)
                AlertDialog.Builder connectError = new AlertDialog.Builder(EmployedHomePage.this).
                        setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                connectError.show();
            }
        });
    }

    /**
     * download image view from storage firebase
     * @param imageView
     * @param businessNumber
     */
    public void downloadImageView(ImageView imageView, int businessNumber)
    {
        // get the byte array of the image from the storage
        StorageReference currentPhotoStorageReference = storageReference.
                child("imagesBusinesses/business" + businessNumber + ".jpg");

        final long TWENTY_MEGABYTE = 20 * 1024 * 1024;
        currentPhotoStorageReference.getBytes(TWENTY_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // turn the byte array to bitmap and set the image view to this image
                Bitmap photoBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                // initialize the image view and text views and the photo and texts
                System.out.println("set image");
                imageView.setImageBitmap(photoBitmap);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // if there was an error so show toast and stop the dialog
                Toast hasNotImageToast = Toast.makeText(EmployedHomePage.this,
                        "the business has not have an image",Toast.LENGTH_LONG);
                hasNotImageToast.setGravity(Gravity.CENTER, 10, 10);
                hasNotImageToast.show();
            }
        });
    }

    /**
     * show values of position when click on it - send request / decline
     * @param positionListView
     * @param businessNumber
     */
    public void clickOnPosition(ListView positionListView, int businessNumber){
        positionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                finalPosition = String.valueOf(parent.getAdapter().getItem(position));
                System.out.println("finalPosition " + finalPosition);
                // new alert of position
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(EmployedHomePage.this);
                View viewValue = getLayoutInflater().inflate(R.layout.dialog_position_values, null);
                TextView positionNameTextView = viewValue.findViewById(R.id.textViewPositionName);
                TextView scopeJobTextView =  viewValue.findViewById(R.id.textViewScopeJob);
                TextView fromAgeTextView =  viewValue.findViewById(R.id.textViewFromAge);
                TextView hourSalaryTextView =  viewValue.findViewById(R.id.textViewSalary);
                TextView shiftTypesTextView =  viewValue.findViewById(R.id.textViewShifts);
                TextView trailTextView =  viewValue.findViewById(R.id.textViewTrail);

                // read from database the position deatails
                int positionNumber = positionNameList.indexOf(finalPosition);
                businessRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        System.out.println("businessNumber " + businessNumber);
                        System.out.println("positionNumber " + positionNumber);

                        String positionNameFromData = ((String) dataSnapshot.
                                child("business " + businessNumber).child("positions").
                                child(String.valueOf(positionNumber)).child("positionName").getValue());
                        positionNameTextView.setText(positionNameTextView.getText() + positionNameFromData);

                        String scopeJobFromData = ((String) dataSnapshot.
                                child("business " + businessNumber).child("positions").
                                child(String.valueOf(positionNumber)).child("scopeJob").getValue());
                        scopeJobTextView.setText(scopeJobTextView.getText() + scopeJobFromData);

                        long ageFromData = ((long)dataSnapshot.
                                child("business " + businessNumber).child("positions").
                                child(String.valueOf(positionNumber)).child("age").getValue());
                        fromAgeTextView.setText(fromAgeTextView.getText() + String.valueOf(ageFromData));

                        long hourSalaryFromData = ((long) dataSnapshot.
                                child("business " + businessNumber).child("positions").
                                child(String.valueOf(positionNumber)).child("hourSalary").getValue());
                        hourSalaryTextView.setText(hourSalaryTextView.getText() + String.valueOf(hourSalaryFromData));

                        int numberOfShifts = (int) dataSnapshot.
                                child("business " + businessNumber).child("positions").
                                child(String.valueOf(positionNumber)).child("shiftTypes").getChildrenCount();

                        for (int i=0;i<numberOfShifts;i++){
                            String shiftFromData = ((String) dataSnapshot.
                                    child("business " + businessNumber).child("positions").
                                    child(String.valueOf(positionNumber)).child("shiftTypes").
                                    child(String.valueOf(i)).getValue());
                            shiftTypesTextView.setText(shiftTypesTextView.getText() + shiftFromData);
                            if (i != numberOfShifts-1){
                                shiftTypesTextView.setText(shiftTypesTextView.getText() + ", ");
                            }
                        }

                        String trailFromData = ((String) dataSnapshot.
                                child("business " + businessNumber).child("positions").
                                child(String.valueOf(positionNumber)).child("trail").getValue());
                        trailTextView.setText(trailTextView.getText() + trailFromData);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // when there is an error (no internet, no signal)
                        AlertDialog.Builder connectError = new AlertDialog.Builder(EmployedHomePage.this).
                                setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                        connectError.show();
                    }
                });


                alertBuilder.setNegativeButton("decline", null);
                alertBuilder.setPositiveButton("send request", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(EmployedHomePage.this, "request sent",
                                Toast.LENGTH_SHORT).show();
                        ratingList = new ArrayList<>(); // reset the list
                        ReadCandidateList(businessNumber, positionNumber);
                    }
                });
                alertBuilder.setView(viewValue);
                AlertDialog dialog = alertBuilder.create();
                dialog.show();
            }
        });
    }

    /**
     * read employedList from entered position , if does not exist create new key and add,
     * if exist add to the list.
     * @param businessNumber
     * @param positionNumber
     */
    public void ReadCandidateList(int businessNumber, int positionNumber){
        String employedStatus = "defaultStatus";
        double employedRate = 0; //default rate of employed to his business
        Employed employed = new Employed(username, password, employedName, employedEmail,
                Double.parseDouble(employedAge), employedGender, employedPhoneNumber,
                employedAddress, employedStatus, employedRate);
        businessRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // read employedList from entered position , if does not exist create new key and add,
                // if exist add to the list.
                numOfCandidate = (int) dataSnapshot.child("business " +businessNumber).
                        child("positions").child(String.valueOf(positionNumber)).child("employedList").
                        getChildrenCount();
                for (int i=0;i<numOfCandidate;i++){

                    String addressEmployedFromData = (String) dataSnapshot.
                            child("business " +businessNumber).child("positions").
                            child(String.valueOf(positionNumber)).child("employedList").
                            child(String.valueOf(i)).child("address").getValue();

                    String ageEmployedFromData = String.valueOf ((long) dataSnapshot.
                            child("business " +businessNumber).child("positions").
                            child(String.valueOf(positionNumber)).child("employedList").
                            child(String.valueOf(i)).child("age").getValue());

                    String emailEmployedFromData = (String) dataSnapshot.
                            child("business " +businessNumber).child("positions").
                            child(String.valueOf(positionNumber)).child("employedList").
                            child(String.valueOf(i)).child("email").getValue();

                    String genderEmployedFromData = (String) dataSnapshot.
                            child("business " +businessNumber).child("positions").
                            child(String.valueOf(positionNumber)).child("employedList").
                            child(String.valueOf(i)).child("gender").getValue();

                    String nameEmployedFromData = (String) dataSnapshot.
                            child("business " +businessNumber).child("positions").
                            child(String.valueOf(positionNumber)).child("employedList").
                            child(String.valueOf(i)).child("name").getValue();

                    String passwordEmployedFromData = (String) dataSnapshot.
                            child("business " +businessNumber).child("positions").
                            child(String.valueOf(positionNumber)).child("employedList").
                            child(String.valueOf(i)).child("password").getValue();

                    String phoneEmployedFromData = (String) dataSnapshot.
                            child("business " +businessNumber).child("positions").
                            child(String.valueOf(positionNumber)).child("employedList").
                            child(String.valueOf(i)).child("phoneNumber").getValue();

                    String usernameEmployedFromData = (String) dataSnapshot.
                            child("business " +businessNumber).child("positions").
                            child(String.valueOf(positionNumber)).child("employedList").
                            child(String.valueOf(i)).child("userName").getValue();


                    employedListFromData.add(new Employed(usernameEmployedFromData, passwordEmployedFromData,
                            nameEmployedFromData, emailEmployedFromData, Double.parseDouble(ageEmployedFromData),
                            genderEmployedFromData, phoneEmployedFromData, addressEmployedFromData, employedStatus, employedRate));

                }
                addCandidateToDatabase(businessNumber, positionNumber, employed, employedListFromData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // when there is an error (no internet, no signal)
                AlertDialog.Builder connectError = new AlertDialog.Builder(EmployedHomePage.this).
                        setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                connectError.show();
            }
        });
    }

    /**
     * add employed to employedList in database in the wanted reference
     * @param businessNumber
     * @param positionNumber
     * @param employed
     * @param employedArrayList
     */
    public void addCandidateToDatabase(int businessNumber, int positionNumber, Employed employed,
                                       ArrayList<Employed> employedArrayList) {
        boolean exist = false;
        for (int i=0; i<numOfCandidate; i++){
            if (employed.toString().equals(employedArrayList.get(i).toString())){
                // to check if employedArrayList contains employed
                exist = true;
            }
        }
        if (!exist) {
            businessRef.child("business " + businessNumber).child("positions").child(String.valueOf(positionNumber))
                    .child("employedList").child(String.valueOf(numOfCandidate++)).setValue(employed);
            employedArrayList.add(employed); // add to java employedArrayList, when i dons=d read from data base
        }
    }

    /**
     * define seek bar to set radius - to draw polygon rounded employed address
     */
    public void defineSeekBar(){
        circleRadiusSeekBar = findViewById(R.id.circleRadiusSeekBar);
        circleRadiusTextView = findViewById(R.id.radiusTextView);
        submitButton = findViewById(R.id.submitButton);

        circleRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                showRadiusSeekBar(progress, circleRadiusTextView);
                circleRadius = progress;
                submitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LatLng latLng = new LatLng(latEmployed, longEmployed);
                        drawCircle(latLng ,circleRadius);
                    }
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progressValue = seekBar.getProgress();
                showRadiusSeekBar(progressValue, circleRadiusTextView);
                circleRadius = progressValue;
                submitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LatLng latLng = new LatLng(latEmployed, longEmployed);
                        if (map.getPolylines().size() > 0) {
                            map.removePolyline(map.getPolylines().get(0)); // delete polyline from map
                        }
                        drawCircle(latLng ,circleRadius);
                    }
                });
            }
        });
    }

    /**
     * this function create the circle and draw it around the address of employed
     * @param position
     * @param radiusMeters
     */
    public void drawCircle(LatLng position, double radiusMeters) {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.width(3); // change the line width here
        polylineOptions.addAll(getCirclePoints(position, radiusMeters));
        map.addPolyline(polylineOptions);
    }

    /**
     * this function calculate the circle with the radius
     * @param position
     * @param radius
     * @return polygon
     */
    private ArrayList<LatLng> getCirclePoints(LatLng position, double radius) {
        int degreesBetweenPoints = 10; // change here for shape
        int numberOfPoints = (int) Math.floor(360 / degreesBetweenPoints);
        double distRadians = radius / 6371000.0; // earth radius in meters
        double centerLatRadians = position.getLatitude() * Math.PI / 180;
        double centerLonRadians = position.getLongitude() * Math.PI / 180;
        ArrayList<LatLng> polygons = new ArrayList<>(); // array to hold all the points
        for (int index = 0; index < numberOfPoints; index++) {
            double degrees = index * degreesBetweenPoints;
            double degreeRadians = degrees * Math.PI / 180;
            double pointLatRadians = Math.asin(sin(centerLatRadians) * cos(distRadians)
                    + cos(centerLatRadians) * sin(distRadians) * cos(degreeRadians));
            double pointLonRadians = centerLonRadians + Math.atan2(sin(degreeRadians)
                            * sin(distRadians) * cos(centerLatRadians),
                    cos(distRadians) - sin(centerLatRadians) * sin(pointLatRadians));
            double pointLat = pointLatRadians * 180 / Math.PI;
            double pointLon = pointLonRadians * 180 / Math.PI;
            LatLng point = new LatLng(pointLat, pointLon);
            polygons.add(point);
        }
        // add first point at end to close circle
        polygons.add(polygons.get(0));
        return polygons;
    }


    /**
     * this function calculate the distance between employed address to marker that clicked
     * @param marker
     * @return distanceInMeters
     */

    public int[] distanceBetweenEmployedToMarker(Marker marker){
        Location loc1 = new Location("");
        loc1.setLatitude(latEmployed);
        loc1.setLongitude(longEmployed);

        Location loc2 = new Location("");
        loc2.setLatitude(marker.getPosition().getLatitude());
        loc2.setLongitude(marker.getPosition().getLongitude());

        float distanceInMeters = loc1.distanceTo(loc2);
        int finalDistance = Integer.valueOf((int) distanceInMeters);
        int [] arrayDistance = convertMetersToKilometers(finalDistance);

        return arrayDistance;
    }

    public int[] convertMetersToKilometers(int distance){
       int numKm = distance / 1000;
       int numMeter = distance % 1000;

       int [] arrayDistance = {numKm, numMeter};
       return arrayDistance;
    }

    public void showDistanceToMarker(Marker marker, TextView distanceTextView){
        int kilometer = distanceBetweenEmployedToMarker(marker)[0]; // get kilometer of distance
        int meter = distanceBetweenEmployedToMarker(marker)[1]; // get meter of distance
        if (kilometer == 0){
            distanceTextView.setText("distance from your address is: " + meter + " meters");
        }
        else if (meter == 0){
            distanceTextView.setText("distance from your address is: " + kilometer + " kilometers");
        }
        else{
            distanceTextView.setText("distance from your address is: " +
                    kilometer + " kilometers and " + meter + " meters");
        }
    }

    public void showRadiusSeekBar(int distance, TextView distanceTextView){
        int kilometer = convertMetersToKilometers(distance)[0]; // get kilometer of radius
        int meter = convertMetersToKilometers(distance)[1]; // // get meter of radius

        if (kilometer == 0){
            distanceTextView.setText("your address circle radius is " + meter + " meters");
        }
        else if (meter == 0){
            distanceTextView.setText("your address circle radius is " +
                    kilometer + " kilometers");
        }
        else{
            distanceTextView.setText("your address circle radius is " +
                    kilometer + " kilometers and " + meter + " meters");
        }
    }

    /**
     * this function find the searched address. if the customer
     * choose to find a subject(position) instead of address
     * the function call setSearchToPosition(customerAddress),
     * customerAddress - is the searched subject.
     */
    public void getCustomerLatLng() {
        LatLng latLng = new LatLng();
        searchAddress = findViewById(R.id.searchAddress);
        searchAddress.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                customerAddress = query;
                if (getLocationFromAddress(customerAddress) == null){

                    businessAddressesList = new ArrayList<>(); // to reset the value
                    businessNameList = new ArrayList<>(); // to reset the value
                    businessUserNameList = new ArrayList<>(); // to reset the value
                    businessStatusList = new ArrayList<>(); //to reset the value
                    businessLatitudesList = new ArrayList<>(); //to reset the value
                    businessLongitudesList = new ArrayList<>(); //to reset the value


                    setSearchToPosition(customerAddress); // customerAddress turn to customer position that searched
                }
                else {
                    latLng.setLatitude(getLocationFromAddress(customerAddress)[0]);
                    latLng.setLongitude(getLocationFromAddress(customerAddress)[1]);
                    animateCameraToPlace(latLng);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals("")){
                    addMarkers(); // if the customer delete his search return the map to the regular view
                }
                return false;
            }
        });
    }

    /**
     * this function turn the searchAddress to searchPosition, if the subject(position)
     * not exist in the system Toast will write "not found result".
     * if the subject exist i will remove all the businesses markers from the map and
     * add only the businesses who has the position.
     * @param customerSearch
     */
    public void setSearchToPosition(String customerSearch){
        if (!positionsGeneralList.contains(customerSearch)){
            AlertDialog.Builder notFoundDialog = new AlertDialog.Builder(EmployedHomePage.this)
                    .setMessage("Profession not found. sorry there is no business that offers this profession")
                    .setPositiveButton("ok", null);
            notFoundDialog.show();
        }
        else {
            // delete all businesses that does not have the searched position
            for (Marker marker: map.getMarkers()){
                if (!marker.getIcon().equals(iconBlue)){
                    marker.remove();
                }
            }
            // read the businesses deatails again from data base.
            businessRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    numOfBusinesses = (int) dataSnapshot.getChildrenCount();
                    for (int i = 1; i <= numOfBusinesses; i++) {
                        String businessPlaceFromData = ((String) dataSnapshot.
                                child("business " + i).child("businessPlace").getValue());
                        businessAddressesList.add(businessPlaceFromData);
                        String businessNameFromData = ((String) dataSnapshot.
                                child("business " + i).child("businessName").getValue());
                        businessNameList.add(businessNameFromData);
                        String businessUserNameFromData = ((String) dataSnapshot.
                                child("business " + i).child("userName").getValue());
                        businessUserNameList.add(businessUserNameFromData);
                        String businessStatusFromData = ((String) dataSnapshot.
                                child("business " + i).child("status").getValue());
                        businessStatusList.add(businessStatusFromData);
                        double latitudeFromData = ((double) dataSnapshot.
                                child("business " + i).child("location").child("latitude").getValue());
                        businessLatitudesList.add(latitudeFromData);
                        double longitudeFromData = ((double) dataSnapshot.
                                child("business " + i).child("location").child("longitude").getValue());
                        businessLongitudesList.add(longitudeFromData);

                        // add to positions list witch subject is exist
                        int numOfPositions = (int) dataSnapshot.child("business " +i).
                                child("positions").getChildrenCount();
                        for (int j =0; j<numOfPositions;j++){
                            String positionNameGeneral = ((String) dataSnapshot.
                                    child("business " + i).child("positions").
                                    child(String.valueOf(j)).child("positionName").getValue());
                            String subject_position = customerSearch;
                            if (subject_position.equals(positionNameGeneral)) {
                                // if the subject(position) is equal to the searched position add the business marker.
                                addMarkerOfSearchView(latitudeFromData, longitudeFromData);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // when there is an error (no internet, no signal)
                    AlertDialog.Builder connectError = new AlertDialog.Builder(EmployedHomePage.this)
                            .setMessage(databaseError.getMessage()).setPositiveButton("ok", null);
                    connectError.show();
                }
            });

        }
    }

    /**
     * this function add marker to map
     * @param latitude
     * @param longitude
     */
    public void addMarkerOfSearchView(double latitude, double longitude){
        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(latitude, longitude));
        map.addMarker(options);
        onClickMarker();
    }



    /**
     * this function moves the app camera to the location that searched
     */
    public void animateCameraToPlace(LatLng latLng){
        // Move map camera to the selected location
        map.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(12)
                        .bearing(0) // bearing is 0 north
                        .build()), 3000);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
