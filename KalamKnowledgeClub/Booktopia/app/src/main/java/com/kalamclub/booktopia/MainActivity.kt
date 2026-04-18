package com.kalamclub.booktopia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.kalamclub.booktopia.navigation.BooktopiaNavigation
import com.kalamclub.booktopia.ui.theme.BooktopiaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            BooktopiaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    BooktopiaNavigation()
                }
            }
        }
    }
}
