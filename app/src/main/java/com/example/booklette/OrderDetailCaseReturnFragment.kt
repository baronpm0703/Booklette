package com.example.booklette

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso
import java.io.File
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date


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
    private lateinit var exContext : Context
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
        var customerID = ""
        var userSetting = true
        var shopSetting = true
        var hcmusBook = false
        exContext = requireContext()
        orderID?.let {
            db.collection("orders")
                .document(it)
                .get()
                .addOnSuccessListener {document->
                        val orderData = document.data
                    customerID = orderData?.get("customerID").toString()
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
                                        orderRef.update("status","Yêu cầu hoàn trả đang duyệt")
                                    }
                                    var imageList = arrayListOf(URI1Ref,URI2Ref)
                                    val returnDoc = hashMapOf(
                                        "customerID" to auth.uid.toString(),
                                        "image" to imageList.toList(),
                                        "orderID" to orderID,
                                        "shopReason" to "",
                                        "itemID" to itemsFragment.getListClickedBookID(),
                                        "reason" to reasonField.text.toString(),
                                        "requestID" to "RNE$count",
                                        "returnDate" to timestamp,
                                        "status" to "Yêu cầu hoàn trả đang duyệt",
                                    )
                                    returnRef.add(returnDoc)
                                        .addOnSuccessListener {
                                            Toast.makeText(requireContext(),getString(R.string.orderDetailReturnConfirmYes),Toast.LENGTH_SHORT).show()
                                            db.collection("accounts")
                                                .whereEqualTo("UID", auth.uid)
                                                .get().addOnSuccessListener { documents ->
                                                    for (document in documents) {
                                                        val userData = document.data
                                                        val deliveryNotif = userData?.get("deliveryNotif")
                                                        userSetting = if (deliveryNotif is Boolean) {
                                                            deliveryNotif
                                                        } else {
                                                            false // Default value if deliveryNotif is null or not a Boolean
                                                        }

                                                    }
                                                }

                                            //send email
                                            db.collection("orders").document(orderID!!)
                                                .get()
                                                .addOnSuccessListener { documentSnapshot ->
                                                    var orderDetail = ""
                                                    val orderData = documentSnapshot.data
                                                    var shopEmail = ""
                                                    var shopFullName = ""
                                                    val itemsMap =
                                                        orderData?.get("items") as? Map<String, Map<String, Any>>
                                                    val fetchBookNamesTasks =
                                                        itemsMap?.flatMap { (shopID, itemMap) ->
                                                            itemMap.map { (itemId, itemData) ->
                                                                Log.d("shopID", shopID)
                                                                Log.d("itemId", itemId)
                                                                Log.d("itemData", itemData.toString())

                                                                // Get shop name
                                                                db.collection("accounts")
                                                                    .whereEqualTo("UID", shopID)
                                                                    .get()
                                                                    .addOnSuccessListener { querySnapshot ->
                                                                        for (document in querySnapshot) {
                                                                            val storeData = document.data
                                                                            val bookStoreRef =
                                                                                storeData["store"] as DocumentReference
                                                                            var bookStoreName =
                                                                                storeData["fullname"].toString()
                                                                            val salesNotif = storeData["salesNotif"]
                                                                            shopSetting = if (salesNotif is Boolean) {
                                                                                salesNotif
                                                                            } else {
                                                                                false // Default value if deliveryNotif is null or not a Boolean
                                                                            }
                                                                            shopFullName = bookStoreName
                                                                            bookStoreRef.get()
                                                                                .addOnSuccessListener {
                                                                                    if (it.exists()) {
                                                                                        val bookStoreData = it.data
                                                                                        bookStoreName =
                                                                                            bookStoreData?.get("storeName")
                                                                                                .toString()
                                                                                        shopEmail =
                                                                                            bookStoreData?.get("email")
                                                                                                .toString()
                                                                                        shopFullName = bookStoreName
                                                                                    }
                                                                                }
                                                                                .addOnFailureListener { exception ->
                                                                                    // Handle any errors
                                                                                    Log.e(
                                                                                        "getStoreNameError",
                                                                                        "Error getting document: $exception"
                                                                                    )
                                                                                }


                                                                            // Fetch books after getting store name
                                                                            db.collection("books")
                                                                                .whereEqualTo("bookID", itemId)
                                                                                .get()
                                                                                .addOnSuccessListener { bookQuerySnapshot ->
                                                                                    for (bookDocument in bookQuerySnapshot) {
                                                                                        val bookData =
                                                                                            bookDocument.data
                                                                                        val id = itemId
                                                                                        val name =
                                                                                            bookData["name"].toString()
                                                                                        val author =
                                                                                            bookData["author"].toString()
                                                                                        val imageUrl =
                                                                                            bookData["image"].toString()

                                                                                        val itemMap2 =
                                                                                            itemData as? Map<*, *>
                                                                                        val genre =
                                                                                            bookData["genre"] as String

                                                                                        val price =
                                                                                            (itemMap2?.get("totalSum") as Number).toLong()
                                                                                        val quantity =
                                                                                            itemMap2.get("quantity") as Long
                                                                                        orderDetail += exContext.getString(
                                                                                            R.string.store
                                                                                        ) + ": " + bookStoreName + "\n"
                                                                                        orderDetail += exContext.getString(
                                                                                            R.string.manageshop_mybooks_add_name
                                                                                        ) + ": " + name + "\n"
                                                                                        orderDetail += exContext.getString(
                                                                                            R.string.manageshop_mybooks_add_author
                                                                                        ) + ": " + author + "\n"
                                                                                        orderDetail += exContext.getString(
                                                                                            R.string.quantity
                                                                                        ) + " " + quantity.toString() + "\n\n"
                                                                                    }

                                                                                }
                                                                                .addOnFailureListener { exception ->
                                                                                    Log.e(
                                                                                        "Loi",
                                                                                        exception.toString()
                                                                                    )
                                                                                }
                                                                        }
                                                                    }
                                                                    .addOnFailureListener { exception ->
                                                                        Log.e("Loi", exception.toString())
                                                                    }
                                                            }
                                                        }
                                                    Tasks.whenAllComplete(fetchBookNamesTasks!!)
                                                        .addOnSuccessListener {
                                                            val emailSender = EmailSender(exContext)
                                                            val emailSubject =
                                                                exContext.getString(R.string.orderReturnSubject)
                                                            val shopEmailSubject =
                                                                exContext.getString(R.string.orderReturnSubject)
                                                            val totalMoney =
                                                                (orderData?.get("totalSum") as Number).toLong()
                                                            val status = orderData?.get("status") as String
                                                            val timeStamp =
                                                                orderData?.get("creationDate") as Timestamp

                                                            val date: Date? = timeStamp?.toDate()
                                                            val sdf = SimpleDateFormat("dd-MM-yyyy")
                                                            val paymentMethod =
                                                                orderData?.get("paymentMethod") as? Map<String, Any>
                                                            val paymentMethodType =
                                                                paymentMethod?.get("Type").toString()
                                                            val shippingAddress =
                                                                orderData?.get("shippingAddress") as String
                                                            val beforeDiscount =
                                                                (orderData?.get("beforeDiscount") as Number).toLong()
                                                            var emailBody = ""
                                                            var shopEmailBody = ""
                                                            var userFullName = ""
                                                            var userEmail = ""

                                                            val statusEmail = changeStatusText(status)
                                                            val discount =
                                                                "-" + formatMoney(beforeDiscount - totalMoney)
                                                            var orderMessage = ""
                                                            Log.d("emailHcmus", hcmusBook.toString())
                                                            if (!hcmusBook) {
                                                                orderMessage = exContext.getString(R.string.email_return_created)
                                                            } else {
                                                                orderMessage =
                                                                    exContext.getString(R.string.email_return_created)
                                                            }

                                                            val shopOrderMessage = exContext.getString(R.string.email_order_return)
                                                            db.collection("accounts")
                                                                .whereEqualTo("UID", customerID)
                                                                .get()
                                                                .addOnSuccessListener { querySnapshot ->
                                                                    for (snapshot in querySnapshot) {
                                                                        Log.d("emailsnapshot", snapshot.toString())
                                                                        val userData = snapshot.data
                                                                        userFullName =
                                                                            userData["fullname"].toString()
                                                                        val bookStoreRef =
                                                                            userData["store"] as DocumentReference
                                                                        bookStoreRef.get()
                                                                            .addOnSuccessListener {
                                                                                if (it.exists()) {
                                                                                    val bookStoreData = it.data
                                                                                    userEmail =
                                                                                        bookStoreData?.get("email")
                                                                                            .toString()
                                                                                    emailBody = emailSender.setBodyReturn(
                                                                                        userFullName,
                                                                                        orderMessage,
                                                                                        "RNE$count",
                                                                                        changeStatusText("Yêu cầu hoàn trả đang duyệt"),
                                                                                        sdf.format(Date()).toString(),
                                                                                        reasonField.text.toString().takeIf { it.isNotBlank() } ?: exContext.getString(R.string.no_reason_provided),
                                                                                    )
                                                                                    shopEmailBody =
                                                                                        emailSender.setBodyReturn(
                                                                                            shopFullName,
                                                                                            shopOrderMessage,
                                                                                            "RNE$count",
                                                                                            changeStatusText("Yêu cầu hoàn trả đang duyệt"),
                                                                                            sdf.format(Date()).toString(),
                                                                                            reasonField.text.toString().takeIf { it.isNotBlank() } ?: exContext.getString(R.string.no_reason_provided),
                                                                                        )
                                                                                    Log.d(
                                                                                        "userEmail",
                                                                                        "user " + userEmail
                                                                                    )
                                                                                    if (userSetting) {
                                                                                        emailSender.sendEmail(
                                                                                            userEmail,
                                                                                            emailSubject,
                                                                                            emailBody
                                                                                        )
                                                                                    }

                                                                                    Log.d(
                                                                                        "shopEmail",
                                                                                        "shop " + shopEmail
                                                                                    )
                                                                                    if (shopSetting) {
                                                                                        emailSender.sendEmail(
                                                                                            shopEmail,
                                                                                            shopEmailSubject,
                                                                                            shopEmailBody
                                                                                        )
                                                                                    }
                                                                                }
                                                                            }
                                                                            .addOnFailureListener { exception ->
                                                                                // Handle any errors
                                                                                Log.e(
                                                                                    "bookStoreRef",
                                                                                    "Error getting document: $exception"
                                                                                )
                                                                            }

                                                                    }


                                                                }
                                                        }
                                                }
                                            requireActivity().onBackPressedDispatcher.onBackPressed()
                                        }
                                        .addOnFailureListener{
                                            Log.e("error", it.toString())
                                        }
                                }
