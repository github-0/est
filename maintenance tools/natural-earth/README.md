# Natural Earth data for the Europe map

Input for `../europe_map_geo.py`, kept here so the map can be regenerated without
downloading anything. The app never reads these files.

| File | Contents |
|---|---|
| `land-50m.json` | Land polygons, Natural Earth 1:50m, as TopoJSON |
| `countries-50m.json` | Country polygons, Natural Earth 1:50m, as TopoJSON (used only for the borders output) |

**Source:** npm package [`world-atlas`](https://github.com/topojson/world-atlas) **2.0.2**, which
converts [Natural Earth](https://www.naturalearthdata.com/) data to TopoJSON. Downloaded 2026-09-26:

    curl -sSLO https://cdn.jsdelivr.net/npm/world-atlas@2.0.2/land-50m.json
    curl -sSLO https://cdn.jsdelivr.net/npm/world-atlas@2.0.2/countries-50m.json

SHA-256:

    619477ff690c086885e45cb91707d783805561bd75ae8e437b7d4694b0204e0f  land-50m.json
    04342cdc1e3016bcd7db1630de95684d67b79fe3c8c460321e87aef469502394  countries-50m.json

**License:** Natural Earth data is public domain; `world-atlas` is ISC licensed.

These files reproduce the current `EuropeMap.landPath` exactly. If you replace them with a newer
release, the coastline will change slightly: replace the whole `landPath` then, not just some rings.
