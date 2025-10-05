#!/usr/bin/env python3
"""
replace_imports.py

Usage:
  python scripts/replace_imports.py [--dry-run] [--apply] [--backup]

Description:
  Reads `neoforge_match.txt` for lines like:
    import <forge> -> <neoforge|None>
  Then walks `src` and replaces matching import lines in Java files.
  - If mapping target is `None`, it will add a TODO comment at top of file noting the unmatched import.
  - By default runs in --dry-run and prints proposed changes. Use --apply to write files.

"""
from __future__ import annotations

import argparse
import os
import re
import shutil
from pathlib import Path
from typing import Dict, List, Tuple


ROOT = Path(__file__).resolve().parents[1]
SRC_DIR = ROOT / "src"
MAPPING_FILE = ROOT / "neoforge_match.txt"


def load_mappings(path: Path) -> Dict[str, str]:
    """Load mappings from neoforge_match.txt.
    Returns dict: forge_import (full import string) -> target (neoforge full or 'None')
    """
    mapping: Dict[str, str] = {}
    if not path.exists():
        raise FileNotFoundError(f"Mapping file not found: {path}")
    with path.open(encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            # expected: import <forge> -> <neoforge|None>
            m = re.match(r"import\s+([^\s]+)\s*->\s*(\S+)", line)
            if not m:
                continue
            forge = m.group(1)
            target = m.group(2)
            mapping[forge] = target
    return mapping


def find_java_files(src_root: Path) -> List[Path]:
    files: List[Path] = []
    if not src_root.exists():
        return files
    for p in src_root.rglob("*.java"):
        files.append(p)
    return files


def process_file(path: Path, mappings: Dict[str, str]) -> Tuple[bool, List[str]]:
    """Process a single Java file. Returns (changed, diff_lines).
    diff_lines are human-readable descriptions of changes.
    """
    changed = False
    diffs: List[str] = []
    text = path.read_text(encoding="utf-8")
    lines = text.splitlines()

    # build a fast lookup for imports to replace
    import_pattern = re.compile(r"^\s*import\s+([^;]+);\s*$")

    new_lines = []
    todo_comments: List[str] = []
    for i, ln in enumerate(lines, start=1):
        m = import_pattern.match(ln)
        if m:
            fq = m.group(1).strip()
            if fq in mappings:
                target = mappings[fq]
                if target == 'None':
                    # leave original import but add TODO at top later
                    todo_comments.append(f"// TODO: no NeoForge equivalent for import {fq}")
                    diffs.append(f"{path}: line {i}: import {fq} -> None (annotate TODO)")
                    new_lines.append(ln)
                else:
                    # replace import line
                    new_ln = re.sub(re.escape(fq), target, ln)
                    new_lines.append(new_ln)
                    changed = True
                    diffs.append(f"{path}: line {i}: import {fq} -> {target}")
                continue
        new_lines.append(ln)

    if todo_comments:
        # if not already present, insert TODOs after package/imports header
        header_insertion_index = 0
        # skip package declaration
        for idx, ln in enumerate(new_lines):
            if ln.strip().startswith("package "):
                header_insertion_index = idx + 1
                break
        # avoid duplicate TODOs: only add those not present
        existing = "\n".join(new_lines)
        added_any = False
        for t in todo_comments:
            if t not in existing:
                new_lines.insert(header_insertion_index, t)
                header_insertion_index += 1
                added_any = True
        if added_any:
            changed = True

    if changed:
        return True, diffs
    return False, diffs


def apply_changes(file_changes: Dict[Path, str], backup: bool = True) -> None:
    for p, new_text in file_changes.items():
        if backup:
            bak = p.with_suffix(p.suffix + '.bak')
            shutil.copy2(p, bak)
        p.write_text(new_text, encoding="utf-8")


def main(argv=None):
    parser = argparse.ArgumentParser()
    parser.add_argument('--apply', action='store_true', help='Actually write files (default: dry-run)')
    parser.add_argument('--backup', action='store_true', help='Create .bak backups when applying')
    parser.add_argument('--src', type=str, default=str(SRC_DIR), help='Source root to scan')
    parser.add_argument('--map', type=str, default=str(MAPPING_FILE), help='Mapping file (neoforge_match.txt)')
    args = parser.parse_args(argv)

    mappings = load_mappings(Path(args.map))
    java_files = find_java_files(Path(args.src))
    if not java_files:
        print(f"No Java files found under {args.src}")
        return

    total_changed = 0
    file_changes: Dict[Path, str] = {}
    for jf in java_files:
        changed, diffs = process_file(jf, mappings)
        if diffs:
            for d in diffs:
                print(d)
        if changed:
            total_changed += 1
            # Re-read and produce modified text for writing
            # (we re-run the replacement to produce final text)
            original = jf.read_text(encoding='utf-8')
            new_text = original
            for forge, target in mappings.items():
                if target == 'None':
                    # inject TODO if not present
                    todo = f"// TODO: no NeoForge equivalent for import {forge}"
                    if todo not in new_text:
                        # insert after package declaration if any
                        new_text = re.sub(r"(package\s+[^;]+;\s*)", r"\1" + todo + "\n", new_text, count=1)
                else:
                    new_text = re.sub(r"(^\s*import\s+)" + re.escape(forge) + r"(\s*;\s*$)", r"\1" + target + r";", new_text, flags=re.MULTILINE)
            file_changes[jf] = new_text

    print(f"Scanned {len(java_files)} java files, {total_changed} files would be changed.")
    if args.apply and file_changes:
        apply_changes(file_changes, backup=args.backup)
        print(f"Applied changes to {len(file_changes)} files. Backups created: {args.backup}")
    else:
        print("Dry-run mode (no files written). Use --apply to write changes.")


if __name__ == '__main__':
    main()
