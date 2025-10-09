package edu.bluejack25_1.synwc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import edu.bluejack25_1.synwc.ui.navigation.AppNavGraph
import edu.bluejack25_1.synwc.ui.theme.SyNWcTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SyNWcTheme { // will automatically detect dark/light
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController)
                }
            }
        }
    }
}