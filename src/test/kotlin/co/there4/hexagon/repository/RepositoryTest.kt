package co.there4.hexagon.repository

import co.there4.hexagon.serialization.SerializationTest
import co.there4.hexagon.util.CompanionLogger
import com.github.fakemongo.Fongo
import com.mongodb.MongoBulkWriteException
import com.mongodb.MongoClient
import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.client.model.InsertManyOptions
import com.mongodb.client.model.InsertOneOptions
import com.mongodb.client.model.UpdateOptions
import java.lang.System.*
import kotlin.reflect.KClass
import com.mongodb.client.model.Filters.*

abstract class RepositoryTest<T : Any, K : Any> (type: KClass<T>, val idField: String) :
    SerializationTest<T> (type) {

    companion object : CompanionLogger (RepositoryTest::class)

    val USE_REAL_MONGO_DB = getProperty ("useRealMongoDb") != null
    protected val collection: MongoRepository<T> = createCollection(type)

    fun createDatabase (type: KClass<*>) =
        try {
            if (USE_REAL_MONGO_DB) {
                val mongoClient = MongoClient ()
                mongoClient.getDatabase (RepositoryTest::class.simpleName)
            }
            else {
                val fongoClient = Fongo (type.simpleName)
                fongoClient.getDatabase (RepositoryTest::class.simpleName)
            }
        }
        catch (e: Exception) {
            val fongoClient = Fongo (type.simpleName)
            fongoClient.getDatabase (RepositoryTest::class.simpleName)
        }

    fun <T : Any> createCollection (type: KClass<T>): MongoRepository<T> {
        val database = createDatabase (type)
        val collection = database.getCollection(type.simpleName)
        val repository = MongoRepository (type, collection)
        setupCollection(repository)
        return repository
    }

    protected open fun <T : Any> setupCollection (collection: MongoRepository<T>) {}

    protected abstract fun setObjectKey (obj: T, id: Int): T
    protected abstract fun getObjectKey (obj: T): K
    protected abstract fun createObject(): T
    protected abstract fun changeObject(source: T): T

    protected fun createObjects() = (0..9).map { setObjectKey (createObject(), it) }

    fun one_object_is_stored_and_loaded_without_error() {
        testObjects.forEach {
            collection.deleteAll()
            collection.insertOneObject(it)
            var result: T = collection.findObjects().first()

            assert(result == it)

            collection.deleteAll()
            val object2 = changeObject(it)
            collection.insertOneObject(object2, InsertOneOptions())
            assert(collection.count() == 1L)
            result = collection.findObjects().first()
            assert(result == object2)

            collection.deleteAll()
        }
    }

    fun many_objects_are_stored_and_loaded_without_error() {
        testObjects.forEach {
            collection.deleteAll()
            val objects = createObjects ()

            try {
                collection.insertManyObjects(objects)
            } catch (e: MongoBulkWriteException) {
                e.printStackTrace()
            }

            assert(collection.count() == 10L)
            val firstObject = objects[0]
            val obj = collection.findObjects(eq<K>(idField, getObjectKey(firstObject))).first()
            assert(obj == firstObject)

            collection.deleteAll()

            collection.insertManyObjects(objects, InsertManyOptions().ordered(false))
            assert(collection.count() == 10L)

            collection.deleteAll()
        }
    }

    fun replace_object_stores_modified_data_in_db() {
        testObjects.forEach {
            collection.deleteAll()
            val entity = createObject()
            val replacement = changeObject(entity)
            val query = eq<K>(idField, getObjectKey(entity))

            collection.insertOneObject(entity)
            var result = collection.replaceOneObject(query, replacement)
            assert(result.matchedCount == 1L)

            assert(collection.findObjects(query).first() == replacement)

            result = collection.replaceOneObject(query, entity, UpdateOptions().upsert(false))
            assert(result.matchedCount == 1L)

            assert(collection.findObjects(query).first() == entity)

            collection.deleteAll()
        }
    }

    fun find_and_replace_object_stores_modified_data_in_db() {
        testObjects.forEach {
            collection.deleteAll()
            var t = nanoTime()
            val entity = createObject()
            val replacement = changeObject(entity)
            val query = eq<K>(idField, getObjectKey(entity))
            trace("Test setup: " + (nanoTime() - t))

            t = nanoTime()
            collection.insertOneObject(entity)
            trace("Insert: " + (nanoTime() - t))
            t = nanoTime()
            var result = collection.findOneObjectAndReplace(query, replacement)
            trace("Find and replace: " + (nanoTime() - t))

            assert(entity == result)

            t = nanoTime()
            val options = FindOneAndReplaceOptions().upsert(false)
            result = collection.findOneObjectAndReplace(query, entity, options)
            trace("Find and replace (params): " + (nanoTime() - t))
            t = nanoTime()

            assert(replacement == result)

            collection.deleteAll()
            trace("Delete all records: " + (nanoTime() - t))
        }
    }
}