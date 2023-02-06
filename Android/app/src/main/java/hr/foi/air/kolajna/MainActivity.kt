package hr.foi.air.kolajna

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {


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

    }

}