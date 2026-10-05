package com.example.measurear.ui.measure

enum class MeasureInstruction {
    DETECT_SURFACES,
    TAP_FIRST_POINT,
    TAP_SECOND_POINT,
    MEASUREMENT_COMPLETE,
    MOVE_DEVICE,
    AR_SESSION_STOPPED,
    AR_UNSUPPORTED,
    CAMERA_PERMISSION_REQUIRED,
    POINT_NOT_ON_SURFACE,
}
