// Bare-bones browser version of the app: join an existing room, pick a show, give points and
// see the other members' points live. Talks to the same Firestore data as the Android app
// (see FirestoreRepository.kt), so firestore.rules apply unchanged.

import { initializeApp } from "https://www.gstatic.com/firebasejs/13.0.0/firebase-app.js";
import { getAuth, signInAnonymously, onAuthStateChanged } from "https://www.gstatic.com/firebasejs/13.0.0/firebase-auth.js";
import {
  getFirestore, collection, doc, getDoc, onSnapshot, runTransaction,
  setDoc, updateDoc, deleteDoc, serverTimestamp, Timestamp,
} from "https://www.gstatic.com/firebasejs/13.0.0/firebase-firestore.js";
import { firebaseConfig } from "./firebase-config.js";
import { STRINGS, countryFlag, translateCountry } from "./i18n.js";

// Windows doesn't draw flag emoji; this loads a flag-only font there. Optional, so a failed load is ignored.
import("https://cdn.jsdelivr.net/npm/country-flag-emoji-polyfill@0.1.10/dist/index.mjs")
  .then(m => m.polyfillCountryFlagEmojis())
  .catch(() => {});

const SHOW_IDS = ["semi1", "semi2", "final"];
const HEARTBEAT_MS = 3 * 60 * 1000;   // same cadence as the app
const ONLINE_MS = 5 * 60 * 1000;      // a member counts as online if their heartbeat is newer than this
const DAY_MS = 24 * 60 * 60 * 1000;

// localStorage can be unavailable (private mode, blocked site data); the page works without it.
const store = {
  get(key) { try { return localStorage.getItem("est." + key); } catch { return null; } },
  set(key, value) {
    try {
      if (value == null) localStorage.removeItem("est." + key);
      else localStorage.setItem("est." + key, value);
    } catch { /* not persisted */ }
  },
};

const state = {
  lang: store.get("lang") ?? (navigator.language?.toLowerCase().startsWith("fi") ? "fi" : "en"),
  phase: "connecting",        // connecting | join | room | error
  errorKey: null,
  uid: null,
  roomCode: null,
  username: null,
  showId: null,
  shows: {},                  // showId → participants sorted by order
  year: null,
  members: {},                // uid → username
  presence: {},               // uid → lastSeenAt millis
  votes: {},                  // order → { uid → points }
  joining: false,
  joinError: null,
  notice: null,
};

let auth, db;
let showsWatched = false;
let roomUnsubs = [];
let votesUnsub = null;
let votesLoaded = false;
let heartbeatTimer = null;
let pickerOrder = null;

const t = () => STRINGS[state.lang];
const $ = sel => document.querySelector(sel);
const esc = s => String(s).replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[c]);

// ---------------------------------------------------------------------------
// Startup and auth
// ---------------------------------------------------------------------------

function start() {
  if (!firebaseConfig.apiKey) {
    showError("configMissing");
    return;
  }
  const app = initializeApp(firebaseConfig);
  auth = getAuth(app);
  db = getFirestore(app);

  // Anonymous auth persists in the browser, so a returning visitor keeps the same UID (and
  // therefore their membership and votes) like an app install does.
  onAuthStateChanged(auth, user => {
    if (!user) {
      // Also covers the UID being deleted mid-session: drop the room and sign in afresh.
      if (state.uid) exitRoom();
      state.uid = null;
      signInAnonymously(auth).catch(() => showError("authFailed"));
      return;
    }
    if (state.uid === user.uid) return;
    if (state.uid) exitRoom();
    state.uid = user.uid;
    watchShows();
    restoreRoom();
  });
}

function showError(key) {
  state.phase = "error";
  state.errorKey = key;
  render();
}

function watchShows() {
  if (showsWatched) return;
  showsWatched = true;
  onSnapshot(collection(db, "shows"), snap => {
    const shows = {};
    let year = null;
    snap.forEach(d => {
      const data = d.data();
      shows[d.id] = (data.participants ?? [])
        .map(p => ({ order: p.order ?? 0, country: p.country ?? "", artist: p.artist ?? "", song: p.song ?? "" }))
        .sort((a, b) => a.order - b.order);
      if (Number.isInteger(data.year)) year = Math.max(year ?? data.year, data.year);
    });
    state.shows = shows;
    state.year = year;
    if (state.phase === "room") renderRoom();
  }, () => {});
}

