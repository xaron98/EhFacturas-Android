package es.ehfacturas.ui.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuscripcionScreen(
    onBack: () -> Unit
) {
    // TODO: Inject BillingManager when configured

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EhFacturas! Pro") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Desbloquea IA ilimitada",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Accede a Claude y OpenAI sin límites para crear facturas por voz, gestionar clientes y más.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Plan mensual
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { /* billing.comprar(activity, PRODUCT_MONTHLY) */ }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Pro Mensual", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("4,99 €/mes", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Plan anual
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { /* billing.comprar(activity, PRODUCT_YEARLY) */ }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pro Anual", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        SuggestionChip(onClick = {}, label = { Text("Ahorra 2 meses") })
                    }
                    Text("39,99 €/año", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    Text("3,33 €/mes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Features incluidas gratis
            Text(
                text = "Incluido gratis: facturación, PDF, VeriFactu, informes, escáner, firma, importador, IA on-device",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { /* billing.restaurarCompras() */ }) {
                Text("Restaurar compras")
            }
        }
    }
}
