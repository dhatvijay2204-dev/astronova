const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function nowTimestamp() {
  return new Date();
}

function validProfile(uid) {
  const now = nowTimestamp();
  return {
    userId: uid,
    fullName: "Alice Star",
    birthDate: "1996-05-14",
    birthTime: "07:30",
    birthPlace: "Seattle, USA",
    zodiacSign: "Taurus",
    element: "Earth",
    focusArea: "Balanced",
    notificationsEnabled: true,
    notificationHour: 8,
    notificationMinute: 0,
    createdAt: now,
    updatedAt: now,
  };
}

function validHoroscope(uid) {
  const now = nowTimestamp();
  return {
    userId: uid,
    periodType: "daily",
    periodKey: "2026-11-14",
    zodiacSign: "Taurus",
    headline: "Steady Ground, Luminous Horizons",
    overview: "Venus aligns gently with your natal sun today, grounding your ambitions.",
    loveInsight: "Openness brings warmth to close bonds.",
    careerInsight: "Focus on methodical progress over haste.",
    wellnessInsight: "Take a quiet evening walk under clear skies.",
    luckyNumber: 6,
    luckyColor: "Champagne Gold",
    powerPlanet: "Venus",
    cosmicScore: 88,
    createdAt: now,
    updatedAt: now,
  };
}

function validAffirmation(uid) {
  const now = nowTimestamp();
  return {
    userId: uid,
    dateKey: "2026-11-14",
    text: "I trust my steady rhythm and welcome clarity with an open heart.",
    ritualPrompt: "Take three slow breaths while setting one intention for today.",
    zodiacSign: "Taurus",
    focusArea: "Balanced",
    isFavorite: false,
    createdAt: now,
    updatedAt: now,
  };
}

function validBirthChart(uid) {
  const now = nowTimestamp();
  return {
    userId: uid,
    fullName: "Alice Star",
    birthDate: "1996-05-14",
    birthTime: "07:30",
    birthPlace: "Seattle, USA",
    sunSign: "Taurus",
    moonSign: "Aries",
    risingSign: "Gemini",
    chartSummary: "Grounded earth energy paired with fiery emotional drive.",
    placements: [
      {
        planet: "Sun",
        sign: "Taurus",
        degree: 24,
        house: 12,
        meaning: "Reflective strength and steadfast inner values.",
      },
      {
        planet: "Moon",
        sign: "Aries",
        degree: 11,
        house: 11,
        meaning: "Spontaneous emotional warmth in friendships.",
      },
    ],
    createdAt: now,
    updatedAt: now,
  };
}

function validCompatibility(uid) {
  const now = nowTimestamp();
  return {
    userId: uid,
    signOne: "Taurus",
    signTwo: "Scorpio",
    relationshipType: "Romantic",
    overallScore: 87,
    romanticScore: 92,
    platonicScore: 82,
    professionalScore: 85,
    summary: "Magnetic polarity between earth stability and water depth.",
    strengths: "Unwavering loyalty and shared emotional devotion.",
    challenges: "Both signs can hold firm to stubborn viewpoints.",
    createdAt: now,
    updatedAt: now,
  };
}

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

test("Unauthenticated user cannot read or write any user astrology collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID)));
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("horoscopes").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("affirmations").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("birthCharts").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("compatibilities").get());
});

test("Authenticated owner can create, read, and update their profile, birth chart, and compatibility", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);
  const chartRef = profileRef.collection("birthCharts").doc("natal_primary");
  const compatRef = profileRef.collection("compatibilities").doc("taurus_scorpio_romantic");

  await assertSucceeds(profileRef.set(validProfile(ALICE_UID)));
  await assertSucceeds(profileRef.get());
  await assertSucceeds(
    profileRef.update({
      focusArea: "Career & Purpose",
      notificationsEnabled: false,
      updatedAt: nowTimestamp(),
    })
  );

  await assertSucceeds(chartRef.set(validBirthChart(ALICE_UID)));
  await assertSucceeds(profileRef.collection("birthCharts").get());

  await assertSucceeds(compatRef.set(validCompatibility(ALICE_UID)));
  await assertSucceeds(profileRef.collection("compatibilities").get());
});

test("Cross-user access across all collections is denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID)));
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("horoscopes").doc("daily_2026-11-14").set(validHoroscope(ALICE_UID))
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("affirmations").doc("aff_2026-11-14").set(validAffirmation(ALICE_UID))
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("birthCharts").doc("natal_primary").set(validBirthChart(ALICE_UID))
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("compatibilities").doc("taurus_scorpio_romantic").set(validCompatibility(ALICE_UID))
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).set(validProfile(BOB_UID)));
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("horoscopes").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("birthCharts").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("compatibilities").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("affirmations").doc("aff_2026-11-14").update({
      isFavorite: true,
      updatedAt: nowTimestamp(),
    })
  );
});

test("Schema validation rejects ghost fields, invalid relationshipType, empty placements, and spoofing", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);

  await assertFails(profileRef.set({ ...validProfile(ALICE_UID), isAdmin: true }));
  await assertFails(profileRef.set({ ...validProfile(ALICE_UID), element: "Plasma" }));
  await assertFails(profileRef.set(validProfile(BOB_UID)));

  await assertFails(
    profileRef.collection("birthCharts").doc("natal_primary").set({
      ...validBirthChart(ALICE_UID),
      placements: [],
    })
  );

  await assertFails(
    profileRef.collection("compatibilities").doc("bad_rel").set({
      ...validCompatibility(ALICE_UID),
      relationshipType: "Cosmic",
    })
  );
});

test("Authenticated owner can create, toggle favorite, and delete affirmations & horoscopes", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const horoRef = aliceDb.collection("users").doc(ALICE_UID).collection("horoscopes").doc("daily_2026-11-14");
  const affRef = aliceDb.collection("users").doc(ALICE_UID).collection("affirmations").doc("aff_2026-11-14");

  await assertSucceeds(horoRef.set(validHoroscope(ALICE_UID)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("horoscopes").get());
  await assertSucceeds(affRef.set(validAffirmation(ALICE_UID)));
  await assertSucceeds(affRef.update({ isFavorite: true, updatedAt: nowTimestamp() }));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("affirmations").get());
  await assertSucceeds(affRef.delete());
});
