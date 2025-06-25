package com.practica.proyectoihc.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.practica.proyectoihc.R
import com.practica.proyectoihc.databinding.FragmentDatosBinding
import java.util.*

class DatosFragment : Fragment() {

    private var _binding: FragmentDatosBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDatosBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Mostrar calendario al hacer clic en la fecha
        binding.tvFechaNac.apply {
            isFocusable = false
            setOnClickListener { mostrarDatePicker() }
        }

        // Guardar datos en Firestore
        binding.btnGuardarInformacion.setOnClickListener {
            guardarDatosEnFirestore()
        }

        return binding.root
    }

    private fun mostrarDatePicker() {
        val calendario = Calendar.getInstance()
        val anio = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.EstiloDatePicker,
            { _, año, mesSeleccionado, diaSeleccionado ->
                val fecha = String.format("%02d-%02d-%04d", diaSeleccionado, mesSeleccionado + 1, año)
                binding.tvFechaNac.setText(fecha)
            },
            anio, mes, dia
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun guardarDatosEnFirestore() {
        val uid = auth.currentUser?.uid
        val correo = auth.currentUser?.email
        val nombres = binding.tvNombre.text.toString().trim()
        val apellidoPaterno = binding.tvApellidoPaterno.text.toString().trim()
        val apellidoMaterno = binding.tvApellidoMaterno.text.toString().trim()
        val fechaNacimiento = binding.tvFechaNac.text.toString().trim()

        if (uid == null || correo == null || nombres.isEmpty() || apellidoPaterno.isEmpty()
            || apellidoMaterno.isEmpty() || fechaNacimiento.isEmpty()) {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val datos = hashMapOf(
            "uid" to uid,
            "nombres" to nombres,
            "apellidoPaterno" to apellidoPaterno,
            "apellidoMaterno" to apellidoMaterno,
            "fechaNacimiento" to fechaNacimiento,
            "correo" to correo
        )

        firestore.collection("usuarios").document(uid)
            .set(datos)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
                // Opcional: Navegar al perfil u otra pantalla
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al guardar los datos", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
