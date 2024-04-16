import android.app.Activity
import android.os.Bundle
import android.os.Handler
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
import com.example.booklette.BankCardFragment
import com.example.booklette.CartFragment
import com.example.booklette.R
import com.example.booklette.ShipAddressFragment
import com.example.booklette.databinding.FragmentCheckOutBinding
import com.example.booklette.homeActivity
import com.example.booklette.model.CartObject
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.rpc.context.AttributeContext.Resource
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class CheckOutFragment : Fragment() {
    private var _binding: FragmentCheckOutBinding? = null
    private val binding get() = _binding!!

    private lateinit var selectedItems: ArrayList<CartObject>
//    private lateinit var defaultAddress: ShipAddressObject

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var radioButtonClicked: RadioButton
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


        val totalAmount = adapter.calculateTotalAmount()
        val afterFomartedTotalAmount = String.format("%,.0f", totalAmount)
        binding.totalAmount.text = "$afterFomartedTotalAmount VND"
        binding.totalPayment.text = "$afterFomartedTotalAmount VND"
        binding.totalPaymentInPaymentDetail.text = "$afterFomartedTotalAmount VND"

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            radioButtonClicked = view.findViewById<RadioButton>(checkedId)
        }

        binding.placeOrderBtn.setOnClickListener({
            if (::radioButtonClicked.isInitialized) {
                if (radioButtonClicked.text == getString(R.string.paypalMethod)) {
//                    Toast.makeText(context, totalAmount.toString(), Toast.LENGTH_SHORT).show()
                }
            }
            else {
                MotionToast.createColorToast(
                    context as Activity,
                    getString(R.string.failed),
                    getString(R.string.haveNotPickPaymentMethodError),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(context as Activity, www.sanju.motiontoast.R.font.helvetica_regular))
            }
        })



//        binding.ivBackToPrev.setOnClickListener {
//            val cartFragment = CartFragment()
//            activity?.supportFragmentManager?.beginTransaction()
//                ?.replace(R.id.fcvNavigation, cartFragment)
//                ?.commit()
//        }

        binding.ivBackToPrev.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return view
    }

    private fun calculateTotalAmount(): Float {
        var total = 0f
        for (item in selectedItems) {
            total += item.price * item.bookQuantity
        }
        return total
    }
}