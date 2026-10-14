@file:OptIn(ExperimentalWasmJsInterop::class)

package org.neteinstein.couples.data.local

import kotlinx.coroutines.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.js.Promise

/**
 * Lazily opened handle to the web build's IndexedDB database - the browser's counterpart of the
 * SQLDelight `CoupleMomentsDatabase` Android and iOS get. It mirrors that schema: a `cards` object
 * store keyed on `id` (with a `languageCode` index standing in for Card.sq's `selectForLanguage`)
 * and a single-row `seed_metadata` store.
 *
 * Opening is attempted once per page load. IndexedDB can be missing or refuse to open (some
 * privacy modes, sandboxed iframes, a storage quota of zero), so [get] returns null in that case
 * rather than throwing, and the IndexedDB-backed DAOs fall back to the in-memory/`localStorage`
 * ones ([WebCardDao]/[WebSeedMetadataDao]) for the rest of the session.
 */
class IndexedDbDatabase(
    private val name: String = DATABASE_NAME,
) {
    private val mutex = Mutex()
    private var opened = false
    private var database: JsAny? = null

    suspend fun get(): JsAny? {
        if (opened) return database
        return mutex.withLock {
            if (!opened) {
                database = runCatching { idbOpen(name, DATABASE_VERSION).await<JsAny?>() }.getOrNull()
                opened = true
            }
            database
        }
    }

    private companion object {
        const val DATABASE_NAME = "couple_moments"

        // Bump alongside a matching branch in idbOpen's onupgradeneeded - the IndexedDB
        // equivalent of a numbered SQLDelight .sqm migration.
        const val DATABASE_VERSION = 1
    }
}

// --- JS interop -------------------------------------------------------------------------------
// Kotlin/Wasm's js() bodies must be the whole body of a top-level function, so every IndexedDB
// request is wrapped here as a Promise and awaited by the DAOs. A write resolves on the
// transaction's `complete` event (not the request's `success`), so it is durable once awaited.

internal fun idbOpen(
    name: String,
    version: Int,
): Promise<JsAny?> =
    js(
        """
        new Promise((resolve, reject) => {
            if (typeof indexedDB === 'undefined') {
                reject(new Error('IndexedDB is not available'));
                return;
            }
            const request = indexedDB.open(name, version);
            request.onupgradeneeded = () => {
                const db = request.result;
                if (!db.objectStoreNames.contains('cards')) {
                    const cards = db.createObjectStore('cards', { keyPath: 'id' });
                    cards.createIndex('languageCode', 'languageCode', { unique: false });
                }
                if (!db.objectStoreNames.contains('seed_metadata')) {
                    db.createObjectStore('seed_metadata', { keyPath: 'id' });
                }
            };
            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
            request.onblocked = () => reject(new Error('IndexedDB open blocked'));
        })
        """,
    )

internal fun idbCard(
    id: Int,
    text: String,
    languageCode: String,
    category: String,
    isHidden: Boolean,
    audience: String,
): JsAny = js("({ id, text, languageCode, category, isHidden, audience })")

internal fun idbCardId(card: JsAny): Int = js("card.id")

internal fun idbCardText(card: JsAny): String = js("card.text")

internal fun idbCardLanguageCode(card: JsAny): String = js("card.languageCode")

internal fun idbCardCategory(card: JsAny): String = js("card.category")

internal fun idbCardIsHidden(card: JsAny): Boolean = js("card.isHidden === true")

internal fun idbCardAudience(card: JsAny): String = js("card.audience ?? 'both'")

internal fun idbPutAll(
    db: JsAny,
    store: String,
    values: JsArray<JsAny>,
): Promise<JsAny?> =
    js(
        """
        new Promise((resolve, reject) => {
            const tx = db.transaction(store, 'readwrite');
            const objectStore = tx.objectStore(store);
            for (const value of values) objectStore.put(value);
            tx.oncomplete = () => resolve(null);
            tx.onerror = () => reject(tx.error);
            tx.onabort = () => reject(tx.error);
        })
        """,
    )

internal fun idbClear(
    db: JsAny,
    store: String,
): Promise<JsAny?> =
    js(
        """
        new Promise((resolve, reject) => {
            const tx = db.transaction(store, 'readwrite');
            tx.objectStore(store).clear();
            tx.oncomplete = () => resolve(null);
            tx.onerror = () => reject(tx.error);
            tx.onabort = () => reject(tx.error);
        })
        """,
    )

internal fun idbCardsForLanguage(
    db: JsAny,
    languageCode: String,
): Promise<JsArray<JsAny>> =
    js(
        """
        new Promise((resolve, reject) => {
            const request = db.transaction('cards', 'readonly')
                .objectStore('cards').index('languageCode').getAll(languageCode);
            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
        })
        """,
    )

internal fun idbHiddenCardIds(db: JsAny): Promise<JsArray<JsNumber>> =
    js(
        """
        new Promise((resolve, reject) => {
            const request = db.transaction('cards', 'readonly').objectStore('cards').getAll();
            request.onsuccess = () => resolve(request.result.filter((c) => c.isHidden === true).map((c) => c.id));
            request.onerror = () => reject(request.error);
        })
        """,
    )

internal fun idbMarkHidden(
    db: JsAny,
    cardId: Int,
): Promise<JsAny?> =
    js(
        """
        new Promise((resolve, reject) => {
            const tx = db.transaction('cards', 'readwrite');
            const store = tx.objectStore('cards');
            const request = store.get(cardId);
            request.onsuccess = () => {
                const card = request.result;
                if (card) {
                    card.isHidden = true;
                    store.put(card);
                }
            };
            tx.oncomplete = () => resolve(null);
            tx.onerror = () => reject(tx.error);
            tx.onabort = () => reject(tx.error);
        })
        """,
    )

internal fun idbResetAllHidden(db: JsAny): Promise<JsAny?> =
    js(
        """
        new Promise((resolve, reject) => {
            const tx = db.transaction('cards', 'readwrite');
            const request = tx.objectStore('cards').openCursor();
            request.onsuccess = () => {
                const cursor = request.result;
                if (!cursor) return;
                if (cursor.value.isHidden === true) {
                    const card = cursor.value;
                    card.isHidden = false;
                    cursor.update(card);
                }
                cursor.continue();
            };
            tx.oncomplete = () => resolve(null);
            tx.onerror = () => reject(tx.error);
            tx.onabort = () => reject(tx.error);
        })
        """,
    )

internal fun idbSeedVersion(
    db: JsAny,
    id: Int,
): Promise<JsNumber?> =
    js(
        """
        new Promise((resolve, reject) => {
            const request = db.transaction('seed_metadata', 'readonly').objectStore('seed_metadata').get(id);
            request.onsuccess = () => resolve(request.result ? request.result.version : null);
            request.onerror = () => reject(request.error);
        })
        """,
    )

internal fun idbSeedMetadata(
    id: Int,
    version: Int,
): JsAny = js("({ id, version })")
