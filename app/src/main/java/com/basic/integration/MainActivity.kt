package com.basic.integration

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.basic.integration.activity.Login
import com.basic.integration.activity.UserDetails
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth:FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.statusBarColor = ContextCompat.getColor(this,R.color.silver)
        supportActionBar?.hide()

        firebaseAuth = FirebaseAuth.getInstance()

        val background=object :Thread(){
            override fun run() {
                Thread.sleep(4000)
                val intent:Intent
                var firebaseUser = firebaseAuth.currentUser
                if (firebaseUser != null){
                    intent= Intent(this@MainActivity, UserDetails::class.java)
                    startActivity(intent)
                }else{
                    intent= Intent(this@MainActivity, Login::class.java)
                    startActivity(intent)
                }
                finish()
            }
        }
        background.start()
    }
}