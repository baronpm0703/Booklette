import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.BankCardFragment
import com.example.booklette.R
import com.example.booklette.ShipAddressFragment
import com.example.booklette.databinding.FragmentCheckOutBinding
import com.example.booklette.model.CartObject

class CheckOutFragment : Fragment() {
    private var _binding: FragmentCheckOutBinding? = null
    private val binding get() = _binding!!

    private lateinit var selectedItems: ArrayList<CartObject>
    companion object {
        fun newInstance(selectedItems: ArrayList<CartObject>): CheckOutFragment {
            val fragment = CheckOutFragment()
            val args = Bundle()
            args.putParcelableArrayList("SELECTED_ITEMS", selectedItems)
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

        val adapter = CheckOutRecyclerViewAdapter(requireContext(), selectedItems)
        binding.rvCheckout.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCheckout.adapter = adapter

        binding.changeAddress.setOnClickListener {
            val shipAddressFragment = ShipAddressFragment()
            val ft = activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fcvNavigation, shipAddressFragment)
                ?.commit()
        }
        binding.changeCardBtn.setOnClickListener {
            val bankCardFragment = BankCardFragment()
            val ft = activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fcvNavigation, bankCardFragment)
                ?.commit()
        }

        return view
    }
}