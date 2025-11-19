package com.example.gametalk.model.data.repository

import com.example.gametalk.model.data.dao.TopicDao
import com.example.gametalk.model.data.entities.Topic
import kotlinx.coroutines.flow.Flow

class TopicRepository(private val topicDao: TopicDao) {

    fun getAllTopics(): Flow<List<Topic>> {
        return topicDao.getAllTopics()
    }

    fun getTopicsByCategory(categoryId: Int): Flow<List<Topic>> {
        return topicDao.getTopicsByCategory(categoryId)
    }

    suspend fun getTopicById(topicId: Int): Topic? {
        return topicDao.getTopicById(topicId)
    }

    fun getTopicsByUser(userId: Int): Flow<List<Topic>> {
        return topicDao.getTopicsByUser(userId)
    }

    suspend fun insertTopic(topic: Topic): Long {
        return topicDao.insertTopic(topic)
    }

    suspend fun updateTopic(topic: Topic) {
        topicDao.updateTopic(topic)
    }

    suspend fun deleteTopic(topic: Topic) {
        topicDao.deleteTopic(topic)
    }

    suspend fun deleteTopicById(topicId: Int) {
        topicDao.deleteTopicById(topicId)
    }

    suspend fun getTopicsCountByCategory(categoryId: Int): Int {
        return topicDao.getTopicsCountByCategory(categoryId)
    }
}
