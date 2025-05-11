package com.patternlockscreen.patternscreensampleprojectr

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.patternscreensampleprojectr.R
import com.screen.lock.pattern.BasePatternScreen
import com.screen.lock.pattern.DrawingSetting

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
              modifier = Modifier.size(400.dp),
              drawingSetting = DrawingSetting(
                dotSize = 10.dp,
                lineWidth = 4.dp,
                lineColor = Color.Cyan,
                selectedDotColor = Color.Magenta,
                unselectedDotColor = Color.Blue,
                minimumLineConnectionCount = 3,
                vibrateTime = 20L
              ),
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
  Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

@Preview
@Composable
fun PatternPreview() {

  MaterialTheme {
    Column(
      modifier = Modifier.background(Color.White)
    ) {
      Spacer(modifier = Modifier.weight(1f))
      BasePatternScreen(
        modifier = Modifier.size(400.dp),
        drawingSetting = DrawingSetting(
          dotSize = 10.dp,
          lineWidth = 4.dp,
          lineColor = Color.Cyan,
          selectedDotColor = Color.Magenta,
          unselectedDotColor = Color.Blue,
          minimumLineConnectionCount = 3,
        ),
        onLessCountPatternSelected = { },
        onPatternSuccessfullySelected = { }
      )
      Spacer(modifier = Modifier.weight(1f))
    }
  }

}