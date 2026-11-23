# Parentheses change the grouping, so the steps run in a different order.
hack (1 + 2) * 3
# expect: [Pod 042] Hack: (1 + 2) * 3
# expect: Grouping: (* (group (+ 1.0 2.0)) 3.0)
# expect: Steps:
# expect: 1. 1 + 2 = 3
# expect: 2. 3 * 3 = 9
# expect: Result: 9
