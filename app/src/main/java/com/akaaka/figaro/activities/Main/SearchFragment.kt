package com.akaaka.figaro.activities.Main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import org.json.JSONObject
import kotlinx.android.synthetic.main.fragment_search.*
import org.json.JSONArray
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.akaaka.figaro.R
import com.akaaka.figaro.activities.Main.controller.BarbershopAdapter
import com.akaaka.figaro.activities.Main.model.BarbershopData
import com.akaaka.figaro.activities.Main.model.jsonToBarbershop
import com.akaaka.figaro.network.NetworkUtils


class SearchFragment : Fragment() {
    private lateinit var parentActivity: Activity
    private lateinit var jsonLocation : JSONObject
    private val bbsList = ArrayList<BarbershopData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { return inflater.inflate(R.layout.fragment_search, container, false) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler_view.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        val item1 = AHBottomNavigationItem("Distance", R.drawable.distance)
        val item2 = AHBottomNavigationItem("Name", R.drawable.text)
        val item3 = AHBottomNavigationItem("Rating", R.drawable.star)

        search_top_navigation.addItem(item1)
        search_top_navigation.addItem(item2)
        search_top_navigation.addItem(item3)
        search_top_navigation.isBehaviorTranslationEnabled = false
        search_top_navigation.currentItem = 1

        jsonLocation = getLocation()

        // listeners
        search_top_navigation.setOnTabSelectedListener { position, wasSelected ->
            if (wasSelected)
                return@setOnTabSelectedListener false

            when (position) {
                0 -> {
                    search_name.visibility = View.GONE

                    // create POST payload
                    val jsonData = JSONObject(jsonLocation.toString())
                    jsonData.put("criteria", "distance")

                    // create request
                    val reqBBS = NetworkUtils.httpRequest("post", "figaro/barbershop", jsonData)

                    // make request
                    NetworkUtils.makeRefreshingRequest(reqBBS, ::setBarbershops, parentActivity, false)
                }
                1 -> {
                    search_name.visibility = View.VISIBLE
                }
                2 -> {
                    search_name.visibility = View.GONE

                    val jsonData = JSONObject(jsonLocation.toString())
                    jsonData.put("criteria", "rating")
                    val reqBBS = NetworkUtils.httpRequest("post", "figaro/barbershop", jsonData)
                    NetworkUtils.makeRefreshingRequest(reqBBS, ::setBarbershops, parentActivity, false)
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
                    val jsonData = JSONObject(jsonLocation.toString())
                    jsonData.put("criteria", "name")
                    jsonData.put("name", v.text)

                    val reqBBS = NetworkUtils.httpRequest("post", "figaro/barbershop", jsonData)
                    NetworkUtils.makeRefreshingRequest(reqBBS, ::setBarbershops, parentActivity, false)
                    return true
                }

                return false
            }
        } )
    }

    private fun getLocation() : JSONObject{
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 111)
        }

        val mLocationManager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = mLocationManager.getProviders(true)
        var location : Location? = null

        for (provider in providers) {
            val l = mLocationManager.getLastKnownLocation(provider) ?: continue

            if (location == null || l.accuracy < location.accuracy)
                location = l
        }

        val ret = JSONObject()

        if (location == null) {
            Toast.makeText(activity, "NU MERGE LUATA LOCATIA", Toast.LENGTH_LONG).show()
            ret.put("coordX", 0)
            ret.put("coordY", 0)
        }
        else {
            ret.put("coordX", location.latitude)
            ret.put("coordY", location.longitude)
        }

        return ret
    }

    fun setBarbershops(responseData: String) {
        if (recycler_view == null)
            return

        bbsList.removeAll(bbsList)

        val arr = JSONArray(responseData)
        for (i : Int in 0 until arr.length())
            bbsList.add(jsonToBarbershop(arr.getJSONObject(i)))

        recycler_view.adapter = BarbershopAdapter(bbsList, parentActivity)
    }

    override fun onResume() {
        super.onResume()
        search_top_navigation.currentItem = 1
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is Activity)
            parentActivity = context
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SearchFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}