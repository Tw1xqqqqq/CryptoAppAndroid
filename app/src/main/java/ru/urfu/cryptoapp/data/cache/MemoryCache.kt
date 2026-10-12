package ru.urfu.cryptoapp.data.cache

import java.util.concurrent.ConcurrentHashMap

/**
 * Простой потокобезопасный кэш в памяти с временем жизни записей.
 * Устаревшие записи не удаляются: их можно показать, пока грузятся свежие данные,
 * или если сеть недоступна.
 */
class MemoryCache<K : Any, V : Any>(
    private val ttlMillis: Long,
    private val now: () -> Long = System::currentTimeMillis,
) {
    class Entry<V>(val value: V, val isFresh: Boolean)

    private val entries = ConcurrentHashMap<K, Pair<V, Long>>()

    operator fun get(key: K): Entry<V>? =
        entries[key]?.let { (value, savedAt) -> Entry(value, now() - savedAt < ttlMillis) }

    operator fun set(key: K, value: V) {
        entries[key] = value to now()
    }
}
