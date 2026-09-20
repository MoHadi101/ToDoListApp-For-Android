package com.hadi.todolistapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hadi.todolistapp.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var database: TodoDatabase
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        database = TodoDatabase.getDatabase(this)
        userDao = database.userDao()

        // Prüfen, ob bereits ein Benutzer eingeloggt ist
        val userId = sessionManager.getUserId()
        if (userId != -1) {
            lifecycleScope.launch {
                val user = withContext(Dispatchers.IO) { userDao.getUserById(userId) }
                if (user != null && !user.isBlocked) {
                    navigateToMain(user)
                } else {
                    sessionManager.clearSession()
                }
            }
        }

        // Automatisch Admin erstellen, wenn keine Benutzer existieren
        lifecycleScope.launch {
            val users = withContext(Dispatchers.IO) { userDao.getAllUsers() }
            if (users.isEmpty()) {
                val salt = PasswordUtils.generateSalt()
                val hash = PasswordUtils.hashPassword("admin123", salt)
                val admin = UserEntity(
                    username = "admin",
                    passwordHash = hash,
                    salt = salt,
                    isAdmin = true,
                    isBlocked = false
                )
                withContext(Dispatchers.IO) {
                    userDao.insertUser(admin)
                }
                Toast.makeText(this@LoginActivity, "Admin erstellt: admin/admin123", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Bitte wenden Sie sich an den Administrator, um Ihr Passwort zurückzusetzen.", Toast.LENGTH_LONG).show()
        }
    }

    private fun performLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Bitte Benutzername und Passwort eingeben", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) { userDao.getUserByUsername(username) }
            if (user == null) {
                Toast.makeText(this@LoginActivity, "Benutzer nicht gefunden", Toast.LENGTH_SHORT).show()
                return@launch
            }
            if (user.isBlocked) {
                Toast.makeText(this@LoginActivity, "Dieser Benutzer wurde blockiert.", Toast.LENGTH_SHORT).show()
                return@launch
            }
            if (!PasswordUtils.verifyPassword(password, user.salt, user.passwordHash)) {
                Toast.makeText(this@LoginActivity, "Falsches Passwort", Toast.LENGTH_SHORT).show()
                return@launch
            }
            sessionManager.saveUserId(user.id)
            navigateToMain(user)
        }
    }

    private fun navigateToMain(user: UserEntity) {
        val intent = if (user.isAdmin) {
            Intent(this, AdminActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }
        intent.putExtra("userId", user.id)
        startActivity(intent)
        finish()
    }
}