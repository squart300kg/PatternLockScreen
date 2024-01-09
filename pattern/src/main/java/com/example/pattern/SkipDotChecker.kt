package com.example.pattern

import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Offset
import kotlin.properties.Delegates

class SkipDotChecker(
    private val rowDotIndex: Int,
    private val colDotIndex: Int,
    private val targetUiModel: MutableState<BasePatternDrawingUiState>
) {

    var middleOffset = Offset.Zero
        private set
    var password: Int by Delegates.notNull()
        private set

    fun isSkippedMiddleDot(): Boolean {
        when ((rowDotIndex to colDotIndex)) {
            (2 to 2) -> if (targetUiModel.value.latestSelectedDotOffset == targetUiModel.value.threeByThreeDotOffsets[0][0]) {
                middleOffset = Offset(
                    x = targetUiModel.value.threeByThreeDotOffsets[1][1].x,
                    y = targetUiModel.value.threeByThreeDotOffsets[1][1].y,
                )
                password = getPasswordByDotOffsetIndex(
                    rowIndex = 1,
                    colIndex = 1
                )
                return true
            }
            (0 to 0) -> if (targetUiModel.value.latestSelectedDotOffset == targetUiModel.value.threeByThreeDotOffsets[2][2]) {
                middleOffset = Offset(
                    x = targetUiModel.value.threeByThreeDotOffsets[1][1].x,
                    y = targetUiModel.value.threeByThreeDotOffsets[1][1].y,
                )
                password = getPasswordByDotOffsetIndex(
                    rowIndex = 1,
                    colIndex = 1
                )

                return true
            }
        }
        when ((rowDotIndex to colDotIndex)) {
            (2 to 0) -> if (targetUiModel.value.latestSelectedDotOffset == targetUiModel.value.threeByThreeDotOffsets[0][2]) {
                middleOffset = Offset(
                    x = targetUiModel.value.threeByThreeDotOffsets[1][1].x,
                    y = targetUiModel.value.threeByThreeDotOffsets[1][1].y,
                )
                password = getPasswordByDotOffsetIndex(
                    rowIndex = 1,
                    colIndex = 1
                )
                return true
            }
            (0 to 2) -> if (targetUiModel.value.latestSelectedDotOffset == targetUiModel.value.threeByThreeDotOffsets[2][0]) {
                middleOffset = Offset(
                    x = targetUiModel.value.threeByThreeDotOffsets[1][1].x,
                    y = targetUiModel.value.threeByThreeDotOffsets[1][1].y,
                )
                password = getPasswordByDotOffsetIndex(
                    rowIndex = 1,
                    colIndex = 1
                )
                return true
            }
        }
        when (rowDotIndex) {
            0 -> if (targetUiModel.value.latestSelectedDotOffset == targetUiModel.value.threeByThreeDotOffsets[2][colDotIndex]) {
                middleOffset = Offset(
                    x = targetUiModel.value.threeByThreeDotOffsets[1][colDotIndex].x,
                    y = targetUiModel.value.threeByThreeDotOffsets[1][colDotIndex].y,
                )
                password = getPasswordByDotOffsetIndex(
                    rowIndex = 1,
                    colIndex = colDotIndex
                )
                return true
            }
            2 -> if (targetUiModel.value.latestSelectedDotOffset == targetUiModel.value.threeByThreeDotOffsets[0][colDotIndex]) {
                middleOffset = Offset(
                    x = targetUiModel.value.threeByThreeDotOffsets[1][colDotIndex].x,
                    y = targetUiModel.value.threeByThreeDotOffsets[1][colDotIndex].y,
                )
                password = getPasswordByDotOffsetIndex(
                    rowIndex = 1,
                    colIndex = colDotIndex
                )
                return true
            }
        }
        when (colDotIndex) {
            0 -> if (targetUiModel.value.latestSelectedDotOffset == targetUiModel.value.threeByThreeDotOffsets[rowDotIndex][2]) {
                middleOffset = Offset(
                    x = targetUiModel.value.threeByThreeDotOffsets[rowDotIndex][1].x,
                    y = targetUiModel.value.threeByThreeDotOffsets[rowDotIndex][1].y,
                )
                password = getPasswordByDotOffsetIndex(
                    rowIndex = rowDotIndex,
                    colIndex = 1
                )
                return true
            }
            2 -> if (targetUiModel.value.latestSelectedDotOffset == targetUiModel.value.threeByThreeDotOffsets[rowDotIndex][0]) {
                middleOffset = Offset(
                    x = targetUiModel.value.threeByThreeDotOffsets[rowDotIndex][1].x,
                    y = targetUiModel.value.threeByThreeDotOffsets[rowDotIndex][1].y,
                )
                password = getPasswordByDotOffsetIndex(
                    rowIndex = rowDotIndex,
                    colIndex = 1
                )
                return true
            }
        }

        return false
    }

    fun isMiddleDotNotSelected(): Boolean =
        !targetUiModel.value.selectedOffsets.contains(middleOffset)


}
fun getPasswordByDotOffsetIndex(rowIndex: Int, colIndex: Int): Int =
    3 * rowIndex + colIndex