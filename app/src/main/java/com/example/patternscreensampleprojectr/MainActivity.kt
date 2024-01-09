package com.example.patternscreensampleprojectr

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.pattern.BasePatternScreen
import com.example.patternscreensampleprojectr.ui.theme.PatternScreenSampleProjectrTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PatternScreenSampleProjectrTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Column {
                        BasePatternScreen(
                            modifier = Modifier,
//                            dotSelectedColor = R.color.pattern_dot_select,
//                            dotUnselectedColor = R.color.pattern_dot_select,
//                            lineSelectedColor = R.color.pattern_dot_select,
//                            lineUnselectedColor = R.color.pattern_dot_select,
                            onPatternInput = { result ->
                                Log.i("patternResult", result)
                            }
                        )
                    }
                }
            }
        }
    }
}