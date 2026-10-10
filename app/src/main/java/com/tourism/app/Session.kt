package com.tourism.app

import android.content.Context

object Session {
    private const val FILE = "session"

    fun save(ctx: Context, u: User) {
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putLong("uid", u.id).apply()
    }

    fun current(ctx: Context): User? {
        val id = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).getLong("uid", -1L)
        return if (id < 0) null else Db.get(ctx).getUser(id)
    }

    fun clear(ctx: Context) {
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
