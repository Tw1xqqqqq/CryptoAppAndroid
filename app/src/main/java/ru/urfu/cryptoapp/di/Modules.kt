package ru.urfu.cryptoapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.chuckerteam.chucker.api.ChuckerInterceptor
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import ru.urfu.cryptoapp.BuildConfig
import ru.urfu.cryptoapp.data.local.db.AppDatabase
import ru.urfu.cryptoapp.data.local.prefs.FiltersRepositoryImpl
import ru.urfu.cryptoapp.data.local.profile.ProfileDto
import ru.urfu.cryptoapp.data.local.profile.ProfileRepositoryImpl
import ru.urfu.cryptoapp.data.local.profile.ProfileSerializer
import ru.urfu.cryptoapp.data.notification.LectureReminderSchedulerImpl
import ru.urfu.cryptoapp.data.remote.ApiKeyInterceptor
import ru.urfu.cryptoapp.data.remote.CoinGeckoApi
import ru.urfu.cryptoapp.data.repository.AvatarRepositoryImpl
import ru.urfu.cryptoapp.data.repository.CoinRepositoryImpl
import ru.urfu.cryptoapp.data.repository.DocumentRepositoryImpl
import ru.urfu.cryptoapp.data.repository.FavoritesRepositoryImpl
import ru.urfu.cryptoapp.domain.repository.AvatarRepository
import ru.urfu.cryptoapp.domain.repository.CoinRepository
import ru.urfu.cryptoapp.domain.repository.DocumentRepository
import ru.urfu.cryptoapp.domain.repository.FavoritesRepository
import ru.urfu.cryptoapp.domain.repository.FiltersRepository
import ru.urfu.cryptoapp.domain.repository.LectureReminderScheduler
import ru.urfu.cryptoapp.domain.repository.ProfileRepository
import ru.urfu.cryptoapp.domain.usecase.AddFavoriteUseCase
import ru.urfu.cryptoapp.domain.usecase.CreateCameraCaptureUriUseCase
import ru.urfu.cryptoapp.domain.usecase.DeleteUnusedAvatarsUseCase
import ru.urfu.cryptoapp.domain.usecase.DownloadResumeUseCase
import ru.urfu.cryptoapp.domain.usecase.GetCoinMarketsUseCase
import ru.urfu.cryptoapp.domain.usecase.GetCoinOverviewUseCase
import ru.urfu.cryptoapp.domain.usecase.ImportAvatarUseCase
import ru.urfu.cryptoapp.domain.usecase.NormalizeResumeUrlUseCase
import ru.urfu.cryptoapp.domain.usecase.ObserveFavoritesUseCase
import ru.urfu.cryptoapp.domain.usecase.ObserveFiltersUseCase
import ru.urfu.cryptoapp.domain.usecase.ObserveIsFavoriteUseCase
import ru.urfu.cryptoapp.domain.usecase.SaveFiltersUseCase
import ru.urfu.cryptoapp.domain.usecase.ObserveProfileUseCase
import ru.urfu.cryptoapp.domain.usecase.RemoveFavoriteUseCase
import ru.urfu.cryptoapp.domain.usecase.SaveProfileUseCase
import ru.urfu.cryptoapp.domain.usecase.ScheduleLectureReminderUseCase
import ru.urfu.cryptoapp.ui.coins.details.CoinDetailsViewModel
import ru.urfu.cryptoapp.ui.coins.list.CoinListViewModel
import ru.urfu.cryptoapp.ui.favorites.FavoritesViewModel
import ru.urfu.cryptoapp.ui.filters.FilterBadgeCache
import ru.urfu.cryptoapp.ui.filters.FiltersViewModel
import ru.urfu.cryptoapp.ui.profile.EditProfileViewModel
import ru.urfu.cryptoapp.ui.profile.ProfileViewModel
import java.util.concurrent.TimeUnit

