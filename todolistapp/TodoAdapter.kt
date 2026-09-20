package com.hadi.todolistapp

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.checkbox.MaterialCheckBox

class TodoAdapter(
    private val onCheckedChanged: (TodoEntity, Boolean) -> Unit,
    private val onDeleteClicked: (TodoEntity) -> Unit
) : BaseAdapter() {

    private val todos = mutableListOf<TodoEntity>()

    fun updateTodos(newTodos: List<TodoEntity>) {
        todos.clear()
        todos.addAll(newTodos)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return todos.size
    }

    override fun getItem(position: Int): TodoEntity {
        return todos[position]
    }

    override fun getItemId(position: Int): Long {
        return todos[position].id.toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {

        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)

        val todo = getItem(position)

        val checkBox = view.findViewById<MaterialCheckBox>(R.id.todoCheckBox)
        val textView = view.findViewById<TextView>(R.id.todoText)
        val deleteButton = view.findViewById<ImageButton>(R.id.deleteButton)

        // Wichtig bei wiederverwendeten ListView-Zeilen
        checkBox.setOnCheckedChangeListener(null)

        checkBox.isChecked = todo.completed
        textView.text = todo.text

        if (todo.completed) {
            textView.paintFlags =
                textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            textView.alpha = 0.45f
        } else {
            textView.paintFlags =
                textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            textView.alpha = 1f
        }

        checkBox.setOnCheckedChangeListener { _, checked ->
            if (checked != todo.completed) {
                onCheckedChanged(todo, checked)
            }
        }

        deleteButton.setOnClickListener {
            onDeleteClicked(todo)
        }

        return view
    }
}