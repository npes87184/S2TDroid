package com.npes87184.s2tdroid

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private val permissionsList: MutableList<String> = ArrayList()
    private val requestCodeAskPermission = 124

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val permissionsNeeded: MutableList<String> = ArrayList()

        if (!addPermission(
                permissionsList,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) permissionsNeeded.add("READ_EXTERNAL_STORAGE")
        if (!addPermission(
                permissionsList,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) permissionsNeeded.add("WRITE_EXTERNAL_STORAGE")

        if (permissionsList.size > 0) {
            showUpPermissionDialog()
        }
    }

    private fun showUpPermissionDialog() {
        val alertDialog: AlertDialog = this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(getString(R.string.app_name))
                setMessage(getString(R.string.file_permission))
                setNegativeButton(android.R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        finish()
                    })
                setPositiveButton(android.R.string.ok,
                    DialogInterface.OnClickListener { dialog, id ->
                        ActivityCompat.requestPermissions(
                            this@MainActivity,
                            permissionsList.toTypedArray(),
                            requestCodeAskPermission
                        )
                    })
            }

            // Create the AlertDialog
            builder.create()
        }
        alertDialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun addPermission(permissionsList: MutableList<String>, permission: String): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsList.add(permission)
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    permission
                )
            ) return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            requestCodeAskPermission -> {
                val perms: MutableMap<String, Int> = HashMap()
                // Initial
                perms[Manifest.permission.READ_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] =
                    PackageManager.PERMISSION_GRANTED

                // Fill with results
                var i = 0
                while (i < permissions.size) {
                    perms[permissions[i]] = grantResults[i]
                    i++
                }
                // Check for ACCESS_FINE_LOCATION
                if (perms[Manifest.permission.READ_EXTERNAL_STORAGE] != PackageManager.PERMISSION_GRANTED
                    || perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] != PackageManager.PERMISSION_GRANTED
                ) {
                    showUpPermissionDialog()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}