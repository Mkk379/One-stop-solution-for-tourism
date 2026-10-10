package com.tourism.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.tourism.app.databinding.ActivityListBinding
import com.tourism.app.databinding.DialogBookBinding
import java.util.Calendar

class ListingsActivity : AppCompatActivity() {
    private lateinit var b: ActivityListBinding
    private lateinit var user: User
    private lateinit var mode: String
    private var type: String? = null
    private lateinit var adapter: ListingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val u = Session.current(this)
        if (u == null) {
            finish()
            return
        }
        user = u
        mode = intent.getStringExtra(Extra.MODE) ?: Mode.BROWSE
        type = intent.getStringExtra(Extra.TYPE)

        b = ActivityListBinding.inflate(layoutInflater)
        setContentView(b.root)

        title = when (mode) {
            Mode.BROWSE -> when (type) {
                ListingType.HOTEL -> "Hotels"
                ListingType.EVENT -> "Events"
                else -> "Cabs"
            }
            Mode.OWN -> "My Listings"
            else -> "All Listings"
        }

        adapter = ListingAdapter { listing ->
            if (mode == Mode.BROWSE) showBook(listing) else confirmDelete(listing)
        }
        b.rv.layoutManager = LinearLayoutManager(this)
        b.rv.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) load()
    }

    private fun load() {
        val db = Db.get(this)
        val items = when (mode) {
            Mode.BROWSE -> db.listings(type = type)
            Mode.OWN -> db.listings(ownerId = user.id)
            else -> db.listings()
        }
        adapter.submit(items, if (mode == Mode.BROWSE) "Book" else "Delete")
        b.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun confirmDelete(l: Listing) {
        AlertDialog.Builder(this)
            .setTitle("Delete listing?")
            .setMessage("${l.name} and its bookings will be removed.")
            .setPositiveButton("Delete") { _, _ ->
                Db.get(this).deleteListing(l.id)
                load()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showBook(l: Listing) {
        val d = DialogBookBinding.inflate(layoutInflater)
        val unit = when (l.type) {
            ListingType.HOTEL -> "Nights"
            ListingType.EVENT -> "Tickets"
            else -> "Days"
        }
        val dateLabel = when (l.type) {
            ListingType.HOTEL -> "Check-in date"
            ListingType.EVENT -> "Event date"
            else -> "Pickup date"
        }
        d.tvInfo.text = l.name + "\n" + money(l.price) + " per " + unit.lowercase().removeSuffix("s")
        d.tilDate.hint = dateLabel
        d.tilQty.hint = unit

        d.etDate.setOnClickListener {
            val c = Calendar.getInstance()
            val dp = DatePickerDialog(
                this,
                { _, y, m, day -> d.etDate.setText(String.format("%02d/%02d/%04d", day, m + 1, y)) },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            )
            dp.datePicker.minDate = System.currentTimeMillis() - 1000
            dp.show()
        }

        AlertDialog.Builder(this)
            .setTitle("Book " + typeLabel(l.type))
            .setView(d.root)
            .setPositiveButton("Confirm") { _, _ ->
                val date = d.etDate.str()
                val qty = d.etQty.str().toIntOrNull() ?: 0
                if (date.isEmpty() || qty <= 0) {
                    toast("Pick a date and enter a valid quantity")
                } else {
                    Db.get(this).book(l.id, user.id, date, qty, l.price * qty)
                    toast("Booking placed! See My Bookings.")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
