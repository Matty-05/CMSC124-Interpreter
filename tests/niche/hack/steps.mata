# Each operation is shown innermost first, with names swapped for their values.
unit hp = 40
unit dmg = 15
hack hp - dmg * 2 > 0
# expect: [Pod 042] Hack: hp - dmg * 2 > 0
# expect: Grouping: (> (- hp (* dmg 2.0)) 0.0)
# expect: Steps:
# expect: 1. dmg * 2 -> 15 * 2 = 30
# expect: 2. hp - 30 -> 40 - 30 = 10
# expect: 3. 10 > 0 = active
# expect: Result: active
