package com.example.booklette

import DetailInvoiceItem
import EInvoiceAdapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklette.databinding.FragmentEInvoiceBinding
import com.example.booklette.model.CartObject
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
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
    private var _binding: FragmentEInvoiceBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var itemList: ArrayList<DetailInvoiceItem> = ArrayList()
    private lateinit var adapter: EInvoiceAdapter

    var orderID = arguments?.getString(ORDERID_PARAM)

    // Trong phương thức companion object newInstance, nhận thêm một tham số để truyền dữ liệu
    @SuppressLint("NotifyDataSetChanged")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEInvoiceBinding.inflate(inflater, container, false)
        val view = binding.root
        adapter = EInvoiceAdapter(requireContext(), itemList)

        db = Firebase.firestore
        auth = Firebase.auth

        orderID = arguments?.getString(ORDERID_PARAM)

        Log.d("orderid", orderID.toString())

        binding.rvEinvoiceItem.adapter = adapter
        binding.rvEinvoiceItem.layoutManager = LinearLayoutManager(requireContext())
        if(itemList.size == 0){
            loadItemListData()
        }
        else {
            binding.rvEinvoiceItem.visibility = View.VISIBLE
        }
        binding.ivBackToPrev.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        // Other code for handling UI interactions

        return view
    }

    fun loadItemListData() {
        itemList.clear()
        binding.rvEinvoiceItem.visibility = View.GONE
        binding.orderId.text = orderID.toString()
        orderID?.let {
            db.collection("orders")
                .document(it)
                .get()
                .addOnSuccessListener { document ->
                    val orderData = document.data
                    binding.subtotal.text = orderData?.get("totalSum").toString()
                    binding.totalAmount.text = orderData?.get("totalSum").toString()
                    binding.moneyCollected.text = orderData?.get("totalSum").toString()
                    binding.customerAddress.text = orderData?.get("shippingAddress").toString()

                    val timeStamp = orderData?.get("creationDate") as Timestamp

                    val date: Date? = timeStamp?.toDate()
                    val sdf = SimpleDateFormat("dd-MM-yyyy")

                    binding.invoiceDate.text = sdf.format(date).toString()
                    if (orderData?.get("items") != null) {
                        val bookList = orderData?.get("items") as? Map<String, Any>
                        bookList?.let { bookListData ->
                            for (item in bookListData) {
                                val itemId = item.key
                                val quantity = (item.value as? Map<String, Any>)?.get("quantity") as? Number
                                val totalSum = (item.value as? Map<String, Any>)?.get("totalSum") as? Number
                                db.collection("books")
                                    .whereEqualTo("bookID", itemId)
                                    .get()
                                    .addOnSuccessListener { bookDocument ->
                                        for (eachBook in bookDocument) {
                                            itemList.add(
                                                DetailInvoiceItem(
                                                    eachBook.data["name"].toString(),
                                                    quantity?.toInt() ?: 1,
                                                    (totalSum?.toFloat() ?: 0.0F) / (quantity?.toFloat() ?: 1.0F),
                                                    totalSum?.toFloat() ?: 0.0F
                                                )
                                            )
                                        }

                                        adapter.notifyDataSetChanged()

                                        Handler().postDelayed({
                                            binding.rvEinvoiceItem.visibility = View.VISIBLE
                                        }, 2000)
                                    }
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("loadItemListData", "Error loading item list: $e")
                }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}