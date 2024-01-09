package com.example.pattern

import android.content.Context
import android.view.MotionEvent
import androidx.annotation.ColorRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
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

    fun getDotColor(rowIndex: Int, colIndex: Int): Int =
        if (selectedOffsets.contains(threeByThreeDotOffsets[rowIndex][colIndex])) {
            R.color.pattern_dot_select
        } else {
            R.color.pattern_dot_unselect
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
    @ColorRes dotSelectedColor: Int = R.color.pattern_dot_select,
    @ColorRes dotUnselectedColor: Int = R.color.pattern_dot_unselect,
    @ColorRes lineSelectedColor: Int = R.color.line_dot_select,
    @ColorRes lineUnselectedColor: Int = R.color.line_dot_unselect,
    minimumLineConnectionCount: Int = 2,
    onPatternInput: (String) -> Unit) {

    val basePatternDrawingUiState = remember { mutableStateOf( BasePatternDrawingUiState()) }

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
                                        basePatternDrawingUiState.value.threeByThreeDotOffsets[rowIndex][colIndex]
                                    if (firstTabOffset.is80DpCloserThan(
                                            dotOffset
                                        )
                                    ) {

                                        CommonUtil.vibrate(
                                            context,
                                            BasePatternDrawingUiState.VIBRATOR_MILLS
                                        )
                                        Offset(
                                            x = dotOffset.x,
                                            y = dotOffset.y
                                        ).let { firstSelectedOffset ->
                                            basePatternDrawingUiState.value =
                                                basePatternDrawingUiState.value.copy(
                                                    selectedOffsets = basePatternDrawingUiState.value.selectedOffsets.apply {
                                                        add(
                                                            element = firstSelectedOffset
                                                        )
                                                    },
                                                    latestSelectedDotOffset = firstSelectedOffset,
                                                    draggingOffset = firstSelectedOffset,
                                                    password = basePatternDrawingUiState.value.password.apply {
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
                            basePatternDrawingUiState.value =
                                basePatternDrawingUiState.value.copy(
                                    draggingOffset = Offset(
                                        x = motionEvent.x,
                                        y = motionEvent.y
                                    )
                                )

                            repeat(BasePatternDrawingUiState.DOT_SIZE) { rowDotIndex ->
                                repeat(BasePatternDrawingUiState.DOT_SIZE) { colDotIndex ->
                                    val dotOffset =
                                        basePatternDrawingUiState.value.threeByThreeDotOffsets[rowDotIndex][colDotIndex]

                                    if (basePatternDrawingUiState.value.draggingOffset.is80DpCloserThan(
                                            dotOffset
                                        )
                                    ) {

                                        if (!basePatternDrawingUiState.value.selectedOffsets.contains(
                                                dotOffset
                                            )
                                        ) {

                                            SkipDotChecker(
                                                rowDotIndex = rowDotIndex,
                                                colDotIndex = colDotIndex,
                                                targetUiModel = basePatternDrawingUiState
                                            ).run {
                                                if (isSkippedMiddleDot()) {
                                                    if (isMiddleDotNotSelected()) {
                                                        CommonUtil.vibrate(
                                                            context,
                                                            BasePatternDrawingUiState.VIBRATOR_MILLS
                                                        )
                                                        basePatternDrawingUiState.drawDotAndLine(
                                                            destinationOffset = middleOffset
                                                        )
                                                        basePatternDrawingUiState.savePassword(
                                                            password
                                                        )
                                                    }
                                                }
                                            }

                                            CommonUtil.vibrate(
                                                context,
                                                BasePatternDrawingUiState.VIBRATOR_MILLS
                                            )
                                            basePatternDrawingUiState.drawDotAndLine(
                                                destinationOffset = dotOffset
                                            )
                                            basePatternDrawingUiState.savePassword(
                                                getPasswordByDotOffsetIndex(
                                                    rowIndex = rowDotIndex,
                                                    colIndex = colDotIndex
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        MotionEvent.ACTION_UP -> {
                            if (basePatternDrawingUiState.value.selectedOffsets.size >= 4) {
                                onPatternInput(
                                    basePatternDrawingUiState.value.password
                                        .toMutableList()
                                        .joinToString("")
                                )
                            }

                            basePatternDrawingUiState.value =
                                BasePatternDrawingUiState()

                        }
                    }
                    true
                }
            )
            .drawWithContent {
                drawContent()
                if (basePatternDrawingUiState.value.latestSelectedDotOffset != Offset.Zero) {
                    drawLine(
                        color = Color(context.getColor(R.color.pattern_dot_select)),
                        start = basePatternDrawingUiState.value.latestSelectedDotOffset,
                        end = basePatternDrawingUiState.value.draggingOffset,
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
                        color = Color(context.getColor(basePatternDrawingUiState.value.getDotColor(
                            rowIndex = rowIndex,
                            colIndex = colIndex))),
                        center = colPosition,
                        radius = BasePatternDrawingUiState.DOT_RADIUS.toPx()
                    )

                    rowDotPositions.add(
                        index = colIndex,
                        element = colPosition
                    )
                }

                basePatternDrawingUiState.value.threeByThreeDotOffsets[rowIndex] = rowDotPositions

            }

            repeat(basePatternDrawingUiState.value.lineOffsets.size) { lineIndex ->
                if (basePatternDrawingUiState.value.lineOffsets[lineIndex].first != Offset.Zero) {
                    drawLine(
                        color = Color(context.getColor(R.color.pattern_dot_select)),
                        start = basePatternDrawingUiState.value.lineOffsets[lineIndex].first,
                        end = basePatternDrawingUiState.value.lineOffsets[lineIndex].second,
                        strokeWidth = BasePatternDrawingUiState.LINE_STROKE_WIDTH
                    )
                }
            }
        })
}

private fun Offset.is80DpCloserThan(target: Offset): Boolean =
    abs(this.x - target.x).dp <= BasePatternDrawingUiState.DOT_RADIUS * 8 &&
            abs(this.y - target.y).dp <= BasePatternDrawingUiState.DOT_RADIUS * 8

private fun MutableState<BasePatternDrawingUiState>.drawDotAndLine(destinationOffset: Offset) {
    this.value = this.value.copy(
        lineOffsets = this.value.lineOffsets.apply { add(element =
        Pair(
            first = this@drawDotAndLine.value.latestSelectedDotOffset,
            second = destinationOffset)
        ) },
        selectedOffsets = this.value.selectedOffsets.apply { add(element = destinationOffset) },
        latestSelectedDotOffset = destinationOffset
    )
}

private fun MutableState<BasePatternDrawingUiState>.savePassword(password: Int) {
    this.value = this.value.copy(
        password = this.value.password.apply {
            add(element = password)
        }
    )
}