package com.weatherapp.db.fb

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.ktx.Firebase

class FBDatabase {

    interface Listener {
        fun onUserLoaded(user: FBUser)
        fun onUserSignOut()
        fun onCityAdded(city: FBCity)
        fun onCityUpdated(city: FBCity)
        fun onCityRemoved(city: FBCity)
    }

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private var citiesListReg: ListenerRegistration? = null
    private var listener: Listener? = null

    init {
        auth.addAuthStateListener { auth ->
            if (auth.currentUser == null) {
                citiesListReg?.remove()
                listener?.onUserSignOut()
                return@addAuthStateListener
            }

            val refCurrUser = db.collection("users")
                .document(auth.currentUser!!.uid)

            refCurrUser.get().addOnSuccessListener {
                it.toObject(FBUser::class.java)?.let { user ->
                    listener?.onUserLoaded(user)
                }
            }

            citiesListReg = refCurrUser
                .collection("cities")
                .addSnapshotListener { snapshots, ex ->

                    if (ex != null) return@addSnapshotListener
                    if (snapshots == null) return@addSnapshotListener

                    snapshots.documentChanges.forEach { change ->
                        val fbCity = change.document.toObject(FBCity::class.java)

                        when (change.type) {
                            DocumentChange.Type.ADDED ->
                                listener?.onCityAdded(fbCity)

                            DocumentChange.Type.MODIFIED ->
                                listener?.onCityUpdated(fbCity)

                            DocumentChange.Type.REMOVED ->
                                listener?.onCityRemoved(fbCity)
                        }
                    }
                }
        }
    }

    fun setListener(listener: Listener? = null) {
        this.listener = listener
    }

    fun register(user: FBUser) {
        if (auth.currentUser == null)
            throw RuntimeException("User not logged in!")

        val uid = auth.currentUser!!.uid
        db.collection("users")
            .document(uid)
            .set(user)
    }

    fun add(city: FBCity) {
        if (auth.currentUser == null)
            throw RuntimeException("User not logged in!")

        if (city.name.isNullOrEmpty())
            throw RuntimeException("City with null or empty name!")

        val uid = auth.currentUser!!.uid

        db.collection("users")
            .document(uid)
            .collection("cities")
            .document(city.name!!)
            .set(city)
    }

    fun remove(city: FBCity) {
        if (auth.currentUser == null)
            throw RuntimeException("User not logged in!")

        if (city.name.isNullOrEmpty())
            throw RuntimeException("City with null or empty name!")

        val uid = auth.currentUser!!.uid

        db.collection("users")
            .document(uid)
            .collection("cities")
            .document(city.name!!)
            .delete()
    }

    fun update(city: FBCity) {
        if (auth.currentUser == null) throw RuntimeException("Not logged in!")

        val uid = auth.currentUser!!.uid

        val changes = mapOf(
            "lat" to city.lat,
            "lng" to city.lng,
            "monitored" to city.monitored
        )

        db.collection("users")
            .document(uid)
            .collection("cities")
            .document(city.name!!)
            .update(changes)
    }
}