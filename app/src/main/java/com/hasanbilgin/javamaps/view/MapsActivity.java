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
    //çöp torbasını oluşturduk
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


        //databasemizi oluşturduk Database adı Places tir
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
    //harita hazır olduğunda ne yapılcak metodu
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //uzun basılma için eklendi
        mMap.setOnMapLongClickListener(this);
        //gelen info verisi new ise demek
        if (getIntent().getStringExtra("info").equals("new")) {
            //gösterilsin demek
            binding.saveButton.setVisibility(View.VISIBLE);
            //gösterilmeyi bırak gizlendiği yerdede xmlden tamamen kaldırılması demektirki direk save butonu orayagelicektir
            binding.deleteButton.setVisibility(View.GONE);

            //        // Add a marker in Sydney and move the camera
//        //latitude ->enlem //longitude-> boylam demektir
//        LatLng sydney = new LatLng(-34, 151);
//        //enlem boylam nerden bakılıyor bakılcak gösterilcek
//        //markır eklenmesi gerekiyormuş kordinat vermiş altta onu eklemiş (işaretçi eklemek)
//        //üstüne tıklanınca Marker in Sydney olucaktır
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        //haritayı oraya odaklanarak başlat
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

            //kordinatlarını bulmak için maps.google.com da  bi eyfel kulesinin markır(tıklamak) landığında linkte yer alır enlem48.85 boylam2.29 gibi markıra sağ tıklarsanda görürsün yada markıra what is here da verir kordiyi

            //konum objsini tanımladık  //sınıf lokasyon yöneticisidir
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            //bir arayüzdür //konum güncellendiğinde çalışan bir arayüzdür
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    //daha konum alınmadı
                    //System.out.println("location: " + location);
                    //location.getAltitude()//yükseklikmiş
                    info = sharedPreferences.getBoolean("info", false);

                    if (!info) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        sharedPreferences.edit().putBoolean("info", true).apply();
                    }
                }
            };
            //manifestte izin alınmadıysa burda kontrol eder
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
                //ilk 0 demek 0sn de konum güncelle demek //1000 olsaydı 1sn bir güncellemek olurdu
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                //region en son konumu almak //hatta onLocationChanged metodu boş olursa en son konumu değiştirirsek tekrar çalıştırmada en son işaretlenen yeri veriri
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                }
                //endregion
                //haritada mavi bir konumun orda olduğu gönstern bir simge gösterir
                mMap.setMyLocationEnabled(true);
            }
            //güncel lokasyonumuzu al
            //locationManager.getCurrentLocation();
            //konum güncellemelerini almaya başla
            //locationManager.requestLocationUpdates();
            //konum güncellemelerini bir kere başla
            //locationManager.requestSingleUpdate();


            //48.858840278297876, 2.294325501892498
            //bu kod sadece o tarafa yönelcektir markır koymucaktır (addmarker olmadığı zaman)
//        LatLng eiffel = new LatLng(48.858840278297876, 2.294325501892498);
//        mMap.addMarker(new MarkerOptions().position(eiffel).title("Eiffel Tower"));
//        //mMap.moveCamera(CameraUpdateFactory.newLatLng(eiffel));
//        //zoomlu yönlendirebiliriiz onun için
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eiffel, 15));//zoom aralığı 0 ile 25 arasındadır

        } else {
            mMap.clear();
            selectedPlace = (Place) getIntent().getSerializableExtra("place");
            LatLng latLng=new LatLng(selectedPlace.latitude,selectedPlace.longitude);
            mMap.addMarker(new MarkerOptions().position(latLng).title(selectedPlace.name));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
            binding.placeNameEditText.setText(selectedPlace.name);
            //savebuttonu yokettik
            binding.saveButton.setVisibility(View.GONE);
            //görünür yapmak
            binding.deleteButton.setVisibility(View.VISIBLE);

//bu arada markera tıkalrsa sağ altta navigasyon çıkıyo deneyebilirsiniz
            //uygulamayı imzalayıp exini almayı ve harita anahtarlarını kısıtlamayı 12.videoda gösteriyo izleyebilirsiniz
        }



    }

    private void registerLauncher() {
        permissionLaouncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    //permissin granted // izin verildi
                    if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        //illa tekrar ifi istedi mecbur yazdık
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                        //region en son konumu almak //hatta onLocationChanged metodu boş olursa en son konumu değiştirirsek tekrar çalıştırmada en son işaretlenen yeri veriri
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
    //haritada uzun basıldığında çalışan metod
    public void onMapLongClick(@NonNull LatLng latLng) {
        //latLng kordinattır
        //mMap.clear() koyulan marker ları temizler
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));

        selectedLatitude = latLng.latitude;
        selectedLongitude = latLng.longitude;

        binding.saveButton.setEnabled(true);

    }

    public void saveButtonOnClick(View view) {

        //Room için Entity, Data Acces Object (DAO),Database sınıfı

        Place place = new Place(binding.placeNameEditText.getText().toString(), selectedLatitude, selectedLongitude);
        //subscribeOn nerede bu işlemi yapıyım
        //observeOn nerede bu işlemi gözlemleyim

        //threading ->Main threading yada UI threading (ana tred) ,Defult (CPU Intensive) (listeyi dizmek gibi,arka arkaya işlem yapmak gibi vs.) ,IO (network,database)

        //disposable//kullan at demek//rxjavada yaptığımız işlemler (Completable lar,Flowable lar,Single lar , Obsorveler bunları çöp torbasına konulabiliyo)
        //

        //subscribe çalıştırma işlemini yapar
        //MapsActivity.this::handleResponse metod referans verildi en son o metodu çalıştırır
        // placeDao.insert(place) işlemini yap ama bunu  Schedulers.io() tredde yap AndroidSchedulers.mainThread() de gözlemlicem
        //
        compositeDisposable.add(placeDao.insert(place)
                .subscribeOn(Schedulers.io())
                // .observeOn(AndroidSchedulers.mainThread()) bu metot dinleme yada gözlemeleme ihtiyaç olmadığı yazılmayada bilir
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
    //aktivite kapandığında çalışan metot
    protected void onDestroy() {
        super.onDestroy();
        //çöp torbasını boşalttık/temizledik
        compositeDisposable.clear();
    }


}

