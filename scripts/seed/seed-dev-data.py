#!/usr/bin/env python3
"""Seed reference, studio, person, and content dev data via HTTP (+ SQL for baselines)."""
from __future__ import annotations

import argparse
import json
import os
import subprocess
import sys
import urllib.error
import urllib.parse
import urllib.request
import uuid
from typing import Any

ACTOR = os.environ.get("SEED_ACTOR", "dev-seed")
GATEWAY = os.environ.get("GATEWAY_BASE", "http://localhost:8080").rstrip("/")
REFERENCE = os.environ.get("REFERENCE_SERVICE_URL", "http://localhost:8087").rstrip("/")
REPO_ROOT = os.environ.get("REPO_ROOT", ".")
ENV_DEV = os.environ.get("ENV_DEV", "env/.env.dev")
COMPOSE_FILE = os.path.join(REPO_ROOT, "docker/docker-compose.dev.yaml")

TAXONOMIES = f"{GATEWAY}/api/v1/taxonomies"
PLATFORMS = f"{GATEWAY}/api/v1/social-platforms"
STUDIOS = f"{GATEWAY}/api/v1/studios"
PERSONS = f"{GATEWAY}/api/v1/persons"
FILES = f"{GATEWAY}/api/v1/files"
# Trailing slash avoids servlet redirect (302) POST→GET that yields CONTENT_LISTED instead of CONTENT_CREATED.
CONTENT = f"{GATEWAY}/api/v1/content/"
PERSON_OWNER_SERVICE = "sbp-person-service"
PERSON_OWNER_ENTITY_TYPE = "person"
ENTITY_TAX = f"{REFERENCE}/internal/v1/entity-taxonomies"

SEED_TITLE_PREFIX = "[seed] "


def api(method: str, url: str, body: dict | None = None) -> dict:
    data = None
    headers = {"Content-Type": "application/json", "Accept": "application/json"}
    if body is not None:
        data = json.dumps(body).encode("utf-8")
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=120) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        payload = e.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"{method} {url} -> {e.code}: {payload}") from e


def data_list(resp: dict) -> list:
    d = resp.get("data")
    return d if isinstance(d, list) else []


def data_obj(resp: dict) -> dict:
    d = resp.get("data")
    return d if isinstance(d, dict) else {}


def find_slug(items: list, slug: str) -> dict | None:
    for item in items:
        if item.get("slug") == slug:
            return item
    return None


def ensure_taxonomy(t: dict, catalog: list) -> uuid.UUID:
    hit = find_slug(catalog, t["slug"])
    if hit:
        return uuid.UUID(hit["id"])
    body = {
        "type": t["type"],
        "name": t["name"],
        "slug": t["slug"],
        "description": t.get("description"),
        "active": True,
        "actor": ACTOR,
    }
    created = data_obj(api("POST", TAXONOMIES, body))
    print(f"  taxonomy {t['slug']} -> {created['id']}")
    return uuid.UUID(created["id"])


def ensure_platform(p: dict, catalog: list) -> uuid.UUID:
    hit = find_slug(catalog, p["slug"])
    if hit:
        return uuid.UUID(hit["id"])
    svg = p.get("svg") or p.get("svgIcon")
    body = {
        "name": p["name"],
        "slug": p["slug"],
        "baseUrl": p["baseUrl"],
        "svg": svg,
        "svgIcon": svg,
        "brandColor": p.get("brandColor"),
        "active": True,
        "actor": ACTOR,
    }
    created = data_obj(api("POST", PLATFORMS, body))
    print(f"  platform {p['slug']} -> {created['id']}")
    return uuid.UUID(created["id"])


def ensure_studio(s: dict, catalog: list) -> uuid.UUID:
    hit = find_slug(catalog, s["slug"])
    if hit:
        return uuid.UUID(hit["id"])
    body = {
        "name": s["name"],
        "slug": s["slug"],
        "website": s.get("website"),
        "description": s.get("description"),
        "svg": s.get("svg"),
        "actor": ACTOR,
    }
    created = data_obj(api("POST", STUDIOS, body))
    print(f"  studio {s['slug']} -> {created['id']}")
    return uuid.UUID(created["id"])


def ensure_lookup(base: str, row: dict) -> uuid.UUID:
    slug = row["slug"]
    encoded = urllib.parse.quote(slug, safe="")
    try:
        hit = data_obj(api("GET", f"{base}/by-slug/{encoded}"))
        return uuid.UUID(hit["id"])
    except RuntimeError:
        pass
    body = {
        "name": row["name"],
        "slug": slug,
        "description": row.get("description", ""),
        "actor": ACTOR,
    }
    created = data_obj(api("POST", base, body))
    print(f"  lookup {slug} -> {created['id']}")
    return uuid.UUID(created["id"])


def assign_entity_taxonomy(entity_id: uuid.UUID, entity_type: str, taxonomy_id: uuid.UUID) -> None:
    body = {"entityId": str(entity_id), "entityType": entity_type, "taxonomyId": str(taxonomy_id)}
    try:
        api("POST", ENTITY_TAX, body)
        print(f"  entity_taxonomy {entity_type} {entity_id} + {taxonomy_id}")
    except RuntimeError as e:
        if "409" in str(e) or "already" in str(e).lower():
            return
        raise


def docker_psql(service: str, db_key_prefix: str, sql: str) -> None:
    db_name = os.environ[f"{db_key_prefix}_DB_NAME"]
    db_user = os.environ[f"{db_key_prefix}_DB_USERNAME"]
    cmd = [
        "docker", "compose", "-p", "sbp-dev", "--env-file", ENV_DEV, "-f", COMPOSE_FILE,
        "exec", "-T", service, "psql", "-U", db_user, "-d", db_name,
    ]
    proc = subprocess.run(cmd, input=sql, capture_output=True, text=True)
    if proc.returncode != 0:
        raise RuntimeError(f"{service} psql failed: {proc.stderr or proc.stdout}")


