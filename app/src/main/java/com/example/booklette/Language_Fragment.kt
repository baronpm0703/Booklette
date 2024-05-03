package com.example.booklette

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.booklette.databinding.FragmentLanguageBinding
import com.example.booklette.databinding.FragmentProductListBinding
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Language_Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Language_Fragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!
    lateinit var lastClick: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        val view = binding.root
        setLocale("en")
        lastClick = binding.englishLanguage
        lastClick.setBackgroundColor(Color.parseColor("#0094FF"))

        binding.vietnameseLanguage.setOnClickListener{
            setLocale("vi")
            lastClick.setBackgroundColor(Color.parseColor("#FFFFFF"))
            lastClick = binding.vietnameseLanguage
            lastClick.setBackgroundColor(Color.parseColor("#0094FF"))
        }
        binding.englishLanguage.setOnClickListener{
            setLocale("en")
            lastClick.setBackgroundColor(Color.parseColor("#FFFFFF"))
            lastClick = binding.englishLanguage
            lastClick.setBackgroundColor(Color.parseColor("#0094FF"))
        }


        return view
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)

        val displayMetrics = resources.displayMetrics
        resources.updateConfiguration(configuration, displayMetrics)

        // Store last modified language
        val preferences = activity?.getPreferences(Context.MODE_PRIVATE)
        val editor = preferences?.edit()
        if (editor != null) {
            editor.putString("selectedLanguage", languageCode)
        }
        if (editor != null) {
            editor.apply()
        }


    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Language_Fragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Language_Fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}