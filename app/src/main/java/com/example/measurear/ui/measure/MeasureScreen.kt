package com.example.measurear.ui.measure

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.measurear.R
import com.example.measurear.ar.ArMeasureSurfaceView
import com.example.measurear.data.repository.MeasurementRepositoryImpl
import com.example.measurear.domain.MeasurementMode
import com.example.measurear.ui.measure.components.ArMeasurementViewport
import com.example.measurear.ui.measure.components.MeasureBottomPanel
import com.example.measurear.ui.measure.components.MeasurementModeSelector
import com.google.ar.core.ArCoreApk
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasureScreen(
    modifier: Modifier = Modifier,
    viewModel: MeasureViewModel = viewModel(
        factory = MeasureViewModelFactory(MeasurementRepositoryImpl()),
    ),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val comingSoonMessage = stringResource(R.string.coming_soon_message)
    var arSurfaceView by remember { mutableStateOf<ArMeasureSurfaceView?>(null) }
    var clearAnchorsRequest by remember { mutableIntStateOf(0) }
    var tapFeedbackPosition by remember { mutableStateOf<Offset?>(null) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val isPlacementEnabled = uiState.isArSupported &&
        uiState.selectedMode == MeasurementMode.LENGTH
    val canShowArCamera = uiState.isArSupported && hasCameraPermission

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) return@LaunchedEffect

        var availability = ArCoreApk.getInstance().checkAvailability(context)
        if (!availability.isSupported) {
            viewModel.setArSupported(isSupported = false)
            return@LaunchedEffect
        }
        while (availability.isTransient) {
            availability = ArCoreApk.getInstance().checkAvailability(context)
        }
        val activity = context as? ComponentActivity ?: return@LaunchedEffect
        try {
            val installStatus = ArCoreApk.getInstance().requestInstall(activity, true)
            viewModel.setArSupported(installStatus == ArCoreApk.InstallStatus.INSTALLED)
        } catch (_: UnavailableUserDeclinedInstallationException) {
            viewModel.setArSupported(isSupported = false)
        }
    }

    LaunchedEffect(uiState.showComingSoonMessage) {
        if (uiState.showComingSoonMessage) {
            snackbarHostState.showSnackbar(comingSoonMessage)
            viewModel.dismissComingSoonMessage()
        }
    }

    LaunchedEffect(clearAnchorsRequest) {
        if (clearAnchorsRequest > 0) {
            arSurfaceView?.clearAnchors()
        }
    }

    LaunchedEffect(tapFeedbackPosition) {
        if (tapFeedbackPosition != null) {
            delay(TAP_FEEDBACK_VISIBLE_MS)
            tapFeedbackPosition = null
        }
    }

    val bottomInstruction = when {
        !hasCameraPermission -> MeasureInstruction.CAMERA_PERMISSION_REQUIRED
        !uiState.isArSupported -> MeasureInstruction.AR_UNSUPPORTED
        else -> uiState.instruction
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.measure_screen_title)) },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                canShowArCamera -> {
                    ArMeasurementViewport(
                        projectedPoints = uiState.projectedPoints,
                        tapFeedbackPosition = tapFeedbackPosition,
                        isPlacementEnabled = isPlacementEnabled,
                        onTapFeedback = { tapFeedbackPosition = it },
                        onPlacementFailed = viewModel::onPlacementFailed,
                        onPointPlaced = viewModel::onPointPlaced,
                        onFrameUpdated = viewModel::onFrameUpdated,
                        onTrackingStateChanged = viewModel::onTrackingStateChanged,
                        onCameraUnavailable = viewModel::onCameraUnavailable,
                        onSurfaceViewReady = { arSurfaceView = it },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                !hasCameraPermission -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(text = stringResource(R.string.instruction_camera_permission))
                        Button(
                            onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier.padding(top = 12.dp),
                        ) {
                            Text(text = stringResource(R.string.action_grant_camera))
                        }
                    }
                }

                else -> {
                    Text(
                        text = stringResource(R.string.instruction_ar_unsupported),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }

            MeasurementModeSelector(
                selectedMode = uiState.selectedMode,
                onModeSelected = viewModel::onModeSelected,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .zIndex(2f),
            )
            MeasureBottomPanel(
                instruction = bottomInstruction,
                formattedLength = if (canShowArCamera) uiState.formattedLength else null,
                onResetClick = {
                    viewModel.resetMeasurement()
                    clearAnchorsRequest += 1
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(2f),
            )
        }
    }
}

private const val TAP_FEEDBACK_VISIBLE_MS = 450L
