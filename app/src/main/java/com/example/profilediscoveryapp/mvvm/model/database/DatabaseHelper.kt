//package com.example.profilediscoveryapp
//
//import android.content.ContentValues
//import android.content.Context
//import android.database.sqlite.SQLiteDatabase
//import android.database.sqlite.SQLiteOpenHelper
//import android.util.Log
//import com.example.profile_discovery.mvvm.model.dataclass.Profile
//import com.example.profile_discovery.mvvm.model.dataclass.SavedProfile
//import com.example.profilediscoveryapp.DatabaseHelper.Companion.COLUMN_ID
//import com.example.profilediscoveryapp.DatabaseHelper.Companion.COLUMN_USER_ID
//
//import java.util.UUID
//
//class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
//
//    companion object {
//        private const val DATABASE_NAME = "profile_discovery.db"
//        private const val DATABASE_VERSION = 2
//        private const val TAG = "DatabaseHelper"
//
//        // Table Names
//        const val TABLE_USERS = "users"
//        const val TABLE_PROFILES = "profiles"
//        const val TABLE_SAVED_PROFILES = "saved_profiles"
//
//        // Common Columns
//        const val COLUMN_ID = "id"
//        const val COLUMN_USER_ID = "user_id"
//        const val COLUMN_EMAIL = "email"
//        const val COLUMN_PASSWORD = "password"
//        const val COLUMN_FULL_NAME = "full_name"
//        const val COLUMN_AGE = "age"
//        const val COLUMN_PHONE = "phone"
//        const val COLUMN_OCCUPATION = "occupation"
//        const val COLUMN_LOCATION = "location"
//        const val COLUMN_ABOUT_ME = "about_me"
//        const val COLUMN_PROFILE_PICTURE = "profile_picture"
//        const val COLUMN_PROFILE_ID = "profile_id"
//        const val COLUMN_SAVED_AT = "saved_at"
//    }
//
//    override fun onCreate(db: SQLiteDatabase) {
//        // Create users table
//        val createUsersTable = """
//            CREATE TABLE $TABLE_USERS (
//                $COLUMN_ID TEXT PRIMARY KEY,
//                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
//                $COLUMN_PASSWORD TEXT NOT NULL,
//                $COLUMN_FULL_NAME TEXT NOT NULL
//            )
//        """.trimIndent()
//
//        // Create profiles table with AUTO-GENERATED ID? No, SQLite doesn't auto-gen UUIDs
//        val createProfilesTable = """
//            CREATE TABLE $TABLE_PROFILES (
//                $COLUMN_ID TEXT PRIMARY KEY,
//                $COLUMN_USER_ID TEXT NOT NULL,
//                $COLUMN_FULL_NAME TEXT NOT NULL,
//                $COLUMN_AGE INTEGER NOT NULL,
//                $COLUMN_EMAIL TEXT NOT NULL,
//                $COLUMN_PHONE TEXT NOT NULL,
//                $COLUMN_OCCUPATION TEXT NOT NULL,
//                $COLUMN_LOCATION TEXT NOT NULL,
//                $COLUMN_ABOUT_ME TEXT NOT NULL,
//                $COLUMN_PROFILE_PICTURE TEXT,
//                FOREIGN KEY ($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID)
//            )
//        """.trimIndent()
//
//        // Create saved profiles table
//        val createSavedProfilesTable = """
//            CREATE TABLE $TABLE_SAVED_PROFILES (
//                $COLUMN_ID TEXT PRIMARY KEY,
//                $COLUMN_PROFILE_ID TEXT NOT NULL,
//                $COLUMN_USER_ID TEXT NOT NULL,
//                $COLUMN_SAVED_AT LONG NOT NULL,
//                FOREIGN KEY ($COLUMN_PROFILE_ID) REFERENCES $TABLE_PROFILES($COLUMN_ID),
//                FOREIGN KEY ($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID)
//            )
//        """.trimIndent()
//
//        db.execSQL(createUsersTable)
//        db.execSQL(createProfilesTable)
//        db.execSQL(createSavedProfilesTable)
//    }
//
//    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
//        db.execSQL("DROP TABLE IF EXISTS $TABLE_SAVED_PROFILES")
//        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILES")
//        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
//        onCreate(db)
//    }
//
//    // User Operations
//    fun insertUser(id: String, email: String, password: String, fullName: String): Boolean {
//        val db = writableDatabase
//        val values = ContentValues().apply {
//            put(COLUMN_ID, id)
//            put(COLUMN_EMAIL, email)
//            put(COLUMN_PASSWORD, password)
//            put(COLUMN_FULL_NAME, fullName)
//        }
//        return try {
//            db.insert(TABLE_USERS, null, values) != -1L
//        } catch (e: Exception) {
//            Log.e(TAG, "Error inserting user: ${e.message}")
//            false
//        }
//    }
//
//    fun checkUser(email: String, password: String): String? {
//        val db = readableDatabase
//        val cursor = db.query(
//            TABLE_USERS,
//            arrayOf(COLUMN_ID),
//            "$COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?",
//            arrayOf(email, password),
//            null, null, null
//        )
//        val userId = if (cursor.moveToFirst()) cursor.getString(0) else null
//        cursor.close()
//        return userId
//    }
//
//    fun checkUserExists(email: String): Boolean {
//        val db = readableDatabase
//        val cursor = db.query(
//            TABLE_USERS,
//            arrayOf(COLUMN_ID),
//            "$COLUMN_EMAIL = ?",
//            arrayOf(email),
//            null, null, null
//        )
//        val exists = cursor.count > 0
//        cursor.close()
//        return exists
//    }
//
//    fun updatePassword(email: String, newPassword: String): Boolean {
//        val db = writableDatabase
//        val values = ContentValues().apply {
//            put(COLUMN_PASSWORD, newPassword)
//        }
//        return db.update(TABLE_USERS, values, "$COLUMN_EMAIL = ?", arrayOf(email)) > 0
//    }
//
//
//    fun getProfileByUserId(userId: String): Profile? {
//        val db = readableDatabase
//        val cursor = db.query(
//            TABLE_PROFILES,
//            null,
//            "$COLUMN_USER_ID = ?",
//            arrayOf(userId),
//            null, null, null
//        )
//
//        var profile: Profile? = null
//        if (cursor.moveToFirst()) {
//            profile = Profile(
//                id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
//                userId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
//                fullName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)),
//                age = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AGE)),
//                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
//                phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
//                occupation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OCCUPATION)),
//                location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
//                aboutMe = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ABOUT_ME)),
//                profilePictureUrl = cursor.getString(
//                    cursor.getColumnIndexOrThrow(
//                        COLUMN_PROFILE_PICTURE
//                    )
//                )
//            )
//        }
//        cursor.close()
//        return profile
//    }
//
//
//    fun getAllProfiles(): List<Profile> {
//        val profiles = mutableListOf<Profile>()
//        val db = readableDatabase
//        val cursor = db.query(TABLE_PROFILES, null, null, null, null, null, null)
//
//        while (cursor.moveToNext()) {
//            profiles.add(
//                Profile(
//                    id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
//                    userId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
//                    fullName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)),
//                    age = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AGE)),
//                    email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
//                    phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
//                    occupation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OCCUPATION)),
//                    location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
//                    aboutMe = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ABOUT_ME)),
//                    profilePictureUrl = cursor.getString(
//                        cursor.getColumnIndexOrThrow(
//                            COLUMN_PROFILE_PICTURE
//                        )
//                    )
//                )
//            )
//        }
//        cursor.close()
//        return profiles
//    }
//
//    // Saved Profiles Operations
//    fun saveProfile(savedProfile: SavedProfile): Boolean {
//        val db = writableDatabase
//        val values = ContentValues().apply {
//            put(COLUMN_ID, savedProfile.id)
//            put(COLUMN_PROFILE_ID, savedProfile.profileId)
//            put(COLUMN_USER_ID, savedProfile.userId)
//            put(COLUMN_SAVED_AT, savedProfile.savedAt)
//        }
//        return try {
//            db.insert(TABLE_SAVED_PROFILES, null, values) != -1L
//        } catch (e: Exception) {
//            Log.e(TAG, "Error saving profile: ${e.message}")
//            false
//        }
//    }
//
//    fun unsaveProfile(profileId: String, userId: String): Boolean {
//        val db = writableDatabase
//        return try {
//            db.delete(
//                TABLE_SAVED_PROFILES,
//                "$COLUMN_PROFILE_ID = ? AND $COLUMN_USER_ID = ?",
//                arrayOf(profileId, userId)
//            ) > 0
//        } catch (e: Exception) {
//            Log.e(TAG, "Error unsaving profile: ${e.message}")
//            false
//        }
//    }
//
//    fun isProfileSaved(profileId: String, userId: String): Boolean {
//        val db = readableDatabase
//        val cursor = db.query(
//            TABLE_SAVED_PROFILES,
//            arrayOf(COLUMN_ID),
//            "$COLUMN_PROFILE_ID = ? AND $COLUMN_USER_ID = ?",
//            arrayOf(profileId, userId),
//            null, null, null
//        )
//        val exists = cursor.count > 0
//        cursor.close()
//        return exists
//    }
//
//    fun getSavedProfileIds(userId: String): List<String> {
//        val profileIds = mutableListOf<String>()
//        val db = readableDatabase
//        val cursor = db.query(
//            TABLE_SAVED_PROFILES,
//            arrayOf(COLUMN_PROFILE_ID),
//            "$COLUMN_USER_ID = ?",
//            arrayOf(userId),
//            null, null, null
//        )
//
//        while (cursor.moveToNext()) {
//            profileIds.add(cursor.getString(0))
//        }
//        cursor.close()
//        return profileIds
//    }
//
//    fun getSavedProfiles(userId: String): List<Profile> {
//        val savedProfileIds = getSavedProfileIds(userId)
//        return getAllProfiles().filter { profile ->
//            savedProfileIds.contains(profile.id)
//        }
//    }
//
//
//
//
//    fun insertProfile(profile: Profile): Boolean {
//        val db = writableDatabase
//
//        // Generate local ID if needed
//        val localId = if (profile.id!!.isEmpty()) {
//            UUID.randomUUID().toString()
//        } else {
//            profile.id
//        }
//
//        val values = ContentValues().apply {
//            put(COLUMN_ID, localId)
//            put(COLUMN_USER_ID, profile.userId)
//            put(COLUMN_FULL_NAME, profile.fullName)
//            put(COLUMN_AGE, profile.age)
//            put(COLUMN_EMAIL, profile.email)
//            put(COLUMN_PHONE, profile.phoneNumber)
//            put(COLUMN_OCCUPATION, profile.occupation)
//            put(COLUMN_LOCATION, profile.location)
//            put(COLUMN_ABOUT_ME, profile.aboutMe)
//            put(COLUMN_PROFILE_PICTURE, profile.profilePictureUrl)
//        }
//
//        return try {
//            val result = db.insert(TABLE_PROFILES, null, values)
//            if (result != -1L) {
//                Log.d(TAG, "Profile inserted successfully with local ID: $localId")
//                true
//            } else {
//                Log.e(TAG, "Failed to insert profile")
//                false
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error inserting profile: ${e.message}")
//            false
//        }
//    }
//
//    fun updateProfile(profile: Profile): Boolean {
//        val db = writableDatabase
//        val values = ContentValues().apply {
//            put(COLUMN_FULL_NAME, profile.fullName)
//            put(COLUMN_AGE, profile.age)
//            put(COLUMN_EMAIL, profile.email)
//            put(COLUMN_PHONE, profile.phoneNumber)
//            put(COLUMN_OCCUPATION, profile.occupation)
//            put(COLUMN_LOCATION, profile.location)
//            put(COLUMN_ABOUT_ME, profile.aboutMe)
//            put(COLUMN_PROFILE_PICTURE, profile.profilePictureUrl)
//        }
//
//        return try {
//            val rowsAffected =
//                db.update(TABLE_PROFILES, values, "$COLUMN_ID = ?", arrayOf(profile.id))
//            if (rowsAffected > 0) {
//                Log.d(TAG, "Profile updated successfully: ${profile.id}")
//                true
//            } else {
//                Log.e(TAG, "No profile found with ID: ${profile.id}")
//                false
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error updating profile: ${e.message}")
//            false
//        }
//    }
//
//
//    // Add this method to DatabaseHelper.kt
//    fun getProfileById(profileId: String): Profile? {
//        val db = readableDatabase
//        val cursor = db.query(
//            TABLE_PROFILES,
//            null,
//            "$COLUMN_ID = ?",
//            arrayOf(profileId),
//            null, null, null
//        )
//
//        var profile: Profile? = null
//        if (cursor.moveToFirst()) {
//            profile = Profile(
//                id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
//                userId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
//                fullName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)),
//                age = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AGE)),
//                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
//                phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
//                occupation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OCCUPATION)),
//                location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
//                aboutMe = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ABOUT_ME)),
//                profilePictureUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_PICTURE))
//            )
//        }
//        cursor.close()
//        return profile
//    }
//}

