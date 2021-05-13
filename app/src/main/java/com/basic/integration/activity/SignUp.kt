package com.basic.integration.activity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.basic.integration.MainActivity
import com.basic.integration.R
import com.basic.integration.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUp : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var name: EditText
    private lateinit var emailEdt : EditText
    private lateinit var passwordEdt : EditText
    private lateinit var phoneEdt : EditText
    private lateinit var signInTxt : TextView
    private lateinit var registerButton: Button
    private lateinit var userDetailsRef: DatabaseReference;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        title = "Sign Up"

        auth = FirebaseAuth.getInstance()
        userDetailsRef = FirebaseDatabase.getInstance().getReference("User Details")

        name = findViewById(R.id.name)
        emailEdt = findViewById(R.id.Email)
        passwordEdt = findViewById(R.id.Password)
        phoneEdt = findViewById(R.id.Number)
        signInTxt = findViewById(R.id.signin)
        registerButton = findViewById(R.id.register)

        signInTxt.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        registerButton.setOnClickListener{
            if (name.text.toString().isEmpty()){
                name.requestFocus()
                name.setError("Enter your Name")
            }else if (emailEdt.text.toString().isEmpty()){
                emailEdt.requestFocus()
                emailEdt.setError("Enter your email")
            }else if (!Patterns.EMAIL_ADDRESS.matcher(emailEdt.text.toString()).matches()){
                emailEdt.requestFocus()
                emailEdt.setError("Enter valid Email Id")
            }else if (phoneEdt.text.toString().isEmpty()){
                phoneEdt.requestFocus()
                phoneEdt.setError("Enter phone number")
            }else if (passwordEdt.text.toString().isEmpty()){
                passwordEdt.requestFocus()
                passwordEdt.setError("Enter your password")
            }else{
                val builder = AlertDialog.Builder(this)
                val view: View = layoutInflater.inflate(R.layout.layoutdialog,null)

                val titleTxt: TextView = view.findViewById(R.id.progressBarTitle)
                val messageTxt:TextView = view.findViewById(R.id.progressBarMessage)

                titleTxt.setText("Sign Up")
                messageTxt.setText("Please wait .....")

                builder.setCancelable(false).setView(view)
                val dialog:AlertDialog = builder.create()
                dialog.show()

                  auth.createUserWithEmailAndPassword(emailEdt.text.toString(),passwordEdt.text.toString())
                    .addOnCompleteListener(this){
                    task ->

                    if (task.isSuccessful){
                        val userDetails = Users(auth.currentUser.uid,name.text.toString(),emailEdt.text.toString(),phoneEdt.text.toString(),"None")
                        userDetailsRef.child(auth.currentUser.uid).setValue(userDetails).addOnCompleteListener(this){
                                task ->
                            if (task.isSuccessful){
                                dialog.dismiss()
                                startActivity(Intent(this, UserDetails::class.java))
                                finish()
                            }else{
                                dialog.dismiss()
                                Toast.makeText(this,task.exception.toString(),Toast.LENGTH_SHORT).show()
                            }
                        }
                    }else{
                        dialog.dismiss()
                        Toast.makeText(this,task.exception.toString(),Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }



    }

    override fun onBackPressed() {
        startActivity(Intent(this, Login::class.java))
        finish()
    }
}