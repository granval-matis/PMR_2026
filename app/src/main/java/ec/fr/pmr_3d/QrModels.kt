package ec.fr.pmr_3d

import kotlinx.serialization.Serializable

@Serializable
data class QrPayload(
    val piece: String,    // "fond" | "gauche" | "droite" | "haut" | "bas" | "bitoniau_N"
    val face: String,     // "avant" | "arriere" | "haut" | "bas" | "gauche" | "droite" | "interieure" | "exterieure" | "tete"
    val v: Int = 1        // version du schema
)

data class OrientationCheck(
    val pieceId: String,
    val faceAttendue: String,
    val stepId: Int,
    val labelErreur: String
)

val ORIENTATION_CHECKS = listOf(
    // Étape 1 : panneaux latéraux + fond
    OrientationCheck("fond",   "avant",  stepId = 1,
        labelErreur = "Retournez le fond : la face gravée doit être visible"),
    OrientationCheck("gauche", "interieure", stepId = 1,
        labelErreur = "Panneau gauche à l'envers : face intérieure vers le centre"),
    OrientationCheck("droite", "interieure", stepId = 1,
        labelErreur = "Panneau droit à l'envers : face intérieure vers le centre"),

    // Étape 2 : haut, bas et chevilles
    OrientationCheck("haut",  "bas",   stepId = 2,
        labelErreur = "Panneau du haut à l'envers : face inférieure vers le bas"),
    OrientationCheck("bas",   "haut",  stepId = 2,
        labelErreur = "Panneau du bas à l'envers : face supérieure vers le haut"),
    OrientationCheck("bitoniau_5",  "tete", stepId = 2,
        labelErreur = "Cheville 5 dans le mauvais sens"),
    OrientationCheck("bitoniau_6",  "tete", stepId = 2,
        labelErreur = "Cheville 6 dans le mauvais sens"),
    // … bitoniau_7 à bitoniau_12 peuvent être ajoutés ici
)

fun getOrientationCheck(pieceId: String, stepId: Int): OrientationCheck? =
    ORIENTATION_CHECKS.find { it.pieceId == pieceId && it.stepId == stepId }

sealed class ScanResult {
    object Idle : ScanResult()
    object Scanning : ScanResult()
    data class Correct(val pieceId: String, val face: String) : ScanResult()
    data class Incorrect(
        val pieceId: String,
        val faceLue: String,
        val faceAttendue: String,
        val messageErreur: String
    ) : ScanResult()
    data class UnknownPiece(val rawValue: String) : ScanResult()
    data class ParseError(val rawValue: String) : ScanResult()
}

data class QrScanState(
    val isActive: Boolean = false,
    val lastResult: ScanResult = ScanResult.Idle,
    val currentStepId: Int = 1
)