package com.example.profilediscoveryapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.profilediscoveryapp.mvvm.model.dataclass.Profile
import com.example.profilediscoveryapp.mvvm.model.dataclass.SavedProfile
import java.util.UUID

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "profile_discovery.db"
        private const val DATABASE_VERSION = 3
        private const val TAG = "DatabaseHelper"

        // Table Names
        const val TABLE_USERS = "users"
        const val TABLE_PROFILES = "profiles"
        const val TABLE_SAVED_PROFILES = "saved_profiles"

        // Common Columns
        const val COLUMN_ID = "id"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_FULL_NAME = "full_name"
        const val COLUMN_AGE = "age"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_OCCUPATION = "occupation"
        const val COLUMN_LOCATION = "location"
        const val COLUMN_ABOUT_ME = "about_me"
        const val COLUMN_PROFILE_PICTURE = "profile_picture"
        const val COLUMN_PROFILE_ID = "profile_id"
        const val COLUMN_SAVED_AT = "saved_at"
        const val COLUMN_API_ID = "api_id"  // New column to store API's ID separately
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create users table
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_FULL_NAME TEXT NOT NULL
            )
        """.trimIndent()

        // Create profiles table with separate API ID column
        val createProfilesTable = """
            CREATE TABLE $TABLE_PROFILES (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_API_ID TEXT,
                $COLUMN_USER_ID TEXT NOT NULL,
                $COLUMN_FULL_NAME TEXT NOT NULL,
                $COLUMN_AGE INTEGER NOT NULL,
                $COLUMN_EMAIL TEXT NOT NULL,
                $COLUMN_PHONE TEXT NOT NULL,
                $COLUMN_OCCUPATION TEXT NOT NULL,
                $COLUMN_LOCATION TEXT NOT NULL,
                $COLUMN_ABOUT_ME TEXT NOT NULL,
                $COLUMN_PROFILE_PICTURE TEXT,
                FOREIGN KEY ($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID)
            )
        """.trimIndent()

        // Create saved profiles table
        val createSavedProfilesTable = """
            CREATE TABLE $TABLE_SAVED_PROFILES (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_PROFILE_ID TEXT NOT NULL,
                $COLUMN_USER_ID TEXT NOT NULL,
                $COLUMN_SAVED_AT LONG NOT NULL,
                FOREIGN KEY ($COLUMN_PROFILE_ID) REFERENCES $TABLE_PROFILES($COLUMN_ID),
                FOREIGN KEY ($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID)
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createProfilesTable)
        db.execSQL(createSavedProfilesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SAVED_PROFILES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // User Operations
    fun insertUser(id: String, email: String, password: String, fullName: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, id)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_FULL_NAME, fullName)
        }
        return try {
            db.insert(TABLE_USERS, null, values) != -1L
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting user: ${e.message}")
            false
        }
    }

    fun checkUser(email: String, password: String): String? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(email, password),
            null, null, null
        )
        val userId = if (cursor.moveToFirst()) cursor.getString(0) else null
        cursor.close()
        return userId
    }

    fun checkUserExists(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun updatePassword(email: String, newPassword: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PASSWORD, newPassword)
        }
        return db.update(TABLE_USERS, values, "$COLUMN_EMAIL = ?", arrayOf(email)) > 0
    }

    // Profile Operations
    fun insertProfile(profile: Profile): Boolean {
        val db = writableDatabase

        // Generate a unique local ID (UUID) instead of using API ID
        val localId = UUID.randomUUID().toString()

        val values = ContentValues().apply {
            put(COLUMN_ID, localId)
            put(COLUMN_API_ID, profile.id)
            put(COLUMN_USER_ID, profile.userId)
            put(COLUMN_FULL_NAME, profile.fullName)
            put(COLUMN_AGE, profile.age)
            put(COLUMN_EMAIL, profile.email)
            put(COLUMN_PHONE, profile.phoneNumber)
            put(COLUMN_OCCUPATION, profile.occupation)
            put(COLUMN_LOCATION, profile.location)
            put(COLUMN_ABOUT_ME, profile.aboutMe)
            put(COLUMN_PROFILE_PICTURE, profile.profilePictureUrl)
        }

        return try {
            val result = db.insert(TABLE_PROFILES, null, values)
            if (result != -1L) {
                Log.d(
                    TAG,
                    "Profile inserted successfully with local ID: $localId, API ID: ${profile.id}"
                )
                true
            } else {
                Log.e(TAG, "Failed to insert profile")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting profile: ${e.message}")
            false
        }
    }

    fun updateProfile(profile: Profile): Boolean {
        val db = writableDatabase

        // Find the local ID by API ID or user ID
        val localId = getLocalIdByApiId(profile.id) ?: getLocalIdByUserId(profile.userId)

        if (localId == null) {
            Log.e(
                TAG,
                "Cannot update profile: No local record found for API ID: ${profile.id} or User ID: ${profile.userId}"
            )
            return false
        }

        val values = ContentValues().apply {
            put(COLUMN_FULL_NAME, profile.fullName)
            put(COLUMN_AGE, profile.age)
            put(COLUMN_EMAIL, profile.email)
            put(COLUMN_PHONE, profile.phoneNumber)
            put(COLUMN_OCCUPATION, profile.occupation)
            put(COLUMN_LOCATION, profile.location)
            put(COLUMN_ABOUT_ME, profile.aboutMe)
            put(COLUMN_PROFILE_PICTURE, profile.profilePictureUrl)
        }

        return try {
            val rowsAffected = db.update(TABLE_PROFILES, values, "$COLUMN_ID = ?", arrayOf(localId))
            if (rowsAffected > 0) {
                Log.d(TAG, "Profile updated successfully: $localId")
                true
            } else {
                Log.e(TAG, "No profile found with ID: $localId")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile: ${e.message}")
            false
        }
    }

    fun getProfileByUserId(userId: String): Profile? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PROFILES,
            null,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId),
            null, null, null
        )

        var profile: Profile? = null
        if (cursor.moveToFirst()) {
            profile = Profile(
                id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_API_ID)) ?: "",
                userId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                fullName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)),
                age = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AGE)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                occupation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OCCUPATION)),
                location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                aboutMe = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ABOUT_ME)),
                profilePictureUrl = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        COLUMN_PROFILE_PICTURE
                    )
                )
            )
        }
        cursor.close()
        return profile
    }

    fun getProfileById(apiId: String): Profile? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PROFILES,
            null,
            "$COLUMN_API_ID = ?",
            arrayOf(apiId),
            null, null, null
        )

        var profile: Profile? = null
        if (cursor.moveToFirst()) {
            profile = Profile(
                id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_API_ID)) ?: "",
                userId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                fullName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)),
                age = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AGE)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                occupation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OCCUPATION)),
                location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                aboutMe = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ABOUT_ME)),
                profilePictureUrl = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        COLUMN_PROFILE_PICTURE
                    )
                )
            )
        }
        cursor.close()
        return profile
    }

    fun getLocalIdByApiId(apiId: String): String? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PROFILES,
            arrayOf(COLUMN_ID),
            "$COLUMN_API_ID = ?",
            arrayOf(apiId),
            null, null, null
        )
        val localId = if (cursor.moveToFirst()) cursor.getString(0) else null
        cursor.close()
        return localId
    }

    fun getLocalIdByUserId(userId: String): String? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PROFILES,
            arrayOf(COLUMN_ID),
            "$COLUMN_USER_ID = ?",
            arrayOf(userId),
            null, null, null
        )
        val localId = if (cursor.moveToFirst()) cursor.getString(0) else null
        cursor.close()
        return localId
    }

    fun getAllProfiles(): List<Profile> {
        val profiles = mutableListOf<Profile>()
        val db = readableDatabase
        val cursor = db.query(TABLE_PROFILES, null, null, null, null, null, null)

        while (cursor.moveToNext()) {
            profiles.add(
                Profile(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_API_ID)) ?: "",
                    userId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                    fullName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)),
                    age = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AGE)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                    occupation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OCCUPATION)),
                    location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                    aboutMe = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ABOUT_ME)),
                    profilePictureUrl = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                            COLUMN_PROFILE_PICTURE
                        )
                    )
                )
            )
        }
        cursor.close()
        return profiles
    }

    // Saved Profiles Operations
    fun saveProfile(savedProfile: SavedProfile): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, savedProfile.id)
            put(COLUMN_PROFILE_ID, savedProfile.profileId)
            put(COLUMN_USER_ID, savedProfile.userId)
            put(COLUMN_SAVED_AT, savedProfile.savedAt)
        }
        return try {
            db.insert(TABLE_SAVED_PROFILES, null, values) != -1L
        } catch (e: Exception) {
            Log.e(TAG, "Error saving profile: ${e.message}")
            false
        }
    }

    fun unsaveProfile(profileId: String, userId: String): Boolean {
        val db = writableDatabase
        return try {
            db.delete(
                TABLE_SAVED_PROFILES,
                "$COLUMN_PROFILE_ID = ? AND $COLUMN_USER_ID = ?",
                arrayOf(profileId, userId)
            ) > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error unsaving profile: ${e.message}")
            false
        }
    }

    fun isProfileSaved(profileId: String, userId: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SAVED_PROFILES,
            arrayOf(COLUMN_ID),
            "$COLUMN_PROFILE_ID = ? AND $COLUMN_USER_ID = ?",
            arrayOf(profileId, userId),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getSavedProfileIds(userId: String): List<String> {
        val profileIds = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SAVED_PROFILES,
            arrayOf(COLUMN_PROFILE_ID),
            "$COLUMN_USER_ID = ?",
            arrayOf(userId),
            null, null, null
        )

        while (cursor.moveToNext()) {
            profileIds.add(cursor.getString(0))
        }
        cursor.close()
        return profileIds
    }

    fun getSavedProfiles(userId: String): List<Profile> {
        val savedProfileIds = getSavedProfileIds(userId)
        return getAllProfiles().filter { profile ->
            savedProfileIds.contains(profile.id)
        }
    }
}