#!/usr/bin/env python3
"""Eurovision Firestore maintenance tool."""

import base64
import json
import random
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from datetime import datetime, timezone, timedelta
from pathlib import Path

from _credentials import PROJECT_ID, SERVICE_ACCOUNT_KEY

BASE_URL            = f"https://firestore.googleapis.com/v1/projects/{PROJECT_ID}/databases/(default)/documents"
AUTH_BASE_URL       = f"https://identitytoolkit.googleapis.com/v1/projects/{PROJECT_ID}"
AUTH_V3_URL         = "https://www.googleapis.com/identitytoolkit/v3/relyingparty"
BACKUPS_DIR         = Path(__file__).parent.parent / "Backups"
PARTICIPANTS_FILE   = Path(__file__).parent / "participants.json"
RESULTS_FILE        = Path(__file__).parent / "results.json"

_TIMESTAMP_RE = re.compile(r"^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d+)?Z?$")

_DEMO_ROOM_CODE = "DEMO01"
_DEMO_SHOW_ID   = "final"
_DEMO_NOW_TS    = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S.%fZ")
_DEMO_MEMBERS   = [
    {"uid": "demouid-AA",                   "username": "AA"},
    {"uid": "demouid-JJ",                   "username": "JJ"},
    {"uid": "demouid-MK",                   "username": "MK"},
    {"uid": "demouid-TV",                  "username": "TV"},
]


# ── Auth ──────────────────────────────────────────────────────────────────────

def get_token():
    if not SERVICE_ACCOUNT_KEY.exists():
        sys.exit(
            f"Error: {SERVICE_ACCOUNT_KEY} not found.\n"
            "Download it from: Firebase console → Project settings → "
            "Service accounts → Generate new private key"
        )
    from cryptography.hazmat.primitives import hashes, serialization
    from cryptography.hazmat.primitives.asymmetric import padding

    with open(SERVICE_ACCOUNT_KEY) as f:
        key = json.load(f)

    now     = int(time.time())
    header  = {"alg": "RS256", "typ": "JWT"}
    payload = {
        "iss":   key["client_email"],
        "sub":   key["client_email"],
        "scope": "https://www.googleapis.com/auth/cloud-platform",
        "aud":   "https://oauth2.googleapis.com/token",
        "iat":   now,
        "exp":   now + 3600,
    }

    def b64url(obj):
        return base64.urlsafe_b64encode(json.dumps(obj).encode()).rstrip(b"=").decode()

    signing_input = f"{b64url(header)}.{b64url(payload)}".encode()
    private_key   = serialization.load_pem_private_key(key["private_key"].encode(), password=None)
    signature     = private_key.sign(signing_input, padding.PKCS1v15(), hashes.SHA256())
    jwt           = f"{signing_input.decode()}.{base64.urlsafe_b64encode(signature).rstrip(b'=').decode()}"

    body = urllib.parse.urlencode({
        "grant_type": "urn:ietf:params:oauth:grant-type:jwt-bearer",
        "assertion":  jwt,
    }).encode()
    req = urllib.request.Request(
        "https://oauth2.googleapis.com/token", data=body,
        headers={"Content-Type": "application/x-www-form-urlencoded"},
    )
    try:
        with urllib.request.urlopen(req) as r:
            return json.loads(r.read())["access_token"]
    except urllib.error.HTTPError as e:
        sys.exit(
            f"Error: could not authenticate with service account ({e.code} {e.reason}).\n"
            "Check that service_account.json is valid and has Firestore access."
        )


# ── Firestore helpers ─────────────────────────────────────────────────────────

def to_fs(value):
    if isinstance(value, bool):
        return {"booleanValue": value}
    if isinstance(value, int):
        return {"integerValue": str(value)}
    if isinstance(value, float):
        return {"doubleValue": value}
    if isinstance(value, str):
        if _TIMESTAMP_RE.match(value):
            return {"timestampValue": value}
        return {"stringValue": value}
    if value is None:
        return {"nullValue": None}
    if isinstance(value, list):
        return {"arrayValue": {"values": [to_fs(v) for v in value]}}
    if isinstance(value, dict):
        return {"mapValue": {"fields": {k: to_fs(v) for k, v in value.items()}}}
    return {"nullValue": None}


