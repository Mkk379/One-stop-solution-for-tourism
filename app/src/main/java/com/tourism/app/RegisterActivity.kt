package com.tourism.app

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.tourism.app.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var b: ActivityRegisterBinding
    private val labels = listOf("Tourist / User", "Hotel Owner", "Event Organizer", "Cab Operator")
    private val roles = listOf(Role.USER, Role.HOTEL, Role.EVENT, Role.CAB)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.spRole.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)

        b.btnRegister.setOnClickListener {
            val name = b.etName.str()
            val email = b.etEmail.str()
            val phone = b.etPhone.str()
            val pw = b.etPassword.str()
            when {
                name.isEmpty() || email.isEmpty() -> toast("Name and email are required")
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> toast("Enter a valid email")
                pw.length < 6 -> toast("Password must be at least 6 characters")
                else -> {
                    val ok = Db.get(this).register(name, email, phone, pw, roles[b.spRole.selectedItemPosition])
                    if (ok) {
                        toast("Account created. Please login.")
                        finish()
                    } else {
                        toast("That email is already registered")
                    }
                }
            }
        }
    }
}
