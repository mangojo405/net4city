package com.example.netcity.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.netcity.data.NetCityRepository
import com.example.netcity.model.*
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class ScanResultUi(
    val code: String,
    val foundObject: BarcodeObject?,
    val foundCharacter: NetCityCharacter?,
    val addedToInventory: Boolean,
    val message: String
)

data class NetCitySnapshot(
    val profile: UserProfile?,
    val admins: List<AdminRecord>,
    val classes: List<ClassRoom>,
    val members: List<SchoolMember>,
    val characters: List<NetCityCharacter>,
    val barcodes: List<BarcodeObject>,
    val dioramaModules: List<DioramaModule>,
    val scanLogs: List<ScanLog>,
    val cityWorlds: List<CityWorld>,
    val assessments: List<AssessmentRecord>
) {
    val activePersona: WorkspacePersona
        get() = WorkspacePersona.fromRaw(profile?.workspacePersona)

    val selectedClassId: String
        get() = profile?.selectedClassId?.ifBlank { classes.firstOrNull()?.id ?: "x_tkj_1" } ?: "x_tkj_1"

    val activeClass: ClassRoom?
        get() = classes.firstOrNull { it.id == selectedClassId } ?: classes.firstOrNull()

    val activeWorld: CityWorld?
        get() = cityWorlds.firstOrNull { it.classId == selectedClassId } ?: cityWorlds.firstOrNull()

    val teachers: List<SchoolMember>
        get() = members.filter { it.memberType == "GURU" }

    val students: List<SchoolMember>
        get() = members.filter { it.memberType == "SISWA" }

    val classStudents: List<SchoolMember>
        get() = students.filter { it.classId == selectedClassId }
}

