package com.example.wifiwithkotlin

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wifiwithkotlin.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import kotlin.math.round

data class myLatLng(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

class RouteActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    //private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.mapview2) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val routefinish:Button = findViewById(R.id.btnrouteFinish)
        routefinish.setOnClickListener{
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val database = FirebaseDatabase.getInstance()
        val parentRef: DatabaseReference = database.reference.child("$user_name")

        val textView: TextView = findViewById(R.id.textview2)
        val printdistance = round(user_distance * 1000) / 1000.0f
        textView.text = "${user_name}의 이동 거리: $printdistance km"

        parentRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val currentTime = dataSnapshot.key
                val pathRef: DatabaseReference = parentRef.child(currentTime.toString())
                val coordinates: MutableList<myLatLng> = mutableListOf()

                pathRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (childSnapshot in dataSnapshot.children) {
                            val coordinate: myLatLng? = childSnapshot.getValue(object : GenericTypeIndicator<myLatLng>() {})
                            coordinate?.let { coordinates.add(it) }
                        }
                        // coordinates 배열을 이용하여 필요한 처리 로직을 수행합니다.
                        drawPolyline(coordinates)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        println("데이터 읽기 중 오류 발생: $databaseError")
                    }
                })
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // 경로 데이터가 변경되었을 때의 처리 로직을 작성합니다.
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                // 경로 데이터가 제거되었을 때의 처리 로직을 작성합니다.
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // 경로 데이터가 이동되었을 때의 처리 로직을 작성합니다.
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("데이터 읽기 중 오류 발생: $databaseError")
            }
        })

//        val valueEventListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    val arrayData = mutableListOf<myLatLng>()
//                    for (childSnapshot in dataSnapshot.children) {
//                        val latLngData = childSnapshot.getValue(myLatLng::class.java)
//                        latLngData?.let { arrayData.add(it) }
//                    }
//                    // 배열 데이터 사용
//                    drawPolyline(arrayData)
//                }
//                else {
//                    // 데이터베이스에 해당 경로에 값이 없는 경우 처리
//                    Toast.makeText(applicationContext, "저장된 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
//                    finish()
//                }
//            }
//            override fun onCancelled(databaseError: DatabaseError) {
//                // 읽기 작업이 취소된 경우 처리
//            }
//        }
//        childRef.addValueEventListener(valueEventListener)



    }

    fun drawPolyline(latLngs: List<myLatLng>) {
        val polylineOptions = PolylineOptions()
            .addAll(latLngs.map { LatLng(it.latitude, it.longitude) })

        mMap.addPolyline(polylineOptions)

        // 카메라 위치 변경
        val lastLatLng = latLngs.lastOrNull()
        if (lastLatLng != null) {
            // 카메라를 마지막 LatLng 위치로 이동합니다.
            val cameraPosition = CameraPosition.Builder()
                .target(LatLng(lastLatLng.latitude, lastLatLng.longitude))
                .zoom(16f) // 줌 레벨 설정
                .build()
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }
}
