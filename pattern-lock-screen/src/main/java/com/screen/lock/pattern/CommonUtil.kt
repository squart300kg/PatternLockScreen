package com.screen.lock.pattern

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object CommonUtil {

    fun vibrate(context: Context, millSecond: Long = 5) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
                .defaultVibrator
                .vibrate(
                    VibrationEffect.createOneShot(
                        millSecond,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
        } else {
            (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(millSecond)
        }
    }
}