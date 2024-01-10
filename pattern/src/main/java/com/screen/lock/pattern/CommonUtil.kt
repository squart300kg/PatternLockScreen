package com.screen.lock.pattern

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object CommonUtil {

    fun vibrate(context: Context, millSecond: Long = 5) {
        getVibrator(context).vibrate(
            VibrationEffect.createOneShot(
                millSecond,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    }

    private fun getVibrator(context: Context) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

}