package com.hadi.todolistapp

import java.security.MessageDigest
import java.security.SecureRandom

object PasswordUtils {
    private const val SALT_LENGTH = 16

    fun generateSalt(): String {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    fun hashPassword(password: String, salt: String): String {
        val bytes = (password + salt).toByteArray()
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(bytes)
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, salt: String, hash: String): Boolean {
        return hashPassword(password, salt) == hash
    }
}