def seed_reference() -> tuple[dict[str, uuid.UUID], dict[str, uuid.UUID]]:
    print("=== Reference: social platforms ===")
    platform_defs = [
        {
            "name": "X",
            "slug": "x",
            "baseUrl": "https://x.com",
            "brandColor": "#000000",
            "svg": '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="currentColor" d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"/></svg>',
        },
        {
            "name": "Instagram",
            "slug": "instagram",
            "baseUrl": "https://instagram.com",
            "brandColor": "#E4405F",
            "svg": '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path fill="currentColor" d="M7 2h10a5 5 0 0 1 5 5v10a5 5 0 0 1-5 5H7a5 5 0 0 1-5-5V7a5 5 0 0 1 5-5zm5 5a5 5 0 1 0 0 10 5 5 0 0 0 0-10zm6.5-.75a1.25 1.25 0 1 0 0 2.5 1.25 1.25 0 0 0 0-2.5z"/></svg>',
        },
        {
            "name": "OnlyFans",
            "slug": "onlyfans",
            "baseUrl": "https://onlyfans.com",
            "brandColor": "#00AFF0",
            "svg": '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10" fill="currentColor"/></svg>',
        },
    ]
    plat_catalog = data_list(api("GET", f"{PLATFORMS}?active=true"))
    platform_ids = {p["slug"]: ensure_platform(p, plat_catalog) for p in platform_defs}

    print("=== Reference: taxonomies (non-ROLE) ===")
    taxonomy_defs = [
        {"type": "CATEGORY", "name": "Studio Network", "slug": "studio-network", "description": "Major studio label"},
        {"type": "GENRE", "name": "Gonzo", "slug": "gonzo", "description": "Gonzo style production"},
        {"type": "GENRE", "name": "Feature", "slug": "feature", "description": "Feature-length production"},
        {"type": "GENRE", "name": "Reality", "slug": "reality", "description": "Reality-based production"},
        {"type": "TAG", "name": "Award Nominee", "slug": "award-nominee", "description": "Industry award nominee"},
        {"type": "TAG", "name": "Fan Favorite", "slug": "fan-favorite", "description": "High fan engagement"},
        {"type": "MOOD_TAG", "name": "Intense", "slug": "intense", "description": "High energy mood"},
        {"type": "MOOD_TAG", "name": "Romantic", "slug": "romantic", "description": "Romantic mood"},
        {"type": "SPECIALTIES", "name": "Contract Star", "slug": "contract-star", "description": "Exclusive contract talent"},
        {"type": "SPECIALTIES", "name": "Big Naturals", "slug": "big-naturals", "description": "Big naturals specialty"},
        {"type": "SPECIALTIES", "name": "Office Fantasy", "slug": "office-fantasy", "description": "Office roleplay specialty"},
        {"type": "SPECIALTIES", "name": "Massage Parlor", "slug": "massage-parlor", "description": "Massage scene specialty"},
        {"type": "SPECIALTIES", "name": "Reality Host", "slug": "reality-host", "description": "Reality-style hosting"},
        {"type": "SPECIALTIES", "name": "POV Specialist", "slug": "pov-specialist", "description": "POV-focused performer"},
    ]
    tax_catalog = data_list(api("GET", f"{TAXONOMIES}?active=true"))
    taxonomy_ids = {t["slug"]: ensure_taxonomy(t, tax_catalog) for t in taxonomy_defs}
    return platform_ids, taxonomy_ids


def seed_studios(taxonomy_ids: dict[str, uuid.UUID]) -> dict[str, uuid.UUID]:
    print("=== Studio: studios ===")
    studio_defs = [
        {
            "name": "Brazzers",
            "slug": "brazzers",
            "website": "https://www.brazzers.com",
            "description": "Flagship studio network (seed).",
            "svg": '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64"><rect width="64" height="64" rx="8" fill="#ffcc00"/><text x="32" y="38" text-anchor="middle" font-size="14" fill="#000">BZ</text></svg>',
            "taxonomySlugs": ["studio-network", "feature", "award-nominee"],
        },
        {
            "name": "Reality Kings",
            "slug": "reality-kings",
            "website": "https://www.realitykings.com",
            "description": "Reality-style studio brand (seed).",
            "svg": '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64"><rect width="64" height="64" rx="8" fill="#e63946"/><text x="32" y="38" text-anchor="middle" font-size="12" fill="#fff">RK</text></svg>',
            "taxonomySlugs": ["studio-network", "reality", "fan-favorite"],
        },
        {
            "name": "Digital Playground",
            "slug": "digital-playground",
            "website": "https://www.digitalplayground.com",
            "description": "Feature-focused studio (seed).",
            "taxonomySlugs": ["studio-network", "feature"],
        },
        {
            "name": "Vixen Media Group",
            "slug": "vixen-media-group",
            "website": "https://www.vixen.com",
            "description": "Premium cinematic studio group (seed).",
            "taxonomySlugs": ["studio-network", "feature", "intense"],
        },
        {
            "name": "Wicked Pictures",
            "slug": "wicked-pictures",
            "website": "https://www.wickedpictures.com",
            "description": "Legacy feature studio (seed).",
            "taxonomySlugs": ["studio-network", "feature", "romantic"],
        },
    ]
    catalog = data_list(api("GET", f"{STUDIOS}?active=true"))
    studio_ids: dict[str, uuid.UUID] = {}
    for s in studio_defs:
        sid = ensure_studio(s, catalog)
        studio_ids[s["slug"]] = sid
        for slug in s.get("taxonomySlugs", []):
            tid = taxonomy_ids.get(slug)
            if tid:
                assign_entity_taxonomy(sid, "STUDIO", tid)
    return studio_ids


