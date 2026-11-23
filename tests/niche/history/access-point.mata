unit hp = 100
unit shield
access_point "ready"
hp = 60
shield = 5
unit armor = 10
scan "ready"
# expect: [Pod 042] hp: 100 -> 60
# expect: [Pod 042] shield: void -> 5
# expect: [Pod 042] armor: <undefined> -> 10
report hp # expect: 60
restore "ready"
# expect: [Pod 042] Restoring "ready":
# expect: [Pod 042] hp: 60 -> 100
# expect: [Pod 042] shield: 5 -> void
# expect: [Pod 042] armor: 10 -> <undefined>
restore "ready"
# expect: [Pod 042] Restoring "ready":
report hp # expect: 100
report shield # expect: void
scan hp
# expect: [Pod 042] Scan: hp = 100
# expect: line 1: 100 (100)
# expect: line 4: 60 (60)
# expect: line 12: 100 (restore "ready")
