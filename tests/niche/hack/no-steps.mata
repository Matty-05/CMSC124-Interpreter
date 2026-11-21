# A single value has nothing to compute; -5 = -5 is left out as well.
unit hp = 40
hack hp
# expect: [Pod 042] Hack: hp
# expect: Grouping: hp
# expect: Steps: none
# expect: Result: 40
hack -5
# expect: [Pod 042] Hack: -5
# expect: Grouping: (- 5.0)
# expect: Steps: none
# expect: Result: -5
