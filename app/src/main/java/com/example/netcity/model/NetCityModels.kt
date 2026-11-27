package com.example.netcity.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class WorkspacePersona(val label: String, val subtitle: String) {
    ADMIN("Administrator", "Akses Penuh Sistem & Data Sekolah"),
    GURU("Guru TKJ", "Kelola Kelas, Sesi Praktik & Penilaian"),
    SISWA("Siswa SMK", "Bangun Kota, Karakter, Misi & Scan Diorama");

    companion object {
        fun fromRaw(raw: String?): WorkspacePersona =
            entries.firstOrNull { it.name == raw } ?: ADMIN
    }
}

enum class StemPhase(val title: String, val badge: String, val guidance: String) {
    EXPLORE("1. EXPLORE", "Identifikasi Masalah", "Amati kebutuhan jaringan kota, jumlah perangkat, dan kendala topologi."),
    DESIGN("2. DESIGN", "Rancang Arsitektur", "Hitung anggaran, panjang kabel, IP/subnet, atau link budget sebelum membangun."),
    BUILD("3. BUILD", "Bangun Virtual & Diorama", "Tempatkan perangkat di peta 2.5D, hubungkan kabel, dan susun modul diorama fisik."),
    TEST("4. TEST", "Uji & Evaluasi", "Uji konektivitas, selesaikan gangguan simulasi, dan verifikasi bersama guru.");

    companion object {
        fun fromRaw(raw: String?): StemPhase =
            entries.firstOrNull { it.name == raw } ?: EXPLORE
    }
}

