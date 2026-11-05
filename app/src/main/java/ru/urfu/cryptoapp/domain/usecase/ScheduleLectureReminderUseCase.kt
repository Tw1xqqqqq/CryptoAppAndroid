package ru.urfu.cryptoapp.domain.usecase

import ru.urfu.cryptoapp.domain.model.LectureTime
import ru.urfu.cryptoapp.domain.repository.LectureReminderScheduler
import java.util.Calendar

/**
 * Ставит напоминание на указанное время сегодняшнего дня; прежнее напоминание при этом заменяется.
 * Если это время уже прошло, напоминание снимается: AlarmManager сработал бы сразу же.
 */
class ScheduleLectureReminderUseCase(
    private val scheduler: LectureReminderScheduler,
    private val now: () -> Long = System::currentTimeMillis,
) {
    /** @return false — время сегодня уже прошло, напоминания не будет */
    operator fun invoke(time: LectureTime, ownerName: String): Boolean {
        val currentMillis = now()
        val triggerAtMillis = Calendar.getInstance().apply {
            timeInMillis = currentMillis
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (triggerAtMillis <= currentMillis) {
            scheduler.cancel()
            return false
        }
        scheduler.schedule(triggerAtMillis, ownerName)
        return true
    }
}
