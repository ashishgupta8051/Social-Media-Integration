package com.basic.integration.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.basic.integration.R
import com.basic.integration.model.Users
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import java.util.*

class Login : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var emailEdt : EditText
    private lateinit var passwordEdt : EditText
    private lateinit var signUpTxt : TextView
    private lateinit var forgotPasswordTxt : TextView
    private lateinit var loginButton: Button
    private lateinit var userDetailsRef: DatabaseReference;
    private lateinit var signInButton: SignInButton
    private lateinit var facebookLogin: LoginButton
    private lateinit var callBackManager: CallbackManager
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseUser: FirebaseUser
    private val RC_SIGN_IN: Int = 321
    private val EMAIL:String = "email"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        title = "Sign In"

        firebaseAuth = FirebaseAuth.getInstance()
        userDetailsRef = FirebaseDatabase.getInstance().getReference("User Details")

        emailEdt = findViewById(R.id.mail)
        passwordEdt = findViewById(R.id.password)
        signUpTxt = findViewById(R.id.signup)
        forgotPasswordTxt = findViewById(R.id.forgot_password)
        loginButton = findViewById(R.id.login)
        signInButton = findViewById(R.id.google_login)
        facebookLogin = findViewById(R.id.facebook_login_button)

        signUpTxt.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
            finish()
        }

        forgotPasswordTxt.setOnClickListener {
            startActivity(Intent(this, ResetPassword::class.java))
            finish()
        }

        //Gmail Login
        signInButton.setOnClickListener {
            signIn()
        }
        createRequest()

        //Facebook Login
        callBackManager = CallbackManager.Factory.create()

        facebookLogin.setReadPermissions(Arrays.asList(EMAIL))

        facebookLogin.setOnClickListener {
            loginWithFacebook()
        }

        //Login with email and password
        loginButton.setOnClickListener{
            if (emailEdt.text.toString().isEmpty()){
                emailEdt.requestFocus()
                emailEdt.setError("Enter email id")
            }else if (!Patterns.EMAIL_ADDRESS.matcher(emailEdt.text.toString()).matches()){
                emailEdt.requestFocus()
                emailEdt.setError("Enter valid Email id")
            }else if (passwordEdt.text.toString().isEmpty()){
                passwordEdt.requestFocus()
                passwordEdt.setError("Enter password")
            }else{
                //Alert Dialog
                val builder = AlertDialog.Builder(this)
                val view: View = layoutInflater.inflate(R.layout.layoutdialog,null)

                val titleTxt: TextView = view.findViewById(R.id.progressBarTitle)
                val messageTxt:TextView = view.findViewById(R.id.progressBarMessage)

                titleTxt.setText("Login")
                messageTxt.setText("Logging In .....")

                builder.setCancelable(false).setView(view)
                val dialog: AlertDialog = builder.create()

                dialog.show()
                firebaseAuth.signInWithEmailAndPassword(emailEdt.text.toString(),passwordEdt.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            dialog.dismiss()
                            startActivity(Intent(this, UserDetails::class.java))
                            finish()
                        } else {
                            dialog.dismiss()
                            Toast.makeText(this,task.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Alert Dialog for Gmail Login
        val builder = AlertDialog.Builder(this)
        val view: View = layoutInflater.inflate(R.layout.layoutdialog,null)

        val titleTxt: TextView = view.findViewById(R.id.progressBarTitle)
        val messageTxt:TextView = view.findViewById(R.id.progressBarMessage)

        titleTxt.setText("Login with Google")
        messageTxt.setText("Logging In ...")

        builder.setCancelable(false).setView(view)
        val ProgressDialog :AlertDialog = builder.create()


        if (requestCode == RC_SIGN_IN) {
            ProgressDialog.show()
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account:GoogleSignInAccount = task.getResult(ApiException::class.java)!!
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!,ProgressDialog)
            } catch (e: ApiException) {
                ProgressDialog.dismiss()
                Log.w("TAG", "Google sign in failed", e)
            }
        }

        //For facebook
        callBackManager.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        finish()
    }

    private fun loginWithFacebook() {
        //Alert Dialog for Facebook Login
        val builder2 = AlertDialog.Builder(this)
        val view2: View = layoutInflater.inflate(R.layout.layoutdialog,null)

        val titleTxt2: TextView = view2.findViewById(R.id.progressBarTitle)
        val messageTxt2:TextView = view2.findViewById(R.id.progressBarMessage)

        titleTxt2.setText("Login with Facebook")
        messageTxt2.setText("Logging In ...")

        builder2.setCancelable(false).setView(view2)
        var ProgressDialog:AlertDialog = builder2.create()

        facebookLogin.registerCallback(callBackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                ProgressDialog.show()
                Log.d("TAG", "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken,ProgressDialog)
            }

            override fun onCancel() {
                ProgressDialog.dismiss()
                Log.d("TAG", "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                ProgressDialog.dismiss()
                Log.d("TAG", "facebook:onError", error)

            }
        })
    }

    private fun handleFacebookAccessToken(token: AccessToken, ProgressDialog: AlertDialog) {
         val credential = FacebookAuthProvider.getCredential(token.token)
         firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    ProgressDialog.dismiss()
                    checkUserForFacebook(ProgressDialog)
                } else {
                    ProgressDialog.dismiss()
                    Toast.makeText(this,task.exception.toString(),Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createRequest() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(idToken: String, ProgressDialog: AlertDialog) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    checkUserForGoogle(ProgressDialog)
                } else {
                    ProgressDialog.dismiss()
                    Toast.makeText(this,task.exception.toString(),Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUserForGoogle(ProgressDialog: AlertDialog) {
        firebaseUser = firebaseAuth.currentUser!!
        userDetailsRef.child(firebaseUser.uid).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    ProgressDialog.dismiss()
                    startActivity(Intent(this@Login,UserDetails::class.java))
                    finish()
                }else{
                    val name = firebaseUser.displayName
                    val email = firebaseUser.email
                    val photoUrl = firebaseUser.photoUrl.toString()

                    val userDetails =
                        name?.let {
                            if (email != null) {
                                Users(firebaseUser.uid, it,email,"No Phone Number",photoUrl)
                            }
                        }
                    userDetailsRef.child(firebaseUser.uid).setValue(userDetails).addOnCompleteListener(this@Login){
                            task ->

                        if (task.isSuccessful){
                            ProgressDialog.dismiss()
                            startActivity(Intent(this@Login, UserDetails::class.java))
                            finish()
                        }else{
                            ProgressDialog.dismiss()
                            Toast.makeText(this@Login,task.exception.toString(),Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Login,error.message,Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkUserForFacebook(ProgressDialog: AlertDialog) {
        firebaseUser = firebaseAuth.currentUser!!
        userDetailsRef.child(firebaseUser.uid).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    ProgressDialog.dismiss()
                    startActivity(Intent(this@Login,UserDetails::class.java))
                    finish()
                }else{
                    val name = firebaseUser.displayName
                    val email = firebaseUser.email
                    val photoUrl = firebaseUser.photoUrl.toString()

                    val userDetails = Users(firebaseUser.uid,
                        name!!, email!!,"No Phone Number",photoUrl)
                    userDetailsRef.child(firebaseUser.uid).setValue(userDetails).addOnCompleteListener(this@Login){
                            task ->

                        if (task.isSuccessful){
                            ProgressDialog.dismiss()
                            startActivity(Intent(this@Login, UserDetails::class.java))
                            finish()
                        }else{
                            ProgressDialog.dismiss()
                            Toast.makeText(this@Login,task.exception.toString(),Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Login,error.message,Toast.LENGTH_SHORT).show()
            }

        })
    }

}