def from_fs(value):
    if "stringValue"    in value: return value["stringValue"]
    if "integerValue"   in value: return int(value["integerValue"])
    if "doubleValue"    in value: return value["doubleValue"]
    if "booleanValue"   in value: return value["booleanValue"]
    if "nullValue"      in value: return None
    if "timestampValue" in value: return value["timestampValue"]
    if "bytesValue"     in value: return value["bytesValue"]
    if "referenceValue" in value: return value["referenceValue"]
    if "geoPointValue"  in value: return value["geoPointValue"]
    if "arrayValue"     in value:
        return [from_fs(v) for v in value["arrayValue"].get("values", [])]
    if "mapValue"       in value:
        return {k: from_fs(v) for k, v in value["mapValue"]["fields"].items()}
    return None


def _fs_field_path(name):
    if re.match(r"^[a-zA-Z_][a-zA-Z0-9_]*$", name):
        return name
    return f"`{name}`"


def patch(token, path, fields):
    url  = f"{BASE_URL}/{path}"
    body = json.dumps({"fields": {k: to_fs(v) for k, v in fields.items()}}).encode()
    req  = urllib.request.Request(
        url, data=body, method="PATCH",
        headers={"Authorization": f"Bearer {token}", "Content-Type": "application/json"},
    )
    with urllib.request.urlopen(req) as r:
        return r.status


def patch_raw(token, path, raw_fields):
    """PATCH with pre-encoded Firestore wire-format fields (used for rename ops)."""
    url  = f"{BASE_URL}/{path}"
    body = json.dumps({"fields": raw_fields}).encode()
    req  = urllib.request.Request(
        url, data=body, method="PATCH",
        headers={"Authorization": f"Bearer {token}", "Content-Type": "application/json"},
    )
    with urllib.request.urlopen(req) as r:
        return r.status


def delete_field(token, path, field_name):
    """Remove one field from a document via updateMask (field not present in body = deleted)."""
    fp  = _fs_field_path(field_name)
    url = f"{BASE_URL}/{path}?updateMask.fieldPaths={urllib.parse.quote(fp)}"
    body = json.dumps({"fields": {}}).encode()
    req  = urllib.request.Request(
        url, data=body, method="PATCH",
        headers={"Authorization": f"Bearer {token}", "Content-Type": "application/json"},
    )
    with urllib.request.urlopen(req) as r:
        return r.status


def get_doc(token, path):
    req = urllib.request.Request(
        f"{BASE_URL}/{path}",
        headers={"Authorization": f"Bearer {token}"},
    )
    try:
        with urllib.request.urlopen(req) as r:
            return json.loads(r.read())
    except urllib.error.HTTPError as e:
        if e.code == 404:
            return None
        raise


def list_collection(token, path):
    page_token = None
    while True:
        url = f"{BASE_URL}/{path}?showMissing=true"
        if page_token:
            url += f"&pageToken={page_token}"
        req = urllib.request.Request(url, headers={"Authorization": f"Bearer {token}"})
        try:
            with urllib.request.urlopen(req) as r:
                data = json.loads(r.read())
        except urllib.error.HTTPError as e:
            if e.code == 404:
                return
            raise
        yield from data.get("documents", [])
        page_token = data.get("nextPageToken")
        if not page_token:
            break


def delete_doc(token, path):
    req = urllib.request.Request(
        f"{BASE_URL}/{path}", method="DELETE",
        headers={"Authorization": f"Bearer {token}"},
    )
    try:
        with urllib.request.urlopen(req) as r:
            return r.status
    except urllib.error.HTTPError as e:
        if e.code == 404:
            return 404
        raise


def list_collection_ids(token, doc_path=None):
    url = f"{BASE_URL}/{doc_path}:listCollectionIds" if doc_path else f"{BASE_URL}:listCollectionIds"
    ids, page_token = [], None
    while True:
        body = {"pageSize": 100}
        if page_token:
            body["pageToken"] = page_token
        req = urllib.request.Request(
            url, data=json.dumps(body).encode(),
            headers={"Authorization": f"Bearer {token}", "Content-Type": "application/json"},
        )
        with urllib.request.urlopen(req) as r:
            data = json.loads(r.read())
        ids.extend(data.get("collectionIds", []))
        page_token = data.get("nextPageToken")
        if not page_token:
            break
    return ids


# ── Input helpers ─────────────────────────────────────────────────────────────

def _input_safe(prompt=""):
    try:
        return input(prompt)
    except KeyboardInterrupt:
        print("\nAborted.")
        sys.exit(0)


def confirm(prompt="Press Y to proceed (anything else cancels): "):
    return _input_safe(prompt).strip().upper() == "Y"


# ── Room operations ───────────────────────────────────────────────────────────

