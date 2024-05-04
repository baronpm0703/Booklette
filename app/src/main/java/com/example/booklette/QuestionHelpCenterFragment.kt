package com.example.booklette

import CheckOutFragment
import ShipAddressObject
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklette.databinding.FragmentShipAddressBinding
import com.maxkeppeler.sheets.core.SheetStyle
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.FragmentHelpCenterBinding
import com.example.booklette.databinding.FragmentQuestionHelpCenterBinding
import com.example.booklette.model.CartObject
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle


class QuestionHelpCenterFragment : Fragment(){
    private var _binding: FragmentQuestionHelpCenterBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionHelpCenterBinding.inflate(inflater, container, false)
        val view = binding.root

        val question = arguments?.getString("question")
        if(question == "question1"){
            binding.layoutQuestion1.visibility = View.VISIBLE
            binding.layoutQuestion2.visibility = View.GONE
            binding.layoutQuestion3.visibility = View.GONE
            binding.layoutQuestion4.visibility = View.GONE


        }
        if(question == "question2"){
            binding.layoutQuestion2.visibility = View.VISIBLE
            binding.layoutQuestion1.visibility = View.GONE
            binding.layoutQuestion3.visibility = View.GONE
            binding.layoutQuestion4.visibility = View.GONE
        }

        if(question == "question3"){
            binding.layoutQuestion3.visibility = View.VISIBLE
            binding.layoutQuestion1.visibility = View.GONE
            binding.layoutQuestion2.visibility = View.GONE
            binding.layoutQuestion4.visibility = View.GONE
        }

        if(question == "question4"){
            binding.layoutQuestion4.visibility = View.VISIBLE
            binding.layoutQuestion1.visibility = View.GONE
            binding.layoutQuestion2.visibility = View.GONE
            binding.layoutQuestion3.visibility = View.GONE
        }
        binding.ivBackToPrev.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        return view
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}