package com.visiontwin.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.visiontwin.app.data.api.RetrofitClient
import com.visiontwin.app.data.cache.CacheManager
import com.visiontwin.app.data.repository.VisionTwinRepository
import com.visiontwin.app.ui.navigation.NavGraph
import com.visiontwin.app.ui.theme.ScreenBackground
import com.visiontwin.app.ui.theme.VisionTwinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val cacheManager = CacheManager(applicationContext)
        val repository = VisionTwinRepository(RetrofitClient.apiService, cacheManager)

        setContent {
            VisionTwinTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ScreenBackground
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        repository = repository
                    )
                }
            }
        }
    }
}
