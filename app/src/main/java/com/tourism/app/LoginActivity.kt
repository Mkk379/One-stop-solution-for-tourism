package com.tourism.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tourism.app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var b: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Session.current(this) != null) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnLogin.setOnClickListener {
            val email = b.etEmail.str()
            val pw = b.etPassword.str()
            if (email.isEmpty() || pw.isEmpty()) {
                toast("Enter email and password")
                return@setOnClickListener
            }
            val user = Db.get(this).login(email, pw)
            if (user == null) {
                toast("Invalid email or password")
            } else {
                Session.save(this, user)
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }
        }
        b.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
