unit hp = 30
hack not (hp == 30)
# expect: [Pod 042] Hack: not (hp == 30)
# expect: Grouping: (not (group (== hp 30.0)))
# expect: Steps:
# expect: 1. hp == 30 -> 30 == 30 = active
# expect: 2. not active = inactive
# expect: Result: inactive
