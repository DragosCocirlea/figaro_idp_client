package com.akaaka.figaro

import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import khttp.post as httpPost

import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), HomeFragment.OnFragmentInteractionListener, ProfileFragment.OnFragmentInteractionListener, SearchFragment.OnFragmentInteractionListener {

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private lateinit var auth: FirebaseAuth

    lateinit var profileFragment : ProfileFragment
    lateinit var homeFragment : HomeFragment
    lateinit var searchFragment: SearchFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        profileFragment = ProfileFragment.newInstance()
        homeFragment = HomeFragment.newInstance()
        searchFragment = SearchFragment.newInstance()

        val bottomNavigation = bottom_navigation

        val item1 = AHBottomNavigationItem("Account", R.drawable.account, R.color.color_tab_1)
        val item2 = AHBottomNavigationItem("Home", R.drawable.home, R.color.color_tab_2)
        val item3 = AHBottomNavigationItem("Search", R.drawable.magnify, R.color.color_tab_3)

        bottom_navigation.addItem(item1)
        bottom_navigation.addItem(item2)
        bottom_navigation.addItem(item3)

        bottom_navigation.currentItem = 1
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, homeFragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()

        bottom_navigation.setOnTabSelectedListener { position, wasSelected ->
            if (!wasSelected)
                when (position) {
                    0 -> supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.container, profileFragment)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .commit()
                    1 -> supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.container, homeFragment)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .commit()
                    2 -> supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.container, searchFragment)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .commit()
                }

            true
        }

//        Toast.makeText(this, "User logged in is ${auth.currentUser!!.email}", Toast.LENGTH_LONG).show();
    }

    override fun onBackPressed() {

        if (bottom_navigation.currentItem == 1)
            super.onBackPressed()

        bottom_navigation.currentItem = 1
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, homeFragment)
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .commit()

    }
}


