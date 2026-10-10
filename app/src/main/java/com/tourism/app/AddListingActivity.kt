package com.tourism.app

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.tourism.app.databinding.ActivityAddListingBinding

class AddListingActivity : AppCompatActivity() {
    private lateinit var b: ActivityAddListingBinding
    private val types = listOf(ListingType.HOTEL, ListingType.EVENT, ListingType.CAB)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user = Session.current(this)
        if (user == null) {
            finish()
            return
        }
        b = ActivityAddListingBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.spType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types.map { typeLabel(it) })
        if (user.role != Role.ADMIN) {
            // owners can only add their own kind of listing (Role and ListingType share values)
            b.spType.setSelection(types.indexOf(user.role).coerceAtLeast(0))
            b.spType.isEnabled = false
        }

        b.btnSave.setOnClickListener {
            val name = b.etName.str()
            val loc = b.etLocation.str()
            val price = b.etPrice.str().toDoubleOrNull()
            if (name.isEmpty() || loc.isEmpty() || price == null || price <= 0) {
                toast("Name, location and a valid price are required")
                return@setOnClickListener
            }
            Db.get(this).addListing(types[b.spType.selectedItemPosition], name, loc, price, b.etDesc.str(), user.id)
            toast("Listing saved")
            finish()
        }
    }
}