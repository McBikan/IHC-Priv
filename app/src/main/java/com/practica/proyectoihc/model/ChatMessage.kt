package com.practica.proyectoihc.model

data class ChatMessage(
    val texto: String,
    val esUsuario: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
