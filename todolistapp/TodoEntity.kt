package com.hadi.todolistapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_items")
data class TodoEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val text: String,

    val completed: Boolean = false,

    val userId: Int
)