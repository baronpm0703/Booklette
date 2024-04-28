import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.booklette.BankCardFragment
import com.example.booklette.CartFragment
import com.example.booklette.R
import com.example.booklette.ShipAddressFragment
import com.example.booklette.bookDetailShopVoucherRVAdapter
import com.example.booklette.databinding.FragmentCheckOutBinding
import com.example.booklette.homeActivity
import com.example.booklette.model.CartObject
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.rpc.context.AttributeContext.Resource
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.corepayments.Environment
import com.paypal.android.corepayments.PayPalSDKError
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutClient
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutListener
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutRequest
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutResult
import com.paypal.android.paypalnativepayments.PayPalNativePaysheetActions
import com.paypal.android.paypalnativepayments.PayPalNativeShippingAddress
import com.paypal.android.paypalnativepayments.PayPalNativeShippingListener
import com.paypal.android.paypalnativepayments.PayPalNativeShippingMethod
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CheckOutFragment : Fragment() {

    interface VoucherSumListener {
        fun onVoucherSumCalculated(sum: Float)
    }
    private var _binding: FragmentCheckOutBinding? = null
    private val binding get() = _binding!!

    private lateinit var selectedItems: ArrayList<CartObject>
//    private lateinit var defaultAddress: ShipAddressObject

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var radioButtonClicked: RadioButton

    private lateinit var queue: RequestQueue
    var orderId = ""
    val url = "https://api-m.sandbox.paypal.com/v2/checkout/orders/"
    val urlToken = "https://api-m.sandbox.paypal.com/v1/oauth2/token"
    var token = "Bearer "
    val clientId = "AZY4SgNyOhRXLoVpTswQxyd_cku-w428NGNf_OFr2Tor4t1bCeN4ngxQQIpT6bBAo5_8FPOcXXyKSXDm"
    val clientSecret = "EGNxh5iA4Hzk7G3eSbbTBg4QNmFHp2_SMHla2vxjfM6-B4S7bPPxToYtiFohePAOTfU5VlJVu7uceY_v"


    companion object {

        private const val ARG_SELECTED_ADDRESS = "selected_address"

        fun passSelectedAddressToCheckOut(selectedAddress: ShipAddressObject): CheckOutFragment {
            val fragment = CheckOutFragment()
            val args = Bundle()
            args.putParcelable(ARG_SELECTED_ADDRESS, selectedAddress)
            fragment.arguments = args
            return fragment
        }
        fun passSelectedItemToCheckOut(selectedItems: ArrayList<CartObject>): CheckOutFragment {
            val fragment = CheckOutFragment()
            val args = Bundle()
            args.putParcelableArrayList("SELECTED_ITEMS", selectedItems)
            fragment.arguments = args
            return fragment
        }

        private const val ARG_TOTAL_AMOUNT = "total_amount"

        fun passSTotalAmountItemToCheckOut(totalAmount: Float): CheckOutFragment {
            val fragment = CheckOutFragment()
            val args = Bundle()
            args.putFloat(ARG_TOTAL_AMOUNT, totalAmount)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCheckOutBinding.inflate(inflater, container, false)
        val view = binding.root

        queue = Volley.newRequestQueue(context)

        selectedItems = arguments?.getParcelableArrayList<CartObject>("SELECTED_ITEMS") ?: ArrayList()

        val selectedAddress = arguments?.getParcelable<ShipAddressObject>(ARG_SELECTED_ADDRESS)
        if (selectedAddress != null) {
            // Update UI with the selected address information
            // For example:
            binding.recieverName.text = selectedAddress.receiverName
            binding.recieverPhone.text = selectedAddress.receiverPhone
            binding.addressNumber.text = selectedAddress.addressNumber
            binding.addressZone.text = selectedAddress.province + ", " + selectedAddress.city + ", " + selectedAddress.ward
        }

        val adapter = CheckOutRecyclerViewAdapter(requireContext(), selectedItems)
        binding.rvCheckout.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCheckout.adapter = adapter



        auth = Firebase.auth
        db = Firebase.firestore

        binding.changeAddress.setOnClickListener {
            val shipAddressFragment = ShipAddressFragment()
            val args = Bundle()
            args.putParcelableArrayList("SELECTED_ITEMS", selectedItems)
            shipAddressFragment.arguments = args
            val homeAct = (activity as homeActivity)
            homeAct.supportFragmentManager.popBackStack()
            homeAct.changeFragmentContainer(shipAddressFragment, homeAct.smoothBottomBarStack[homeAct.smoothBottomBarStack.size - 1])
        }

        binding.changeCardBtn.setOnClickListener {
            val bankCardFragment = BankCardFragment()
            val homeAct = (activity as homeActivity)
            homeAct.changeFragmentContainer(bankCardFragment, homeAct.smoothBottomBarStack[homeAct.smoothBottomBarStack.size - 1])
        }


        val totalAmount = calculateTotalAmount()
        Log.d("totalamount", "Voucher Discount: $totalAmount")


        val afterFomartedTotalAmount = String.format("%,.0f", totalAmount)
        binding.totalAmount.text = "$afterFomartedTotalAmount VND"

        binding.totalPayment.text = "$afterFomartedTotalAmount VND"
        binding.totalPaymentInPaymentDetail.text = "$afterFomartedTotalAmount VND"



        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            radioButtonClicked = view.findViewById<RadioButton>(checkedId)
        }

        adapter.setVoucherSumListener(object : CheckOutRecyclerViewAdapter.VoucherSumListener {
            override fun onVoucherSumCalculated(sum: Float) {
                val afterFomartedTotalDiscountAmount = String.format("%,.0f", sum)
                binding.totalDiscount.text = "$afterFomartedTotalDiscountAmount VND"
                val afterCalculateWithVoucher = totalAmount-sum
                val formattedAfterCalculateWithVoucher = String.format("%,.0f", afterCalculateWithVoucher)
                binding.totalPaymentInPaymentDetail.text = "$formattedAfterCalculateWithVoucher VND"
                binding.totalPayment.text = "$formattedAfterCalculateWithVoucher VND"
            }
        })

        binding.placeOrderBtn.setOnClickListener {
            if (::radioButtonClicked.isInitialized) {
                if (radioButtonClicked.text == getString(R.string.paypalMethod)) {
//                    Toast.makeText(context, totalAmount.toString(), Toast.LENGTH_SHORT).show()
                    GlobalScope.launch {
                        try {
                            token += getPayPalToken(urlToken)
                            orderId = createPayPalOrder(url, token)
                            launchPayPalCheckout(orderId)
                        } catch (e: Exception) {
                            // Handle exceptions
                            e.printStackTrace()
                        }
                    }
                } else if (radioButtonClicked.text == getString(R.string.payOnDelivery)) {
                    val storeItemMap: MutableMap<Any, MutableMap<Any, HashMap<Any, Any>>> =
                        mutableMapOf()

                    var totalAmount = 0.0F

                    for (cartObject in selectedItems) {
                        // Calculate total sum
                        val totalSum = cartObject.price * cartObject.bookQuantity
                        totalAmount += totalSum

                        // Create HashMap with quantity and totalSum
                        val itemData = hashMapOf<Any, Any>(
                            "quantity" to cartObject.bookQuantity,
                            "totalSum" to totalSum,
                        )

                        val selectedItemsMap: MutableMap<Any, HashMap<Any, Any>> = mutableMapOf()
                        selectedItemsMap[cartObject.bookID.toString()] = itemData
                        // Add to selectedItemsMap with bookID as key

                        // Add to the map with same storeID
                        val storeID = cartObject.storeID.toString()
                        if (storeItemMap.containsKey(storeID)) {
                            // If storeID already exists, retrieve its corresponding map
                            val existingMap =
                                storeItemMap[storeID] as MutableMap<Any, HashMap<Any, Any>>

                            // Update the existing map with new data
                            existingMap.putAll(selectedItemsMap)

                            // Update storeItemMap with the modified map
                            storeItemMap[storeID] = existingMap
                        } else {
                            // If storeID doesn't exist, simply add the selectedItemsMap
                            storeItemMap[storeID] = selectedItemsMap
                        }
                    }

                    val x = 1
                    val data: HashMap<Any, Any> = hashMapOf(
                        "creationDate" to Timestamp(Date()),
                        "customerID" to auth.currentUser!!.uid.toString(),
                        "items" to storeItemMap,
                        "paymentMethod" to hashMapOf<Any, Any>(
                            "Type" to "COD",
                            "cardHolder" to "",
                            "cardNumber" to "",
                            "expiryDate" to ""
                        ),
                        "status" to "Đang xử lý",
                        "beforeDiscount" to totalAmount,
                        "totalSum" to afterFomartedTotalAmount.toFloat(),
                        "shippingAddress" to (binding.recieverName.text.toString() + " - " +
                                binding.recieverPhone.text.toString() + " - " +
                                binding.addressNumber.text.toString() + " " +
                                binding.addressZone.text.toString())
                    )

                    db.collection("orders").add(data).addOnCompleteListener { documentReference ->
//                            for (cartObject in selectedItems) {
//                                val fieldMap = hashMapOf<Any, Any>(
//                                    cartObject.bookID.toString() to FieldValue.delete()
//                                )
//                            }
                        db.collection("accounts")
                            .whereEqualTo("UID", auth.currentUser!!.uid.toString())
                            .get().addOnSuccessListener { documents ->
                                for (document in documents) {
                                    for (cartObject in selectedItems) {
//                                            val fieldMap = hashMapOf<Any, Any>(
//                                                cartObject.bookID.toString() to FieldValue.delete()
//                                            )
                                        db.collection("accounts").document(document.id).update(
                                            "cart.${cartObject.bookID}",
                                            FieldValue.delete()
                                        ).addOnSuccessListener { result ->

                                        }
                                    }
                                }

                                MotionToast.createColorToast(
                                    context as Activity,
                                    getString(R.string.successfully),
                                    getString(R.string.paymentSuccessfully),
                                    MotionToastStyle.SUCCESS,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.SHORT_DURATION,
                                    ResourcesCompat.getFont(
                                        context as Activity,
                                        www.sanju.motiontoast.R.font.helvetica_regular
                                    )
                                )

                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }
                    }
                }
            } else {
                MotionToast.createColorToast(
                    context as Activity,
                    getString(R.string.failed),
                    getString(R.string.haveNotPickPaymentMethodError),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(
                        context as Activity,
                        www.sanju.motiontoast.R.font.helvetica_regular
                    )
                )
            }
        }

        binding.ivBackToPrev.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return view
    }

    private suspend fun getPayPalToken(url: String): String {
        return suspendCoroutine { continuation ->
            val jsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url, null,
                Response.Listener { response ->
                    val jsonResponse = JSONObject(response.toString())
                    val accessToken = jsonResponse.getString("access_token")
//                    Toast.makeText(this@MainActivity, accessToken.toString(), Toast.LENGTH_SHORT).show()

                    continuation.resume(accessToken)
                },
                Response.ErrorListener { error ->
//                    Toast.makeText(this@MainActivity, "Failed", Toast.LENGTH_SHORT).show()
                    continuation.resumeWithException(error)
                }) {

                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Accept"] = "application/json"
                    headers["Accept-Language"] = "en_US"
                    headers["Authorization"] = "Basic " + Base64.encodeToString("$clientId:$clientSecret".toByteArray(), Base64.NO_WRAP)
                    return headers
                }

                override fun getBody(): ByteArray {
                    // Set the request body
                    return ("grant_type=client_credentials").toByteArray()
                }

                override fun getBodyContentType(): String {
                    // Set the request body content type
                    return "application/x-www-form-urlencoded"
                }
            }

            queue.add(jsonObjectRequest)
        }
    }

    private suspend fun createPayPalOrder(url: String, token: String): String {
        return suspendCoroutine { continuation ->
            val requestObject = JSONObject()
            requestObject.put("intent", "CAPTURE")
            val purchaseUnits = JSONObject()
            val amount = JSONObject()
            amount.put("currency_code", "USD")
            amount.put("value", "5.00")
            purchaseUnits.put("amount", amount)
            val purchaseUnitsArray = JSONArray()
            purchaseUnitsArray.put(purchaseUnits)
            requestObject.put("purchase_units", purchaseUnitsArray)

            val jsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url, requestObject,
                Response.Listener { response ->
                    val orderId = response.optString("id")
                    continuation.resume(orderId)
                },
                Response.ErrorListener { error ->
                    continuation.resumeWithException(error)
                }) {
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = token
                    headers["Content-Type"] = "application/json"
                    return headers
                }
            }

            queue.add(jsonObjectRequest)
        }
    }

    private fun launchPayPalCheckout(orderId: String) {
        val coreConfig = CoreConfig("AZY4SgNyOhRXLoVpTswQxyd_cku-w428NGNf_OFr2Tor4t1bCeN4ngxQQIpT6bBAo5_8FPOcXXyKSXDm", environment = Environment.SANDBOX)

        val payPalNativeClient = PayPalNativeCheckoutClient(
            application = requireActivity().application,
            coreConfig = coreConfig,
            returnUrl = "com.example.booklette://paypalpay"
        )

        payPalNativeClient.listener = object : PayPalNativeCheckoutListener {
            override fun onPayPalCheckoutStart() {
                // the PayPal paysheet is about to show up
                Toast.makeText(activity, "STARTING", Toast.LENGTH_SHORT).show()
            }

            override fun onPayPalCheckoutSuccess(result: PayPalNativeCheckoutResult) {
                // order was approved and is ready to be captured/authorized
//                Toast.makeText(this@MainActivity, "SUCCESSFULL", Toast.LENGTH_SHORT).show()

                GlobalScope.launch {
                    try {
                        val tmp = capturePayPalOrder(token, orderId)
//                        Toast.makeText(activity, tmp.toString(), Toast.LENGTH_SHORT).show()

                        val selectedItemsMap: MutableMap<Any, HashMap<Any, Any>> = mutableMapOf()
                        var totalAmount = 0.0F

                        for (cartObject in selectedItems) {
                            // Calculate total sum
                            val totalSum = cartObject.price * cartObject.bookQuantity
                            totalAmount += totalSum

                            // Create HashMap with quantity and totalSum
                            val itemData = hashMapOf<Any, Any>(
                                "quantity" to cartObject.bookQuantity,
                                "totalSum" to totalSum
                            )

                            // Add to selectedItemsMap with bookID as key
                            selectedItemsMap[cartObject.bookID.toString()] = itemData
                        }

                        val data: HashMap<Any, Any> = hashMapOf(
                            "creationDate" to Timestamp(Date()),
                            "customerID" to auth.currentUser!!.uid.toString(),
                            "items" to selectedItemsMap,
                            "paymentMethod" to hashMapOf<Any, Any>(
                                "Type" to "Paypal",
                                "cardHolder" to "",
                                "cardNumber" to "",
                                "expiryDate" to ""
                            ),
                            "status" to "Thành công",
                            "totalSum" to totalAmount,
                            "shippingAddress" to (binding.recieverName.text.toString() + " - " +
                                                    binding.recieverPhone.text.toString() + " - " +
                                                    binding.addressNumber.text.toString() + " " +
                                                binding.addressZone.text.toString())
                            )

                        db.collection("orders").add(data).addOnCompleteListener{documentReference ->
//                            for (cartObject in selectedItems) {
//                                val fieldMap = hashMapOf<Any, Any>(
//                                    cartObject.bookID.toString() to FieldValue.delete()
//                                )
//                            }
                            db.collection("accounts")
                                .whereEqualTo("UID", auth.currentUser!!.uid.toString())
                                .get().addOnSuccessListener { documents ->
                                    for (document in documents) {


                                        for (cartObject in selectedItems) {
//                                            val fieldMap = hashMapOf<Any, Any>(
//                                                cartObject.bookID.toString() to FieldValue.delete()
//                                            )
                                            db.collection("accounts").document(document.id).update("cart.${cartObject.bookID}", FieldValue.delete()).addOnSuccessListener { result ->

                                            }
                                        }
                                    }

                                    MotionToast.createColorToast(
                                        context as Activity,
                                        getString(R.string.successfully),
                                        getString(R.string.paymentSuccessfully),
                                        MotionToastStyle.SUCCESS,
                                        MotionToast.GRAVITY_BOTTOM,
                                        MotionToast.SHORT_DURATION,
                                        ResourcesCompat.getFont(context as Activity, www.sanju.motiontoast.R.font.helvetica_regular))

                                    requireActivity().onBackPressedDispatcher.onBackPressed()
                                }
                        }


                    } catch (e: Exception) {
                        // Handle exceptions
                        e.printStackTrace()
                    }
                }
            }

            override fun onPayPalCheckoutFailure(error: PayPalSDKError) {
                // handle the error
//                Toast.makeText(activity, "FAILED", Toast.LENGTH_SHORT).show()
                MotionToast.createColorToast(
                    context as Activity,
                    getString(R.string.failed),
                    getString(R.string.paymentFailed),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(context as Activity, www.sanju.motiontoast.R.font.helvetica_regular))
            }

            override fun onPayPalCheckoutCanceled() {
                // the user canceled the flow
                MotionToast.createColorToast(
                    context as Activity,
                    getString(R.string.canceled),
                    getString(R.string.paymentCanceled),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(context as Activity, www.sanju.motiontoast.R.font.helvetica_regular))
            }
        }

        payPalNativeClient.shippingListener = object : PayPalNativeShippingListener {
            override fun onPayPalNativeShippingAddressChange(
                actions: PayPalNativePaysheetActions,
                shippingAddress: PayPalNativeShippingAddress
            ) {
                actions.approve()
            }

            override fun onPayPalNativeShippingMethodChange(
                actions: PayPalNativePaysheetActions,
                shippingMethod: PayPalNativeShippingMethod
            ) {
                try {
                    actions.approve()
                } catch (e: Exception) {
                    actions.reject()
                }
            }
        }

        val request = PayPalNativeCheckoutRequest(orderId)
        payPalNativeClient.startCheckout(request)
    }

    private suspend fun capturePayPalOrder(token: String, orderId: String): String {
        return suspendCoroutine { continuation ->
            var url = url + orderId + "/capture"

            val jsonObjectRequest = object : JsonObjectRequest(
                Method.POST, url, null,
                Response.Listener { response ->
                    Log.d("CaptureResponse", response.toString())
//                    Toast.makeText(this, "Order captured successfully!", Toast.LENGTH_SHORT).show()
                    continuation.resume("SUCCESSFULL")
                },
                Response.ErrorListener { error ->
                    Log.e("CaptureError", error.toString())
//                    Toast.makeText(this, "Error capturing order: ${error.message}", Toast.LENGTH_SHORT).show()
                    continuation.resumeWithException(error)
                }) {
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = token
                    headers["Content-Type"] = "application/json"
                    return headers
                }
            }

            queue.add(jsonObjectRequest)
        }
    }


    // Space for momo
    
    private fun calculateTotalAmount(): Float {
        var total = 0f
        for (item in selectedItems) {
            total += item.price * item.bookQuantity
        }
        return total
    }

    private fun calculateTotalVoucherShopDiscount(): Float {
        var totalVoucherShopDiscount = 0f
        for (item in selectedItems) {
            totalVoucherShopDiscount += item.voucherShopDiscount
        }
        return totalVoucherShopDiscount
    }

}