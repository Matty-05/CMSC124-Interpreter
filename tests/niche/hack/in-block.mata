# hack is a statement, so it works inside blocks and loops and sees local units.
for (unit i = 1, i <= 2, i = i + 1) {
    hack i * 10
}
# expect: [Pod 042] Hack: i * 10
# expect: Grouping: (* i 10.0)
# expect: Steps:
# expect: 1. i * 10 -> 1 * 10 = 10
# expect: Result: 10
# expect: [Pod 042] Hack: i * 10
# expect: Grouping: (* i 10.0)
# expect: Steps:
# expect: 1. i * 10 -> 2 * 10 = 20
# expect: Result: 20
