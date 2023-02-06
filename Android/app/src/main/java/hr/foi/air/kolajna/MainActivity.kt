package hr.foi.air.kolajna


import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        auth = FirebaseAuth.getInstance()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            else{

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
            builder.setPositiveButton("Ponovno postavi", { _, _ ->
                forgotPassword(username)
            })
            builder.setNegativeButton("Zatvori", { _, _ -> })

            builder.show()
        }
    }
    private fun forgotPassword(username : EditText){
        auth.sendPasswordResetEmail(username.getText().toString())
            .addOnCompleteListener(OnCompleteListener<Void?> { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@MainActivity,
                        "Poslan je e-mail s oporavkom lozinke.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                }
            })
            .addOnFailureListener {
                Toast.makeText(
                    this@MainActivity,
                    "Ne postoji registrirani korisnik s ovom e-mail adresom.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}