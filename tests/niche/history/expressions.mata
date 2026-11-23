unit hp = 40 + 10
hp = hp - 20
hack hp = hp + 5
# expect: [Pod 042] Hack: hp = hp + 5
# expect: Grouping: (= hp (+ hp 5.0))
# expect: Steps:
# expect: 1. hp + 5 -> 30 + 5 = 35
# expect: 2. hp = 35 (hp was 30, now 35)
# expect: Result: 35
scan hp
# expect: [Pod 042] Scan: hp = 35
# expect: line 1: 50 (40 + 10)
# expect: line 2: 30 (hp - 20)
# expect: line 3: 35 (hp + 5)
