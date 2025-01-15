package com.tesji.videos_a_firebase

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tesji.videos_a_firebase.databinding.ActivityAgregarVideoBinding
import com.tesji.videos_a_firebase.databinding.ActivityMainBinding

class AgregarVideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgregarVideoBinding
    private var uriVideo : Uri ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelectVideo.setOnClickListener{
            videoPickDialog()
        }
    }

    private fun videoPickDialog() {
        val popupMenu = PopupMenu(this, binding.btnSelectVideo)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Galeria")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Camara")

        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { item ->
            val itemId = item.itemId
            if(itemId == 1){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    videoPickGaleria()
                }else{
                    solicitarPermisoAlmacenamiento.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }else if(itemId == 2){
                Toast.makeText(applicationContext, "Seleccionaste Camara", Toast.LENGTH_SHORT).show()
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun videoPickGaleria(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "video/*"
        resultGaleriaARL.launch(intent)
    }

    private val resultGaleriaARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ resultado ->
            if(resultado.resultCode == Activity.RESULT_OK){
                val data = resultado.data
                uriVideo = data!!.data
                setVideo()
                binding.tvEstadoVideo.text = "Video seleccionado y listo"
            }else{
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }
        }

    private val solicitarPermisoAlmacenamiento =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ esConcedido ->
            if(esConcedido){
                videoPickGaleria()
            }else{
                Toast.makeText(this, "El permiso de almacenamiento no esta concedido", Toast.LENGTH_SHORT).show()
            }
        }

    private fun setVideo() {
        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.videoView)

        binding.videoView.setMediaController(mediaController)
        binding.videoView.setVideoURI(uriVideo)
        binding.videoView.requestFocus()
        binding.videoView.setOnPreparedListener {
            binding.videoView.pause()
        }
    }
}