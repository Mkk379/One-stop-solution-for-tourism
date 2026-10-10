package com.tourism.app

object Role {
    const val ADMIN = "ADMIN"
    const val USER = "USER"
    const val HOTEL = "HOTEL"
    const val EVENT = "EVENT"
    const val CAB = "CAB"
}

object ListingType {
    const val HOTEL = "HOTEL"
    const val EVENT = "EVENT"
    const val CAB = "CAB"
}

object BookingStatus {
    const val PENDING = "PENDING"
    const val CONFIRMED = "CONFIRMED"
    const val CANCELLED = "CANCELLED"
}

data class User(val id: Long, val name: String, val email: String, val phone: String, val role: String)

data class Listing(
    val id: Long, val type: String, val name: String, val location: String,
    val price: Double, val description: String, val ownerId: Long
)

data class Booking(
    val id: Long, val listingId: Long, val listingName: String, val listingType: String,
    val userId: Long, val userName: String, val date: String, val quantity: Int,
    val total: Double, val status: String
)
