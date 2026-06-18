package ec.fr.pmr_3d.models

enum class Screen { HOME, NOTICE, SCAN_MEUBLE, ASSEMBLAGE, PARAMETRES, APROPOS }

data class AssemblyStep(
    val id: Int,
    val titre: String,
    val description: String,
    val animationKey: String,
    val icone: String
)

val ASSEMBLY_STEPS = listOf(
    AssemblyStep(1, "Panneaux latéraux", "Assemblez les panneaux gauche et droit avec le fond.", "animation1", "🧱"),
    AssemblyStep(2, "Fixation haut/bas", "Positionnez les panneaux haut et bas avec les chevilles.", "animation2", "🔩"),
)

data class MenuItem(
    val icone: String,
    val titre: String,
    val description: String,
    val screen: Screen
)

val MENU_ITEMS = listOf(
    MenuItem("📖", "Notice d'utilisation", "Lisez les instructions avant de commencer", Screen.NOTICE),
    MenuItem("🛠️", "Assemblage du meuble", "Suivez les étapes d'assemblage en 3D", Screen.SCAN_MEUBLE),
    MenuItem("⚙️", "Paramètres", "Configurer l'affichage et les préférences", Screen.PARAMETRES),
    MenuItem("ℹ️", "À propos du projet", "Équipe, contexte et technologies utilisées", Screen.APROPOS),
)