def seed_person_lookups() -> tuple[uuid.UUID, dict[str, uuid.UUID]]:
    print("=== Person: body_types & relationship_types ===")
    body_types = [
        ("Slim", "slim", "Slender build"),
        ("Athletic", "athletic", "Toned build"),
        ("Average", "average", "Average build"),
        ("Curvy", "curvy", "Curvy build"),
        ("Petite", "petite", "Petite frame"),
        ("Plus Size", "plus-size", "Plus size build"),
    ]
    bt_ids: dict[str, uuid.UUID] = {}
    for name, slug, desc in body_types:
        bt_ids[slug] = ensure_lookup(f"{PERSONS}/body-types", {"name": name, "slug": slug, "description": desc})
    rel_id = ensure_lookup(
        f"{PERSONS}/relationship-types",
        {"name": "Exclusive", "slug": "exclusive", "description": "Primary studio contract (seed)"},
    )
    return rel_id, bt_ids


def get_role_taxonomy_ids() -> dict[str, uuid.UUID]:
    catalog = data_list(api("GET", f"{TAXONOMIES}?active=true"))
    roles = {}
    for slug in ("performer", "director", "producer", "model"):
        hit = find_slug([t for t in catalog if t.get("type") == "ROLE"], slug)
        if hit:
            roles[slug] = uuid.UUID(hit["id"])
    return roles


def register_file_reference(object_key: str, person_id: uuid.UUID, purpose: str) -> uuid.UUID:
    body = {
        "objectKey": object_key,
        "ownerService": PERSON_OWNER_SERVICE,
        "ownerEntityType": PERSON_OWNER_ENTITY_TYPE,
        "ownerEntityId": str(person_id),
        "purpose": purpose,
        "visibility": "PUBLIC",
        "metadata": {"seed": True, "source": "seed-dev-data.py"},
        "actor": ACTOR,
    }
    resp = api("POST", f"{FILES}/register-reference", body)
    if resp.get("code") not in ("FILE_REFERENCE_REGISTERED",):
        raise RuntimeError(f"Unexpected register-reference response: {resp.get('code')}")
    file_id = uuid.UUID(resp["data"]["fileId"])
    print(f"    file {purpose} -> {file_id} ({object_key})")
    return file_id


def sync_person_media(person_id: uuid.UUID, defn: dict) -> None:
    profile = defn.get("profileObjectKey")
    backdrop = defn.get("backdropObjectKey")
    if not profile and not backdrop:
        return
    profile_resp = data_obj(api("GET", f"{PERSONS}/{person_id}"))
    patch: dict[str, Any] = {"actor": ACTOR}
    if profile and not profile_resp.get("profileFileId"):
        patch["profileFileId"] = str(register_file_reference(profile, person_id, "profile"))
    if backdrop and not profile_resp.get("backdropFileId"):
        patch["backdropFileId"] = str(register_file_reference(backdrop, person_id, "backdrop"))
    if len(patch) > 1:
        api("PATCH", f"{PERSONS}/{person_id}", patch)


def person_by_slug(slug: str) -> dict | None:
    for row in data_list(api("GET", PERSONS)):
        if row.get("slug") == slug:
            return row
    return None


def find_existing_person(defn: dict) -> dict | None:
    """Match by current slug or optional legacy slug (rename / typo fixes)."""
    hit = person_by_slug(defn["slug"])
    if hit:
        return hit
    legacy = defn.get("legacySlug")
    if legacy:
        return person_by_slug(legacy)
    return None


def create_or_get_person(defn: dict, skip_if_exists: bool) -> tuple[uuid.UUID, bool]:
    if skip_if_exists:
        existing = find_existing_person(defn)
        if existing:
            pid = uuid.UUID(existing["id"])
            existing_slug = existing.get("slug")
            if existing_slug and existing_slug != defn["slug"]:
                print(f"  person rename {existing_slug} -> {defn['slug']} (pid {pid})")
                api(
                    "PUT",
                    f"{PERSONS}/{pid}",
                    {
                        "name": defn["name"],
                        "slug": defn["slug"],
                        "stageName": defn["stageName"],
                        "legalName": defn["legalName"],
                        "bio": defn["bio"],
                        "birthDate": defn["birthDate"],
                        "birthPlace": defn["birthPlace"],
                        "nationality": defn["nationality"],
                        "gender": defn["gender"],
                        "heightCm": defn["heightCm"],
                        "weightKg": defn["weightKg"],
                        "verified": defn["verified"],
                        "debutYear": defn["debutYear"],
                        "careerStatus": defn["careerStatus"],
                        "retiredYear": defn.get("retiredYear"),
                        "lastActiveYear": defn.get("lastActiveYear"),
                        "career": defn.get("career"),
                        "sourceMetadata": defn.get("sourceMetadata"),
                        "actor": ACTOR,
                    },
                )
            else:
                print(f"  person {defn['slug']} exists -> {pid}")
            sync_person_media(pid, defn)
            return pid, False
    body = {
        "name": defn["name"],
        "slug": defn["slug"],
        "stageName": defn["stageName"],
        "legalName": defn["legalName"],
        "bio": defn["bio"],
        "birthDate": defn["birthDate"],
        "birthPlace": defn["birthPlace"],
        "nationality": defn["nationality"],
        "gender": defn["gender"],
        "heightCm": defn["heightCm"],
        "weightKg": defn["weightKg"],
        "verified": defn["verified"],
        "debutYear": defn["debutYear"],
        "careerStatus": defn["careerStatus"],
        "retiredYear": defn.get("retiredYear"),
        "lastActiveYear": defn.get("lastActiveYear"),
        "career": defn.get("career"),
        "sourceMetadata": defn.get("sourceMetadata"),
        "actor": ACTOR,
    }
    resp = api("POST", PERSONS, body)
    if resp.get("code") != "PERSON_CREATED":
        raise RuntimeError(f"Unexpected create person response: {resp.get('code')}")
    pid = uuid.UUID(resp["data"]["id"])
    print(f"  created person {defn['name']} -> {pid}")
    sync_person_media(pid, defn)
    return pid, True


