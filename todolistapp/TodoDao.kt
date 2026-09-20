package com.hadi.todolistapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo_items ORDER BY id DESC")
    suspend fun getAllTodosForAdmin(): List<TodoEntity>

    @Query("SELECT * FROM todo_items WHERE userId = :userId ORDER BY id DESC")
    suspend fun getTodosForUser(userId: Int): List<TodoEntity>

    @Insert
    suspend fun insertTodo(todo: TodoEntity)

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Delete
    suspend fun deleteTodo(todo: TodoEntity)
}