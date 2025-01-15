package com.tesji.videos_a_firebase

import android.app.Activity
import android.app.ProgressDialog
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.tesji.videos_a_firebase.databinding.ActivityAgregarVideoBinding
import com.tesji.videos_a_firebase.databinding.ActivityMainBinding

class AgregarVideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgregarVideoBinding
    private var uriVideo : Uri ?= null
    private var titulo : String ?= null
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por Favor Espere")
        progressDialog.setMessage("Se esta subiendo el video")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.btnSelectVideo.setOnClickListener{
            videoPickDialog()
        }

        binding.btnUploadVideo.setOnClickListener {
            titulo = binding.etTituloVideo.text.toString().trim()
            if(titulo.isNullOrEmpty()){
                Toast.makeText(applicationContext, "Ingresa un titulo de video", Toast.LENGTH_SHORT).show()
            }else if(uriVideo == null){
                Toast.makeText(applicationContext, "Carga un video", Toast.LENGTH_SHORT).show()
            }else{
                uploadVideo()
            }
        }
    }

    private fun uploadVideo() {
        progressDialog.show()

        val autor = firebaseAuth.currentUser!!.displayName

        val tiempo = ""+System.currentTimeMillis()
        val rutaNombre = "Video/video_${tiempo}"
        val storageRef = FirebaseStorage.getInstance().getReference(rutaNombre)
        storageRef.putFile(uriVideo!!)
            .addOnSuccessListener { task ->
                val uriTask = task.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val downloadUri = uriTask.result.toString()
                if(uriTask.isSuccessful){
                    val hashMap = HashMap<String, Any>()
                    hashMap["id"] = "${tiempo}"
                    hashMap["titulo"] = "${titulo}"
                    hashMap["tiempo"] = "${tiempo}"
                    hashMap["videoUri"] = "${downloadUri}"
                    hashMap["autor"] = "${autor}"

                    val reference = FirebaseDatabase.getInstance().getReference("Videos")
                    reference.child(tiempo)
                        .setValue(hashMap)
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(applicationContext, "Video subido Exitosamente!!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@AgregarVideoActivity, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()
                        }
            }
        }
            .addOnFailureListener{ e ->
                Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()
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
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    recordVideo()
                }else{
                    solicitarPermisoCamara.launch(android.Manifest.permission.CAMERA)
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun videoPickGaleria(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "video/*"
        resultGaleriaARL.launch(intent)
    }

    private fun recordVideo(){
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        recordVideoARL.launch(intent)
    }

    private val recordVideoARL = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == Activity.RESULT_OK){
            val data = result.data
            uriVideo = data!!.data
            setVideo()
            binding.tvEstadoVideo.text = "Video seleccionado y listo"
        }else{
            Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
        }
    }

    private val solicitarPermisoCamara =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ esConcedido ->
            if(esConcedido){
                recordVideo()
            }else{
                Toast.makeText(this, "El permiso para la camara no se ha concedido", Toast.LENGTH_SHORT).show()
            }
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