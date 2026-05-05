# -*- coding: utf-8 -*-
from docx import Document
import sys

doc = Document(r'd:\TLU\TTTN\A44216_DangBaHuy-BaoCaoTTTN.docx')
with open(r'c:\Users\Admin\Downloads\ecommerce-backend\docx_content.txt', 'w', encoding='utf-8') as f:
    for i, p in enumerate(doc.paragraphs):
        if p.text.strip():
            f.write(f"[{i}] {p.text}\n")
print("Done!")
