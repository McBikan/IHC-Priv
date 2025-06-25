package com.practica.proyectoihc.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practica.proyectoihc.R
import com.practica.proyectoihc.model.Pregunta
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.InputStreamReader

class PsicoFragment : Fragment() {
    private lateinit var tvPregunta: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var btnSiguiente: Button
    private lateinit var progreso: ProgressBar

    private lateinit var preguntas: List<Pregunta>
    private var preguntasSeleccionadas: List<Pregunta> = listOf()
    private val puntajesPorBloque = mutableMapOf<Int, Int>()
    private var indiceActual = 0
    private var puntajeTotal = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_psico, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvPregunta = view.findViewById(R.id.tvPregunta)
        radioGroup = view.findViewById(R.id.radioGroupOpciones)
        btnSiguiente = view.findViewById(R.id.btnSiguiente)
        progreso = view.findViewById(R.id.progreso)

        preguntas = leerPreguntasDesdeAssets(requireContext())
        preguntasSeleccionadas = seleccionarPreguntasDistribuidas(preguntas)
        progreso.max = preguntasSeleccionadas.size

        mostrarPregunta()

        btnSiguiente.setOnClickListener {
            val seleccion = radioGroup.checkedRadioButtonId
            if (seleccion != -1) {
                val radioBtn = view.findViewById<RadioButton>(seleccion)
                val valor = radioBtn.tag as Int
                puntajeTotal += valor

                val pregunta = preguntasSeleccionadas[indiceActual]
                val bloqueIndex = pregunta.bloque
                puntajesPorBloque[bloqueIndex] = puntajesPorBloque.getOrDefault(bloqueIndex, 0) + valor
                indiceActual++
                if (indiceActual < preguntasSeleccionadas.size) {
                    mostrarPregunta()
                } else {
                    mostrarResultadoFinal()
                }
            } else {
                Toast.makeText(requireContext(), "Seleccione una opción", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun leerPreguntasDesdeAssets(context: Context): List<Pregunta> {
        val inputStream = context.assets.open("preguntas_psicologicas.json")
        val reader = InputStreamReader(inputStream, "UTF-8")
        val tipo = object : TypeToken<List<Pregunta>>() {}.type
        return Gson().fromJson(reader, tipo)
    }

    private fun mostrarPregunta() {
        val pregunta = preguntasSeleccionadas[indiceActual]
        tvPregunta.text = pregunta.texto
        progreso.progress = indiceActual + 1

        radioGroup.removeAllViews()
        for (opcion in pregunta.opciones) {
            val radioButton = RadioButton(requireContext()).apply {
                text = opcion.texto
                tag = opcion.valor
                textSize = 16f
                setTextColor(Color.DKGRAY)
                setPadding(16, 24, 18, 24)
            }
            radioGroup.addView(radioButton)
        }
    }

    private fun seleccionarPreguntasDistribuidas(lista: List<Pregunta>): List<Pregunta> {
        val totalBloques = 10
        val seleccionPorBloque = 3
        return mutableListOf<Pregunta>().apply {
            for (i in 0 until totalBloques) {
                val bloque = lista.filter { it.bloque == i }
                addAll(bloque.shuffled().take(seleccionPorBloque))
            }
        }.shuffled()
    }

    private fun mostrarResultadoFinal() {
        val feedback = StringBuilder("Resultados por categoría:\n\n")
        val puntajeMaximoPorBloque = 3 * 3
        val resultados = mutableListOf<Pair<String, Int>>()

        for ((bloque, puntaje) in puntajesPorBloque) {
            val categoria = preguntas.firstOrNull { it.bloque == bloque }?.categoria ?: "Bloque $bloque"
            val interpretacion = when {
                puntaje <= 3 -> "Nivel Bajo"
                puntaje <= 6 -> "Nivel Promedio"
                else -> "Nivel Normal"
            }
            feedback.append("- $categoria: $interpretacion ($puntaje/$puntajeMaximoPorBloque)\n")
            resultados.add(Pair(categoria, puntaje))
        }

        val tresPeores = resultados.sortedBy { it.second }.take(3).map { it.first }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val firestore = FirebaseFirestore.getInstance()
            val update = hashMapOf(
                "antecedente1" to (tresPeores.getOrNull(0) ?: ""),
                "antecedente2" to (tresPeores.getOrNull(1) ?: ""),
                "antecedente3" to (tresPeores.getOrNull(2) ?: "")
            )
            firestore.collection("usuarios").document(uid)
                .update(update as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Resultados psicológicos guardados", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al guardar antecedentes", Toast.LENGTH_SHORT).show()
                }
        }

        tvPregunta.text = feedback.toString()
        radioGroup.removeAllViews()
        btnSiguiente.text = "Reiniciar"
        progreso.progress = progreso.max

        btnSiguiente.setOnClickListener {
            indiceActual = 0
            puntajeTotal = 0
            puntajesPorBloque.clear()
            preguntasSeleccionadas = seleccionarPreguntasDistribuidas(preguntas)
            btnSiguiente.text = "Siguiente"
            mostrarPregunta()
        }
    }
}
