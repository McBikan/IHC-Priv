    package com.practica.proyectoihc.ui

    import android.content.Intent
    import android.Manifest
    import android.content.pm.PackageManager
    import android.os.Bundle
    import android.speech.RecognitionListener
    import android.speech.RecognizerIntent
    import android.speech.SpeechRecognizer
    import android.speech.tts.TextToSpeech
    import android.view.LayoutInflater
    import android.view.MotionEvent
    import android.view.View
    import android.view.ViewGroup
    import android.widget.*
    import androidx.core.app.ActivityCompat
    import androidx.core.content.ContextCompat
    import androidx.fragment.app.Fragment
    import com.practica.proyectoihc.R
    import okhttp3.*
    import okhttp3.MediaType.Companion.toMediaType
    import okhttp3.RequestBody.Companion.toRequestBody
    import org.json.JSONObject
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

        private val mensajes: MutableList<Pair<String, Boolean>> = mutableListOf() // Pair(texto, esUsuario)

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
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("es", "MX"))
            }
        }

        private fun iniciarEscucha() {
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle?) {
                    val texto = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                    texto?.let {
                        agregarMensaje(it, true)
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
                        agregarMensaje(respuesta, false)
                        tts.speak(respuesta, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                }
            })
        }

        private fun agregarMensaje(texto: String, esUsuario: Boolean) {
            mensajes.add(Pair(texto, esUsuario))
        }

        private fun mostrarMensajes() {
            contenedorMensajes.removeAllViews()
            val inflater = LayoutInflater.from(requireContext())

            mensajes.forEach { (mensaje, esUsuario) ->
                val layout = inflater.inflate(
                    if (esUsuario) R.layout.item_mensaje_usuario else R.layout.item_mensaje_bot,
                    contenedorMensajes,
                    false
                )

                val tvMensaje = layout.findViewById<TextView>(
                    if (esUsuario) R.id.tvMensajeUsuario else R.id.tvMensajeBot
                )

                tvMensaje.text = mensaje
                contenedorMensajes.addView(layout)
            }
        }


        override fun onDestroyView() {
            super.onDestroyView()
            speechRecognizer.destroy()
            tts.shutdown()
        }
    }
