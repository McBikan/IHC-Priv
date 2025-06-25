package com.practica.proyectoihc.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.practica.proyectoihc.R
import com.practica.proyectoihc.ui.base.BaseMenuFragment
import java.text.SimpleDateFormat
import java.util.*
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage


class PerfilFragment : BaseMenuFragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivMenu = view.findViewById<View>(R.id.ivMenu)
        setupMenuNavigation(ivMenu)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val uid = auth.currentUser?.uid ?: return

        val tvTituloNombre = view.findViewById<TextView>(R.id.tvTituloNombre)
        val tvNombreValor = view.findViewById<TextView>(R.id.tvNombreValor)
        val tvApellidoValor = view.findViewById<TextView>(R.id.tvApellidoValor)
        val tvEdadValor = view.findViewById<TextView>(R.id.tvEdadValor)
        val ivProfileImage = view.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.ivProfileImage)

        // 🔹 Cargar imagen del Storage
        val storageRef = FirebaseStorage.getInstance().reference.child("fotos_perfil/$uid.png")
        storageRef.downloadUrl
            .addOnSuccessListener { uri ->
                Glide.with(requireContext())
                    .load(uri)
                    .placeholder(R.drawable.default_user)
                    .into(ivProfileImage)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "No se pudo cargar la imagen de perfil", Toast.LENGTH_SHORT).show()
            }

        // 🔹 Cargar datos Firestore
        firestore.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nombres = doc.getString("nombres") ?: ""
                    val apellidoPaterno = doc.getString("apellidoPaterno") ?: ""
                    val apellidoMaterno = doc.getString("apellidoMaterno") ?: ""
                    val fechaNac = doc.getString("fechaNacimiento") ?: ""

                    tvTituloNombre.text = nombres.split(" ").firstOrNull()?.uppercase() ?: ""
                    tvNombreValor.text = nombres
                    tvApellidoValor.text = "$apellidoPaterno $apellidoMaterno"

                    val edad = calcularEdad(fechaNac)
                    tvEdadValor.text = "$edad AÑOS"
                } else {
                    Toast.makeText(requireContext(), "No se encontraron datos", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
    }


    private fun calcularEdad(fechaNacimiento: String): Int {
        return try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val birthDate = sdf.parse(fechaNacimiento)
            val today = Calendar.getInstance()
            val birth = Calendar.getInstance()
            birth.time = birthDate!!

            var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            age
        } catch (e: Exception) {
            0
        }
    }
}
