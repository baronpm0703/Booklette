package com.example.booklette

import EInvoiceAdapter
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklette.databinding.FragmentEInvoiceBinding
import com.example.booklette.databinding.FragmentOrderDetailProcessingBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ORDERID_PARAM = "param1"
private const val ORDERNAME_PARAM = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [OrderDetailCaseProcessingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EInvoiceFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var orderID: String? = null
    private var _binding: FragmentEInvoiceBinding? = null
    private var itemsMap: Map<String, Any>? = null


    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    private lateinit var db : FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            itemsMap = it.getSerializable("itemsMap") as? Map<String, Any>
        }
    }

    // Trong phương thức companion object newInstance, nhận thêm một tham số để truyền dữ liệu

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEInvoiceBinding.inflate(inflater, container, false)
        val view = binding.root

        db = Firebase.firestore

        itemsMap = arguments?.getSerializable("itemsMap") as? Map<String, Any>

        val recyclerView = binding.rvEinvoiceItem
        val adapter = EInvoiceAdapter(itemsMap)
        recyclerView.adapter = adapter

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // back
        val backButton = binding.ivBackToPrev
        backButton.setOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }


        // cancel order
        return view
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(itemsMap: Map<String, Any>): EInvoiceFragment {
            val fragment = EInvoiceFragment()
            val args = Bundle()
            args.putSerializable("itemsMap", itemsMap as Serializable)
            fragment.arguments = args
            return fragment
        }
    }
}