// Re-enter the saved room if this browser is still a member of it.
async function restoreRoom() {
  const code = store.get("roomCode");
  if (code) {
    try {
      const member = await getDoc(doc(db, "rooms", code, "members", state.uid));
      if (member.exists()) {
        const room = await getDoc(doc(db, "rooms", code));
        enterRoom(code, member.data().username, room.data()?.lastActivityAt ?? null);
        return;
      }
      store.set("roomCode", null);
    } catch { /* offline or no access: fall back to the join form */ }
  }
  state.phase = "join";
  render();
}

// ---------------------------------------------------------------------------
// Rooms
// ---------------------------------------------------------------------------

// Same transaction as FirestoreRepository.joinRoom: claim the username lock and write the
// member doc, keeping the original joinedAt on a re-join.
async function joinRoom(code, username) {
  const uid = state.uid;
  const roomRef = doc(db, "rooms", code);
  if (!(await getDoc(roomRef)).exists()) throw new Error("notFound");
  const usernameRef = doc(roomRef, "usernames", username.toLowerCase());
  const memberRef = doc(roomRef, "members", uid);
  await runTransaction(db, async tx => {
    const usernameDoc = await tx.get(usernameRef);
    const memberDoc = await tx.get(memberRef);
    if (usernameDoc.exists() && usernameDoc.data().uid !== uid) throw new Error("taken");
    const joinedAt = memberDoc.exists() ? memberDoc.data().joinedAt : null;
    tx.set(usernameRef, { uid });
    tx.set(memberRef, { username, joinedAt: joinedAt ?? Timestamp.now() });
  });
}

function enterRoom(code, username, lastActivityAt) {
  // Restore the saved show only when coming back to the room it was picked in.
  const sameRoom = store.get("lastRoom") === code;
  state.roomCode = code;
  state.username = username;
  state.showId = sameRoom ? store.get("showId") : null;
  state.phase = "room";
  state.notice = null;
  store.set("roomCode", code);
  store.set("username", username);
  store.set("lastRoom", code);
  if (!sameRoom) store.set("showId", null);

  touchRoom(code, lastActivityAt);

  const roomRef = doc(db, "rooms", code);
  roomUnsubs = [
    onSnapshot(collection(roomRef, "members"), snap => {
      const members = {};
      snap.forEach(d => { members[d.id] = d.data().username ?? ""; });
      if (!snap.metadata.fromCache && !(state.uid in members)) return onRemoved();
      state.members = members;
      renderRoom();
    }, onRoomError),
    onSnapshot(collection(roomRef, "presence"), snap => {
      const presence = {};
      snap.forEach(d => {
        const seen = d.get("lastSeenAt", { serverTimestamps: "estimate" });
        if (seen) presence[d.id] = seen.toMillis();
      });
      state.presence = presence;
      renderMembers();
    }, () => {}),
  ];
  startHeartbeat();
  watchVotes();
  render();
}

// Keeps lastActivityAt fresh for the 90-day purge; written at most once a day.
function touchRoom(code, lastActivityAt) {
  if (lastActivityAt && Date.now() - lastActivityAt.toMillis() < DAY_MS) return;
  updateDoc(doc(db, "rooms", code), { lastActivityAt: Timestamp.now() }).catch(() => {});
}

// Being removed by the room creator revokes read access, so listeners fail with permission-denied.
function onRoomError(err) {
  if (err?.code === "permission-denied") onRemoved();
}

function onRemoved() {
  if (state.phase !== "room") return;
  exitRoom();
  state.phase = "join";
  state.notice = t().removed;
  render();
}

function leaveRoom() {
  if (state.roomCode && state.uid) {
    deleteDoc(doc(db, "rooms", state.roomCode, "presence", state.uid)).catch(() => {});
  }
  exitRoom();
  state.phase = "join";
  render();
}

function exitRoom() {
  roomUnsubs.forEach(unsub => unsub());
  roomUnsubs = [];
  votesUnsub?.();
  votesUnsub = null;
  stopHeartbeat();
  closePicker();
  Object.assign(state, { roomCode: null, username: null, showId: null, members: {}, presence: {}, votes: {} });
  store.set("roomCode", null);
}

// ---------------------------------------------------------------------------
// Presence heartbeat (only while the page is visible, like the app in the foreground)
// ---------------------------------------------------------------------------

function sendHeartbeat() {
  if (!state.roomCode || !state.uid) return;
  setDoc(doc(db, "rooms", state.roomCode, "presence", state.uid), { lastSeenAt: serverTimestamp() })
    .catch(() => {});
}

