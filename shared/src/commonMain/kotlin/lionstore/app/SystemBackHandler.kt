package lionstore.app

import androidx.compose.runtime.Composable

@Composable
expect fun SystemBackHandler(enabled: Boolean = true, onBack: () -> Unit)
