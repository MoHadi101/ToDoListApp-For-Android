package com.hadi.todolistapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.hadi.todolistapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var database: TodoDatabase
    private lateinit var todoDao: TodoDao

    private lateinit var todoAdapter: TodoAdapter

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*
         * Android 15+ verwendet Edge-to-Edge.
         * Dadurch kann der Inhalt sonst unter der Statusleiste liegen.
         *
         * Wir geben dem gesamten Layout automatisch den
         * notwendigen Abstand oben und unten.
         */
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                view.paddingLeft,
                systemBars.top,
                view.paddingRight,
                systemBars.bottom
            )

            insets
        }

        sessionManager = SessionManager(this)

        database = TodoDatabase.getDatabase(this)
        todoDao = database.todoDao()

        // Eingeloggten Benutzer holen
        userId = sessionManager.getUserId()

        // Kein Benutzer eingeloggt
        if (userId == -1) {
            goToLogin()
            return
        }

        setupList()
        setupButtons()
        setupLogout()

        loadTodos()
    }


    private fun setupList() {

        todoAdapter = TodoAdapter(

            onCheckedChanged = { todo, checked ->
                updateTodo(todo, checked)
            },

            onDeleteClicked = { todo ->
                deleteTodo(todo)
            }
        )

        binding.lvTodo.adapter = todoAdapter
    }


    private fun setupButtons() {

        binding.btnAdd.setOnClickListener {

            val text = binding.etTodo.text
                ?.toString()
                ?.trim()
                ?: ""

            if (text.isEmpty()) {

                Toast.makeText(
                    this,
                    "Bitte eine Aufgabe eingeben",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val todo = TodoEntity(
                text = text,
                completed = false,
                userId = userId
            )

            lifecycleScope.launch {

                withContext(Dispatchers.IO) {
                    todoDao.insertTodo(todo)
                }

                binding.etTodo.text?.clear()

                Toast.makeText(
                    this@MainActivity,
                    "Aufgabe hinzugefügt",
                    Toast.LENGTH_SHORT
                ).show()

                loadTodos()
            }
        }
    }


    private fun setupLogout() {

        binding.btnLogout.setOnClickListener {

            sessionManager.clearSession()

            Toast.makeText(
                this,
                "Du wurdest abgemeldet",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(
                this,
                LoginActivity::class.java
            )

            // Verhindert, dass man mit Zurück wieder
            // auf die ToDo-Liste kommt.
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)

            finish()
        }
    }


    private fun loadTodos() {

        lifecycleScope.launch {

            val todos = withContext(Dispatchers.IO) {
                todoDao.getTodosForUser(userId)
            }

            todoAdapter.updateTodos(todos)
        }
    }


    private fun updateTodo(
        todo: TodoEntity,
        checked: Boolean
    ) {

        lifecycleScope.launch {

            val updatedTodo = todo.copy(
                completed = checked
            )

            withContext(Dispatchers.IO) {
                todoDao.updateTodo(updatedTodo)
            }

            loadTodos()
        }
    }


    private fun deleteTodo(todo: TodoEntity) {

        lifecycleScope.launch {

            withContext(Dispatchers.IO) {
                todoDao.deleteTodo(todo)
            }

            Toast.makeText(
                this@MainActivity,
                "Aufgabe gelöscht",
                Toast.LENGTH_SHORT
            ).show()

            loadTodos()
        }
    }


    private fun goToLogin() {

        sessionManager.clearSession()

        val intent = Intent(
            this,
            LoginActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        finish()
    }


    override fun onResume() {
        super.onResume()

        if (userId != -1) {
            loadTodos()
        }
    }
}