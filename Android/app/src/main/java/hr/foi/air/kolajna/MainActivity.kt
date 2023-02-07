package hr.foi.air.kolajna

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import hr.foi.air.kolajna.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var binding: ActivityMainBinding

    private var callbackManager:CallbackManager?=null

    override fun onCreate(savedInstanceState: Bundle?) {

        auth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //onClick btnLogin
        btnLogin.setOnClickListener {

            //uzima vrijednosti inputa
            val username = idEmail.text.toString().trim()
            val password = idPassword.text.toString().trim()

            //provjera
            if(username.isEmpty() || !username.contains("@")){
                idEmail.error = "Unesi ispravan e-mail"
                idEmail.requestFocus()   //prikazuje miš
            }
            else if(password.isEmpty()){
                idPassword.error = "Unesi lozinku"
                idPassword.requestFocus()
            }
            else {
                val intent = Intent(this, Pocetna::class.java)
                startActivity(intent)
                finish()
            }

        }

        tekstRegistracija.setOnClickListener {
            val intent = Intent(this, Registracija::class.java)
            startActivity(intent)
            finish()
        }

        forgetpassword.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Zaboravljena lozinka")
            val view = layoutInflater.inflate(R.layout.dialog_forgot_password, null)

            val username = view.findViewById<EditText>(R.id.idEmail)

            builder.setView(view)
            builder.setPositiveButton("Ponovno postavi") { _, _ ->
                forgotPassword(username)
            }
            builder.setNegativeButton("Zatvori") { _, _ -> }

            builder.show()
        }


        //Google Sign In

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("795380535893-bevetu61pl5iafej1omo3kvghcs6rqtn.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googleBtn.setOnClickListener {
            signIn()
        }

        //za Facebook SignIn

        printKeyHash()

        facebookBtn.setOnClickListener{
            facebookSignIn()
        }
    }

    private fun facebookSignIn() {

        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
            override fun onCancel() {
                Toast.makeText(this@MainActivity, "Login otkazan", Toast.LENGTH_LONG).show()
            }

            override fun onError(error: FacebookException) {
                Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_LONG).show()
            }

            override fun onSuccess(result: LoginResult) {
                handleFacebookAccessToken(result.accessToken)
            }

        })
    }

    private fun handleFacebookAccessToken(accessToken: AccessToken?) {
        val credential = FacebookAuthProvider.getCredential(accessToken!!.token)
        auth.signInWithCredential(credential)
            .addOnFailureListener{ e->
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
            .addOnSuccessListener { result->
                val email = result.user?.email
                Toast.makeText(this, "Prijavljeni ste s e-mail adresom $email", Toast.LENGTH_LONG).show()
            }
    }


    @SuppressLint("PackageManagerGetSignatures")
    private fun printKeyHash() {
        try{
            val info = packageManager.getPackageInfo("hr.foi.air.kolajna", PackageManager.GET_SIGNATURES)
            for(signature in info.signatures){
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.e("KEYHASH", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        }
        catch (e:PackageManager.NameNotFoundException){

        }
        catch (e:NoSuchAlgorithmException){

        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        callbackManager!!.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(applicationContext, GoogleSignInActivity::class.java)
            intent.putExtra(EXTRA_NAME, user.displayName)
            startActivity(intent)
        }
    }
    companion object{
        const val RC_SIGN_IN = 1001
        const val EXTRA_NAME = "EXTRA NAME"
    }

    private fun forgotPassword(username : EditText){
        auth.sendPasswordResetEmail(username.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) Toast.makeText(
                    this@MainActivity,
                    "Poslan je e-mail s oporavkom lozinke.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this@MainActivity,
                    "Ne postoji registrirani korisnik s ovom e-mail adresom.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}