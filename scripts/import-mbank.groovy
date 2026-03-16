#!/usr/bin/env groovy
/**
 * Automated smoke test and mBank CSV import script.
 *
 * Exercises all REST API endpoints in a logical order:
 *   Phase 1 — Clean-state verification (positions, accounts, portfolio empty)
 *   Phase 2 — Instrument catalog (masterdata present from Flyway)
 *   Phase 3 — Import mBank CSVs (Makler, IKE, IKZE)
 *   Phase 4 — Post-import verification (positions, accounts, portfolio populated)
 *   Phase 5 — Position detail & import session re-read
 *
 * Usage:
 *   groovy scripts/import-mbank.groovy
 *
 * Prerequisites:
 *   - Backend running on localhost:8080 (./dev.sh reset for clean state)
 *   - Database seeded with instrument catalog (Flyway migrations)
 */

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.transform.Field

// ---------------------------------------------------------------------------
// Configuration
// ---------------------------------------------------------------------------

@Field String API = "http://localhost:8080/api/v1"
@Field String CSV_DIR = "${System.getProperty('user.home')}/Downloads"

@Field List IMPORTS = [
    [file: "${System.getProperty('user.home')}/Downloads/mbank-makler.Csv", account: "mBank Makler"],
    [file: "${System.getProperty('user.home')}/Downloads/mbank-ike.Csv",    account: "mBank IKE"],
    [file: "${System.getProperty('user.home')}/Downloads/mbank-ikze.Csv",   account: "mBank IKZE"],
]

// mBank instrument name → catalog symbol (TICKER.MARKET format post-V12)
@Field Map MAPPING_TABLE = [
    // GPW stocks
    "06MAGNA"    : "06N.PL",
    "ALIOR"      : "ALR.PL",
    "APATOR"     : "APT.PL",
    "ASBIS"      : "ASB.PL",
    "ATREM"      : "ATR.PL",
    "BUDIMEX"    : "BDX.PL",
    "CDPROJEKT"  : "CDR.PL",
    "COGNOR"     : "COG.PL",
    "DBENERGY"   : "DBE.PL",
    "DEVELIA"    : "DVL.PL",
    "DIAG"       : "DIA.PL",
    "DINOPL"     : "DNP.PL",
    "EKOBOX-NC"  : "EBX.PL",
    "ELEKTROTI"  : "ELT.PL",
    "ELQ-NC"     : "ELQ.PL",
    "ENEA"       : "ENA.PL",
    "ERBUD"      : "ERB.PL",
    "ETFBTBSP"   : "ETFBTBSP.PL",
    "FEERUM"     : "FEE.PL",
    "FERRO"      : "FRO.PL",
    "GENOMTEC"   : "GMT.PL",
    "GRUPAAZOTY" : "ATT.PL",
    "INGBSK"     : "ING.PL",
    "JWWINVEST"  : "JWW.PL",
    "KOGENERA"   : "KGN.PL",
    "MABION"     : "MAB.PL",
    "MBANK"      : "MBK.PL",
    "MEDINICE"   : "ICE.PL",
    "MILLENNIUM" : "MIL.PL",
    "MIRBUD"     : "MRB.PL",
    "MLSYSTEM"   : "MLS.PL",
    "MOBRUK"     : "MBR.PL",
    "MODIVO"     : "MDV.PL",
    "ONDE"       : "OND.PL",
    "OTLOG"      : "OTS.PL",
    "PEKAO"      : "PEO.PL",
    "PEPCO"      : "PCO.PL",
    "PKNORLEN"   : "PKN.PL",
    "PKOBP"      : "PKO.PL",
    "PKPCARGO"   : "PKP.PL",
    "POLIMEXMS"  : "PXM.PL",
    "RAINBOW"    : "RBW.PL",
    "ROCCA-NC"   : "RCA.PL",
    "SANPL"      : "SPL.PL",
    "STALEXP"    : "STX.PL",
    "SUNEX"      : "SNX.PL",
    "SYGNITY"    : "SGN.PL",
    "TORPOL"     : "TOR.PL",
    "TRAKCJA"    : "TRK.PL",
    "VOTUM"      : "VOT.PL",
    "VRG"        : "VRG.PL",
    "WITTCHEN"   : "WTN.PL",
    "XTB"        : "XTB.PL",
    "XTPL"       : "XTP.PL",
    "ZUE"        : "ZUE.PL",
    // GlobalConnect (international stocks on GPW, traded in PLN)
    "ALIBABA GRP"  : "BABA.US",
    "ASML HOLDING" : "ASML.PL",
    "APPLIED MATE" : "AMAT.PL",
    // LSE ETFs
    "EMIM LN ETF"  : "EMIM.UK",
    "SWDA LN ETF"  : "SWDA.UK",
    "ECAR LN ETF"  : "ECAR.UK",
    "RBOT LN ETF"  : "RBOT.UK",
    "USPY LN ETF"  : "USPY.UK",
    "FXC LN ETF"   : "FXC.UK",
    "NDIA LN ETF"  : "NDIA.UK",
    "CRUD LN ETF"  : "CRUD.UK",
    // XETRA ETFs
    "NQSE GR ETF"  : "NQSE.DE",
    "DTLE GR ETF"  : "DTLE.DE",
    // GPW ETFs (additional)
    "ETFBSPXPL"    : "ETFBSPXPL.PL",
]

