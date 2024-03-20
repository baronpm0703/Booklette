package com.example.booklette

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.ListView


class CheckoutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_check_out, container, false)

        val checkoutItems = listOf(
            CheckoutItem("Vo Chanh Tin bookstore", "Tuesday Mooney Talks to Ghosts", "Kate Racculia", "120.000", "2"),
            // Add more items here
            CheckoutItem("Vo Chanh Tin bookstore", "Tuesday Mooney Talks to Ghosts", "Kate Racculia", "120.000", "2"),
            CheckoutItem("Vo Chanh Tin bookstore", "Tuesday Mooney Talks to Ghosts", "Kate Racculia", "120.000", "2"),
            CheckoutItem("Vo Chanh Tin bookstore", "Tuesday Mooney Talks to Ghosts", "Kate Racculia", "120.000", "2"),


            )

        val lvCheckout = view.findViewById<ListView>(R.id.lvCheckout)

        val adapter = CheckOutListViewAdapter(requireContext(), checkoutItems)
        lvCheckout.adapter = adapter

        return view
    }
}