def safe_link(label: str, fn) -> None:
    try:
        fn()
        print(f"  {label}")
    except RuntimeError as e:
        if "409" in str(e) or "already" in str(e).lower() or "duplicate" in str(e).lower():
            return
        raise


def sync_person_career(person_id: uuid.UUID, defn: dict) -> None:
    career = defn.get("career")
    if not career:
        return
    safe_link("career sync", lambda: api("PATCH", f"{PERSONS}/{person_id}", {"career": career, "actor": ACTOR}))


def content_by_title(title: str) -> dict | None:
    for row in data_list(api("GET", CONTENT)):
        if row.get("title") == title:
            return row
    return None


def ensure_content(
    item: dict,
    studio_ids: dict[str, uuid.UUID],
    person_ids_by_slug: dict[str, uuid.UUID],
    taxonomy_ids: dict[str, uuid.UUID],
) -> uuid.UUID:
    title = item["title"]
    existing = content_by_title(title)
    if existing:
        print(f"  content exists {title} -> {existing['id']}")
        return uuid.UUID(existing["id"])

    studio_slug = item["studioSlug"]
    studio_id = studio_ids.get(studio_slug)
    if not studio_id:
        raise RuntimeError(f"Unknown studio slug for content: {studio_slug}")

    cast_ids = [str(person_ids_by_slug[s]) for s in item.get("castSlugs", []) if s in person_ids_by_slug]
    body = {
        "title": title,
        "tagline": item.get("tagline"),
        "overview": item.get("overview"),
        "adult": item.get("adult", True),
        "contentType": item.get("contentType", "SCENES"),
        "contentRating": item.get("contentRating", "RESTRICTED_ADULT_21_PLUS"),
        "status": item.get("status", "RELEASED"),
        "visibility": item.get("visibility", "PUBLIC"),
        "originalLanguage": item.get("originalLanguage", "en"),
        "originCountry": item.get("originCountry", ["US"]),
        "releaseDate": item.get("releaseDate"),
        "runtime": item.get("runtime"),
        "posterPath": item.get("posterPath"),
        "backdropPath": item.get("backdropPath"),
        "studioIds": [str(studio_id)],
        "castPersonIds": cast_ids,
        "tagIds": [str(taxonomy_ids[s]) for s in item.get("tagSlugs", []) if s in taxonomy_ids],
        "genreIds": [str(taxonomy_ids[s]) for s in item.get("genreSlugs", []) if s in taxonomy_ids],
        "moodTagIds": [str(taxonomy_ids[s]) for s in item.get("moodSlugs", []) if s in taxonomy_ids],
        "assets": item.get("assets", []),
    }
    resp = api("POST", CONTENT, body)
    if resp.get("code") != "CONTENT_CREATED":
        raise RuntimeError(f"Unexpected create content response: {resp.get('code')} for {title}")
    created = data_obj(resp)
    raw_id = created.get("id")
    if raw_id is None:
        raise RuntimeError(f"Content create missing id in data for {title}: keys={list(created.keys())}")
    cid = uuid.UUID(raw_id)
    print(f"  created content {title} -> {cid}")
    return cid


def seed_content(
    studio_ids: dict[str, uuid.UUID],
    person_ids_by_slug: dict[str, uuid.UUID],
    taxonomy_ids: dict[str, uuid.UUID],
) -> dict[str, uuid.UUID]:
    print("=== Content: catalog entries ===")
    title_to_id: dict[str, uuid.UUID] = {}
    for item in CONTENT_DEFS:
        cid = ensure_content(item, studio_ids, person_ids_by_slug, taxonomy_ids)
        title_to_id[item["title"]] = cid
    return title_to_id


