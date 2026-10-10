package com.tourism.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.tourism.app.databinding.ActivityListBinding

class BookingsActivity : AppCompatActivity() {
    private lateinit var b: ActivityListBinding
    private lateinit var user: User
    private lateinit var mode: String
    private lateinit var adapter: BookingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val u = Session.current(this)
        if (u == null) {
            finish()
            return
        }
        user = u
        mode = intent.getStringExtra(Extra.MODE) ?: Mode.MINE

        b = ActivityListBinding.inflate(layoutInflater)
        setContentView(b.root)

        title = when (mode) {
            Mode.MINE -> "My Bookings"
            Mode.RECEIVED -> "Bookings Received"
            else -> "All Bookings"
        }
        b.tvEmpty.text = "No bookings yet"

        adapter = BookingAdapter(
            canConfirm = mode != Mode.MINE,
            onConfirm = { setStatus(it, BookingStatus.CONFIRMED) },
            onCancel = { setStatus(it, BookingStatus.CANCELLED) }
        )
        b.rv.layoutManager = LinearLayoutManager(this)
        b.rv.adapter = adapter
        load()
    }

    private fun setStatus(bk: Booking, status: String) {
        Db.get(this).setBookingStatus(bk.id, status)
        load()
    }

    private fun load() {
        val db = Db.get(this)
        val items = when (mode) {
            Mode.MINE -> db.bookings(userId = user.id)
            Mode.RECEIVED -> db.bookings(ownerId = user.id)
            else -> db.bookings()
        }
        adapter.submit(items)
        b.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }
}
