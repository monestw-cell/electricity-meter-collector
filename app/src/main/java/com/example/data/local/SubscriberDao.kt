package com.example.data.local

import androidx.room.*
import com.example.data.model.Subscriber
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriberDao {
    @Query("SELECT * FROM subscribers ORDER BY name ASC")
    fun getAllSubscribers(): Flow<List<Subscriber>>

    @Query("SELECT * FROM subscribers WHERE id = :id")
    suspend fun getSubscriberByIdSync(id: Int): Subscriber?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriber(subscriber: Subscriber)

    @Update
    suspend fun updateSubscriber(subscriber: Subscriber)

    @Delete
    suspend fun deleteSubscriber(subscriber: Subscriber)
}
