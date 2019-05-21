package com.akaaka.figaro

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_search.*
import org.json.JSONArray
import org.json.JSONObject

class HomeFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null

    private lateinit var auth: FirebaseAuth
    var id : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }

        auth = FirebaseAuth.getInstance()
        id = auth.currentUser!!.email.toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val AppList = ArrayList<AppointmentsData>()

        rv_main.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rv_main.adapter = MyAdapterMain(AppList, context!!)

        val data = JSONObject()
            .put("User_ID", id)
        val ip = "http://18.197.8.98:5070/getappointment"
        val req = ip.httpPost().jsonBody(data.toString())

        req.header("Content-Type", "application/json")
        req.response { _, response, result ->
            when (result) {
                is Result.Success -> {
                    AppList.removeAll(AppList)

                    val arr = JSONArray(String(response.data))
                    for (i : Int in 0 until arr.length()) {
                        val entry = arr.getJSONArray(i)
                        AppList.add(i, AppointmentsData(entry.getString(0), entry.getString(1), entry.getDouble(2), entry.getDouble(3),
                            entry.getString(4), entry.getInt(6), entry.getInt(7), entry.getInt(8), entry.getInt(9), entry.getInt(10),
                            entry.getInt(14), entry.getInt(15)))
                    }

                    rv_main.adapter = MyAdapterMain(AppList, context!!)
                }
            }
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