//                                // Initialize a progress dialog
//                                val progressDialog = ProgressDialog(requireContext())
//                                progressDialog.setTitle(getString(R.string.documentUploading))
//                                progressDialog.setMessage(getString(R.string.pleaseWait))
//                                progressDialog.setCancelable(false) // Disable canceling the dialog by clicking outside of it
//
//                                // Show the progress dialog
//                                progressDialog.show()
                                if (uploadTasks.isNotEmpty()) {
                                    // Wait for all upload tasks to complete
                                    Tasks.whenAllSuccess<Uri>(uploadTasks)
                                        .addOnSuccessListener { downloadUrls ->
                                            // Process download URLs
                                            URI1Ref = if (URI1.isNullOrEmpty()) "" else downloadUrls[0].toString()
                                            URI2Ref = if (URI2.isNullOrEmpty()) "" else downloadUrls.getOrElse(1) { "" }.toString()

                                            uploadImagesAndAddDocument()
                                            //progressDialog.dismiss()

                                        }
                                        .addOnFailureListener { exception ->
                                            Log.e("Upload", "Error uploading images", exception)
                                            //progressDialog.dismiss()

                                        }
                                } else {
                                    uploadImagesAndAddDocument()
                                    //progressDialog.dismiss()

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
            status.contains("xử lý", true) -> exContext.getString(R.string.my_order_processing_button)
            status.contains("huỷ", true) -> exContext.getString(R.string.my_order_cancelled_button)
            status.contains("trả đang duyệt", true) -> exContext.getString(R.string.my_order_detail_item_return_in_process)
            status.contains("trả thành công", true) -> exContext.getString(R.string.my_order_detail_item_return_success)
            status.contains("trả bị từ chối", true) -> exContext.getString(R.string.my_order_detail_item_return_failed)
            status.contains("thành công", true) -> exContext.getString(R.string.my_order_completed_button)
            status.contains("đã giao", true) -> exContext.getString(R.string.my_order_delivered_button)
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