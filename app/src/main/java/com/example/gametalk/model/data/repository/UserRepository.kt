package com.example.gametalk.model.data.repository

import com.example.gametalk.model.data.dao.UserDao
import com.example.gametalk.model.data.entities.User

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(email: String, password: String, username: String): Result<User> {
        return try {
            val existing = userDao.getUserByEmail(email)
            if (existing != null) {
                Result.failure(Exception("El correo ya está registrado"))
            } else {
                val newUser = User(email = email, password = password, username = username)
                userDao.insertUser(newUser)
                Result.success(newUser)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val user = userDao.getUserByCredentials(email, password)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Credenciales incorrectas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}