// Stooq ticker overrides — when our TICKER.MARKET doesn't match Stooq's format
@Field Map STOOQ_TICKER_OVERRIDES = [
    "DTLE.DE"  : "DTLE.UK",   // Stooq lists this under .UK, not .DE
]

// Fallback prices (2026-03-16) for instruments where Stooq lookup might fail
// Sources: Stooq, Yahoo Finance, Hargreaves Lansdown, JustETF, Investing.com
@Field Map FALLBACK_PRICES = [
    "ETFBSPXPL.PL" : "116.26",   // PLN — Stooq
    "DTLE.DE"      : "2.93",     // EUR — IUSV on XETRA (JustETF/boerse-frankfurt)
    "NQSE.DE"      : "14.30",    // EUR — Stooq
    "EMIM.UK"      : "35.69",    // GBP — Stooq 3569 pence → 35.69
    "SWDA.UK"      : "96.62",    // GBP — HL 9662p → 96.62
    "ECAR.UK"      : "9.57",     // USD — Stooq/CNBC
    "RBOT.UK"      : "15.92",    // USD — Yahoo Finance
    "USPY.UK"      : "30.25",    // USD — HL (L&G Cyber Security)
    "FXC.UK"       : "79.66",    // GBP — Stooq 7966 pence → 79.66 (iShares China Large Cap)
    "NDIA.UK"      : "8.55",     // USD — iShares NAV
    "CRUD.UK"      : "14.25",    // USD — Stooq (WisdomTree Brent Crude Oil)
]

// ---------------------------------------------------------------------------
// Price fetching (Stooq ad-hoc)
// ---------------------------------------------------------------------------

String fetchStooqPrice(String symbol) {
    def stooqSymbol = STOOQ_TICKER_OVERRIDES.getOrDefault(symbol, symbol).toLowerCase()
    def url = "https://stooq.com/q/l/?s=${stooqSymbol}&f=sd2t2ohlcv&d=l&e=csv"
    try {
        def csv = new URL(url).text.trim()
        def fields = csv.split(",")
        // fields: Symbol, Date, Time, Open, High, Low, Close, Volume
        if (fields.length >= 7 && fields[6] != "N/D") {
            return fields[6]
        }
    } catch (Exception e) {
        println "      WARNING: Stooq fetch failed for ${symbol}: ${e.message}"
    }
    return null
}

// ---------------------------------------------------------------------------
// Test infrastructure
// ---------------------------------------------------------------------------

@Field JsonSlurper jsonParser = new JsonSlurper()
@Field int passed = 0
@Field int failed = 0
@Field List completedImportSessionIds = []