def link_person(
    person_id: uuid.UUID,
    defn: dict,
    rel_id: uuid.UUID,
    bt_ids: dict[str, uuid.UUID],
    studio_ids: dict[str, uuid.UUID],
    platform_ids: dict[str, uuid.UUID],
    taxonomy_ids: dict[str, uuid.UUID],
    role_ids: dict[str, uuid.UUID],
    *,
    full_links: bool,
) -> None:
    for slug in defn.get("specialtySlugs", []):
        tid = taxonomy_ids.get(slug)
        if tid:
            safe_link(
                f"specialty {slug}",
                lambda tid=tid: api("POST", f"{PERSONS}/{person_id}/taxonomies", {"taxonomyId": str(tid)}),
            )
    if not full_links:
        return
    for slug in defn.get("categorySlugs", []):
        tid = taxonomy_ids.get(slug)
        if tid:
            safe_link(
                f"category {slug}",
                lambda tid=tid: api("POST", f"{PERSONS}/{person_id}/taxonomies", {"taxonomyId": str(tid)}),
            )
    for slug in defn.get("genreSlugs", []):
        tid = taxonomy_ids.get(slug)
        if tid:
            safe_link(
                f"genre {slug}",
                lambda tid=tid: api("POST", f"{PERSONS}/{person_id}/taxonomies", {"taxonomyId": str(tid)}),
            )
    for slug in defn.get("tagSlugs", []):
        tid = taxonomy_ids.get(slug)
        if tid:
            safe_link(
                f"tag {slug}",
                lambda tid=tid: api("POST", f"{PERSONS}/{person_id}/taxonomies", {"taxonomyId": str(tid)}),
            )
    for slug in defn.get("moodTagSlugs", []):
        tid = taxonomy_ids.get(slug)
        if tid:
            safe_link(
                f"mood {slug}",
                lambda tid=tid: api("POST", f"{PERSONS}/{person_id}/taxonomies", {"taxonomyId": str(tid)}),
            )
    for slug in defn.get("roleSlugs", []):
        tid = role_ids.get(slug)
        if tid:
            safe_link(
                f"role {slug}",
                lambda tid=tid: api("POST", f"{PERSONS}/{person_id}/taxonomies", {"taxonomyId": str(tid)}),
            )
    for slug in defn.get("studioSlugs", []):
        sid = studio_ids.get(slug)
        if sid:
            safe_link(
                f"studio {slug}",
                lambda sid=sid: api(
                    "POST",
                    f"{PERSONS}/{person_id}/studios",
                    {"studioId": str(sid), "relationshipTypeId": str(rel_id)},
                ),
            )
    for acct in defn.get("social", []):
        plat = platform_ids.get(acct["platformSlug"])
        if not plat:
            continue
        safe_link(
            f"social {acct['username']}",
            lambda plat=plat, acct=acct: api(
                "POST",
                f"{PERSONS}/{person_id}/social-accounts",
                {
                    "socialPlatformId": str(plat),
                    "username": acct["username"],
                    "verified": acct.get("verified", False),
                    "available": True,
                    "followersCount": acct.get("followersCount", 0),
                    "actor": ACTOR,
                },
            ),
        )
    physical = dict(defn.get("physical") or {})
    bt_slug = defn.get("bodyTypeSlug")
    if bt_slug and bt_slug in bt_ids:
        physical["bodyTypeId"] = str(bt_ids[bt_slug])
    if physical:
        safe_link("physical attributes", lambda: api("PUT", f"{PERSONS}/{person_id}/physical-attributes", physical))
    for alias in defn.get("aliases", []):
        safe_link(f"alias {alias}", lambda alias=alias: api("POST", f"{PERSONS}/{person_id}/aliases", {"alias": alias}))


