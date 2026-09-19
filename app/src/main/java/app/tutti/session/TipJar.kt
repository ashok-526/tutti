package app.tutti.session

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.tutti.BuildConfig
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.purchaseWith

/** One way to thank the band: a consumable package from the current RevenueCat offering. */
class Tip(val pkg: Package) {
    val id: String get() = pkg.identifier
    val name: String get() = pkg.product.name
    val price: String get() = pkg.product.price.formatted
}

/**
 * The tip jar. Nothing in Tutti is locked: after dinner is served the cook can leave a tip, sold through
 * RevenueCat, and the orchestra answers every tip with the final chord ([onThanks]).
 */
class TipJar(private val onThanks: () -> Unit) {
    var tips by mutableStateOf<List<Tip>>(emptyList())
        private set
    /** The tip whose purchase sheet is open, so the jar can't be tapped twice. */
    var pending by mutableStateOf<String?>(null)
        private set
    var note by mutableStateOf<String?>(null)
        private set

    fun load() {
        if (!Purchases.isConfigured || tips.isNotEmpty()) return
        Purchases.sharedInstance.getOfferingsWith(
            onError = { Log.w("Tutti", "tip jar: ${it.message}") },
        ) { offerings ->
            tips = offerings.current?.availablePackages.orEmpty()
                .sortedBy { it.product.price.amountMicros }
                .map(::Tip)
        }
    }

    fun give(activity: Activity, tip: Tip) {
        if (pending != null) return
        pending = tip.id
        note = null
        Purchases.sharedInstance.purchaseWith(
            PurchaseParams.Builder(activity, tip.pkg).build(),
            onError = { error, userCancelled ->
                pending = null
                if (!userCancelled) note = "That tip didn't go through. ${error.message}"
            },
        ) { _, _ ->
            pending = null
            note = "Thank you. The band played that one for you."
            onThanks()
        }
    }

    companion object {
        /** Returns false when there's no key, which leaves the tip jar out of the app. */
        fun configure(context: Context): Boolean {
            val key = BuildConfig.REVENUECAT_API_KEY
            // Test Store keys stop release builds on purpose, so they only run in debug builds.
            if (key.isBlank() || (key.startsWith("test_") && !BuildConfig.DEBUG)) return false
            if (!Purchases.isConfigured) Purchases.configure(PurchasesConfiguration.Builder(context, key).build())
            return true
        }
    }
}