void check(String name, Closure test) {
    try {
        test()
        println "  PASS  ${name}"
        passed++
    } catch (AssertionError e) {
        println "  FAIL  ${name}"
        println "        ${e.message}"
        failed++
    } catch (Exception e) {
        println "  FAIL  ${name}"
        println "        ${e.class.simpleName}: ${e.message}"
        failed++
    }
}

// ---------------------------------------------------------------------------
// HTTP helpers
// ---------------------------------------------------------------------------

Map getJson(String url) {
    def conn = new URL(url).openConnection() as HttpURLConnection
    conn.requestMethod = "GET"
    conn.connectTimeout = 5000
    conn.readTimeout = 10000
    def code = conn.responseCode
    def body = (code >= 200 && code < 300)
        ? conn.inputStream.text
        : conn.errorStream?.text ?: "No response body"
    return [code: code, body: jsonParser.parseText(body)]
}

Map postJson(String url, Object payload) {
    def conn = new URL(url).openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.doOutput = true
    conn.connectTimeout = 5000
    conn.readTimeout = 10000
    conn.setRequestProperty("Content-Type", "application/json")
    conn.outputStream.write(JsonOutput.toJson(payload).getBytes("UTF-8"))
    def code = conn.responseCode
    def body = (code >= 200 && code < 300)
        ? conn.inputStream.text
        : conn.errorStream?.text ?: "No response body"
    return [code: code, body: jsonParser.parseText(body)]
}

Map uploadCsv(String baseUrl, String filePath, String accountName) {
    def file = new File(filePath)
    if (!file.exists()) return [code: -1, body: [error: "File not found: ${filePath}"]]

    def boundary = "----FormBoundary${System.currentTimeMillis()}"
    def conn = new URL(baseUrl).openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.doOutput = true
    conn.connectTimeout = 5000
    conn.readTimeout = 30000
    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=${boundary}")

    conn.outputStream.withWriter("UTF-8") { writer ->
        writer << "--${boundary}\r\n"
        writer << "Content-Disposition: form-data; name=\"broker\"\r\n\r\n"
        writer << "mBank\r\n"
        writer << "--${boundary}\r\n"
        writer << "Content-Disposition: form-data; name=\"accountName\"\r\n\r\n"
        writer << "${accountName}\r\n"
        writer << "--${boundary}\r\n"
        writer << "Content-Disposition: form-data; name=\"file\"; filename=\"${file.name}\"\r\n"
        writer << "Content-Type: text/csv\r\n\r\n"
        writer.flush()
        conn.outputStream << file.bytes
        conn.outputStream.flush()
        writer << "\r\n--${boundary}--\r\n"
    }

    def code = conn.responseCode
    def body = (code >= 200 && code < 300)
        ? conn.inputStream.text
        : conn.errorStream?.text ?: "No response body"
    return [code: code, body: jsonParser.parseText(body)]
}

// ---------------------------------------------------------------------------
// Import flow helper
// ---------------------------------------------------------------------------

