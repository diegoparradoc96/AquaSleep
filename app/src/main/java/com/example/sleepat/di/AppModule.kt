package com.example.sleepat.di

import android.content.Context
import com.example.sleepat.domain.manager.DeviceAdminManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDeviceAdminManager(@ApplicationContext context: Context): DeviceAdminManager {
        return DeviceAdminManager(context)
    }
}
