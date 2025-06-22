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
import com.practica.proyectoihc.model.Opcion
import java.io.InputStreamReader

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PsicoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PsicoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    // UI
    private lateinit var tvPregunta: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var btnSiguiente: Button
    private lateinit var progreso: ProgressBar

    // Logica
    private lateinit var preguntas: List<Pregunta>
    private var preguntasSeleccionadas: List<Pregunta> = listOf()
    private val puntajesPorBloque = mutableMapOf<Int, Int>()
    private var indiceActual = 0
    private var puntajeTotal = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_psico, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Enlazar vistas
        tvPregunta = view.findViewById(R.id.tvPregunta)
        radioGroup = view.findViewById(R.id.radioGroupOpciones)
        btnSiguiente = view.findViewById(R.id.btnSiguiente)
        progreso = view.findViewById(R.id.progreso)

        // Cargar preguntas desde el archivo Json
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
                }
                else {
                    mostrarResultadoFinal()
                }
            }
            else {
                Toast.makeText(requireContext(), "Seleccione una opcion", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun leerPreguntasDesdeAssets(context: Context): List<Pregunta> {
        val inputStream = context.assets.open("preguntas_psicologicas.json")
        val reader = InputStreamReader(inputStream, "UTF-8")
        val tipo = object : TypeToken<List<Pregunta>> () {}.type
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
                val bloque = lista.filter {it.bloque == i}
                addAll(bloque.shuffled().take(seleccionPorBloque))
            }
        }.shuffled()
    }

    private fun mostrarResultadoFinal() {
        val feedback = StringBuilder("Resultados por categoria:\n\n")
        val puntajeMaximoPorBloque = 3 * 3
        for ((bloque, puntaje) in puntajesPorBloque) {
            val categoria = preguntas.firstOrNull { it.bloque == bloque }?.categoria ?: "Bloque $bloque"
            val interpretation = when {
                puntaje <= 3 -> "Nivel Bajo"
                puntaje <= 6 -> "Nivel Promedio"
                else -> "Nivel Normal"
            }
            feedback.append("- $categoria: $interpretation ($puntaje/$puntajeMaximoPorBloque)\n")
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


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PsicoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PsicoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}