package com.example.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.localization.AppLanguage
import java.util.Calendar

object NotificationHelper {
    const val CHANNEL_ID = "astranova_daily_affirmations"
    private const val PREFS_NAME = "astranova_prefs"
    private const val KEY_ENABLED = "notif_enabled"
    private const val KEY_HOUR = "notif_hour"
    private const val KEY_MINUTE = "notif_minute"
    private const val KEY_LATEST_AFFIRMATION = "latest_affirmation_text"
    private const val KEY_USER_NAME = "latest_user_name"
    private const val KEY_ZODIAC_SIGN = "latest_zodiac_sign"
    private const val REQUEST_CODE = 2048
    private const val NOTIFICATION_ID = 9001

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val desc = context.getString(R.string.notification_channel_desc)
            val channel = NotificationChannel(
                CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = desc
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun cacheAffirmationPayload(
        context: Context,
        affirmationText: String,
        userName: String,
        zodiacSign: String
    ) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LATEST_AFFIRMATION, affirmationText)
            .putString(KEY_USER_NAME, userName)
            .putString(KEY_ZODIAC_SIGN, zodiacSign)
            .apply()
    }

    fun scheduleDailyAffirmation(context: Context, hour: Int, minute: Int) {
        val appContext = context.applicationContext
        ensureChannel(appContext)
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_ENABLED, true)
            .putInt(KEY_HOUR, hour)
            .putInt(KEY_MINUTE, minute)
            .apply()

        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(appContext, DailyAffirmationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelDailyAffirmation(context: Context) {
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ENABLED, false).apply()

        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(appContext, DailyAffirmationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun rescheduleIfEnabled(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_ENABLED, false)) {
            val hour = prefs.getInt(KEY_HOUR, 8)
            val minute = prefs.getInt(KEY_MINUTE, 0)
            scheduleDailyAffirmation(context, hour, minute)
        }
    }

    fun showAffirmationNotification(context: Context, customBody: String? = null): Boolean {
        val appContext = context.applicationContext
        ensureChannel(appContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                appContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }

        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userName = prefs.getString(KEY_USER_NAME, "")?.takeIf { it.isNotBlank() }
        val sign = prefs.getString(KEY_ZODIAC_SIGN, "")?.takeIf { it.isNotBlank() }
        val langCode = prefs.getString("preferred_language", AppLanguage.ENGLISH.code)
        val lang = AppLanguage.fromCode(langCode)

        val defaultMessage = when (lang) {
            AppLanguage.HINDI -> "आज ब्रह्मांडीय ऊर्जा आपके साथ है। शांति और विश्वास के साथ आगे बढ़ें।"
            AppLanguage.TAMIL -> "இன்று பிரபஞ்ச ஆற்றல் உங்களுடன் உள்ளது. அமைதியுடனும் நம்பிக்கையுடனும் முன்னேறுங்கள்."
            AppLanguage.BENGALI -> "আজ মহাজাগতিক শক্তি আপনার সাথে আছে। শান্তি ও আত্মবিশ্বাসের সাথে এগিয়ে যান।"
            AppLanguage.MARATHI -> "आज वैश्विक ऊर्जा तुमच्या सोबत आहे. शांतता आणि विश्वासाने पुढे जा."
            AppLanguage.TELUGU -> "ఈరోజు విశ్వశక్తి మీకు తోడుగా ఉంది. ప్రశాంతత మరియు ఆత్మవిశ్వాసంతో ముందుకు సాగండి."
            AppLanguage.SPANISH -> "La energía cósmica te acompaña hoy. Avanza con serenidad y confianza."
            AppLanguage.FRENCH -> "L'énergie cosmique vous accompagne aujourd'hui. Avancez avec calme et confiance."
            AppLanguage.ENGLISH -> "Today's stars align with your inner strength. Move forward with clarity and grace."
        }

        val body = customBody
            ?: prefs.getString(KEY_LATEST_AFFIRMATION, null)?.takeIf { it.isNotBlank() }
            ?: defaultMessage

        val title = buildString {
            append("AstraNova")
            if (userName != null) append(" • $userName")
            if (sign != null) append(" ($sign)")
        }

        val launchIntent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            appContext,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(appContext).notify(NOTIFICATION_ID, notification)
        return true
    }
}

class DailyAffirmationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        NotificationHelper.showAffirmationNotification(context)
    }
}

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationHelper.rescheduleIfEnabled(context)
        }
    }
}