PERSON_DEFS: list[dict[str, Any]] = [
    {
        "name": "Natasha Nice",
        "slug": "natasha-nice",
        "stageName": "Natasha Nice",
        "legalName": "Natasha Vorne",
        "bio": "American performer and director; AVN and XBIZ award nominee (seed profile).",
        "birthDate": "1988-07-28",
        "birthPlace": "Pasadena, California, USA",
        "nationality": "American",
        "gender": "FEMALE",
        "heightCm": 155,
        "weightKg": 54,
        "verified": True,
        "debutYear": 2008,
        "careerStatus": "ACTIVE",
        "lastActiveYear": 2025,
        "profileObjectKey": "/seed/persons/natasha-nice/profile.jpg",
        "backdropObjectKey": "/seed/persons/natasha-nice/backdrop.jpg",
        "sourceMetadata": {"seed": True, "source": "seed-dev-data.py", "dataset": "dev-adult-catalog"},
        "career": {
            "details": "Long-form features and independent production.",
            "highlights": ["AVN nominee", "XBIZ nominee"],
            "previousProfession": "Retail sales associate",
            "startYear": 2008,
        },
        "aliases": ["Natasha Vorne"],
        "roleSlugs": ["performer", "director"],
        "specialtySlugs": ["big-naturals", "office-fantasy", "contract-star"],
        "genreSlugs": ["gonzo", "feature"],
        "tagSlugs": ["award-nominee", "fan-favorite"],
        "featuredContentTitles": [
            f"{SEED_TITLE_PREFIX}Brazzers Exxxtra: After Hours",
            f"{SEED_TITLE_PREFIX}Brazzers: Big Tits at Work (seed)",
        ],
        "studioSlugs": ["brazzers", "reality-kings", "digital-playground"],
        "bodyTypeSlug": "curvy",
        "physical": {"bust": 36, "waist": 27, "hips": 38, "unit": "inches", "eyeColor": "Brown", "hairColor": "Brown"},
        "social": [
            {"platformSlug": "x", "username": "natasha_nice_seed", "verified": False, "followersCount": 500000},
            {"platformSlug": "instagram", "username": "natasha_nice_ig_seed", "verified": True, "followersCount": 800000},
            {"platformSlug": "onlyfans", "username": "natasha_nice_of_seed", "verified": True, "followersCount": 120000},
        ],
    },
    {
        "name": "Britney Amber",
        "slug": "britney-amber",
        "legacySlug": "brittany-amber",
        "stageName": "Britney Amber",
        "legalName": "Amber Victoria Rizzo",
        "bio": "American performer and director; long-running career (dev seed; public bios often list birth Nov 1986, Banning CA — verify independently).",
        "birthDate": "1986-11-10",
        "birthPlace": "Banning, California, USA",
        "nationality": "American",
        "gender": "FEMALE",
        "heightCm": 165,
        "weightKg": 55,
        "verified": True,
        "debutYear": 2008,
        "careerStatus": "ACTIVE",
        "lastActiveYear": 2025,
        "profileObjectKey": "/seed/persons/britney-amber/profile.jpg",
        "backdropObjectKey": "/seed/persons/britney-amber/backdrop.jpg",
        "sourceMetadata": {
            "seed": True,
            "source": "seed-dev-data.py",
            "dataset": "dev-adult-catalog",
            "displayNameNote": "Britney Amber (correct spelling); legacy slug brittany-amber may exist in old DBs",
        },
        "career": {
            "details": "Features, directing, and mentoring.",
            "highlights": ["Director credits", "Long-running career"],
            "previousProfession": "Hair stylist",
            "startYear": 2008,
        },
        "aliases": ["Britney Amber", "Brittany Amber"],
        "roleSlugs": ["performer", "director"],
        "specialtySlugs": ["massage-parlor", "contract-star", "pov-specialist"],
        "genreSlugs": ["feature"],
        "moodTagSlugs": ["intense"],
        "featuredContentTitles": [
            f"{SEED_TITLE_PREFIX}Brazzers: Dirty Masseur (seed)",
        ],
        "studioSlugs": ["brazzers", "wicked-pictures", "vixen-media-group"],
        "bodyTypeSlug": "athletic",
        "physical": {"bust": 34, "waist": 26, "hips": 36, "unit": "inches", "eyeColor": "Hazel", "hairColor": "Blonde"},
        "social": [
            {"platformSlug": "x", "username": "brittany_amber_seed", "verified": True, "followersCount": 650000},
            {"platformSlug": "instagram", "username": "brittany_amber_ig_seed", "verified": True, "followersCount": 400000},
        ],
    },
    {
        "name": "Koda Monroe",
        "slug": "koda-monroe",
        "stageName": "Koda Monroe",
        "legalName": "Koda Monroe",
        "bio": "American performer; rising catalog talent (seed profile).",
        "birthDate": "1999-03-15",
        "birthPlace": "Las Vegas, Nevada, USA",
        "nationality": "American",
        "gender": "FEMALE",
        "heightCm": 168,
        "weightKg": 55,
        "verified": False,
        "debutYear": 2021,
        "careerStatus": "ACTIVE",
        "lastActiveYear": 2025,
        "profileObjectKey": "/seed/persons/koda-monroe/profile.jpg",
        "backdropObjectKey": "/seed/persons/koda-monroe/backdrop.jpg",
        "sourceMetadata": {"seed": True, "source": "seed-dev-data.py", "dataset": "dev-adult-catalog"},
        "career": {
            "details": "Independent and network scenes.",
            "highlights": ["Rising fan favorite"],
            "previousProfession": "Barista and college student",
            "startYear": 2021,
        },
        "aliases": [],
        "roleSlugs": ["performer", "model"],
        "specialtySlugs": ["reality-host", "pov-specialist", "big-naturals"],
        "genreSlugs": ["reality"],
        "tagSlugs": ["fan-favorite"],
        "moodTagSlugs": ["romantic"],
        "featuredContentTitles": [
            f"{SEED_TITLE_PREFIX}Reality Kings: Money Talks (seed)",
            f"{SEED_TITLE_PREFIX}Reality Kings: We Live Together (seed)",
        ],
        "studioSlugs": ["reality-kings", "digital-playground"],
        "bodyTypeSlug": "petite",
        "physical": {"bust": 32, "waist": 24, "hips": 34, "unit": "inches", "eyeColor": "Blue", "hairColor": "Brown"},
        "social": [
            {"platformSlug": "x", "username": "koda_monroe_seed", "verified": False, "followersCount": 85000},
            {"platformSlug": "onlyfans", "username": "koda_monroe_of_seed", "verified": False, "followersCount": 42000},
        ],
    },
    {
        "name": "Kendra Lust",
        "slug": "kendra-lust",
        "stageName": "Kendra Lust",
        "legalName": "Michele Mason",
        "bio": "American performer and director; founder of Lust Cinema (seed profile).",
        "birthDate": "1978-09-18",
        "birthPlace": "Madison Heights, Michigan, USA",
        "nationality": "American",
        "gender": "FEMALE",
        "heightCm": 160,
        "weightKg": 56,
        "verified": True,
        "debutYear": 2012,
        "careerStatus": "ACTIVE",
        "lastActiveYear": 2025,
        "profileObjectKey": "/seed/persons/kendra-lust/profile.jpg",
        "backdropObjectKey": "/seed/persons/kendra-lust/backdrop.jpg",
        "sourceMetadata": {"seed": True, "source": "seed-dev-data.py", "dataset": "dev-adult-catalog"},
        "career": {
            "details": "Feature films and director-owned label.",
            "highlights": ["Founded Lust Cinema"],
            "previousProfession": "Nurse",
            "startYear": 2012,
        },
        "aliases": ["Michele Mason"],
        "roleSlugs": ["performer", "director", "producer"],
        "specialtySlugs": ["contract-star", "office-fantasy", "massage-parlor"],
        "genreSlugs": ["feature"],
        "tagSlugs": ["award-nominee"],
        "moodTagSlugs": ["intense"],
        "studioSlugs": ["brazzers", "vixen-media-group", "wicked-pictures"],
        "bodyTypeSlug": "curvy",
        "physical": {"bust": 34, "waist": 28, "hips": 36, "unit": "inches", "eyeColor": "Brown", "hairColor": "Brown"},
        "social": [
            {"platformSlug": "x", "username": "kendra_lust_seed", "verified": True, "followersCount": 900000},
            {"platformSlug": "instagram", "username": "kendra_lust_ig_seed", "verified": True, "followersCount": 1200000},
        ],
    },
]


