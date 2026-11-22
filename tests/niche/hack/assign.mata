# An assignment step shows the old and new value; the change sticks afterwards.
unit hp = 40
hack hp = hp - 10
# expect: [Pod 042] Hack: hp = hp - 10
# expect: Grouping: (= hp (- hp 10.0))
# expect: Steps:
# expect: 1. hp - 10 -> 40 - 10 = 30
# expect: 2. hp = 30 (hp was 40, now 30)
# expect: Result: 30
report hp # expect: 30
