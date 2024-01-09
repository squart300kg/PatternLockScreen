package com.example.patternscreensampleprojectr

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.pattern.BasePatternScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White,
                ) {
                    Column {
                        Spacer(modifier = Modifier.weight(1f))
                        BasePatternScreen(
                            modifier = Modifier.size(300.dp),
                            lineColor = Color.Cyan,
                            unselectedDotColor = Color.Red,
                            selectedDotColor = Color.Blue,
                            minimumLineConnectionCount = 3,
                            onLessCountPatternSelected = { selectedCount ->
                                showToast(getString(R.string.lessPatternSelectedGuide, selectedCount))
                            },
                            onPatternSuccessfullySelected = { result ->
                                showToast(getString(R.string.patternSuccessfullySelectedGuide, result))
                            }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun ComponentActivity.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}