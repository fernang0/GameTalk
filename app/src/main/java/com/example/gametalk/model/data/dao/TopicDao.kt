package com.example.gametalk.model.data.dao

import androidx.room.*
import com.example.gametalk.model.data.entities.Topic
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics ORDER BY lastActivity DESC")
    fun getAllTopics(): Flow<List<Topic>>

    @Query("SELECT * FROM topics WHERE categoryId = :categoryId ORDER BY lastActivity DESC")
    fun getTopicsByCategory(categoryId: Int): Flow<List<Topic>>

    @Query("SELECT * FROM topics WHERE id = :topicId")
    suspend fun getTopicById(topicId: Int): Topic?

    @Query("SELECT * FROM topics WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTopicsByUser(userId: Int): Flow<List<Topic>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: Topic): Long

    @Update
    suspend fun updateTopic(topic: Topic)

    @Delete
    suspend fun deleteTopic(topic: Topic)

    @Query("DELETE FROM topics WHERE id = :topicId")
    suspend fun deleteTopicById(topicId: Int)

    @Query("SELECT COUNT(*) FROM topics WHERE categoryId = :categoryId")
    suspend fun getTopicsCountByCategory(categoryId: Int): Int
}