function startHeartbeat() {
  stopHeartbeat();
  if (document.visibilityState !== "visible" || state.phase !== "room") return;
  sendHeartbeat();
  heartbeatTimer = setInterval(sendHeartbeat, HEARTBEAT_MS);
}

function stopHeartbeat() {
  clearInterval(heartbeatTimer);
  heartbeatTimer = null;
}

document.addEventListener("visibilitychange", () => {
  if (state.phase !== "room") return;
  if (document.visibilityState === "visible") startHeartbeat();
  else stopHeartbeat();
});

// Online dots go stale without any snapshot arriving, so re-check them every minute.
setInterval(() => { if (state.phase === "room") renderMembers(); }, 60 * 1000);

// ---------------------------------------------------------------------------
// Shows and votes
// ---------------------------------------------------------------------------

function selectShow(showId) {
  if (state.showId === showId) return;
  state.showId = showId;
  store.set("showId", showId);
  watchVotes();
  renderRoom();
}

function watchVotes() {
  votesUnsub?.();
  votesUnsub = null;
  votesLoaded = false;
  state.votes = {};
  const { roomCode, showId } = state;
  if (!roomCode || !showId) return;
  votesUnsub = onSnapshot(collection(db, "rooms", roomCode, "votes", showId, "entries"), snap => {
    const votes = {};
    snap.forEach(d => {
      const order = Number.parseInt(d.id, 10);
      if (Number.isNaN(order)) return;
      const entry = {};
      for (const [uid, points] of Object.entries(d.data())) {
        if (Number.isInteger(points)) entry[uid] = points;
      }
      votes[order] = entry;
    });
    // Animate cells that changed (own: pop, others: flash), but not on the first load.
    const changed = new Set();
    if (votesLoaded) {
      for (const [order, entry] of Object.entries(votes)) {
        for (const [uid, points] of Object.entries(entry)) {
          if (state.votes[order]?.[uid] !== points) changed.add(`${order}:${uid}`);
        }
      }
    }
    votesLoaded = true;
    state.votes = votes;
    renderBoard(changed);
  }, onRoomError);
}

function submitVote(order, points) {
  const { roomCode, showId, uid } = state;
  if (!roomCode || !showId || !uid) return;
  // The local snapshot fires straight away, so the table updates before the server confirms.
  setDoc(doc(db, "rooms", roomCode, "votes", showId, "entries", String(order)), { [uid]: points }, { merge: true })
    .catch(() => toast(t().voteFailed));
}

// ---------------------------------------------------------------------------
// Rendering
// ---------------------------------------------------------------------------

function render() {
  document.documentElement.lang = state.lang;
  document.querySelectorAll(".lang-btn").forEach(b => b.classList.toggle("active", b.dataset.lang === state.lang));
  const app = $("#app");
  switch (state.phase) {
    case "connecting":
      app.innerHTML = `<p class="status">${esc(t().connecting)}</p>`;
      break;
    case "error":
      app.innerHTML = `<p class="status">${esc(t()[state.errorKey])}</p>`;
      break;
    case "join":
      renderJoin(app);
      break;
    case "room":
      app.innerHTML = `
        <section class="card">
          <div class="room-head">
            <div>
              <div class="label">${esc(t().roomCode)}</div>
              <div class="code">${esc(state.roomCode)}</div>
            </div>
            <button class="btn-ghost" id="leave">${esc(t().leave)}</button>
          </div>
          <div class="label">${esc(t().members)}</div>
          <div class="pills" id="members"></div>
        </section>
        <section class="card">
          <div class="label" id="show-label"></div>
          <div class="segmented" id="shows"></div>
        </section>
        <section id="board"></section>`;
      $("#leave").addEventListener("click", leaveRoom);
      renderRoom();
      break;
  }
}

