package com.giriraj.cabalert

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("cab_alert_prefs", MODE_PRIVATE)

        val packageInput = findViewById<EditText>(R.id.packageInput)
        val thresholdInput = findViewById<EditText>(R.id.thresholdInput)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val openAccessibilityButton = findViewById<Button>(R.id.openAccessibilityButton)

        packageInput.setText(prefs.getString("target_package", "com.routemetic.driver"))
        thresholdInput.setText(prefs.getInt("threshold_minutes", 3).toString())

        saveButton.setOnClickListener {
            val pkg = packageInput.text.toString().trim()
            val threshold = thresholdInput.text.toString().toIntOrNull() ?: 3
            prefs.edit()
                .putString("target_package", pkg)
                .putInt("threshold_minutes", threshold)
                .apply()
            Toast.makeText(this, "Saved. Package: $pkg, Alert at: $threshold min", Toast.LENGTH_LONG).show()
        }

        openAccessibilityButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }
}