Map processImport(Map importDef) {
    def filePath = importDef.file
    def accountName = importDef.account

    println "    Importing: ${accountName}"

    // Step 1: Upload
    def result = uploadCsv("${API}/imports", filePath, accountName)
    if (result.code < 200 || result.code >= 300) {
        println "      Upload failed (HTTP ${result.code}): ${result.body}"
        return null
    }
    def session = result.body
    def sessionId = session.importSessionId
    def status = session.status
    println "      Session ${sessionId}: ${status} " +
            "(${session.summary?.totalTransactions} txns, " +
            "${session.summary?.matchedInstruments} matched, " +
            "${session.summary?.unmatchedInstruments} unmatched)"

    // Step 2: Handle unmatched instruments
    if (status == "PENDING_REVIEW") {
        def unmatched = session.summary?.unmatchedDetails ?: []
        def mappings = []
        def unknowns = []

        unmatched.each { item ->
            def catalogSymbol = MAPPING_TABLE[item.brokerName]
            if (catalogSymbol) {
                mappings << [brokerName: item.brokerName, catalogSymbol: catalogSymbol]
            } else {
                unknowns << item.brokerName
            }
        }

        if (!unknowns.isEmpty()) {
            println "      WARNING: unmapped instruments: ${unknowns}"
        }
        if (!mappings.isEmpty()) {
            println "      Confirming with ${mappings.size()} mapping(s)..."
        }

        result = postJson("${API}/imports/${sessionId}/confirm", [mappings: mappings])
        if (result.code < 200 || result.code >= 300) {
            println "      Confirm failed (HTTP ${result.code}): ${result.body}"
            return null
        }
        session = result.body
        status = session.status
        println "      After confirm: ${status}"
    }

    // Step 3: Handle pending prices — fetch real prices from Stooq
    if (status == "PENDING_PRICES") {
        def needingPrices = session.summary?.instrumentsNeedingPrices ?: []
        if (!needingPrices.isEmpty()) {
            println "      Fetching real prices from Stooq for ${needingPrices.size()} instrument(s)..."
            def prices = needingPrices.collect { instr ->
                def realPrice = fetchStooqPrice(instr.symbol)
                if (realPrice) {
                    println "        ${instr.symbol}: ${realPrice} ${instr.currency} (Stooq)"
                } else {
                    realPrice = FALLBACK_PRICES[instr.symbol]
                    if (realPrice) {
                        println "        ${instr.symbol}: ${realPrice} ${instr.currency} (fallback)"
                    } else {
                        println "        ${instr.symbol}: NO PRICE AVAILABLE — skipping"
                        return null
                    }
                }
                [symbol: instr.symbol, price: realPrice, currency: instr.currency]
            }.findAll { it != null }
            result = postJson("${API}/imports/${sessionId}/prices", [prices: prices])
            if (result.code < 200 || result.code >= 300) {
                println "      Prices failed (HTTP ${result.code}): ${result.body}"
                return null
            }
            session = result.body
            status = session.status
            println "      After prices: ${status}"
        }
    }

    return session
}

// ---------------------------------------------------------------------------
// Main
// ---------------------------------------------------------------------------

println ""
println "=" * 60
println " Investment Tracker — API Smoke Test"
println "=" * 60
println ""

// Connectivity check
try {
    def conn = new URL("${API}/positions").openConnection() as HttpURLConnection
    conn.requestMethod = "GET"
    conn.connectTimeout = 3000
    conn.responseCode
} catch (Exception e) {
    println "ERROR: Backend not reachable at ${API}"
    println "Start the app first: ./dev.sh start (or ./dev.sh reset for clean state)"
    System.exit(1)
}

// ===== Phase 1: Clean-state verification =====
println "Phase 1: Clean-state verification"
println "-" * 40

check("GET /positions — empty on clean DB") {
    def r = getJson("${API}/positions")
    assert r.code == 200
    assert r.body.positions != null
    assert r.body.positions.size() == 0 : "Expected 0 positions, got ${r.body.positions.size()}"
    assert r.body.totalCount == 0
}

check("GET /accounts — empty on clean DB") {
    def r = getJson("${API}/accounts")
    assert r.code == 200
    assert r.body.accounts != null
    assert r.body.accounts.size() == 0 : "Expected 0 accounts, got ${r.body.accounts.size()}"
}

check("GET /portfolio — empty on clean DB") {
    def r = getJson("${API}/portfolio")
    assert r.code == 200
    assert r.body.positionsCount == 0
}

println ""

// ===== Phase 2: Instrument catalog =====
println "Phase 2: Instrument catalog (Flyway masterdata)"
println "-" * 40

check("GET /instruments?q=CDR.PL — catalog has Flyway seeds") {
    def r = getJson("${API}/instruments?q=CDR.PL")
    assert r.code == 200
    assert r.body.instruments != null
    assert r.body.totalCount > 0 : "Expected instruments from Flyway seeds"
    println "        (found ${r.body.totalCount} result(s) for CDR.PL)"
}

check("GET /instruments?q=CDR — search by ticker") {
    def r = getJson("${API}/instruments?q=CDR")
    assert r.code == 200
    assert r.body.instruments.size() > 0 : "Expected CDR.PL in results"
    assert r.body.instruments.any { it.symbol == "CDR.PL" } : "CDR.PL not found in search results"
}

