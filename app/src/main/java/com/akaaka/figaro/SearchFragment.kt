package com.akaaka.figaro

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import org.json.JSONObject
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.fragment_search.*
import org.json.JSONArray
import androidx.core.content.ContextCompat.getSystemService
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class SearchFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }

        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 111)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val bbsList = ArrayList<BBSData>()
        recycler_view.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recycler_view.adapter = MyAdapter(bbsList, context!!)

        val item1 = AHBottomNavigationItem("Distance", R.drawable.distance, R.color.color_tab_1)
        val item2 = AHBottomNavigationItem("Name", R.drawable.text, R.color.color_tab_2)
        val item3 = AHBottomNavigationItem("Rating", R.drawable.star, R.color.color_tab_3)

        search_top_navigation.addItem(item1)
        search_top_navigation.addItem(item2)
        search_top_navigation.addItem(item3)
        search_top_navigation.isBehaviorTranslationEnabled = false

        search_top_navigation.currentItem = 1

        search_top_navigation.setOnTabSelectedListener { position, wasSelected ->
            if (!wasSelected)
                when (position) {
                    0 -> {
                        search_name.visibility = View.GONE
                        bbsList.removeAll(bbsList)

                        val mLocationManager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        val providers = mLocationManager.getProviders(true)
                        var location : Location? = null

                        for (provider in providers) {
                            val l = mLocationManager.getLastKnownLocation(provider)

                            if (l == null)
                                continue

                            if (location == null || l.accuracy < location.accuracy)
                                location = l
                        }

                        if (location == null) {
                            Toast.makeText(activity, "NU MERGE LUATA LOCATIA", Toast.LENGTH_LONG).show()
                            return@setOnTabSelectedListener false
                        }

                        val lat = location.latitude.toString()
                        val long = location.longitude.toString()

                        // create POST payload
                        val data = JSONObject()
                        data.put("Criteria", "distance")
                        data.put("Name", "none")
                        data.put("CoordX", lat)
                        data.put("CoordY", long)

                        val ip = "http://18.197.8.98:5070/search"
                        val req = ip.httpPost().jsonBody(data.toString())

                        req.header("Content-Type", "application/json")
                        req.response { _, response, result ->
                            when (result) {
                                is Result.Success -> {
                                    recycler_view.visibility = View.VISIBLE

                                    bbsList.removeAll(bbsList)

                                    val arr = JSONObject(String(response.data)).getJSONArray("R")
                                    for (i : Int in 0 until arr.length()) {
                                        val entry = arr.getJSONArray(i)
                                        val obj = entry.getJSONArray(0)
                                        bbsList.add(BBSData(obj.getInt(0), obj.getString(1), obj.getDouble(2), obj.getString(3), entry.getDouble(1)))
                                    }

                                    recycler_view.adapter = MyAdapter(bbsList, context!!)

                                }
                            }
                        }


                    }
                    1 -> {
                        search_name.visibility = View.VISIBLE
                        bbsList.removeAll(bbsList)
                        recycler_view.adapter = MyAdapter(bbsList, context!!)
                    }
                    2 -> {
                        search_name.visibility = View.GONE
                        bbsList.removeAll(bbsList)

                        // create POST payload
                        val data = JSONObject()
                        data.put("Criteria", "rating")
                        data.put("Name", "none")
                        data.put("CoordX", "0")
                        data.put("CoordY", "0")

                        val ip = "http://18.197.8.98:5070/search"
                        val req = ip.httpPost().jsonBody(data.toString())

                        req.header("Content-Type", "application/json")
                        req.response { _, response, result ->
                            when (result) {
                                is Result.Success -> {
                                    recycler_view.visibility = View.VISIBLE

                                    bbsList.removeAll(bbsList)

                                    val arr = JSONObject(String(response.data)).getJSONArray("R")
                                    for (i : Int in 0 until arr.length()) {
                                        val obj = arr.getJSONArray(i)
                                        bbsList.add(BBSData(obj.getInt(0), obj.getString(1), obj.getDouble(2), obj.getString(3)))
                                    }

                                    recycler_view.adapter = MyAdapter(bbsList, context!!)
                                }
                            }

                        }
                    }
                }

            true
        }

        search_name.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v!!.windowToken, 0)

                    // create POST payload
                    val data = JSONObject()
                    data.put("Criteria", "name")
                    data.put("Name", v!!.text)
                    data.put("CoordX", "0")
                    data.put("CoordY", "0")

                    val ip = "http://18.197.8.98:5070/search"
                    val req = ip.httpPost().jsonBody(data.toString())

                    req.header("Content-Type", "application/json")
                    req.response { request, response, result ->
                        when (result) {
                            is Result.Success -> {
                                recycler_view.visibility = View.VISIBLE

                                bbsList.removeAll(bbsList)

                                val arr = JSONObject(String(response.data)).getJSONArray("R")
                                for (i : Int in 0 until arr.length()) {
                                    val obj = arr.getJSONArray(i)
                                    bbsList.add(BBSData(obj.getInt(0), obj.getString(1), obj.getDouble(2), obj.getString(3)))
                                }

                                recycler_view.adapter = MyAdapter(bbsList, context!!)
                            }
                        }

                    }

                    return true
                }

                return false
            }
        } )
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onResume() {
        super.onResume()

        search_top_navigation.currentItem = 1
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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            SearchFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }


    class AsyncSearchPOST(val activity: Context) : AsyncTask<Pair<String, JSONObject>, String, JSONObject>() {

        override fun doInBackground(vararg params: Pair<String, JSONObject>?) : JSONObject? {
            val input = params[0]

            val type = input!!.first
            val body = input.second
            var ret : JSONObject? = null
            val lock = Any()

            synchronized(lock) {

                val ip = "http://18.197.8.98:5070/search"
                val req = ip.httpPost().jsonBody(body.toString())

                req.header("Content-Type", "application/json")
                req.response { request, response, result ->
                    when (result) {
                        is Result.Success -> {
                            ret = JSONObject(String(response.data))
                            Toast.makeText(activity, "a iesit din async: ${String(response.data)}", Toast.LENGTH_LONG).show()
                        }
                    }

                }

            }


            return ret
        }

        override fun onPostExecute(result: JSONObject?) {
            super.onPostExecute(result)

            Toast.makeText(activity, "a iesit din async: ${result.toString()}", Toast.LENGTH_LONG).show()
        }

        override fun onPreExecute() {
            super.onPreExecute()

            Toast.makeText(activity, "a intrat in async", Toast.LENGTH_LONG).show()
        }
    }

}

