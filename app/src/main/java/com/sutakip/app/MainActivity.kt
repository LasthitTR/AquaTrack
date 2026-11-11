package com.sutakip.app

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.sutakip.app.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PreferenceManager(this)

        setupTabs()
        setupListeners()
        refreshAll()
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.layoutDailyView.visibility = View.VISIBLE
                        binding.layoutStatsView.visibility = View.GONE
                        refreshDailyUI()
                    }
                    1 -> {
                        binding.layoutDailyView.visibility = View.GONE
                        binding.layoutStatsView.visibility = View.VISIBLE
                        refreshStatsUI()
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupListeners() {
        // Hedef düzenleme butonu
        binding.btnDailyGoalEdit.setOnClickListener {
            showGoalDialog()
        }

        // Özel miktar ekleme butonu
        binding.btnCustomAdd.setOnClickListener {
            showAddWaterDialog()
        }

        // Bir önceki işlemi geri alma butonu
        binding.btnUndo.setOnClickListener {
            undoLastIntake()
        }

        // Günü bitir butonu
        binding.btnFinishDay.setOnClickListener {
            showFinishDayConfirmation()
        }
    }

    private fun refreshAll() {
        refreshDailyUI()
        refreshQuickButtons()
        refreshStatsUI()
    }

    private fun refreshDailyUI() {
        val current = prefs.currentWater
        val goal = prefs.dailyGoal

        binding.btnDailyGoalEdit.text = String.format(Locale.getDefault(), "Hedef: %.2f L", goal)
        binding.tvWaterAmount.text = String.format(Locale.getDefault(), "%.2f / %.2f L", current, goal)

        val percentage = if (goal > 0) ((current / goal) * 100).toInt() else 0
        binding.tvPercentage.text = "%$percentage tamamlandı"
        binding.progressBarWater.progress = percentage.coerceAtMost(100)

        // Hedefe ulaşıldı mesajı
        if (current >= goal && goal > 0) {
            binding.cardCelebration.visibility = View.VISIBLE
        } else {
            binding.cardCelebration.visibility = View.GONE
        }

        // Geri alma butonu durumu: Eğer bugün içim kaydı yoksa pasif/aktif durumu
        val intakes = prefs.getTodayIntakes()
        binding.btnUndo.isEnabled = intakes.isNotEmpty()
        binding.btnUndo.alpha = if (intakes.isNotEmpty()) 1.0f else 0.5f
    }

    private fun refreshQuickButtons() {
        val recent = prefs.getRecentAmounts()
        val buttons = listOf(binding.btnQuick1, binding.btnQuick2, binding.btnQuick3)

        for (i in buttons.indices) {
            if (i < recent.size) {
                val amt = recent[i]
                buttons[i].visibility = View.VISIBLE
                buttons[i].text = String.format(Locale.getDefault(), "+%.2f L", amt)
                buttons[i].setOnClickListener {
                    addWater(amt)
                }
            } else {
                buttons[i].visibility = View.GONE
            }
        }
    }

    private fun addWater(amount: Double) {
        if (amount <= 0) return
        val newAmount = prefs.currentWater + amount
        prefs.currentWater = newAmount
        prefs.addRecentAmount(amount)

        // Bugünkü saatlik içim listesine kaydet
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeStr = timeFormat.format(Date())
        prefs.addTodayIntake(WaterIntakeRecord(timeStr = timeStr, amount = amount))

        refreshDailyUI()
        refreshQuickButtons()
        Toast.makeText(this, String.format(Locale.getDefault(), "+%.2f L su eklendi", amount), Toast.LENGTH_SHORT).show()
    }

    private fun undoLastIntake() {
        val lastIntake = prefs.removeLastIntake()
        if (lastIntake != null) {
            val updated = (prefs.currentWater - lastIntake.amount).coerceAtLeast(0.0)
            prefs.currentWater = updated
            refreshDailyUI()
            Toast.makeText(
                this,
                String.format(Locale.getDefault(), "Geri alındı: -%.2f L (%s)", lastIntake.amount, lastIntake.timeStr),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(this, "Geri alınacak bir işlem yok!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddWaterDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Su Ekle")
        builder.setMessage("Eklenecek miktarı Litre olarak girin (örn. 0.33):")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.hint = "Örn: 0.35"

        val container = LinearLayout(this).apply {
            setPadding(60, 20, 60, 20)
            addView(input)
        }
        builder.setView(container)

        builder.setPositiveButton("Ekle") { dialog, _ ->
            val text = input.text.toString().trim().replace(',', '.')
            val amt = text.toDoubleOrNull()
            if (amt != null && amt > 0) {
                addWater(amt)
            } else {
                Toast.makeText(this, "Geçersiz miktar!", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("İptal") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun showGoalDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Günlük Hedef Belirle")
        builder.setMessage("Yeni günlük su hedefinizi Litre olarak girin:")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.setText(String.format(Locale.US, "%.2f", prefs.dailyGoal))

        val container = LinearLayout(this).apply {
            setPadding(60, 20, 60, 20)
            addView(input)
        }
        builder.setView(container)

        builder.setPositiveButton("Kaydet") { dialog, _ ->
            val text = input.text.toString().trim().replace(',', '.')
            val goal = text.toDoubleOrNull()
            if (goal != null && goal > 0) {
                prefs.dailyGoal = goal
                refreshDailyUI()
                Toast.makeText(this, "Hedef güncellendi", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Geçersiz hedef!", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("İptal") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun showFinishDayConfirmation() {
        val current = prefs.currentWater
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Günü Bitir")
        builder.setMessage(
            String.format(
                Locale.getDefault(),
                "Bugünkü toplam tüketiminiz: %.2f L\n\nGünü bitirmek istediğinizden emin misiniz? Gün haftalık istatistiklere kaydedilecek ve günlük saatlik döküm sıfırlanacaktır.",
                current
            )
        )

        builder.setPositiveButton("Evet, Günü Bitir") { dialog, _ ->
            finishDay()
            dialog.dismiss()
        }

        builder.setNegativeButton("Vazgeç") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun finishDay() {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val dateStr = dateFormat.format(Date())

        val record = DayRecord(
            dateStr = dateStr,
            amount = prefs.currentWater,
            goal = prefs.dailyGoal
        )
        prefs.addCompletedDay(record)

        // Yeni gün sıfırdan başlar
        prefs.currentWater = 0.0

        // Günlük içim listesi gün bitiminde silinir (kullanıcı talebi)
        prefs.clearTodayIntakes()

        refreshDailyUI()
        refreshStatsUI()
        Toast.makeText(this, "Gün tamamlandı ve kaydedildi! Yeni gün başladı.", Toast.LENGTH_LONG).show()
    }

    private fun refreshStatsUI() {
        // 1. Günlük İstatistikler (Saat Kaçta Ne Kadar İçildi)
        val todayIntakes = prefs.getTodayIntakes()
        val todayTotal = prefs.currentWater
        binding.tvTodayStatsTotal.text = String.format(Locale.getDefault(), "%.2f L", todayTotal)
        binding.tvTodayStatsCount.text = "${todayIntakes.size} kez"

        if (todayIntakes.isNotEmpty()) {
            val sb = StringBuilder()
            todayIntakes.reversed().forEachIndexed { index, intake ->
                sb.append("• Saat ${intake.timeStr} ➔ +${String.format(Locale.getDefault(), "%.2f L", intake.amount)}\n")
            }
            binding.tvTodayIntakeList.text = sb.toString().trimEnd()
        } else {
            binding.tvTodayIntakeList.text = "Bugün henüz su girişi yapılmadı."
        }

        // 2. Haftalık İstatistikler (Son 7 Tamamlanmış Gün)
        val history = prefs.getHistory()
        val last7 = history.takeLast(7)
        if (last7.isNotEmpty()) {
            val total7 = last7.sumOf { it.amount }
            val avg7 = total7 / last7.size
            binding.tvWeeklyTotal.text = String.format(Locale.getDefault(), "%.2f L", total7)
            binding.tvWeeklyAverage.text = String.format(Locale.getDefault(), "%.2f L", avg7)

            // Grafik verisi (etiket: G1, G2...)
            val chartData = last7.mapIndexed { index, record ->
                val label = "G${index + 1}"
                Pair(label, record.amount)
            }
            binding.weeklyBarChart.visibility = View.VISIBLE
            binding.weeklyBarChart.setData(chartData, prefs.dailyGoal)

            // Günlük tüketim dökümü
            val breakdown = StringBuilder()
            last7.reversed().forEach { record ->
                val percent = if (record.goal > 0) ((record.amount / record.goal) * 100).toInt() else 0
                breakdown.append("• ${record.dateStr}: ${String.format(Locale.getDefault(), "%.2f L", record.amount)} (%$percent)\n")
            }
            binding.tvWeeklyBreakdown.text = breakdown.toString().trimEnd()
        } else {
            binding.tvWeeklyTotal.text = "0.00 L"
            binding.tvWeeklyAverage.text = "0.00 L"
            binding.weeklyBarChart.visibility = View.GONE
            binding.tvWeeklyBreakdown.text = "Henüz tamamlanmış gün kaydı yok.\n'Günü Bitir' butonuna bastığınızda burada listelenecektir."
        }
    }
}