private const val TIMEOUT_SECONDS = 15L
private const val DOWNLOAD_TIMEOUT_SECONDS = 60L

private val Context.filtersDataStore: DataStore<Preferences> by preferencesDataStore(name = "filters")

private val Context.profileDataStore: DataStore<ProfileDto> by dataStore(
    fileName = "profile.json",
    serializer = ProfileSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { ProfileSerializer.defaultValue },
)

private val BASE_CLIENT = named("base_client")
private val API_CLIENT = named("api_client")
private val DOWNLOAD_CLIENT = named("download_client")

val networkModule = module {
    single(BASE_CLIENT) {
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            // В release подключается no-op версия Chucker
            .addInterceptor(ChuckerInterceptor.Builder(androidContext()).build())
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                }
            }
            .build()
    }
    // Ключ CoinGecko добавляется только в запросы к CoinGecko
    single(API_CLIENT) {
        get<OkHttpClient>(BASE_CLIENT).newBuilder()
            .apply { interceptors().add(0, ApiKeyInterceptor(BuildConfig.COINGECKO_API_KEY)) }
            .build()
    }
    // Ссылка на резюме ведёт на произвольный сайт, а файл может быть большим
    single(DOWNLOAD_CLIENT) {
        get<OkHttpClient>(BASE_CLIENT).newBuilder()
            .readTimeout(DOWNLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
    single<CoinGeckoApi> {
        Retrofit.Builder()
            .baseUrl(BuildConfig.COINGECKO_BASE_URL)
            .client(get<OkHttpClient>(API_CLIENT))
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
            .create()
    }
}

val localModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, AppDatabase.NAME).build()
    }
    single { get<AppDatabase>().favoritesDao() }
    single { androidContext().filtersDataStore }
}

val repositoryModule = module {
    single<CoinRepository> { CoinRepositoryImpl(get()) }
    single<FavoritesRepository> { FavoritesRepositoryImpl(get()) }
    singleOf(::FiltersRepositoryImpl) bind FiltersRepository::class
    single<ProfileRepository> { ProfileRepositoryImpl(androidContext().profileDataStore) }
    single<AvatarRepository> { AvatarRepositoryImpl(androidContext()) }
    single<LectureReminderScheduler> { LectureReminderSchedulerImpl(androidContext()) }
    single<DocumentRepository> { DocumentRepositoryImpl(androidContext(), get(DOWNLOAD_CLIENT)) }
}

val useCaseModule = module {
    factoryOf(::GetCoinMarketsUseCase)
    factoryOf(::GetCoinOverviewUseCase)
    factoryOf(::ObserveFiltersUseCase)
    factoryOf(::SaveFiltersUseCase)
    factoryOf(::ObserveFavoritesUseCase)
    factoryOf(::ObserveIsFavoriteUseCase)
    factoryOf(::AddFavoriteUseCase)
    factoryOf(::RemoveFavoriteUseCase)
    factoryOf(::ObserveProfileUseCase)
    factoryOf(::SaveProfileUseCase)
    factoryOf(::NormalizeResumeUrlUseCase)
    factoryOf(::CreateCameraCaptureUriUseCase)
    factoryOf(::ImportAvatarUseCase)
    factoryOf(::DeleteUnusedAvatarsUseCase)
    factoryOf(::DownloadResumeUseCase)
    factory { ScheduleLectureReminderUseCase(get()) }
}

val presentationModule = module {
    // Один экземпляр на всё приложение — общий для экрана списка и экрана фильтров
    singleOf(::FilterBadgeCache)

    viewModelOf(::CoinListViewModel)
    viewModelOf(::CoinDetailsViewModel) // SavedStateHandle Koin подставляет сам
    viewModelOf(::FiltersViewModel)
    viewModelOf(::FavoritesViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::EditProfileViewModel)
}

val appModules = listOf(networkModule, localModule, repositoryModule, useCaseModule, presentationModule)