def fetch_room(token, room_code):
    """Return (room_fields_dict, members_list) or None if room not found."""
    doc = get_doc(token, f"rooms/{room_code}")
    if doc is None:
        return None
    room_fields = {k: from_fs(v) for k, v in doc.get("fields", {}).items()}
    members = []
    for m_doc in list_collection(token, f"rooms/{room_code}/members"):
        uid = m_doc["name"].rsplit("/", 1)[-1]
        f   = m_doc.get("fields", {})
        members.append({
            "uid":      uid,
            "username": from_fs(f["username"]) if "username" in f else uid,
        })
    return room_fields, members


def delete_member(token, room_code, uid, username):
    base = f"rooms/{room_code}"

    delete_doc(token, f"{base}/members/{uid}")
    print(f"  Deleted member document ({uid})")

    delete_doc(token, f"{base}/usernames/{username.lower()}")
    print(f"  Deleted username lock '{username.lower()}'")

    for show_doc in list_collection(token, f"{base}/votes"):
        show_id = show_doc["name"].rsplit("/", 1)[-1]
        count   = 0
        for entry_doc in list_collection(token, f"{base}/votes/{show_id}/entries"):
            order = entry_doc["name"].rsplit("/", 1)[-1]
            if uid in entry_doc.get("fields", {}):
                delete_field(token, f"{base}/votes/{show_id}/entries/{order}", uid)
                count += 1
        if count:
            print(f"  Removed votes from {count} entr{'y' if count == 1 else 'ies'} (show: {show_id})")

    for show_doc in list_collection(token, f"{base}/guesses"):
        show_id = show_doc["name"].rsplit("/", 1)[-1]
        status  = delete_doc(token, f"{base}/guesses/{show_id}/picks/{uid}")
        if status != 404:
            print(f"  Deleted guess picks (show: {show_id})")


def delete_room(token, room_code):
    base = f"rooms/{room_code}"

    uids = [d["name"].rsplit("/", 1)[-1] for d in list_collection(token, f"{base}/members")]
    for uid in uids:
        delete_doc(token, f"{base}/members/{uid}")
    print(f"  Deleted {len(uids)} member document(s)")

    names = [d["name"].rsplit("/", 1)[-1] for d in list_collection(token, f"{base}/usernames")]
    for name in names:
        delete_doc(token, f"{base}/usernames/{name}")
    print(f"  Deleted {len(names)} username lock(s)")

    for show_doc in list_collection(token, f"{base}/votes"):
        show_id = show_doc["name"].rsplit("/", 1)[-1]
        orders  = [d["name"].rsplit("/", 1)[-1] for d in list_collection(token, f"{base}/votes/{show_id}/entries")]
        for order in orders:
            delete_doc(token, f"{base}/votes/{show_id}/entries/{order}")
        print(f"  Deleted {len(orders)} vote entries (show: {show_id})")

    for show_doc in list_collection(token, f"{base}/guesses"):
        show_id = show_doc["name"].rsplit("/", 1)[-1]
        uids2   = [d["name"].rsplit("/", 1)[-1] for d in list_collection(token, f"{base}/guesses/{show_id}/picks")]
        for uid in uids2:
            delete_doc(token, f"{base}/guesses/{show_id}/picks/{uid}")
        print(f"  Deleted {len(uids2)} guess pick(s) (show: {show_id})")

    delete_doc(token, base)
    print(f"  Deleted room document {room_code}")


def purge_stale_rooms(token, days=60):
    """Delete all rooms with no activity in the last `days` days. Returns count deleted."""
    cutoff = datetime.now(timezone.utc) - timedelta(days=days)

    print(f"Scanning rooms (cutoff: inactive since {cutoff.strftime('%Y-%m-%d')})...")
    stale = []
    for room_doc in list_collection(token, "rooms"):
        room_code = room_doc["name"].rsplit("/", 1)[-1]
        fields    = room_doc.get("fields", {})
        last_act  = from_fs(fields["lastActivityAt"]) if "lastActivityAt" in fields else None
        if last_act is None:
            stale.append((room_code, "unknown"))
            continue
        ts = datetime.fromisoformat(last_act.rstrip("Z").split(".")[0]).replace(tzinfo=timezone.utc)
        if ts < cutoff:
            stale.append((room_code, last_act[:10]))

    if not stale:
        print("No stale rooms found.")
        return 0

    print(f"\nFound {len(stale)} stale room(s):")
    for code, last in stale:
        print(f"  {code}  (last activity: {last})")

    print(f"\nThis will permanently delete {len(stale)} room(s) and all their data.")
    if not confirm():
        return 0

    for code, _ in stale:
        print(f"\nDeleting {code}...")
        delete_room(token, code)

    print(f"\nPurged {len(stale)} room(s).")
    return len(stale)


