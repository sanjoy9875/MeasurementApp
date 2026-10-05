package com.example.measurear.ui.measure.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.measurear.R
import com.example.measurear.domain.MeasurementMode
import com.example.measurear.ui.theme.MeasureARTheme

@Composable
internal fun MeasurementModeSelector(
    selectedMode: MeasurementMode,
    onModeSelected: (MeasurementMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MeasurementMode.entries.forEach { mode ->
            FilterChip(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                enabled = true,
                label = {
                    Text(
                        text = mode.label(),
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
            )
        }
    }
}

@Composable
private fun MeasurementMode.label(): String {
    return when (this) {
        MeasurementMode.LENGTH -> stringResource(R.string.mode_length)
        MeasurementMode.AREA -> stringResource(R.string.mode_area)
        MeasurementMode.DIAMETER -> stringResource(R.string.mode_diameter)
        MeasurementMode.HEIGHT -> stringResource(R.string.mode_height)
    }
}

@Preview
@Composable
private fun MeasurementModeSelectorPreview() {
    MeasureARTheme {
        MeasurementModeSelector(
            selectedMode = MeasurementMode.LENGTH,
            onModeSelected = {},
        )
    }
}
