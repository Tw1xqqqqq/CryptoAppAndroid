package ru.urfu.cryptoapp.feature.profile

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.urfu.cryptoapp.core.network.NetworkQualifiers
import ru.urfu.cryptoapp.feature.profile.data.local.ProfileDto
import ru.urfu.cryptoapp.feature.profile.data.local.ProfileRepositoryImpl
import ru.urfu.cryptoapp.feature.profile.data.local.ProfileSerializer
import ru.urfu.cryptoapp.feature.profile.data.notification.LectureReminderSchedulerImpl
import ru.urfu.cryptoapp.feature.profile.data.repository.AvatarRepositoryImpl
import ru.urfu.cryptoapp.feature.profile.data.repository.DocumentRepositoryImpl
import ru.urfu.cryptoapp.feature.profile.domain.repository.AvatarRepository
import ru.urfu.cryptoapp.feature.profile.domain.repository.DocumentRepository
import ru.urfu.cryptoapp.feature.profile.domain.repository.LectureReminderScheduler
import ru.urfu.cryptoapp.feature.profile.domain.repository.ProfileRepository
import ru.urfu.cryptoapp.feature.profile.domain.usecase.CreateCameraCaptureUriUseCase
import ru.urfu.cryptoapp.feature.profile.domain.usecase.DeleteUnusedAvatarsUseCase
import ru.urfu.cryptoapp.feature.profile.domain.usecase.DownloadResumeUseCase
import ru.urfu.cryptoapp.feature.profile.domain.usecase.ImportAvatarUseCase
import ru.urfu.cryptoapp.feature.profile.domain.usecase.NormalizeResumeUrlUseCase
import ru.urfu.cryptoapp.feature.profile.domain.usecase.ObserveProfileUseCase
import ru.urfu.cryptoapp.feature.profile.domain.usecase.SaveProfileUseCase
import ru.urfu.cryptoapp.feature.profile.domain.usecase.ScheduleLectureReminderUseCase
import ru.urfu.cryptoapp.feature.profile.ui.EditProfileRoute
import ru.urfu.cryptoapp.feature.profile.ui.EditProfileViewModel
import ru.urfu.cryptoapp.feature.profile.ui.ProfileRoute
import ru.urfu.cryptoapp.feature.profile.ui.ProfileViewModel

/*
 * Публичное API модуля «Профиль». Всё остальное в модуле internal:
 * app подключает Koin-модуль и граф навигации и ничего не знает о том, как профиль устроен внутри.
 */

/** Маршруты вкладки «Профиль» — для нижней навигации в app. */
object ProfileNavigation {
    const val GRAPH_ROUTE = "profile"
    const val START_ROUTE = "profile/view"
    internal const val EDIT_ROUTE = "profile/edit"
}

/** Граф вкладки: профиль -> редактирование. */
fun NavGraphBuilder.profileGraph(navController: NavController) {
    navigation(startDestination = ProfileNavigation.START_ROUTE, route = ProfileNavigation.GRAPH_ROUTE) {
        composable(ProfileNavigation.START_ROUTE) {
            ProfileRoute(onEditClick = { navController.navigate(ProfileNavigation.EDIT_ROUTE) })
        }
        composable(ProfileNavigation.EDIT_ROUTE) {
            EditProfileRoute(onClose = { navController.popBackStack() })
        }
    }
}

private val Context.profileDataStore: DataStore<ProfileDto> by dataStore(
    fileName = "profile.json",
    serializer = ProfileSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler { ProfileSerializer.defaultValue },
)

/** Зависимости профиля. Требует [ru.urfu.cryptoapp.core.network.coreNetworkModule]. */
val profileModule: Module = module {
    single<ProfileRepository> { ProfileRepositoryImpl(androidContext().profileDataStore) }
    single<AvatarRepository> { AvatarRepositoryImpl(androidContext()) }
    single<LectureReminderScheduler> { LectureReminderSchedulerImpl(androidContext()) }
    single<DocumentRepository> { DocumentRepositoryImpl(androidContext(), get(NetworkQualifiers.DOWNLOAD_CLIENT)) }

    factoryOf(::ObserveProfileUseCase)
    factoryOf(::SaveProfileUseCase)
    factoryOf(::NormalizeResumeUrlUseCase)
    factoryOf(::CreateCameraCaptureUriUseCase)
    factoryOf(::ImportAvatarUseCase)
    factoryOf(::DeleteUnusedAvatarsUseCase)
    factoryOf(::DownloadResumeUseCase)
    factory { ScheduleLectureReminderUseCase(get()) }

    viewModelOf(::ProfileViewModel)
    viewModelOf(::EditProfileViewModel)
}