# ── Backup ────────────────────────────────────────────────────────────────────

def _count_documents(token, collection_path):
    parts  = collection_path.split("/")
    col_id = parts[-1]
    parent = "/".join(parts[:-1])
    url    = f"{BASE_URL}/{parent}:runAggregationQuery" if parent else f"{BASE_URL}:runAggregationQuery"
    body   = {
        "structuredAggregationQuery": {
            "aggregations": [{"count": {}, "alias": "count"}],
            "structuredQuery": {"from": [{"collectionId": col_id}]},
        }
    }
    req = urllib.request.Request(
        url, data=json.dumps(body).encode(),
        headers={"Authorization": f"Bearer {token}", "Content-Type": "application/json"},
    )
    with urllib.request.urlopen(req) as r:
        data = json.loads(r.read())
    return int(data[0]["result"]["aggregateFields"]["count"]["integerValue"])


def _backup_collection(token, collection_path, depth=1):
    result   = {}
    raw_docs = list(list_collection(token, collection_path))
    implicit = sum(1 for d in raw_docs if not d.get("fields"))
    note     = f"  ({implicit} implicit)" if implicit else ""
    print(f"  {'  ' * depth}{collection_path}/  ({len(raw_docs)} doc(s){note})")
    for doc in raw_docs:
        doc_id    = doc["name"].rsplit("/", 1)[-1]
        entry     = {"fields": {k: from_fs(v) for k, v in doc.get("fields", {}).items()}}
        sub_names = list_collection_ids(token, f"{collection_path}/{doc_id}")
        if sub_names:
            entry["collections"] = {
                sub: _backup_collection(token, f"{collection_path}/{doc_id}/{sub}", depth + 1)
                for sub in sub_names
            }
        result[doc_id] = entry
    return result


def _check_backup_counts(token, backup):
    mismatches = []

    def walk(col_path, col_docs):
        backed_up = sum(1 for d in col_docs.values() if d.get("fields") or not d.get("collections"))
        live      = _count_documents(token, col_path)
        ok        = backed_up == live
        print(f"  {col_path}: {backed_up} backed up, {live} live  [{'OK' if ok else 'MISMATCH'}]")
        if not ok:
            mismatches.append(col_path)
        for doc_id, doc_data in col_docs.items():
            for sub_name, sub_docs in doc_data.get("collections", {}).items():
                walk(f"{col_path}/{doc_id}/{sub_name}", sub_docs)

    for col_name, col_docs in backup["collections"].items():
        walk(col_name, col_docs)
    return mismatches


def do_backup(token):
    print("\nDiscovering root collections:")
    root_cols = list_collection_ids(token)
    for name in root_cols:
        print(f"  {name}")

    backup = {
        "timestamp":   datetime.now().isoformat(),
        "project_id":  PROJECT_ID,
        "collections": {},
    }

    print("\nBacking up:")
    for col in root_cols:
        backup["collections"][col] = _backup_collection(token, col)

    BACKUPS_DIR.mkdir(parents=True, exist_ok=True)
    ts       = datetime.now().strftime("%Y%m%d_%H%M%S")
    out_path = BACKUPS_DIR / f"backup_{ts}.json"
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(backup, f, ensure_ascii=False, indent=2)

    size_kb = out_path.stat().st_size / 1024
    print(f"\nSaved → {out_path}  ({size_kb:.1f} KB)")

    print("\nVerifying counts:")
    mismatches = _check_backup_counts(token, backup)
    if mismatches:
        print(f"\nWARNING: count mismatch in {len(mismatches)} collection(s):")
        for p in mismatches:
            print(f"  {p}")
    else:
        print("All counts match.")


# ── Restore ───────────────────────────────────────────────────────────────────

def _restore_collection(token, col_path, col_docs, counters):
    for doc_id, doc_data in col_docs.items():
        fields      = doc_data.get("fields", {})
        sub_cols    = doc_data.get("collections", {})
        doc_path    = f"{col_path}/{doc_id}"
        is_implicit = not fields and sub_cols
        if not is_implicit:
            try:
                patch(token, doc_path, fields)
                print(f"  OK  {doc_path}")
                counters["written"] += 1
            except urllib.error.HTTPError as e:
                body_text = e.read().decode(errors="replace")
                print(f"  ERROR  {doc_path}: {e.code} {e.reason} — {body_text[:200]}", file=sys.stderr)
        for sub_name, sub_docs in sub_cols.items():
            _restore_collection(token, f"{doc_path}/{sub_name}", sub_docs, counters)


