const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
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

test("Unauthenticated user: cannot read user documents", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Unauthenticated user: cannot read transactions subcollection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("transactions").get());
});

test("Authenticated user: cannot read another user's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      uid: BOB_UID,
      email: "bob@example.com",
      displayName: "Bob",
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});

test("Authenticated user: cannot read another user's transactions", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("transactions").doc("1").set({
      id: 1,
      type: "IN",
      amount: 100,
      currency: "USD",
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("transactions").doc("1").get());
});

test("Authenticated user: can create their own profile with valid schema", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      uid: ALICE_UID,
      email: "alice@example.com",
      displayName: "Alice",
      startingCapital: 50000,
      capitalCurrency: "USD",
      displayCurrency: "USD",
    })
  );
});

test("Authenticated user: cannot create profile with spoofed uid", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      uid: BOB_UID,
      email: "alice@example.com",
      displayName: "Alice",
    })
  );
});

test("Authenticated user: can create and read their own transaction", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const txRef = aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("101");

  await assertSucceeds(
    txRef.set({
      id: 101,
      type: "IN",
      amount: 2500,
      currency: "USD",
      category: "Salary",
      dateMillis: 1730000000000,
    })
  );

  await assertSucceeds(txRef.get());
});

test("Authenticated user: cannot create transaction with invalid schema (missing required fields)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const txRef = aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("102");

  await assertFails(
    txRef.set({
      id: 102,
      // missing type, amount, currency
      notes: "Incomplete transaction",
    })
  );
});

test("Authenticated user: can manage investments", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const invRef = aliceDb.collection("users").doc(ALICE_UID).collection("investments").doc("201");

  await assertSucceeds(
    invRef.set({
      id: 201,
      name: "BMW 520i",
      category: "Cars",
      initialCapital: 32000,
      currentValue: 34500,
      currency: "USD",
    })
  );

  await assertSucceeds(invRef.get());
});

test("Authenticated user: can manage debts and payments", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const debtRef = aliceDb.collection("users").doc(ALICE_UID).collection("debts_credits").doc("301");
  const payRef = aliceDb.collection("users").doc(ALICE_UID).collection("debt_payments").doc("401");

  await assertSucceeds(
    debtRef.set({
      id: 301,
      type: "CREDIT",
      personName: "Karwan",
      totalAmount: 5000,
      amountPaid: 1000,
      currency: "USD",
    })
  );

  await assertSucceeds(
    payRef.set({
      id: 401,
      debtId: 301,
      amount: 1000,
      dateMillis: 1730000000000,
    })
  );

  await assertSucceeds(debtRef.get());
  await assertSucceeds(payRef.get());
});
