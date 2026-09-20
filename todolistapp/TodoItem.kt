package com.hadi.todolistapp

data class TodoItem(
    val text: String,
    var isCompleted: Boolean = false
)