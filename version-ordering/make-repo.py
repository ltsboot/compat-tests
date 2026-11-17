#!/usr/bin/env python3
"""Generates the fake file:// Maven repository in ./repo (minimal jars + poms)."""
import os, zipfile, sys
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "repo")
G = "org.example"

def pom(a, v, deps=(), bom_versions=None, packaging="jar"):
    d = "".join(f"<dependency><groupId>{G}</groupId><artifactId>{x}</artifactId><version>{y}</version></dependency>" for x, y in deps)
    dm = ""
    if bom_versions:
        dm = "<dependencyManagement><dependencies>" + "".join(
            f"<dependency><groupId>{G}</groupId><artifactId>{x}</artifactId><version>{y}</version></dependency>" for x, y in bom_versions) + "</dependencies></dependencyManagement>"
    return (f'<?xml version="1.0" encoding="UTF-8"?>\n<project xmlns="http://maven.apache.org/POM/4.0.0">'
            f"<modelVersion>4.0.0</modelVersion><groupId>{G}</groupId><artifactId>{a}</artifactId><version>{v}</version>"
            f"<packaging>{packaging}</packaging>{dm}" + (f"<dependencies>{d}</dependencies>" if d else "") + "</project>\n")

def put(a, v, deps=(), bom_versions=None, jar=True):
    d = os.path.join(ROOT, *G.split("."), a, v)
    os.makedirs(d, exist_ok=True)
    open(os.path.join(d, f"{a}-{v}.pom"), "w").write(pom(a, v, deps, bom_versions, "jar" if jar else "pom"))
    if jar:
        with zipfile.ZipFile(os.path.join(d, f"{a}-{v}.jar"), "w") as z:
            z.writestr("META-INF/MANIFEST.MF", f"Manifest-Version: 1.0\nImplementation-Version: {v}\n")

put("lib", "2.7.18"); put("lib", "2.7.18.1")
put("a", "1.0", [("lib", "2.7.18")])
put("b", "1.0", [("lib", "2.7.18.1")])
put("lib-bom", "2.7.18.1", bom_versions=[("lib", "2.7.18.1")], jar=False)
print("repo generated in", ROOT)
