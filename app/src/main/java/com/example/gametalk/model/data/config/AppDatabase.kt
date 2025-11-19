package com.example.gametalk.model.data.config

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gametalk.model.data.dao.UserDao
import com.example.gametalk.model.data.dao.CategoryDao
import com.example.gametalk.model.data.dao.TopicDao
import com.example.gametalk.model.data.entities.User
import com.example.gametalk.model.data.entities.Category
import com.example.gametalk.model.data.entities.Topic

@Database(entities = [User::class, Category::class, Topic::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun topicDao(): TopicDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gametalk_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}