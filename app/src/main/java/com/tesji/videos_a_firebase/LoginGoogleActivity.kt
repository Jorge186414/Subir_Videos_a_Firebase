package com.tesji.videos_a_firebase

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.tesji.videos_a_firebase.databinding.ActivityLoginGoogleBinding

class LoginGoogleActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginGoogleBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginGoogleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Cargando, Por Favor Espere")
        progressDialog.setCanceledOnTouchOutside(false)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id_strings))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        binding.btnLoginGoogle.setOnClickListener {
            iniciarGoogle()
        }
    }

    private fun iniciarGoogle(){
        val googleSignInClient = mGoogleSignInClient.signInIntent
        googleSignInARL.launch(googleSignInClient)
    }

    private val googleSignInARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){resultado->

        if(resultado.resultCode == RESULT_OK){
            val data = resultado.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val cuenta = task.getResult(ApiException::class.java)
                // Iniciamos Sesion
                autenticarCuentaGoogle(cuenta.idToken)
            }catch (e:Exception){
                Toast.makeText(this,"${e.message}",Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this,"Cancelado",Toast.LENGTH_SHORT).show()
        }
    }

    private  fun autenticarCuentaGoogle(idToken: String?){
        val credencial = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credencial)
            .addOnSuccessListener { authResult ->
                if(authResult.additionalUserInfo!!.isNewUser){
                    actualizarInfoUsuario()
                }else{
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener{e->
                Toast.makeText(this,"${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarInfoUsuario() {
        progressDialog.setMessage("Guardando informacion")
        progressDialog.show()

        val uid = firebaseAuth.uid
        val nombre = firebaseAuth.currentUser?.displayName
        val email = firebaseAuth.currentUser?.email

        val datosUsuario = HashMap<String, Any>()
        datosUsuario["uid"] = "${uid}"
        datosUsuario["nombre"] = "${nombre}"
        datosUsuario["email"] = "${email}"

        val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
        reference.child(uid!!)
            .setValue(datosUsuario)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this,"${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

}