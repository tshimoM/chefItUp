package com.chefitup.app.data.firestore

import com.chefitup.app.domain.model.RecipeSummary
import com.chefitup.app.domain.model.UserGamification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class CloudUserProfile(
    val name: String = "",
    val surname: String = "",
    val username: String = "",
    val phone: String = "",
    val email: String = "",
    val diet: String = "",
    val allergies: List<String> = emptyList(),
    val languageCode: String = "en",
    val theme: String = "SYSTEM",
    val photoUrl: String? = null
)

data class CloudGamification(
    val xp: Int = 0,
    val recipesCompleted: Int = 0,
    val unlockedBadges: List<String> = emptyList()
)

/**
 * Cloud backup + restore for profile preferences, gamification, favourites, and FCM tokens.
 * Local Room remains source of truth while offline; this pushes/pulls when online.
 */
@Singleton
class FirestoreUserSync @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    suspend fun pushProfile(
        name: String,
        surname: String,
        username: String,
        phone: String,
        diet: String,
        allergies: List<String>,
        languageCode: String,
        theme: String,
        photoUrl: String? = null
    ) {
        val user = firebaseAuth.currentUser ?: return
        val data = mutableMapOf<String, Any>(
            "name" to name,
            "surname" to surname,
            "username" to username,
            "phone" to phone,
            "email" to (user.email.orEmpty()),
            "diet" to diet,
            "allergies" to allergies,
            "languageCode" to languageCode,
            "theme" to theme,
            "updatedAt" to System.currentTimeMillis()
        )
        if (!photoUrl.isNullOrBlank()) {
            data["photoUrl"] = photoUrl
        }
        firestore.collection("users").document(user.uid)
            .set(data, SetOptions.merge())
            .await()
    }

    suspend fun pullProfile(): CloudUserProfile? {
        val uid = firebaseAuth.currentUser?.uid ?: return null
        val snap = firestore.collection("users").document(uid).get().await()
        if (!snap.exists()) return null
        @Suppress("UNCHECKED_CAST")
        val allergies = (snap.get("allergies") as? List<*>)
            ?.mapNotNull { it as? String }
            .orEmpty()
        return CloudUserProfile(
            name = snap.getString("name").orEmpty(),
            surname = snap.getString("surname").orEmpty(),
            username = snap.getString("username").orEmpty(),
            phone = snap.getString("phone").orEmpty(),
            email = snap.getString("email").orEmpty(),
            diet = snap.getString("diet").orEmpty(),
            allergies = allergies,
            languageCode = snap.getString("languageCode") ?: "en",
            theme = snap.getString("theme") ?: "SYSTEM",
            photoUrl = snap.getString("photoUrl")
        )
    }

    suspend fun pushGamification(state: UserGamification) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val data = mapOf(
            "xp" to state.xp,
            "level" to state.level.name,
            "recipesCompleted" to state.recipesCompleted,
            "unlockedBadges" to state.earnedBadgeIds,
            "updatedAt" to System.currentTimeMillis()
        )
        firestore.collection("users").document(uid)
            .collection("meta").document("gamification")
            .set(data, SetOptions.merge())
            .await()
    }

    suspend fun pullGamification(): CloudGamification? {
        val uid = firebaseAuth.currentUser?.uid ?: return null
        val snap = firestore.collection("users").document(uid)
            .collection("meta").document("gamification")
            .get()
            .await()
        if (!snap.exists()) return null
        @Suppress("UNCHECKED_CAST")
        val badges = (snap.get("unlockedBadges") as? List<*>)
            ?.mapNotNull { it as? String }
            .orEmpty()
        return CloudGamification(
            xp = (snap.getLong("xp") ?: 0L).toInt(),
            recipesCompleted = (snap.getLong("recipesCompleted") ?: 0L).toInt(),
            unlockedBadges = badges
        )
    }

    suspend fun pushFavourite(recipe: RecipeSummary, isFavourite: Boolean) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val doc = firestore.collection("users").document(uid)
            .collection("favourites").document(recipe.id)
        if (isFavourite) {
            doc.set(
                mapOf(
                    "recipeId" to recipe.id,
                    "title" to recipe.title,
                    "imageUrl" to (recipe.imageUrl.orEmpty()),
                    "readyInMinutes" to recipe.readyInMinutes,
                    "servings" to recipe.servings,
                    "difficulty" to recipe.difficulty,
                    "rating" to recipe.rating,
                    "vegetarian" to recipe.vegetarian,
                    "vegan" to recipe.vegan,
                    "glutenFree" to recipe.glutenFree,
                    "dairyFree" to recipe.dairyFree,
                    "cuisines" to recipe.cuisines,
                    "dishTypes" to recipe.dishTypes,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
        } else {
            doc.delete().await()
        }
    }

    /** Backwards-compatible overload used when only an id is known. */
    suspend fun pushFavourite(recipeId: String, isFavourite: Boolean) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val doc = firestore.collection("users").document(uid)
            .collection("favourites").document(recipeId)
        if (isFavourite) {
            doc.set(
                mapOf(
                    "recipeId" to recipeId,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            ).await()
        } else {
            doc.delete().await()
        }
    }

    suspend fun pullFavourites(): List<RecipeSummary> {
        val uid = firebaseAuth.currentUser?.uid ?: return emptyList()
        val snap = firestore.collection("users").document(uid)
            .collection("favourites")
            .get()
            .await()
        return snap.documents.mapNotNull { doc ->
            val id = doc.getString("recipeId") ?: doc.id
            val title = doc.getString("title") ?: return@mapNotNull null
            @Suppress("UNCHECKED_CAST")
            RecipeSummary(
                id = id,
                title = title,
                imageUrl = doc.getString("imageUrl")?.takeIf { it.isNotBlank() },
                readyInMinutes = (doc.getLong("readyInMinutes") ?: 30L).toInt(),
                servings = (doc.getLong("servings") ?: 2L).toInt(),
                difficulty = doc.getString("difficulty") ?: "Medium",
                rating = doc.getDouble("rating") ?: 4.0,
                vegetarian = doc.getBoolean("vegetarian") ?: false,
                vegan = doc.getBoolean("vegan") ?: false,
                glutenFree = doc.getBoolean("glutenFree") ?: false,
                dairyFree = doc.getBoolean("dairyFree") ?: false,
                cuisines = (doc.get("cuisines") as? List<*>)?.mapNotNull { it as? String }.orEmpty(),
                dishTypes = (doc.get("dishTypes") as? List<*>)?.mapNotNull { it as? String }.orEmpty()
            )
        }
    }

    suspend fun saveFcmToken(token: String) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        if (token.isBlank()) return
        firestore.collection("users").document(uid)
            .collection("meta").document("fcm")
            .set(
                mapOf(
                    "token" to token,
                    "updatedAt" to System.currentTimeMillis(),
                    "platform" to "android"
                ),
                SetOptions.merge()
            )
            .await()
    }
}