@Keep
@IgnoreExtraProperties
data class AdminRecord(
    @DocumentId val id: String = "",
    val displayName: String = "",
    val email: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val workspacePersona: String = WorkspacePersona.ADMIN.name,
    val selectedClassId: String = "x_tkj_1",
    val selectedStudentId: String = "",
    val demoSeeded: Boolean = false,
    val coins: Long = 5000L,
    val xp: Long = 0L,
    val unlockedLevel: Long = 1L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ClassRoom(
    @DocumentId val id: String = "",
    val userId: String = "",
    val code: String = "",
    val name: String = "",
    val teacherName: String = "",
    val maxUnlockedLevel: Long = 1L,
    val activeSessionMission: String = "",
    val labGroups: List<String> = emptyList(),
    val demoData: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SchoolMember(
    @DocumentId val id: String = "",
    val userId: String = "",
    val memberType: String = "SISWA",
    val fullName: String = "",
    val identityNumber: String = "",
    val classId: String = "",
    val assignedClasses: List<String> = emptyList(),
    val groupName: String = "",
    val xp: Long = 0L,
    val level: Long = 1L,
    val characterId: String = "",
    val demoData: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class NetCityCharacter(
    @DocumentId val id: String = "",
    val userId: String = "",
    val characterCode: String = "",
    val name: String = "",
    val ownerName: String = "",
    val classId: String = "",
    val profession: String = "Computer Technician",
    val faceShape: String = "Bulat",
    val hairStyle: String = "Pendek Rapi",
    val hairColor: String = "Hitam",
    val outfitStyle: String = "Seragam Teknisi Biru",
    val pantsColor: String = "Navy",
    val shoeStyle: String = "Safety Boots",
    val accessory: String = "Kacamata Anti-Radiasi",
    val workTool: String = "Tang Crimping RJ45",
    val level: Long = 1L,
    val xp: Long = 0L,
    val skills: List<String> = emptyList(),
    val demoData: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class BarcodeObject(
    @DocumentId val id: String = "",
    val userId: String = "",
    val barcodeCode: String = "",
    val objectName: String = "",
    val category: String = "Perangkat",
    val functionDesc: String = "",
    val specs: String = "",
    val portCount: Long = 0L,
    val virtualPrice: Long = 0L,
    val installNeeds: String = "",
    val relatedLevel: Long = 1L,
    val storageLocation: String = "",
    val inventoryStatus: String = "TERSEDIA",
    val activeClassId: String = "",
    val activeStudentName: String = "",
    val demoData: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DioramaModule(
    @DocumentId val id: String = "",
    val userId: String = "",
    val moduleCode: String = "",
    val name: String = "",
    val category: String = "",
    val barcodeCode: String = "",
    val description: String = "",
    val storageLocation: String = "",
    val moduleStatus: String = "TERSEDIA",
    val requiredLevels: List<Long> = emptyList(),
    val activeClassId: String = "",
    val activeSessionLabel: String = "",
    val verifiedByTeacher: Boolean = false,
    val demoData: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ScanLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val barcodeCode: String = "",
    val objectName: String = "",
    val classId: String = "",
    val studentName: String = "",
    val sessionName: String = "",
    val addedToInventory: Boolean = false,
    val scanNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class CityNode(
    val nodeId: String,
    val type: String,
    val label: String,
    val gridX: Int,
    val gridY: Int,
    val barcodeCode: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "nodeId" to nodeId,
        "type" to type,
        "label" to label,
        "gridX" to gridX,
        "gridY" to gridY,
        "barcodeCode" to barcodeCode
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): CityNode = CityNode(
            nodeId = map["nodeId"] as? String ?: "",
            type = map["type"] as? String ?: "PC",
            label = map["label"] as? String ?: "Perangkat",
            gridX = (map["gridX"] as? Number)?.toInt() ?: 0,
            gridY = (map["gridY"] as? Number)?.toInt() ?: 0,
            barcodeCode = map["barcodeCode"] as? String ?: ""
        )
    }
}

data class CableLink(
    val fromId: String,
    val toId: String,
    val cableType: String // "UTP" or "FIBER"
) {
    fun toMap(): Map<String, Any> = mapOf(
        "fromId" to fromId,
        "toId" to toId,
        "cableType" to cableType
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): CableLink = CableLink(
            fromId = map["fromId"] as? String ?: "",
            toId = map["toId"] as? String ?: "",
            cableType = map["cableType"] as? String ?: "UTP"
        )
    }
}

@Keep
@IgnoreExtraProperties
data class CityWorld(
    @DocumentId val id: String = "",
    val userId: String = "",
    val classId: String = "x_tkj_1",
    val activeLevel: Long = 1L,
    val coins: Long = 5000L,
    val xp: Long = 0L,
    val networkHealth: Long = 100L,
    val activeFault: String = "",
    val stemPhase: String = StemPhase.EXPLORE.name,
    val completedLevels: List<Long> = emptyList(),
    val placedNodes: List<Map<String, Any>> = emptyList(),
    val cableLinks: List<Map<String, Any>> = emptyList(),
    val sessionInventory: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun parsedNodes(): List<CityNode> = placedNodes.map { CityNode.fromMap(it) }
    fun parsedLinks(): List<CableLink> = cableLinks.map { CableLink.fromMap(it) }
}

@Keep
@IgnoreExtraProperties
data class AssessmentRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val studentName: String = "",
    val classId: String = "x_tkj_1",
    val groupName: String = "",
    val levelNumber: Long = 1L,
    val missionTitle: String = "",
    val durationMinutes: Long = 45L,
    val technicalScore: Long = 80L,
    val designScore: Long = 80L,
    val physicalScore: Long = 80L,
    val troubleshootingScore: Long = 80L,
    val finalScore: Long = 80L,
    val gameResultSummary: String = "",
    val dioramaVerified: Boolean = false,
    val teacherNote: String = "",
    val status: String = "DRAFT",
    val demoData: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class LearningLevelSpec(
    val number: Int,
    val title: String,
    val subtitle: String,
    val story: String,
    val learningObjectives: List<String>,
    val topics: List<String>,
    val mainMission: String,
    val challenge: String,
    val virtualPractice: String,
    val dioramaPractice: String,
    val rewardCoins: Int,
    val rewardXp: Int,
    val requiredNodeTypes: List<String>,
    val requiredLinksCount: Int,
    val stemScience: String,
    val stemTechnology: String,
    val stemEngineering: String,
    val stemMathPrompt: String,
    val stemMathOptions: List<String>,
    val stemMathCorrectIndex: Int,
    val faultScenario: String
)

data class BuildableSpec(
    val type: String,
    val label: String,
    val category: String,
    val price: Int,
    val ports: Int,
    val functionDesc: String,
    val specs: String,
    val installNeeds: String,
    val minLevel: Int,
    val defaultBarcodePrefix: String
)
