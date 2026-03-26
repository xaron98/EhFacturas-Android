package es.ehfacturas.service

import android.app.Activity
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de suscripciones via Google Play Billing.
 *
 * CONFIGURACIÓN NECESARIA:
 * 1. Configurar app en Google Play Console
 * 2. Crear productos de suscripción:
 *    - es.ehfacturas.pro.monthly (4,99 €/mes)
 *    - es.ehfacturas.pro.yearly (39,99 €/año)
 * 3. Descomentar dependencia Play Billing en build.gradle.kts
 * 4. Descomentar el código de billing en los métodos
 *
 * Features Pro:
 * - Acceso ilimitado a Claude y OpenAI (cloud AI)
 * - Usuarios free: solo IA on-device (Gemini Nano / Gemma)
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val PRODUCT_MONTHLY = "es.ehfacturas.pro.monthly"
        const val PRODUCT_YEARLY = "es.ehfacturas.pro.yearly"
    }

    data class SubscriptionState(
        val isProSubscriber: Boolean = false,
        val planActivo: String? = null,  // "monthly" | "yearly"
        val fechaExpiracion: Long? = null,
        val disponible: Boolean = false  // true cuando billing está configurado
    )

    private val _subscriptionState = MutableStateFlow(SubscriptionState())
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    val isProSubscriber: Boolean get() = _subscriptionState.value.isProSubscriber

    /**
     * Inicializa la conexión con Google Play Billing.
     * Llamar en Application.onCreate() o en la primera Activity.
     */
    fun inicializar() {
        // TODO: Implementar cuando se configure Play Console
        //
        // val billingClient = BillingClient.newBuilder(context)
        //     .setListener { billingResult, purchases ->
        //         purchases?.forEach { purchase ->
        //             verificarCompra(purchase)
        //         }
        //     }
        //     .enablePendingPurchases()
        //     .build()
        //
        // billingClient.startConnection(object : BillingClientStateListener {
        //     override fun onBillingSetupFinished(result: BillingResult) {
        //         if (result.responseCode == BillingClient.BillingResponseCode.OK) {
        //             _subscriptionState.value = _subscriptionState.value.copy(disponible = true)
        //             consultarSuscripcionesActivas()
        //         }
        //     }
        //     override fun onBillingServiceDisconnected() {}
        // })
    }

    /**
     * Lanza el flujo de compra de suscripción.
     * @param activity Activity necesaria para el dialog de compra
     * @param planId PRODUCT_MONTHLY o PRODUCT_YEARLY
     */
    fun comprar(activity: Activity, planId: String) {
        // TODO: Implementar
        //
        // val params = QueryProductDetailsParams.newBuilder()
        //     .setProductList(listOf(
        //         QueryProductDetailsParams.Product.newBuilder()
        //             .setProductId(planId)
        //             .setProductType(BillingClient.ProductType.SUBS)
        //             .build()
        //     ))
        //     .build()
        //
        // billingClient.queryProductDetailsAsync(params) { result, productDetails ->
        //     val product = productDetails.firstOrNull() ?: return
        //     val flowParams = BillingFlowParams.newBuilder()
        //         .setProductDetailsParamsList(listOf(
        //             BillingFlowParams.ProductDetailsParams.newBuilder()
        //                 .setProductDetails(product)
        //                 .build()
        //         ))
        //         .build()
        //     billingClient.launchBillingFlow(activity, flowParams)
        // }
    }

    /**
     * Restaura compras existentes (ej: reinstalación).
     */
    fun restaurarCompras() {
        // TODO: Implementar
    }
}
