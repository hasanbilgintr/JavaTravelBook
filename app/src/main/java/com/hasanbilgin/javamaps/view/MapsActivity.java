package com.hasanbilgin.javamaps.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.hasanbilgin.javamaps.R;
import com.hasanbilgin.javamaps.databinding.ActivityMapsBinding;
import com.hasanbilgin.javamaps.model.Place;
import com.hasanbilgin.javamaps.roomdb.PlaceDao;
import com.hasanbilgin.javamaps.roomdb.PlaceDatabase;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    ActivityResultLauncher<String> permissionLaouncher;
    LocationManager locationManager;
    LocationListener locationListener;
    SharedPreferences sharedPreferences;
    boolean info;
    PlaceDatabase db;
    PlaceDao placeDao;
    Double selectedLatitude;
    Double selectedLongitude;
    //????p torbas??n?? olu??turduk
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    Place selectedPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        registerLauncher();

        sharedPreferences = MapsActivity.this.getSharedPreferences("com.hasanbilgin.javamaps", MODE_PRIVATE);
        info = false;


        //databasemizi olu??turduk Database ad?? Places tir
        db = Room.databaseBuilder(getApplicationContext(), PlaceDatabase.class, "Places").build();
        placeDao = db.placeDao();

        selectedLatitude = 0.0;
        selectedLongitude = 0.0;


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    //harita haz??r oldu??unda ne yap??lcak metodu
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //uzun bas??lma i??in eklendi
        mMap.setOnMapLongClickListener(this);
        //gelen info verisi new ise demek
        if (getIntent().getStringExtra("info").equals("new")) {
            //g??sterilsin demek
            binding.saveButton.setVisibility(View.VISIBLE);
            //g??sterilmeyi b??rak gizlendi??i yerdede xmlden tamamen kald??r??lmas?? demektirki direk save butonu orayagelicektir
            binding.deleteButton.setVisibility(View.GONE);

            //        // Add a marker in Sydney and move the camera
//        //latitude ->enlem //longitude-> boylam demektir
//        LatLng sydney = new LatLng(-34, 151);
//        //enlem boylam nerden bak??l??yor bak??lcak g??sterilcek
//        //mark??r eklenmesi gerekiyormu?? kordinat vermi?? altta onu eklemi?? (i??aret??i eklemek)
//        //??st??ne t??klan??nca Marker in Sydney olucakt??r
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        //haritay?? oraya odaklanarak ba??lat
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

            //kordinatlar??n?? bulmak i??in maps.google.com da  bi eyfel kulesinin mark??r(t??klamak) land??????nda linkte yer al??r enlem48.85 boylam2.29 gibi mark??ra sa?? t??klarsanda g??r??rs??n yada mark??ra what is here da verir kordiyi

            //konum objsini tan??mlad??k  //s??n??f lokasyon y??neticisidir
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            //bir aray??zd??r //konum g??ncellendi??inde ??al????an bir aray??zd??r
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    //daha konum al??nmad??
                    //System.out.println("location: " + location);
                    //location.getAltitude()//y??kseklikmi??
                    info = sharedPreferences.getBoolean("info", false);

                    if (!info) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        sharedPreferences.edit().putBoolean("info", true).apply();
                    }
                }
            };
            //manifestte izin al??nmad??ysa burda kontrol eder
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //android kendi kontrol zorunlu ise...
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Snackbar.make(binding.getRoot(), "Permission needed for maps", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //izin rica etmek //request permission
                            permissionLaouncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                    });
                } else {
                    //izin rica etmek //request permission
                    permissionLaouncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            } else {
                //ilk 0 demek 0sn de konum g??ncelle demek //1000 olsayd?? 1sn bir g??ncellemek olurdu
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                //region en son konumu almak //hatta onLocationChanged metodu bo?? olursa en son konumu de??i??tirirsek tekrar ??al????t??rmada en son i??aretlenen yeri veriri
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                }
                //endregion
                //haritada mavi bir konumun orda oldu??u g??nstern bir simge g??sterir
                mMap.setMyLocationEnabled(true);
            }
            //g??ncel lokasyonumuzu al
            //locationManager.getCurrentLocation();
            //konum g??ncellemelerini almaya ba??la
            //locationManager.requestLocationUpdates();
            //konum g??ncellemelerini bir kere ba??la
            //locationManager.requestSingleUpdate();


            //48.858840278297876, 2.294325501892498
            //bu kod sadece o tarafa y??nelcektir mark??r koymucakt??r (addmarker olmad?????? zaman)
