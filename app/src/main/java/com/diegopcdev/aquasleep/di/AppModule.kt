package com.diegopcdev.aquasleep.di

import android.content.Context
import com.diegopcdev.aquasleep.domain.manager.MediaController
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
    fun provideMediaController(@ApplicationContext context: Context): MediaController {
        return MediaController(context)
    }
}
