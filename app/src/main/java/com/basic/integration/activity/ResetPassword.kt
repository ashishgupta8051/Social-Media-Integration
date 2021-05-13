package com.basic.integration.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.basic.integration.R
import com.google.firebase.auth.FirebaseAuth

class ResetPassword : AppCompatActivity() {
    private lateinit var emailxt: EditText
    private lateinit var sendMail: Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        title = "Reset Password"

        emailxt = findViewById(R.id.reset_mail)
        sendMail = findViewById(R.id.send_mail)


        firebaseAuth = FirebaseAuth.getInstance()

        sendMail.setOnClickListener{
            if (emailxt.text.toString().isEmpty()){
                emailxt.requestFocus()
                emailxt.setError("Enter your email")
            }else if (!Patterns.EMAIL_ADDRESS.matcher(emailxt.text.toString()).matches()){
                emailxt.requestFocus()
                emailxt.setError("Enter valid email")
            }else{
                val builder = AlertDialog.Builder(this)
                val view: View = layoutInflater.inflate(R.layout.layoutdialog,null)

                val titleTxt: TextView = view.findViewById(R.id.progressBarTitle)
                val messageTxt: TextView = view.findViewById(R.id.progressBarMessage)

                titleTxt.setText("Reset Password")
                messageTxt.setText("Please wait .....")

                builder.setCancelable(false).setView(view)
                val dialog: AlertDialog = builder.create()
                dialog.show()

                firebaseAuth.sendPasswordResetEmail(emailxt.text.toString()).addOnCompleteListener(this){
                    task ->

                    if (task.isSuccessful){
                        dialog.dismiss()
                        Toast.makeText(this,"Check your email id",Toast.LENGTH_SHORT).show()
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