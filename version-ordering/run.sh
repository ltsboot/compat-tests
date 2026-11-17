#!/usr/bin/env bash
# T-2.4 -- proof of version ordering (2.7.18.1 > 2.7.18) and of conflict resolution, in Docker.
# Exits non-zero if any assertion fails. Usage: ./run.sh   (needs Docker; uses docker/mvn.sh and docker/gradle.sh of the workspace)
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
WS="${LTS_WORKSPACE:-$(cd "$HERE/../../.." && pwd)}"
MVN="$WS/docker/mvn.sh"; GRADLE="$WS/docker/gradle.sh"
OUT="$HERE/out"; rm -rf "$OUT"; mkdir -p "$OUT"
FAIL=0; PASS=0
ok()   { echo "  PASS  $*"; PASS=$((PASS+1)); }
bad()  { echo "  FAIL  $*"; FAIL=$((FAIL+1)); }
# assert_eq <description> <expected> <actual>
assert_eq() { if [[ "$2" == "$3" ]]; then ok "$1 (= $3)"; else bad "$1: expected '$2', got '$3'"; fi; }

echo "== 0. fake repository (file://)"
python3 "$HERE/make-repo.py" >/dev/null && echo "  repo/ ready"

echo "== 1. pure ordering"
"$MVN" "$HERE" 17 -q -f ordering-maven/pom.xml compile exec:java >"$OUT/ordering-maven.log" 2>&1 || { bad "maven ordering run failed (see out/ordering-maven.log)"; tail -20 "$OUT/ordering-maven.log"; }
"$GRADLE" "$HERE" 17 -q -p ordering-gradle order >"$OUT/ordering-gradle.log" 2>&1 || { bad "gradle ordering run failed (see out/ordering-gradle.log)"; tail -20 "$OUT/ordering-gradle.log"; }
grep '^RESULT' "$OUT/ordering-maven.log" | sed 's/^RESULT //' >"$OUT/ordering-maven.txt"
grep '^RESULT' "$OUT/ordering-gradle.log" | sed 's/^RESULT //' >"$OUT/ordering-gradle.txt"
printf '%-34s | %-6s | %-6s | %s\n' "pair" "Maven" "Gradle" "required" | tee "$OUT/ordering-table.txt"
while read -r a b req; do
  [[ -z "${a:-}" || "$a" == \#* ]] && continue
  m=$(grep -F "$a " "$OUT/ordering-maven.txt" | awk -v a="$a" -v b="$b" '$1==a && $3==b {print $2}')
  g=$(grep -F "$a " "$OUT/ordering-gradle.txt" | awk -v a="$a" -v b="$b" '$1==a && $3==b {print $2}')
  printf '%-34s | %-6s | %-6s | %s\n' "$a ? $b" "$m" "$g" "$req" | tee -a "$OUT/ordering-table.txt"
  if [[ "$req" != "-" ]]; then
    assert_eq "maven:  $a $req $b" "$req" "$m"
    assert_eq "gradle: $a $req $b" "$req" "$g"
  fi
done <"$HERE/pairs.txt"

echo "== 2a. Maven conflict resolution (nearest-wins; BOM import)"
maven_lib() { # <pom file> -> resolved lib version
  "$MVN" "$HERE" 17 -q -f "conflict-maven/$1.xml" dependency:list -DincludeArtifactIds=lib -DoutputFile="/work/out/$1.txt" >"$OUT/$1.log" 2>&1 \
    || { echo "  (mvn failed for $1, see out/$1.log)" >&2; return; }
  grep -o 'org.example:lib:jar:[^:]*' "$OUT/$1.txt" | cut -d: -f4
}
assert_eq "maven   a,b  (a declared first)  -> nearest-wins keeps first"      "2.7.18"   "$(maven_lib app-ab)"
assert_eq "maven   b,a  (b declared first)  -> declaration order decides"     "2.7.18.1" "$(maven_lib app-ba)"
assert_eq "maven   a only (no BOM)"                                           "2.7.18"   "$(maven_lib app-a-only)"
assert_eq "maven   BOM 2.7.18.1 + a,b"                                        "2.7.18.1" "$(maven_lib app-bom-ab)"
assert_eq "maven   BOM 2.7.18.1 + b,a"                                        "2.7.18.1" "$(maven_lib app-bom-ba)"
assert_eq "maven   BOM 2.7.18.1 + a only (BOM forces up)"                     "2.7.18.1" "$(maven_lib app-bom-a-only)"

echo "== 2b. Gradle conflict resolution (highest version wins; platform())"
"$GRADLE" "$HERE" 17 -q -p conflict-gradle resolveAll >"$OUT/conflict-gradle.log" 2>&1 || { bad "gradle conflict run failed"; tail -20 "$OUT/conflict-gradle.log"; }
gl() { grep "^RESULT gradle $1 " "$OUT/conflict-gradle.log" | sed 's/.*lib=//'; }
assert_eq "gradle  a,b  (no platform) -> highest wins"                        "2.7.18.1" "$(gl abPlain)"
assert_eq "gradle  b,a  (no platform) -> highest wins, order irrelevant"      "2.7.18.1" "$(gl baPlain)"
assert_eq "gradle  a only (no platform)"                                      "2.7.18"   "$(gl aOnlyPlain)"
assert_eq "gradle  platform(BOM 2.7.18.1) + a,b"                              "2.7.18.1" "$(gl abPlatform)"
assert_eq "gradle  platform(BOM 2.7.18.1) + b,a"                              "2.7.18.1" "$(gl baPlatform)"
assert_eq "gradle  platform(BOM 2.7.18.1) + a only (platform forces up)"      "2.7.18.1" "$(gl aOnlyPlatform)"

echo; echo "== SUMMARY: $PASS passed, $FAIL failed"
[[ $FAIL -eq 0 ]]
