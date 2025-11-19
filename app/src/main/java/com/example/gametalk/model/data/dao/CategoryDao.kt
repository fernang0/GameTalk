package com.example.gametalk.model.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gametalk.model.data.entities.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>
    
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Int): Category?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)
    
    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
    
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoriesCount(): Int
}
