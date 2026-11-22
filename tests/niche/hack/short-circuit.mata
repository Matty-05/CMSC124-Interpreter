# and/or stop early like normal evaluation, so the right side is never run.
hack inactive and undefined_name
# expect: [Pod 042] Hack: inactive and undefined_name
# expect: Grouping: (and false undefined_name)
# expect: Steps:
# expect: 1. inactive and undefined_name = inactive (right side skipped)
# expect: Result: inactive
hack 1 > 2 or active
# expect: [Pod 042] Hack: 1 > 2 or active
# expect: Grouping: (or (> 1.0 2.0) true)
# expect: Steps:
# expect: 1. 1 > 2 = inactive
# expect: 2. inactive or active = active
# expect: Result: active
