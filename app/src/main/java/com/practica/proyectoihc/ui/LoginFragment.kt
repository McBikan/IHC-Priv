package com.practica.proyectoihc.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.practica.proyectoihc.databinding.FragmentLoginBinding
import androidx.navigation.fragment.findNavController
import android.widget.Toast
import com.practica.proyectoihc.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

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
                        findNavController().navigate(R.id.action_loginFragment_to_perfilFragment)
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


    private fun mostrarMensaje(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}