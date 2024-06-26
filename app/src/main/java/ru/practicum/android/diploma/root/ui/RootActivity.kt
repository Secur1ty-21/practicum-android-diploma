package ru.practicum.android.diploma.root.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.ActivityRootBinding

class RootActivity : AppCompatActivity() {

    private var binding: ActivityRootBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(binding!!.rootFragmentContainerView.id) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(binding!!.bottomNavigationView.id)
        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.searchFragment, R.id.favouritesFragment, R.id.teamFragment -> {
                    binding!!.bottomNavigationView.visibility = View.VISIBLE
                    binding!!.horizontalLine.visibility = View.VISIBLE
                }

                else -> {
                    binding!!.bottomNavigationView.visibility = View.GONE
                    binding!!.horizontalLine.visibility = View.GONE
                }
            }
        }
    }
}
