# Strings are written back as Nier literals, escapes included, so each
# hack line stays one valid line. Result prints the value like report does.
hack "a\"b" + "!"
# expect: [Pod 042] Hack: "a\"b" + "!"
# expect: Grouping: (+ "a\"b" "!")
# expect: Steps:
# expect: 1. "a\"b" + "!" = "a\"b!"
# expect: Result: a"b!
hack "one\ntwo" + ""
# expect: [Pod 042] Hack: "one\ntwo" + ""
# expect: Grouping: (+ "one\ntwo" "")
# expect: Steps:
# expect: 1. "one\ntwo" + "" = "one\ntwo"
# expect: Result: one
# expect: two
hack "\t" == "\t"
# expect: [Pod 042] Hack: "\t" == "\t"
# expect: Grouping: (== "\t" "\t")
# expect: Steps:
# expect: 1. "\t" == "\t" = active
# expect: Result: active
hack "back\\slash"
# expect: [Pod 042] Hack: "back\\slash"
# expect: Grouping: "back\\slash"
# expect: Steps: none
# expect: Result: back\slash
