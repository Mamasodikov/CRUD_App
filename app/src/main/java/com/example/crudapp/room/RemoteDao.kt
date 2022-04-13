package com.example.crudapp.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.crudapp.models.RemoteKey

@Dao
interface RemoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(remoteKey:List<RemoteKey>)

    @Query("SELECT * FROM remote_key WHERE id = :id")
    suspend fun remoteKeyByQuery(id: Int): RemoteKey

    @Query("DELETE FROM remote_key")
    suspend fun deleteByQuery()
}