CONTENT_DEFS: list[dict[str, Any]] = [
    {
        "title": f"{SEED_TITLE_PREFIX}Brazzers Exxxtra: After Hours",
        "tagline": "Office closes, scene opens.",
        "overview": "Seed scene in the Brazzers Exxxtra line: workplace fantasy with Natasha Nice (dev catalog only).",
        "contentType": "SCENES",
        "contentRating": "RESTRICTED_ADULT_21_PLUS",
        "status": "RELEASED",
        "visibility": "PUBLIC",
        "releaseDate": "2024-06-15",
        "runtime": 42,
        "posterPath": "/seed/content/brazzers-exxxtra-after-hours/poster.jpg",
        "backdropPath": "/seed/content/brazzers-exxxtra-after-hours/backdrop.jpg",
        "studioSlug": "brazzers",
        "castSlugs": ["natasha-nice"],
        "genreSlugs": ["gonzo", "feature"],
        "tagSlugs": ["award-nominee"],
        "moodSlugs": ["intense"],
        "assets": [
            {"type": "TRAILER", "keys": [{"value": "/seed/content/brazzers-exxxtra-after-hours/trailer.mp4"}]},
            {"type": "CLIP", "keys": [{"value": "/seed/content/brazzers-exxxtra-after-hours/clip-01.mp4"}]},
            {"type": "BTS", "keys": [{"value": "/seed/content/brazzers-exxxtra-after-hours/bts-stills.zip"}]},
        ],
    },
    {
        "title": f"{SEED_TITLE_PREFIX}Brazzers: Big Tits at Work (seed)",
        "tagline": "Corporate fantasy, Brazzers network.",
        "overview": "Seed episode inspired by the Big Tits at Work series; starring Natasha Nice (not an official release).",
        "contentType": "SCENES",
        "releaseDate": "2023-11-02",
        "runtime": 38,
        "posterPath": "/seed/content/brazzers-big-tits-at-work/poster.jpg",
        "backdropPath": "/seed/content/brazzers-big-tits-at-work/backdrop.jpg",
        "studioSlug": "brazzers",
        "castSlugs": ["natasha-nice", "britney-amber"],
        "genreSlugs": ["feature"],
        "tagSlugs": ["fan-favorite"],
        "moodSlugs": ["intense"],
        "assets": [
            {"type": "TEASER", "keys": [{"value": "/seed/content/brazzers-big-tits-at-work/teaser.mp4"}]},
            {"type": "TRAILER", "keys": [{"value": "/seed/content/brazzers-big-tits-at-work/trailer.mp4"}]},
        ],
    },
    {
        "title": f"{SEED_TITLE_PREFIX}Brazzers: Dirty Masseur (seed)",
        "tagline": "Massage table mayhem.",
        "overview": "Seed scene inspired by Brazzers Dirty Masseur branding with Britney Amber.",
        "contentType": "SCENES",
        "releaseDate": "2022-08-19",
        "runtime": 36,
        "posterPath": "/seed/content/brazzers-dirty-masseur/poster.jpg",
        "backdropPath": "/seed/content/brazzers-dirty-masseur/backdrop.jpg",
        "studioSlug": "brazzers",
        "castSlugs": ["britney-amber"],
        "genreSlugs": ["feature"],
        "tagSlugs": ["award-nominee"],
        "moodSlugs": ["romantic"],
        "assets": [
            {"type": "CLIP", "keys": [{"value": "/seed/content/brazzers-dirty-masseur/clip-main.mp4"}]},
        ],
    },
    {
        "title": f"{SEED_TITLE_PREFIX}Reality Kings: Money Talks (seed)",
        "tagline": "Cash for chaos on the street.",
        "overview": "Seed segment inspired by Reality Kings Money Talks; features Koda Monroe (dev catalog).",
        "contentType": "SCENES",
        "releaseDate": "2024-03-22",
        "runtime": 45,
        "posterPath": "/seed/content/rk-money-talks/poster.jpg",
        "backdropPath": "/seed/content/rk-money-talks/backdrop.jpg",
        "studioSlug": "reality-kings",
        "castSlugs": ["koda-monroe"],
        "genreSlugs": ["reality", "gonzo"],
        "tagSlugs": ["fan-favorite"],
        "moodSlugs": ["intense"],
        "assets": [
            {"type": "TRAILER", "keys": [{"value": "/seed/content/rk-money-talks/trailer.mp4"}]},
            {"type": "CLIP", "keys": [{"value": "/seed/content/rk-money-talks/clip-01.mp4"}]},
        ],
    },
    {
        "title": f"{SEED_TITLE_PREFIX}Reality Kings: We Live Together (seed)",
        "tagline": "Roommates, reality-style.",
        "overview": "Seed episode inspired by Reality Kings We Live Together; Koda Monroe and Natasha Nice.",
        "contentType": "SCENES",
        "releaseDate": "2025-01-10",
        "runtime": 40,
        "posterPath": "/seed/content/rk-we-live-together/poster.jpg",
        "backdropPath": "/seed/content/rk-we-live-together/backdrop.jpg",
        "studioSlug": "reality-kings",
        "castSlugs": ["koda-monroe", "natasha-nice"],
        "genreSlugs": ["reality"],
        "tagSlugs": ["fan-favorite"],
        "moodSlugs": ["romantic"],
        "assets": [
            {"type": "BTS", "keys": [{"value": "/seed/content/rk-we-live-together/bts.mp4"}]},
            {"type": "TEASER", "keys": [{"value": "/seed/content/rk-we-live-together/teaser.mp4"}]},
        ],
    },
]


