package com.hadi.todolistapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat

class UserAdapter(
    private val onBlockToggle: (UserEntity) -> Unit,
    private val onResetPassword: (UserEntity) -> Unit
) : BaseAdapter() {

    private var users = mutableListOf<UserEntity>()

    fun updateUsers(newUsers: List<UserEntity>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = users.size
    override fun getItem(position: Int): UserEntity = users[position]
    override fun getItemId(position: Int): Long = users[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)

        val user = getItem(position)
        val tvUsername = view.findViewById<TextView>(R.id.tvUsername)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
        val btnBlock = view.findViewById<Button>(R.id.btnBlockToggle)
        val btnReset = view.findViewById<Button>(R.id.btnResetPassword)

        tvUsername.text = user.username + (if (user.isAdmin) " (Admin)" else "")
        tvStatus.text = if (user.isBlocked) "Gesperrt" else "Aktiv"
        tvStatus.setTextColor(
            if (user.isBlocked) ContextCompat.getColor(parent.context, R.color.delete_red)
            else ContextCompat.getColor(parent.context, R.color.primary)
        )

        btnBlock.text = if (user.isBlocked) "Entsperren" else "Sperren"
        btnBlock.setOnClickListener {
            onBlockToggle(user)
        }

        btnReset.setOnClickListener {
            onResetPassword(user)
        }

        return view
    }
}