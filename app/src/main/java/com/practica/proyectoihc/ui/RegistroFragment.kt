package com.practica.proyectoihc.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.practica.proyectoihc.databinding.FragmentRegistroBinding
import android.os.Handler
import android.os.Looper


class RegistroFragment : Fragment() {

    private var _binding: FragmentRegistroBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistroBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        binding.btnRegistrarse.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmarPassword = binding.etConfirmarPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmarPassword.isEmpty()) {
                mostrarBurbuja("Completa todos los campos")
                return@setOnClickListener
            }

            if (password != confirmarPassword) {
                mostrarBurbuja("Las contraseñas no coinciden")
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    auth.currentUser?.sendEmailVerification()
                        ?.addOnSuccessListener {
                            mostrarBurbuja("Registro exitoso. Revisa tu correo para verificar la cuenta.")
                            Handler(Looper.getMainLooper()).postDelayed({
                                findNavController().popBackStack()
                            }, 2000)
                        }
                        ?.addOnFailureListener {
                            mostrarBurbuja("Error al enviar correo de verificación: ${it.message}")
                        }
                }

                .addOnFailureListener { e ->
                    if (e is FirebaseAuthUserCollisionException) {
                        mostrarBurbuja("Este correo ya está registrado")
                    } else {
                        mostrarBurbuja("Error: ${e.message}")
                    }
                }
        }

        return binding.root
    }

    private fun mostrarBurbuja(mensaje: String) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
