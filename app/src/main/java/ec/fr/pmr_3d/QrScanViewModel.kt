package ec.fr.pmr_3d

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class QrScanViewModel : ViewModel() {
    private val _state = MutableStateFlow(QrScanState())
    val state: StateFlow<QrScanState> = _state.asStateFlow()

    fun activateScanner(stepId: Int) {
        _state.update {
            it.copy(
                isActive = true,
                currentStepId = stepId,
                lastResult = ScanResult.Scanning
            )
        }
    }

    fun deactivateScanner() {
        _state.update { it.copy(isActive = false, lastResult = ScanResult.Idle) }
    }

    fun onBarcodeDetected(rawValue: String) {
        if (!_state.value.isActive || _state.value.lastResult !is ScanResult.Scanning) return

        val payload = try {
            Json.decodeFromString<QrPayload>(rawValue)
        } catch (_: Exception) {
            _state.update { it.copy(lastResult = ScanResult.ParseError(rawValue)) }
            autoDeactivateAfterDelay()
            return
        }

        val check = getOrientationCheck(payload.piece, _state.value.currentStepId)
        if (check == null) {
            _state.update { it.copy(lastResult = ScanResult.UnknownPiece(rawValue)) }
            autoDeactivateAfterDelay()
            return
        }

        val result = if (payload.face == check.faceAttendue) {
            ScanResult.Correct(payload.piece, payload.face)
        } else {
            ScanResult.Incorrect(
                pieceId = payload.piece,
                faceLue = payload.face,
                faceAttendue = check.faceAttendue,
                messageErreur = check.labelErreur
            )
        }
        _state.update { it.copy(lastResult = result) }

        autoDeactivateAfterDelay()
    }

    private fun autoDeactivateAfterDelay() {
        viewModelScope.launch {
            delay(3000)
            deactivateScanner()
        }
    }
}
