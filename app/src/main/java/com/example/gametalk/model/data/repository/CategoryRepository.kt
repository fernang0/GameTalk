package com.example.gametalk.model.data.repository

import com.example.gametalk.model.data.dao.CategoryDao
import com.example.gametalk.model.data.entities.Category
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun getCategoryById(categoryId: Int): Category? {
        return categoryDao.getCategoryById(categoryId)
    }

    suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }

    suspend fun initializeDefaultCategories() {
        val count = categoryDao.getCategoriesCount()
        if (count == 0) {
            val defaultCategories = listOf(
                Category(
                    name = "Acción y Aventura",
                    description = "Discute juegos de acción, aventura y mundo abierto",
                    icon = "🎮",
                    topicsCount = 0
                ),
                Category(
                    name = "RPG",
                    description = "Juegos de rol, RPGs japoneses y occidentales",
                    icon = "⚔️",
                    topicsCount = 0
                ),
                Category(
                    name = "Shooters",
                    description = "FPS, TPS y juegos de disparos competitivos",
                    icon = "🔫",
                    topicsCount = 0
                ),
                Category(
                    name = "Estrategia",
                    description = "RTS, TBS, 4X y juegos de estrategia",
                    icon = "🧠",
                    topicsCount = 0
                ),
                Category(
                    name = "MOBA",
                    description = "League of Legends, Dota 2 y otros MOBAs",
                    icon = "🏆",
                    topicsCount = 0
                ),
                Category(
                    name = "Battle Royale",
                    description = "Fortnite, PUBG, Apex Legends y más",
                    icon = "🎯",
                    topicsCount = 0
                ),
                Category(
                    name = "Deportes",
                    description = "FIFA, NBA, simuladores deportivos",
                    icon = "⚽",
                    topicsCount = 0
                ),
                Category(
                    name = "Indie",
                    description = "Juegos independientes y experimentales",
                    icon = "🎨",
                    topicsCount = 0
                ),
                Category(
                    name = "Retro Gaming",
                    description = "Juegos clásicos y nostalgia gamer",
                    icon = "👾",
                    topicsCount = 0
                ),
                Category(
                    name = "E-Sports",
                    description = "Competencias, torneos y gaming profesional",
                    icon = "🏅",
                    topicsCount = 0
                )
            )
            categoryDao.insertCategories(defaultCategories)
        }
    }
}
