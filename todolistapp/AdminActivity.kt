package com.hadi.todolistapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hadi.todolistapp.databinding.ActivityAdminBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var database: TodoDatabase
    private lateinit var userDao: UserDao
    private lateinit var todoDao: TodoDao
    private lateinit var userAdapter: UserAdapter
    private lateinit var adminTodoAdapter: AdminTodoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        database = TodoDatabase.getDatabase(this)
        userDao = database.userDao()
        todoDao = database.todoDao()

        userAdapter = UserAdapter(
            onBlockToggle = { user -> toggleBlockUser(user) },
            onResetPassword = { user -> resetPassword(user) }
        )
        binding.lvUsers.adapter = userAdapter

        adminTodoAdapter = AdminTodoAdapter { todo ->
            deleteTodo(todo)
        }
        binding.lvAdminTodos.adapter = adminTodoAdapter

        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            val users = withContext(Dispatchers.IO) { userDao.getAllUsers() }
            val todos = withContext(Dispatchers.IO) { todoDao.getAllTodosForAdmin() }  // ← hier geändert
            userAdapter.updateUsers(users)
            adminTodoAdapter.updateTodos(todos, users)
        }
    }

    private fun toggleBlockUser(user: UserEntity) {
        lifecycleScope.launch {
            val newBlocked = !user.isBlocked
            withContext(Dispatchers.IO) {
                userDao.setUserBlocked(user.id, newBlocked)
            }
            Toast.makeText(
                this@AdminActivity,
                if (newBlocked) "Benutzer gesperrt" else "Benutzer entsperrt",
                Toast.LENGTH_SHORT
            ).show()
            loadData()
        }
    }

    private fun resetPassword(user: UserEntity) {
        // Generiere ein neues Passwort (hier fest "newPass123", du kannst auch zufällig generieren)
        val newPassword = "newPass123"
        lifecycleScope.launch {
            val salt = PasswordUtils.generateSalt()
            val hash = PasswordUtils.hashPassword(newPassword, salt)
            val updatedUser = user.copy(passwordHash = hash, salt = salt)
            withContext(Dispatchers.IO) {
                userDao.updateUser(updatedUser)
            }
            Toast.makeText(
                this@AdminActivity,
                "Neues Passwort für ${user.username}: $newPassword",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun deleteTodo(todo: TodoEntity) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                todoDao.deleteTodo(todo)
            }
            Toast.makeText(this@AdminActivity, "Todo gelöscht", Toast.LENGTH_SHORT).show()
            loadData()
        }
    }
}