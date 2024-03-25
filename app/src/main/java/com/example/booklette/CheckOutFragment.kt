import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.CheckoutItem
import com.example.booklette.R
import com.example.booklette.ShipAddressFragment

class CheckoutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_check_out, container, false)

        val checkoutItems = listOf(
            CheckoutItem("Vo Chanh Tin bookstore", "Tuesday Mooney Talks to Ghosts", "Kate Racculia", "120.000", "2"),
            CheckoutItem("Vo Chanh Tin bookstore", "Tuesday Mooney Talks to Ghosts", "Kate Racculia", "120.000", "2"),
            CheckoutItem("Vo Chanh Tin bookstore", "Tuesday Mooney Talks to Ghosts", "Kate Racculia", "120.000", "2"),
            CheckoutItem("Vo Chanh Tin bookstore", "Tuesday Mooney Talks to Ghosts", "Kate Racculia", "120.000", "2"),
        )

        val rvCheckout = view.findViewById<RecyclerView>(R.id.rvCheckout)
        rvCheckout.layoutManager = LinearLayoutManager(requireContext())
        val adapter = CheckOutRecyclerViewAdapter(requireContext(), checkoutItems)
        rvCheckout.adapter = adapter

        val btnChangeAddress = view.findViewById<Button>(R.id.changeAddress)

        btnChangeAddress.setOnClickListener {
            val shipAddressFragment = ShipAddressFragment()
            val ft = activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fcvNavigation, shipAddressFragment)
                ?.commit()
        }

        return view
    }
}