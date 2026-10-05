package com.example.measurear.ui.measure.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.measurear.R
import com.example.measurear.ui.measure.MeasureInstruction
import com.example.measurear.ui.theme.MeasureARTheme

@Composable
internal fun MeasureBottomPanel(
    instruction: MeasureInstruction,
    formattedLength: String?,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            .padding(16.dp),
    ) {
        Text(
            text = instruction.toMessage(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        formattedLength?.let { lengthText ->
            Text(
                text = stringResource(R.string.length_result, lengthText),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        ) {
            OutlinedButton(
                onClick = onResetClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.action_reset))
            }
        }
    }
}

@Composable
private fun MeasureInstruction.toMessage(): String {
    return when (this) {
        MeasureInstruction.DETECT_SURFACES -> stringResource(R.string.instruction_detect_surfaces)
        MeasureInstruction.TAP_FIRST_POINT -> stringResource(R.string.instruction_tap_first_point)
        MeasureInstruction.TAP_SECOND_POINT -> stringResource(R.string.instruction_tap_second_point)
        MeasureInstruction.MEASUREMENT_COMPLETE -> stringResource(R.string.instruction_measurement_complete)
        MeasureInstruction.MOVE_DEVICE -> stringResource(R.string.instruction_move_device)
        MeasureInstruction.AR_SESSION_STOPPED -> stringResource(R.string.instruction_ar_stopped)
        MeasureInstruction.AR_UNSUPPORTED -> stringResource(R.string.instruction_ar_unsupported)
        MeasureInstruction.CAMERA_PERMISSION_REQUIRED -> stringResource(R.string.instruction_camera_permission)
        MeasureInstruction.POINT_NOT_ON_SURFACE -> stringResource(R.string.instruction_point_not_on_surface)
    }
}

@Preview
@Composable
private fun MeasureBottomPanelPreview() {
    MeasureARTheme {
        MeasureBottomPanel(
            instruction = MeasureInstruction.TAP_SECOND_POINT,
            formattedLength = "12.3 cm",
            onResetClick = {},
        )
    }
}
