package ru.urfu.cryptoapp.data.notification

import android.Manifest
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
import ru.urfu.cryptoapp.R
import ru.urfu.cryptoapp.domain.repository.LectureReminderScheduler

private const val EXTRA_OWNER_NAME = "owner_name"
private const val CHANNEL_ID = "lecture_reminders"
private const val NOTIFICATION_ID = 1
private const val ALARM_REQUEST_CODE = 1
private const val OPEN_APP_REQUEST_CODE = 2

class LectureReminderSchedulerImpl(context: Context) : LectureReminderScheduler {

    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)

    override fun schedule(triggerAtMillis: Long, ownerName: String) {
        // Имя кладём в интент: в уведомлении нужно имя на момент установки напоминания
        val alarm = alarmIntent().putExtra(EXTRA_OWNER_NAME, ownerName).toPendingBroadcast()
        // На Android 12 доступ к точным будильникам можно отозвать в настройках — тогда сработает с небольшой задержкой
        val canBeExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (canBeExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, alarm)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, alarm)
        }
    }

    override fun cancel() {
        alarmManager.cancel(alarmIntent().toPendingBroadcast())
    }

    private fun alarmIntent() = Intent(appContext, LectureReminderReceiver::class.java)

    private fun Intent.toPendingBroadcast(): PendingIntent = PendingIntent.getBroadcast(
        appContext,
        ALARM_REQUEST_CODE,
        this,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

/** Получает сигнал от AlarmManager и показывает уведомление. */
class LectureReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationsAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!notificationsAllowed) return

        createChannel(context)
        val ownerName = intent.getStringExtra(EXTRA_OWNER_NAME).orEmpty()
        val text = if (ownerName.isBlank()) {
            context.getString(R.string.reminder_text_no_name)
        } else {
            context.getString(R.string.reminder_text, ownerName)
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_schedule)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openAppIntent(context))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /**
     * Открывает приложение. Если оно уже запущено, CLEAR_TOP + SINGLE_TOP возвращают открытую Activity
     * с текущим экраном: без них система создала бы поверх неё вторую, с пустой навигацией.
     */
    private fun openAppIntent(context: Context): PendingIntent = PendingIntent.getActivity(
        context,
        OPEN_APP_REQUEST_CODE,
        context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
