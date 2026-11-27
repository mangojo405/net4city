const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_tkj_01";
const BOB_UID = "bob_tkj_02";
const ADMIN_UID = "admin_netcity_01";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function nowPast(msAgo = 5000) {
  return new Date(Date.now() - msAgo);
}

test("Unauthenticated user: denied across all NETCITY collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("classes").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("members").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("barcodes").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("assessments").get()
  );
});

test("UserProfile: owner can create & update; rejects shadow fields & future timestamps", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = nowPast(10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Budi Teknisi",
      workspacePersona: "ADMIN",
      selectedClassId: "x_tkj_1",
      selectedStudentId: "siswa_01",
      demoSeeded: false,
      coins: 5000,
      xp: 0,
      unlockedLevel: 1,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Partial update of mutable fields succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      workspacePersona: "GURU",
      coins: 5500,
      updatedAt: nowPast(1000),
    })
  );

  // Reject immutable createdAt mutation
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: nowPast(500),
      updatedAt: nowPast(500),
    })
  );

  // Reject ghost/shadow field (isAdmin)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: nowPast(500),
    })
  );

  // Reject future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      coins: 6000,
      updatedAt: new Date(Date.now() + 60000),
    })
  );
});

test("Multi-user isolation: Bob cannot read or write Alice's classes, members, or barcodes", async () => {
  const ts = nowPast(10000);
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db
      .collection("users")
      .doc(ALICE_UID)
      .collection("classes")
      .doc("x_tkj_1")
      .set({
        userId: ALICE_UID,
        code: "X TKJ 1",
        name: "Teknik Jaringan Komputer 1",
        teacherName: "Pak Hendra",
        maxUnlockedLevel: 3,
        activeSessionMission: "Level 1 - Merakit PC",
        labGroups: ["Kelompok A", "Kelompok B"],
        demoData: true,
        createdAt: ts,
        updatedAt: ts,
      });
    await db
      .collection("users")
      .doc(ALICE_UID)
      .collection("members")
      .doc("siswa_1")
      .set({
        userId: ALICE_UID,
        memberType: "SISWA",
        fullName: "Raka Pratama",
        identityNumber: "NIS-2026-001",
        classId: "x_tkj_1",
        assignedClasses: [],
        groupName: "Kelompok A",
        xp: 120,
        level: 2,
        characterId: "NC-CHAR-00001",
        demoData: true,
        createdAt: ts,
        updatedAt: ts,
      });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("classes").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("classes")
      .doc("x_tkj_1")
      .get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("members").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("members")
      .doc("siswa_1")
      .update({ xp: 99999, updatedAt: nowPast(1000) })
  );
});

test("Classes, Members, Characters, Barcodes, Diorama, ScanLogs, CityWorld: Kotlin-aligned CRUD succeeds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = nowPast(10000);

  // ClassRoom
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("classes")
      .doc("x_tkj_1")
      .set({
        userId: ALICE_UID,
        code: "X TKJ 1",
        name: "Kelas X TKJ 1",
        teacherName: "Pak Hendra",
        maxUnlockedLevel: 2,
        activeSessionMission: "Level 1",
        labGroups: ["Kelompok Alpha"],
        demoData: true,
        createdAt: ts,
        updatedAt: ts,
      })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("classes").get()
  );

  // Reject writing @DocumentId `id` inside payload
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("classes")
      .doc("x_tkj_2")
      .set({
        id: "x_tkj_2",
        userId: ALICE_UID,
        code: "X TKJ 2",
        name: "Kelas X TKJ 2",
        teacherName: "Bu Sari",
        maxUnlockedLevel: 1,
        activeSessionMission: "",
        labGroups: [],
        demoData: true,
        createdAt: ts,
        updatedAt: ts,
      })
  );

  // BarcodeObject
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("barcodes")
      .doc("NC-RTR-001")
      .set({
        userId: ALICE_UID,
        barcodeCode: "NC-RTR-001",
        objectName: "Router Mikrotik RB750Gr3",
        category: "Router",
        functionDesc: "Menghubungkan jaringan berbeda dan routing paket IP.",
        specs: "5x Gigabit Ethernet, 256MB RAM, Dual Core 880MHz",
        portCount: 5,
        virtualPrice: 850,
        installNeeds: "Adaptor 24V, Kabel UTP Cat6",
        relatedLevel: 7,
        storageLocation: "Rak Lab B2",
        inventoryStatus: "TERSEDIA",
        activeClassId: "x_tkj_1",
        activeStudentName: "Raka Pratama",
        demoData: true,
        createdAt: ts,
        updatedAt: ts,
      })
  );

  // Immutable barcodeCode guard
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("barcodes")
      .doc("NC-RTR-001")
      .update({
        barcodeCode: "NC-RTR-999",
        updatedAt: nowPast(1000),
      })
  );

  // ScanLog append-only
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("scan_logs")
      .doc("scan_001")
      .set({
        userId: ALICE_UID,
        barcodeCode: "NC-RTR-001",
        objectName: "Router Mikrotik RB750Gr3",
        classId: "x_tkj_1",
        studentName: "Raka Pratama",
        sessionName: "Sesi Praktikum 1",
        addedToInventory: true,
        scanNote: "Berhasil ditambahkan ke inventory sesi",
        createdAt: ts,
      })
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("scan_logs")
      .doc("scan_001")
      .update({ scanNote: "Diubah" })
  );

  // CityWorld
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("city_worlds")
      .doc("world_x_tkj_1")
      .set({
        userId: ALICE_UID,
        classId: "x_tkj_1",
        activeLevel: 1,
        coins: 5000,
        xp: 250,
        networkHealth: 95,
        activeFault: "",
        stemPhase: "BUILD",
        completedLevels: [1],
        placedNodes: [{ nodeId: "n1", type: "SEKOLAH", gridX: 2, gridY: 2 }],
        cableLinks: [{ fromId: "n1", toId: "n2", cableType: "UTP" }],
        sessionInventory: ["NC-RTR-001"],
        createdAt: ts,
        updatedAt: ts,
      })
  );
});

