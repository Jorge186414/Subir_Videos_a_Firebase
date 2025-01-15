package com.tesji.videos_a_firebase

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tesji.videos_a_firebase.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var videoArrayList : ArrayList<ModeloVideo>
    private lateinit var adaptadorVideo: AdaptadorVideo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        comprobarSesion()

        cargarVideos()

        binding.fabAgregarvideo.setOnClickListener {
            startActivity(Intent(applicationContext, AgregarVideoActivity::class.java))
        }
    }

    private fun cargarVideos() {
        videoArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Videos")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                videoArrayList.clear()
                for (ds in snapshot.children){
                    val modeloVideo = ds.getValue(ModeloVideo::class.java)
                    videoArrayList.add(modeloVideo!!)
                }

                adaptadorVideo = AdaptadorVideo(this@MainActivity, videoArrayList)
                binding.rvVideos.adapter = adaptadorVideo
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun comprobarSesion(){
        if(firebaseAuth.currentUser == null){
            startActivity(Intent(applicationContext, LoginGoogleActivity::class.java))
            finishAffinity()
        }
    }
}