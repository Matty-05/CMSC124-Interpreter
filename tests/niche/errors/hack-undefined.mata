# A failing first operand prints the header and no steps.
hack missing + 1
# expect: [Pod 042] Hack: missing + 1
# expect: Grouping: (+ missing 1.0)
# expect: Steps: none
# expect runtime error: Undefined variable 'missing'.
