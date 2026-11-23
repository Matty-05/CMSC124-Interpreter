# A call is one step; the protocol body runs normally, so its output comes first.
protocol double(x) {
    report "doubling"
    transmit x * 2
}
unit hp = 30
hack double(hp) + 1
# expect: [Pod 042] Hack: double(hp) + 1
# expect: Grouping: (+ (call double hp) 1.0)
# expect: doubling
# expect: Steps:
# expect: 1. double(hp) -> double(30) = 60
# expect: 2. 60 + 1 = 61
# expect: Result: 61
