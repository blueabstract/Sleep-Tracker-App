package com.example.sleeptracker

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    // Views
    private lateinit var chronometer:     Chronometer
    private lateinit var btnStart:        Button
    private lateinit var btnStop:         Button
    private lateinit var btnReset:        Button
    private lateinit var btnSettings:     Button
    private lateinit var tvSleepStage:    TextView
    private lateinit var tvStageEmoji:    TextView
    private lateinit var tvGoalStatus:    TextView
    private lateinit var tvAvgSleep:      TextView
    private lateinit var tvLastSleep:     TextView
    private lateinit var tvTotalSessions: TextView
    private lateinit var cardSleepStage:  android.view.View
    private lateinit var progressSleep:   ProgressBar
    private lateinit var prefs:           SharedPreferences

    // State
    private var isTracking         = false
    private var startTimeMillis    = 0L
    private var elapsedBeforePause = 0L
    private var goalHours          = 8
    private var goalMinutes        = 0

    // Stage updater — runs every 1 second
    private val handler = Handler(Looper.getMainLooper())
    private val stageRunnable = object : Runnable {
        override fun run() {
            if (isTracking) {
                updateSleepStage()
                updateProgressBar()
                handler.postDelayed(this, 1_000L)  // ← 1 second
            }
        }
    }

    companion object {
        const val PREFS_NAME       = "SleepTrackerPrefs"
        const val KEY_SESSIONS     = "sleep_sessions"
        const val KEY_GOAL_HOURS   = "goal_hours"
        const val KEY_GOAL_MINUTES = "goal_minutes"
        const val NREM_MS = 5L  * 1000   // 5 sec  → triggers Light Sleep
        const val DEEP_MS = 10L * 1000   // 10 sec → triggers Deep Sleep
        const val REM_MS  = 15L * 1000   // 15 sec → triggers REM Sleep
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = "\uD83D\uDCA4 Sleep Tracker by Subarno"

        prefs       = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        goalHours   = prefs.getInt(KEY_GOAL_HOURS, 8)
        goalMinutes = prefs.getInt(KEY_GOAL_MINUTES, 0)

        bindViews()
        setupClicks()
        refreshSummary()
        tvGoalStatus.text = "Goal: ${formatGoal()}"
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(stageRunnable)
    }

    // ── View Setup ────────────────────────────────────────────────────────────

    private fun bindViews() {
        chronometer     = findViewById(R.id.chronometer)
        btnStart        = findViewById(R.id.btnStart)
        btnStop         = findViewById(R.id.btnStop)
        btnReset        = findViewById(R.id.btnReset)
        btnSettings     = findViewById(R.id.btnSettings)
        tvSleepStage    = findViewById(R.id.tvSleepStage)
        tvStageEmoji    = findViewById(R.id.tvStageEmoji)
        tvGoalStatus    = findViewById(R.id.tvGoalStatus)
        tvAvgSleep      = findViewById(R.id.tvAvgSleep)
        tvLastSleep     = findViewById(R.id.tvLastSleep)
        tvTotalSessions = findViewById(R.id.tvTotalSessions)
        cardSleepStage  = findViewById(R.id.cardSleepStage)
        progressSleep   = findViewById(R.id.progressSleep)

        btnStop.isEnabled  = false
        btnReset.isEnabled = false
        setStage("Awaiting Sleep", "🌙", "#3D5A80")
    }

    private fun setupClicks() {
        btnStart.setOnClickListener    { startTracking() }
        btnStop.setOnClickListener     { stopTracking()  }
        btnReset.setOnClickListener    { confirmReset()  }
        btnSettings.setOnClickListener { showSettings()  }
    }

    // ── Tracking ──────────────────────────────────────────────────────────────

    private fun startTracking() {
        isTracking      = true
        startTimeMillis = System.currentTimeMillis()

        chronometer.base = SystemClock.elapsedRealtime() - elapsedBeforePause
        chronometer.start()

        btnStart.isEnabled = false
        btnStop.isEnabled  = true
        btnReset.isEnabled = false

        setStage("Falling Asleep…", "😴", "#293241")
        handler.post(stageRunnable)
        toast("Sleep tracking started!")
    }

    private fun stopTracking() {
        if (!isTracking) { toast("Tracking hasn't started!"); return }

        isTracking = false
        chronometer.stop()

        val duration = elapsedBeforePause + (System.currentTimeMillis() - startTimeMillis)
        elapsedBeforePause = 0L

        saveSession(duration)
        refreshSummary()
        showSummaryDialog(duration)

        btnStart.isEnabled = true
        btnStop.isEnabled  = false
        btnReset.isEnabled = true

        setStage("Session Complete", "✅", "#3D5A80")
        handler.removeCallbacks(stageRunnable)
    }

    private fun confirmReset() {
        AlertDialog.Builder(this)
            .setTitle("Reset Session")
            .setMessage("Clear current session and reset the timer?")
            .setPositiveButton("Reset") { _, _ ->
                isTracking         = false
                startTimeMillis    = 0L
                elapsedBeforePause = 0L

                chronometer.stop()
                chronometer.base       = SystemClock.elapsedRealtime()
                progressSleep.progress = 0

                btnStart.isEnabled = true
                btnStop.isEnabled  = false
                btnReset.isEnabled = false
                tvGoalStatus.text  = "Goal: ${formatGoal()}"

                setStage("Awaiting Sleep", "🌙", "#3D5A80")
                handler.removeCallbacks(stageRunnable)
                toast("Session reset.")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Sleep Stages ─────────────────────────────────────────────────────────

    private fun updateSleepStage() {
        val elapsed = System.currentTimeMillis() - startTimeMillis + elapsedBeforePause
        when {
            elapsed < NREM_MS -> setStage("Light Sleep", "💤", "#293241")
            elapsed < DEEP_MS -> setStage("NREM Sleep",  "🌊", "#1B4332")
            elapsed < REM_MS  -> setStage("Deep Sleep",  "🌑", "#10002B")
            else              -> setStage("REM Sleep",   "🌈", "#240046")
        }
    }

    private fun setStage(label: String, emoji: String, hex: String) {
        tvSleepStage.text = label
        tvStageEmoji.text = emoji
        try { cardSleepStage.setBackgroundColor(Color.parseColor(hex)) }
        catch (_: IllegalArgumentException) {}
    }

    private fun updateProgressBar() {
        val goalMs = (goalHours * 3600L + goalMinutes * 60L) * 1000L
        if (goalMs == 0L) return
        val elapsed  = System.currentTimeMillis() - startTimeMillis + elapsedBeforePause
        val pct      = ((elapsed.toFloat() / goalMs) * 100).coerceIn(0f, 100f).toInt()
        progressSleep.progress = pct
        val remaining = goalMs - elapsed
        tvGoalStatus.text = if (remaining > 0)
            "Goal: ${formatMs(remaining)} remaining"
        else
            "🎉 Sleep goal reached!"
    }

    // ── Data Persistence ─────────────────────────────────────────────────────

    private fun saveSession(ms: Long) {
        val date     = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val sessions = getSessions().toMutableList().also { it.add("$date|$ms") }
        val trimmed  = if (sessions.size > 30) sessions.takeLast(30) else sessions
        prefs.edit().putString(KEY_SESSIONS, trimmed.joinToString(";")).apply()
    }

    private fun getSessions(): List<String> {
        val raw = prefs.getString(KEY_SESSIONS, "") ?: ""
        return raw.split(";").filter { it.isNotBlank() }
    }

    private fun getDurations(): List<Long> =
        getSessions().mapNotNull { it.split("|").getOrNull(1)?.toLongOrNull() }

    private fun refreshSummary() {
        val d = getDurations()
        tvAvgSleep.text      = if (d.isEmpty()) "Avg Sleep: —" else "Avg: ${formatMs(d.average().toLong())}"
        tvLastSleep.text     = if (d.isEmpty()) "Last: —"      else "Last: ${formatMs(d.last())}"
        tvTotalSessions.text = "Sessions: ${d.size}"
    }

    // ── Settings & Dialogs ────────────────────────────────────────────────────

    private fun showSettings() {
        val view        = layoutInflater.inflate(R.layout.dialog_settings, null)
        val tvGoalLabel = view.findViewById<TextView>(R.id.tvCurrentGoal)
        val btnPick     = view.findViewById<Button>(R.id.btnPickTime)
        val btnClear    = view.findViewById<Button>(R.id.btnClearData)

        tvGoalLabel.text = "Current Goal: ${formatGoal()}"

        btnPick.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                goalHours   = h
                goalMinutes = m
                prefs.edit()
                    .putInt(KEY_GOAL_HOURS, h)
                    .putInt(KEY_GOAL_MINUTES, m)
                    .apply()
                tvGoalLabel.text  = "Current Goal: ${formatGoal()}"
                tvGoalStatus.text = "Goal: ${formatGoal()}"
                toast("Goal set to ${formatGoal()}")
            }, goalHours, goalMinutes, true).show()
        }

        btnClear.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear All Data")
                .setMessage("Delete all sleep history?")
                .setPositiveButton("Clear") { _, _ ->
                    prefs.edit().remove(KEY_SESSIONS).apply()
                    refreshSummary()
                    toast("History cleared.")
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        AlertDialog.Builder(this)
            .setTitle("⚙️ Settings")
            .setView(view)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showSummaryDialog(ms: Long) {
        val goalMs = (goalHours * 3600L + goalMinutes * 60L) * 1000L
        val diff   = ms - goalMs
        val msg    = when {
            goalMs == 0L -> "No goal set."
            diff >= 0    -> "You slept ${formatMs(abs(diff))} MORE than your goal! 🎉"
            else         -> "You slept ${formatMs(abs(diff))} LESS than your goal."
        }
        AlertDialog.Builder(this)
            .setTitle("Session Complete")
            .setMessage("Duration: ${formatMs(ms)}\n\n$msg\n\nSaved to history!")
            .setPositiveButton("OK", null)
            .show()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun formatMs(ms: Long): String {
        if (ms <= 0) return "0m"
        val totalSec = ms / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0          -> "${h}h"
            m > 0          -> "${m}m"
            else           -> "${s}s"   // shows seconds for short test sessions
        }
    }

    private fun formatGoal() =
        "${goalHours}h ${goalMinutes.toString().padStart(2, '0')}m"

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    // ── Menu ──────────────────────────────────────────────────────────────────

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            showSettings()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}