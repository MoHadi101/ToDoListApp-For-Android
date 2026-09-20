package com.hadi.todolistapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView

class AdminTodoAdapter(
    private val onDelete: (TodoEntity) -> Unit
) : BaseAdapter() {

    private var todos = mutableListOf<TodoEntity>()
    private var userMap = mutableMapOf<Int, String>()

    fun updateTodos(newTodos: List<TodoEntity>, users: List<UserEntity>) {
        todos.clear()
        todos.addAll(newTodos)
        userMap.clear()
        users.forEach { user ->
            userMap[user.id] = user.username
        }
        notifyDataSetChanged()
    }

    override fun getCount(): Int = todos.size
    override fun getItem(position: Int): TodoEntity = todos[position]
    override fun getItemId(position: Int): Long = todos[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_todo, parent, false)

        val todo = getItem(position)
        val tvText = view.findViewById<TextView>(R.id.tvTodoText)
        val tvUser = view.findViewById<TextView>(R.id.tvTodoUser)
        val btnDelete = view.findViewById<Button>(R.id.btnDeleteTodo)

        tvText.text = todo.text
        tvText.paintFlags = if (todo.completed) android.graphics.Paint.STRIKE_THRU_TEXT_FLAG else 0
        val username = userMap[todo.userId] ?: "Unbekannt"
        tvUser.text = "Benutzer: $username"

        btnDelete.setOnClickListener {
            onDelete(todo)
        }

        return view
    }
}