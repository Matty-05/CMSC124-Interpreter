unit hp = 100
hp = 90
hp = 80
hp = 70
hp = 60
hp = 50
scan hp
# expect: [Pod 042] Scan: hp = 50
# expect: line 2: 90 (90)
# expect: line 3: 80 (80)
# expect: line 4: 70 (70)
# expect: line 5: 60 (60)
# expect: line 6: 50 (50)
scan all hp
# expect: [Pod 042] Scan: hp = 50
# expect: line 1: 100 (100)
# expect: line 2: 90 (90)
# expect: line 3: 80 (80)
# expect: line 4: 70 (70)
# expect: line 5: 60 (60)
# expect: line 6: 50 (50)