test("Assessments: terminal state locking on FINALIZED blocks non-admin mutation; verified admin overrides", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = nowPast(10000);

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("assessments")
      .doc("assess_01")
      .set({
        userId: ALICE_UID,
        studentName: "Raka Pratama",
        classId: "x_tkj_1",
        groupName: "Kelompok A",
        levelNumber: 1,
        missionTitle: "Merakit Komputer Lab",
        durationMinutes: 45,
        technicalScore: 88,
        designScore: 85,
        physicalScore: 90,
        troubleshootingScore: 84,
        finalScore: 87,
        gameResultSummary: "4/4 PC terpasang sempurna",
        dioramaVerified: true,
        teacherNote: "Sangat baik dan rapi",
        status: "DRAFT",
        demoData: true,
        createdAt: ts,
        updatedAt: ts,
      })
  );

  // Finalize from DRAFT succeeds
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("assessments")
      .doc("assess_01")
      .update({
        status: "FINALIZED",
        updatedAt: nowPast(2000),
      })
  );

  // Subsequent edit on FINALIZED by regular owner fails (terminal state lock)
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("assessments")
      .doc("assess_01")
      .update({
        finalScore: 100,
        updatedAt: nowPast(1000),
      })
  );

  // Spoofed admin email (email_verified: false) MUST FAIL
  const spoofAdminDb = testEnv
    .authenticatedContext("spoof_uid", {
      email: "dabojo.1487@gmail.com",
      email_verified: false,
    })
    .firestore();
  await assertFails(
    spoofAdminDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("assessments")
      .doc("assess_01")
      .update({
        finalScore: 99,
        updatedAt: nowPast(1000),
      })
  );

  // Verified bootstrapped admin succeeds
  const verifiedAdminDb = testEnv
    .authenticatedContext(ADMIN_UID, {
      email: "dabojo.1487@gmail.com",
      email_verified: true,
    })
    .firestore();
  await assertSucceeds(
    verifiedAdminDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("assessments")
      .doc("assess_01")
      .update({
        finalScore: 92,
        teacherNote: "Revisi admin terverifikasi",
        updatedAt: nowPast(500),
      })
  );
});

test("Admin database (/admins): Dabojo (verified dabojo.1487@gmail.com) can register & list; regular users denied", async () => {
  const ts = nowPast(5000);
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const dabojoDb = testEnv
    .authenticatedContext(ADMIN_UID, {
      email: "dabojo.1487@gmail.com",
      email_verified: true,
    })
    .firestore();

  // Regular user cannot create or list /admins
  await assertFails(
    aliceDb.collection("admins").doc(ALICE_UID).set({
      displayName: "Hacker",
      email: "hacker@example.com",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertFails(aliceDb.collection("admins").get());

  // Verified Dabojo can register and list /admins
  await assertSucceeds(
    dabojoDb.collection("admins").doc(ADMIN_UID).set({
      displayName: "Dabojo",
      email: "dabojo.1487@gmail.com",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(dabojoDb.collection("admins").get());
  await assertSucceeds(
    dabojoDb.collection("admins").doc(ADMIN_UID).update({
      displayName: "Dabojo",
      email: "dabojo.1487@gmail.com",
      updatedAt: nowPast(1000),
    })
  );
});