function renderJoin(app) {
  // Keep what was typed when the form re-renders (failed join, language switch).
  const prev = $("#join-form");
  const params = new URLSearchParams(location.search);
  const code = normalizeCode(prev?.code.value ?? params.get("room") ?? store.get("lastRoom") ?? "");
  const username = normalizeUsername(prev?.username.value ?? store.get("username") ?? "");
  app.innerHTML = `
    <form class="join card" id="join-form" novalidate>
      <p>${esc(t().joinIntro)}</p>
      ${state.notice ? `<div class="notice">${esc(state.notice)}</div>` : ""}
      <label class="field">
        <div class="label">${esc(t().username)}</div>
        <input name="username" maxlength="2" autocomplete="off" autocapitalize="characters" spellcheck="false" value="${esc(username)}">
        <small>${esc(t().usernameHint)}</small>
      </label>
      <label class="field">
        <div class="label">${esc(t().roomCode)}</div>
        <input name="code" maxlength="6" autocomplete="off" autocapitalize="characters" spellcheck="false" value="${esc(code)}">
      </label>
      ${state.joinError ? `<div class="error">${esc(state.joinError)}</div>` : ""}
      <button class="btn" type="submit" id="join-btn">${esc(t().joinRoom)}</button>
      <p class="small-print">${esc(t().createInApp)} <a href="../">${esc(t().getApp)}</a></p>
    </form>`;
  const form = $("#join-form");
  const btn = $("#join-btn");
  const update = () => {
    form.username.value = normalizeUsername(form.username.value);
    form.code.value = normalizeCode(form.code.value);
    btn.disabled = state.joining || form.username.value.length !== 2 || form.code.value.length !== 6;
  };
  form.username.addEventListener("input", update);
  form.code.addEventListener("input", update);
  form.addEventListener("submit", e => { e.preventDefault(); if (!btn.disabled) onJoin(form.code.value, form.username.value); });
  update();
}

const normalizeUsername = s => s.toUpperCase().replace(/[^A-Z0-9ÄÖÅ]/g, "").slice(0, 2);
const normalizeCode = s => s.toUpperCase().replace(/[^A-Z0-9]/g, "").slice(0, 6);

async function onJoin(code, username) {
  state.joining = true;
  state.joinError = null;
  state.notice = null;
  render();
  try {
    await joinRoom(code, username);
    state.joining = false;
    enterRoom(code, username, null);
  } catch (err) {
    state.joining = false;
    state.joinError = err.message === "notFound" ? t().roomNotFound
      : err.message === "taken" ? t().usernameTaken
      : t().joinFailed;
    render();
  }
}

function renderRoom() {
  if (state.phase !== "room") return;
  renderMembers();
  renderShows();
  renderBoard();
}

// Own member first, then the rest alphabetically (same order as the app's table columns).
function sortedMembers() {
  return Object.entries(state.members)
    .sort(([a, an], [b, bn]) => (a === state.uid ? -1 : b === state.uid ? 1 : an.localeCompare(bn)));
}

function isOnline(uid) {
  return uid === state.uid || Date.now() - (state.presence[uid] ?? 0) < ONLINE_MS;
}

function renderMembers() {
  const el = $("#members");
  if (!el) return;
  el.innerHTML = sortedMembers().map(([uid, name]) => {
    const online = isOnline(uid);
    return `<span class="pill${uid === state.uid ? " own" : ""}${online ? " online" : ""}"
      ${online ? `title="${esc(t().online(name))}"` : ""}>${esc(name.toUpperCase())}</span>`;
  }).join("");
}

function renderShows() {
  const el = $("#shows");
  if (!el) return;
  $("#show-label").textContent = t().show + (state.year ? ` · ${state.year}` : "");
  el.innerHTML = SHOW_IDS.map(id => {
    const enabled = (state.shows[id]?.length ?? 0) > 0;
    return `<button class="seg${id === state.showId ? " active" : ""}" data-show="${id}" ${enabled ? "" : "disabled"}>
      ${esc(t().showLabel[id])}</button>`;
  }).join("");
  el.querySelectorAll(".seg").forEach(b => b.addEventListener("click", () => selectShow(b.dataset.show)));
}

