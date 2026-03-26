package es.ehfacturas.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import es.ehfacturas.MainActivity

class FacturasWidget : GlanceAppWidget() {

    companion object {
        val KEY_PENDIENTE = doublePreferencesKey("widget_pendiente")
        val KEY_COBRADO = doublePreferencesKey("widget_cobrado")
        val KEY_VENCIDAS = intPreferencesKey("widget_vencidas")
        val KEY_NUM_FACTURAS = intPreferencesKey("widget_num_facturas")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent()
        }
    }

    @androidx.compose.runtime.Composable
    private fun WidgetContent() {
        val prefs = currentState<Preferences>()
        val pendiente = prefs[KEY_PENDIENTE] ?: 0.0
        val vencidas = prefs[KEY_VENCIDAS] ?: 0
        val numFacturas = prefs[KEY_NUM_FACTURAS] ?: 0

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
                .background(GlanceTheme.colors.surface)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EhFacturas!",
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = "Pendiente: ${String.format("%.2f", pendiente)} \u20AC",
                style = TextStyle(fontSize = 12.sp)
            )
            if (vencidas > 0) {
                Text(
                    text = "$vencidas vencidas",
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)
                )
            }
            Text(
                text = "$numFacturas facturas",
                style = TextStyle(fontSize = 11.sp)
            )
        }
    }
}

class FacturasWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FacturasWidget()
}
