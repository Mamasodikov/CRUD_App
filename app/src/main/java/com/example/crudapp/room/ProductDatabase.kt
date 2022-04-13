package com.example.crudapp.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.crudapp.models.Product
import com.example.crudapp.models.RemoteKey

@Database(
    entities = [Product::class, RemoteKey::class],
    version = 1,
    exportSchema = false
)

abstract class ProductDatabase : RoomDatabase() {
    abstract fun productsDao(): ProductsDao
    abstract fun remoteDao(): RemoteDao

    companion object {
        @Volatile
        private var instance: ProductDatabase? = null
        private val lock = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(lock) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            ProductDatabase::class.java,
            "products"
        ).build()

    }

}