def do_restore(token):
    files = sorted(BACKUPS_DIR.glob("backup_*.json"), reverse=True) if BACKUPS_DIR.exists() else []
    if not files:
        print(f"No backup files found in {BACKUPS_DIR}")
        return

    print("\nAvailable backups:")
    for i, f in enumerate(files):
        size_kb = f.stat().st_size / 1024
        print(f"  [{i + 1}] {f.name}  ({size_kb:.1f} KB)")

    try:
        idx = int(_input_safe(f"\nSelect backup [1-{len(files)}]: ").strip()) - 1
        if not 0 <= idx < len(files):
            raise ValueError
    except ValueError:
        print("Cancelled.")
        return

    backup_path = files[idx]
    with open(backup_path, encoding="utf-8") as f:
        backup = json.load(f)

    timestamp  = backup.get("timestamp", "unknown")
    project_id = backup.get("project_id", "unknown")
    all_cols   = list(backup["collections"].keys())

    print(f"\nBackup:      {backup_path.name}")
    print(f"Taken at:    {timestamp}")
    print(f"Project:     {project_id}")
    print(f"Target:      {PROJECT_ID}")
    print(f"Collections: {', '.join(all_cols)}")

    if project_id != PROJECT_ID:
        print(f"\nWARNING: backup is from '{project_id}' but target is '{PROJECT_ID}'.")

    if not confirm():
        return

    counters = {"written": 0}
    for col in all_cols:
        print(f"\n{col}/")
        _restore_collection(token, col, backup["collections"][col], counters)

    print(f"\nWritten: {counters['written']} document(s) across {len(all_cols)} collection(s).")


# ── Upload / demo room ────────────────────────────────────────────────────────

def do_upload_participants(token):
    if not PARTICIPANTS_FILE.exists():
        print(f"Error: {PARTICIPANTS_FILE.name} not found.")
        print(f"Create it at: {PARTICIPANTS_FILE}")
        print('Format: {"final": [{"order": 1, "country": "...", "artist": "...", "song": "..."}, ...], "semi1": [], "semi2": []}')
        return

    with open(PARTICIPANTS_FILE, encoding="utf-8") as f:
        shows = json.load(f)

    for show_id, participants in shows.items():
        if not participants:
            print(f"  {show_id}: skipped (empty)")
            continue
        print(f"  {show_id}: {len(participants)} participant(s)...", end=" ", flush=True)
        status = patch(token, f"shows/{show_id}", {"participants": participants})
        print(f"HTTP {status}")

    print("Done.")


def do_upload_results(token):
    if not RESULTS_FILE.exists():
        print(f"Error: {RESULTS_FILE.name} not found.")
        print(f"Create it at: {RESULTS_FILE}")
        print('Format: {"year": 2026, "entries": [{"order": 1, "rank": 7, "juryScore": 165, "publicScore": 78}, ...]}')
        return

    with open(RESULTS_FILE, encoding="utf-8") as f:
        data = json.load(f)

    year    = data["year"]
    entries = data["entries"]

    status = patch(token, "results/final", {"year": year})
    print(f"  results/final (year={year}): HTTP {status}")

    for entry in entries:
        order  = entry["order"]
        status = patch(token, f"results/final/entries/{order}", {
            "rank":        entry["rank"],
            "juryScore":   entry["juryScore"],
            "publicScore": entry["publicScore"],
        })
        print(f"  entry {order:2d}: rank={entry['rank']:2d}  jury={entry['juryScore']:4d}  public={entry['publicScore']:4d}  [HTTP {status}]")

    print("Done.")