check("GET /instruments?q=EMIM — search finds foreign instrument") {
    def r = getJson("${API}/instruments?q=EMIM")
    assert r.code == 200
    assert r.body.instruments.any { it.symbol == "EMIM.UK" } : "EMIM.UK not found"
}

println ""

// ===== Phase 3: Import mBank CSVs =====
println "Phase 3: Import mBank CSVs"
println "-" * 40

IMPORTS.each { importDef ->
    check("Import ${importDef.account}") {
        def session = processImport(importDef)
        assert session != null : "Import returned null — check errors above"
        assert session.status == "COMPLETED" : "Expected COMPLETED, got ${session.status}"
        completedImportSessionIds << session.importSessionId
    }
}

println ""

// ===== Phase 4: Post-import verification =====
println "Phase 4: Post-import verification"
println "-" * 40

check("GET /positions — positions created after import") {
    def r = getJson("${API}/positions")
    assert r.code == 200
    assert r.body.positions.size() > 0 : "Expected positions after import"
    assert r.body.totalCount > 0
    println "        (${r.body.totalCount} positions)"
}

check("GET /accounts — accounts created after import") {
    def r = getJson("${API}/accounts")
    assert r.code == 200
    assert r.body.accounts.size() > 0 : "Expected accounts after import"
    def accountNames = r.body.accounts.collect { it.name }
    println "        (${accountNames})"
}

check("GET /portfolio — portfolio has data after import") {
    def r = getJson("${API}/portfolio")
    assert r.code == 200
    assert r.body.positionsCount > 0 : "Expected positionsCount > 0"
    println "        (${r.body.positionsCount} positions in portfolio)"
}

println ""

// ===== Phase 5: Detail endpoints =====
println "Phase 5: Detail endpoints"
println "-" * 40

check("GET /positions/{symbol} — position detail for a known position") {
    def listResult = getJson("${API}/positions")
    assert listResult.body.positions.size() > 0
    def firstSymbol = listResult.body.positions[0].instrumentSymbol
    def r = getJson("${API}/positions/${firstSymbol}")
    assert r.code == 200
    assert r.body.instrumentSymbol == firstSymbol
    println "        (${firstSymbol}: ${r.body.holdings?.size() ?: 0} holding(s))"
}

check("GET /accounts/{id} — account detail for first account") {
    def listResult = getJson("${API}/accounts")
    assert listResult.body.accounts.size() > 0
    def firstId = listResult.body.accounts[0].id
    def r = getJson("${API}/accounts/${firstId}")
    assert r.code == 200
    assert r.body.id == firstId
    println "        (account: ${r.body.name})"
}

if (completedImportSessionIds) {
    check("GET /imports/{id} — re-read completed import session") {
        def sessionId = completedImportSessionIds[0]
        def r = getJson("${API}/imports/${sessionId}")
        assert r.code == 200
        assert r.body.status == "COMPLETED"
        assert r.body.importSessionId == sessionId
        println "        (session ${sessionId}: ${r.body.broker} / ${r.body.accountName})"
    }
}

println ""

// ===== Phase 6: Error handling =====
println "Phase 6: Error handling"
println "-" * 40

check("GET /positions/NONEXISTENT.XX — 404 for unknown position") {
    def r = getJson("${API}/positions/NONEXISTENT.XX")
    assert r.code == 404 || r.code == 400 : "Expected 404 or 400, got ${r.code}"
}

check("GET /imports/00000000-0000-0000-0000-000000000000 — 404 for unknown session") {
    def r = getJson("${API}/imports/00000000-0000-0000-0000-000000000000")
    assert r.code == 404 || r.code == 400 : "Expected 404 or 400, got ${r.code}"
}

println ""

// ===== Summary =====
println "=" * 60
def total = passed + failed
println " Results: ${passed}/${total} passed, ${failed} failed"
println "=" * 60

if (failed > 0) {
    System.exit(1)
}
