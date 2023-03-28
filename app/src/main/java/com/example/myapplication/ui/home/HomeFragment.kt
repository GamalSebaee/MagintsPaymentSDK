package com.example.myapplication.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.databinding.FragmentHomeBinding
import com.magints.nbe_sdk.MagintsNBESDK
import com.magints.nbe_sdk.utils.PaymentEnvironment

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        /*homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }*/
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnConfirmPayment.setOnClickListener {
            startPaymentCycle()
        }
    }

    private fun startPaymentCycle() {
        val magintsNBESDK = MagintsNBESDK(
            activity, "TESTEGPTEST", "c622b7e9e550292df400be7d3e846476", 61, PaymentEnvironment.test
        )
        val paymentSession = binding.etSession.text.toString()
        if (paymentSession.isNotEmpty()) {
            magintsNBESDK.isCreateSessionAutomatically = false
            magintsNBESDK.sessionToken = paymentSession
        }
        magintsNBESDK.amount = "10.00"
        //magintsNBESDK.currency="EGP";
        magintsNBESDK.initPayment {
            if (it != null) {
                if (it.isStatus) {
                    Log.d("magintsNBESDKCallBack", "Card info : " + it.savedCardInfo.toString())
                    Toast.makeText(activity, "Done Payment process", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(activity, "" + it.failMessage, Toast.LENGTH_LONG).show()
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
