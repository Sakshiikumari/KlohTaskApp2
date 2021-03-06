package com.klohtaskapp.klohtaskapp.Activites;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.klohtaskapp.klohtaskapp.Models.EventDetailModel;
import com.klohtaskapp.klohtaskapp.Models.EventListCardModel;
import com.klohtaskapp.klohtaskapp.R;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.klohtaskapp.klohtaskapp.Utility.ApiClient;
import com.klohtaskapp.klohtaskapp.Utility.ApiInterface;
import com.klohtaskapp.klohtaskapp.Utility.CircleTransform;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.klohtaskapp.klohtaskapp.Activites.EventListActivity.isNetConnected;

public class EventDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap gmap;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String TAG ="API" ;
    String activityId = null;
    ImageView eventImageview;
    ImageView eventHostImageview,backButton;
    TextView eventTitle;
    TextView eventTime;
    TextView eventLocation;
    TextView eventHostName;
    TextView eventSummary;
    TextView eventDescription;

    ApiInterface apiService;
    private LocationManager locationManager;
    private LocationListener listerner;
    private LatLng mapsPoint;
    Double eventlattitude,eventlongitude;
    private GoogleMap mMap;
    private String extra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        apiService = ApiClient.getClient().create(ApiInterface.class);

        //default
        eventlattitude=12.926031;
        eventlongitude=77.676246;

        extra = getIntent().getStringExtra("activityId");
        getEventDetails(extra);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        backButton =findViewById(R.id.back_button_view);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        mapView = findViewById(R.id.map_view);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);


    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    void getEventDetails(final String activityId)
    {
        Call<JsonElement> call = apiService.getEventDetail(activityId);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement>call, Response<JsonElement> response) {

                JsonParser parser = new JsonParser();
                JsonObject object = (JsonObject) parser.parse(String.valueOf(response.body().getAsJsonObject()));


                String meventPicUrl =object.getAsJsonObject("response").getAsJsonObject().get("imageUrl")+"";
                String meventTitle=object.getAsJsonObject("response").get("title")+"";
                String meventHostPicture=object.getAsJsonObject("response").get("ownerProfileImageUrl")+"";
                String meventSummary=object.getAsJsonObject("response").get("summary")+"";
                String meventDescription=object.getAsJsonObject("response").get("description")+"";
                String meventTime = object.getAsJsonObject("response").getAsJsonObject("activityTime").get("activityDateString")+"";
                String meventHostName =object.getAsJsonObject("response").get("ownerName")+"";
                String meventLocation = object.getAsJsonObject("response").getAsJsonObject("location").get("name")+"";
                Double meventLat = object.getAsJsonObject("response").getAsJsonObject("location").get("lat").getAsDouble();
                Double meventLon = object.getAsJsonObject("response").getAsJsonObject("location").get("lon").getAsDouble();
                eventlattitude =meventLat;
                eventlongitude =meventLon;
                EventDetailModel eventData = new EventDetailModel(activityId,meventPicUrl,meventHostName,
                        meventSummary,meventDescription,meventHostPicture,meventTime,meventLat,meventLon,meventTitle,meventLocation);
               
                attachDatatoViews(eventData);
                Log.d(TAG, "Response "+ response.message());
            }

            @Override
            public void onFailure(Call<JsonElement>call, Throwable t) {
                // Log error here since request failed
                Log.e(TAG, t.toString());
            }
        });


    }

    private void attachDatatoViews(EventDetailModel eventData) {

        //attaching with views
        eventLocation = findViewById(R.id.event_location_view);
        eventSummary =findViewById(R.id.event_summary_view);
        eventDescription = findViewById(R.id.event_descrpition_view);
        eventTime =findViewById(R.id.event_time_view);
        eventHostName =findViewById(R.id.event_host_name_view);
        eventTitle = findViewById(R.id.event_title_view);

        eventImageview= findViewById(R.id.event_imageview);
        eventHostImageview=findViewById(R.id.event_host_image_view);

        eventTime.setText(eventData.getEventTime().replace("\"",""));
        eventHostName.setText(eventData.getEventHostName().replace("\"",""));
        eventDescription.setText(eventData.getEventDesription().replace("\"",""));
        eventSummary.setText(eventData.getEventSummary().replace("\"",""));
        eventLocation.setText("@"+eventData.getMeventLocation().replace("\"",""));
        eventTitle.setText(eventData.getMeventTitle().replace("\"",""));

        mapsPoint = new LatLng(eventData.getEventlattitue(), eventData.getEventlongitude());


        Log.d("picUrl",eventData.getEventPicture());
        Picasso.with(EventDetailActivity.this).load(eventData.getEventPicture().replace("\"","")).into(eventImageview);
        Picasso.with(EventDetailActivity.this).load(eventData.getEventHostPicture().replace("\"","")).transform(new CircleTransform()).into(eventHostImageview);


    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
      //setting marker on map
        mMap = googleMap;
        mMap.setMinZoomPreference(12);
        mMap.setIndoorEnabled(true);
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setIndoorLevelPickerEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setZoomControlsEnabled(true);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(eventlattitude,eventlongitude));
        mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(eventlattitude,eventlongitude)));

        //Net Connection Issue
        if (isNetConnected(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "Connected to Internet", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getApplicationContext(), "Not Connected to Internet", Toast.LENGTH_SHORT).show();

        }

    }
    @Override
    protected void onResume() {
        super.onResume();

        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
}
