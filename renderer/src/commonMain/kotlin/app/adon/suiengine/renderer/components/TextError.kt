package app.adon.suiengine.renderer.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TextError(s: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 1.dp
    ) {
        Text(
            text = s,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            modifier = modifier.padding(8.dp)
        )
    }
}