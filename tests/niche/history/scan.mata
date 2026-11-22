unit hp = 100
hp = 90
hp = 80
hp = 70
hp = 60
hp = 50
scan hp
# expect: [Pod 042] hp: 100 -> 90
# expect: [Pod 042] hp: 90 -> 80
# expect: [Pod 042] hp: 80 -> 70
# expect: [Pod 042] hp: 70 -> 60
# expect: [Pod 042] hp: 60 -> 50
scan all hp
# expect: [Pod 042] hp: <undefined> -> 100
# expect: [Pod 042] hp: 100 -> 90
# expect: [Pod 042] hp: 90 -> 80
# expect: [Pod 042] hp: 80 -> 70
# expect: [Pod 042] hp: 70 -> 60
# expect: [Pod 042] hp: 60 -> 50
