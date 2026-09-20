package com.hadi.todolistapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hadi.todolistapp.databinding.ActivityRegisterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var database: TodoDatabase
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = TodoDatabase.getDatabase(this)
        userDao = database.userDao()

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {
        val username = binding.etNewUsername.text.toString().trim()
        val password = binding.etNewPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 4) {
            Toast.makeText(this, "Passwort muss mindestens 4 Zeichen haben", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val existing = withContext(Dispatchers.IO) { userDao.getUserByUsername(username) }
            if (existing != null) {
                Toast.makeText(this@RegisterActivity, "Benutzername bereits vergeben", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val salt = PasswordUtils.generateSalt()
            val hash = PasswordUtils.hashPassword(password, salt)
            val user = UserEntity(
                username = username,
                passwordHash = hash,
                salt = salt,
                isAdmin = false,
                isBlocked = false
            )
            withContext(Dispatchers.IO) {
                userDao.insertUser(user)
            }
            Toast.makeText(this@RegisterActivity, "Registrierung erfolgreich. Bitte einloggen.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}