package com.tourism.app

import android.content.Context
import android.widget.TextView
import android.widget.Toast

object Mode {
    const val BROWSE = "BROWSE"     // user browses a type
    const val MANAGE = "MANAGE"     // admin: all listings
    const val OWN = "OWN"           // owner: own listings
    const val MINE = "MINE"         // user: own bookings
    const val RECEIVED = "RECEIVED" // owner: bookings on own listings
    const val ALL = "ALL"           // admin: all bookings
}

object Extra {
    const val MODE = "mode"
    const val TYPE = "type"
}

fun TextView.str(): String = text.toString().trim()

fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun money(d: Double): String =
    "₹" + (if (d % 1.0 == 0.0) d.toLong().toString() else String.format("%.2f", d))

fun typeLabel(t: String): String = when (t) {
    ListingType.HOTEL -> "Hotel"
    ListingType.EVENT -> "Event"
    else -> "Cab"
}

fun roleLabel(r: String): String = when (r) {
    Role.ADMIN -> "Administrator"
    Role.HOTEL -> "Hotel Owner"
    Role.EVENT -> "Event Organizer"
    Role.CAB -> "Cab Operator"
    else -> "Tourist"
}
