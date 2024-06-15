package ru.practicum.android.diploma.di

import android.content.Context
import androidx.room.Room
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.practicum.android.diploma.BuildConfig
import ru.practicum.android.diploma.core.data.NetworkClient
import ru.practicum.android.diploma.core.data.network.ConnectionChecker
import ru.practicum.android.diploma.core.data.network.ConnectionCheckerImpl
import ru.practicum.android.diploma.core.data.network.HhApi
import ru.practicum.android.diploma.core.data.network.RetrofitNetworkClient
import ru.practicum.android.diploma.core.data.storage.sharedpreferences.FilterStorage
import ru.practicum.android.diploma.core.data.storage.sharedpreferences.FilterStorageImpl
import ru.practicum.android.diploma.favourites.data.db.AppDatabase
import ru.practicum.android.diploma.vacancy.data.ExternalNavigatorImpl
import ru.practicum.android.diploma.vacancy.domain.api.ExternalNavigator

val dataModule = module {

    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "database.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    factory<ExternalNavigator> {
        ExternalNavigatorImpl(context = androidContext())
    }

    single<NetworkClient> {
        RetrofitNetworkClient(hhApi = get(), connectionChecker = get())
    }

    single<ConnectionChecker> {
        ConnectionCheckerImpl(context = androidContext())
    }
    single<HhApi> {
        val interceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            interceptor.level = HttpLoggingInterceptor.Level.BODY
        }
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(get())
            .build()
        Retrofit.Builder()
            .baseUrl("https://api.hh.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create()
    }

    single<FilterStorage> {
        FilterStorageImpl(
            sharedPreferences = androidContext().getSharedPreferences("Filter", Context.MODE_PRIVATE)
        )
    }
    single {
        Interceptor { chain ->
            chain.run {
                proceed(
                    request()
                        .newBuilder()
                        .addHeader("Authorization", "Bearer ${BuildConfig.HH_ACCESS_TOKEN}")
                        .addHeader(
                            "HH-User-Agent",
                            "JobSeeker/${BuildConfig.VERSION_NAME} (${BuildConfig.DEVELOPER_EMAIL})"
                        )
                        .build()
                )
            }
        }
    }
}
