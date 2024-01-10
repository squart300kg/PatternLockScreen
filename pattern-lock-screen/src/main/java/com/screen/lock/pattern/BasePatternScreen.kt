package com.screen.lock.pattern

import android.content.Context
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

private typealias RowDotOffsets = MutableList<Offset>
private typealias LineOffset = Pair<Offset, Offset>
data class BasePatternDrawingUiState(
    val threeByThreeDotOffsets: MutableList<RowDotOffsets> =
        MutableList(DOT_SIZE) { MutableList(DOT_SIZE) { Offset.Zero} },
    val lineOffsets: MutableList<LineOffset> = mutableListOf(),
    val latestSelectedDotOffset: Offset = Offset.Zero,
    val draggingOffset: Offset = Offset.Zero,
    val selectedOffsets: MutableList<Offset> = mutableListOf(),
    val password: MutableList<Int> = mutableListOf()
) {

    fun getDotColor(
        rowIndex: Int,
        colIndex: Int,
        selectedDotColor: Color,
        unselectedDotColor: Color
    ): Color =
        if (selectedOffsets.contains(threeByThreeDotOffsets[rowIndex][colIndex])) {
            selectedDotColor
        } else {
            unselectedDotColor
        }

    companion object {
        internal const val DOT_SIZE = 3
    }
}

