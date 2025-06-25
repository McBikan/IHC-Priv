package com.practica.proyectoihc.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.practica.proyectoihc.R
import com.practica.proyectoihc.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 👉 Botón Iniciar Sesión
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                mostrarMensaje("Completa todos los campos")
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val user: FirebaseUser? = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        verificarDatosUsuarioYRedirigir(user.uid)
                    } else {
                        mostrarMensaje("Verifica tu correo antes de continuar")
                    }
                }
                .addOnFailureListener {
                    mostrarMensaje("Credenciales inválidas o usuario no registrado")
                }
        }

        // 👉 Recuperar contraseña
        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            if (email.isEmpty()) {
                mostrarMensaje("Por favor, ingresa tu correo para recuperar la contraseña")
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    mostrarMensaje("Correo de recuperación enviado. Revisa tu bandeja de entrada o spam.")
                }
                .addOnFailureListener {
                    mostrarMensaje("No se pudo enviar el correo: ${it.message}")
                }
        }

        // 👉 Navegar al registro
        binding.registerSection.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registroFragment)
        }

        return binding.root
    }

    // ✅ Verifica si el usuario ya completó sus datos personales
    private fun verificarDatosUsuarioYRedirigir(uid: String) {
        firestore.collection("usuarios").document(uid).get()
            .addOnSuccessListener { documento ->
                if (documento != null && documento.exists()) {
                    val nombres = documento.getString("nombres")
                    val apellidoPaterno = documento.getString("apellidoPaterno")
                    val apellidoMaterno = documento.getString("apellidoMaterno")
                    val fechaNacimiento = documento.getString("fechaNacimiento")

                    val datosCompletos = !nombres.isNullOrBlank() &&
                            !apellidoPaterno.isNullOrBlank() &&
                            !apellidoMaterno.isNullOrBlank() &&
                            !fechaNacimiento.isNullOrBlank()

                    if (datosCompletos) {
                        findNavController().navigate(R.id.action_loginFragment_to_perfilFragment)
                    } else {
                        findNavController().navigate(R.id.action_loginFragment_to_datosFragment)
                    }
                } else {
                    // Si no existe documento, asumir datos incompletos
                    findNavController().navigate(R.id.action_loginFragment_to_datosFragment)
                }
            }
            .addOnFailureListener {
                mostrarMensaje("Error al verificar tus datos personales")
            }
    }

    private fun mostrarMensaje(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