def seed_sql_extras(
    person_defs: list[dict],
    person_ids_by_slug: dict[str, uuid.UUID],
    content_title_to_id: dict[str, uuid.UUID],
) -> None:
    print("=== SQL: baselines & person_featured_content ===")
    docker_psql(
        "studio-db",
        "STUDIO",
        """
INSERT INTO studio_baseline (external_id, display_name)
VALUES ('dev-seed-studio-baseline', 'Seed dev studio baseline')
ON CONFLICT (external_id) DO UPDATE SET display_name = EXCLUDED.display_name, updated_at = NOW();
""",
    )
    docker_psql(
        "person-db",
        "PERSON",
        """
INSERT INTO person_baseline (external_id, display_name)
VALUES ('dev-seed-person-baseline', 'Seed dev person baseline')
ON CONFLICT (external_id) DO UPDATE SET display_name = EXCLUDED.display_name, updated_at = NOW();
""",
    )
    for defn in person_defs:
        slug = defn["slug"]
        pid = person_ids_by_slug.get(slug)
        if not pid:
            continue
        for i, title in enumerate(defn.get("featuredContentTitles", [])):
            cid = content_title_to_id.get(title)
            if not cid:
                continue
            docker_psql(
                "person-db",
                "PERSON",
                f"""
INSERT INTO person_featured_content (person_id, content_id, sort_order)
VALUES ('{pid}', '{cid}', {i})
ON CONFLICT (person_id, content_id) DO UPDATE SET sort_order = EXCLUDED.sort_order;
""",
            )
    docker_psql(
        "person-db",
        "PERSON",
        """
UPDATE person_social_accounts
SET last_synced_at = NOW()
WHERE username LIKE '%\\_seed' ESCAPE '\\' AND last_synced_at IS NULL;
""",
    )
    print("  baselines + featured content + social last_synced_at upserted")


def reset_person_database() -> None:
    print("=== Truncate person DB (all rows) ===")
    docker_psql(
        "person-db",
        "PERSON",
        """
SET client_min_messages TO WARNING;
TRUNCATE TABLE
    person_social_accounts,
    person_aliases,
    person_studios,
    person_physical_attributes,
    persons,
    body_types,
    relationship_types
CASCADE;
""",
    )
    try:
        docker_psql(
            "reference-db",
            "REFERENCE",
            "DELETE FROM entity_taxonomies WHERE entity_type = 'PERSON';",
        )
    except RuntimeError as e:
        print(f"  warning: could not clear reference entity_taxonomies: {e}", file=sys.stderr)
    print("  person DB truncated")


def reset_seed_data() -> None:
    print("=== Reset seed-owned rows (dev only) ===")
    docker_psql(
        "person-db",
        "PERSON",
        """
DELETE FROM person_featured_content;
DELETE FROM person_social_accounts;
DELETE FROM person_aliases;
DELETE FROM person_studios;
DELETE FROM person_physical_attributes;
DELETE FROM person_career_highlights;
DELETE FROM persons WHERE created_by = 'dev-seed' OR slug IN ('natasha-nice','britney-amber','brittany-amber','koda-monroe','kendra-lust');
""",
    )
    docker_psql(
        "reference-db",
        "REFERENCE",
        "DELETE FROM entity_taxonomies WHERE entity_type IN ('PERSON','STUDIO');",
    )
    docker_psql(
        "studio-db",
        "STUDIO",
        """
DELETE FROM studios WHERE created_by = 'dev-seed'
   OR slug IN ('brazzers','reality-kings','digital-playground','vixen-media-group','wicked-pictures');
""",
    )
    docker_psql(
        "reference-db",
        "REFERENCE",
        """
DELETE FROM social_platforms WHERE created_by = 'dev-seed'
   OR slug IN ('x','instagram','onlyfans');
DELETE FROM taxonomies WHERE created_by = 'dev-seed';
""",
    )
    docker_psql(
        "content-db",
        "CONTENT",
        f"DELETE FROM contents WHERE title LIKE '{SEED_TITLE_PREFIX}%';",
    )


def main() -> int:
    parser = argparse.ArgumentParser(description="Seed reference, studio, person, and content dev catalog data.")
    parser.add_argument("--reset", action="store_true", help="Remove prior dev-seed rows then re-seed")
    parser.add_argument(
        "--reset-db",
        action="store_true",
        help="Truncate the person database (and PERSON taxonomy links) before seeding",
    )
    parser.add_argument(
        "--force",
        action="store_true",
        help="Re-apply taxonomies, studios, social, physical, and aliases for existing persons",
    )
    args = parser.parse_args()

    if args.reset_db:
        reset_person_database()
    elif args.reset:
        reset_seed_data()

    platform_ids, taxonomy_ids = seed_reference()
    studio_ids = seed_studios(taxonomy_ids)
    rel_id, bt_ids = seed_person_lookups()
    role_ids = get_role_taxonomy_ids()

    print("=== Person: profiles & links ===")
    person_ids_by_slug: dict[str, uuid.UUID] = {}
    for defn in PERSON_DEFS:
        pid, created = create_or_get_person(defn, skip_if_exists=True)
        person_ids_by_slug[defn["slug"]] = pid
        sync_person_career(pid, defn)
        full = created or args.force
        link_person(
            pid,
            defn,
            rel_id,
            bt_ids,
            studio_ids,
            platform_ids,
            taxonomy_ids,
            role_ids,
            full_links=full,
        )
        if not full:
            print(f"  skip full links for existing person {defn['slug']} (use --force for studios/social/etc.)")

    content_title_to_id = seed_content(studio_ids, person_ids_by_slug, taxonomy_ids)
    seed_sql_extras(PERSON_DEFS, person_ids_by_slug, content_title_to_id)
    print("\nDone. Requires gateway + reference, studio, person, and content services running.")
    print(f"  Persons: {GATEWAY}/api/v1/persons")
    print(f"  Content: {GATEWAY}/api/v1/content/")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except RuntimeError as e:
        print(f"Error: {e}", file=sys.stderr)
        raise SystemExit(1) from e