def do_create_demo_room(token):
    room = _DEMO_ROOM_CODE
    show = _DEMO_SHOW_ID

    status = patch(token, f"rooms/{room}", {
        "createdAt":      _DEMO_NOW_TS,
        "lastActivityAt": _DEMO_NOW_TS,
    })
    print(f"  Room {room}  [HTTP {status}]")

    for m in _DEMO_MEMBERS:
        uid, name = m["uid"], m["username"]
        status = patch(token, f"rooms/{room}/members/{uid}", {"username": name, "joinedAt": _DEMO_NOW_TS})
        print(f"  Member {name} ({uid})  [HTTP {status}]")
        status = patch(token, f"rooms/{room}/usernames/{name.lower()}", {"uid": uid})
        print(f"  Username lock '{name.lower()}'  [HTTP {status}]")

    all_uids = [m["uid"] for m in _DEMO_MEMBERS]
    votes = {uid: {order: random.randint(5, 12) for order in range(1, 26)} for uid in all_uids}
    for order in range(1, 26):
        fields = {uid: votes[uid][order] for uid in all_uids}
        status = patch(token, f"rooms/{room}/votes/{show}/entries/{order}", fields)
        print(f"  Vote entry {order:2d}  [HTTP {status}]")

    for uid in all_uids:
        top3 = random.sample(range(1, 26), 3)
        picks = {str(rank): order for rank, order in enumerate(top3, 1)}
        status = patch(token, f"rooms/{room}/guesses/{show}/picks/{uid}", picks)
        name   = next(m["username"] for m in _DEMO_MEMBERS if m["uid"] == uid)
        print(f"  Guess picks for {name} ({uid})  [HTTP {status}]")

    print(f"\nDone. Demo room {room} created.")


# ── Rename / restore shows and results ───────────────────────────────────────

def do_rename_shows_final(token):
    print("Reading shows/final...", end=" ", flush=True)
    doc = get_doc(token, "shows/final")
    if doc is None:
        print("not found.")
        return
    fields       = doc.get("fields", {})
    participants = fields.get("participants", {}).get("arrayValue", {}).get("values", [])
    print(f"OK ({len(participants)} participant(s))")

    print("Writing shows/final_test...", end=" ", flush=True)
    status = patch_raw(token, "shows/final_test", fields)
    print(f"HTTP {status}")

    print("Deleting shows/final...", end=" ", flush=True)
    status = delete_doc(token, "shows/final")
    print(f"HTTP {status}")
    print("Done.")


def do_restore_shows_final_test(token):
    print("Reading shows/final_test...", end=" ", flush=True)
    doc = get_doc(token, "shows/final_test")
    if doc is None:
        print("not found.")
        return
    fields       = doc.get("fields", {})
    participants = fields.get("participants", {}).get("arrayValue", {}).get("values", [])
    print(f"OK ({len(participants)} participant(s))")

    print("Writing shows/final...", end=" ", flush=True)
    status = patch_raw(token, "shows/final", fields)
    print(f"HTTP {status}")

    print("Deleting shows/final_test...", end=" ", flush=True)
    status = delete_doc(token, "shows/final_test")
    print(f"HTTP {status}")
    print("Done.")


def do_rename_results_final(token):
    print("Reading results/final...", end=" ", flush=True)
    doc = get_doc(token, "results/final")
    if doc is None:
        print("not found.")
        return
    parent_fields = doc.get("fields", {})
    print("OK")

    print("Reading results/final/entries...", end=" ", flush=True)
    entries = list(list_collection(token, "results/final/entries"))
    print(f"OK ({len(entries)} entr{'y' if len(entries) == 1 else 'ies'})")

    print("Writing results/final_test...", end=" ", flush=True)
    status = patch_raw(token, "results/final_test", parent_fields)
    print(f"HTTP {status}")

    for entry_doc in entries:
        order = entry_doc["name"].rsplit("/", 1)[-1]
        print(f"  Writing results/final_test/entries/{order}...", end=" ", flush=True)
        status = patch_raw(token, f"results/final_test/entries/{order}", entry_doc.get("fields", {}))
        print(f"HTTP {status}")

    for entry_doc in entries:
        order = entry_doc["name"].rsplit("/", 1)[-1]
        print(f"  Deleting results/final/entries/{order}...", end=" ", flush=True)
        status = delete_doc(token, f"results/final/entries/{order}")
        print(f"HTTP {status}")

    print("Deleting results/final...", end=" ", flush=True)
    status = delete_doc(token, "results/final")
    print(f"HTTP {status}")
    print("Done.")


def do_restore_results_final_test(token):
    print("Reading results/final_test...", end=" ", flush=True)
    doc = get_doc(token, "results/final_test")
    if doc is None:
        print("not found.")
        return
    parent_fields = doc.get("fields", {})
    print("OK")

    print("Reading results/final_test/entries...", end=" ", flush=True)
    entries = list(list_collection(token, "results/final_test/entries"))
    print(f"OK ({len(entries)} entr{'y' if len(entries) == 1 else 'ies'})")

    print("Writing results/final...", end=" ", flush=True)
    status = patch_raw(token, "results/final", parent_fields)
    print(f"HTTP {status}")

    for entry_doc in entries:
        order = entry_doc["name"].rsplit("/", 1)[-1]
        print(f"  Writing results/final/entries/{order}...", end=" ", flush=True)
        status = patch_raw(token, f"results/final/entries/{order}", entry_doc.get("fields", {}))
        print(f"HTTP {status}")

    for entry_doc in entries:
        order = entry_doc["name"].rsplit("/", 1)[-1]
        print(f"  Deleting results/final_test/entries/{order}...", end=" ", flush=True)
        status = delete_doc(token, f"results/final_test/entries/{order}")
        print(f"HTTP {status}")

    print("Deleting results/final_test...", end=" ", flush=True)
    status = delete_doc(token, "results/final_test")
    print(f"HTTP {status}")
    print("Done.")