//        LatLng eiffel = new LatLng(48.858840278297876, 2.294325501892498);
//        mMap.addMarker(new MarkerOptions().position(eiffel).title("Eiffel Tower"));
//        //mMap.moveCamera(CameraUpdateFactory.newLatLng(eiffel));
//        //zoomlu y??nlendirebiliriiz onun i??in
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eiffel, 15));//zoom aral?????? 0 ile 25 aras??ndad??r

        } else {
            mMap.clear();
            selectedPlace = (Place) getIntent().getSerializableExtra("place");
            LatLng latLng=new LatLng(selectedPlace.latitude,selectedPlace.longitude);
            mMap.addMarker(new MarkerOptions().position(latLng).title(selectedPlace.name));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
            binding.placeNameEditText.setText(selectedPlace.name);
            //savebuttonu yokettik
            binding.saveButton.setVisibility(View.GONE);
            //g??r??n??r yapmak
            binding.deleteButton.setVisibility(View.VISIBLE);

//bu arada markera t??kalrsa sa?? altta navigasyon ????k??yo deneyebilirsiniz
            //uygulamay?? imzalay??p exini almay?? ve harita anahtarlar??n?? k??s??tlamay?? 12.videoda g??steriyo izleyebilirsiniz
        }



    }

    private void registerLauncher() {
        permissionLaouncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    //permissin granted // izin verildi
                    if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        //illa tekrar ifi istedi mecbur yazd??k
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                        //region en son konumu almak //hatta onLocationChanged metodu bo?? olursa en son konumu de??i??tirirsek tekrar ??al????t??rmada en son i??aretlenen yeri veriri
                        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (lastLocation != null) {
                            LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                        }
                        //endregion
                    }

                } else {
                    Toast.makeText(MapsActivity.this, "Permission Needed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    //haritada uzun bas??ld??????nda ??al????an metod
    public void onMapLongClick(@NonNull LatLng latLng) {
        //latLng kordinatt??r
        //mMap.clear() koyulan marker lar?? temizler
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));

        selectedLatitude = latLng.latitude;
        selectedLongitude = latLng.longitude;

        binding.saveButton.setEnabled(true);

    }

    public void saveButtonOnClick(View view) {

        //Room i??in Entity, Data Acces Object (DAO),Database s??n??f??

        Place place = new Place(binding.placeNameEditText.getText().toString(), selectedLatitude, selectedLongitude);
        //subscribeOn nerede bu i??lemi yap??y??m
        //observeOn nerede bu i??lemi g??zlemleyim

        //threading ->Main threading yada UI threading (ana tred) ,Defult (CPU Intensive) (listeyi dizmek gibi,arka arkaya i??lem yapmak gibi vs.) ,IO (network,database)

        //disposable//kullan at demek//rxjavada yapt??????m??z i??lemler (Completable lar,Flowable lar,Single lar , Obsorveler bunlar?? ????p torbas??na konulabiliyo)
        //

        //subscribe ??al????t??rma i??lemini yapar
        //MapsActivity.this::handleResponse metod referans verildi en son o metodu ??al????t??r??r
        // placeDao.insert(place) i??lemini yap ama bunu  Schedulers.io() tredde yap AndroidSchedulers.mainThread() de g??zlemlicem
        //
        compositeDisposable.add(placeDao.insert(place)
                .subscribeOn(Schedulers.io())
                // .observeOn(AndroidSchedulers.mainThread()) bu metot dinleme yada g??zlemeleme ihtiya?? olmad?????? yaz??lmayada bilir
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(MapsActivity.this::handleResponse)
        );

    }

    private void handleResponse() {
        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void deleteButtonOnClick(View view) {
        compositeDisposable.add(placeDao.delete(selectedPlace)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(MapsActivity.this::handleResponse)
        );
    }


    @Override
    //aktivite kapand??????nda ??al????an metot
    protected void onDestroy() {
        super.onDestroy();
        //????p torbas??n?? bo??altt??k/temizledik
        compositeDisposable.clear();
    }


}

