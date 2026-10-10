package com.tourism.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest

class Db private constructor(ctx: Context) : SQLiteOpenHelper(ctx, "tourism.db", null, 1) {

    companion object {
        @Volatile
        private var inst: Db? = null

        fun get(ctx: Context): Db =
            inst ?: synchronized(this) { inst ?: Db(ctx.applicationContext).also { inst = it } }

        fun hash(s: String): String =
            MessageDigest.getInstance("SHA-256").digest(s.toByteArray())
                .joinToString("") { "%02x".format(it) }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, " +
                "email TEXT NOT NULL UNIQUE, phone TEXT, password TEXT NOT NULL, role TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE listings(id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT NOT NULL, " +
                "name TEXT NOT NULL, location TEXT, price REAL, description TEXT, owner_id INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE bookings(id INTEGER PRIMARY KEY AUTOINCREMENT, listing_id INTEGER NOT NULL, " +
                "user_id INTEGER NOT NULL, date TEXT, quantity INTEGER, total REAL, status TEXT)"
        )

        // Default admin account
        val admin = ContentValues()
        admin.put("name", "Administrator")
        admin.put("email", "admin@tourism.com")
        admin.put("phone", "")
        admin.put("password", hash("admin123"))
        admin.put("role", Role.ADMIN)
        val adminId = db.insert("users", null, admin)

        // Sample data so the app isn't empty on first run
        seed(db, adminId, ListingType.HOTEL, "Sea Breeze Resort", "Kovalam, Kerala", 3500.0, "Beachfront rooms with breakfast included.")
        seed(db, adminId, ListingType.HOTEL, "Hilltop Residency", "Munnar, Kerala", 2800.0, "Tea-garden views, free Wi-Fi and parking.")
        seed(db, adminId, ListingType.EVENT, "Backwater Music Fest", "Alappuzha, Kerala", 750.0, "Live music evening on the backwaters.")
        seed(db, adminId, ListingType.EVENT, "Kathakali Night", "Kochi, Kerala", 400.0, "Traditional Kathakali performance with commentary.")
        seed(db, adminId, ListingType.CAB, "Sedan - Airport Transfer", "Kochi", 1200.0, "AC sedan, up to 4 passengers, per day.")
        seed(db, adminId, ListingType.CAB, "SUV - Sightseeing", "Munnar", 2500.0, "7-seater SUV with driver, per day.")
    }

    private fun seed(db: SQLiteDatabase, owner: Long, type: String, name: String, loc: String, price: Double, desc: String) {
        val cv = ContentValues()
        cv.put("type", type); cv.put("name", name); cv.put("location", loc)
        cv.put("price", price); cv.put("description", desc); cv.put("owner_id", owner)
        db.insert("listings", null, cv)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    // ---------- Users ----------
    fun register(name: String, email: String, phone: String, password: String, role: String): Boolean {
        val cv = ContentValues()
        cv.put("name", name); cv.put("email", email.lowercase()); cv.put("phone", phone)
        cv.put("password", hash(password)); cv.put("role", role)
        return writableDatabase.insert("users", null, cv) != -1L
    }

    fun login(email: String, password: String): User? {
        readableDatabase.rawQuery(
            "SELECT id,name,email,phone,role FROM users WHERE email=? AND password=?",
            arrayOf(email.lowercase(), hash(password))
        ).use { c ->
            return if (c.moveToFirst()) User(c.getLong(0), c.getString(1), c.getString(2), c.getString(3) ?: "", c.getString(4)) else null
        }
    }

    fun getUser(id: Long): User? {
        readableDatabase.rawQuery(
            "SELECT id,name,email,phone,role FROM users WHERE id=?", arrayOf(id.toString())
        ).use { c ->
            return if (c.moveToFirst()) User(c.getLong(0), c.getString(1), c.getString(2), c.getString(3) ?: "", c.getString(4)) else null
        }
    }

    // ---------- Listings ----------
    fun addListing(type: String, name: String, location: String, price: Double, desc: String, ownerId: Long) {
        seed(writableDatabase, ownerId, type, name, location, price, desc)
    }

    fun listings(type: String? = null, ownerId: Long? = null): List<Listing> {
        val where = ArrayList<String>()
        val args = ArrayList<String>()
        if (type != null) { where.add("type=?"); args.add(type) }
        if (ownerId != null) { where.add("owner_id=?"); args.add(ownerId.toString()) }
        val sql = "SELECT id,type,name,location,price,description,owner_id FROM listings" +
            (if (where.isEmpty()) "" else " WHERE " + where.joinToString(" AND ")) + " ORDER BY id DESC"
        val out = ArrayList<Listing>()
        readableDatabase.rawQuery(sql, args.toTypedArray()).use { c ->
            while (c.moveToNext()) {
                out.add(Listing(c.getLong(0), c.getString(1), c.getString(2), c.getString(3) ?: "", c.getDouble(4), c.getString(5) ?: "", c.getLong(6)))
            }
        }
        return out
    }

    fun deleteListing(id: Long) {
        val db = writableDatabase
        db.delete("bookings", "listing_id=?", arrayOf(id.toString()))
        db.delete("listings", "id=?", arrayOf(id.toString()))
    }

    // ---------- Bookings ----------
    fun book(listingId: Long, userId: Long, date: String, qty: Int, total: Double) {
        val cv = ContentValues()
        cv.put("listing_id", listingId); cv.put("user_id", userId); cv.put("date", date)
        cv.put("quantity", qty); cv.put("total", total); cv.put("status", BookingStatus.PENDING)
        writableDatabase.insert("bookings", null, cv)
    }

    fun bookings(userId: Long? = null, ownerId: Long? = null): List<Booking> {
        val where = ArrayList<String>()
        val args = ArrayList<String>()
        if (userId != null) { where.add("b.user_id=?"); args.add(userId.toString()) }
        if (ownerId != null) { where.add("l.owner_id=?"); args.add(ownerId.toString()) }
        val sql = "SELECT b.id,b.listing_id,l.name,l.type,b.user_id,u.name,b.date,b.quantity,b.total,b.status " +
            "FROM bookings b JOIN listings l ON l.id=b.listing_id JOIN users u ON u.id=b.user_id" +
            (if (where.isEmpty()) "" else " WHERE " + where.joinToString(" AND ")) + " ORDER BY b.id DESC"
        val out = ArrayList<Booking>()
        readableDatabase.rawQuery(sql, args.toTypedArray()).use { c ->
            while (c.moveToNext()) {
                out.add(Booking(c.getLong(0), c.getLong(1), c.getString(2), c.getString(3), c.getLong(4), c.getString(5),
                    c.getString(6) ?: "", c.getInt(7), c.getDouble(8), c.getString(9) ?: ""))
            }
        }
        return out
    }

    fun setBookingStatus(id: Long, status: String) {
        val cv = ContentValues()
        cv.put("status", status)
        writableDatabase.update("bookings", cv, "id=?", arrayOf(id.toString()))
    }
}