# ── Firebase Auth user cleanup ────────────────────────────────────────────────

def list_auth_users(token):
    """Yield all Firebase Auth user records via v3 downloadAccount."""
    page_token = None
    while True:
        payload = {"maxResults": 1000}
        if page_token:
            payload["nextPageToken"] = page_token
        body = json.dumps(payload).encode()
        req  = urllib.request.Request(
            f"{AUTH_V3_URL}/downloadAccount", data=body,
            headers={"Authorization": f"Bearer {token}", "Content-Type": "application/json"},
        )
        try:
            with urllib.request.urlopen(req) as r:
                data = json.loads(r.read())
        except urllib.error.HTTPError as e:
            body_text = e.read().decode(errors="replace")
            sys.exit(f"Error listing Auth users: {e.code} {e.reason} — {body_text[:300]}")
        yield from data.get("users", [])
        page_token = data.get("nextPageToken")
        if not page_token:
            break


def _delete_auth_user(token, uid):
    """Delete a single Firebase Auth user by UID via v3 deleteAccount. Returns True on success."""
    body = json.dumps({"localId": uid}).encode()
    req  = urllib.request.Request(
        f"{AUTH_V3_URL}/deleteAccount", data=body,
        headers={"Authorization": f"Bearer {token}", "Content-Type": "application/json"},
    )
    try:
        with urllib.request.urlopen(req) as r:
            r.read()
        return True
    except urllib.error.HTTPError as e:
        body_text = e.read().decode(errors="replace")
        print(f"  ERROR deleting {uid}: {e.code} {e.reason} — {body_text[:120]}", file=sys.stderr)
        return False


def purge_stale_auth_users(token, days=90):
    """Delete anonymous Firebase Auth users with no sign-in activity in the last `days` days."""
    cutoff    = datetime.now(timezone.utc) - timedelta(days=days)
    cutoff_ms = cutoff.timestamp() * 1000

    print(f"Scanning Firebase Auth users (cutoff: inactive since {cutoff.strftime('%Y-%m-%d')})...")
    stale = []
    total = 0
    for user in list_auth_users(token):
        total += 1
        if user.get("providerUserInfo"):
            continue  # not anonymous
        last_ms = user.get("lastLoginAt") or user.get("createdAt")
        if last_ms is None or int(last_ms) < cutoff_ms:
            stale.append((user["localId"], int(last_ms) if last_ms else None))

    print(f"Scanned {total} user(s). Found {len(stale)} stale anonymous user(s).")
    if not stale:
        return 0

    show_n = min(10, len(stale))
    print(f"\nSample (first {show_n}):")
    for uid, ts_ms in stale[:show_n]:
        last = datetime.fromtimestamp(ts_ms / 1000, tz=timezone.utc).strftime("%Y-%m-%d") if ts_ms else "unknown"
        print(f"  {uid}  (last sign-in: {last})")
    if len(stale) > show_n:
        print(f"  ... and {len(stale) - show_n} more")

    print(f"\nThis will permanently delete {len(stale)} anonymous Auth user(s).")
    if not confirm():
        return 0

    deleted = 0
    for i, (uid, _) in enumerate(stale, 1):
        if _delete_auth_user(token, uid):
            deleted += 1
        if i % 50 == 0:
            print(f"  Progress: {i}/{len(stale)} processed, {deleted} deleted so far")

    print(f"\nPurged {deleted} of {len(stale)} anonymous Auth user(s).")
    return deleted


# ── Menus ─────────────────────────────────────────────────────────────────────

