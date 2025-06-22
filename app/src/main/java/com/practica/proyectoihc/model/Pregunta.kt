package com.practica.proyectoihc.model

data class Pregunta(
    val id: Int,
    val bloque: Int,
    val categoria: String,
    val texto: String,
    val opciones: List<Opcion>
)