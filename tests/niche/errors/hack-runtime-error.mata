# The steps that worked are printed, then the normal runtime error (exit 70).
unit hp = 40
unit dmg = 15
hack hp / (dmg - 15)
# expect: [Pod 042] Hack: hp / (dmg - 15)
# expect: Grouping: (/ hp (group (- dmg 15.0)))
# expect: Steps:
# expect: 1. dmg - 15 -> 15 - 15 = 0
# expect runtime error: [line 4] Runtime error: Division by zero.