def menu_rename_restore(token):
    while True:
        print("\nRename / restore shows and results")
        print("  1) Rename  shows/final        →  shows/final_test")
        print("  2) Restore shows/final_test   →  shows/final")
        print("  3) Rename  results/final       →  results/final_test")
        print("  4) Restore results/final_test  →  results/final")
        print("  0) Back")
        choice = _input_safe("\nChoice: ").strip()
        if choice == "0":
            return
        elif choice == "1":
            print("\nRename shows/final → shows/final_test")
            if confirm():
                do_rename_shows_final(token)
        elif choice == "2":
            print("\nRestore shows/final_test → shows/final")
            if confirm():
                do_restore_shows_final_test(token)
        elif choice == "3":
            print("\nRename results/final → results/final_test")
            if confirm():
                do_rename_results_final(token)
        elif choice == "4":
            print("\nRestore results/final_test → results/final")
            if confirm():
                do_restore_results_final_test(token)


def menu_database(token):
    while True:
        print("\nDatabase maintenance")
        print("  1) Backup Firestore")
        print("  2) Restore Firestore from backup")
        print("  3) Upload participants")
        print("  4) Upload results")
        print(f"  5) Create demo room ({_DEMO_ROOM_CODE})")
        print("  6) Rename / restore shows and results")
        print("  0) Back")
        choice = _input_safe("\nChoice: ").strip()
        if choice == "0":
            return
        elif choice == "1":
            do_backup(token)
        elif choice == "2":
            do_restore(token)
        elif choice == "3":
            do_upload_participants(token)
        elif choice == "4":
            do_upload_results(token)
        elif choice == "5":
            print(f"\nCreate demo room {_DEMO_ROOM_CODE} with fixed members and randomly generated votes/guesses (overwrites existing data).")
            if confirm():
                do_create_demo_room(token)
        elif choice == "6":
            menu_rename_restore(token)


def _menu_room_manage(token):
    while True:
        room_code = _input_safe("\nEnter room code (or 0 to go back): ").strip().upper()
        if room_code == "0":
            return
        if not room_code:
            continue

        result = fetch_room(token, room_code)
        if result is None:
            print("Room not found.")
            continue

        room_fields, members = result
        created  = room_fields.get("createdAt", "—")
        last_act = room_fields.get("lastActivityAt", "—")

        print(f"\nRoom: {room_code}")
        print(f"Created:       {created}")
        print(f"Last activity: {last_act}")
        if members:
            print("Members:")
            for i, m in enumerate(members, 1):
                print(f"  {i}) {m['username']}  (uid: {m['uid']})")
        else:
            print("Members: (none)")

        while True:
            print("\n  1) Delete a member")
            print("  2) Delete whole room")
            print("  0) Back")
            choice = _input_safe("\nChoice: ").strip()
            if choice == "0":
                break
            elif choice == "1":
                if not members:
                    print("No members in this room.")
                    continue
                try:
                    idx = int(_input_safe(f"Select member [1-{len(members)}]: ").strip()) - 1
                    if not 0 <= idx < len(members):
                        raise ValueError
                except ValueError:
                    print("Invalid selection.")
                    continue
                m = members[idx]
                print(f"\nDelete {m['username']} (uid: {m['uid']}) and all their votes/guesses?")
                if confirm():
                    delete_member(token, room_code, m["uid"], m["username"])
                    members.pop(idx)
                    print("Done.")
            elif choice == "2":
                print(f"\nDelete room {room_code} and ALL its data (members, votes, guesses)?")
                if confirm():
                    delete_room(token, room_code)
                    print("Done.")
                    break


def menu_room(token):
    while True:
        print("\nRoom maintenance")
        print("  1) Manage specific room")
        print("  2) Purge rooms inactive for 60+ days")
        print("  3) Purge stale anonymous Auth users (90+ days)")
        print("  0) Back")
        choice = _input_safe("\nChoice: ").strip()
        if choice == "0":
            return
        elif choice == "1":
            _menu_room_manage(token)
        elif choice == "2":
            purge_stale_rooms(token)
        elif choice == "3":
            purge_stale_auth_users(token)


def main():
    if not SERVICE_ACCOUNT_KEY.exists():
        sys.exit(
            f"Error: {SERVICE_ACCOUNT_KEY} not found.\n"
            "Download it from: Firebase console → Project settings → "
            "Service accounts → Generate new private key"
        )

    print("Authenticating...", end=" ", flush=True)
    token = get_token()
    print("OK\n")

    while True:
        print("Main menu")
        print("  1) Room maintenance")
        print("  2) Database maintenance")
        print("  0) Quit")
        choice = _input_safe("\nChoice: ").strip()
        if choice == "0":
            print("Goodbye.")
            break
        elif choice == "1":
            menu_room(token)
        elif choice == "2":
            menu_database(token)
        print()


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\nAborted.")
        sys.exit(0)