data class DrawingSetting(
    val dotSize: Dp = 10.dp,
    val lineWidth: Dp = 4.dp,
    val lineColor: Color = Color.Blue,
    val selectedDotColor: Color = Color.Blue,
    val unselectedDotColor: Color = Color.Gray,
    val minimumLineConnectionCount: Int = 2,
    val vibrateTime: Long = 20L,
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColumnScope.BasePatternScreen(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    drawingSetting: DrawingSetting = DrawingSetting(),
    onLessCountPatternSelected: (Int) -> Unit,
    onPatternSuccessfullySelected: (String) -> Unit) {

    var uiState by remember { mutableStateOf(BasePatternDrawingUiState()) }

    Canvas(
        modifier = modifier
            .size(264.dp)
            .align(Alignment.CenterHorizontally)
            .pointerInteropFilter(
                onTouchEvent = { motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            val firstTabOffset = Offset(
                                x = motionEvent.x,
                                y = motionEvent.y
                            )

                            repeat(BasePatternDrawingUiState.DOT_SIZE) { rowIndex ->
                                repeat(BasePatternDrawingUiState.DOT_SIZE) { colIndex ->
                                    val dotOffset =
                                        uiState.threeByThreeDotOffsets[rowIndex][colIndex]
                                    if (firstTabOffset.is80DpCloserThan(dotOffset)) {

                                        CommonUtil.vibrate(
                                            context,
                                            drawingSetting.vibrateTime
                                        )
                                        Offset(
                                            x = dotOffset.x,
                                            y = dotOffset.y
                                        ).let { firstSelectedOffset ->
                                            uiState =
                                                uiState.copy(
                                                    selectedOffsets = uiState.selectedOffsets.apply {
                                                        add(firstSelectedOffset)
                                                    },
                                                    latestSelectedDotOffset = firstSelectedOffset,
                                                    draggingOffset = firstSelectedOffset,
                                                    password = uiState.password.apply {
                                                        add(
                                                            element = getPasswordByDotOffsetIndex(
                                                                rowIndex = rowIndex,
                                                                colIndex = colIndex
                                                            )
                                                        )
                                                    }
                                                )
                                        }
                                    }
                                }
                            }
                        }

                        MotionEvent.ACTION_MOVE -> {
                            uiState =
                                uiState.copy(
                                    draggingOffset = Offset(
                                        x = motionEvent.x,
                                        y = motionEvent.y
                                    )
                                )

                            repeat(BasePatternDrawingUiState.DOT_SIZE) { rowDotIndex ->
                                repeat(BasePatternDrawingUiState.DOT_SIZE) { colDotIndex ->
                                    val dotOffset =
                                        uiState.threeByThreeDotOffsets[rowDotIndex][colDotIndex]

                                    if (uiState.draggingOffset.is80DpCloserThan(dotOffset)) {

                                        if (!uiState.selectedOffsets.contains(dotOffset)) {

                                            SkipDotChecker(
                                                rowDotIndex = rowDotIndex,
                                                colDotIndex = colDotIndex,
                                                targetUiModel = uiState
                                            ).run {
                                                if (isSkippedMiddleDot()) {
                                                    if (isMiddleDotNotSelected()) {
                                                        CommonUtil.vibrate(
                                                            context,
                                                            drawingSetting.vibrateTime
                                                        )
                                                        drawDotAndLine(
                                                            currentUiState = uiState,
                                                            destinationOffset = middleOffset,
                                                            onDrawSuccess = { newUiState ->
                                                                uiState = newUiState
                                                            }
                                                        )
                                                        savePassword(
                                                            currentUiState = uiState,
                                                            password = password,
                                                            onDrawSuccess = { newUiState ->
                                                                uiState = newUiState
                                                            }
                                                        )
                                                    }
                                                }
                                            }

                                            CommonUtil.vibrate(
                                                context,
                                                drawingSetting.vibrateTime
                                            )
                                            drawDotAndLine(
                                                currentUiState = uiState,
                                                destinationOffset = dotOffset,
                                                onDrawSuccess = { newUiState ->
                                                    uiState = newUiState
                                                }
                                            )
                                            savePassword(
                                                currentUiState = uiState,
                                                password = getPasswordByDotOffsetIndex(
                                                    rowIndex = rowDotIndex,
                                                    colIndex = colDotIndex
                                                ),
                                                onDrawSuccess = { newUiState ->
                                                    uiState = newUiState
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        MotionEvent.ACTION_UP -> {
                            if (uiState.selectedOffsets.size >= drawingSetting.minimumLineConnectionCount + 1) {
                                onPatternSuccessfullySelected(
                                    uiState.password
                                        .toMutableList()
                                        .joinToString("")
                                )
                            } else {
                                onLessCountPatternSelected(drawingSetting.minimumLineConnectionCount)
                            }

                            uiState = BasePatternDrawingUiState()

                        }
                    }
                    true
                }
            )
            .drawWithContent {
                drawContent()
                if (uiState.latestSelectedDotOffset != Offset.Zero) {
                    drawLine(
                        color = drawingSetting.lineColor,
                        start = uiState.latestSelectedDotOffset,
                        end = uiState.draggingOffset,
                        strokeWidth = convertDpToPx(context = context, dp = drawingSetting.lineWidth)
                    )
                }
            },
        onDraw = {
            val dotHorizontalMargin = size.width / 6
            val dotVerticalMargin = size.height / 6

            repeat(BasePatternDrawingUiState.DOT_SIZE) { rowIndex ->
                val rowDotPositions = mutableListOf<Offset>()
                repeat(BasePatternDrawingUiState.DOT_SIZE) { colIndex ->
                    val positionOfX = dotHorizontalMargin * (2 * colIndex + 1)
                    val positionOfY = dotVerticalMargin * (2 * rowIndex + 1)
                    val colPosition = Offset(
                        x = positionOfX,
                        y = positionOfY
                    )

                    drawCircle(
                        color = uiState.getDotColor(
                            rowIndex = rowIndex,
                            colIndex = colIndex,
                            selectedDotColor = drawingSetting.selectedDotColor,
                            unselectedDotColor = drawingSetting.unselectedDotColor),
                        center = colPosition,
                        radius = drawingSetting.dotSize.toPx()
                    )

                    rowDotPositions.add(
                        index = colIndex,
                        element = colPosition
                    )
                }

                uiState.threeByThreeDotOffsets[rowIndex] = rowDotPositions

            }

            repeat(uiState.lineOffsets.size) { lineIndex ->
                if (uiState.lineOffsets[lineIndex].first != Offset.Zero) {
                    drawLine(
                        color = drawingSetting.lineColor,
                        start = uiState.lineOffsets[lineIndex].first,
                        end = uiState.lineOffsets[lineIndex].second,
                        strokeWidth = convertDpToPx(context = context, dp = drawingSetting.lineWidth)
                    )
                }
            }
        })
}

private fun Offset.is80DpCloserThan(target: Offset): Boolean {
    val barrierDot = 10.dp
    return abs(this.x - target.x).dp <= barrierDot * 8 &&
           abs(this.y - target.y).dp <= barrierDot * 8
}


private fun drawDotAndLine(
    currentUiState: BasePatternDrawingUiState,
    destinationOffset: Offset,
    onDrawSuccess: (BasePatternDrawingUiState) -> Unit
) {
    onDrawSuccess(
        currentUiState.copy(
            lineOffsets = currentUiState.lineOffsets.apply {
                add(
                    Pair(
                        first = currentUiState.latestSelectedDotOffset,
                        second = destinationOffset)
                )
            },
            selectedOffsets = currentUiState.selectedOffsets.apply { add(destinationOffset) },
            latestSelectedDotOffset = destinationOffset
        ),

    )
}

private fun savePassword(
    currentUiState: BasePatternDrawingUiState,
    password: Int,
    onDrawSuccess: (BasePatternDrawingUiState) -> Unit
) {
    onDrawSuccess(
        currentUiState.copy(
            password = currentUiState.password.apply { add(password) }
        )
    )
}

private fun convertDpToPx(context: Context, dp: Dp): Float {
    val density = context.resources.displayMetrics.density
    return dp.value * density
}