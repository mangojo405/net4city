package com.example.netcity.data

import android.content.Context
import com.example.R
import com.example.netcity.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NetCityRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("Pengguna harus masuk dengan Google sebelum mengakses Firestore.")

    private fun sanitizeDocId(raw: String, prefix: String): String {
        val cleaned = raw.trim().replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64)
        return if (cleaned.isNotEmpty()) cleaned else "${prefix}_${UUID.randomUUID().toString().take(8)}"
    }

    private fun <T> observeCollection(
        userId: String,
        collectionName: String,
        clazz: Class<T>
    ): Flow<List<T>> = flow {
        val path = "users/$userId/$collectionName"
        emitAll(
            db.collection("users").document(userId).collection(collectionName)
                .snapshots()
                .map { snap -> snap.toObjects(clazz, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeProfile(userId: String): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snap ->
                    if (snap.exists()) {
                        snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(ex, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeClasses(userId: String): Flow<List<ClassRoom>> =
        observeCollection(userId, "classes", ClassRoom::class.java)

    fun observeMembers(userId: String): Flow<List<SchoolMember>> =
        observeCollection(userId, "members", SchoolMember::class.java)

    fun observeCharacters(userId: String): Flow<List<NetCityCharacter>> =
        observeCollection(userId, "characters", NetCityCharacter::class.java)

    fun observeBarcodes(userId: String): Flow<List<BarcodeObject>> =
        observeCollection(userId, "barcodes", BarcodeObject::class.java)

    fun observeDioramaModules(userId: String): Flow<List<DioramaModule>> =
        observeCollection(userId, "diorama_modules", DioramaModule::class.java)

    fun observeScanLogs(userId: String): Flow<List<ScanLog>> =
        observeCollection(userId, "scan_logs", ScanLog::class.java)

    fun observeCityWorlds(userId: String): Flow<List<CityWorld>> =
        observeCollection(userId, "city_worlds", CityWorld::class.java)

    fun observeAssessments(userId: String): Flow<List<AssessmentRecord>> =
        observeCollection(userId, "assessments", AssessmentRecord::class.java)

    fun isVerifiedBootstrappedAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        return user.email.equals(PRIMARY_ADMIN_EMAIL, ignoreCase = true) && user.isEmailVerified
    }

    fun observeAdmins(): Flow<List<AdminRecord>> = flow {
        if (!isVerifiedBootstrappedAdmin()) {
            emit(emptyList())
            return@flow
        }
        val uid = requireUserId()
        val path = "admins"
        emitAll(
            db.collection("admins")
                .snapshots()
                .map { snap -> snap.toObjects(AdminRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun registerAdminRecord(
        adminDocId: String,
        displayName: String,
        email: String
    ): Result<Unit> {
        var path = "admins/$adminDocId"
        return try {
            requireUserId()
            check(isVerifiedBootstrappedAdmin()) { "Hanya admin terverifikasi ($PRIMARY_ADMIN_EMAIL) yang dapat mengelola database admin." }
            val cleanName = displayName.trim().take(80)
            val cleanEmail = email.trim().lowercase().take(120)
            require(cleanName.length >= 2) { "Nama admin minimal 2 karakter" }
            require(cleanEmail.contains("@") && cleanEmail.length >= 5) { "Email admin tidak valid" }

            val safeId = sanitizeDocId(adminDocId, "admin")
            val ref = db.collection("admins").document(safeId)
            path = ref.path
            val existing = ref.get().await()
            if (!existing.exists()) {
                ref.set(
                    mapOf(
                        "displayName" to cleanName,
                        "email" to cleanEmail,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                ref.update(
                    mapOf(
                        "displayName" to cleanName,
                        "email" to cleanEmail,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun ensureWorkspaceInitialized(displayName: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("Belum login"))
        val uid = user.uid
        val userPath = "users/$uid"
        return try {
            val isPrimaryAdmin = isVerifiedBootstrappedAdmin()
            if (isPrimaryAdmin) {
                registerAdminRecord(
                    adminDocId = uid,
                    displayName = PRIMARY_ADMIN_NAME,
                    email = PRIMARY_ADMIN_EMAIL
                )
            }

            val userRef = db.collection("users").document(uid)
            val snap = userRef.get().await()
            if (!snap.exists()) {
                val safeName = if (isPrimaryAdmin) PRIMARY_ADMIN_NAME
                else displayName.trim().ifBlank { "Pengguna NET4CITY" }.take(80)
                userRef.set(
                    mapOf(
                        "userId" to uid,
                        "displayName" to safeName,
                        "workspacePersona" to WorkspacePersona.ADMIN.name,
                        "selectedClassId" to "x_tkj_1",
                        "selectedStudentId" to "siswa_01",
                        "demoSeeded" to false,
                        "coins" to 5000L,
                        "xp" to 250L,
                        "unlockedLevel" to 3L,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                seedDemoData().getOrThrow()
            } else {
                if (isPrimaryAdmin && snap.getString("displayName") != PRIMARY_ADMIN_NAME) {
                    userRef.update(
                        mapOf(
                            "displayName" to PRIMARY_ADMIN_NAME,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
                val seeded = snap.getBoolean("demoSeeded") ?: false
                if (!seeded) {
                    seedDemoData().getOrThrow()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userPath)
            Result.failure(e)
        }
    }

    companion object {
        const val PRIMARY_ADMIN_NAME = "Dabojo"
        const val PRIMARY_ADMIN_EMAIL = "dabojo.1487@gmail.com"
    }

    suspend fun seedDemoData(): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Belum login"))
        val basePath = "users/$uid"
        return try {
            val userDoc = db.collection("users").document(uid)

            suspend fun writeBatch(collectionName: String, entries: List<Pair<String, Map<String, Any>>>) {
                val batch = db.batch()
                entries.forEach { (docId, payload) ->
                    val ref = userDoc.collection(collectionName).document(docId)
                    val fullPayload = payload.toMutableMap().apply {
                        put("createdAt", FieldValue.serverTimestamp())
                        put("updatedAt", FieldValue.serverTimestamp())
                    }
                    batch.set(ref, fullPayload)
                }
                batch.commit().await()
            }

            writeBatch("classes", CurriculumCatalog.createDemoClasses(uid))
            writeBatch("members", CurriculumCatalog.createDemoMembers(uid))
            writeBatch("characters", CurriculumCatalog.createDemoCharacters(uid))
            writeBatch("barcodes", CurriculumCatalog.createDemoBarcodes(uid))
            writeBatch("diorama_modules", CurriculumCatalog.createDemoDioramaModules(uid))
            writeBatch("assessments", CurriculumCatalog.createDemoAssessments(uid))

            // Create initial city worlds for X TKJ 1..4
            val worldBatch = db.batch()
            listOf("x_tkj_1", "x_tkj_2", "x_tkj_3", "x_tkj_4").forEach { classId ->
                val ref = userDoc.collection("city_worlds").document("world_$classId")
                val initialNodes = listOf(
                    CityNode("node_lab_1", "LAB", "Lab TKJ", 3, 3, "NC-LAB-001").toMap(),
                    CityNode("node_pc_1", "PC", "PC-01", 4, 3, "NC-PC-001").toMap(),
                    CityNode("node_swt_1", "SWITCH", "Switch 24P", 4, 4, "NC-SWT-001").toMap(),
                    CityNode("node_sch_1", "SEKOLAH", "SMK NET4CITY", 2, 5, "NC-SCH-001").toMap()
                )
                val initialLinks = listOf(
                    CableLink("node_pc_1", "node_swt_1", "UTP").toMap(),
                    CableLink("node_lab_1", "node_swt_1", "UTP").toMap()
                )
                worldBatch.set(
                    ref,
                    mapOf(
                        "userId" to uid,
                        "classId" to classId,
                        "activeLevel" to 1L,
                        "coins" to 5000L,
                        "xp" to 250L,
                        "networkHealth" to 96L,
                        "activeFault" to "",
                        "stemPhase" to StemPhase.EXPLORE.name,
                        "completedLevels" to emptyList<Long>(),
                        "placedNodes" to initialNodes,
                        "cableLinks" to initialLinks,
                        "sessionInventory" to listOf("NC-LAB-001", "NC-PC-001", "NC-SWT-001"),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            worldBatch.commit().await()

            // Initial demo scan log
            userDoc.collection("scan_logs").document("scan_demo_01").set(
                mapOf(
                    "userId" to uid,
                    "barcodeCode" to "NC-SWT-001",
                    "objectName" to "Switch Manageable 24P-A",
                    "classId" to "x_tkj_1",
                    "studentName" to "Raka Pratama",
                    "sessionName" to "Sesi Simulasi Awal",
                    "addedToInventory" to true,
                    "scanNote" to "Data simulasi: perangkat berhasil dipindai ke inventory sesi.",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            userDoc.update(
                mapOf(
                    "demoSeeded" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, basePath)
            Result.failure(e)
        }
    }

    suspend fun updateWorkspacePersona(persona: WorkspacePersona): Result<Unit> {
        var path = "users/unknown"
        return try {
            val uid = requireUserId()
            path = "users/$uid"
            db.collection("users").document(uid).update(
                mapOf(
                    "workspacePersona" to persona.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateSelectedClass(classId: String): Result<Unit> {
        var path = "users/unknown"
        return try {
            val uid = requireUserId()
            path = "users/$uid"
            db.collection("users").document(uid).update(
                mapOf(
                    "selectedClassId" to classId.take(64),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- CLASS MANAGEMENT ---
    suspend fun saveClassRoom(
        existingId: String?,
        code: String,
        name: String,
        teacherName: String,
        maxUnlockedLevel: Int,
        activeSessionMission: String,
        labGroups: List<String>
    ): Result<Unit> {
        var path = "users/unknown/classes"
        return try {
            val uid = requireUserId()
            val cleanCode = code.trim().take(30)
            val cleanName = name.trim().take(80)
            val cleanTeacher = teacherName.trim().take(80)
            require(cleanCode.length >= 2) { "Kode kelas minimal 2 karakter" }
            require(cleanName.length >= 2) { "Nama kelas minimal 2 karakter" }
            require(cleanTeacher.length >= 2) { "Nama guru pengampu minimal 2 karakter" }
            val boundedGroups = labGroups.map { it.trim().take(80) }.filter { it.isNotEmpty() }.take(10)
            val levelClamped = maxUnlockedLevel.coerceIn(1, 10).toLong()

            val docId = existingId?.takeIf { it.isNotBlank() } ?: sanitizeDocId(cleanCode.lowercase(), "kelas")
            val ref = db.collection("users").document(uid).collection("classes").document(docId)
            path = ref.path
            if (existingId.isNullOrBlank()) {
                ref.set(
                    mapOf(
                        "userId" to uid,
                        "code" to cleanCode,
                        "name" to cleanName,
                        "teacherName" to cleanTeacher,
                        "maxUnlockedLevel" to levelClamped,
                        "activeSessionMission" to activeSessionMission.trim().take(120),
                        "labGroups" to boundedGroups,
                        "demoData" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                ref.update(
                    mapOf(
                        "code" to cleanCode,
                        "name" to cleanName,
                        "teacherName" to cleanTeacher,
                        "maxUnlockedLevel" to levelClamped,
                        "activeSessionMission" to activeSessionMission.trim().take(120),
                        "labGroups" to boundedGroups,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteClassRoom(classId: String): Result<Unit> {
        var path = "users/unknown/classes/$classId"
        return try {
            val uid = requireUserId()
            val ref = db.collection("users").document(uid).collection("classes").document(classId)
            path = ref.path
            ref.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    // --- MEMBER (GURU / SISWA) CRUD + CSV IMPORT ---
    suspend fun saveMember(
        existingId: String?,
        memberType: String,
        fullName: String,
        identityNumber: String,
        classId: String,
        assignedClasses: List<String>,
        groupName: String
    ): Result<Unit> {
        var path = "users/unknown/members"
        return try {
            val uid = requireUserId()
            val safeType = if (memberType == "GURU") "GURU" else "SISWA"
            val cleanName = fullName.trim().take(80)
            val cleanIdNum = identityNumber.trim().take(40)
            require(cleanName.length >= 2) { "Nama lengkap minimal 2 karakter" }
            require(cleanIdNum.length >= 2) { "NIS/NIP minimal 2 karakter" }

            val docId = existingId?.takeIf { it.isNotBlank() } ?: sanitizeDocId("${safeType.lowercase()}_${UUID.randomUUID().toString().take(8)}", "mem")
            val ref = db.collection("users").document(uid).collection("members").document(docId)
            path = ref.path

            if (existingId.isNullOrBlank()) {
                ref.set(
                    mapOf(
                        "userId" to uid,
                        "memberType" to safeType,
                        "fullName" to cleanName,
                        "identityNumber" to cleanIdNum,
                        "classId" to classId.trim().take(64),
                        "assignedClasses" to assignedClasses.take(10),
                        "groupName" to groupName.trim().take(60),
                        "xp" to 0L,
                        "level" to 1L,
                        "characterId" to "",
                        "demoData" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                ref.update(
                    mapOf(
                        "memberType" to safeType,
                        "fullName" to cleanName,
                        "identityNumber" to cleanIdNum,
                        "classId" to classId.trim().take(64),
                        "assignedClasses" to assignedClasses.take(10),
                        "groupName" to groupName.trim().take(60),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun importStudentsCsv(csvText: String, defaultClassId: String): Result<Int> {
        var path = "users/unknown/members"
        return try {
            val uid = requireUserId()
            path = "users/$uid/members"
            val lines = csvText.lines().map { it.trim() }.filter { it.isNotEmpty() }
            val batch = db.batch()
            var count = 0
            for (line in lines.take(40)) {
                val cols = line.split(",", ";").map { it.trim() }
                if (cols.size < 2) continue
                val name = cols[0].take(80)
                val nis = cols[1].take(40)
                if (name.equals("nama", ignoreCase = true) || name.length < 2 || nis.length < 2) continue
                val cls = cols.getOrNull(2)?.takeIf { it.isNotBlank() }?.lowercase()?.replace(" ", "_") ?: defaultClassId
                val grp = cols.getOrNull(3)?.take(60) ?: "Kelompok Alpha"
                val docId = sanitizeDocId("csv_${nis.lowercase()}_${count}", "siswa")
                val ref = db.collection("users").document(uid).collection("members").document(docId)
                batch.set(
                    ref,
                    mapOf(
                        "userId" to uid,
                        "memberType" to "SISWA",
                        "fullName" to name,
                        "identityNumber" to nis,
                        "classId" to cls.take(64),
                        "assignedClasses" to emptyList<String>(),
                        "groupName" to grp,
                        "xp" to 0L,
                        "level" to 1L,
                        "characterId" to "",
                        "demoData" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                count++
            }
            if (count > 0) batch.commit().await()
            Result.success(count)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteMember(memberId: String): Result<Unit> {
        var path = "users/unknown/members/$memberId"
        return try {
            val uid = requireUserId()
            val ref = db.collection("users").document(uid).collection("members").document(memberId)
            path = ref.path
            ref.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    // --- CHARACTER STUDIO ---
    suspend fun saveCharacter(
        existingId: String?,
        characterCode: String,
        name: String,
        ownerName: String,
        classId: String,
        profession: String,
        faceShape: String,
        hairStyle: String,
        hairColor: String,
        outfitStyle: String,
        pantsColor: String,
        shoeStyle: String,
        accessory: String,
        workTool: String,
        skills: List<String>
    ): Result<String> {
        var path = "users/unknown/characters"
        return try {
            val uid = requireUserId()
            val cleanCode = characterCode.trim().ifBlank {
                "NC-CHAR-%05d".format((1000..99999).random())
            }.take(32)
            val cleanName = name.trim().take(60)
            val cleanOwner = ownerName.trim().take(80)
            require(cleanName.length >= 2) { "Nama karakter minimal 2 karakter" }
            require(cleanOwner.length >= 2) { "Nama pemilik minimal 2 karakter" }

            val docId = existingId?.takeIf { it.isNotBlank() } ?: sanitizeDocId(cleanCode, "char")
            val ref = db.collection("users").document(uid).collection("characters").document(docId)
            path = ref.path

            if (existingId.isNullOrBlank()) {
                ref.set(
                    mapOf(
                        "userId" to uid,
                        "characterCode" to cleanCode,
                        "name" to cleanName,
                        "ownerName" to cleanOwner,
                        "classId" to classId.take(64),
                        "profession" to profession.take(60),
                        "faceShape" to faceShape.take(30),
                        "hairStyle" to hairStyle.take(30),
                        "hairColor" to hairColor.take(30),
                        "outfitStyle" to outfitStyle.take(40),
                        "pantsColor" to pantsColor.take(30),
                        "shoeStyle" to shoeStyle.take(30),
                        "accessory" to accessory.take(40),
                        "workTool" to workTool.take(40),
                        "level" to 1L,
                        "xp" to 100L,
                        "skills" to skills.take(10),
                        "demoData" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                ref.update(
                    mapOf(
                        "name" to cleanName,
                        "ownerName" to cleanOwner,
                        "classId" to classId.take(64),
                        "profession" to profession.take(60),
                        "faceShape" to faceShape.take(30),
                        "hairStyle" to hairStyle.take(30),
                        "hairColor" to hairColor.take(30),
                        "outfitStyle" to outfitStyle.take(40),
                        "pantsColor" to pantsColor.take(30),
                        "shoeStyle" to shoeStyle.take(30),
                        "accessory" to accessory.take(40),
                        "workTool" to workTool.take(40),
                        "skills" to skills.take(10),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(cleanCode)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- BARCODE MANAGEMENT & SCAN MODE ---
    suspend fun saveBarcodeObject(
        existingId: String?,
        barcodeCode: String,
        objectName: String,
        category: String,
        functionDesc: String,
        specs: String,
        portCount: Int,
        virtualPrice: Int,
        installNeeds: String,
        relatedLevel: Int,
        storageLocation: String,
        inventoryStatus: String
    ): Result<Unit> {
        var path = "users/unknown/barcodes"
        return try {
            val uid = requireUserId()
            val cleanCode = barcodeCode.trim().uppercase().take(32)
            val cleanName = objectName.trim().take(80)
            require(cleanCode.length >= 4) { "Kode barcode minimal 4 karakter (contoh: NC-RTR-003)" }
            require(cleanName.length >= 2) { "Nama objek minimal 2 karakter" }

            val docId = existingId?.takeIf { it.isNotBlank() } ?: sanitizeDocId(cleanCode, "bc")
            val ref = db.collection("users").document(uid).collection("barcodes").document(docId)
            path = ref.path
            val safeStatus = if (inventoryStatus in listOf("TERSEDIA", "DIGUNAKAN", "PERBAIKAN")) inventoryStatus else "TERSEDIA"

            if (existingId.isNullOrBlank()) {
                ref.set(
                    mapOf(
                        "userId" to uid,
                        "barcodeCode" to cleanCode,
                        "objectName" to cleanName,
                        "category" to category.trim().ifBlank { "Perangkat" }.take(40),
                        "functionDesc" to functionDesc.trim().ifBlank { "Perangkat praktik jaringan TKJ." }.take(300),
                        "specs" to specs.trim().ifBlank { "Standar laboratorium SMK." }.take(300),
                        "portCount" to portCount.coerceIn(0, 256).toLong(),
                        "virtualPrice" to virtualPrice.coerceIn(0, 1000000).toLong(),
                        "installNeeds" to installNeeds.trim().take(200),
                        "relatedLevel" to relatedLevel.coerceIn(1, 10).toLong(),
                        "storageLocation" to storageLocation.trim().ifBlank { "Rak Lab TJKT" }.take(80),
                        "inventoryStatus" to safeStatus,
                        "activeClassId" to "",
                        "activeStudentName" to "",
                        "demoData" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                ref.update(
                    mapOf(
                        "objectName" to cleanName,
                        "category" to category.trim().ifBlank { "Perangkat" }.take(40),
                        "functionDesc" to functionDesc.trim().ifBlank { "Perangkat praktik jaringan TKJ." }.take(300),
                        "specs" to specs.trim().ifBlank { "Standar laboratorium SMK." }.take(300),
                        "portCount" to portCount.coerceIn(0, 256).toLong(),
                        "virtualPrice" to virtualPrice.coerceIn(0, 1000000).toLong(),
                        "installNeeds" to installNeeds.trim().take(200),
                        "relatedLevel" to relatedLevel.coerceIn(1, 10).toLong(),
                        "storageLocation" to storageLocation.trim().ifBlank { "Rak Lab TJKT" }.take(80),
                        "inventoryStatus" to safeStatus,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun recordScanEvent(
        barcodeCode: String,
        objectName: String,
        barcodeDocId: String?,
        classId: String,
        studentName: String,
        sessionName: String,
        addToSessionInventory: Boolean,
        scanNote: String,
        worldDocId: String?,
        currentInventory: List<String>
    ): Result<Unit> {
        var path = "users/unknown/scan_logs"
        return try {
            val uid = requireUserId()
            val logId = "scan_${System.currentTimeMillis()}_${(100..999).random()}"
            val logRef = db.collection("users").document(uid).collection("scan_logs").document(logId)
            path = logRef.path
            logRef.set(
                mapOf(
                    "userId" to uid,
                    "barcodeCode" to barcodeCode.take(32),
                    "objectName" to objectName.take(80),
                    "classId" to classId.take(64),
                    "studentName" to studentName.take(80),
                    "sessionName" to sessionName.take(120),
                    "addedToInventory" to addToSessionInventory,
                    "scanNote" to scanNote.take(200),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            if (!barcodeDocId.isNullOrBlank()) {
                db.collection("users").document(uid).collection("barcodes").document(barcodeDocId)
                    .update(
                        mapOf(
                            "inventoryStatus" to "DIGUNAKAN",
                            "activeClassId" to classId.take(64),
                            "activeStudentName" to studentName.take(80),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
            }

            if (addToSessionInventory && !worldDocId.isNullOrBlank() && !currentInventory.contains(barcodeCode)) {
                val updated = (currentInventory + barcodeCode).distinct().take(40)
                db.collection("users").document(uid).collection("city_worlds").document(worldDocId)
                    .update(
                        mapOf(
                            "sessionInventory" to updated,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- DIORAMA MANAGER ---
    suspend fun updateDioramaModuleStatus(
        moduleId: String,
        newStatus: String,
        activeClassId: String,
        activeSessionLabel: String,
        verifiedByTeacher: Boolean
    ): Result<Unit> {
        var path = "users/unknown/diorama_modules/$moduleId"
        return try {
            val uid = requireUserId()
            val ref = db.collection("users").document(uid).collection("diorama_modules").document(moduleId)
            path = ref.path
            val safeStatus = if (newStatus == "DIGUNAKAN") "DIGUNAKAN" else "TERSEDIA"
            ref.update(
                mapOf(
                    "moduleStatus" to safeStatus,
                    "activeClassId" to activeClassId.take(64),
                    "activeSessionLabel" to activeSessionLabel.take(120),
                    "verifiedByTeacher" to verifiedByTeacher,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- CITY WORLD SIMULATION ---
    suspend fun updateCityWorld(
        worldId: String,
        activeLevel: Int,
        coins: Long,
        xp: Long,
        networkHealth: Int,
        activeFault: String,
        stemPhase: StemPhase,
        completedLevels: List<Long>,
        nodes: List<CityNode>,
        links: List<CableLink>,
        sessionInventory: List<String>
    ): Result<Unit> {
        var path = "users/unknown/city_worlds/$worldId"
        return try {
            val uid = requireUserId()
            val ref = db.collection("users").document(uid).collection("city_worlds").document(worldId)
            path = ref.path
            ref.update(
                mapOf(
                    "activeLevel" to activeLevel.coerceIn(1, 10).toLong(),
                    "coins" to coins.coerceIn(0L, 10_000_000L),
                    "xp" to xp.coerceIn(0L, 10_000_000L),
                    "networkHealth" to networkHealth.coerceIn(0, 100).toLong(),
                    "activeFault" to activeFault.take(120),
                    "stemPhase" to stemPhase.name,
                    "completedLevels" to completedLevels.distinct().take(10),
                    "placedNodes" to nodes.take(60).map { it.toMap() },
                    "cableLinks" to links.take(80).map { it.toMap() },
                    "sessionInventory" to sessionInventory.distinct().take(40),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- ASSESSMENTS ---
    suspend fun saveAssessment(
        existingId: String?,
        studentName: String,
        classId: String,
        groupName: String,
        levelNumber: Int,
        missionTitle: String,
        durationMinutes: Int,
        technicalScore: Int,
        designScore: Int,
        physicalScore: Int,
        troubleshootingScore: Int,
        gameResultSummary: String,
        dioramaVerified: Boolean,
        teacherNote: String,
        status: String
    ): Result<Unit> {
        var path = "users/unknown/assessments"
        return try {
            val uid = requireUserId()
            val cleanStudent = studentName.trim().take(80)
            val cleanMission = missionTitle.trim().take(120)
            require(cleanStudent.length >= 2) { "Nama siswa minimal 2 karakter" }
            require(cleanMission.length >= 2) { "Judul misi minimal 2 karakter" }

            val t = technicalScore.coerceIn(0, 100)
            val d = designScore.coerceIn(0, 100)
            val p = physicalScore.coerceIn(0, 100)
            val tr = troubleshootingScore.coerceIn(0, 100)
            val finalScore = ((t + d + p + tr) / 4.0).toInt().coerceIn(0, 100)
            val safeStatus = if (status == "FINALIZED") "FINALIZED" else "DRAFT"

            val docId = existingId?.takeIf { it.isNotBlank() } ?: sanitizeDocId("assess_${System.currentTimeMillis()}", "assess")
            val ref = db.collection("users").document(uid).collection("assessments").document(docId)
            path = ref.path

            if (existingId.isNullOrBlank()) {
                ref.set(
                    mapOf(
                        "userId" to uid,
                        "studentName" to cleanStudent,
                        "classId" to classId.ifBlank { "x_tkj_1" }.take(64),
                        "groupName" to groupName.trim().take(60),
                        "levelNumber" to levelNumber.coerceIn(1, 10).toLong(),
                        "missionTitle" to cleanMission,
                        "durationMinutes" to durationMinutes.coerceIn(0, 600).toLong(),
                        "technicalScore" to t.toLong(),
                        "designScore" to d.toLong(),
                        "physicalScore" to p.toLong(),
                        "troubleshootingScore" to tr.toLong(),
                        "finalScore" to finalScore.toLong(),
                        "gameResultSummary" to gameResultSummary.trim().take(200),
                        "dioramaVerified" to dioramaVerified,
                        "teacherNote" to teacherNote.trim().take(500),
                        "status" to safeStatus,
                        "demoData" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                ref.update(
                    mapOf(
                        "studentName" to cleanStudent,
                        "classId" to classId.ifBlank { "x_tkj_1" }.take(64),
                        "groupName" to groupName.trim().take(60),
                        "levelNumber" to levelNumber.coerceIn(1, 10).toLong(),
                        "missionTitle" to cleanMission,
                        "durationMinutes" to durationMinutes.coerceIn(0, 600).toLong(),
                        "technicalScore" to t.toLong(),
                        "designScore" to d.toLong(),
                        "physicalScore" to p.toLong(),
                        "troubleshootingScore" to tr.toLong(),
                        "finalScore" to finalScore.toLong(),
                        "gameResultSummary" to gameResultSummary.trim().take(200),
                        "dioramaVerified" to dioramaVerified,
                        "teacherNote" to teacherNote.trim().take(500),
                        "status" to safeStatus,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }
}
