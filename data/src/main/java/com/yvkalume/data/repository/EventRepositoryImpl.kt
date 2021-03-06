package com.yvkalume.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yvkalume.data.util.FireBasePath
import com.yvkalume.data.util.getOnlyFuture
import com.yvkalume.domain.entity.Event
import com.yvkalume.domain.entity.User
import com.yvkalume.domain.repository.EventRepository
import com.yvkalume.util.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(private val firestore: FirebaseFirestore) : EventRepository {
    @ExperimentalCoroutinesApi
    override fun getAll() = callbackFlow {
        firestore.collection(FireBasePath.events)
            .addSnapshotListener { value, error ->
                if (error != null && value == null) {
                    if (!isClosedForSend) {
                        offer(Result.Error(Exception("Une Erreur s'est produite")))
                    }
                    return@addSnapshotListener
                }

                value?.toObjects(Event::class.java)?.also {
                    offer(Result.Success(it.getOnlyFuture()))
                    Log.d("EventRepository",it.toString())
                }
            }
        awaitClose()
    }

    override fun getOneByUid(uid: String): Flow<Result<Event>> {
        TODO("Not yet implemented")
    }

    @ExperimentalCoroutinesApi
    override fun getOnline() = callbackFlow {
        firestore.collection("/${FireBasePath.events}")
            .whereEqualTo(Event::offline.name,false)
//            .orderBy(Event::date.name,Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null && value == null) {
                    if (!isClosedForSend) {
                        offer(Result.Error(Exception("Une Erreur s'est produite")))
                    }
                    return@addSnapshotListener
                }

                value?.toObjects(Event::class.java)?.also {
                    if (!isClosedForSend)
                        offer(Result.Success(it.getOnlyFuture()))
                    Log.d("EventRepository",it.toString())
                }
            }
        awaitClose()
    }

    @ExperimentalCoroutinesApi
    override fun getOffline() = callbackFlow {
        firestore.collection(FireBasePath.events)
            .whereEqualTo(Event::offline.name,true)
//            .orderBy(Event::date.name,Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null && value == null) {
                    if (!isClosedForSend) {
                        offer(Result.Error(Exception("Une Erreur s'est produite")))
                    }
                    return@addSnapshotListener
                }

                value?.toObjects(Event::class.java)?.also {
                    if (!isClosedForSend)
                    offer(Result.Success(it.getOnlyFuture()))
                    Log.d("EventRepository",it.toString())
                }
            }
        awaitClose()
    }

    @ExperimentalCoroutinesApi
    override fun getNext() = callbackFlow {
        firestore.collection(FireBasePath.events)
            .orderBy(Event::date.name,Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null && value == null) {
                    if (!isClosedForSend) {
                        offer(Result.Error(Exception("Une Erreur s'est produite")))
                    }
                    return@addSnapshotListener
                }

                value?.toObjects(Event::class.java)?.also {
                    if (!isClosedForSend)
                        offer(Result.Success(it.getOnlyFuture().firstOrNull()))
                }
            }
        awaitClose()
    }

    @ExperimentalCoroutinesApi
    override fun getAttendees(eventUid: String) = callbackFlow {
        firestore.collection(FireBasePath.getAttendeesCollection(eventUid))
                .addSnapshotListener { value, error ->
                    if (error != null && value == null) {
                        if (!isClosedForSend) {
                            offer(Result.Error(Exception("Une Erreur s'est produite")))
                        }
                        return@addSnapshotListener
                    }

                    value?.toObjects(User::class.java)?.also {
                        if (!isClosedForSend)
                            offer(Result.Success(it))
                    }
                }
        awaitClose()
    }

    @ExperimentalCoroutinesApi
    override fun checkIfUserIsAttending(userUid: String, eventUid: String) = callbackFlow {
        firestore.document("${FireBasePath.getAttendeesCollection(eventUid)}/$userUid")
                .get()
                .addOnFailureListener {
                    if (!isClosedForSend)
                        offer(Result.Error(it))
                }
                .addOnSuccessListener {
                    if (!isClosedForSend)
                        offer(Result.Success(it.exists()))
                }
        awaitClose()
    }
}