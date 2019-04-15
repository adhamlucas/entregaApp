package com.example.entregaapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button_register.setOnClickListener{
            val email = email_edittext_register.text.toString()
            val password = password_edittext_register.text.toString()

            if(email.isEmpty()|| password.isEmpty()){
                Toast.makeText(this, "Please enter text in email/password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("MainActivity", "Emails is: " + email)
            Log.d("MainActivity", "Password is:  $password")


            //Firebase Authenticate to Create a user with email and password
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        Log.d("MainActivity", "Sucessufly create user with uid: ${it.result?.user?.uid}")
                        Toast.makeText(this,"Sucessufly create user!", Toast.LENGTH_SHORT).show()
                    } //
                    else{
                        return@addOnCompleteListener
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                    Log.d("MainActivity", "Failed to create user: ${it.message}")
                }
//
        }

        already_have_account_text_view.setOnClickListener{
            Log.d("MainActivity", "Try to show Login Activity")
            finish()
        }
    }
}