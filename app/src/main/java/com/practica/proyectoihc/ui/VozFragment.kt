package com.practica.proyectoihc.ui

import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import androidx.navigation.fragment.findNavController
import com.practica.proyectoihc.R
import com.practica.proyectoihc.ui.base.BaseMenuFragment

class VozFragment : BaseMenuFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_voz, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ivMenu = view.findViewById<View>(R.id.ivMenu)
        setupMenuNavigation(ivMenu)

        val btnMic = view.findViewById<ImageButton>(R.id.btnMicrofono)

        btnMic.setOnClickListener {
            val action = VozFragmentDirections.actionVozFragmentToChatFragment()
            findNavController().navigate(action)
        }
    }
}