function renderBoard(changed = new Set()) {
  const el = $("#board");
  if (!el) return;
  const s = t();
  const participants = state.shows[state.showId] ?? [];
  if (!state.showId || participants.length === 0) {
    const anyShow = SHOW_IDS.some(id => state.shows[id]?.length);
    el.innerHTML = `<p class="status">${esc(anyShow ? s.pickShow : s.noShows)}</p>`;
    return;
  }

  const members = sortedMembers();
  const scored = participants.filter(p => state.votes[p.order]?.[state.uid] != null).length;
  const hidden = 100 - (scored / participants.length) * 100;
  const scrollLeft = el.querySelector(".table-wrap")?.scrollLeft ?? 0;

  const head = members.map(([uid, name]) =>
    `<th class="${uid === state.uid ? "c-own own" : ""}">${esc(name.toUpperCase())}</th>`).join("");
  const rows = participants.map(p => {
    const entry = state.votes[p.order] ?? {};
    const cells = members.map(([uid]) => {
      const pts = entry[uid];
      const own = uid === state.uid;
      const cls = [
        own ? "c-own own" : "",
        pts == null ? "empty" : "",
        changed.has(`${p.order}:${uid}`) ? (own ? "pop" : "flash") : "",
      ].filter(Boolean).join(" ");
      return `<td class="${cls}"><span class="v">${pts ?? "—"}</span></td>`;
    }).join("");
    const name = translateCountry(state.lang, p.country);
    return `<tr data-order="${p.order}" tabindex="0">
      <td class="c-rank">${p.order}</td>
      <td class="c-country" title="${esc(name)}"><span class="flag">${countryFlag(p.country)}</span><span class="cname">${esc(name)}</span></td>
      ${cells}</tr>`;
  }).join("");

  el.innerHTML = `
    <div class="board">
      <div class="voting-banner">
        <div class="row">${esc(s.votingOn)} <span class="grad">${esc(s.showTitle[state.showId])}</span></div>
        <div class="progress"><i style="clip-path:inset(0 ${hidden}% 0 0)"></i></div>
      </div>
      <div class="table-wrap">
        <table class="grid">
          <thead><tr><th class="c-rank">#</th><th class="c-country"></th>${head}</tr></thead>
          <tbody>${rows}</tbody>
        </table>
      </div>
    </div>
    <p class="hint">${esc(s.tapToScore)}</p>`;
  el.querySelector(".table-wrap").scrollLeft = scrollLeft;
  el.querySelectorAll("tbody tr").forEach(tr => {
    const order = Number(tr.dataset.order);
    tr.addEventListener("click", () => openPicker(order));
    tr.addEventListener("keydown", e => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); openPicker(order); } });
  });
}

// ---------------------------------------------------------------------------
// Point picker
// ---------------------------------------------------------------------------

// Same blue → orange ramp as heatColor() in NumberPickerDialog.kt.
function heatColor(points) {
  const k = (points - 1) / 11;
  const mix = (a, b) => Math.round(a + (b - a) * k);
  return `rgb(${mix(0x3D, 0xFF)},${mix(0x68, 0x91)},${mix(0xB8, 0x00)})`;
}

const picker = $("#picker");

function openPicker(order) {
  const p = (state.shows[state.showId] ?? []).find(x => x.order === order);
  if (!p) return;
  pickerOrder = order;
  const current = state.votes[order]?.[state.uid];
  const song = [p.artist, p.song].filter(Boolean).join(" - ");
  picker.innerHTML = `
    <div class="picker">
      <div class="p-title"><span class="flag">${countryFlag(p.country)}</span><span>${esc(translateCountry(state.lang, p.country))}</span></div>
      ${song ? `<div class="p-song" title="${esc(song)}">♪ ${esc(song)}</div>` : ""}
      <div class="chips">
        ${Array.from({ length: 12 }, (_, i) => i + 1).map(v =>
          `<button class="chip${v === current ? " current" : ""}" data-points="${v}" style="background:${heatColor(v)}">${v}</button>`).join("")}
      </div>
      <div class="picker-foot"><button class="btn-ghost" id="picker-cancel">${esc(t().cancel)}</button></div>
    </div>`;
  picker.querySelectorAll(".chip").forEach(b => b.addEventListener("click", () => {
    const points = Number(b.dataset.points);
    closePicker();
    submitVote(order, points);
  }));
  $("#picker-cancel").addEventListener("click", closePicker);
  if (!picker.open) picker.showModal();
  picker.querySelector(".chip.current, .chip")?.focus();
}

function closePicker() {
  pickerOrder = null;
  if (picker.open) picker.close();
}

// A click outside the panel lands on the dialog element itself (its backdrop).
picker.addEventListener("click", e => { if (e.target === picker) closePicker(); });
picker.addEventListener("close", () => { pickerOrder = null; });

// ---------------------------------------------------------------------------
// Misc UI
// ---------------------------------------------------------------------------

let toastTimer = null;
function toast(text) {
  const el = $("#toast");
  el.textContent = text;
  el.classList.add("show");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => el.classList.remove("show"), 3500);
}

document.querySelectorAll(".lang-btn").forEach(b => b.addEventListener("click", () => {
  state.lang = b.dataset.lang;
  store.set("lang", state.lang);
  if (pickerOrder != null) openPicker(pickerOrder);
  render();
}));

render();
start();
