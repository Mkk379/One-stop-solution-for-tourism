package com.tourism.app

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.tourism.app.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {
    private lateinit var b: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user = Session.current(this)
        if (user == null) {
            goLogin()
            return
        }
        b = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.tvWelcome.text = "Welcome, ${user.name}"
        b.tvRole.text = roleLabel(user.role)

        when (user.role) {
            Role.ADMIN -> {
                addBtn("Add Hotel / Event / Cab") { startActivity(Intent(this, AddListingActivity::class.java)) }
                addBtn("Manage All Listings") { openList(Mode.MANAGE, null) }
                addBtn("All Bookings") { openBookings(Mode.ALL) }
            }
            Role.USER -> {
                addBtn("Explore Hotels") { openList(Mode.BROWSE, ListingType.HOTEL) }
                addBtn("Explore Events") { openList(Mode.BROWSE, ListingType.EVENT) }
                addBtn("Book a Cab") { openList(Mode.BROWSE, ListingType.CAB) }
                addBtn("My Bookings") { openBookings(Mode.MINE) }
            }
            else -> {
                addBtn("Add My ${typeLabel(user.role)}") { startActivity(Intent(this, AddListingActivity::class.java)) }
                addBtn("My Listings") { openList(Mode.OWN, null) }
                addBtn("Bookings Received") { openBookings(Mode.RECEIVED) }
            }
        }
        addBtn("Logout") {
            Session.clear(this)
            goLogin()
        }
    }

    private fun goLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun openList(mode: String, type: String?) {
        val i = Intent(this, ListingsActivity::class.java)
        i.putExtra(Extra.MODE, mode)
        if (type != null) i.putExtra(Extra.TYPE, type)
        startActivity(i)
    }

    private fun openBookings(mode: String) {
        val i = Intent(this, BookingsActivity::class.java)
        i.putExtra(Extra.MODE, mode)
        startActivity(i)
    }

    private fun addBtn(label: String, onClick: () -> Unit) {
        val btn = MaterialButton(this)
        btn.text = label
        btn.setOnClickListener { onClick() }
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.topMargin = (14 * resources.displayMetrics.density).toInt()
        b.container.addView(btn, lp)
    }
}
