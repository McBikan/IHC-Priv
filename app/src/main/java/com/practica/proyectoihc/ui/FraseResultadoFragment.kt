package com.practica.proyectoihc.ui

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.practica.proyectoihc.R
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.* // Para Locale
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody


class FraseResultadoFragment : Fragment(), TextToSpeech.OnInitListener {

    private val args: FraseResultadoFragmentArgs by navArgs()
    private lateinit var tvResultado: TextView
    private lateinit var btnRepetir: Button
    private lateinit var btnDetener: Button

    private val client = OkHttpClient()
    private val GROQ_API_KEY = "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"

    private var tts: TextToSpeech? = null
    private var fraseGenerada: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_frase_resultado, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvResultado = view.findViewById(R.id.tvResultado)
        btnRepetir = view.findViewById(R.id.btnRepetir)
        btnDetener = view.findViewById(R.id.btnDetener)
        btnRepetir.setOnClickListener {
            reproducirFrase()
        }

        btnDetener.setOnClickListener {
            detenerFrase()
        }
        tts = TextToSpeech(requireContext(), this)

        val contexto = args.fraseContexto
        view.postDelayed({
            generarFraseMotivacional(contexto)
        }, 1500)
    }

    private fun generarFraseMotivacional(contexto: String) {
        val prompt = """
            Eres un acompañante emocional compasivo. 
            Dame UNICAMENTE una frase un poco extensa, cálida y poderosa para reconfortar emocionalmente a alguien que está pasando por lo siguiente: $contexto.
            Reglas:
            - No des una explicacion, contexto ni introduccion.
            - Devuelme solo la frase, sin comillas, sin adornos.
            - Que sea apropiada para ser leida en voz alta por una app de apoyo emocional.
            - Que este en idioma Español
            - Se comprensiv o    
            Solo escribe la frase, Nada mas.
        """.trimIndent()

        val json = JSONObject().apply {
            put("model", "llama3-8b-8192")
            val messagesArray = org.json.JSONArray().put(
                JSONObject().put("role", "user").put("content", prompt)
            )
            put("messages", messagesArray)
        }

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .addHeader("Authorization", "Bearer $GROQ_API_KEY")
            .addHeader("Content-Type", "application/json")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    tvResultado.text = "Error al generar frase: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    val result = JSONObject(body)
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .trim()

                    fraseGenerada = result
                    activity?.runOnUiThread {
                        tvResultado.text = fraseGenerada
                        reproducirFrase()
                    }
                } else {
                    activity?.runOnUiThread {
                        tvResultado.text = "Error al obtener respuesta de Groq"
                    }
                }
            }
        })
    }

    private fun reproducirFrase() {
        fraseGenerada.takeIf { it.isNotBlank() }?.let {
            tts?.speak(it, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }
    private fun detenerFrase() {
        tts?.stop()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("es", "MX") // Español (España), puedes usar "es", "MX" también
        }
    }

    override fun onDestroyView() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroyView()
    }
}