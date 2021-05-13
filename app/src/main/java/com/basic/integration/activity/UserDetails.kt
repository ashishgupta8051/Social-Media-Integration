package com.basic.integration.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.basic.integration.R
import com.basic.integration.model.Users
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class UserDetails : AppCompatActivity() {
    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var loginManager: LoginManager
    private lateinit var databaseReference: DatabaseReference
    private lateinit var profileImage:ImageView
    private lateinit var nameTxt:TextView
    private lateinit var emailTxt:TextView
    private lateinit var phoneTxt:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        title = "User Details"

        firebaseAuth = FirebaseAuth.getInstance()
        loginManager = LoginManager.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("User Details").child(firebaseAuth.uid.toString())

        profileImage = findViewById(R.id.profile_image)
        nameTxt = findViewById(R.id.getName)
        emailTxt = findViewById(R.id.getEmail)
        phoneTxt = findViewById(R.id.getPhone)
    }

    override fun onStart() {
        super.onStart()
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    var profileImageUrl = snapshot.child("profilePicUrl").getValue().toString()
                    nameTxt.text = snapshot.child("name").getValue().toString()
                    emailTxt.text = snapshot.child("email").getValue().toString()

                    if (snapshot.child("phone").getValue().toString().equals("No Phone Number")){
                        phoneTxt.text = "No Phone Number"
                    }else{
                        phoneTxt.text = snapshot.child("phone").getValue().toString()
                    }

                    if (profileImageUrl.equals("None")){
                        profileImage.setImageResource(R.drawable.profile_image)
                    }else{
                        Picasso.get().load(profileImageUrl).placeholder(R.drawable.progress).into(profileImage)
                    }
                }else{
                    Log.d("Error","User Not Found !!!")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@UserDetails,databaseError.message,Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.logout_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.logout -> {
                firebaseAuth.signOut()
                loginManager.logOut()
                startActivity(Intent(this, Login::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}