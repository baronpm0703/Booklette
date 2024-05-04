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
import com.example.booklette.model.CartObject
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle


class HelpCenterFragment : Fragment(){
    private var _binding: FragmentHelpCenterBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpCenterBinding.inflate(inflater, container, false)
        val view = binding.root



        binding.question1.setOnClickListener {
            val questionHelpCenterFragment = QuestionHelpCenterFragment()
            val bundle = Bundle()
            bundle.putString("question", "question1")
            questionHelpCenterFragment.arguments = bundle

            val homeAct = (activity as homeActivity)
            homeAct.changeFragmentContainer(questionHelpCenterFragment, homeAct.smoothBottomBarStack[homeAct.smoothBottomBarStack.size - 1])
        }

        binding.question2.setOnClickListener {
            val questionHelpCenterFragment = QuestionHelpCenterFragment()
            val bundle = Bundle()
            bundle.putString("question", "question2")
            questionHelpCenterFragment.arguments = bundle

            val homeAct = (activity as homeActivity)
            homeAct.changeFragmentContainer(questionHelpCenterFragment, homeAct.smoothBottomBarStack[homeAct.smoothBottomBarStack.size - 1])
        }

        binding.question3.setOnClickListener {
            val questionHelpCenterFragment = QuestionHelpCenterFragment()
            val bundle = Bundle()
            bundle.putString("question", "question3")
            questionHelpCenterFragment.arguments = bundle

            val homeAct = (activity as homeActivity)
            homeAct.changeFragmentContainer(questionHelpCenterFragment, homeAct.smoothBottomBarStack[homeAct.smoothBottomBarStack.size - 1])
        }

        binding.question4.setOnClickListener {
            val questionHelpCenterFragment = QuestionHelpCenterFragment()
            val bundle = Bundle()
            bundle.putString("question", "question4")
            questionHelpCenterFragment.arguments = bundle

            val homeAct = (activity as homeActivity)
            homeAct.changeFragmentContainer(questionHelpCenterFragment, homeAct.smoothBottomBarStack[homeAct.smoothBottomBarStack.size - 1])
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