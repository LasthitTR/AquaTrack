package com.sutakip.app

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class DayRecord(
    val dateStr: String,
    val amount: Double,
    val goal: Double
)

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("su_takip_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CURRENT_WATER = "current_water"
        private const val KEY_DAILY_GOAL = "daily_goal"
        private const val KEY_RECENT_AMOUNTS = "recent_amounts"
        private const val KEY_HISTORY = "history"
    }

    // Günlük su miktarı (Litre)
    var currentWater: Double
        get() = prefs.getFloat(KEY_CURRENT_WATER, 0.0f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_CURRENT_WATER, value.toFloat()).apply()

    // Günlük hedef (Litre - varsayılan 2.5 L)
    var dailyGoal: Double
        get() = prefs.getFloat(KEY_DAILY_GOAL, 2.5f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_DAILY_GOAL, value.toFloat()).apply()

    // Son kullanılan 3 hızlı ekleme miktarı
    fun getRecentAmounts(): List<Double> {
        val raw = prefs.getString(KEY_RECENT_AMOUNTS, null)
        if (raw.isNullOrEmpty()) {
            return listOf(0.25, 0.50, 1.00)
        }
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<Double>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getDouble(i))
            }
            if (list.isEmpty()) listOf(0.25, 0.50, 1.00) else list
        } catch (e: Exception) {
            listOf(0.25, 0.50, 1.00)
        }
    }

    fun saveRecentAmounts(amounts: List<Double>) {
        val jsonArray = JSONArray()
        amounts.take(3).forEach { jsonArray.put(it) }
        prefs.edit().putString(KEY_RECENT_AMOUNTS, jsonArray.toString()).apply()
    }

    fun addRecentAmount(amount: Double) {
        val current = getRecentAmounts().toMutableList()
        // Aynı miktar varsa önce çıkarılır, listenin en başına eklenir (böylece tekrar etmez)
        current.removeAll { Math.abs(it - amount) < 0.001 }
        current.add(0, amount)
        saveRecentAmounts(current.take(3))
    }

    // Tamamlanmış günlerin geçmişi
    fun getHistory(): List<DayRecord> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<DayRecord>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    DayRecord(
                        dateStr = obj.optString("date", ""),
                        amount = obj.optDouble("amount", 0.0),
                        goal = obj.optDouble("goal", 2.5)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addCompletedDay(record: DayRecord) {
        val history = getHistory().toMutableList()
        history.add(record)
        val jsonArray = JSONArray()
        for (item in history) {
            val obj = JSONObject()
            obj.put("date", item.dateStr)
            obj.put("amount", item.amount)
            obj.put("goal", item.goal)
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
    }
}

