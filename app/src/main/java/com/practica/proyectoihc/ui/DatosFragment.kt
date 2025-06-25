package com.practica.proyectoihc.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.practica.proyectoihc.R
import com.practica.proyectoihc.databinding.FragmentDatosBinding
import java.io.ByteArrayOutputStream
import java.util.*

class DatosFragment : Fragment() {

    private var _binding: FragmentDatosBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private val SELECT_IMAGE_REQUEST = 1001
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDatosBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Mostrar calendario
        binding.tvFechaNac.apply {
            isFocusable = false
            setOnClickListener { mostrarDatePicker() }
        }

        // Elegir foto
        binding.ivFotoUsuario.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, SELECT_IMAGE_REQUEST)
        }

        // Guardar datos
        binding.btnGuardarInformacion.setOnClickListener {
            guardarDatosEnFirestore()
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SELECT_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            selectedImageUri?.let {
                binding.ivFotoUsuario.setImageURI(it)
                subirFotoFirebase(it)
            }
        }
    }

    private fun subirFotoFirebase(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("fotos_perfil/$uid.png")

        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
        }

        // Redimensionar (ejemplo: 100x100, no uses 10x10, es muy pequeño)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true)

        val baos = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageData = baos.toByteArray()

        storageRef.putBytes(imageData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Foto subida con éxito", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al subir la foto", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(
            requireContext(),
            R.style.EstiloDatePicker,
            { _, y, m, d -> binding.tvFechaNac.setText(String.format("%02d-%02d-%04d", d, m + 1, y)) },
            year, month, day
        )

        dialog.datePicker.maxDate = System.currentTimeMillis()
        dialog.show()
    }

    private fun guardarDatosEnFirestore() {
        val uid = auth.currentUser?.uid
        val correo = auth.currentUser?.email
        val nombres = binding.tvNombre.text.toString().trim()
        val apellidoPaterno = binding.tvApellidoPaterno.text.toString().trim()
        val apellidoMaterno = binding.tvApellidoMaterno.text.toString().trim()
        val fechaNacimiento = binding.tvFechaNac.text.toString().trim()

        if (uid == null || correo == null || nombres.isEmpty() ||
            apellidoPaterno.isEmpty() || apellidoMaterno.isEmpty() || fechaNacimiento.isEmpty()) {
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
                findNavController().navigate(R.id.action_datosFragment_to_perfilFragment)
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
