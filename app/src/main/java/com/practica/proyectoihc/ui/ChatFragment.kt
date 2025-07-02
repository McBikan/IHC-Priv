package com.practica.proyectoihc.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practica.proyectoihc.R
import com.practica.proyectoihc.model.ChatMessage
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*

class ChatFragment : Fragment() {

    private lateinit var btnMicrofono: ImageButton
    private lateinit var icMuteOverlay: ImageView
    private lateinit var tvEstadoMicrofono: TextView
    private lateinit var contenedorMensajes: LinearLayout
    private lateinit var vistaPrincipal: ScrollView
    private lateinit var vistaChat: View
    private lateinit var btnVerChat: Button
    private lateinit var btnVolver: Button

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent
    private lateinit var tts: TextToSpeech

    private val mensajes: MutableList<ChatMessage> = mutableListOf() // Pair(texto, esUsuario)
    private val gson = Gson()
    private val archivoChat by lazy {
        File(requireContext().filesDir, "chat.json")
    }
    private val GROQ_API_KEY = "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"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnMicrofono = view.findViewById(R.id.btnMicrofono)
        icMuteOverlay = view.findViewById(R.id.icMuteOverlay)
        tvEstadoMicrofono = view.findViewById(R.id.tvEstadoMicrofono)
        contenedorMensajes = view.findViewById(R.id.contenedorMensajes)
        vistaPrincipal = view.findViewById(R.id.vistaPrincipal)
        vistaChat = view.findViewById(R.id.vistaChat)
        btnVerChat = view.findViewById(R.id.btnVerChat)
        btnVolver = view.findViewById(R.id.btnVolver)

        inicializarVoz()
        cargarMensajes()

        btnMicrofono.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        iniciarEscucha()
                        icMuteOverlay.visibility = View.GONE
                        tvEstadoMicrofono.text = "Escuchando..."
                    } else {
                        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), 101)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    detenerEscucha()
                    icMuteOverlay.visibility = View.VISIBLE
                    tvEstadoMicrofono.text = "Silenciado"
                    true
                }
                else -> false
            }
        }

        btnVerChat.setOnClickListener {
            vistaPrincipal.visibility = View.GONE
            vistaChat.visibility = View.VISIBLE
            mostrarMensajes()
        }

        btnVolver.setOnClickListener {
            vistaPrincipal.visibility = View.VISIBLE
            vistaChat.visibility = View.GONE
        }
    }

    private fun inicializarVoz() {
        tts = TextToSpeech(requireContext()) {
            if (it == TextToSpeech.SUCCESS) {
                tts.language = Locale("es", "MX")
            }
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE,   "es-ES")
        }
    }

    private fun iniciarEscucha() {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val texto = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                texto?.let {
                    agregarMensaje(ChatMessage(it, true))
                    enviarAGroq(it)
                }
            }
            override fun onError(error: Int) {
                tvEstadoMicrofono.text = "Error de voz"
            }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(speechIntent)
    }

    private fun detenerEscucha() {
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.stopListening()
        }
    }

    private fun enviarAGroq(texto: String) {
        val prompt = """
            Eres un consejero emocional comprensivo. Devuelve una respuesta hablada al usuario:
            "$texto"
            Reglas:
            - No repitas lo que dijo el usuario.
            - Sé empático, claro y natural. No exagerado.
            - Solo responde con el mensaje. Nada más.
        """.trimIndent()

        val json = JSONObject().apply {
            put("model", "llama3-8b-8192")
            put("messages", org.json.JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
        }

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .addHeader("Authorization", "Bearer $GROQ_API_KEY")
            .addHeader("Content-Type", "application/json")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return
                val respuesta = JSONObject(body)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                activity?.runOnUiThread {
                    agregarMensaje(ChatMessage(respuesta, false))
                    tts.speak(respuesta, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
        })
    }

    private fun agregarMensaje(mensaje: ChatMessage) {
        mensajes.add(mensaje)
        guardarMensajes()
    }

    private fun mostrarMensajes() {
        contenedorMensajes.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        mensajes.forEach {
            val layout = inflater.inflate(
                if (it.esUsuario) R.layout.item_mensaje_usuario else R.layout.item_mensaje_bot,
                contenedorMensajes,
                false
            )

            val tvMensaje = layout.findViewById<TextView>(
                if (it.esUsuario) R.id.tvMensajeUsuario else R.id.tvMensajeBot
            )

            tvMensaje.text = it.texto
            contenedorMensajes.addView(layout)
        }
    }
    private fun guardarMensajes() {
        val json = gson.toJson(mensajes)
        archivoChat.writeText(json)
    }

    private fun cargarMensajes() {
        if (archivoChat.exists()) {
            val tipo = object : TypeToken<MutableList<ChatMessage>> () {}.type
            val json = archivoChat.readText()
            mensajes.clear()
            mensajes.addAll(gson.fromJson(json, tipo))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        speechRecognizer.destroy()
        tts.shutdown()
    }
}
