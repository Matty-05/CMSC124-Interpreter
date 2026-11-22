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
# expect: [Pod 042] hp: 60 -> 100
# expect: [Pod 042] shield: 5 -> void
# expect: [Pod 042] armor: 10 -> <undefined>
restore "ready"
report hp # expect: 100
report shield # expect: void
scan hp
# expect: [Pod 042] hp: <undefined> -> 100
# expect: [Pod 042] hp: 100 -> 60
# expect: [Pod 042] hp: 60 -> 100
