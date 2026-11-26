"""Genera la mascota Axo (VectorDrawable + SVG de vista previa) en 4 estados de ánimo."""
import sys, os
res, prev = sys.argv[1], sys.argv[2]

def ell(cx, cy, rx, ry):
    return f"M{cx-rx},{cy} a{rx},{ry} 0 1,0 {2*rx},0 a{rx},{ry} 0 1,0 {-2*rx},0 z"

GILL, HEAD, BELLY, CHEEK, INK, MOUTH = "#EE6F9C", "#FBB9D0", "#FDD7E5", "#F27FA8", "#3A2230", "#7A2E4A"
# (forma, datos): forma = fill | stroke | gill(rotación)
base = []
for cx, cy, rot in [(22, 42, -35), (15, 58, 0), (22, 74, 35), (98, 42, 35), (105, 58, 0), (98, 74, -35)]:
    base.append(("gill", ell(cx, cy, 13, 5.5), GILL, (rot, cx, cy)))
base += [("fill", ell(60, 64, 40, 34), HEAD), ("fill", ell(60, 80, 24, 13), BELLY),
         ("fill", ell(35, 75, 6.5, 5), CHEEK), ("fill", ell(85, 75, 6.5, 5), CHEEK)]
ojos = [("fill", ell(44, 61, 5.5, 6), INK), ("fill", ell(76, 61, 5.5, 6), INK),
        ("fill", ell(46, 58.5, 1.9, 1.9), "#FFFFFF"), ("fill", ell(78, 58.5, 1.9, 1.9), "#FFFFFF")]
sonrisa = [("stroke", "M53,74 q7,6 14,0", MOUTH, 2.8)]

moods = {
    "feliz": base + [("stroke", "M37,62 q7,-8 14,0", INK, 3.4), ("stroke", "M69,62 q7,-8 14,0", INK, 3.4),
                     ("fill", "M49,72 q11,14 22,0 z", MOUTH),
                     ("fill", "M101,22 l2,5 5,2 -5,2 -2,5 -2,-5 -5,-2 5,-2 z", "#FFC94D"),
                     ("fill", "M16,18 l1.5,3.5 3.5,1.5 -3.5,1.5 -1.5,3.5 -1.5,-3.5 -3.5,-1.5 3.5,-1.5 z", "#FFC94D")],
    "tranqui": base + ojos + sonrisa,
    "preocupado": base + ojos + [("stroke", "M37,51 L49,47", INK, 2.8), ("stroke", "M71,47 L83,51", INK, 2.8),
                                 ("stroke", "M51,77 q4.5,-4 9,0 q4.5,4 9,0", MOUTH, 2.8),
                                 ("fill", "M95,30 q6,9 0,13 q-6,-4 0,-13 z", "#7EC8F2")],
    "estudiando": base + ojos + sonrisa + [
        ("stroke", ell(44, 61, 10, 10), "#5B3A4A", 2.4), ("stroke", ell(76, 61, 10, 10), "#5B3A4A", 2.4),
        ("stroke", "M54,60 L66,60", "#5B3A4A", 2.4),
        ("fill", "M34,94 L60,99 L86,94 L86,113 L60,118 L34,113 z", "#6C8CFF"),
        ("stroke", "M60,99 L60,118", "#FFFFFF", 2)],
}

for name, shapes in moods.items():
    vd = ['<?xml version="1.0" encoding="utf-8"?>',
          '<!-- Axo, mascota de Gradify. Generado por docs/assets/axo/gen.py: no editar a mano. -->',
          '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
          '    android:width="120dp" android:height="120dp"',
          '    android:viewportWidth="120" android:viewportHeight="120">']
    svg = ['<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 120 120" width="240" height="240">']
    for s in shapes:
        kind, d, color = s[0], s[1], s[2]
        if kind == "gill":
            rot, cx, cy = s[3]
            vd += [f'    <group android:rotation="{rot}" android:pivotX="{cx}" android:pivotY="{cy}">',
                   f'        <path android:fillColor="{color}" android:pathData="{d}"/>', '    </group>']
            svg.append(f'<path fill="{color}" d="{d}" transform="rotate({rot} {cx} {cy})"/>')
        elif kind == "fill":
            vd.append(f'    <path android:fillColor="{color}" android:pathData="{d}"/>')
            svg.append(f'<path fill="{color}" d="{d}"/>')
        else:
            w = s[3]
            vd.append(f'    <path android:strokeColor="{color}" android:strokeWidth="{w}" '
                      f'android:strokeLineCap="round" android:strokeLineJoin="round" android:pathData="{d}"/>')
            svg.append(f'<path fill="none" stroke="{color}" stroke-width="{w}" stroke-linecap="round" '
                       f'stroke-linejoin="round" d="{d}"/>')
    vd.append('</vector>\n'); svg.append('</svg>\n')
    open(os.path.join(res, f"axo_{name}.xml"), "w").write("\n".join(vd))
    open(os.path.join(prev, f"axo_{name}.svg"), "w").write("\n".join(svg))
print("ok", list(moods))
