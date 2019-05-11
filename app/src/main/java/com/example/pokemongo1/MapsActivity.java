package com.example.pokemongo1;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;


import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    // sử dụng mediaplayer để cung cấp service phát nhạc
    LocationManager locationManager;
    LocationListener locationListener;
    MediaPlayer player;
    private GoogleMap mMap;

    // tạo button trong activity để load
    //  lại activity giúp cập nhật vị trí của người dùng
    Button btn4;
    PokemonAdapter pokemonAdapter;
    // cái này để thêm pokemon vào adapter
    // khi tạo recycleview or list view cần tạo
    // 1 arraylist để add vào nó và dùng 1 adapter để nó hiện lên
    // listview/recycleview cho mình
    // theo mình hiểu là vậy
    ArrayList<Pokemonp> dsPoke = new ArrayList<>();
    // arr để kiểm tra xem tên pokemon đã có trong arraylist chưa
    // mỗi khi bắt đc pokemon thì nó sẽ tự thêm vào arraylist này, sau đó
    // các lần bắt khác nó sẽ kiểm tra
    ArrayList<String> arr = new ArrayList<>();

    ///////////////////////FIREBASE TESTER///////////////////////
    // mData để upload pokemon lên firebase
    DatabaseReference mData;

    ////////////////////////////////////////
    ArrayList<String> usernames = new ArrayList<String>();
    // pokeds
    ArrayList<String> pokeds = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabhost);

        //activity_maps
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

       // tabhost là cái ở trong game có 2 chữ map và pokemon đã bắt ý
        // nhìn vào đoạn code dưới có thể đoán được chức năng từng hàm đúng k
        TabHost.TabSpec tab1 = tabHost.newTabSpec("t1");
        tab1.setContent(R.id.tab1);
        tab1.setIndicator("Map");
        tabHost.addTab(tab1);
        TabHost.TabSpec tab2 = tabHost.newTabSpec("t2");
        tab2.setContent(R.id.tab2);
        tab2.setIndicator("pokemon đã bắt");
        tabHost.addTab(tab2);

        //ko khai bao ham` nay`
        // nho khai bao ham` nay` nhe
        // ko la ko len du lieu dau..ok ban minh hieu r. Cam on b nhieu
        // ok

        // 2 thằng dưới được thêm khi hàm onCreate triển khai
        // CheckUserPermsions là hàm để yêu cầu xác nhận yêu cầu GPS
        // addPokemon là hàm dùng arraylist để app pokemon kèm tọa độ
        // vào 1 arraylist, từ arraylist này mình sẽ set lên google map
        // recycleview() dùng để hiện thị ảnh, các con pokemon đã bắt ở tab " pokemon đã bắt"
        CheckUserPermsions();
        addPokemon();
        addPokemonp();
        //recycleview();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        // 3 dòng code dưới để tạo gạch kẻ ngang cho mỗi item trong recycle view
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),DividerItemDecoration.VERTICAL);
        Drawable drawable = ContextCompat.getDrawable(getApplication(),R.drawable.custom_divider);
        dividerItemDecoration.setDrawable(drawable);

        recyclerView.addItemDecoration(dividerItemDecoration);
        pokemonAdapter = new PokemonAdapter(dsPoke,getApplicationContext(),getApplicationContext());
        recyclerView.setAdapter(pokemonAdapter);
        ///////////////set firebase////////////////////
        mData = FirebaseDatabase.getInstance().getReference();
        /////////////////////set button//////////
        /*
        btn4 = (Button) findViewById(R.id.btn4);
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setClass(MapsActivity.this, MapsActivity.class);
                startActivity(intent);


                //////////////////////////////////////////

            }
        });

      */

    }


    // 2 hàm dưới dùng để cấp quyền vào GPS của người dùng
    void CheckUserPermsions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
        }

        mylocation();
    }

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mylocation();//cập nhật vị trí của bạn



                } else {
                    Toast.makeText(this, "Bạn vui lòng cấp quyền GPS cho mình", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // cập nhật định vị vị trí cho người chơi
    public void mylocation() {
        ///////////////////////////////////////////////////////////////////////////
        // đoạn code dưới để cập nhật vị trí người dùng, cung cấp bởi dịch vụ
        // search thêm trên android developer để biết thêm về các tham sô trong hàm
        MyLocationListener myloc = new MyLocationListener();
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3, 10, myloc);
        //Location lastKnownLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //////////////////////////////////////////////////////////////////
        // cái này tạo ra database tên "Request" để thêm vào parse lưu trữ vị trí người dùng
        /// trang chủ của parse : back4app.com tên tk : duongduchsgshnue
        /// mật khẩu : Fizzdanhca
        /*

        ParseObject request = new ParseObject("Request");
        // tạo query để truy vấn trên bảng Request theo mình hiểu là vậy, nó sẽ xóa đi tên
        // của người dùng nếu đã có... ở cột tên là username. Mỗi 1 điện thoại khi cài đặt
        // parse server sẽ tạo ra 1 tên user ảo để thêm vào csdl, khi nó đã tồn tại thì ban đầu
        // mình sẽ xóa, sau đó sẽ thêm lại tên người dùng với địa chỉ mới, mục đích việc xóa
        // để xóa đi vị trí cũ cuẩ người dùng đó đi, thêm vị trí mới vào
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Request");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        for (ParseObject object : objects) {
                            //Log.i("xoa","????????????????");
                            object.deleteInBackground();
                        }

                    }
                }
            }
        });

    ///////////////////////////////////////////////////
        // Sau khi xóa như ở đoạn code trên thì ở đoạn code dưới này nó sẽ thêm
        // vị trí sau khi được xóa sẽ được thêm lại ở bảng Request
        // put là đưa giá trị của người dùng lên bảng Request ở 2 cột là username và location(gần phía phải)
        ParseObject request1 = new ParseObject("Request");
        // put tên người dùng đã được tạo
        request1.put("username", ParseUser.getCurrentUser().getUsername());

        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
        // put vị trí hiện tại của người dùng lên server, 2 cái tham số trong ParseGeoPoint
        // lat và log ở vị trí hiện tại
        request1.put("location",parseGeoPoint);
        request1.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

            }
        });
*/
        /////////////////////////////////////////////////////////////
     // tạo 1 luồng thread để cập nhật vị trí = việc sử dụng runOnUiThread(new Runable(){....}) -> cập nhật trên Thread UI
        // và cho luồng đó chạy thôi
        thread a = new thread();
        a.start();

    }

    // nếu không có oldloc thì numOfpokemon tăng liên tục do vòng lặp
    Location oldloc;

    // tạo thread để thay đổi UI trên màn hình
    // nếu k sử dụng thread thì máy sẽ bị dừng lại k hoạt động được

    class thread extends Thread {
        thread(){
            // cái này set thuộc tính cho vị trí oldloc để so sánh thôi
            oldloc = new Location("...");
            oldloc.setLongitude(0);
            oldloc.setLatitude(0);
        }

        @Override
        public void run() {
            // đến khi break ???
            while (true) {

                try {
                    Thread.sleep(1000);
                    // ở dưới là so sánh  vị trí của mình với oldloc(0,0) được tạo ở trên
                    // ban đầu đương nhiên khoảng cách chúng khác 0 nên toàn bộ đoạn dưới continue
                    // đều xảy ra, sau đó oldloc = vị trí hiện tại, nếu sau 1 s nghỉ nó k thay đổi tức bằng 0
                    // toàn bộ đoạn phía dưới continue k chạy còn không nó sẽ chạy nếu MyLocationListener(vị trí của mình)
                    // có thay đổi
                    if (oldloc.distanceTo(MyLocationListener.location) == 0) {
                        continue;
                    }
                    oldloc = MyLocationListener.location;
                    runOnUiThread(new Runnable() {
                        //@Override
                        public void run() {
                            mMap.clear();
                            // 4 dòng code dưới là lấy vị trí hiện tại của mình
                            // có thể mình viết thừa 1 số hàm k cần thiết
                            MyLocationListener myloc = new MyLocationListener();
                            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3, 1, myloc);
                            //Location lastKnownLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);



                            ///////////////////////////////////////////////////////////////
                            /////////////////////////////////////////////////
                            // đoạn dưới tạo truy vấn query1 để lấy từ bảng Request, mình cần lấy địa chỉ(Location thôi) ở cột "location"
                            /*
                            ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Request");
                            final ParseGeoPoint parseGeoPoint1 = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            query1.whereNear("location",parseGeoPoint1);
                            //query.setLimit(10);
                            query1.findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> objects, ParseException e) {
                                    if(e == null) {
                                        // kiểm tra xem pt tồn tại không, nếu có sẽ lấy hết xuống
                                        // sau đó dùng mMap để gán tọa độ nhận được từ DB lên bản đồ
                                        // à đoạn contains để kiểm tra nó tồn tại chưa, nếu rồi thì thôi k add lên map nữa
                                        // nếu chưa thì add lên map
                                        if (objects.size() > 0) {
                                            for (ParseObject object : objects) {

                                                ParseGeoPoint requestLocation = (ParseGeoPoint) object.get("location");
                                                //usernames.add(object.getString("username"));
                                                if(!usernames.contains(object.getString("username"))) {
                                                    Log.i("...............",".....................");
                                                    mMap.addMarker(new MarkerOptions().position(new LatLng(requestLocation.getLatitude(), requestLocation.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.mez)));
                                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(MyLocationListener.location.getLatitude(), MyLocationListener.location.getLongitude()), 17));
                                                    usernames.add(object.getString("username"));
                                                }
                                                //usernames.add(object.getString("username"));


                                            }
                                        }
                                      }
                                    }
                                                   });
                                                   */
                            mMap.addMarker(new MarkerOptions().position(new LatLng(MyLocationListener.location.getLatitude(), MyLocationListener.location.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.mez)));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(MyLocationListener.location.getLatitude(), MyLocationListener.location.getLongitude()), 17));
                            Bundle extras = getIntent().getExtras();
                            String idfb = extras.getString("idfb");

                            for (int i = 0; i < list.size(); i++) {
                                // tạo đối tượng pokemon để get các thành phần trong arraylist
                                Pokemon pokemon = list.get(i);
                               //
                                //if (pokemon.isCatch() == false) {
                                    // 3 dòng code duoi để gán pokemon lên map thôi !!!
                                    /*LatLng locofpokemon = new LatLng(pokemon.getLocation().getLatitude(), pokemon.getLocation().getLongitude());
                                    mMap.addMarker(new MarkerOptions().position(locofpokemon).title(pokemon.getName()).icon(BitmapDescriptorFactory.fromResource(pokemon.getImage())));
                                    */
                                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 17));
                                    // khi bắt được Pokemon

                                    if (MyLocationListener.location.distanceTo(pokemon.getLocation()) < 150) {
                                        LatLng locofpokemon = new LatLng(pokemon.getLocation().getLatitude(), pokemon.getLocation().getLongitude());
                                        mMap.addMarker(new MarkerOptions().position(locofpokemon).title(pokemon.getName()).icon(BitmapDescriptorFactory.fromResource(pokemon.getImage())));
                                        //Lưu pokemon vào list
                                        // kiểm tra xem đã tồn tại phần tử ???? và add pokemon vào dsPoke. dspoke là 1 arraylist để lưu các pokemon trong mảng thôi. Thông qua adapter
                                        //nó sẽ đưa lên app (qua adapter ở hàm onCreate)
                                        if (!arr.contains(pokemon.getName())) {
                                            numOfPokemon = numOfPokemon + 1;
                                            // 2 dòng code dưới để add pokemon vào recycle view đã tạo bên trên
                                            //dsPoke.add(pokemon);
                                            arr.add(pokemon.getName());
                                            // khi catch đc pokeom thì nó phát nhạc
                                            player = MediaPlayer.create(MapsActivity.this,R.raw.mario);
                                            player.start();
                                            // thêm pokemon vào firebase bằng  cách mình tạo 1 arraylist pppp chứa pokemon y hệt
                                            // nhưng nó k có tọa độ, hay hình ảnh vì Firebase mình không lưu được tọa độ, còn hình ảnh
                                            // up lên firebase lẫn tải xuống nó lằng nhằng nên thôi
                                            for(int j = 0; j <pppp.size();j++){
                                                Pokemonp pokemonp = pppp.get(j);
                                                // đoạn này mình kiểm tra pokemon bắt được nó bằng phần tử nào trong pppp
                                                // cái mình up lên firebase là pokemon trong pppp
                                                // nếu bằng nhau thì push pppp (upload lên firebase)
                                                if(pokemon.getName().equals(pokemonp.getName())){
                                                    //Bundle extras = getIntent().getExtras();
                                                    //String idfb = extras.getString("idfb");

                                                    mData.child(idfb).push().setValue(pokemonp);
                                                    dsPoke.add(pokemonp);

                                                }

                                            }






                                        }

                                        //tạo notification chứa tên con pokemon vừa bắt và tổng số pokemon hiện có
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MapsActivity.this);
                                        builder.setSmallIcon(R.drawable.pikachu);
                                        builder.setContentTitle("Bạn vừa bắt được " + pokemon.getName());
                                        builder.setContentText("Bạn hiện đang có " + numOfPokemon + " .Hãy vào túi kiểm tra");
                                        Intent intent = new Intent(MapsActivity.this, MapsActivity.class);

                                        //  MapsActivity.class
                                        PendingIntent pendingIntent = PendingIntent.getActivity(MapsActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

                                        builder.setContentIntent(pendingIntent);
                                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                        notificationManager.notify("...", 123, builder.build());


                                        list.remove(pokemon);

                                    }
                                    // đoạn này để lấy Pokemon pppp đã lưu xuống, sau đó dùng hàm đã cho điền theo
                                    // hướng dẫn

                                    mData.child(idfb).addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                            Pokemonp pokemonpzzzzx = dataSnapshot.getValue(Pokemonp.class);
                                            // cái này để lưu Pokemon lấy được từ firebase xuống, lưu nó vào trong dsPoke
                                            // rồi cuối cùng mình set adapter để nó hiện lên recycle view
                                            for(int k = 0; k < pppp.size(); k++){
                                                Pokemonp pokemon5 = pppp.get(k);
                                                if(pokemonpzzzzx.getName().equals(pokemon5.getName()) && !arr.contains(pokemon5.getName())){
                                                    dsPoke.add(pokemon5);
                                                    Log.i("heell" , pokemon5.getName());
                                                    arr.add(pokemon5.getName());
                                                }

                                            }
                                            //pokemonAdapter.notifyDataSetChanged();

                                        }

                                        @Override
                                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                        }

                                        @Override
                                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                                        }

                                        @Override
                                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                //}


                            }


                            // .. set lại adapter mỗi lần thay đổi
                            pokemonAdapter.notifyDataSetChanged();
                        }
                    });


                } catch (Exception e) {

                }

            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


    }
    // 2 cái arraylist phía dưới : 1 cái dùng để đính lên google map
    // cái arraylist dưới dùng để up lên firebase như mình nói phía trên, giống tên nhưng k có
    // location, và nó dùng để đưa lên recycle view
    int numOfPokemon = 0;
    // TẠO arraylist thêm pokemon
    ArrayList<Pokemon> list = new ArrayList<>();

    public void addPokemon() {



        list.add(new Pokemon(R.drawable.bulbasaurz, "Bulbasaur", "45", 21.040450, 105.783106));
        list.add(new Pokemon(R.drawable.charmanderz, "Charmander","32",  21.039949, 105.780231));
        list.add(new Pokemon(R.drawable.metapodz, "Metapod","25",  21.037045, 105.784351));
        list.add(new Pokemon(R.drawable.pidgeotz, "Pidgeot", "45", 21.037556, 105.784748));
        list.add(new Pokemon(R.drawable.poliwrathz, "Poliwrathz","60",  21.035483, 105.783729));
        list.add(new Pokemon(R.drawable.arbok ,"Arbok","48",21.008645,105.814592));
        list.add(new Pokemon(R.drawable.bellsprout,"bellsprout","59",21.022596,105.803273));
        list.add(new Pokemon(R.drawable.diglett,"diglett","78",21.026402,105.796160));
        list.add(new Pokemon(R.drawable.dodrio,"dodrio","56",21.036266,105.789358));
        list.add(new Pokemon(R.drawable.dragonite,"dragonite","89",21.035785,105.786204));
        list.add(new Pokemon(R.drawable.exeggutor,"exeggutor","19",21.032030,105.784305));
        list.add(new Pokemon(R.drawable.gengar,"gengar","57",21.028475,105.779895));
        list.add(new Pokemon(R.drawable.growlithe,"growlithe","77",21.027423,105.778318));
        list.add(new Pokemon(R.drawable.haunter,"haunter","74",21.017218,105.790813));
        list.add(new Pokemon(R.drawable.hitmonlee,"hitmonlee","90",21.009772,105.797779));
        list.add(new Pokemon(R.drawable.jolteon,"jolteon","78",21.006119,105.801079));
        list.add(new Pokemon(R.drawable.koffing,"koffing","64",21.004637,105.798826));
        list.add(new Pokemon(R.drawable.krabby,"krabby","86",21.002203,105.800875));
        list.add(new Pokemon(R.drawable.magnemite,"magnemite","76",20.998267,105.802989));
        list.add(new Pokemon(R.drawable.mankey,"mankey","39",20.986466,105.814105));
        list.add(new Pokemon(R.drawable.nidoran,"nidoran","64",21.000149,105.857124));
        list.add(new Pokemon(R.drawable.poliwag,"poliwag","78",20.999207,105.854517));
        list.add(new Pokemon(R.drawable.ponyta,"ponyta","65",20.998796,105.853498));
        list.add(new Pokemon(R.drawable.sandshrew,"sandshrew","65",20.997354,105.850290));
        list.add(new Pokemon(R.drawable.scyther,"scyther","67",20.996032, 105.845494));
        list.add(new Pokemon(R.drawable.snorlax,"snorlax","87",20.991114,105.855300));
        list.add(new Pokemon(R.drawable.venomoth,"venomoth","98",21.005291,105.845651));
        list.add(new Pokemon(R.drawable.vulpix,"vulpix","76",21.002697,105.851080));
    }

    ArrayList<Pokemonp> pppp = new ArrayList<>();
    public void addPokemonp(){

        pppp.add(new Pokemonp(R.drawable.bulbasaurz, "Bulbasaur", "45"));
        pppp.add(new Pokemonp(R.drawable.charmanderz, "Charmander","32"));
        pppp.add(new Pokemonp(R.drawable.metapodz, "Metapod","25"));
        pppp.add(new Pokemonp(R.drawable.pidgeotz, "Pidgeot", "45"));
        pppp.add(new Pokemonp(R.drawable.poliwrathz, "Poliwrathz","60"));
        pppp.add(new Pokemonp(R.drawable.arbok ,"Arbok","48"));
        pppp.add(new Pokemonp(R.drawable.bellsprout,"bellsprout","59"));
        pppp.add(new Pokemonp(R.drawable.diglett,"diglett","78"));
        pppp.add(new Pokemonp(R.drawable.dodrio,"dodrio","56"));
        pppp.add(new Pokemonp(R.drawable.dragonite,"dragonite","89"));
        pppp.add(new Pokemonp(R.drawable.exeggutor,"exeggutor","19"));
        pppp.add(new Pokemonp(R.drawable.gengar,"gengar","57"));
        pppp.add(new Pokemonp(R.drawable.growlithe,"growlithe","77"));
        pppp.add(new Pokemonp(R.drawable.haunter,"haunter","74"));
        pppp.add(new Pokemonp(R.drawable.hitmonlee,"hitmonlee","90"));
        pppp.add(new Pokemonp(R.drawable.jolteon,"jolteon","78"));
        pppp.add(new Pokemonp(R.drawable.koffing,"koffing","64"));
        pppp.add(new Pokemonp(R.drawable.krabby,"krabby","86"));
        pppp.add(new Pokemonp(R.drawable.magnemite,"magnemite","76"));
        pppp.add(new Pokemonp(R.drawable.mankey,"mankey","39"));
        pppp.add(new Pokemonp(R.drawable.nidoran,"nidoran","64"));
        pppp.add(new Pokemonp(R.drawable.poliwag,"poliwag","78"));
        pppp.add(new Pokemonp(R.drawable.ponyta,"ponyta","65"));
        pppp.add(new Pokemonp(R.drawable.sandshrew,"sandshrew","65"));
        pppp.add(new Pokemonp(R.drawable.scyther,"scyther","67"));
        pppp.add(new Pokemonp(R.drawable.snorlax,"snorlax","87"));
        pppp.add(new Pokemonp(R.drawable.venomoth,"venomoth","98"));
        pppp.add(new Pokemonp(R.drawable.vulpix,"vulpix","76"));

    }



}
