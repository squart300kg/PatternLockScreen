package com.example.pattern

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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import kotlin.math.abs

private typealias RowDotOffsets = MutableList<Offset>
private typealias LineOffset = Pair<Offset, Offset>
data class BasePatternDrawingUiState(
    val threeByThreeDotOffsets: MutableList<RowDotOffsets> = MutableList(DOT_SIZE) { MutableList(
        DOT_SIZE
    ) { Offset.Zero} },
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
        val DOT_RADIUS = 10.dp
        const val DOT_SIZE = 3
        const val LINE_STROKE_WIDTH = 9f
        const val VIBRATOR_MILLS = 20L
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColumnScope.BasePatternScreen(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
    selectedDotColor: Color = colorResource(id = R.color.selected_dot),
    unselectedDotColor: Color = colorResource(id = R.color.unselected_dot),
    lineColor: Color = colorResource(id = R.color.line),
    minimumLineConnectionCount: Int = 2,
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
                                            BasePatternDrawingUiState.VIBRATOR_MILLS
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
                                                            BasePatternDrawingUiState.VIBRATOR_MILLS
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
                                                BasePatternDrawingUiState.VIBRATOR_MILLS
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
                            if (uiState.selectedOffsets.size >= minimumLineConnectionCount + 1) {
                                onPatternSuccessfullySelected(
                                    uiState.password
                                        .toMutableList()
                                        .joinToString("")
                                )
                            } else {
                                onLessCountPatternSelected(minimumLineConnectionCount)
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
                        color = lineColor,
                        start = uiState.latestSelectedDotOffset,
                        end = uiState.draggingOffset,
                        strokeWidth = BasePatternDrawingUiState.LINE_STROKE_WIDTH
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
                            selectedDotColor = selectedDotColor,
                            unselectedDotColor = unselectedDotColor),
                        center = colPosition,
                        radius = BasePatternDrawingUiState.DOT_RADIUS.toPx()
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
                        color = lineColor,
                        start = uiState.lineOffsets[lineIndex].first,
                        end = uiState.lineOffsets[lineIndex].second,
                        strokeWidth = BasePatternDrawingUiState.LINE_STROKE_WIDTH
                    )
                }
            }
        })
}

private fun Offset.is80DpCloserThan(target: Offset): Boolean =
    abs(this.x - target.x).dp <= BasePatternDrawingUiState.DOT_RADIUS * 8 &&
            abs(this.y - target.y).dp <= BasePatternDrawingUiState.DOT_RADIUS * 8

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