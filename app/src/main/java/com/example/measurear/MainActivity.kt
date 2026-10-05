package com.example.measurear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.measurear.ui.measure.MeasureScreen
import com.example.measurear.ui.theme.MeasureARTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeasureARTheme {
                MeasureScreen()
            }
        }
    }
}
