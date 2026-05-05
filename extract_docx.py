# -*- coding: utf-8 -*-
from docx import Document
import sys

doc = Document(r'd:\TLU\TTTN\A44216_DangBaHuy-BaoCaoTTTN.docx')
for i, p in enumerate(doc.paragraphs):
    if p.text.strip():
        print(f"[{i}] {p.text}")
