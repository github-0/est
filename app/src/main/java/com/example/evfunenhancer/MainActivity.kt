package com.example.evfunenhancer

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.evfunenhancer.navigation.NavGraph
import com.example.evfunenhancer.ui.theme.EvFunEnhancerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.parseColor("#1A1730"))
        )
        setContent {
            EvFunEnhancerTheme {
                NavGraph()
            }
        }
    }
}
