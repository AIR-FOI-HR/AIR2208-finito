package hr.foi.air.kolajna

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import hr.foi.air.kolajna.MainActivity.Companion.EXTRA_NAME
import hr.foi.air.kolajna.databinding.ActivityGoogleSignInBinding

class GoogleSignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoogleSignInBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleSignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}