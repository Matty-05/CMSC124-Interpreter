# Steps keep quotes on strings; the result prints like report.
unit name = "Pod"
hack name + " 042"
# expect: [Pod 042] Hack: name + " 042"
# expect: Grouping: (+ name  042)
# expect: Steps:
# expect: 1. name + " 042" -> "Pod" + " 042" = "Pod 042"
# expect: Result: Pod 042
