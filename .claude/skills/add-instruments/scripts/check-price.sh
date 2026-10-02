#!/usr/bin/env bash
# Usage: check-price.sh CDR.PL ETFSP500.PL CSPX.UK AAPL.US ...
# Mirrors the ACL of StooqPriceClient / FinnhubPriceClient to verify a symbol has a live price.
set -u
UA="Mozilla/5.0"
stooq() { curl -s -m 10 -A "$UA" "https://stooq.pl/q/l/?s=$1&f=sd2t2ohlcv&h=&e=csv"; }
for sym in "$@"; do
  ticker="${sym%.*}"; market="${sym##*.}"; lt=$(echo "$ticker" | tr '[:upper:]' '[:lower:]')
  case "$market" in
    PL) out=$(stooq "$lt"); grep -qE 'N/D|B/D' <<<"$out" && out=$(stooq "$lt.pl") ;;  # GPW ETFs need .pl
    UK) out=$(stooq "$lt.uk") ;;
    US) if [ -n "${FINNHUB_API_KEY:-}" ]; then
          out=$(curl -s -m 10 "https://finnhub.io/api/v1/quote?symbol=$ticker&token=$FINNHUB_API_KEY")
        else out="FINNHUB_API_KEY not set"; fi ;;
    DE) out="no price provider for DE market" ;;
    *)  out="unknown market suffix" ;;
  esac
  if grep -qiE '<html|<meta' <<<"$out"; then out="STOOQ ENDPOINT UNAVAILABLE (HTML response) - provider may be broken"; fi
  printf '%-14s %s\n' "$sym" "$(echo "$out" | tail -n +1 | tr '\n' ' ' | cut -c1-160)"
done
