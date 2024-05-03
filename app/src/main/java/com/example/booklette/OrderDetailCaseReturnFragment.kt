package com.example.booklette

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.example.booklette.databinding.FragmentOrderDetailReturnBinding
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso
import java.io.File
import java.net.URI


/**
 * A simple [Fragment] subclass.
 * Use the [OrderDetailCaseReturnFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
private const val ORDERID_PARAM = "param1"
class OrderDetailCaseReturnFragment : Fragment() {
    private var selectedImageUri: Uri? = null
    // TODO: Rename and change types of parameters
    private var orderID: String? = null
    private var _binding: FragmentOrderDetailReturnBinding? = null
    private lateinit var imagePicker: ImageView
    private lateinit var imagePicker2: ImageView
    private lateinit var cancelImagePicker: ImageView
    private lateinit var cancelImagePicker2: ImageView
    private lateinit var itemsFragment: OrderDetailItemListFragment
    private var URI1: String = ""
    private var URI2: String = ""
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    private lateinit var db : FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var cloudStorage: FirebaseStorage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            orderID = it.getString(ORDERID_PARAM)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOrderDetailReturnBinding.inflate(inflater, container, false)
        val view = binding.root

        db = Firebase.firestore
        auth = Firebase.auth
        cloudStorage = Firebase.storage
        val orderItemLayout = binding.orderDetailProductsFragmentFrameLayout
        val reasonField = binding.returnReasonField
        var tempTotalOrgMoney: Long = 0
        orderID?.let {
            db.collection("orders")
                .document(it)
                .get()
                .addOnSuccessListener {document->
                        val orderData = document.data
                        val itemsMap = orderData?.get("items") as? Map<String, Map<String,Any>>
                        var totalQuantity: Long = 0
                        itemsMap?.forEach { (itemID, itemData) ->

                            //Log.d("number",itemMap.toString())
                            totalQuantity += ((itemData )["quantity"] as? Long) ?: 0
                            tempTotalOrgMoney += (itemData["totalSum"] as? Long ?: 0)
                        }

                        // setup recycler view for books
                        itemsFragment = OrderDetailItemListFragment.newInstance(1, itemsMap!!, allowSelection = true, allowMultipleSelection = true)
                        childFragmentManager.beginTransaction()
                            .replace(orderItemLayout.id,itemsFragment)
                            .commit()



                }
        }

        // back
        val backButton = binding.backButton
        backButton.setOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        imagePicker = binding.imagePicker
        imagePicker2 = binding.imagePicker2
        cancelImagePicker = binding.cancelImage1
        cancelImagePicker2 = binding.cancelImage2
        imagePicker.setOnClickListener{
            val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickImageIntent, PICK_IMAGE_REQUEST1)
        }
        imagePicker2.setOnClickListener{
            val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickImageIntent, PICK_IMAGE_REQUEST2)
        }
        cancelImagePicker.setOnClickListener{
            imagePicker.setImageResource(R.drawable.baseline_add_circle_outline_24)
            cancelImagePicker.visibility = View.GONE
            URI1 = ""
        }

        cancelImagePicker2.setOnClickListener{
            imagePicker2.setImageResource(R.drawable.baseline_add_circle_outline_24)
            cancelImagePicker2.visibility = View.GONE
            URI2 = ""
        }

        val timestamp = FieldValue.serverTimestamp()
        val orderRef = orderID?.let { db.collection("orders").document(it) }
        val returnRef = db.collection("returnNExchange")
        val cloudStorageRef = cloudStorage.reference
        var count = 0
        returnRef.get()
            .addOnSuccessListener {
                count = it.size() + 1
            }
            .addOnFailureListener { exception ->
                Log.e("error", exception.toString())
            }
        // return button
        val returnButton = binding.returnButton
        returnButton.setOnClickListener {
            //implement later when document finished.
            val dialogClickListener =
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            if (itemsFragment.getListClickedBookID().isEmpty()){
                                val instruction = context?.getString(R.string.return_instruction)
                                instruction?.let {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                }
                            }
                            else{
                                var URI1Ref = ""
                                var URI2Ref = ""
                                // Create a list to store the upload tasks
                                val uploadTasks = mutableListOf<Task<Uri>>()

                                if (!URI1.isNullOrEmpty()) {
                                    val imageRef = cloudStorageRef.child("returnImages/" + auth.uid + getFileNameFromUri(URI1.toUri()) + timestamp.toString())
                                    val uploadTask1 = imageRef.putFile(URI1.toUri())
                                        .continueWithTask { task ->
                                            if (!task.isSuccessful) {
                                                throw task.exception ?: IllegalStateException("Unknown error")
                                            }
                                            imageRef.downloadUrl
                                        }
                                    uploadTasks.add(uploadTask1)
                                }

                                if (!URI2.isNullOrEmpty()) {
                                    val imageRef = cloudStorageRef.child("returnImages/" + auth.uid+ getFileNameFromUri(URI2.toUri()) + timestamp.toString())
                                    val uploadTask2 = imageRef.putFile(URI2.toUri())
                                        .continueWithTask { task ->
                                            if (!task.isSuccessful) {
                                                throw task.exception ?: IllegalStateException("Unknown error")
                                            }
                                            imageRef.downloadUrl
                                        }
                                    uploadTasks.add(uploadTask2)
                                }

                                fun uploadImagesAndAddDocument() {
                                    // Your code for uploading images and adding the document
                                    if (orderRef != null) {
                                        orderRef.update("status","Yêu cầu trả đang duyệt")
                                    }
                                    var imageList = arrayListOf(URI1Ref,URI2Ref)
                                    val returnDoc = hashMapOf(
                                        "customerID" to auth.uid.toString(),
                                        "image" to imageList.toList(),
                                        "orderID" to orderID,
                                        "itemID" to itemsFragment.getListClickedBookID(),
                                        "reason" to reasonField.text.toString(),
                                        "requestID" to "RNE$count",
                                        "returnDate" to timestamp,
                                        "status" to "Yêu cầu trả đang duyệt",
                                    )
                                    returnRef.add(returnDoc)
                                        .addOnSuccessListener {
                                            Toast.makeText(requireContext(),getString(R.string.orderDetailReturnConfirmYes),Toast.LENGTH_SHORT).show()
                                            requireActivity().onBackPressedDispatcher.onBackPressed()
                                        }
                                        .addOnFailureListener{
                                            Log.e("error", it.toString())
                                        }
                                }
                                // Initialize a progress dialog
                                val progressDialog = ProgressDialog(requireContext())
                                progressDialog.setTitle(getString(R.string.documentUploading))
                                progressDialog.setMessage(getString(R.string.pleaseWait))
                                progressDialog.setCancelable(false) // Disable canceling the dialog by clicking outside of it

                                // Show the progress dialog
                                progressDialog.show()
                                if (uploadTasks.isNotEmpty()) {
                                    // Wait for all upload tasks to complete
                                    Tasks.whenAllSuccess<Uri>(uploadTasks)
                                        .addOnSuccessListener { downloadUrls ->
                                            // Process download URLs
                                            URI1Ref = if (URI1.isNullOrEmpty()) "" else downloadUrls[0].toString()
                                            URI2Ref = if (URI2.isNullOrEmpty()) "" else downloadUrls.getOrElse(1) { "" }.toString()

                                            uploadImagesAndAddDocument()
                                            progressDialog.dismiss()

                                        }
                                        .addOnFailureListener { exception ->
                                            Log.e("Upload", "Error uploading images", exception)
                                            progressDialog.dismiss()

                                        }
                                } else {
                                    uploadImagesAndAddDocument()
                                    progressDialog.dismiss()

                                }
                            }
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {}
                    }
                }

            val builder: AlertDialog.Builder = AlertDialog.Builder(context)

            builder.setMessage(R.string.orderDetailReturnConfirmLabel).setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show()

        }
        return view
    }
    fun changeStatusText(status: String): String {
        return when {
            status.contains("xử lý", true) -> getString(R.string.my_order_processing_button)
            status.contains("huỷ", true) -> getString(R.string.my_order_cancelled_button)
            status.contains("trả đang duyệt", true) -> getString(R.string.my_order_detail_item_return_in_process)
            status.contains("trả thành công", true) -> getString(R.string.my_order_detail_item_return_success)
            status.contains("trả thất bại", true) -> getString(R.string.my_order_detail_item_return_failed)
            status.contains("thành công", true) -> getString(R.string.my_order_completed_button)
            status.contains("đã giao", true) -> getString(R.string.my_order_delivered_button)
            else -> ""
        }
    }
    fun getFileNameFromUri(uri: Uri): String {
        val file = File(uri.path)
        return file.name
    }
    fun formatMoney(number: Long): String {
        val numberString = number.toString()
        val regex = "(\\d)(?=(\\d{3})+$)".toRegex()
        return numberString.replace(regex, "$1.") + " VND"
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param orderID Parameter 1.
         * @param orderFullName Parameter 2.
         * @return A new instance of fragment OrderDetailFragment.
         */
        private const val PICK_IMAGE_REQUEST1 = 13351
        private const val PICK_IMAGE_REQUEST2 = 13352
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(orderID: String) =
            OrderDetailCaseReturnFragment().apply {
                arguments = Bundle().apply {
                    putString(ORDERID_PARAM, orderID)
                }
            }
    }
    private fun setImageToImageView() {
        selectedImageUri?.let { uri ->
            Picasso.get().load(uri).into(imagePicker)
            URI1 = selectedImageUri.toString()
            Toast.makeText(context,URI1,Toast.LENGTH_SHORT).show()
        }
        cancelImagePicker.visibility = View.VISIBLE
    }
    private fun setImageToImageView2() {
        selectedImageUri?.let { uri ->
            Picasso.get().load(uri).into(imagePicker2)
            URI2 = selectedImageUri.toString()
            Toast.makeText(context,URI2,Toast.LENGTH_SHORT).show()
        }
        cancelImagePicker2.visibility = View.VISIBLE
    }
    // Lấy ảnh từ user (Hai?)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 13351 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            setImageToImageView()
        }
        else if (requestCode == 13352 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            setImageToImageView2()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}