class NetCityViewModel(
    private val repository: NetCityRepository,
    private val currentUserId: String,
    private val currentUserDisplayName: String
) : ViewModel() {

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _lastScanResult = MutableStateFlow<ScanResultUi?>(null)
    val lastScanResult: StateFlow<ScanResultUi?> = _lastScanResult.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val coreA = combine(
        repository.observeProfile(currentUserId),
        repository.observeClasses(currentUserId),
        repository.observeMembers(currentUserId),
        repository.observeCharacters(currentUserId),
        repository.observeBarcodes(currentUserId)
    ) { profile, classes, members, characters, barcodes ->
        CorePartA(profile, classes, members, characters, barcodes)
    }

    private val coreB = combine(
        repository.observeDioramaModules(currentUserId),
        repository.observeScanLogs(currentUserId),
        repository.observeCityWorlds(currentUserId),
        repository.observeAssessments(currentUserId),
        repository.observeAdmins()
    ) { diorama, scans, worlds, assessments, admins ->
        CorePartB(diorama, scans, worlds, assessments, admins)
    }

    val uiState: StateFlow<UiState<NetCitySnapshot>> = combine(coreA, coreB) { a, b ->
        NetCitySnapshot(
            profile = a.profile,
            admins = b.admins.sortedBy { it.displayName },
            classes = a.classes.sortedBy { it.code },
            members = a.members.sortedBy { it.fullName },
            characters = a.characters.sortedBy { it.characterCode },
            barcodes = a.barcodes.sortedBy { it.barcodeCode },
            dioramaModules = b.diorama.sortedBy { it.moduleCode },
            scanLogs = b.scans.sortedByDescending { it.createdAt?.seconds ?: 0L },
            cityWorlds = b.worlds,
            assessments = b.assessments.sortedByDescending { it.updatedAt?.seconds ?: 0L }
        )
    }
        .map<NetCitySnapshot, UiState<NetCitySnapshot>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing NETCITY workspace", rootError)
            emit(UiState.Error(rootError.message ?: "Gagal memuat data Cloud Firestore."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    init {
        viewModelScope.launch {
            _isBusy.value = true
            repository.ensureWorkspaceInitialized(currentUserDisplayName)
                .onFailure { e -> _statusBanner.value = "Inisialisasi workspace gagal: ${e.localizedMessage}" }
            _isBusy.value = false
        }
    }

    fun clearBanner() {
        _statusBanner.value = null
    }

    fun showMessage(msg: String) {
        _statusBanner.value = msg
    }

    fun switchPersona(persona: WorkspacePersona) {
        viewModelScope.launch {
            repository.updateWorkspacePersona(persona)
                .onSuccess { _statusBanner.value = "Beralih ke tampilan ${persona.label}" }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Gagal mengganti role." }
        }
    }

    fun selectClass(classId: String) {
        viewModelScope.launch {
            repository.updateSelectedClass(classId)
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Gagal mengganti kelas." }
        }
    }

    fun reseedDemoData() {
        viewModelScope.launch {
            _isBusy.value = true
            repository.seedDemoData()
                .onSuccess { _statusBanner.value = "Data simulasi demo (2 Guru, 4 Kelas, 20 Siswa, 20 Barcode, 10 Diorama) berhasil disinkronkan!" }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Gagal mengisi ulang data demo." }
            _isBusy.value = false
        }
    }

    fun syncPrimaryAdmin(displayName: String = NetCityRepository.PRIMARY_ADMIN_NAME, email: String = NetCityRepository.PRIMARY_ADMIN_EMAIL) {
        viewModelScope.launch {
            repository.registerAdminRecord(currentUserId, displayName, email)
                .onSuccess { _statusBanner.value = "Admin $displayName ($email) tersimpan di database /admins Firestore." }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Gagal mendaftarkan admin." }
        }
    }

    fun saveClassRoom(
        existingId: String?,
        code: String,
        name: String,
        teacherName: String,
        maxUnlockedLevel: Int,
        activeSessionMission: String,
        labGroups: List<String>
    ) {
        viewModelScope.launch {
            repository.saveClassRoom(existingId, code, name, teacherName, maxUnlockedLevel, activeSessionMission, labGroups)
                .onSuccess { _statusBanner.value = "Kelas $code tersimpan ke Firestore." }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Gagal menyimpan kelas." }
        }
    }

    fun deleteClassRoom(classId: String) {
        viewModelScope.launch {
            repository.deleteClassRoom(classId)
                .onSuccess { _statusBanner.value = "Kelas dihapus." }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Gagal menghapus kelas." }
        }
    }

    fun saveMember(
        existingId: String?,
        memberType: String,
        fullName: String,
        identityNumber: String,
        classId: String,
        assignedClasses: List<String>,
        groupName: String
    ) {
        viewModelScope.launch {
            repository.saveMember(existingId, memberType, fullName, identityNumber, classId, assignedClasses, groupName)
                .onSuccess { _statusBanner.value = "Data $memberType ($fullName) tersimpan." }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Gagal menyimpan data anggota." }
        }
    }

    fun importStudentsCsv(csvText: String, defaultClassId: String) {
        viewModelScope.launch {
            repository.importStudentsCsv(csvText, defaultClassId)
                .onSuccess { count -> _statusBanner.value = "Berhasil mengimpor $count siswa dari CSV ke Firestore." }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Gagal impor CSV." }
        }
    }

    fun deleteMember(memberId: String) {
        viewModelScope.launch {
            repository.deleteMember(memberId)
                .onSuccess { _statusBanner.value = "Data anggota dihapus." }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Gagal menghapus anggota." }
        }
    }

    fun saveCharacter(
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
    ) {
        viewModelScope.launch {
            repository.saveCharacter(
                existingId, characterCode, name, ownerName, classId, profession,
                faceShape, hairStyle, hairColor, outfitStyle, pantsColor, shoeStyle,
                accessory, workTool, skills
            )
                .onSuccess { code -> _statusBanner.value = "Karakter $name ($code) tersimpan & QR siap digunakan!" }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Gagal menyimpan karakter." }
        }
    }

    fun saveBarcodeObject(
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
    ) {
        viewModelScope.launch {
            repository.saveBarcodeObject(
                existingId, barcodeCode, objectName, category, functionDesc, specs,
                portCount, virtualPrice, installNeeds, relatedLevel, storageLocation, inventoryStatus
            )
                .onSuccess { _statusBanner.value = "Objek Barcode $barcodeCode berhasil disimpan." }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Gagal menyimpan objek barcode." }
        }
    }

    fun processScannedBarcode(
        rawInput: String,
        snapshot: NetCitySnapshot,
        studentName: String,
        sessionName: String
    ) {
        val code = rawInput.trim().uppercase()
        if (code.length < 4) {
            _statusBanner.value = "Kode barcode tidak valid (minimal 4 karakter)."
            return
        }
        val matchedObj = snapshot.barcodes.firstOrNull { it.barcodeCode.equals(code, ignoreCase = true) }
        val matchedChar = snapshot.characters.firstOrNull { it.characterCode.equals(code, ignoreCase = true) }

        if (matchedObj == null && matchedChar == null) {
            _lastScanResult.value = ScanResultUi(
                code = code,
                foundObject = null,
                foundCharacter = null,
                addedToInventory = false,
                message = "Kode '$code' belum terdaftar di Barcode Registry."
            )
            _statusBanner.value = "Objek $code tidak ditemukan di database."
            return
        }

        val activeWorld = snapshot.activeWorld
        val currentInv = activeWorld?.sessionInventory ?: emptyList()
        val alreadyRegistered = currentInv.any { it.equals(code, ignoreCase = true) }
        val shouldAdd = matchedObj != null && !alreadyRegistered

        val resolvedName = matchedObj?.objectName ?: matchedChar?.name ?: code
        val note = when {
            matchedChar != null && matchedObj == null -> "Identitas karakter ${matchedChar.name} terverifikasi pada sesi ${snapshot.activeClass?.code ?: snapshot.selectedClassId}."
            alreadyRegistered -> "Objek sudah ada di inventory sesi — duplikasi dicegah otomatis."
            else -> "Objek ditambahkan ke inventory sesi virtual & status fisik diperbarui."
        }

        _lastScanResult.value = ScanResultUi(
            code = code,
            foundObject = matchedObj,
            foundCharacter = matchedChar,
            addedToInventory = shouldAdd,
            message = note
        )

        viewModelScope.launch {
            repository.recordScanEvent(
                barcodeCode = code,
                objectName = resolvedName,
                barcodeDocId = matchedObj?.id,
                classId = snapshot.selectedClassId,
                studentName = studentName.ifBlank { "Siswa Praktikum" },
                sessionName = sessionName.ifBlank { snapshot.activeClass?.activeSessionMission ?: "Sesi PID" },
                addToSessionInventory = shouldAdd,
                scanNote = note,
                worldDocId = activeWorld?.id,
                currentInventory = currentInv
            ).onFailure {
                _statusBanner.value = it.localizedMessage ?: "Gagal mencatat riwayat scan."
            }
        }
    }

    fun toggleDioramaModule(
        module: DioramaModule,
        snapshot: NetCitySnapshot,
        verifiedByTeacher: Boolean = module.verifiedByTeacher
    ) {
        val nextStatus = if (module.moduleStatus == "DIGUNAKAN") "TERSEDIA" else "DIGUNAKAN"
        val classId = if (nextStatus == "DIGUNAKAN") snapshot.selectedClassId else ""
        val session = if (nextStatus == "DIGUNAKAN") {
            snapshot.activeClass?.activeSessionMission ?: "Praktik ${snapshot.activeClass?.code ?: ""}"
        } else ""
        viewModelScope.launch {
            repository.updateDioramaModuleStatus(module.id, nextStatus, classId, session, verifiedByTeacher)
                .onSuccess { _statusBanner.value = "${module.name}: status diubah menjadi $nextStatus." }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Gagal memperbarui modul diorama." }
        }
    }

    fun setDioramaTeacherVerification(module: DioramaModule, verified: Boolean) {
        viewModelScope.launch {
            repository.updateDioramaModuleStatus(
                module.id,
                module.moduleStatus,
                module.activeClassId,
                module.activeSessionLabel,
                verified
            ).onSuccess {
                _statusBanner.value = if (verified) {
                    "Verifikasi fisik guru: ${module.name} dinyatakan SESUAI."
                } else {
                    "Verifikasi fisik ${module.name} dibatalkan."
                }
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "Gagal menyimpan verifikasi guru."
            }
        }
    }

    // --- GAME ENGINE ACTIONS ---
    fun placeCityNode(world: CityWorld, spec: BuildableSpec, gridX: Int, gridY: Int) {
        val nodes = world.parsedNodes().toMutableList()
        if (nodes.any { it.gridX == gridX && it.gridY == gridY }) {
            _statusBanner.value = "Tile ($gridX, $gridY) sudah ditempati objek lain."
            return
        }
        if (world.coins < spec.price) {
            _statusBanner.value = "Saldo virtual tidak cukup! Butuh NC$${spec.price}."
            return
        }
        val newNode = CityNode(
            nodeId = "n_${System.currentTimeMillis() % 100000}",
            type = spec.type,
            label = "${spec.label.take(12)} ${nodes.count { it.type == spec.type } + 1}",
            gridX = gridX,
            gridY = gridY,
            barcodeCode = "${spec.defaultBarcodePrefix}-001"
        )
        nodes.add(newNode)
        viewModelScope.launch {
            repository.updateCityWorld(
                worldId = world.id,
                activeLevel = world.activeLevel.toInt(),
                coins = world.coins - spec.price,
                xp = world.xp + 25L,
                networkHealth = world.networkHealth.toInt(),
                activeFault = world.activeFault,
                stemPhase = StemPhase.fromRaw(world.stemPhase),
                completedLevels = world.completedLevels,
                nodes = nodes,
                links = world.parsedLinks(),
                sessionInventory = world.sessionInventory
            ).onSuccess {
                _statusBanner.value = "${spec.label} dibangun di ($gridX,$gridY) (+25 XP)."
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "Gagal membangun objek."
            }
        }
    }

    fun removeCityNode(world: CityWorld, nodeId: String) {
        val remainingNodes = world.parsedNodes().filterNot { it.nodeId == nodeId }
        val remainingLinks = world.parsedLinks().filterNot { it.fromId == nodeId || it.toId == nodeId }
        viewModelScope.launch {
            repository.updateCityWorld(
                worldId = world.id,
                activeLevel = world.activeLevel.toInt(),
                coins = world.coins + 80L,
                xp = world.xp,
                networkHealth = world.networkHealth.toInt(),
                activeFault = world.activeFault,
                stemPhase = StemPhase.fromRaw(world.stemPhase),
                completedLevels = world.completedLevels,
                nodes = remainingNodes,
                links = remainingLinks,
                sessionInventory = world.sessionInventory
            ).onSuccess { _statusBanner.value = "Objek dihapus (+NC$80 pengembalian)." }
        }
    }

    fun connectCityNodes(world: CityWorld, fromId: String, toId: String, cableType: String) {
        if (fromId == toId) return
        val links = world.parsedLinks().toMutableList()
        val exists = links.any {
            (it.fromId == fromId && it.toId == toId) || (it.fromId == toId && it.toId == fromId)
        }
        if (exists) {
            _statusBanner.value = "Koneksi kabel antar kedua perangkat sudah ada."
            return
        }
        val cost = if (cableType == "FIBER") 120L else 40L
        if (world.coins < cost) {
            _statusBanner.value = "Saldo kurang untuk menarik kabel $cableType (butuh NC$$cost)."
            return
        }
        links.add(CableLink(fromId, toId, cableType))
        val newHealth = (world.networkHealth + 2).coerceAtMost(100).toInt()
        viewModelScope.launch {
            repository.updateCityWorld(
                worldId = world.id,
                activeLevel = world.activeLevel.toInt(),
                coins = world.coins - cost,
                xp = world.xp + 30L,
                networkHealth = newHealth,
                activeFault = world.activeFault,
                stemPhase = StemPhase.fromRaw(world.stemPhase),
                completedLevels = world.completedLevels,
                nodes = world.parsedNodes(),
                links = links,
                sessionInventory = world.sessionInventory
            ).onSuccess {
                _statusBanner.value = "Jalur kabel $cableType terpasang (+30 XP)."
            }
        }
    }

    fun setCityLevelAndPhase(world: CityWorld, levelNumber: Int, phase: StemPhase) {
        viewModelScope.launch {
            repository.updateCityWorld(
                worldId = world.id,
                activeLevel = levelNumber.coerceIn(1, 10),
                coins = world.coins,
                xp = world.xp,
                networkHealth = world.networkHealth.toInt(),
                activeFault = world.activeFault,
                stemPhase = phase,
                completedLevels = world.completedLevels,
                nodes = world.parsedNodes(),
                links = world.parsedLinks(),
                sessionInventory = world.sessionInventory
            )
        }
    }

    fun triggerOrFixFault(world: CityWorld, levelSpec: LearningLevelSpec, fix: Boolean) {
        val nextFault = if (fix) "" else levelSpec.faultScenario
        val nextHealth = if (fix) 100 else 62
        val bonusXp = if (fix && world.activeFault.isNotBlank()) 80L else 0L
        viewModelScope.launch {
            repository.updateCityWorld(
                worldId = world.id,
                activeLevel = world.activeLevel.toInt(),
                coins = world.coins + if (fix && world.activeFault.isNotBlank()) 150L else 0L,
                xp = world.xp + bonusXp,
                networkHealth = nextHealth,
                activeFault = nextFault,
                stemPhase = StemPhase.fromRaw(world.stemPhase),
                completedLevels = world.completedLevels,
                nodes = world.parsedNodes(),
                links = world.parsedLinks(),
                sessionInventory = world.sessionInventory
            ).onSuccess {
                _statusBanner.value = if (fix) {
                    "Gangguan berhasil diatasi! Network Health 100% (+80 XP, +NC$150)."
                } else {
                    "Simulasi Gangguan Aktif: ${levelSpec.faultScenario}"
                }
            }
        }
    }

    fun completeCurrentLevelMission(world: CityWorld, levelSpec: LearningLevelSpec, classRoom: ClassRoom?) {
        val nodes = world.parsedNodes()
        val links = world.parsedLinks()
        val missingTypes = levelSpec.requiredNodeTypes.filter { req -> nodes.none { it.type == req } }
        if (missingTypes.isNotEmpty()) {
            _statusBanner.value = "Belum lengkap! Bangun terlebih dahulu: ${missingTypes.joinToString(", ")}."
            return
        }
        if (links.size < levelSpec.requiredLinksCount) {
            _statusBanner.value = "Butuh minimal ${levelSpec.requiredLinksCount} jalur kabel aktif (saat ini ${links.size})."
            return
        }
        if (world.activeFault.isNotBlank()) {
            _statusBanner.value = "Selesaikan simulasi gangguan jaringan terlebih dahulu sebelum menyelesaikan misi!"
            return
        }

        val completed = (world.completedLevels + levelSpec.number.toLong()).distinct().sorted()
        val nextLevel = (levelSpec.number + 1).coerceAtMost(10)
        viewModelScope.launch {
            repository.updateCityWorld(
                worldId = world.id,
                activeLevel = nextLevel,
                coins = world.coins + levelSpec.rewardCoins,
                xp = world.xp + levelSpec.rewardXp,
                networkHealth = 100,
                activeFault = "",
                stemPhase = StemPhase.EXPLORE,
                completedLevels = completed,
                nodes = nodes,
                links = links,
                sessionInventory = world.sessionInventory
            ).onSuccess {
                if (classRoom != null && nextLevel > classRoom.maxUnlockedLevel) {
                    repository.saveClassRoom(
                        existingId = classRoom.id,
                        code = classRoom.code,
                        name = classRoom.name,
                        teacherName = classRoom.teacherName,
                        maxUnlockedLevel = nextLevel,
                        activeSessionMission = "Level $nextLevel - ${CurriculumCatalog.levels[nextLevel - 1].title}",
                        labGroups = classRoom.labGroups
                    )
                }
                _statusBanner.value = "Misi Level ${levelSpec.number} (${levelSpec.title}) SELESAI! +NC$${levelSpec.rewardCoins}, +${levelSpec.rewardXp} XP!"
            }
        }
    }

    // --- ASSESSMENTS ---
    fun saveAssessment(
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
    ) {
        viewModelScope.launch {
            repository.saveAssessment(
                existingId, studentName, classId, groupName, levelNumber, missionTitle,
                durationMinutes, technicalScore, designScore, physicalScore, troubleshootingScore,
                gameResultSummary, dioramaVerified, teacherNote, status
            )
                .onSuccess { _statusBanner.value = "Penilaian untuk $studentName tersimpan ($status)." }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Gagal menyimpan penilaian (mungkin sudah FINALIZED)." }
        }
    }

    private data class CorePartA(
        val profile: UserProfile?,
        val classes: List<ClassRoom>,
        val members: List<SchoolMember>,
        val characters: List<NetCityCharacter>,
        val barcodes: List<BarcodeObject>
    )

    private data class CorePartB(
        val diorama: List<DioramaModule>,
        val scans: List<ScanLog>,
        val worlds: List<CityWorld>,
        val assessments: List<AssessmentRecord>,
        val admins: List<AdminRecord>
    )

    private companion object {
        const val TAG = "NetCityVM"
    }
}
